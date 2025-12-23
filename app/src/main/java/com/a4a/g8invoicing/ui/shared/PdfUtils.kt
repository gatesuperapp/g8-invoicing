package com.a4a.g8invoicing.ui.shared

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory


@Throws(Exception::class)
fun setTimesFont(): PdfFont {
    return PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN)
}

fun setHelveticaFont(): PdfFont {
    return PdfFontFactory.createFont("assets/helvetica.ttf")
}

fun setHelveticaBoldFont(): PdfFont {
    return PdfFontFactory.createFont("assets/helveticabold.ttf")
}


