package com.a4a.g8invoicing.ui.shared

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.app.ActivityCompat
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.screens.getFilePath
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

fun createPdfWithIText(context: Context) {

    val writer = PdfWriter(getFilePath("BL.pdf"))
    val pdfDocument = PdfDocument(writer)

    val dmRegular = PdfFontFactory.createFont("assets/DMSans-Regular.ttf")
    val dmMedium = PdfFontFactory.createFont("assets/DMSans-Medium.ttf")

    val document = Document(pdfDocument, PageSize.A4)
        .setFont(dmRegular)
        .setFontSize(12F)

    document.add(createTitle(dmRegular))
    document.add(createDate())
    document.add(createIssuerAndClientTable(dmMedium))
    document.add(createReference(dmMedium))
    document.add(createProductsTable(context))
    document.add(createFooter(context, dmMedium))
    document.close()

    //if document exist & i()
    val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/g8/BL.pdf"
    val file = File(filePath)
    if (file.exists() && file.isFile) {
        try {
            addPageNumbersAfterPdfIsCreated(context)
        } catch (e: Exception) {
            Log.e("xxx", "Error: ${e.message}")
        }
    }
    Toast.makeText(context, "PDF created", Toast.LENGTH_SHORT).show()
}

fun createTitle(font: PdfFont): Paragraph {
    return Paragraph("Bon de livraison N° XXX")
        .setFont(font)
        .setFontSize(20F)
        .setMarginBottom(-10F)
}

fun createDate(): Paragraph {
    return Paragraph("Date : 08/08/2023").setFontSize(14F)
}

fun createReference(font: PdfFont): Paragraph {
    val reference = Text("Référence : ").setFont(font)
    val referenceNumber = "ODIHDHGBD"
    return Paragraph(reference)
        .add(referenceNumber)
}
fun createIssuerAndClientTable(font: PdfFont): Table {
    val issuerAndClientTable = Table(2)
        .useAllAvailableWidth()
        .setMarginBottom(40F)

    val issuerName = Text("Aude Zu").setFont(font)
    val issuerData = Text(
        "\n" +
                "7 rue paradise\n" +
                "32000 Toulouse \n" +
                "0789837483\n" +
                "N° SIRET : 9387447XX\n" +
                "N° TVA : 9387447XX\n"
    )

    val issuer = Paragraph(issuerName)
        .add(issuerData)
        .setFixedLeading(16F)

    val clientName = Text("Pooki Bap").setFont(font)
    val clientData = Text(
        "\n" +
                "7 rue riton\n" +
                "32000 Bamaks \n" +
                "0789837483\n" +
                "N° SIRET : 9387447XX\n" +
                "N° TVA : 9387447XX\n"
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
                Paragraph("Adressé à")
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

fun createProductsTable(context: Context): Table {
    val columnWidth = floatArrayOf(50f, 10f, 10f, 10f, 10f, 10f)

    val productsTable = Table(UnitValue.createPercentArray(columnWidth))
        .useAllAvailableWidth()
        .setFontSize(10F)
        .setMarginBottom(10f)

    productsTable
        .addCustomCell("Description", alignment = TextAlignment.LEFT, isBold =  true)
        .addCustomCell("Qté", isBold =  true)
        .addCustomCell("Unité", isBold =  true)
        .addCustomCell("TVA", isBold =  true)
        .addCustomCell("PU HT", isBold =  true)
        .addCustomCell("Total HT", isBold =  true)

    var products = listOf(
        DocumentProductState(
            id = 0,
            name = TextFieldValue("Tomates"),
            description = TextFieldValue("cerises"),
            priceWithTax = BigDecimal(13),
            taxRate = BigDecimal(20),
            quantity = BigDecimal(2),
            unit = TextFieldValue("kg"),
            productId = null
        )
    )
    for (i in 1..20) {
        products += DocumentProductState(
            id = 0,
            name = TextFieldValue("Patates"),
            description = TextFieldValue("douces"),
            priceWithTax = BigDecimal(13),
            taxRate = BigDecimal(10),
            quantity = BigDecimal(2),
            unit = TextFieldValue("kg"),
            productId = null
        )
    }

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
        footerTable.addCellInFooter(Paragraph(context.getString(R.string.delivery_note_total_without_tax)))

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
                    Paragraph(context.getString(R.string.delivery_note_tax) + " " + it.first.toString() + "% : ")
                )
                footerTable.addCellInFooter(Paragraph(it.second.toString() + context.getString(R.string.currency)))
            }
        }
    }
    if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITH_TAX.name }) {
        footerTable.addCellInFooter(
            Paragraph(
                context.getString(R.string.delivery_note_total_with_tax) + " "
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
    val font = if(isBold) dmMedium else dmRegular

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


data class DocumentProductState(
    var id: Int,
    var name: TextFieldValue = TextFieldValue(""),
    var description: TextFieldValue? = null,
    var priceWithTax: BigDecimal? = null,
    var taxRate: BigDecimal? = null,
    var quantity: BigDecimal = BigDecimal(1),
    var unit: TextFieldValue? = null,
    var productId: Int? = null,
)

data class FooterRow(
    var rowDescription: String,
    var page: Int,
)

enum class FooterRowName {
    TOTAL_WITHOUT_TAX, TAXES_20, TAXES_10, TAXES_5, TOTAL_WITH_TAX
}


data class DocumentPrices(
    var totalPriceWithoutTax: BigDecimal? = null,
    var totalAmountsOfEachTax: MutableList<Pair<BigDecimal, BigDecimal>>? = null, //ex:  [(20.0, 7.2), (10.0, 2.4)]
    var totalPriceWithTax: BigDecimal? = null,
)


@Throws(Exception::class)
fun addPageNumbersAfterPdfIsCreated(context: Context) {
    val dmRegular = PdfFontFactory.createFont("assets/DMSans-Regular.ttf")

    val pdfDoc = PdfDocument(
        PdfReader(getFilePath("BL.pdf")),
        PdfWriter(getFilePath("BLz.pdf"))
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
    doc.close()
}

