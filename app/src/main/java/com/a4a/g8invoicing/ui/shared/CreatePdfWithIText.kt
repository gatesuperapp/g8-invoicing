package com.a4a.g8invoicing.ui.shared

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.PricesRowName
import com.a4a.g8invoicing.ui.screens.shared.getLinkedDeliveryNotes
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentPrices
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
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
import java.math.BigDecimal
import java.math.RoundingMode


fun createPdfWithIText(inputDocument: DocumentState, context: Context): String {
    var fileNameBeforeNumbering = "${inputDocument.documentNumber.text}_temp.pdf"

    // Check if this file already exists and delete it
    var isDeleted = false
    getFile(fileNameBeforeNumbering)?.let { file ->
        if (file.exists() && file.isFile) {
            isDeleted = deleteFile(fileNameBeforeNumbering)
        }
    }
    val newName = File(fileNameBeforeNumbering).nameWithoutExtension
    var i = 1
    while (getFile(fileNameBeforeNumbering) != null && getFile(fileNameBeforeNumbering)!!.exists() && !isDeleted) {
        // When the pdf has been saved the day or more before
        // it cant be erased anymore, throwing a java.nio.file.NoSuchFileException
        // because there's no read/write permission. So we're renaming the file in such case
        fileNameBeforeNumbering = newName.substringBefore(" - ") + " - " + (i) + ".pdf"
        isDeleted = deleteFile(fileNameBeforeNumbering)
        i += 1
    }

    val writer = PdfWriter(getFilePath(fileNameBeforeNumbering))
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
    inputDocument.documentPrices?.let {
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
    var finalFileName = "${inputDocument.documentNumber.text}.pdf"
    var isDeleted = false
    getFile(finalFileName)?.let { file ->
        if (file.exists() && file.isFile) {
            isDeleted = deleteFile(finalFileName)
        }
    }
    val newName = File(finalFileName).nameWithoutExtension
    var i = 1
    while (getFile(finalFileName) != null && getFile(finalFileName)!!.exists() && !isDeleted) {
        // When the pdf has been saved the day or more before
        // it cant be erased anymore, throwing a java.nio.file.NoSuchFileException
        // because there's no read/write permission. So we're renaming the file in such case
        finalFileName = newName.substringBefore(" - ") + " - " + (i) + ".pdf"
        isDeleted = deleteFile(finalFileName)
        i += 1
    }


    getFile(fileNameBeforeNumbering)?.let {
        if (it.exists() && it.isFile) {
            try {
                val pdfDoc = PdfDocument(
                    PdfReader(getFilePath(fileNameBeforeNumbering)),
                    PdfWriter(getFilePath(finalFileName))
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

                deleteFile(fileNameBeforeNumbering)
                doc.close()
                pdfDoc.close()
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
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
        val issuerEmail = clientOrIssuer?.email?.text?.let { Text(it + "\n") }
        issuerPhone?.let {
            numberAndEmail.add(it)
        }
        issuerEmail?.let {
            numberAndEmail.add(it)
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
        val columnWidth = floatArrayOf(45f, 9f, 13f, 8f, 12f, 13f)
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
            .addCustomCell(
                Strings.get(R.string.document_table_unit),
                isBold = true,
                fontBold = fontBold,
                fontRegular = fontRegular
            )
            .addCustomCell(
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
                    fontRegular = fontRegular
                )
            }
        } else {
            addProductsToTable(
                products,
                productsTable,
                fontBold = fontBold,
                fontRegular = fontRegular,
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
                    .setItalic()
            ).setFixedLeading(spacingBetweenTextLines)
        }
        val paragraphs: List<Paragraph?> = listOf(itemName, spacing, itemDescription)

        productsTable.addCustomCell(
            paragraphs = paragraphs,
            alignment = TextAlignment.LEFT,
            fontBold = fontBold,
            fontRegular = fontRegular
        )
        productsTable.addCustomCell(
            text = it.quantity.stripTrailingZeros().toPlainString().replace(".", ","),
            fontBold = fontBold,
            fontRegular = fontRegular
        )
        productsTable.addCustomCell(
            text = it.unit?.text,
            fontBold = fontBold,
            fontRegular = fontRegular
        )
        productsTable.addCustomCell(text = it.taxRate?.let {
            it.setScale(0, RoundingMode.HALF_UP).toString() + "%"
        } ?: " - ", fontBold = fontBold, fontRegular = fontRegular)

        var priceWithoutTax = BigDecimal(0)
        it.priceWithTax?.let { priceWithTax ->
            priceWithoutTax =
                BigDecimal(priceWithTax.toDouble() / (1.0 + (it.taxRate ?: 0).toDouble() / 100.0))

        }
        productsTable.addCustomCell(
            text = priceWithoutTax.setScale(2, RoundingMode.HALF_UP)
                .toString().replace(".", ",") + Strings.get(R.string.currency),
            fontBold = fontBold,
            fontRegular = fontRegular
        )
        productsTable.addCustomCell(
            text = (priceWithoutTax * it.quantity).setScale(2, RoundingMode.HALF_UP)
                .toString().replace(".", ",") + Strings.get(R.string.currency),
            fontBold = fontBold,
            fontRegular = fontRegular
        )
    }
}

private fun createPrices(font: PdfFont, documentPrices: DocumentPrices, fontSize: Float): Table {
    val pricesArray = listOf(
        PriceRow(
            rowDescription = "TOTAL_WITHOUT_TAX"
        ),
        PriceRow(
            rowDescription = "TAXES_10"
        ),
        PriceRow(
            rowDescription = "TAXES_20"
        ),
        PriceRow(
            rowDescription = "TOTAL_WITH_TAX"
        ),
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
            Paragraph(documentPrices.totalPriceWithoutTax?.toString()?.replace(".", ",") ?: " - ")
        totalWithoutTax.add(Strings.get(R.string.currency))
        pricesTable.addCellInPrices(totalWithoutTax)
    }

    if (pricesArray.any { it.rowDescription.contains("TAXES") }) {
        val taxes = pricesArray
            .filter { it.rowDescription.contains("TAXES") }
            .map { it.rowDescription }.toMutableList()

        var taxesAmount = listOf<Int>()
        taxes.forEach {
            taxesAmount += it.removePrefix("TAXES_").toInt()
        }

        taxesAmount.forEach { tax ->
            documentPrices.totalAmountsOfEachTax?.firstOrNull {
                it.first.stripTrailingZeros() == BigDecimal(
                    tax
                ).stripTrailingZeros()
            }?.let {
                pricesTable.addCellInPrices(
                    Paragraph(
                        Strings.get(R.string.document_tax) + " " + it.first.setScale(
                            0,
                            RoundingMode.HALF_UP
                        ).toString() + "% : "
                    )
                )
                pricesTable.addCellInPrices(Paragraph(it.second.toString() + Strings.get(R.string.currency)))
            }
        }
    }
    if (pricesArray.any { it.rowDescription == PricesRowName.TOTAL_WITH_TAX.name }) {
        pricesTable.addCellInPrices(
            Paragraph(
                Strings.get((R.string.document_total_with_tax)) + " "
            ).setFont(font).setFontSize(fontSize)
        )
        val totalWithTax =
            Paragraph(documentPrices.totalPriceWithTax?.toString()?.replace(".", ",") ?: " - ")
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
    var uri: Uri? = null
    val file = File(getFilePath(fileName))
    try {
        uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )
    } catch (e: Exception) {
        //Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
    return uri
}

fun getFilePath(fileName: String): String {
    var path = ""
    getFile(fileName)?.let {
        path = it.absolutePath
    }
    return path
}

fun getFile(fileName: String): File? {
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

private fun deleteFile(fileName: String): Boolean {
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