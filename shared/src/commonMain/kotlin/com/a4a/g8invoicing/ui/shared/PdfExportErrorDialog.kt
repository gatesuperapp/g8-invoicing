package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.ColorVioletLink

/**
 * Error modal used by every PDF-family export path (classic PDF + Factur-X)
 * when generation throws. The same 𝕠𝕙 𝕟𝕠 kaomoji as the auth/backup error
 * dialogs opens the body so the user recognises "something broke on our end,
 * not on yours" at a glance.
 *
 * The raw exception message is rendered as a clickable purple link — tapping
 * it composes an email to `contact@the-gate.fr` with the error already pasted
 * into the body under a "Bonjour, j'ai eu l'erreur suivante..." intro, so
 * the user only has to hit send. Encoded via percent-encoding into a
 * `mailto:` URL and opened via [LocalUriHandler] — no platform-specific
 * plumbing needed (Android + Desktop mail clients both handle mailto's
 * subject/body query parameters per RFC 6068).
 *
 * When the export succeeds after the [FacturXTextSanitizer] silently strips
 * unsupported glyphs, this dialog never fires — it's reserved for genuine
 * failures the user should tell us about (JAXP XMP quirks, permission
 * denials, disk full, etc.).
 */
@Composable
fun PdfExportErrorDialog(
    errorText: String,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    val mailtoUrl = buildMailtoUrl(
        address = CONTACT_EMAIL,
        subject = EMAIL_SUBJECT,
        body = EMAIL_BODY_INTRO + errorText,
    )

    val displayError = errorText.ifBlank { "(no error message)" }
    val annotated = buildAnnotatedString {
        pushStringAnnotation(tag = "mailto", annotation = mailtoUrl)
        withStyle(
            SpanStyle(
                color = ColorVioletLink,
                textDecoration = TextDecoration.Underline,
            ),
        ) {
            append(displayError)
        }
        pop()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = OH_NO_HEADER,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = FRIENDLY_MESSAGE,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                ClickableText(
                    text = annotated,
                    style = MaterialTheme.typography.bodySmall,
                    onClick = { offset ->
                        annotated
                            .getStringAnnotations(tag = "mailto", start = offset, end = offset)
                            .firstOrNull()
                            ?.let { uriHandler.openUri(it.item) }
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = ColorVioletLink)
            }
        },
    )
}

// TODO(i18n): promote the FR strings below to strings.xml on the translations
// branch once release/1.9 is out. Matches the interim const pattern already
// used by OH_NO_HEADER / OFFLINE_MESSAGE_FR in Account.kt.
private const val OH_NO_HEADER = "𝕠𝕙 𝕟𝕠\n((˃ᯅ˂)ノ)"
private const val CONTACT_EMAIL = "contact@the-gate.fr"
private const val EMAIL_SUBJECT = "[g8] Erreur d'export PDF"
private const val EMAIL_BODY_INTRO =
    "Bonjour,\n\nJ'ai eu l'erreur suivante lors de l'export de mon document :\n\n"
private const val FRIENDLY_MESSAGE =
    "Le PDF n'a pas pu être généré. Réessaie ou clique sur l'erreur pour contacter le support :"

/**
 * Assemble a `mailto:` URL with the subject and body percent-encoded per
 * RFC 6068 §3. Kept in shared code so both Android and Desktop resolve
 * `LocalUriHandler.openUri(...)` to the platform mail client without any
 * intent-side plumbing. Encoding is deliberately strict — pass anything
 * that isn't `[A-Za-z0-9-_.~]` as `%HH` bytes of the UTF-8 encoding,
 * covering accents, quotes, colons, newlines, and everything else likely
 * to end up in an iText stack trace.
 */
private fun buildMailtoUrl(address: String, subject: String, body: String): String =
    "mailto:$address?subject=${percentEncode(subject)}&body=${percentEncode(body)}"

private fun percentEncode(input: String): String {
    val sb = StringBuilder(input.length)
    for (byte in input.encodeToByteArray()) {
        val b = byte.toInt() and 0xFF
        val unreserved = (b in 0x30..0x39) || // 0-9
            (b in 0x41..0x5A) ||               // A-Z
            (b in 0x61..0x7A) ||               // a-z
            b == 0x2D || b == 0x2E || b == 0x5F || b == 0x7E // - . _ ~
        if (unreserved) {
            sb.append(b.toChar())
        } else {
            sb.append('%')
            val hex = b.toString(16).uppercase()
            if (hex.length == 1) sb.append('0')
            sb.append(hex)
        }
    }
    return sb.toString()
}
