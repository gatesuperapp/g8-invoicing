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
import com.a4a.g8invoicing.data.ClientOrIssuerState
import com.a4a.g8invoicing.ui.screens.FooterRowName
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentPrices
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.BorderRadius
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

const val fileNameBeforeNumbering = "BL1.pdf"
const val fileNameAfterNumbering = "BL.pdf"
const val fileName = "BL"

fun createPdfWithIText(deliveryNote: DeliveryNoteState, context: Context) {
    val writer = PdfWriter(getFilePath(fileNameBeforeNumbering))
    val pdfDocument = PdfDocument(writer)

    val dmRegular = PdfFontFactory.createFont("assets/DMSans-Regular.ttf")
    val dmMedium = PdfFontFactory.createFont("assets/DMSans-Medium.ttf")

    val document = Document(pdfDocument, PageSize.A4)
        .setFont(dmRegular)
        .setFontSize(12F)

    document.add(createTitle(deliveryNote.documentNumber.text, dmRegular))
    document.add(createDate(deliveryNote.documentDate))
    document.add(createIssuerAndClientTable(deliveryNote.issuer, deliveryNote.client, dmMedium))
    if (deliveryNote.orderNumber.text.isNotEmpty()) {
        document.add(createReference(deliveryNote.orderNumber.text, dmMedium))
    }
    document.add(createProductsTable(deliveryNote.documentProducts, context))
    document.add(createFooter(context, dmMedium))
    document.close()

    getFile(fileNameBeforeNumbering)?.let {
        if (it.exists() && it.isFile) {
            try {
                addPageNumbersAfterPdfIsCreated(deliveryNote.documentNumber.text)
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }
    Toast.makeText(context, "PDF created", Toast.LENGTH_SHORT).show()
}

fun createTitle(documentNumber: String, font: PdfFont): Paragraph {
    return Paragraph(Strings.get(R.string.delivery_note_number) + documentNumber)
        .setFont(font)
        .setFontSize(20F)
        .setMarginBottom(-10F)
}

fun createDate(date: String): Paragraph {
    return Paragraph(Strings.get(R.string.document_date) + " : " + date).setFontSize(14F)
}

fun createIssuerAndClientTable(
    issuer: ClientOrIssuerState,
    client: ClientOrIssuerState,
    font: PdfFont,
): Table {
    val issuerAndClientTable = Table(2)
        .useAllAvailableWidth()
        .setMarginBottom(40F)

    val issuerName = Text(issuer.firstName?.text + issuer.name.text).setFont(font)
    val issuerData = Text(
        "\n" +
                issuer.address1?.text + "\n" +
                issuer.address2?.text + "\n" +
                issuer.zipCode?.text + " " + issuer.city?.text + "\n" +
                issuer.phone?.text + "\n" +
                issuer.companyId1Label?.text + " : " + issuer.companyId1Number?.text + "\n" +
                issuer.companyId2Label?.text + " : " + issuer.companyId2Number?.text + "\n"
    )

    val issuer = Paragraph(issuerName)
        .add(issuerData)
        .setFixedLeading(16F)


    val clientName = Text(client.firstName?.text + client.name.text).setFont(font)
    val clientData = Text(
        "\n" +
                client.address1?.text + "\n" +
                client.address2?.text + "\n" +
                client.zipCode?.text + " " + client.city?.text + "\n" +
                client.phone?.text + "\n" +
                client.companyId1Label?.text + " : " + client.companyId1Number?.text + "\n" +
                client.companyId2Label?.text + " : " + client.companyId2Number?.text + "\n"
    )

    val client = Paragraph(clientName)
        .add(clientData)
        .setFixedLeading(16F)
        .setPadding(12f)
        .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 2f))
        .setBorderRadius(BorderRadius(24f))

    issuerAndClientTable.addCell(Cell().setBorder(Border.NO_BORDER))
    issuerAndClientTable.addCell(
        Cell()
            .setBorder(Border.NO_BORDER)
            .add(
                Paragraph(Strings.get(R.string.document_recipient))
                    .setFontSize(10F)
                    .setFontColor(ColorConstants.DARK_GRAY)
            )
            .setTextAlignment(TextAlignment.CENTER)
    )
    issuerAndClientTable.addCell(
        Cell()
            .setBorder(Border.NO_BORDER)
            .add(issuer)
    )
    return issuerAndClientTable.addCell(
        Cell()
            .setBorder(Border.NO_BORDER)
            .add(client)
            .setTextAlignment(TextAlignment.CENTER)
    )
}

fun createReference(orderNumber: String, font: PdfFont): Paragraph {
    val reference = Text(Strings.get(R.string.document_order_number)).setFont(font)
    return Paragraph(reference)
        .add(orderNumber)
}

fun createProductsTable(products: List<DocumentProductState>, context: Context): Table {
    val columnWidth = floatArrayOf(50f, 10f, 10f, 10f, 10f, 10f)

    val productsTable = Table(UnitValue.createPercentArray(columnWidth))
        .useAllAvailableWidth()
        .setFontSize(10F)
        .setMarginBottom(10f)

    productsTable
        .addCustomCell("Description", alignment = TextAlignment.LEFT, isBold = true)
        .addCustomCell("Qté", isBold = true)
        .addCustomCell("Unité", isBold = true)
        .addCustomCell("TVA", isBold = true)
        .addCustomCell("PU HT", isBold = true)
        .addCustomCell("Total HT", isBold = true)

    products.forEach {
        val itemName = Text(it.name.text)
        val itemDescription = Text(it.description?.text).setItalic()
        val item = Paragraph(itemName)
            .add("\n")
            .add(itemDescription)
            .setFixedLeading(14F)
        productsTable.addCustomCell(paragraph = item, alignment = TextAlignment.LEFT)
        productsTable.addCustomCell(text = it.quantity.toString())
        productsTable.addCustomCell(text = it.unit?.text)
        productsTable.addCustomCell(text = it.taxRate.toString())

        var priceWithoutTax = BigDecimal(0)
        it.priceWithTax?.let { priceWithTax ->
            priceWithoutTax =
                (priceWithTax - priceWithTax * (it.taxRate ?: BigDecimal(0)) / BigDecimal(100))

        }
        productsTable.addCustomCell(
            text = priceWithoutTax.toString() + context.getString(R.string.currency)
        )
        productsTable.addCustomCell(
            text = (priceWithoutTax * it.quantity).setScale(2, RoundingMode.HALF_UP)
                .toString() + context.getString(R.string.currency)
        )
    }
    return productsTable
}

fun createFooter(context: Context, font: PdfFont): Table {
    val footerArray = listOf(
        FooterRow(
            rowDescription = "TOTAL_WITHOUT_TAX", page = 1
        ),
        FooterRow(
            rowDescription = "TAXES_10", page = 1
        ),
        FooterRow(
            rowDescription = "TAXES_20", page = 1
        ),
        FooterRow(
            rowDescription = "TOTAL_WITH_TAX", page = 1
        ),
    )
    val documentPrices = DocumentPrices(
        totalPriceWithoutTax = BigDecimal(4),
        totalAmountsOfEachTax = mutableListOf(
            Pair(BigDecimal(20), BigDecimal(1)),
            Pair(BigDecimal(10), BigDecimal(1))
        ),
        totalPriceWithTax = BigDecimal(40)
    )

    val footerColumnsWidth = floatArrayOf(90f, 10f)
    val footerTable = Table(UnitValue.createPercentArray(footerColumnsWidth))
        .useAllAvailableWidth()
        .setTextAlignment(TextAlignment.RIGHT)

    if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITHOUT_TAX.name }) {
        footerTable.addCellInFooter(Paragraph(context.getString(R.string.document_total_without_tax)))

        val totalWithoutTax = Paragraph(documentPrices.totalPriceWithoutTax?.toString() ?: " - ")
        totalWithoutTax.add(context.getString(R.string.currency))
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
            documentPrices.totalAmountsOfEachTax?.first { it.first == BigDecimal(tax) }?.let {
                footerTable.addCellInFooter(
                    Paragraph(context.getString(R.string.document_tax) + " " + it.first.toString() + "% : ")
                )
                footerTable.addCellInFooter(Paragraph(it.second.toString() + context.getString(R.string.currency)))
            }
        }
    }
    if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITH_TAX.name }) {
        footerTable.addCellInFooter(
            Paragraph(
                context.getString(R.string.document_total_with_tax) + " "
            ).setFont(font)
        )
        val totalWithTax = Paragraph(documentPrices.totalPriceWithTax?.toString() ?: " - ")
        totalWithTax.add(context.getString(R.string.currency))
        footerTable.addCellInFooter(totalWithTax.setFont(font))
    }

    return footerTable
}

fun Table.addCustomCell(
    text: String? = null,
    paragraph: Paragraph? = null,
    alignment: TextAlignment = TextAlignment.RIGHT,
    isBold: Boolean = false,
): Table {
    val paddingLeft = if (alignment == TextAlignment.LEFT) {
        6f
    } else 0f
    val paddingRight = if (alignment == TextAlignment.RIGHT) {
        6f
    } else 0f
    val textToAdd = if (text != null) {
        Paragraph(text)
    } else paragraph

    val dmRegular = PdfFontFactory.createFont("assets/DMSans-Regular.ttf")
    val dmMedium = PdfFontFactory.createFont("assets/DMSans-Medium.ttf")
    val font = if (isBold) dmMedium else dmRegular

    return this.addCell(
        Cell().add(textToAdd)
            .setTextAlignment(alignment)
            .setPaddingLeft(paddingLeft)
            .setPaddingRight(paddingRight)
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 1f))
            .setFont(font)
    )
}

fun Table.addCellInFooter(
    paragraph: Paragraph? = null,
): Table {
    return this.addCell(
        Cell().add(paragraph)
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontSize(10F)
            .setBorder(Border.NO_BORDER)
            .setPaddingTop(-4f)
            .setPaddingBottom(0f)
    )
}

data class FooterRow(
    var rowDescription: String,
    var page: Int,
)

@Throws(Exception::class)
fun addPageNumbersAfterPdfIsCreated(documentNumber: String) {
    val dmRegular = PdfFontFactory.createFont("assets/DMSans-Regular.ttf")

    //checkIfFileNameAlreadyExist()

    val pdfDoc = PdfDocument(
        PdfReader(getFilePath(fileNameBeforeNumbering)),
        PdfWriter(getFilePath("$fileName$documentNumber.pdf"))
    )
    val doc = Document(pdfDoc)

    val numberOfPages = pdfDoc.numberOfPages

    if (numberOfPages > 1) {
        for (i in 1..numberOfPages) {
            doc.showTextAligned(
                Paragraph(String.format("%s/%s", i, numberOfPages))
                    .setFont(dmRegular)
                    .setFontSize(12F),
                570f, 34f, i, TextAlignment.RIGHT, VerticalAlignment.TOP, 0f
            )
        }
    }

    deleteFile(fileNameBeforeNumbering)
    doc.close()
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

fun deleteFile(fileName: String) {
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