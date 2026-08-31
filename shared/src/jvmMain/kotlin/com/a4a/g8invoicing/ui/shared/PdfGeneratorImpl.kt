package com.a4a.g8invoicing.ui.shared

import com.a4a.g8invoicing.data.AppLocaleHolder
import com.a4a.g8invoicing.data.formatAmount
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.ui.screens.shared.getLinkedDeliveryNotes
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.DocumentTotalPrices
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfName
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.kernel.pdf.filespec.PdfFileSpec
import com.itextpdf.kernel.xmp.XMPMetaFactory
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.IPropertyContainer
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Link
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Tab
import com.itextpdf.layout.element.TabStop
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TabAlignment
import com.itextpdf.layout.font.FontProvider
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.Property
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import com.itextpdf.layout.renderer.CellRenderer
import com.itextpdf.layout.renderer.DrawContext
import java.io.File

/**
 * Shared PDF generation implementation for JVM platforms (Android & Desktop).
 * This contains all the iText-based PDF generation logic.
 */
class PdfGeneratorImpl(
    defaultStrings: PdfStrings,
    private val fileManager: PdfFileManager,
    private val imageStorage: ImageStorage? = null
) {
    // Effective labels for the current PDF. Starts at the app-locale defaults
    // passed to the ctor and is overridden per doc in generatePdf() so a doc
    // created in French exports with French labels even after the user
    // switches the app to English — mirrors the labelsSnapshot mechanism the
    // in-app preview already uses via documentLabel().
    private var strings: PdfStrings = defaultStrings
    private val defaultStrings: PdfStrings = defaultStrings
    private companion object {
        // Primary family name. The FontProvider matches the embedded
        // helvetica.ttf / helveticabold.ttf; unknown-glyph runs fall through
        // to whichever registered font covers them.
        const val FONT_FAMILY = "Helvetica"

        // Right edge of the totals block, in points from the start of the
        // paragraph. Amounts right-align there; the label's right edge is
        // computed per document from the widest actual amount so labels sit
        // right next to their amounts (no fixed gap when amounts are short).
        const val PRICES_AMOUNT_RIGHT = 230f
        // Breathing room between the ":" at the end of the label and the
        // first digit of the amount. Compose preview looks tight at ~4px but
        // the PDF needs more because per-glyph width measurement of a few
        // currency symbols missing from helvetica.ttf (₪ ₼ ₽ ₾) is estimated,
        // not exact — the buffer absorbs any under-estimation.
        const val PRICES_LABEL_AMOUNT_GAP = 12f
    }

    fun generatePdf(document: DocumentState): String {
        // Freeze the label locale to what the doc had at creation.
        // labelsSnapshot is a JSON Map<String,String>; when present, its
        // entries override the default (app-current) PdfStrings values.
        // Legacy docs without a snapshot keep the current-locale behaviour.
        strings = effectiveStrings(document, defaultStrings)

        // Sanitize the document number for the temp filename: users type
        // things like "F/2026" as their invoice number, and the `/` breaks
        // File(cacheDir, name) since it treats it as a subdirectory.
        val tempFileName = "${sanitizeForFileName(document.documentNumber.text).ifBlank { "document" }}_temp.pdf"
        val finalFileName = buildFinalFileName(document)
        val tempFilePath = fileManager.getTempFilePath(tempFileName)

        // Delete temp file if exists
        File(tempFilePath).delete()

        // Create PDF
        val writer = PdfWriter(tempFilePath)
        val pdfDocument = PdfDocument(writer)

        val doc = Document(pdfDocument, PageSize.A4)
        doc.fontProvider = buildFontProvider()
        doc.setProperty(Property.FONT, arrayOf(FONT_FAMILY))
        doc.setFontSize(9.5F)

        // Add content
        buildPdfContent(doc, document)

        doc.close()
        writer.close()
        pdfDocument.close()

        // Add page numbering
        return addPageNumbering(document, tempFileName, finalFileName)
    }

    /**
     * Factur-X 1.0 (EN 16931) export. Renders the same visual PDF as
     * [generatePdf] then embeds the CII XML as an associated file with
     * AFRelationship=Data and the file name `factur-x.xml` — the naming
     * convention the Factur-X spec requires for consumers to locate the
     * structured payload. The XMP metadata is extended with the Factur-X
     * schema so PDF/A-3 aware validators pick up the conformance level.
     *
     * Note: strict PDF/A-3 conformance is NOT enforced here (no
     * PdfADocument + ICC profile). We ship a "hybrid" PDF with the
     * embedded factur-x.xml + AFRelationship + XMP schema — enough for
     * downstream platforms that look up the payload via /AF, which covers
     * the pragmatic use case for 1.9. A follow-up can switch to
     * PdfADocument for stricter validators.
     */
    fun generateFacturX(document: DocumentState, xmlBytes: ByteArray): String {
        strings = effectiveStrings(document, defaultStrings)

        val tempFileName = "${sanitizeForFileName(document.documentNumber.text).ifBlank { "document" }}_facturx_temp.pdf"
        val finalFileName = buildFacturXFinalFileName(document)
        val tempFilePath = fileManager.getTempFilePath(tempFileName)

        File(tempFilePath).delete()

        val writer = PdfWriter(tempFilePath)
        val pdfDocument = PdfDocument(writer)

        val doc = Document(pdfDocument, PageSize.A4)
        doc.fontProvider = buildFontProvider()
        doc.setProperty(Property.FONT, arrayOf(FONT_FAMILY))
        doc.setFontSize(9.5F)

        buildPdfContent(doc, document)

        doc.close()
        writer.close()
        pdfDocument.close()

        return addPageNumbering(document, tempFileName, finalFileName, xmlBytes)
    }

    private fun buildFacturXFinalFileName(document: DocumentState): String {
        val docNumber = sanitizeForFileName(document.documentNumber.text).ifBlank { "document" }
        val date = formatDateForFileName(document.documentDate)
        val client = buildClientNameForFileName(document.documentClient)
        return listOfNotNull(docNumber, date, client).joinToString("-") + "-facturx.pdf"
    }

    /**
     * Filename format: `DocumentNumber-YYYY.MM.DD-Client-Name.pdf` (all-dash, no spaces).
     * Falls back gracefully when the date is unparseable or the client is missing —
     * both parts are skipped instead of leaving empty segments, so the worst case
     * still yields `DocumentNumber.pdf`.
     */
    private fun buildFinalFileName(document: DocumentState): String {
        val docNumber = sanitizeForFileName(document.documentNumber.text).ifBlank { "document" }
        val date = formatDateForFileName(document.documentDate)
        val client = buildClientNameForFileName(document.documentClient)
        return listOfNotNull(docNumber, date, client).joinToString("-") + ".pdf"
    }

    private fun formatDateForFileName(rawDate: String): String? {
        // Stored as "dd/MM/yyyy" (optionally followed by a time), see DateUtils.
        val parts = rawDate.substringBefore(" ").split("/")
        if (parts.size != 3) return null
        val (dd, mm, yyyy) = parts
        if (dd.length != 2 || mm.length != 2 || yyyy.length != 4) return null
        return "$yyyy.$mm.$dd"
    }

    private fun buildClientNameForFileName(client: ClientOrIssuerState?): String? {
        if (client == null) return null
        val first = client.firstName?.text?.trim().orEmpty()
        val last = client.name.text.trim()

        // Cap the client segment so pathological names (users have typed 300+ char
        // "names") don't push the filename past MediaStore's ~255-byte limit — which
        // makes resolver.insert() silently return null and the exporter treat it as
        // success. Rules: the first name is always reduced to its initial ("Last F");
        // if the last name alone still exceeds 30 chars, truncate it and drop the
        // initial. No trailing dot — a segment ending in "." would render as
        // "…F..pdf" once the extension is appended.
        val display: String = when {
            last.length > 30 -> last.take(30)
            first.isNotEmpty() && last.isNotEmpty() -> "$last ${first.take(1)}"
            last.isNotEmpty() -> last
            else -> first
        }

        return sanitizeForFileName(display.uppercase()).ifEmpty { null }
    }

    /**
     * Replace every whitespace run with a single dash and strip filesystem-hostile
     * chars (path separators + Windows reserved). Keeps the filename readable even
     * for names like "Dédé de la Panouse" → "Dédé-de-la-Panouse".
     */
    private fun sanitizeForFileName(value: String): String {
        return value.trim()
            .replace(Regex("""[/\\:*?"<>|]"""), "")
            .replace(Regex("""\s+"""), "-")
    }

    /**
     * Font stack for the whole PDF. The primary family is Helvetica (embedded
     * from assets to get access to symbols like ₹ that WinAnsi lacks). We then
     * pile every readable system font on top so iText's FontSelector can
     * character-by-character fall back to whichever font covers each glyph —
     * this is what makes exotic currency symbols (৳ ֏ ₽ د.إ …) and any
     * user-typed content (CJK names, emoji in a footer) render instead of
     * disappearing.
     *
     * Registration order doesn't matter: FontSelector picks by family+coverage,
     * not order. We silently swallow per-font failures because a few system
     * .ttc entries (colour emoji, some CJK collections) trip up iText's parser
     * and one bad file must not sink the whole PDF.
     */
    private fun buildFontProvider(): FontProvider {
        val provider = FontProvider()
        fun addBytes(name: String) {
            try {
                fileManager.loadAssetBytes(name)?.let { provider.addFont(it) }
            } catch (_: Throwable) { }
        }
        addBytes("helvetica.ttf")
        addBytes("helveticabold.ttf")
        // Always keep the standard 14 available as a last-resort fallback: even
        // if every asset+system add above fails, the PDF still renders ASCII.
        provider.addStandardPdfFonts()
        fileManager.listSystemFontFiles().forEach { path ->
            try { provider.addFont(path) } catch (_: Throwable) { }
        }
        return provider
    }

    private fun buildPdfContent(
        doc: Document,
        document: DocumentState
    ) {
        val titleFontSize = 20F
        val dateFontSize = 16F
        val fontSize = 9.5F
        val currencyCodeForHeader = document.currency.text.ifEmpty { "EUR" }
        val showCurrencyNoticeLine = document.showCurrencyAndAutoTaxColumn && currencyCodeForHeader != "EUR"

        // Header with Logo and Title/Date
        val logoPath = document.documentIssuer?.logoPath
        if (logoPath != null && imageStorage != null) {
            doc.add(createLogoAndTitleTable(
                logoPath = logoPath,
                documentNumber = document.documentNumber.text,
                documentType = document.documentType,
                documentDate = document.documentDate.substringBefore(" "),
                titleFontSize = titleFontSize,
                dateFontSize = dateFontSize,
            ))
        } else {
            // Title (no logo)
            doc.add(createTitle(document.documentNumber.text, document.documentType, titleFontSize))
            // Date
            doc.add(createDate(document.documentDate.substringBefore(" "), dateFontSize))
        }

        // Issuer and Client
        doc.add(createIssuerAndClientTable(document.documentIssuer, document.documentClient, fontSize))

        // Reference
        document.reference?.text?.takeIf { it.isNotEmpty() }?.let {
            doc.add(createReference(it).setMarginTop(4F))
        }

        // Free field
        document.freeField?.text?.takeIf { it.isNotEmpty() }?.let {
            val marginTop = if (document.reference?.text.isNullOrEmpty()) 10F else 2F
            doc.add(createFreeText(it).setMarginTop(marginTop))
        }

        val currencyCode = document.currency.text.ifEmpty { "EUR" }
        // Freeze the formatting locale to whatever was persisted on the doc at
        // creation. null on pre-feature legacy docs → formatAmount falls back
        // to the current app language (same behaviour as before the freeze
        // feature landed).
        val formatLocale = document.formatLocale

        // Products table — post-1.8 docs (marked by showCurrencyAndAutoTaxColumn) hide
        // the tax column when no line carries a rate; legacy docs always show
        // it so a re-export of an old invoice looks identical to the original.
        document.documentProducts?.let { products ->
            val displayTaxColumn = !document.showCurrencyAndAutoTaxColumn ||
                products.any { it.taxRate != null }
            val hideLinkedHeaders = (document as? InvoiceState)?.hideLinkedSourceHeaders == true
            createProductsTable(products, currencyCode, formatLocale, displayTaxColumn, hideLinkedHeaders)?.let {
                val marginTop = if (document.reference?.text.isNullOrEmpty() && document.freeField?.text.isNullOrEmpty()) 20f else 10f
                doc.add(it.setMarginTop(marginTop).setMarginBottom(12f))
            }
        }

        // Prices
        document.documentTotalPrices?.let {
            doc.add(createPrices(it, fontSize, currencyCode, formatLocale))
        }

        if (showCurrencyNoticeLine) {
            doc.add(createCurrencyNotice(currencyCodeForHeader))
        }

        // BT-120 legal mention — mirrors DocumentBasicTemplateFooter: only
        // rendered when the issuer is in franchise en base AND the user has
        // typed a wording in the text menu. Right-aligned, small grey.
        val vatExemptionMention = if (document.documentIssuer?.vatExempt == true) {
            document.vatExemptionText?.text?.trim()?.takeIf { it.isNotEmpty() }
        } else null
        if (vatExemptionMention != null) {
            doc.add(
                Paragraph(vatExemptionMention)
                    .setFontSize(fontSize)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(10f)
                    .setFixedLeading(10F)
            )
        }

        // Grey box grouping "À régler avant le X" + payment means + IBAN/BIC.
        // Same conditionals as the Compose preview: skip whole box if user
        // hid both blocks OR neither is populated.
        val paymentMeansStr = paymentMeansDisplayFor(document)
        val bankStr = bankRenderedFor(document)
        val showPaymentBox = paymentMeansStr != null || bankStr != null
        if (showPaymentBox) {
            doc.add(createPaymentBox(document, paymentMeansStr, bankStr, fontSize))
        }

        // Bottom band under a hairline: terms → footer text → watermark. The
        // separator only draws when at least one of the three sits below it,
        // matching the preview.
        // BT-20 concat of the 3 subject-coded fields (PMT / PMD / AAB), joined
        // with a single space so the 3 sentences read as one flowing paragraph
        // rather than a stack of 3 lines. Empty fields drop.
        val paymentTerms = (document as? InvoiceState)?.let { inv ->
            listOf(
                inv.paymentTermsRecoveryFees.text.trim(),
                inv.paymentTermsLateFees.text.trim(),
                inv.paymentTermsDiscount.text.trim(),
            ).filter { it.isNotEmpty() }.joinToString(" ").takeIf { it.isNotEmpty() }
        }
        val footerText = document.footerText.text.trim().takeIf { it.isNotEmpty() }
        val watermarkText = document.watermarkText?.takeIf { it.isNotBlank() }
        if (paymentTerms != null || footerText != null || watermarkText != null) {
            doc.add(createSeparator(topMargin = 20f))
            if (paymentTerms != null) doc.add(createPaymentTermsBlock(paymentTerms, fontSize))
            if (footerText != null) doc.add(createFooter(footerText, fontSize, paymentTerms != null))
            if (watermarkText != null) doc.add(createWatermark(watermarkText))
        }

        // "Paid" stamp — absolute-positioned via setFixedPosition inside
        // createPaidStamp, so it doesn't push anything down. Added last so it
        // paints on top of the products / totals / footer content.
        if (document is InvoiceState && document.paymentStatus == 2) {
            createPaidStamp(document.formatLocale)?.let { doc.add(it) }
        }
    }

    private fun addPageNumbering(
        document: DocumentState,
        tempFileName: String,
        finalFileName: String,
        facturxXmlBytes: ByteArray? = null,
    ): String {
        // No local fontRegular here anymore — the doc opened below sets
        // `fontProvider = buildFontProvider()` so every Paragraph resolves its
        // font through the provider (needed for currency glyphs that WinAnsi
        // Helvetica doesn't cover).

        val tempFilePath = fileManager.getTempFilePath(tempFileName)
        val finalTempPath = fileManager.getTempFilePath(finalFileName)

        File(finalTempPath).delete()

        try {
            val pdfDoc = PdfDocument(PdfReader(tempFilePath), PdfWriter(finalTempPath))
            val doc = Document(pdfDoc)
            doc.fontProvider = buildFontProvider()
            doc.setProperty(Property.FONT, arrayOf(FONT_FAMILY))
            val numberOfPages = pdfDoc.numberOfPages

            if (numberOfPages > 1) {
                for (i in 1..numberOfPages) {
                    val prefix = if (i == 1) "" else "${getDocumentTypeName(document.documentType, strings)} ${document.documentNumber.text} - "
                    doc.showTextAligned(
                        Paragraph("$prefix$i/$numberOfPages"),
                        570f, 34f, i, TextAlignment.RIGHT, VerticalAlignment.TOP, 0f
                    )
                }
            }

            if (facturxXmlBytes != null) {
                attachFacturXPayload(pdfDoc, facturxXmlBytes)
            }

            doc.close()
            pdfDoc.close()
            fileManager.deleteTempFile(tempFilePath)

            // Save to final location
            fileManager.saveToFinalLocation(finalTempPath, finalFileName)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return finalFileName
    }

    /**
     * Attach the Factur-X CII XML to the given PDF as an Associated File
     * (/AF, AFRelationship = Data) named `factur-x.xml`, and extend the XMP
     * metadata with the Factur-X namespace so PDF/A-3 aware readers pick
     * up the profile. Fails silently if XMP construction hits an unexpected
     * error — the file attachment still lands, which is the load-bearing
     * part for consumers that don't parse XMP.
     */
    private fun attachFacturXPayload(pdfDoc: PdfDocument, xmlBytes: ByteArray) {
        val fileSpec = PdfFileSpec.createEmbeddedFileSpec(
            pdfDoc,
            xmlBytes,
            "Factur-X invoice",
            "factur-x.xml",
            PdfName("text/xml"),
            null,
            PdfName("Data"),
        )
        pdfDoc.addAssociatedFile("factur-x.xml", fileSpec)

        try {
            val xmpMeta = pdfDoc.xmpMetadata ?: XMPMetaFactory.create()
            val fxNs = "urn:factur-x:pdfa:CrossIndustryDocument:invoice:1p0#"
            xmpMeta.setProperty(fxNs, "DocumentType", "INVOICE")
            xmpMeta.setProperty(fxNs, "DocumentFileName", "factur-x.xml")
            xmpMeta.setProperty(fxNs, "Version", "1.0")
            xmpMeta.setProperty(fxNs, "ConformanceLevel", "EXTENDED")
            pdfDoc.xmpMetadata = xmpMeta
        } catch (_: Throwable) {
            // XMP is a nice-to-have. The AF entry alone is enough for most
            // e-invoicing platforms to locate the payload.
        }
    }

    private fun createLogoAndTitleTable(
        logoPath: String,
        documentNumber: String,
        documentType: DocumentType?,
        documentDate: String,
        titleFontSize: Float,
        dateFontSize: Float,
    ): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
            .useAllAvailableWidth()
            .setMarginBottom(24F)

        // Title and Date cell (left)
        val titleCell = Cell().setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.LEFT)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)

        val title = documentType?.let { getDocumentTypeName(it, strings) } ?: ""
        titleCell.add(
            Paragraph(title + " " + documentNumber)
                .pdfBold()
                .setFontSize(titleFontSize)
                .setMarginBottom(-2F)
        )

        val dateLabel = strings.documentDate.trimEnd() + " "
        titleCell.add(
            Paragraph("$dateLabel$documentDate")
                .pdfBold()
                .setFontSize(dateFontSize)
        )

        table.addCell(titleCell)

        // Logo cell (right)
        val logoCell = Cell().setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.RIGHT)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
        try {
            val absoluteLogoPath = imageStorage?.getAbsolutePath(logoPath)
            if (absoluteLogoPath != null && File(absoluteLogoPath).exists()) {
                val imageData = ImageDataFactory.create(absoluteLogoPath)
                val logoImage = Image(imageData)
                    .setMaxHeight(80f)
                    .setMaxWidth(200f)
                    // TextAlignment.RIGHT on the cell only affects text — for a block
                    // Image element we need to opt into right alignment explicitly.
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                logoCell.add(logoImage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        table.addCell(logoCell)

        return table
    }

    private fun createTitle(documentNumber: String, documentType: DocumentType?, fontSize: Float): Paragraph {
        val title = documentType?.let { getDocumentTypeName(it, strings) } ?: ""
        return Paragraph(title + " " + documentNumber)
            .pdfBold()
            .setFontSize(fontSize)
            .setMarginBottom(-2F)
    }

    private fun createDate(date: String, fontSize: Float): Paragraph {
        val dateLabel = strings.documentDate.trimEnd() + " "
        return Paragraph("$dateLabel$date")
            .pdfBold()
            .setFontSize(fontSize)
            .setMarginBottom(24F)
    }

    // "Devise : USD" (or the localised equivalent) rendered just above the
    // Payment-due-date line at the bottom of invoices for documents where
    // showCurrencyAndAutoTaxColumn is set on the doc and the currency isn't EUR. Same weight
    // and size as the due date so the two lines read as a coherent pair.
    private fun createCurrencyNotice(currencyCode: String): Paragraph {
        val labelPattern = strings.currencyNoticeLabel
        val text = if (labelPattern.contains("%1\$s")) {
            labelPattern.replace("%1\$s", currencyCode)
        } else {
            "$labelPattern $currencyCode"
        }
        return Paragraph(text)
            .setFontSize(8F)
            .setFontColor(ColorConstants.DARK_GRAY)
            .setTextAlignment(TextAlignment.RIGHT)
            .setFixedLeading(10F)
            .setPaddingTop(5f)
            .setPaddingRight(3f)
    }

    private fun createIssuerAndClientTable(
        issuer: ClientOrIssuerState?,
        client: ClientOrIssuerState?,
        fontSize: Float
    ): Table {
        val table = Table(2).useAllAvailableWidth().setFixedLayout()

        // Client address title
        client?.addresses?.let { addresses ->
            table.addCell(Cell().setBorder(Border.NO_BORDER))
            table.addCell(createAddressTitle(addresses, 0, fontSize))
            table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(1f))
            table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(1f))
        }

        // Issuer. Always add a cell — even empty — to reserve the left column.
        // Otherwise iText's fixed 2-column layout drops the following client cell
        // into column 0 (left) instead of column 1 (right), and every subsequent
        // additional-address cell cascades to the wrong side too.
        val issuerCell = Cell().setBorder(Border.NO_BORDER)
        issuer?.let {
            createClientOrIssuerParagraph(it, fontSize = fontSize).forEach { p -> issuerCell.add(p) }
        }
        table.addCell(issuerCell)

        // Client
        client?.let {
            table.addCell(
                createClientRectangleAndContent(createClientOrIssuerParagraph(it, fontSize = fontSize))
                    .setPaddingBottom(8f)
            )
            table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(6f))
            table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(6f))

            // Additional addresses (2 and/or 3)
            it.addresses?.let { addresses ->
                if (addresses.size > 1) {
                    // Row for address titles
                    // When there are only 2 addresses, put address 2 on the right (below address 1)
                    if (addresses.size == 2) {
                        table.addCell(Cell().setBorder(Border.NO_BORDER))
                    }
                    for (i in 1..<addresses.size) {
                        table.addCell(createAddressTitle(addresses, i, fontSize))
                    }

                    // Spacer row
                    table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(1f))
                    table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(1f))

                    // Row for address contents
                    if (addresses.size == 2) {
                        table.addCell(Cell().setBorder(Border.NO_BORDER))
                    }
                    for (i in 1..<addresses.size) {
                        table.addCell(
                            createClientRectangleAndContent(
                                createClientOrIssuerParagraph(client, displayAllInfo = false, addressIndex = i, fontSize = fontSize)
                            )
                        )
                    }
                }
            }
        }

        table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(25f))
        table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(25f))

        return table
    }

    private fun createAddressTitle(addresses: List<AddressState>, index: Int, fontSize: Float): Cell {
        val addressTitle = addresses.getOrNull(index)?.addressTitle?.text
        // Show "Addressed to" by default when there's only one address and no custom title
        val title = if (addresses.size == 1 && index == 0 && addressTitle.isNullOrEmpty()) {
            strings.addressedTo
        } else {
            addressTitle ?: ""
        }
        return Cell()
            .setBorder(Border.NO_BORDER)
            .setPaddingTop(8f)
            .add(Paragraph(title).setFontColor(ColorConstants.DARK_GRAY).setFixedLeading(6F).setFontSize(fontSize))
            .setTextAlignment(TextAlignment.CENTER)
    }

    private fun createClientRectangleAndContent(content: List<Paragraph>): Cell {
        // RoundedCellRenderer draws the visible border 2.5pt inside the cell edges, so
        // the effective inner gap is (padding − 2.5)pt. 15pt cell padding gives ~12.5pt
        // of visible breathing room, matching the 10.dp used by the in-app preview and
        // keeping long client names clear of the rounded border.
        val cell = Cell()
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.CENTER)
            .setPaddingBottom(8f)
            .setPaddingLeft(15f)
            .setPaddingRight(15f)
        content.forEach { cell.add(it) }
        cell.setNextRenderer(RoundedCellRenderer(cell, ColorConstants.LIGHT_GRAY, false))
        return cell
    }

    private fun createClientOrIssuerParagraph(
        clientOrIssuer: ClientOrIssuerState?,
        displayAllInfo: Boolean = true,
        addressIndex: Int = 0,
        fontSize: Float
    ): MutableList<Paragraph> {
        val result = mutableListOf<Paragraph>()
        val address = clientOrIssuer?.addresses?.getOrNull(addressIndex)

        val nameAndAddress = Paragraph()
            .setFixedLeading(12F)
            .setPaddingTop(if (clientOrIssuer?.type == ClientOrIssuerType.DOCUMENT_CLIENT) 10f else 0f)
            .setPaddingBottom(5f)

        if (displayAllInfo) {
            clientOrIssuer?.firstName?.text?.let { nameAndAddress.add(Text("$it ")) }
            clientOrIssuer?.name?.text?.let { nameAndAddress.add(Text("$it\n")) }
        }
        result.add(nameAndAddress)

        address?.addressLine1?.text?.takeIf { it.isNotEmpty() }?.let { nameAndAddress.add("$it\n") }
        address?.addressLine2?.text?.takeIf { it.isNotEmpty() }?.let { nameAndAddress.add("$it\n") }

        val zipCodeCity = listOfNotNull(
            address?.zipCode?.text?.takeIf { it.isNotEmpty() },
            address?.city?.text?.takeIf { it.isNotEmpty() }
        ).joinToString(" ")
        if (zipCodeCity.isNotEmpty()) {
            nameAndAddress.add("$zipCodeCity\n")
        }

        if (displayAllInfo) {
            val numberAndEmail = Paragraph().setFixedLeading(12F).setPaddingBottom(5f)
            clientOrIssuer?.phone?.text?.let { numberAndEmail.add(Text("$it\n")) }
            clientOrIssuer?.emails?.forEach { emailState ->
                if (emailState.email.text.isNotEmpty()) {
                    numberAndEmail.add(Text("${emailState.email.text}\n"))
                }
            }
            result.add(numberAndEmail)

            val companyInfo = Paragraph().setFixedLeading(12F).setPaddingBottom(4f)
            clientOrIssuer?.companyId1Number?.text?.let {
                val label = clientOrIssuer.companyId1Label?.text?.takeUnless { it.isBlank() } ?: strings.companyId1Label
                companyInfo.add(Text("$label${strings.labelSeparator}$it\n"))
            }
            clientOrIssuer?.companyId2Number?.text?.let {
                val label = clientOrIssuer.companyId2Label?.text?.takeUnless { it.isBlank() } ?: strings.companyId2Label
                companyInfo.add(Text("$label${strings.labelSeparator}$it\n"))
            }
            clientOrIssuer?.companyId3Number?.text?.let {
                val label = clientOrIssuer.companyId3Label?.text?.takeUnless { it.isBlank() } ?: strings.companyId3Label
                companyInfo.add(Text("$label${strings.labelSeparator}$it\n"))
            }
            result.add(companyInfo)
        }

        return result
    }

    private fun createReference(text: String): Paragraph {
        val referenceLabel = strings.documentReference.trimEnd() + " "
        return Paragraph(Text(referenceLabel).pdfBold()).add(text)
    }

    private fun createFreeText(text: String): Paragraph {
        return Paragraph(text)
    }

    private fun createProductsTable(
        products: List<DocumentProductState>,
        currencyCode: String,
        formatLocale: String?,
        displayTaxColumn: Boolean,
        hideLinkedSourceHeaders: Boolean = false,
    ): Table? {
        try {
            val displayUnitColumn = products.any { !it.unit?.text.isNullOrEmpty() }

            // Description | Qty | [Unit] | [Tax rate] | Unit price HT | Total HT
            // The tax column disappears when the issuer is VAT-exempt (nothing
            // to show in it); the description column absorbs its width. Same
            // for the unit column. PU HT and Total HT stay equal-width so any
            // amount that fits one fits the other (long ISO codes like
            // "1234,56 EGP" would overflow if Total was narrower).
            val descriptionPct = when {
                displayUnitColumn && displayTaxColumn -> 40f
                displayUnitColumn && !displayTaxColumn -> 48f
                !displayUnitColumn && displayTaxColumn -> 53f
                else -> 61f
            }
            val columnWidth = buildList {
                add(descriptionPct)
                add(9f)
                if (displayUnitColumn) add(13f)
                if (displayTaxColumn) add(8f)
                add(15f)
                add(15f)
            }.toFloatArray()

            val table = Table(UnitValue.createPercentArray(columnWidth)).useAllAvailableWidth().setFixedLayout()

            // Header
            table.addCustomCell(strings.tableDescription, TextAlignment.LEFT, true)
            table.addCustomCell(strings.tableQuantity, TextAlignment.RIGHT, true)
            if (displayUnitColumn) {
                table.addCustomCell(strings.tableUnit, TextAlignment.RIGHT, true)
            }
            if (displayTaxColumn) {
                table.addCustomCell(strings.tableTaxRate, TextAlignment.RIGHT, true)
            }
            table.addCustomCell(strings.tableUnitPrice, TextAlignment.RIGHT, true)
            table.addCustomCell(strings.tableTotalPrice, TextAlignment.RIGHT, true)

            // Products
            val linkedDeliveryNotes = getLinkedDeliveryNotes(products)
            val columnCount = columnWidth.size
            if (linkedDeliveryNotes.isNotEmpty()) {
                linkedDeliveryNotes.forEach { (docNumber, docDate) ->
                    if (!hideLinkedSourceHeaders) {
                        val headerText = if (docNumber.isNullOrEmpty()) {
                            strings.otherLines
                        } else {
                            "$docNumber - ${docDate?.substringBefore(" ")}"
                        }
                        table.addCustomCell(
                            headerText,
                            TextAlignment.LEFT, true, isSpan = true, spanColumns = columnCount
                        )
                    }
                    addProductRows(products.filter { it.linkedDocNumber == docNumber }, table, displayUnitColumn, displayTaxColumn, currencyCode, formatLocale)
                }
            } else {
                addProductRows(products, table, displayUnitColumn, displayTaxColumn, currencyCode, formatLocale)
            }

            return table
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun addProductRows(
        products: List<DocumentProductState>,
        table: Table,
        displayUnitColumn: Boolean,
        displayTaxColumn: Boolean,
        currencyCode: String,
        formatLocale: String?,
    ) {
        products.forEach { product ->
            val itemName = Paragraph(Text(product.name.text)).setFixedLeading(10F)
            val spacing = Paragraph("\n").setFixedLeading(4F)
            val descriptionText = product.description?.text
            val itemDescription: Paragraph? = if (!descriptionText.isNullOrEmpty()) {
                val italicText = Text(descriptionText).simulateItalic()
                Paragraph(italicText).setFixedLeading(10F)
            } else {
                null
            }

            table.addCustomCell(paragraphs = listOfNotNull(itemName, spacing, itemDescription), alignment = TextAlignment.LEFT)
            table.addCustomCell(product.quantity.stripTrailingZeros().toPlainString().replace(".", ","))

            if (displayUnitColumn) {
                table.addCustomCell(product.unit?.text)
            }

            if (displayTaxColumn) {
                table.addCustomCell(
                    product.taxRate?.let { "${it.stripTrailingZeros().toPlainString().replace(".", ",")}%" } ?: " - "
                )
            }
            table.addCustomCell(
                product.priceWithoutTax?.let { formatAmount(it, currencyCode, formatLocale) } ?: ""
            )
            table.addCustomCell(
                product.priceWithoutTax?.let { price ->
                    formatAmount(price * product.quantity, currencyCode, formatLocale)
                } ?: ""
            )
        }
    }

    private fun createPrices(prices: DocumentTotalPrices, fontSize: Float, currencyCode: String, formatLocale: String?): Table {
        // Single-line-box layout: each row is one Paragraph whose label and
        // amount sit at their own right-aligned tab stops. One line box per row
        // means one shared baseline, which matters when iText grabs a fallback
        // font for a currency glyph the primary Helvetica doesn't cover.
        data class Line(val label: String, val amount: String, val bold: Boolean)
        val lines = buildList {
            add(Line(
                label = strings.totalWithoutTax,
                amount = prices.totalPriceWithoutTax?.let { formatAmount(it, currencyCode, formatLocale) } ?: " - ",
                bold = false,
            ))
            prices.totalAmountsOfEachTax?.sortedBy { it.first }?.forEach { (taxRate, taxAmount) ->
                add(Line(
                    label = "${strings.tax} ${taxRate.stripTrailingZeros().toPlainString().replace(".", ",")}%${strings.labelSeparator}",
                    amount = formatAmount(taxAmount, currencyCode, formatLocale),
                    bold = false,
                ))
            }
            prices.retentionAmounts.forEach { line ->
                val rateStr = "${line.rate.stripTrailingZeros().toPlainString().replace(".", ",")} %"
                add(Line(
                    label = "${line.label} $rateStr${strings.labelSeparator}",
                    amount = "− " + formatAmount(line.amount, currencyCode, formatLocale),
                    bold = false,
                ))
            }
            add(Line(
                label = strings.totalWithTax,
                amount = prices.totalPriceWithTax?.let { formatAmount(it, currencyCode, formatLocale) } ?: " - ",
                bold = true,
            ))
        }

        // Measure the widest amount so the label's right-align tab lands just
        // before it. Uses the embedded helvetica.ttf (covers €, £, ₹, ₺, ₩,
        // ₴, ₸ and everything Latin) rather than StandardFonts.HELVETICA
        // (Base14, WinAnsi encoded, has none of the currency-symbol block).
        // For the four glyphs even our embedded font misses (₪ ₼ ₽ ₾),
        // per-char measurement returns 0 → we substitute a generous 1em
        // estimate so those rare cases don't collapse the gap.
        val measurementFont = loadPricesMeasurementFont()
        val maxAmountWidth = lines.maxOf { measurePriceWidth(it.amount, measurementFont, fontSize) }
        val labelRight = PRICES_AMOUNT_RIGHT - maxAmountWidth - PRICES_LABEL_AMOUNT_GAP

        val table = Table(1)
            .setWidth(PRICES_AMOUNT_RIGHT)
            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
            .setPaddingBottom(8f)
            .setPaddingRight(4f)

        for (line in lines) {
            table.addPriceLine(line.label, line.amount, fontSize, labelRight, line.bold)
        }
        return table
    }

    // Bold rendering uses Text.simulateBold() (synthetic stroke over the same
    // font) rather than setProperty(FONT_WEIGHT). FONT_WEIGHT forces iText to
    // pick a weight-700 face that covers each glyph — the fallback face for
    // an exotic currency often differs from the label's face and the row ends
    // up in two families. With simulateBold we stay on the font that already
    // renders the row.
    private fun Table.addPriceLine(
        label: String,
        amount: String,
        fontSize: Float,
        labelRight: Float,
        bold: Boolean,
    ): Table {
        val paragraph = Paragraph()
            .addTabStops(
                TabStop(labelRight, TabAlignment.RIGHT),
                TabStop(PRICES_AMOUNT_RIGHT, TabAlignment.RIGHT),
            )
            .setFontSize(fontSize)
            .setFixedLeading(13f)
            .setMargin(0f)
        paragraph.add(Tab())
        paragraph.add(Text(label).also { if (bold) it.simulateBold() })
        paragraph.add(Tab())
        paragraph.add(Text(amount).also { if (bold) it.simulateBold() })
        return this.addCell(
            Cell().add(paragraph)
                .setBorder(Border.NO_BORDER)
                .setPadding(0f)
                .setPaddingBottom(2f)
        )
    }

    /**
     * "À régler avant le dd/mm/yyyy" (invoices with a due date) or the
     * generic "Paiement" fallback (credit notes). Used as the header of the
     * greyed payment box, so it just returns a String — the box owns the
     * bold + font size.
     */
    private fun paymentBoxTitle(document: DocumentState): String {
        val invoiceDate = (document as? InvoiceState)?.dueDate
            ?.substringBefore(" ")
            ?.takeIf { it.isNotBlank() }
        return if (invoiceDate != null) {
            strings.dueDate.trimEnd() + " " + invoiceDate
        } else {
            strings.paymentSectionTitle
        }
    }

    /** BT-81 flatten. Invoice-only — avoir + devis + BL don't render this. */
    private fun paymentMeansDisplayFor(document: DocumentState): String? {
        val invoice = document as? InvoiceState ?: return null
        if (invoice.paymentMeansHidden || invoice.paymentMeansSegments.isEmpty()) return null
        return com.a4a.g8invoicing.data.models
            .flattenPaymentLabel(invoice.paymentMeansSegments, strings.paymentMeansLabels)
            .takeIf { it.isNotEmpty() }
    }

    /**
     * BT-84/86 flatten. Invoice-only for the same reason as
     * paymentMeansDisplayFor — a devis has no payment context, an avoir
     * reverses the flow. Returns "IBAN : … \n BIC : …", null when both empty.
     */
    private fun bankRenderedFor(document: DocumentState): String? {
        val invoice = document as? InvoiceState ?: return null
        if (invoice.paymentBankHidden) return null
        val issuer = invoice.documentIssuer
        val iban = issuer?.paymentIban?.text?.trim().orEmpty()
        val bic = issuer?.paymentBic?.text?.trim().orEmpty()
        if (iban.isEmpty() && bic.isEmpty()) return null
        val country = issuer?.paymentCountry
        val identifierLabel = if (com.a4a.g8invoicing.data.models.CountryCodes.isIbanCountry(country)
            || country == null
        ) strings.bankAccountIbanLabel else strings.bankAccountGenericLabel
        val effectiveSegments = invoice.paymentBankSegments.ifEmpty {
            com.a4a.g8invoicing.data.models.defaultPaymentBankSegments()
        }
        return com.a4a.g8invoicing.data.models
            .flattenPaymentBank(effectiveSegments, identifierLabel, iban, bic)
            .takeIf { it.isNotEmpty() }
    }

    /**
     * The grey rounded box below the totals — header line (due date or
     * "Paiement") + means + IBAN/BIC. Built as a single-cell Table so the
     * background paints under the whole content block, not per Paragraph.
     * Table width is left unspecified so it hugs the widest inner line
     * instead of stretching across the page.
     */
    private fun createPaymentBox(
        document: DocumentState,
        paymentMeansDisplay: String?,
        bankRendered: String?,
        fontSize: Float,
    ): Table {
        val cell = Cell()
            .setBackgroundColor(com.itextpdf.kernel.colors.DeviceRgb(245, 245, 245))
            .setBorder(Border.NO_BORDER)
            .setPaddingTop(10f)
            .setPaddingBottom(10f)
            .setPaddingLeft(14f)
            .setPaddingRight(14f)
        cell.add(
            Paragraph(paymentBoxTitle(document))
                .setFontSize(fontSize)
                .pdfBold()
                .setFixedLeading(11F)
                .setMarginBottom(4f)
        )
        val bodyLines = listOfNotNull(paymentMeansDisplay, bankRendered)
            .joinToString("\n")
        if (bodyLines.isNotEmpty()) {
            cell.add(
                Paragraph(bodyLines)
                    .setFontSize(fontSize)
                    // Match the issuer companyInfo Paragraph's setFixedLeading(12F)
                    // so 'Mode de paiement accepté / IBAN / BIC' reads with the
                    // same interline as SIREN / TVA above.
                    .setFixedLeading(12F)
            )
        }
        return Table(1)
            .setBorder(Border.NO_BORDER)
            .setMarginTop(12f)
            .setHorizontalAlignment(HorizontalAlignment.LEFT)
            .addCell(cell)
    }

    /** Hairline grey rule that groups the terms / footer / watermark trio. */
    private fun createSeparator(topMargin: Float): Table {
        return Table(1)
            .useAllAvailableWidth()
            .setBorder(Border.NO_BORDER)
            .setMarginTop(topMargin)
            .setMarginBottom(8f)
            .addCell(
                Cell()
                    .setBorder(Border.NO_BORDER)
                    .setBorderTop(SolidBorder(com.itextpdf.kernel.colors.DeviceRgb(224, 224, 224), 0.5f))
                    .setPadding(0f)
                    .setHeight(0.5f)
            )
    }

    // Locale-aware "Paid" stamp. Resolve to the invoice's frozen formatLocale
    // so a FR-created invoice keeps the FR stamp even after the app is
    // switched to EN. Fallbacks: current UI locale (legacy pre-1.8 invoices
    // where formatLocale is null), then the language-neutral drawable/
    // default (FR) as last resort.
    //
    // Sized via setWidth and absolute-positioned via setFixedPosition to sit
    // over the products table area, same pattern as the pre-KMP
    // CreatePdfWithIText.
    private fun createPaidStamp(formatLocale: String?): Image? {
        if (imageStorage == null) return null
        val paths = buildList {
            if (!formatLocale.isNullOrBlank()) add("drawable-$formatLocale/img_paid.png")
            val uiLocale = AppLocaleHolder.languageCode
            if (uiLocale != formatLocale) add("drawable-$uiLocale/img_paid.png")
            add("drawable/img_paid.png")
        }
        val bytes = paths.firstNotNullOfOrNull { imageStorage.readBundledResource(it) }
            ?: return null
        return try {
            Image(ImageDataFactory.create(bytes))
                .setWidth(162f)
                .setFixedPosition(200f, 555f)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * User-typed free field rendered under the hairline. Small black centered
     * to match the preview footer. [precededByTerms] adds a small top margin
     * so it doesn't hug the terms above.
     */
    private fun createFooter(text: String, fontSize: Float, precededByTerms: Boolean): Paragraph {
        return Paragraph(text)
            .setFontSize(fontSize - 1.5F)
            .setFixedLeading(10F)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(if (precededByTerms) 6f else 0f)
    }

    /**
     * BT-20 payment terms description — free text. Sits under the hairline,
     * left-aligned in muted grey (mention style). Non-null caller check
     * ensures we don't render an empty paragraph.
     */
    private fun createPaymentTermsBlock(text: String, fontSize: Float): Paragraph {
        return Paragraph(text)
            .setFontSize(fontSize - 1.5F)
            .setFontColor(ColorConstants.DARK_GRAY)
            .setFixedLeading(9F)
            .setTextAlignment(TextAlignment.LEFT)
    }

    private fun createWatermark(text: String): Paragraph {
        // Tiny watermark, smaller than the document body. We split on the URL marker
        // to make only that substring a clickable Link in PDF readers, but the entire
        // line is underlined to match the in-app preview (Text with TextDecoration.Underline).
        // If the (translated) text doesn't contain the marker, we render plain underlined text.
        // The underline uses an explicit thin thickness (0.2pt) — iText's default thickness
        // is too heavy relative to the small font and reads as a smudged bar.
        val urlMarker = "www.the-gate.fr"
        val underlineThickness = 0.2F
        val underlineYPosition = -1F
        val paragraph = Paragraph()
        if (text.contains(urlMarker)) {
            paragraph.add(Text(text.substringBefore(urlMarker)).setUnderline(underlineThickness, underlineYPosition))
            paragraph.add(
                Link(urlMarker, PdfAction.createURI("https://the-gate.fr")).setUnderline(underlineThickness, underlineYPosition)
            )
            paragraph.add(Text(text.substringAfter(urlMarker)).setUnderline(underlineThickness, underlineYPosition))
        } else {
            paragraph.add(Text(text).setUnderline(underlineThickness, underlineYPosition))
        }
        return paragraph
            .setFontSize(7F)
            .setFixedLeading(9F)
            .setCharacterSpacing(0.4F)
            .setFontColor(ColorConstants.GRAY)
            .setMarginTop(8F)
            .setTextAlignment(TextAlignment.CENTER)
    }

    private fun Table.addCustomCell(
        text: String? = null,
        alignment: TextAlignment = TextAlignment.RIGHT,
        isBold: Boolean = false,
        paragraphs: List<Paragraph?>? = null,
        isSpan: Boolean = false,
        spanColumns: Int = 6,
    ): Table {
        val cell = Cell(1, if (isSpan) spanColumns else 1)
            .setTextAlignment(alignment)
            .setPaddingLeft(6f)
            .setPaddingRight(6f)
            .setPaddingTop(if (isBold) 5f else 3f)
            .setPaddingBottom(if (isBold) 5f else 3f)
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 1f))

        if (isBold) cell.pdfBold()

        if (text != null) {
            cell.add(Paragraph(text).setFixedLeading(10F))
        } else {
            paragraphs?.filterNotNull()?.forEach { cell.add(it) }
        }
        return this.addCell(cell)
    }

    // Lazily-loaded PdfFont used only for measuring price-row widths. Kept as
    // a nullable cache field so we don't re-parse the TTF for every PDF; the
    // PdfGeneratorImpl instance is per-generation anyway, so no cross-thread
    // concern. Falls back to the Base14 Helvetica if the asset is missing —
    // measurement will underestimate exotic glyphs but PRICES_LABEL_AMOUNT_GAP
    // has enough slack for that to still look correct.
    private var pricesMeasurementFont: PdfFont? = null
    private fun loadPricesMeasurementFont(): PdfFont {
        pricesMeasurementFont?.let { return it }
        val font = try {
            fileManager.loadAssetBytes("helvetica.ttf")?.let { bytes ->
                PdfFontFactory.createFont(FontProgramFactory.createFont(bytes), PdfEncodings.IDENTITY_H)
            }
        } catch (_: Throwable) { null } ?: PdfFontFactory.createFont(StandardFonts.HELVETICA)
        pricesMeasurementFont = font
        return font
    }

    // Per-char width measurement so we can substitute a fallback estimate for
    // glyphs the measurement font doesn't cover (getWidth returns 0 for those,
    // which would otherwise cause the label to overlap the amount when the
    // currency uses ₪ ₼ ₽ ₾).
    private fun measurePriceWidth(text: String, font: PdfFont, fontSize: Float): Float {
        var total = 0f
        for (c in text) {
            val glyphWidth = font.getWidth(c.code, fontSize)
            total += if (glyphWidth > 0f) glyphWidth else fontSize
        }
        return total
    }

    // Build the PdfStrings actually used by this PDF: start from the app-locale
    // defaults, then override each field with the doc's frozen snapshot when
    // available. Keys must match those in DocumentLabels.keys — the ones the
    // preview already reads via documentLabel(). A snapshot value that is
    // null or blank falls through to the default, so an old snapshot missing
    // newer keys still renders sanely.
    private fun effectiveStrings(document: DocumentState, defaults: PdfStrings): PdfStrings {
        val snap = com.a4a.g8invoicing.ui.screens.shared.DocumentLabels
            .parseSnapshot(document.labelsSnapshot)
        val docLocale = document.formatLocale
        // Snapshot first, then a hardcoded doc-locale fallback (for keys
        // added to DocumentLabels after some docs were already created),
        // then the app-locale defaults passed to the ctor.
        fun pick(key: String, default: String): String {
            snap?.get(key)?.takeIf { it.isNotBlank() }?.let { return it }
            com.a4a.g8invoicing.ui.screens.shared.DocumentLabels
                .localeFallback(key, docLocale)?.let { return it }
            return default
        }
        if (snap == null && docLocale == null) return defaults
        return defaults.copy(
            invoiceNumber = pick("invoice_number", defaults.invoiceNumber),
            deliveryNoteNumber = pick("delivery_note_number", defaults.deliveryNoteNumber),
            creditNoteNumber = pick("credit_note_number", defaults.creditNoteNumber),
            documentDate = pick("document_date_label", defaults.documentDate),
            documentReference = pick("document_reference_label", defaults.documentReference),
            tableDescription = pick("document_table_description", defaults.tableDescription),
            tableQuantity = pick("document_table_quantity", defaults.tableQuantity),
            tableUnit = pick("document_table_unit", defaults.tableUnit),
            tableTaxRate = pick("document_table_tax_rate", defaults.tableTaxRate),
            tableUnitPrice = pick("document_table_unit_price_without_tax", defaults.tableUnitPrice),
            tableTotalPrice = pick("document_table_total_price_without_tax", defaults.tableTotalPrice),
            totalWithoutTax = pick("document_total_without_tax", defaults.totalWithoutTax),
            totalWithTax = pick("document_total_with_tax", defaults.totalWithTax),
            // "tax" isn't a full label — DocumentLabels stores "document_tax_label"
            // which is the full "TVA %1$s :" pattern. Not portable to the split
            // structure of PdfStrings.tax + labelSeparator, so we leave defaults
            // as-is. Consequence: the "TVA / Tax" prefix in the totals block
            // follows current language for legacy snapshots. Acceptable.
            dueDate = pick("invoice_pdf_due_date", defaults.dueDate),
            invoicePaid = pick("invoice_paid", defaults.invoicePaid),
            labelSeparator = pick("label_separator", defaults.labelSeparator),
            addressedTo = pick("addressed_to", defaults.addressedTo),
            companyId1Label = pick("company_identification1", defaults.companyId1Label),
            companyId2Label = pick("company_identification2", defaults.companyId2Label),
            companyId3Label = pick("company_identification3", defaults.companyId3Label),
            currencyNoticeLabel = pick("pdf_currency_notice", defaults.currencyNoticeLabel),
            paymentSectionTitle = pick("document_payment_section_title", defaults.paymentSectionTitle),
            // Freeze the mode labels (BT-81) per-chip so a FR invoice keeps
            // "Virement, chèque" after the user switches app to EN. Same
            // snapshot → localeFallback → app-default cascade as every other
            // field above. Chip identity (not UN/CEFACT code) because PayPal
            // + Stripe share code 68. The prefix isn't handled here — it's
            // the user's paymentMeansLabel on the doc state, not a static
            // PdfStrings key.
            paymentMeansLabels = defaults.paymentMeansLabels.mapValues { (chipId, default) ->
                val labelKey = com.a4a.g8invoicing.data.models.PaymentMeans
                    .fromChipId(chipId)?.labelKey ?: "payment_means_$chipId"
                pick(labelKey, default)
            },
        )
    }

}

/**
 * iText 9.5 doesn't expose a typed setBold()/setFontFamily() on every layout
 * element (Document, Cell), only on Text. FontProvider selection driven by
 * Property.FONT_WEIGHT + Property.FONT gives us a uniform path that works on
 * every element implementing IPropertyContainer.
 */
private fun <T : IPropertyContainer> T.pdfFontFamily(family: String): T {
    setProperty(Property.FONT, arrayOf(family))
    return this
}

private fun <T : IPropertyContainer> T.pdfBold(): T {
    setProperty(Property.FONT_WEIGHT, "bold")
    return this
}

/**
 * Rounded cell renderer for client/issuer boxes.
 * Shared between Android and Desktop.
 */
class RoundedCellRenderer(
    modelElement: Cell,
    private val color: com.itextpdf.kernel.colors.Color,
    private val isColoredBackground: Boolean
) : CellRenderer(modelElement) {

    init {
        modelElement.setBorder(Border.NO_BORDER)
    }

    override fun drawBackground(drawContext: DrawContext) {
        val fullRect: Rectangle = occupiedAreaBBox
        val canvas: PdfCanvas = drawContext.canvas

        // The cell can be stretched vertically to match the tallest cell in its row
        // (typically the issuer cell). Draw the rounded border around the actual
        // content bottom instead of the full cell frame so the box hugs the client
        // info even when the issuer has more lines.
        val contentBottom = childRenderers
            .mapNotNull { it.occupiedArea?.bBox?.bottom }
            .minOrNull()
            ?: fullRect.bottom
        val contentPadding = 6.0
        val drawBottom = (contentBottom - contentPadding).coerceAtLeast(fullRect.bottom + 2.5)
        val drawHeight = (fullRect.top - 2.5) - drawBottom

        canvas.saveState()
            .roundRectangle(fullRect.left + 2.5, drawBottom, fullRect.width - 5.0, drawHeight, 24.0)
            .setStrokeColor(color)
            .setLineWidth(1.5f)
        if (isColoredBackground) {
            canvas.setFillColor(color).fillStroke()
        } else {
            canvas.stroke()
        }
        canvas.restoreState()
    }
}
