package com.a4a.g8invoicing.ui.shared

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.PricesRowName
import com.a4a.g8invoicing.ui.screens.shared.getLinkedDeliveryNotes
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentTotalPrices
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.colors.Color
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
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import com.itextpdf.layout.renderer.CellRenderer
import com.itextpdf.layout.renderer.DrawContext
import java.io.File
import java.io.InputStream
import com.a4a.g8invoicing.data.setScale
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode

// Cache pour stocker l'Uri du dernier PDF exporté (Android 10+)
private var lastExportedPdfUri: Uri? = null
private var lastExportedPdfFileName: String? = null

fun createPdfWithIText(inputDocument: DocumentState, context: Context): String {
    val fileNameBeforeNumbering = "${inputDocument.documentNumber.text}_temp.pdf"

    // Supprimer le fichier temporaire s'il existe déjà
    val tempFile = getTempFile(context, fileNameBeforeNumbering)
    if (tempFile.exists()) {
        tempFile.delete()
    }

    val writer = PdfWriter(getTempFilePath(context, fileNameBeforeNumbering))
    writer.isFullCompression
    val pdfDocument = PdfDocument(writer)

    val fontRegular = PdfFontFactory.createFont("assets/helvetica.ttf")
    val fontBold: PdfFont = PdfFontFactory.createFont("assets/helveticabold.ttf")

    val document = Document(pdfDocument, PageSize.A4)
        .setFont(fontRegular)
        .setFontSize(9.5F)

    val titleFontSize = 20F
    val dateFontSize = 16F
    val clientAndIssuerFontSize = 9.5F
    val referenceFontSize = 9.5F
    val productTableFontSize = 9.5F
    val footerFontSize = 9.5F

    document.add(
        createTitle(
            inputDocument.documentNumber.text,
            inputDocument.documentType,
            fontBold,
            titleFontSize
        )
    )
    document.add(
        createDate(
            inputDocument.documentDate.substringBefore(" "),
            fontBold,
            dateFontSize
        )
    )
    document.add(
        createIssuerAndClientTable(
            inputDocument.documentIssuer,
            inputDocument.documentClient,
            fontBold,
            clientAndIssuerFontSize
        )
    )
    if (!inputDocument.reference?.text.isNullOrEmpty()) {
        inputDocument.reference?.let {
            if (it.text.isNotEmpty()) {
                document.add(
                    createReference(it.text, fontBold)
                        .setMarginTop(4F)
                )
            }
        }
    }

    if (!inputDocument.freeField?.text.isNullOrEmpty()) {
        inputDocument.freeField?.let {
            if (it.text.isNotEmpty()) {
                document.add(
                    createFreeText(it.text, fontRegular)
                        .setMarginTop(
                            if (!inputDocument.reference?.text.isNullOrEmpty())
                                2F
                            else 10F
                        )
                )
            }
        }
    }

    inputDocument.documentProducts?.let {
        createProductsTable(it, fontBold = fontBold, fontRegular = fontRegular)?.let {
            try {
                document.add(
                    it
                        .setMarginTop(
                            if (inputDocument.reference?.text.isNullOrEmpty() && inputDocument.reference?.text.isNullOrEmpty()) 20f
                            else 10f
                        )
                        .setMarginBottom(12f)
                )
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }

    }
    inputDocument.documentTotalPrices?.let {
        document.add(
            createPrices(
                fontBold,
                it,
                productTableFontSize
            )
        )
    }
    if (inputDocument is InvoiceState) {
        document.add(
            createDueDate(
                fontBold,
                inputDocument.dueDate.substringBefore(" "),
                footerFontSize
            )
        )
    }

    document.add(
        createFooter(
            inputDocument.footerText.text,
            footerFontSize
        )
    )

    document.close()

    writer.isCloseStream
    writer.close()
    pdfDocument.close()

    return addPageNumberingAndDeletePreviousFile(inputDocument, fileNameBeforeNumbering, context)
}

fun addPageNumberingAndDeletePreviousFile(
    inputDocument: DocumentState,
    fileNameBeforeNumbering: String,
    context: Context,
): String {
    val fontRegular: PdfFont = PdfFontFactory.createFont("assets/helvetica.ttf")
    val finalFileName = "${inputDocument.documentNumber.text}.pdf"

    // Fichier temporaire source (le PDF sans numérotation)
    val tempSourceFile = getTempFile(context, fileNameBeforeNumbering)
    // Fichier temporaire destination (le PDF final avec numérotation)
    val tempFinalFile = getTempFile(context, finalFileName)

    // Supprimer le fichier final temporaire s'il existe
    if (tempFinalFile.exists()) {
        tempFinalFile.delete()
    }

    if (tempSourceFile.exists() && tempSourceFile.isFile) {
        try {
            val pdfDoc = PdfDocument(
                PdfReader(tempSourceFile.absolutePath),
                PdfWriter(tempFinalFile.absolutePath)
            )
            val doc = Document(pdfDoc)
            val numberOfPages = pdfDoc.numberOfPages

            if (inputDocument is InvoiceState && inputDocument.documentTag == DocumentTag.PAID) {
                createPaidStamp(context)?.let {
                    doc.add(it)
                }
            }

            if (numberOfPages > 1) {
                for (i in 1..numberOfPages) {
                    // Display document number on every page footer except first page
                    val documentNameAndNumber =
                        if (i == 1) "" else getDocumentName(inputDocument.documentType) + " " +
                                inputDocument.documentNumber.text + " - "
                    doc.showTextAligned(
                        Paragraph(
                            documentNameAndNumber
                                    + String.format(
                                "%s/%s",
                                i,
                                numberOfPages
                            )
                        )
                            .setFont(fontRegular),
                        570f, 34f, i, TextAlignment.RIGHT, VerticalAlignment.TOP, 0f
                    )
                }
            }

            doc.close()
            pdfDoc.close()

            // Supprimer le fichier source temporaire
            tempSourceFile.delete()

            // Sauvegarder le PDF final dans Downloads/g8
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ : utiliser MediaStore
                val savedUri = saveToDownloadsWithMediaStore(context, tempFinalFile, finalFileName)
                // Sauvegarder l'Uri dans le cache pour le partage
                lastExportedPdfUri = savedUri
                lastExportedPdfFileName = finalFileName
                // Supprimer le fichier temporaire après copie
                tempFinalFile.delete()
            } else {
                // Android 9 et moins : copier vers Downloads/g8
                val destFile = getFileLegacy(finalFileName)
                destFile?.let {
                    tempFinalFile.copyTo(it, overwrite = true)
                    tempFinalFile.delete()
                }
            }

        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }
    return finalFileName
}

private fun getDocumentName(documentType: DocumentType): String {
    return when (documentType) {
        DocumentType.INVOICE -> Strings.get(R.string.invoice_number)
        DocumentType.DELIVERY_NOTE -> Strings.get(R.string.delivery_note_number)
        DocumentType.CREDIT_NOTE -> Strings.get(R.string.credit_note_number)
    }
}

private fun createTitle(
    documentNumber: String,
    documentType: DocumentType? = null,
    font: PdfFont,
    titleFontSize: Float,
): Paragraph {
    var title = ""
    documentType?.let {
        title = getDocumentName(it)
    }

    return Paragraph(title + documentNumber)
        .setFont(font)
        .setFontSize(titleFontSize)
        .setMarginBottom(-6F)
}

private fun createDate(date: String, font: PdfFont, fontSize: Float): Paragraph {
    return Paragraph(Strings.get(R.string.document_date) + " : " + date)
        .setFont(font)
        .setFontSize(fontSize)
        .setMarginBottom(24F)
}


private fun createIssuerAndClientTable(
    issuer: ClientOrIssuerState?,
    client: ClientOrIssuerState?,
    font: PdfFont,
    fontSize: Float,
): Table {
    val issuerAndClientTable = Table(2)
        .useAllAvailableWidth()
        .setFixedLayout()

    // ROW 1: NOTHING ------- CLIENT ADDRESS TITLE 1
    client?.addresses?.let {
        issuerAndClientTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
        )
        issuerAndClientTable.addCell(createAddressTitle(it, 0, fontSize))

        // ROW X : Just adding space
        issuerAndClientTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .setPaddingBottom(1f)
        )
        issuerAndClientTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .setPaddingBottom(1f)
        )
    }

    // ROW 2 : ISSUER --------- CLIENT ADDRESS 1
    issuer?.let {
        val issuerContent = createClientOrIssuerParagraph(it, font, fontSize = fontSize)
        val issuerCell = Cell().setBorder(Border.NO_BORDER)
        issuerContent.forEach {
            issuerCell.add(it)
        }
        issuerAndClientTable.addCell(issuerCell)
    }
    client?.let {
        issuerAndClientTable.addCell(
            createClientRectangleAndContent(
                createClientOrIssuerParagraph(it, font, fontSize = fontSize)
            ).setPaddingBottom(8f)
        )
        // ROW X : Just adding space
        issuerAndClientTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .setPaddingBottom(6f)
        )
        issuerAndClientTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .setPaddingBottom(6f)
        )
    }


    // ROW 3:  CLIENT ADDRESS TITLE 2 ------- TITLE 3
    client?.addresses?.let { addresses ->
        val numberOfAddresses = addresses.size ?: 1
        for (i in 1..<numberOfAddresses) {
            addresses.let {
                issuerAndClientTable.addCell(createAddressTitle(it, i, fontSize = fontSize))
            }
        }
        // ROW X : Just adding space
        issuerAndClientTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .setPaddingBottom(1f)
        )
        issuerAndClientTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .setPaddingBottom(1f)
        )

        // ROW 4:  CLIENT ADDRESS 2 ----------ADDRESS 3
        for (i in 1..<numberOfAddresses) {
            issuerAndClientTable.addCell(
                createClientRectangleAndContent(
                    createClientOrIssuerParagraph(
                        client,
                        font,
                        displayAllInfo = false,
                        addressIndex = i,
                        fontSize = fontSize
                    )
                )
            )
        }
    }
    // ROW X : Just adding space
    issuerAndClientTable.addCell(
        Cell().setBorder(Border.NO_BORDER)
            .setPaddingBottom(25f)
    )
    issuerAndClientTable.addCell(
        Cell().setBorder(Border.NO_BORDER)
            .setPaddingBottom(25f)
    )

    return issuerAndClientTable
}

private fun createAddressTitle(
    addresses: List<AddressState>,
    index: Int,
    fontSize: Float,
): Cell {
    return Cell()
        .setBorder(Border.NO_BORDER)
        .add(
            Paragraph(
                when (index) {
                    0 -> addresses.first().addressTitle?.text ?: ""
                    else -> addresses[index].addressTitle?.text ?: ""
                }
            )
                .setFontColor(ColorConstants.DARK_GRAY)
                .setFixedLeading(6F)
                .setFontSize(fontSize)
        )
        .setTextAlignment(TextAlignment.CENTER)
}

private fun createClientRectangleAndContent(content: List<Paragraph>): Cell {
    val cell = Cell()
        .setBorder(Border.NO_BORDER)
        .setTextAlignment(TextAlignment.CENTER)
        .setPaddingBottom(8f)
    content.forEach {
        cell.add(it)
    }
    cell.setNextRenderer(RoundedCellRenderer(cell, ColorConstants.LIGHT_GRAY, false))
    return cell
}

private fun createClientOrIssuerParagraph(
    clientOrIssuer: ClientOrIssuerState?,
    font: PdfFont,
    displayAllInfo: Boolean = true,
    addressIndex: Int = 0,
    fontSize: Float,
): MutableList<Paragraph> {
    var listToReturn: MutableList<Paragraph> = mutableListOf()
    val address = clientOrIssuer?.addresses?.get(addressIndex)

    val nameAndAddress = Paragraph()
        .setFixedLeading(12F)
        .setPaddingTop(if (clientOrIssuer?.type == ClientOrIssuerType.DOCUMENT_CLIENT) 10f else 0f)
        .setPaddingBottom(5f)
    if (displayAllInfo) {
        val issuerFirstName = clientOrIssuer?.firstName?.text?.let { Text("$it ").setFont(font) }
        val issuerName = Text(clientOrIssuer?.name?.text + "\n").setFont(font)

        issuerFirstName?.let {
            nameAndAddress.add(it)
        }
        issuerName?.let {
            nameAndAddress.add(it)
        }
    }
    listToReturn.add(nameAndAddress)

    if (!address?.addressLine1?.text.isNullOrEmpty()) {
        nameAndAddress.add(address?.addressLine1?.text + "\n")
    }
    if (!address?.addressLine2?.text.isNullOrEmpty()) {
        nameAndAddress.add(address?.addressLine2?.text + "\n")
    }
    if (!address?.zipCode?.text.isNullOrEmpty() ||
        !address?.city?.text.isNullOrEmpty()
    ) {
        nameAndAddress.add(address?.zipCode?.text + " " + address?.city?.text + "\n")
    }

    if (displayAllInfo) {
        val numberAndEmail = Paragraph()
            .setFixedLeading(12F)
            .setPaddingBottom(5f)
        val issuerPhone = clientOrIssuer?.phone?.text?.let { Text(it + "\n") }
        issuerPhone?.let {
            numberAndEmail.add(it)
        }
        clientOrIssuer?.emails?.forEach { emailState ->
            if (emailState.email.text.isNotEmpty()) {
                numberAndEmail.add(Text(emailState.email.text + "\n"))
            }
        }
        listToReturn.add(numberAndEmail)

        val companyInfo = Paragraph()
            .setFixedLeading(12F)
            .setPaddingBottom(4f)
        val issuerCompanyLabel1 = clientOrIssuer?.companyId1Number?.text?.let {
            Text(clientOrIssuer.companyId1Label?.text + " : " + it + "\n")
        }
        val issuerCompanyLabel2 = clientOrIssuer?.companyId2Number?.text?.let {
            Text(clientOrIssuer.companyId2Label?.text + " : " + it + "\n")
        }
        val issuerCompanyLabel3 = clientOrIssuer?.companyId3Number?.text?.let {
            Text(clientOrIssuer.companyId3Label?.text + " : " + it + "\n")
        }
        issuerCompanyLabel1?.let {
            companyInfo.add(it)
        }
        issuerCompanyLabel2?.let {
            companyInfo.add(it)
        }
        issuerCompanyLabel3?.let {
            companyInfo.add(it)
        }
        listToReturn.add(companyInfo)
    }
    return listToReturn
}

private fun createReference(
    text: String,
    font: PdfFont,
): Paragraph {
    val reference = Text(Strings.get(R.string.document_reference) + " : ").setFont(font)
    return Paragraph(reference)
        .add(text)
}

private fun createFreeText(
    text: String,
    font: PdfFont,
): Paragraph {
    return Paragraph(text).setFont(font)
}

private fun createProductsTable(
    products: List<DocumentProductState>,
    fontRegular: PdfFont,
    fontBold: PdfFont,
): Table? {
    try {
        val displayUnitColumn = products.any { !it.unit?.text.isNullOrEmpty() }

        val columnWidth = if (displayUnitColumn) floatArrayOf(45f, 9f, 13f, 8f, 12f, 13f)
        else floatArrayOf(45f, 9f, 8f, 12f, 13f)

        val productsTable = Table(UnitValue.createPercentArray(columnWidth))
            .useAllAvailableWidth()
            .setFixedLayout()

        productsTable
            .addCustomCell(
                Strings.get(R.string.document_table_description),
                alignment = TextAlignment.LEFT,
                isBold = true,
                fontBold = fontBold,
                fontRegular = fontRegular
            )
            .addCustomCell(
                Strings.get(R.string.document_table_quantity),
                isBold = true,
                fontBold = fontBold,
                fontRegular = fontRegular
            )

        if (displayUnitColumn) {
            productsTable.addCustomCell(
                Strings.get(R.string.document_table_unit),
                isBold = true,
                fontBold = fontBold,
                fontRegular = fontRegular
            )
        }

        productsTable.addCustomCell(
            Strings.get(R.string.document_table_tax_rate),
            isBold = true,
            fontBold = fontBold,
            fontRegular = fontRegular
        )
            .addCustomCell(
                Strings.get(R.string.document_table_unit_price_without_tax),
                isBold = true,
                fontBold = fontBold,
                fontRegular = fontRegular
            )
            .addCustomCell(
                Strings.get(R.string.document_table_total_price_without_tax),
                isBold = true,
                fontBold = fontBold,
                fontRegular = fontRegular
            )

        val linkedDeliveryNotes = getLinkedDeliveryNotes(products)

        if (linkedDeliveryNotes.isNotEmpty()) {
            linkedDeliveryNotes.forEach { docNumberAndDate ->
                addDeliveryNoteRow(
                    productsTable,
                    docNumberAndDate,
                    fontBold = fontBold,
                    fontRegular = fontRegular
                )
                addProductsToTable(
                    products.filter { it.linkedDocNumber == docNumberAndDate.first },
                    productsTable,
                    fontBold = fontBold,
                    fontRegular = fontRegular,
                    displayUnitColumn = displayUnitColumn
                )
            }
        } else {
            addProductsToTable(
                products,
                productsTable,
                fontBold = fontBold,
                fontRegular = fontRegular,
                displayUnitColumn = displayUnitColumn
            )
        }
        return productsTable
    } catch (e: Exception) {
        //Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
    return null
}

private fun addDeliveryNoteRow(
    productsTable: Table,
    docNumberAndDate: Pair<String?, String?>,
    fontRegular: PdfFont,
    fontBold: PdfFont,
) {
    productsTable.addCustomCell(
        text = docNumberAndDate.first + " - " + docNumberAndDate.second?.substringBefore(" "),
        alignment = TextAlignment.LEFT,
        isBold = true,
        isSpan = true,
        fontBold = fontBold,
        fontRegular = fontRegular,
    )
}

private fun addProductsToTable(
    products: List<DocumentProductState>,
    productsTable: Table,
    fontRegular: PdfFont,
    fontBold: PdfFont,
    displayUnitColumn: Boolean,
) {
    val spacingBetweenTextLines = 10F
    val spacingBetweenNameAndDescription = 4F
    products.forEach {
        // To put description in italic + have a larger spacing between name & description
        val itemName = Paragraph(Text(it.name.text)).setFixedLeading(spacingBetweenTextLines)
        val spacing = Paragraph("\n").setFixedLeading(spacingBetweenNameAndDescription)
        var itemDescription: Paragraph? = null
        it.description?.let {
            itemDescription = Paragraph(
                Text(it.text)
                    .simulateItalic()
            ).setFixedLeading(spacingBetweenTextLines)
        }
        val nameAndDescription: List<Paragraph?> = listOf(itemName, spacing, itemDescription)

        productsTable.addCustomCell(
            paragraphs = nameAndDescription,
            alignment = TextAlignment.LEFT,
            fontBold = fontBold,
            fontRegular = fontRegular
        )
        productsTable.addCustomCell(
            text = it.quantity.stripTrailingZeros().toPlainString().replace(".", ","),
            fontBold = fontBold,
            fontRegular = fontRegular
        )

        if (displayUnitColumn)
            productsTable.addCustomCell(
                text = it.unit?.text,
                fontBold = fontBold,
                fontRegular = fontRegular
            )
        productsTable.addCustomCell(text = it.taxRate?.let {
            it.stripTrailingZeros().toPlainString().replace(".", ",") + "%"
        } ?: " - ", fontBold = fontBold, fontRegular = fontRegular)


        productsTable.addCustomCell(
            text = it.priceWithoutTax?.setScale(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
                ?.toPlainString()?.replace(".", ",")?.plus(Strings.get(R.string.currency)) ?: "",
            fontBold = fontBold,
            fontRegular = fontRegular
        )
        productsTable.addCustomCell(
            text = it.priceWithoutTax?.let { price ->
                price * it.quantity
            }?.setScale(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
                ?.toPlainString()?.replace(".", ",")?.plus(Strings.get(R.string.currency)) ?: "",
            fontBold = fontBold,
            fontRegular = fontRegular
        )
    }
}

private fun createPrices(
    font: PdfFont,
    documentPrices: DocumentTotalPrices,
    fontSize: Float,
): Table {
    val pricesArray = listOf(
        PriceRow(rowDescription = "TOTAL_WITHOUT_TAX"),
        PriceRow(rowDescription = "TOTAL_WITH_TAX"),
    )

    val pricesColumnsWidth = floatArrayOf(90f, 10f)
    val pricesTable = Table(UnitValue.createPercentArray(pricesColumnsWidth))
        .useAllAvailableWidth()
        .setTextAlignment(TextAlignment.RIGHT)
        .setPaddingBottom(8f)
        .setPaddingRight(4f)

    if (pricesArray.any { it.rowDescription == PricesRowName.TOTAL_WITHOUT_TAX.name }) {
        pricesTable.addCellInPrices(Paragraph(Strings.get(R.string.document_total_without_tax)))

        val totalWithoutTax =
            Paragraph(documentPrices.totalPriceWithoutTax?.toPlainString()?.replace(".", ",") ?: " - ")
        totalWithoutTax.add(Strings.get(R.string.currency))
        pricesTable.addCellInPrices(totalWithoutTax)
    }

    documentPrices.totalAmountsOfEachTax
        ?.sortedBy { it.first }
        ?.forEach { (taxRate, taxAmount) ->
            pricesTable.addCellInPrices(
                Paragraph(
                    Strings.get(R.string.document_tax) + " " + taxRate.stripTrailingZeros().toPlainString().replace(".", ",") + "% : "
                )
            )
            pricesTable.addCellInPrices(Paragraph(taxAmount.toPlainString().replace(".", ",") + Strings.get(R.string.currency)))
        }
    if (pricesArray.any { it.rowDescription == PricesRowName.TOTAL_WITH_TAX.name }) {
        pricesTable.addCellInPrices(
            Paragraph(
                Strings.get((R.string.document_total_with_tax)) + " "
            ).setFont(font).setFontSize(fontSize)
        )
        val totalWithTax =
            Paragraph(documentPrices.totalPriceWithTax?.toPlainString()?.replace(".", ",") ?: " - ")
        totalWithTax.add(Strings.get(R.string.currency))
        pricesTable.addCellInPrices(totalWithTax.setFont(font).setFontSize(fontSize))
    }

    return pricesTable
}

private fun createDueDate(font: PdfFont, date: String, fontSize: Float): Paragraph {
    return Paragraph(Strings.get(R.string.invoice_pdf_due_date) + " : " + date)
        .setFixedLeading(16F)
        .setPaddingTop(12f)
        .setTextAlignment(TextAlignment.CENTER)
        .setFont(font)
        .setFontSize(fontSize)
}

private fun createPaidStamp(context: Context): Image? {
    try {
        val inputStream: InputStream = context.resources.assets.open("img_paid.png")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val img = Image(ImageDataFactory.create(stream.toByteArray())).apply {
            //setAutoScale(true)
            setWidth(180f)
        }
        img.setFixedPosition(
            220f,
            470f
        )
        return img
    } catch (e: Exception) {
        //Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
    return null
}


private fun createFooter(text: String, fontSize: Float): Paragraph {
    return Paragraph(text)
        .setFontSize(fontSize)
        .setFixedLeading(14F)
        .setTextAlignment(TextAlignment.CENTER)
}

private fun Table.addCustomCell(
    text: String? = null,
    paragraphs: List<Paragraph?>? = null,
    alignment: TextAlignment = TextAlignment.RIGHT,
    isBold: Boolean = false,
    isSpan: Boolean = false, //Used to merge cells,
    fontBold: PdfFont,
    fontRegular: PdfFont,
): Table {
    val paddingLeftOrRight = 6f
    val paddingTopOrBottom = if (isBold) 5f else 3f
    val colSpan = if (isSpan) 6 else 1

    val cell = Cell(1, colSpan)
        .setTextAlignment(alignment)
        .setPaddingLeft(paddingLeftOrRight)
        .setPaddingRight(paddingLeftOrRight)
        .setPaddingTop(paddingTopOrBottom)
        .setPaddingBottom(paddingTopOrBottom)
        .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 1f))
        .setFont(if (isBold) fontBold else fontRegular)

    if (text != null) {
        cell.add(Paragraph(text).setFixedLeading(10F))
    } else {
        paragraphs?.filterNotNull()?.forEach {
            cell.add(it)
        }
    }
    return this.addCell(cell)
}

private fun Table.addCellInPrices(
    paragraph: Paragraph? = null,
): Table {
    return this.addCell(
        Cell().add(paragraph)
            .setTextAlignment(TextAlignment.RIGHT)
            .setBorder(Border.NO_BORDER)
            .setPaddingTop(-4f)
            .setPaddingBottom(6f)
    )
}

data class PriceRow(
    var rowDescription: String,
)

fun getFileUri(context: Context, fileName: String): Uri? {
    // Sur Android 10+, utiliser l'Uri cachée si disponible
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Vérifier d'abord le cache
        if (lastExportedPdfFileName == fileName && lastExportedPdfUri != null) {
            return lastExportedPdfUri
        }
        // Sinon chercher dans MediaStore
        val uri = findMediaStoreUri(context, fileName)
        if (uri != null) return uri
    }

    // Fallback: FileProvider pour les fichiers locaux
    var uri: Uri? = null
    val file = File(getFilePath(context, fileName))
    if (file.exists()) {
        try {
            uri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                file
            )
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error getFileUri: ${e.message}")
        }
    }
    return uri
}

// Chercher un fichier dans MediaStore par nom
private fun findMediaStoreUri(context: Context, fileName: String): Uri? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

    val projection = arrayOf(MediaStore.Downloads._ID)
    val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
    val selectionArgs = arrayOf(fileName)

    context.contentResolver.query(
        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
            return android.content.ContentUris.withAppendedId(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                id
            )
        }
    }
    return null
}

// Sauvegarder un fichier dans Downloads/g8 via MediaStore (Android 10+)
fun saveToDownloadsWithMediaStore(context: Context, sourceFile: File, fileName: String): Uri? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

    // Supprimer le fichier existant s'il y en a un
    deleteFromMediaStore(context, fileName)

    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        put(MediaStore.Downloads.RELATIVE_PATH, "Download/g8")
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            sourceFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    return uri
}

// Supprimer un fichier de MediaStore
private fun deleteFromMediaStore(context: Context, fileName: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false

    val uri = findMediaStoreUri(context, fileName) ?: return false
    return try {
        context.contentResolver.delete(uri, null, null) > 0
    } catch (e: Exception) {
        Log.e(ContentValues.TAG, "Error deleteFromMediaStore: ${e.message}")
        false
    }
}

// Obtenir le chemin d'un fichier temporaire dans le cache
fun getTempFilePath(context: Context, fileName: String): String {
    val cacheDir = File(context.cacheDir, "pdf_temp")
    if (!cacheDir.exists()) cacheDir.mkdirs()
    return File(cacheDir, fileName).absolutePath
}

// Obtenir un fichier temporaire
fun getTempFile(context: Context, fileName: String): File {
    val cacheDir = File(context.cacheDir, "pdf_temp")
    if (!cacheDir.exists()) cacheDir.mkdirs()
    return File(cacheDir, fileName)
}

// Supprimer un fichier temporaire
private fun deleteTempFile(context: Context, fileName: String): Boolean {
    val file = getTempFile(context, fileName)
    return if (file.exists()) file.delete() else true
}

fun getFilePath(context: Context, fileName: String): String {
    // Sur Android 10+, utiliser le cache pour les fichiers temporaires
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return getTempFilePath(context, fileName)
    }
    // Android 9 et moins: utiliser Downloads/g8
    var path = ""
    getFileLegacy(fileName)?.let {
        path = it.absolutePath
    }
    return path
}

// Ancienne méthode pour Android 9 et moins
private fun getFileLegacy(fileName: String): File? {
    val folder =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    val directory = File(folder, "g8")
    if (directory.exists() && directory.isDirectory) {
        //println("Directory exists.")
    } else {
        val result = directory.mkdir()
        if (result) {
            //Log.e(ContentValues.TAG, "Directory created successfully: ${directory.absolutePath}")
        } else {
            //Log.e(ContentValues.TAG, "Failed to create directory.")
            return null
        }
    }
    return File("$folder/g8/$fileName")
}

private fun deleteFile(context: Context, fileName: String): Boolean {
    // Sur Android 10+, supprimer via MediaStore
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Supprimer aussi du cache si présent
        deleteTempFile(context, fileName)
        return deleteFromMediaStore(context, fileName)
    }
    // Android 9 et moins
    var isDeleted = false
    val folder =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    val file = File("$folder/g8/$fileName")
    if (file.exists()) {
        isDeleted = file.delete()
    }
    return isDeleted
}


class RoundedCellRenderer(modelElement: Cell, color: Color, isColoredBackground: Boolean) :
    CellRenderer(modelElement) {
    private var isColoredBackground: Boolean
    private var color: Color

    init {
        modelElement.setBorder(Border.NO_BORDER)
        this.isColoredBackground = isColoredBackground
        this.color = color
    }

    override fun drawBackground(drawContext: DrawContext) {
        val rect: Rectangle = occupiedAreaBBox
        val canvas: PdfCanvas = drawContext.canvas
        canvas
            .saveState()
            .roundRectangle(
                rect.left + 2.5,
                rect.bottom + 2.5,
                rect.width - 5.0,
                rect.height - 5.0,
                24.0
            )
            .setStrokeColor(color)
            .setLineWidth(1.5f)
        if (isColoredBackground) {
            canvas.setFillColor(color)
                .fillStroke()
        } else {
            canvas.stroke()
        }
        canvas.restoreState()
    }
}