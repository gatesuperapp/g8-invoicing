package com.a4a.g8invoicing.ui.shared

import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.data.toStringWithTwoDecimals
import com.a4a.g8invoicing.ui.screens.shared.getLinkedDeliveryNotes
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.DocumentTotalPrices
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
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
    private val strings: PdfStrings,
    private val fileManager: PdfFileManager
) {
    fun generatePdf(document: DocumentState): String {
        val tempFileName = "${document.documentNumber.text}_temp.pdf"
        val tempFilePath = fileManager.getTempFilePath(tempFileName)

        // Delete temp file if exists
        File(tempFilePath).delete()

        // Create PDF
        val writer = PdfWriter(tempFilePath)
        val pdfDocument = PdfDocument(writer)

        val fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA)
        val fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)

        val doc = Document(pdfDocument, PageSize.A4)
            .setFont(fontRegular)
            .setFontSize(9.5F)

        // Add content
        buildPdfContent(doc, document, fontRegular, fontBold)

        doc.close()
        writer.close()
        pdfDocument.close()

        // Add page numbering
        return addPageNumbering(document, tempFileName)
    }

    private fun buildPdfContent(
        doc: Document,
        document: DocumentState,
        fontRegular: PdfFont,
        fontBold: PdfFont
    ) {
        val titleFontSize = 20F
        val dateFontSize = 16F
        val fontSize = 9.5F

        // Title
        doc.add(createTitle(document.documentNumber.text, document.documentType, fontBold, titleFontSize))

        // Date
        doc.add(createDate(document.documentDate.substringBefore(" "), fontBold, dateFontSize))

        // Issuer and Client
        doc.add(createIssuerAndClientTable(document.documentIssuer, document.documentClient, fontBold, fontSize))

        // Reference
        document.reference?.text?.takeIf { it.isNotEmpty() }?.let {
            doc.add(createReference(it, fontBold).setMarginTop(4F))
        }

        // Free field
        document.freeField?.text?.takeIf { it.isNotEmpty() }?.let {
            val marginTop = if (document.reference?.text.isNullOrEmpty()) 10F else 2F
            doc.add(createFreeText(it, fontRegular).setMarginTop(marginTop))
        }

        // Products table
        document.documentProducts?.let { products ->
            createProductsTable(products, fontBold, fontRegular)?.let {
                val marginTop = if (document.reference?.text.isNullOrEmpty() && document.freeField?.text.isNullOrEmpty()) 20f else 10f
                doc.add(it.setMarginTop(marginTop).setMarginBottom(12f))
            }
        }

        // Prices
        document.documentTotalPrices?.let {
            doc.add(createPrices(fontBold, it, fontSize))
        }

        // Due date (invoices only)
        if (document is InvoiceState) {
            doc.add(createDueDate(fontBold, document.dueDate.substringBefore(" "), fontSize))
        }

        // Footer
        doc.add(createFooter(document.footerText.text, fontSize))
    }

    private fun addPageNumbering(document: DocumentState, tempFileName: String): String {
        val fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA)
        val finalFileName = "${document.documentNumber.text}.pdf"

        val tempFilePath = fileManager.getTempFilePath(tempFileName)
        val finalTempPath = fileManager.getTempFilePath(finalFileName)

        File(finalTempPath).delete()

        try {
            val pdfDoc = PdfDocument(PdfReader(tempFilePath), PdfWriter(finalTempPath))
            val doc = Document(pdfDoc)
            val numberOfPages = pdfDoc.numberOfPages

            if (numberOfPages > 1) {
                for (i in 1..numberOfPages) {
                    val prefix = if (i == 1) "" else "${getDocumentTypeName(document.documentType, strings)} ${document.documentNumber.text} - "
                    doc.showTextAligned(
                        Paragraph("$prefix$i/$numberOfPages").setFont(fontRegular),
                        570f, 34f, i, TextAlignment.RIGHT, VerticalAlignment.TOP, 0f
                    )
                }
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

    private fun createTitle(documentNumber: String, documentType: DocumentType?, font: PdfFont, fontSize: Float): Paragraph {
        val title = documentType?.let { getDocumentTypeName(it, strings) } ?: ""
        return Paragraph(title + documentNumber)
            .setFont(font)
            .setFontSize(fontSize)
            .setMarginBottom(-6F)
    }

    private fun createDate(date: String, font: PdfFont, fontSize: Float): Paragraph {
        val dateLabel = strings.documentDate.trimEnd() + " "
        return Paragraph("$dateLabel$date")
            .setFont(font)
            .setFontSize(fontSize)
            .setMarginBottom(24F)
    }

    private fun createIssuerAndClientTable(
        issuer: ClientOrIssuerState?,
        client: ClientOrIssuerState?,
        font: PdfFont,
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

        // Issuer
        issuer?.let {
            val issuerCell = Cell().setBorder(Border.NO_BORDER)
            createClientOrIssuerParagraph(it, font, fontSize = fontSize).forEach { p -> issuerCell.add(p) }
            table.addCell(issuerCell)
        }

        // Client
        client?.let {
            table.addCell(
                createClientRectangleAndContent(createClientOrIssuerParagraph(it, font, fontSize = fontSize))
                    .setPaddingBottom(8f)
            )
            table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(6f))
            table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(6f))

            // Additional addresses
            it.addresses?.let { addresses ->
                // When there are only 2 addresses, put address 2 on the right (below address 1)
                if (addresses.size == 2) {
                    table.addCell(Cell().setBorder(Border.NO_BORDER)) // Empty cell on left
                }
                for (i in 1..<addresses.size) {
                    table.addCell(createAddressTitle(addresses, i, fontSize))
                }
                table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(1f))
                table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(1f))

                // When there are only 2 addresses, put address 2 on the right (below address 1)
                if (addresses.size == 2) {
                    table.addCell(Cell().setBorder(Border.NO_BORDER)) // Empty cell on left
                }
                for (i in 1..<addresses.size) {
                    table.addCell(
                        createClientRectangleAndContent(
                            createClientOrIssuerParagraph(client, font, displayAllInfo = false, addressIndex = i, fontSize = fontSize)
                        )
                    )
                }
            }
        }

        table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(25f))
        table.addCell(Cell().setBorder(Border.NO_BORDER).setPaddingBottom(25f))

        return table
    }

    private fun createAddressTitle(addresses: List<AddressState>, index: Int, fontSize: Float): Cell {
        val title = addresses.getOrNull(index)?.addressTitle?.text ?: ""
        return Cell()
            .setBorder(Border.NO_BORDER)
            .add(Paragraph(title).setFontColor(ColorConstants.DARK_GRAY).setFixedLeading(6F).setFontSize(fontSize))
            .setTextAlignment(TextAlignment.CENTER)
    }

    private fun createClientRectangleAndContent(content: List<Paragraph>): Cell {
        val cell = Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).setPaddingBottom(8f)
        content.forEach { cell.add(it) }
        cell.setNextRenderer(RoundedCellRenderer(cell, ColorConstants.LIGHT_GRAY, false))
        return cell
    }

    private fun createClientOrIssuerParagraph(
        clientOrIssuer: ClientOrIssuerState?,
        font: PdfFont,
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
            clientOrIssuer?.firstName?.text?.let { nameAndAddress.add(Text("$it ").setFont(font)) }
            clientOrIssuer?.name?.text?.let { nameAndAddress.add(Text("$it\n").setFont(font)) }
        }
        result.add(nameAndAddress)

        address?.addressLine1?.text?.takeIf { it.isNotEmpty() }?.let { nameAndAddress.add("$it\n") }
        address?.addressLine2?.text?.takeIf { it.isNotEmpty() }?.let { nameAndAddress.add("$it\n") }

        val zipCode = address?.zipCode?.text ?: ""
        val city = address?.city?.text ?: ""
        if (zipCode.isNotEmpty() || city.isNotEmpty()) {
            nameAndAddress.add("$zipCode $city\n")
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
                companyInfo.add(Text("${clientOrIssuer.companyId1Label?.text}${strings.labelSeparator}$it\n"))
            }
            clientOrIssuer?.companyId2Number?.text?.let {
                companyInfo.add(Text("${clientOrIssuer.companyId2Label?.text}${strings.labelSeparator}$it\n"))
            }
            clientOrIssuer?.companyId3Number?.text?.let {
                companyInfo.add(Text("${clientOrIssuer.companyId3Label?.text}${strings.labelSeparator}$it\n"))
            }
            result.add(companyInfo)
        }

        return result
    }

    private fun createReference(text: String, font: PdfFont): Paragraph {
        val referenceLabel = strings.documentReference.trimEnd() + " "
        return Paragraph(Text(referenceLabel).setFont(font)).add(text)
    }

    private fun createFreeText(text: String, font: PdfFont): Paragraph {
        return Paragraph(text).setFont(font)
    }

    private fun createProductsTable(
        products: List<DocumentProductState>,
        fontBold: PdfFont,
        fontRegular: PdfFont
    ): Table? {
        try {
            val displayUnitColumn = products.any { !it.unit?.text.isNullOrEmpty() }

            val columnWidth = if (displayUnitColumn) floatArrayOf(45f, 9f, 13f, 8f, 12f, 13f)
            else floatArrayOf(45f, 9f, 8f, 12f, 13f)

            val table = Table(UnitValue.createPercentArray(columnWidth)).useAllAvailableWidth().setFixedLayout()

            // Header
            table.addCustomCell(strings.tableDescription, TextAlignment.LEFT, true, fontBold, fontRegular)
            table.addCustomCell(strings.tableQuantity, TextAlignment.RIGHT, true, fontBold, fontRegular)
            if (displayUnitColumn) {
                table.addCustomCell(strings.tableUnit, TextAlignment.RIGHT, true, fontBold, fontRegular)
            }
            table.addCustomCell(strings.tableTaxRate, TextAlignment.RIGHT, true, fontBold, fontRegular)
            table.addCustomCell(strings.tableUnitPrice, TextAlignment.RIGHT, true, fontBold, fontRegular)
            table.addCustomCell(strings.tableTotalPrice, TextAlignment.RIGHT, true, fontBold, fontRegular)

            // Products
            val linkedDeliveryNotes = getLinkedDeliveryNotes(products)
            if (linkedDeliveryNotes.isNotEmpty()) {
                linkedDeliveryNotes.forEach { (docNumber, docDate) ->
                    table.addCustomCell(
                        "$docNumber - ${docDate?.substringBefore(" ")}",
                        TextAlignment.LEFT, true, fontBold, fontRegular, isSpan = true
                    )
                    addProductRows(products.filter { it.linkedDocNumber == docNumber }, table, fontBold, fontRegular, displayUnitColumn)
                }
            } else {
                addProductRows(products, table, fontBold, fontRegular, displayUnitColumn)
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
        fontBold: PdfFont,
        fontRegular: PdfFont,
        displayUnitColumn: Boolean
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

            table.addCustomCell(paragraphs = listOfNotNull(itemName, spacing, itemDescription), alignment = TextAlignment.LEFT, fontBold = fontBold, fontRegular = fontRegular)
            table.addCustomCell(product.quantity.stripTrailingZeros().toPlainString().replace(".", ","), fontBold = fontBold, fontRegular = fontRegular)

            if (displayUnitColumn) {
                table.addCustomCell(product.unit?.text, fontBold = fontBold, fontRegular = fontRegular)
            }

            table.addCustomCell(
                product.taxRate?.let { "${it.stripTrailingZeros().toPlainString().replace(".", ",")}%" } ?: " - ",
                fontBold = fontBold, fontRegular = fontRegular
            )
            table.addCustomCell(
                product.priceWithoutTax?.toStringWithTwoDecimals()?.replace(".", ",")?.plus(strings.currency) ?: "",
                fontBold = fontBold, fontRegular = fontRegular
            )
            table.addCustomCell(
                product.priceWithoutTax?.let { price ->
                    (price * product.quantity).toStringWithTwoDecimals().replace(".", ",") + strings.currency
                } ?: "",
                fontBold = fontBold, fontRegular = fontRegular
            )
        }
    }

    private fun createPrices(font: PdfFont, prices: DocumentTotalPrices, fontSize: Float): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(90f, 10f)))
            .useAllAvailableWidth()
            .setTextAlignment(TextAlignment.RIGHT)
            .setPaddingBottom(8f)
            .setPaddingRight(4f)

        // Total HT
        table.addCellInPrices(Paragraph(strings.totalWithoutTax))
        table.addCellInPrices(Paragraph("${prices.totalPriceWithoutTax?.toStringWithTwoDecimals()?.replace(".", ",") ?: " - "}${strings.currency}"))

        // TVA par taux
        prices.totalAmountsOfEachTax?.sortedBy { it.first }?.forEach { (taxRate, taxAmount) ->
            table.addCellInPrices(Paragraph("${strings.tax} ${taxRate.stripTrailingZeros().toPlainString().replace(".", ",")}%${strings.labelSeparator}"))
            table.addCellInPrices(Paragraph("${taxAmount.toStringWithTwoDecimals().replace(".", ",")}${strings.currency}"))
        }

        // Total TTC
        table.addCellInPrices(Paragraph(strings.totalWithTax).setFont(font).setFontSize(fontSize))
        table.addCellInPrices(Paragraph("${prices.totalPriceWithTax?.toStringWithTwoDecimals()?.replace(".", ",") ?: " - "}${strings.currency}").setFont(font).setFontSize(fontSize))

        return table
    }

    private fun createDueDate(font: PdfFont, date: String, fontSize: Float): Paragraph {
        val dueDateLabel = strings.dueDate.trimEnd() + " "
        return Paragraph("$dueDateLabel$date")
            .setFixedLeading(16F)
            .setPaddingTop(12f)
            .setTextAlignment(TextAlignment.CENTER)
            .setFont(font)
            .setFontSize(fontSize)
    }

    private fun createFooter(text: String, fontSize: Float): Paragraph {
        return Paragraph(text)
            .setFontSize(fontSize)
            .setFixedLeading(14F)
            .setTextAlignment(TextAlignment.CENTER)
    }

    private fun Table.addCustomCell(
        text: String? = null,
        alignment: TextAlignment = TextAlignment.RIGHT,
        isBold: Boolean = false,
        fontBold: PdfFont,
        fontRegular: PdfFont,
        paragraphs: List<Paragraph?>? = null,
        isSpan: Boolean = false
    ): Table {
        val cell = Cell(1, if (isSpan) 6 else 1)
            .setTextAlignment(alignment)
            .setPaddingLeft(6f)
            .setPaddingRight(6f)
            .setPaddingTop(if (isBold) 5f else 3f)
            .setPaddingBottom(if (isBold) 5f else 3f)
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 1f))
            .setFont(if (isBold) fontBold else fontRegular)

        if (text != null) {
            cell.add(Paragraph(text).setFixedLeading(10F))
        } else {
            paragraphs?.filterNotNull()?.forEach { cell.add(it) }
        }
        return this.addCell(cell)
    }

    private fun Table.addCellInPrices(paragraph: Paragraph): Table {
        return this.addCell(
            Cell().add(paragraph)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(-4f)
                .setPaddingBottom(6f)
        )
    }
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
        val rect: Rectangle = occupiedAreaBBox
        val canvas: PdfCanvas = drawContext.canvas
        canvas.saveState()
            .roundRectangle(rect.left + 2.5, rect.bottom + 2.5, rect.width - 5.0, rect.height - 5.0, 24.0)
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
