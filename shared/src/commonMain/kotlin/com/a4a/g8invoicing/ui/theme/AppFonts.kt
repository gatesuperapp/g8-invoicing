package com.a4a.g8invoicing.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.a4a.g8invoicing.shared.resources.Res
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
) {
    NOTO_SANS("noto_sans", "Noto Sans", isPremium = false),
    NOTO_SERIF("noto_serif", "Noto Serif", isPremium = false),
    CABIN("cabin", "Cabin", isPremium = true),
    SOURCE_SANS("source_sans", "Source Sans", isPremium = true),
    LATO("lato", "Lato", isPremium = true),
    INTER("inter", "Inter", isPremium = true),
    SPECTRAL("spectral", "Spectral", isPremium = true),
    ONEST("onest", "Onest", isPremium = true),
    IBM_PLEX_SANS("ibm_plex_sans", "IBM Plex Sans", isPremium = true);

    companion object {
        /** Free default. Anything unknown / null / legacy resolves here. */
        val Default = NOTO_SANS

        fun fromId(id: String?): DocumentFont =
            entries.firstOrNull { it.id == id } ?: Default
    }
}

/**
 * The currently-selected document font, provided at the preview / PDF root so
 * the `textForDocuments*` typography extensions pick it up automatically.
 * Defaults to [DocumentFont.Default] outside a document context.
 */
val LocalDocumentFont = compositionLocalOf { DocumentFont.Default }

@Composable
fun DocumentFont.regularFamily(): FontFamily = when (this) {
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
