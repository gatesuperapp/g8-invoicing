package com.a4a.g8invoicing.ui.shared

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.screens.shared.DocumentProductsRows
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.screens.shared.FooterRowName
import com.a4a.g8invoicing.ui.screens.shared.LinkedDeliveryNoteRow
import com.a4a.g8invoicing.ui.screens.shared.getLinkedDeliveryNotes
import com.a4a.g8invoicing.ui.states.DocumentPrices
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
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
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import com.itextpdf.layout.renderer.CellRenderer
import com.itextpdf.layout.renderer.DrawContext
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode


fun createPdfWithIText(inputDocument: DocumentState) {
    val fileNameBeforeNumbering = "${inputDocument.documentNumber.text}_temp.pdf"


    val writer = PdfWriter(getFilePath(fileNameBeforeNumbering))
    writer.isFullCompression
    val pdfDocument = PdfDocument(writer)

    val fontRegular: PdfFont = PdfFontFactory.createFont("assets/helvetica.ttf")
    val fontBold: PdfFont = PdfFontFactory.createFont("assets/helveticabold.ttf")

    val document = Document(pdfDocument, PageSize.A4)
        .setFont(fontRegular)
        .setFontSize(10F)

    document.add(
        createTitle(
            inputDocument.documentNumber.text,
            inputDocument.documentType,
            fontRegular
        )
    )
    document.add(createDate(inputDocument.documentDate.substringBefore(" ")))
    document.add(
        createIssuerAndClientTable(
            inputDocument.documentIssuer,
            inputDocument.documentClient,
            fontBold,
            inputDocument.documentType
        )
    )
    inputDocument.orderNumber?.let {
        if (it.text.isNotEmpty()) {
            document.add(createReference(it.text, fontBold))
        }
    }
    inputDocument.documentProducts?.let {
        document.add(createProductsTable(it, fontBold = fontBold, fontRegular = fontRegular))
    }
    inputDocument.documentPrices?.let {
        document.add(createPrices(fontBold, it))
    }

    if (inputDocument is InvoiceState) {
        document.add(createDueDate(fontBold, inputDocument.dueDate.substringBefore(" ")))
        document.add(createFooter(inputDocument.footerText.text))
    }

    document.close()

    writer.isCloseStream
    writer.close()
    pdfDocument.close()

    addPageNumbering(inputDocument, fileNameBeforeNumbering)
}

fun addPageNumbering(inputDocument: DocumentState, fileNameBeforeNumbering: String) {
    val fontRegular: PdfFont = PdfFontFactory.createFont("assets/helvetica.ttf")

    val finalFileName = "${inputDocument.documentNumber.text}.pdf"
     getFile(fileNameBeforeNumbering)?.let {
     if (it.exists() && it.isFile) {
         try {
             addPageNumberingAndSaveFile(fileNameBeforeNumbering, finalFileName, fontRegular)
         } catch (e: Exception) {
             Log.e(ContentValues.TAG, "Error: ${e.message}")
         }
     }
 }
}

private fun createTitle(
    documentNumber: String,
    documentType: DocumentType? = null,
    font: PdfFont,
): Paragraph {
    val title = when (documentType) {
        DocumentType.INVOICE -> Strings.get(R.string.invoice_number)
        DocumentType.DELIVERY_NOTE -> Strings.get(R.string.delivery_note_number)
        else -> null
    }

    return Paragraph(title + documentNumber)
        .setFont(font)
        .setFontSize(20F)
        .setMarginBottom(-6F)
}

private fun createDate(date: String): Paragraph {
    return Paragraph(Strings.get(R.string.document_date) + " : " + date).setFontSize(14F)
}

private fun createIssuerAndClientTable(
    issuer: DocumentClientOrIssuerState?,
    client: DocumentClientOrIssuerState?,
    font: PdfFont,
    documentType: DocumentType,
): Table {
    val issuerAndClientTable = Table(2)
        .useAllAvailableWidth()
        .setMarginBottom(40F)
        .setFixedLayout()

    val issuerFirstName = issuer?.firstName?.text?.let { Text("$it ").setFont(font) }
    val issuerName = Text(issuer?.name?.text + "\n").setFont(font)
    val issuerAddress1 = issuer?.address1?.text?.let { Text(it + "\n") }
    val issuerAddress2 = issuer?.address2?.text?.let { Text(it + "\n") }
    val issuerZipCode = issuer?.zipCode?.text?.let { Text("$it ") }
    val issuerCity = issuer?.city?.text?.let { Text(it + "\n") }
    val issuerPhone = issuer?.phone?.text?.let { Text(it + "\n") }
    val issuerCompanyLabel1 = issuer?.companyId1Number?.text?.let {
        Text(issuer.companyId1Label?.text + " : " + it + "\n")
    }
    val issuerCompanyLabel2 = issuer?.companyId2Number?.text?.let {
        Text(issuer.companyId2Label?.text + " : " + it + "\n")
    }
    val issuer = Paragraph()
        .setFixedLeading(16F)
        .setPaddingRight(40f)
    issuerFirstName?.let {
        issuer.add(it)
    }
    issuerName?.let {
        issuer.add(it)
    }
    issuerAddress1?.let {
        issuer.add(it)
    }
    issuerAddress2?.let {
        issuer.add(it)
    }
    issuerZipCode?.let {
        issuer.add(it)
    }
    issuerCity?.let {
        issuer.add(it)
    }
    issuerPhone?.let {
        issuer.add(it)
    }
    issuerCompanyLabel1?.let {
        issuer.add(it)
    }
    issuerCompanyLabel2?.let {
        issuer.add(it)
    }

    val clientFirstName = client?.firstName?.text?.let { Text("$it ").setFont(font) }
    val clientName = Text(client?.name?.text + "\n").setFont(font)
    val clientAddress1 = client?.address1?.text?.let { Text(it + "\n") }
    val clientAddress2 = client?.address2?.text?.let { Text(it + "\n") }
    val clientZipCode = client?.zipCode?.text?.let { Text("$it ") }
    val clientCity = client?.city?.text?.let { Text(it + "\n") }
    val clientPhone = client?.phone?.text?.let { Text(it + "\n") }
    val clientCompanyLabel1 = client?.companyId1Number?.text?.let {
        Text(client.companyId1Label?.text + " : " + it + "\n")
    }
    val clientCompanyLabel2 = client?.companyId2Number?.text?.let {
        Text(client.companyId2Label?.text + " : " + it + "\n")
    }
    val client = Paragraph()
        .setFixedLeading(16F)
        .setPadding(12f)
    clientFirstName?.let {
        client.add(it)
    }
    clientName?.let {
        client.add(it)
    }
    clientAddress1?.let {
        client.add(it)
    }
    clientAddress2?.let {
        client.add(it)
    }
    clientZipCode?.let {
        client.add(it)
    }
    clientCity?.let {
        client.add(it)
    }
    clientPhone?.let {
        client.add(it)
    }
    clientCompanyLabel1?.let {
        client.add(it)
    }
    clientCompanyLabel2?.let {
        client.add(it)
    }

    issuerAndClientTable.addCell(Cell().setBorder(Border.NO_BORDER))
    issuerAndClientTable.addCell(
        Cell()
            .setBorder(Border.NO_BORDER)
            .add(
                Paragraph(
                    when (documentType) {
                        DocumentType.INVOICE -> Strings.get(R.string.document_recipient)
                        DocumentType.DELIVERY_NOTE -> Strings.get(R.string.document_recipient)
                        else -> null
                    }
                )
                    .setFontColor(ColorConstants.DARK_GRAY)
            )
            .setTextAlignment(TextAlignment.CENTER)
    )
    issuerAndClientTable.addCell(
        Cell()
            .setBorder(Border.NO_BORDER)
            .add(issuer)
    )

    val cell = Cell()
        .setBorder(Border.NO_BORDER)
        .add(client)
        .setTextAlignment(TextAlignment.CENTER)
    cell.setNextRenderer(RoundedCellRenderer(cell, ColorConstants.LIGHT_GRAY, false))

    return issuerAndClientTable.addCell(cell)
}

private fun createReference(orderNumber: String, font: PdfFont): Paragraph {
    val reference = Text(Strings.get(R.string.document_order_number) + " : ").setFont(font)
    return Paragraph(reference)
        .add(orderNumber)
}

private fun createProductsTable(products: List<DocumentProductState>, fontRegular: PdfFont, fontBold: PdfFont): Table {
    val columnWidth = floatArrayOf(49f, 10f, 10f, 10f, 10f, 11f)
    val productsTable = Table(UnitValue.createPercentArray(columnWidth))
        .useAllAvailableWidth()
        .setMarginBottom(10f)
        .setFixedLayout()

    productsTable
        .addCustomCell(Strings.get(R.string.document_table_description), alignment = TextAlignment.LEFT, isBold = true, fontBold = fontBold, fontRegular = fontRegular)
        .addCustomCell(Strings.get(R.string.document_table_quantity), isBold = true, fontBold = fontBold, fontRegular = fontRegular)
        .addCustomCell(Strings.get(R.string.document_table_unit), isBold = true, fontBold = fontBold, fontRegular = fontRegular)
        .addCustomCell(Strings.get(R.string.document_table_tax_rate), isBold = true, fontBold = fontBold, fontRegular = fontRegular)
        .addCustomCell(Strings.get(R.string.document_table_unit_price_without_tax), isBold = true, fontBold = fontBold, fontRegular = fontRegular)
        .addCustomCell(Strings.get(R.string.document_table_total_price_without_tax), isBold = true, fontBold = fontBold, fontRegular = fontRegular)

    val linkedDeliveryNotes = getLinkedDeliveryNotes(products)

    if (linkedDeliveryNotes.isNotEmpty()) {
        linkedDeliveryNotes.forEach { docNumberAndDate ->
            addDeliveryNoteRow(productsTable, docNumberAndDate, fontBold = fontBold, fontRegular = fontRegular)
            addProductsToTable(
                products.filter { it.linkedDocNumber == docNumberAndDate.first },
                productsTable, fontBold = fontBold, fontRegular = fontRegular
            )
        }
    } else {
        addProductsToTable(products, productsTable, fontBold = fontBold, fontRegular = fontRegular)
    }
    return productsTable
}

private fun addDeliveryNoteRow(productsTable: Table, docNumberAndDate: Pair<String?, String?>, fontRegular: PdfFont, fontBold: PdfFont) {
    productsTable.addCustomCell(
        text = docNumberAndDate.first + " - " + docNumberAndDate.second?.substringBefore(" "),
        alignment = TextAlignment.LEFT,
        isBold = true,
        isSpan = true,
        fontBold = fontBold, fontRegular = fontRegular
    )
}

private fun addProductsToTable(products: List<DocumentProductState>, productsTable: Table, fontRegular: PdfFont, fontBold: PdfFont) {
    products.forEach {
        val itemName = Text(it.name.text)
        val itemDescription = Text(it.description?.text).setItalic()
        val item = Paragraph(itemName)
            .add("\n")
            .add(itemDescription)
            .setFixedLeading(14F)
        productsTable.addCustomCell(paragraph = item, alignment = TextAlignment.LEFT, fontBold = fontBold, fontRegular = fontRegular)
        productsTable.addCustomCell(text = it.quantity.toString(), fontBold = fontBold, fontRegular = fontRegular)
        productsTable.addCustomCell(text = it.unit?.text, fontBold = fontBold, fontRegular = fontRegular)
        productsTable.addCustomCell(text = it.taxRate?.let {
            it.setScale(0, RoundingMode.HALF_UP).toString() + "%"
        } ?: " - ", fontBold = fontBold, fontRegular = fontRegular)

        var priceWithoutTax = BigDecimal(0)
        it.priceWithTax?.let { priceWithTax ->
            priceWithoutTax =
                (priceWithTax - priceWithTax * (it.taxRate ?: BigDecimal(0)) / BigDecimal(100))

        }
        productsTable.addCustomCell(
            text = priceWithoutTax.setScale(2, RoundingMode.HALF_UP)
                .toString() + Strings.get(R.string.currency), fontBold = fontBold, fontRegular = fontRegular
        )
        productsTable.addCustomCell(
            text = (priceWithoutTax * it.quantity).setScale(2, RoundingMode.HALF_UP)
                .toString() + Strings.get(R.string.currency), fontBold = fontBold, fontRegular = fontRegular
        )
    }
}

private fun createPrices(font: PdfFont, documentPrices: DocumentPrices): Table {
    val footerArray = listOf(
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

    val footerColumnsWidth = floatArrayOf(90f, 10f)
    val footerTable = Table(UnitValue.createPercentArray(footerColumnsWidth))
        .useAllAvailableWidth()
        .setTextAlignment(TextAlignment.RIGHT)
        .setPaddingBottom(8f)

    if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITHOUT_TAX.name }) {
        footerTable.addCellInFooter(Paragraph(Strings.get(R.string.document_total_without_tax)))

        val totalWithoutTax = Paragraph(documentPrices.totalPriceWithoutTax?.toString() ?: " - ")
        totalWithoutTax.add(Strings.get(R.string.currency))
        footerTable.addCellInFooter(totalWithoutTax)

    }

    if (footerArray.any { it.rowDescription.contains("TAXES") }) {
        val taxes = footerArray
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
                footerTable.addCellInFooter(
                    Paragraph(
                        Strings.get(R.string.document_tax) + " " + it.first.setScale(
                            0,
                            RoundingMode.HALF_UP
                        ).toString() + "% : "
                    )
                )
                footerTable.addCellInFooter(Paragraph(it.second.toString() + Strings.get(R.string.currency)))
            }
        }
    }
    if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITH_TAX.name }) {
        footerTable.addCellInFooter(
            Paragraph(
                Strings.get((R.string.document_total_with_tax)) + " "
            ).setFont(font)
        )
        val totalWithTax = Paragraph(documentPrices.totalPriceWithTax?.toString() ?: " - ")
        totalWithTax.add(Strings.get(R.string.currency))
        footerTable.addCellInFooter(totalWithTax.setFont(font))
    }

    return footerTable
}

private fun createDueDate(font: PdfFont, date: String): Paragraph {
    return Paragraph(Strings.get(R.string.invoice_pdf_due_date) + " : " + date)
        .setFixedLeading(16F)
        .setPaddingTop(12f)
        .setTextAlignment(TextAlignment.CENTER)
        .setFont(font)
}

private fun createFooter(text: String): Paragraph {
    return Paragraph(text)
        .setFixedLeading(16F)
        .setTextAlignment(TextAlignment.CENTER)
}

private fun Table.addCustomCell(
    text: String? = null,
    paragraph: Paragraph? = null,
    alignment: TextAlignment = TextAlignment.RIGHT,
    isBold: Boolean = false,
    isSpan: Boolean = false, //Used to merge cells,
    fontBold: PdfFont,
    fontRegular: PdfFont
): Table {
    val paddingLeft = 6f
    val paddingRight = 6f
    val paddingTop = 4f

    val textToAdd = if (text != null) {
        Paragraph(text).setFixedLeading(14F)
    } else paragraph

    val colSpan = if (isSpan) 6 else 1

    return this.addCell(
        Cell(1, colSpan).add(textToAdd)
            .setTextAlignment(alignment)
            .setPaddingLeft(paddingLeft)
            .setPaddingRight(paddingRight)
            .setPaddingTop(paddingTop)
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 1f))
            .setFont(if (isBold) fontBold else fontRegular)
    )
}

private fun Table.addCellInFooter(
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

@Throws(Exception::class)
private fun addPageNumberingAndSaveFile(previousFileName: String, finalFileName: String, fontRegular: PdfFont) {

    val pdfDoc = PdfDocument(
        PdfReader(getFilePath(previousFileName)),
        PdfWriter(getFilePath(finalFileName))
    )
    val doc = Document(pdfDoc)

    val numberOfPages = pdfDoc.numberOfPages

    if (numberOfPages > 1) {
        for (i in 1..numberOfPages) {
            doc.showTextAligned(
                Paragraph(String.format("%s/%s", i, numberOfPages))
                    .setFont(fontRegular),
                570f, 34f, i, TextAlignment.RIGHT, VerticalAlignment.TOP, 0f
            )
        }
    }

    deleteFile(previousFileName)
    doc.close()
    pdfDoc.close()
}


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
        Log.e(ContentValues.TAG, "Error: ${e.message}")
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
        println("Directory exists.")
    } else {
        val result = directory.mkdir()
        if (result) {
            Log.e(ContentValues.TAG, "Directory created successfully: ${directory.absolutePath}")
        } else {
            Log.e(ContentValues.TAG, "Failed to create directory.")
            return null
        }
    }
    return File("$folder/g8/$fileName")
}

private fun deleteFile(fileName: String) {
    val folder =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    val file = File("$folder/g8/$fileName")
    if (file.exists()) {
        file.delete()
        Log.e(ContentValues.TAG, "File deleted successfully: ${file.absolutePath}")
    } else {
        Log.e(ContentValues.TAG, "Error deleting file. ${file.absolutePath}")
    }
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