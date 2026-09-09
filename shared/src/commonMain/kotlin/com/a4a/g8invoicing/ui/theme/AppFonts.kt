package com.a4a.g8invoicing.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.arimo
import com.a4a.g8invoicing.shared.resources.cabinbold
import com.a4a.g8invoicing.shared.resources.cabinregular
import com.a4a.g8invoicing.shared.resources.inter
import com.a4a.g8invoicing.shared.resources.latobold
import com.a4a.g8invoicing.shared.resources.latoregular
import com.a4a.g8invoicing.shared.resources.notosansbold
import com.a4a.g8invoicing.shared.resources.notosansregular
import com.a4a.g8invoicing.shared.resources.notoserifbold
import com.a4a.g8invoicing.shared.resources.notoserifregular
import com.a4a.g8invoicing.shared.resources.onest
import com.a4a.g8invoicing.shared.resources.plexsansbold
import com.a4a.g8invoicing.shared.resources.plexsansregular
import com.a4a.g8invoicing.shared.resources.sourcesansbold
import com.a4a.g8invoicing.shared.resources.sourcesansregular
import com.a4a.g8invoicing.shared.resources.spectralbold
import com.a4a.g8invoicing.shared.resources.spectralregular
import org.jetbrains.compose.resources.Font

/**
 * The pool of typefaces the user can pick for their documents (preview + PDF).
 *
 * - `id` is what lands in the DB — never rename an existing id or old rows
 *   will fall back to [Default].
 * - `displayName` is what the picker shows and is intentionally the font's
 *   own brand name — the picker also renders each row in the font itself so
 *   the label doubles as a preview.
 * - `isPremium = true` gates the export flow: a non-premium user selecting
 *   one is fine, but exporting is blocked with a modal.
 *
 * Ordering here mirrors the picker row order.
 */
enum class DocumentFont(
    val id: String,
    val displayName: String,
    val isPremium: Boolean,
    // iText FontProvider driver — the family string in [pdfFamilyName] MUST
    // match what OS/2 + name records in the two static TTFs actually report
    // (checked with fontTools). iText picks Regular vs Bold by matching the
    // family AND weight, so the two assets must share the family. Variable
    // originals (Arimo / Inter / Onest) were split into static instances
    // via fontTools.varLib because iText 9.x doesn't traverse the wght axis
    // — a variable file registers as a single weight (usually 400) and any
    // pdfBold() request silently keeps rendering Regular.
    val pdfFamilyName: String,
    val pdfRegularAsset: String,
    val pdfBoldAsset: String,
) {
    ARIMO(
        id = "arimo", displayName = "Arimo", isPremium = false,
        pdfFamilyName = "Arimo",
        pdfRegularAsset = "$PDF_FONT_DIR/arimoregular.ttf",
        pdfBoldAsset = "$PDF_FONT_DIR/arimobold.ttf",
    ),
    NOTO_SANS(
        id = "noto_sans", displayName = "Noto Sans", isPremium = false,
        pdfFamilyName = "Noto Sans",
        pdfRegularAsset = "$PDF_FONT_DIR/notosansregular.ttf",
        pdfBoldAsset = "$PDF_FONT_DIR/notosansbold.ttf",
    ),
    NOTO_SERIF(
        id = "noto_serif", displayName = "Noto Serif", isPremium = false,
        pdfFamilyName = "Noto Serif",
        pdfRegularAsset = "$PDF_FONT_DIR/notoserifregular.ttf",
        pdfBoldAsset = "$PDF_FONT_DIR/notoserifbold.ttf",
    ),
    CABIN(
        id = "cabin", displayName = "Cabin", isPremium = true,
        pdfFamilyName = "Cabin",
        pdfRegularAsset = "$PDF_FONT_DIR/cabinregular.ttf",
        pdfBoldAsset = "$PDF_FONT_DIR/cabinbold.ttf",
    ),
    SOURCE_SANS(
        id = "source_sans", displayName = "Source Sans", isPremium = true,
        pdfFamilyName = "Source Sans 3",
        pdfRegularAsset = "$PDF_FONT_DIR/sourcesansregular.ttf",
        pdfBoldAsset = "$PDF_FONT_DIR/sourcesansbold.ttf",
    ),
    LATO(
        id = "lato", displayName = "Lato", isPremium = true,
        pdfFamilyName = "Lato",
        pdfRegularAsset = "$PDF_FONT_DIR/latoregular.ttf",
        pdfBoldAsset = "$PDF_FONT_DIR/latobold.ttf",
    ),
    INTER(
        id = "inter", displayName = "Inter", isPremium = true,
        pdfFamilyName = "Inter",
        pdfRegularAsset = "$PDF_FONT_DIR/interregular.ttf",
        pdfBoldAsset = "$PDF_FONT_DIR/interbold.ttf",
    ),
    SPECTRAL(
        id = "spectral", displayName = "Spectral", isPremium = true,
        pdfFamilyName = "Spectral",
        pdfRegularAsset = "$PDF_FONT_DIR/spectralregular.ttf",
        pdfBoldAsset = "$PDF_FONT_DIR/spectralbold.ttf",
    ),
    ONEST(
        id = "onest", displayName = "Onest", isPremium = true,
        pdfFamilyName = "Onest",
        pdfRegularAsset = "$PDF_FONT_DIR/onestregular.ttf",
        pdfBoldAsset = "$PDF_FONT_DIR/onestbold.ttf",
    ),
    IBM_PLEX_SANS(
        id = "ibm_plex_sans", displayName = "IBM Plex Sans", isPremium = true,
        pdfFamilyName = "IBM Plex Sans",
        pdfRegularAsset = "$PDF_FONT_DIR/plexsansregular.ttf",
        pdfBoldAsset = "$PDF_FONT_DIR/plexsansbold.ttf",
    );

    companion object {
        /** Free default. Anything unknown / null / legacy resolves here. */
        val Default = ARIMO

        fun fromId(id: String?): DocumentFont =
            entries.firstOrNull { it.id == id } ?: Default
    }
}

// Compose-Multiplatform bundles composeResources/font/*.ttf into the Android
// assets tree at this exact path; ClassLoader.getResourceAsStream resolves
// the same path on JVM/Desktop.
private const val PDF_FONT_DIR =
    "composeResources/com.a4a.g8invoicing.shared.resources/font"

/**
 * The currently-selected document font, provided at the preview / PDF root so
 * the `textForDocuments*` typography extensions pick it up automatically.
 * Defaults to [DocumentFont.Default] outside a document context.
 */
val LocalDocumentFont = compositionLocalOf { DocumentFont.Default }

@Composable
fun DocumentFont.regularFamily(): FontFamily = when (this) {
    // Variable font — one file for every weight via wght axis.
    DocumentFont.ARIMO -> FontFamily(Font(Res.font.arimo, FontWeight.Normal))
    DocumentFont.NOTO_SANS -> FontFamily(Font(Res.font.notosansregular, FontWeight.Normal))
    DocumentFont.NOTO_SERIF -> FontFamily(Font(Res.font.notoserifregular, FontWeight.Normal))
    DocumentFont.CABIN -> FontFamily(Font(Res.font.cabinregular, FontWeight.Normal))
    DocumentFont.SOURCE_SANS -> FontFamily(Font(Res.font.sourcesansregular, FontWeight.Normal))
    DocumentFont.LATO -> FontFamily(Font(Res.font.latoregular, FontWeight.Normal))
    // Variable font — same file handles Regular via the wght axis default.
    DocumentFont.INTER -> FontFamily(Font(Res.font.inter, FontWeight.Normal))
    DocumentFont.SPECTRAL -> FontFamily(Font(Res.font.spectralregular, FontWeight.Normal))
    // Variable font — same file for both weights.
    DocumentFont.ONEST -> FontFamily(Font(Res.font.onest, FontWeight.Normal))
    DocumentFont.IBM_PLEX_SANS -> FontFamily(Font(Res.font.plexsansregular, FontWeight.Normal))
}

@Composable
fun DocumentFont.boldFamily(): FontFamily = when (this) {
    DocumentFont.ARIMO -> FontFamily(Font(Res.font.arimo, FontWeight.Bold))
    DocumentFont.NOTO_SANS -> FontFamily(Font(Res.font.notosansbold, FontWeight.Bold))
    DocumentFont.NOTO_SERIF -> FontFamily(Font(Res.font.notoserifbold, FontWeight.Bold))
    DocumentFont.CABIN -> FontFamily(Font(Res.font.cabinbold, FontWeight.Bold))
    DocumentFont.SOURCE_SANS -> FontFamily(Font(Res.font.sourcesansbold, FontWeight.Bold))
    DocumentFont.LATO -> FontFamily(Font(Res.font.latobold, FontWeight.Bold))
    DocumentFont.INTER -> FontFamily(Font(Res.font.inter, FontWeight.Bold))
    DocumentFont.SPECTRAL -> FontFamily(Font(Res.font.spectralbold, FontWeight.Bold))
    DocumentFont.ONEST -> FontFamily(Font(Res.font.onest, FontWeight.Bold))
    DocumentFont.IBM_PLEX_SANS -> FontFamily(Font(Res.font.plexsansbold, FontWeight.Bold))
}
