package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.addressed_to
import com.a4a.g8invoicing.shared.resources.company_identification1
import com.a4a.g8invoicing.shared.resources.company_identification2
import com.a4a.g8invoicing.shared.resources.company_identification3
import com.a4a.g8invoicing.shared.resources.credit_note_number
import com.a4a.g8invoicing.shared.resources.delivery_note_number
import com.a4a.g8invoicing.shared.resources.document_date_label
import com.a4a.g8invoicing.shared.resources.document_payment_section_title
import com.a4a.g8invoicing.shared.resources.document_reference_label
import com.a4a.g8invoicing.shared.resources.document_table_description
import com.a4a.g8invoicing.shared.resources.document_table_quantity
import com.a4a.g8invoicing.shared.resources.document_table_tax_rate
import com.a4a.g8invoicing.shared.resources.document_table_total_price_without_tax
import com.a4a.g8invoicing.shared.resources.document_table_unit
import com.a4a.g8invoicing.shared.resources.document_table_unit_price_without_tax
import com.a4a.g8invoicing.shared.resources.document_tax_label
import com.a4a.g8invoicing.shared.resources.document_total_with_tax
import com.a4a.g8invoicing.shared.resources.document_total_without_tax
import com.a4a.g8invoicing.shared.resources.invoice_number
import com.a4a.g8invoicing.shared.resources.invoice_paid
import com.a4a.g8invoicing.shared.resources.invoice_pdf_due_date
import com.a4a.g8invoicing.shared.resources.label_separator
import com.a4a.g8invoicing.shared.resources.pdf_currency_notice
import com.a4a.g8invoicing.shared.resources.pdf_payment_means_prefix
import com.a4a.g8invoicing.shared.resources.payment_means_10
import com.a4a.g8invoicing.shared.resources.payment_means_30
import com.a4a.g8invoicing.shared.resources.payment_means_42
import com.a4a.g8invoicing.shared.resources.payment_means_48
import com.a4a.g8invoicing.shared.resources.payment_means_58
import com.a4a.g8invoicing.shared.resources.payment_means_other
import com.a4a.g8invoicing.shared.resources.payment_means_paypal
import com.a4a.g8invoicing.shared.resources.payment_means_stripe
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

private val LABELS_SERIALIZER = MapSerializer(String.serializer(), String.serializer())

/**
 * Labels du template document (Invoice / DeliveryNote / CreditNote) qui sont
 * snapshotés à la création — le doc garde ses labels dans la langue d'origine
 * même si l'app passe ensuite dans une autre langue.
 *
 * Stockage : colonne `labels_snapshot TEXT` (JSON `Map<String, String>`) sur les 3
 * tables. NULL = pas de snapshot, on retombe sur `stringResource` (= comportement
 * historique, pour les docs créés avant cette feature).
 */
internal object DocumentLabels {

    /**
     * Liste des paires (clé JSON, ressource Compose). La clé JSON est stockée en BDD ;
     * la ressource sert à résoudre la valeur à la création et au fallback à l'affichage.
     */
    val keys: List<Pair<String, StringResource>> = listOf(
        "addressed_to" to Res.string.addressed_to,
        "company_identification1" to Res.string.company_identification1,
        "company_identification2" to Res.string.company_identification2,
        "company_identification3" to Res.string.company_identification3,
        "credit_note_number" to Res.string.credit_note_number,
        "delivery_note_number" to Res.string.delivery_note_number,
        "document_date_label" to Res.string.document_date_label,
        "document_payment_section_title" to Res.string.document_payment_section_title,
        "document_reference_label" to Res.string.document_reference_label,
        "document_table_description" to Res.string.document_table_description,
        "document_table_quantity" to Res.string.document_table_quantity,
        "document_table_tax_rate" to Res.string.document_table_tax_rate,
        "document_table_total_price_without_tax" to Res.string.document_table_total_price_without_tax,
        "document_table_unit" to Res.string.document_table_unit,
        "document_table_unit_price_without_tax" to Res.string.document_table_unit_price_without_tax,
        "document_tax_label" to Res.string.document_tax_label,
        "document_total_with_tax" to Res.string.document_total_with_tax,
        "document_total_without_tax" to Res.string.document_total_without_tax,
        "invoice_number" to Res.string.invoice_number,
        "invoice_paid" to Res.string.invoice_paid,
        "invoice_pdf_due_date" to Res.string.invoice_pdf_due_date,
        "label_separator" to Res.string.label_separator,
        "pdf_currency_notice" to Res.string.pdf_currency_notice,
        // Payment-means block (1.9). Frozen so a FR invoice keeps its FR mode
        // labels even after the user switches the app to another language.
        "pdf_payment_means_prefix" to Res.string.pdf_payment_means_prefix,
        "payment_means_30" to Res.string.payment_means_30,
        "payment_means_42" to Res.string.payment_means_42,
        "payment_means_48" to Res.string.payment_means_48,
        "payment_means_58" to Res.string.payment_means_58,
        "payment_means_10" to Res.string.payment_means_10,
        "payment_means_paypal" to Res.string.payment_means_paypal,
        "payment_means_stripe" to Res.string.payment_means_stripe,
        "payment_means_other" to Res.string.payment_means_other,
    )

    /**
     * Capture les labels dans la langue courante et retourne le JSON à stocker dans
     * `labels_snapshot`. À appeler une fois à la création d'un nouveau document.
     */
    suspend fun captureSnapshotJson(): String {
        val map: Map<String, String> = keys.associate { (key, res) -> key to getString(res) }
        return Json.encodeToString(LABELS_SERIALIZER, map)
    }

    /**
     * Parse un JSON stocké en `labels_snapshot` vers une map utilisable au render.
     * Retourne null si la string est null/vide/invalide (→ on tombera sur le fallback
     * `stringResource` au render).
     */
    fun parseSnapshot(json: String?): Map<String, String>? {
        if (json.isNullOrBlank()) return null
        return runCatching { Json.decodeFromString(LABELS_SERIALIZER, json) }.getOrNull()
    }

    /**
     * Doc-locale fallbacks for labels that were added to [keys] AFTER their
     * users' documents were created — the snapshot won't carry the key, but
     * we can still render in the doc's frozen [formatLocale] instead of the
     * app-current locale. New labels should be added here at the same time
     * they land in [keys]. Once no legacy doc predates the key any more, the
     * entry can be dropped.
     */
    private val LOCALE_FALLBACKS: Map<String, Map<String, String>> = mapOf(
        "pdf_currency_notice" to mapOf(
            "fr" to "Devise : %1\$s",
            "en" to "Currency: %1\$s",
            "de" to "Währung: %1\$s",
            "es" to "Moneda: %1\$s",
        ),
        "pdf_payment_means_prefix" to mapOf(
            "fr" to "Moyens de paiement acceptés :",
            "en" to "Accepted payment means:",
            "de" to "Akzeptierte Zahlungsmittel:",
            "es" to "Medios de pago aceptados:",
        ),
        "payment_means_30" to mapOf("fr" to "Virement", "en" to "Bank transfer", "de" to "Überweisung", "es" to "Transferencia"),
        "payment_means_42" to mapOf("fr" to "Chèque", "en" to "Cheque", "de" to "Scheck", "es" to "Cheque"),
        "payment_means_48" to mapOf("fr" to "CB", "en" to "Card", "de" to "Karte", "es" to "Tarjeta"),
        "payment_means_58" to mapOf("fr" to "SEPA", "en" to "SEPA", "de" to "SEPA", "es" to "SEPA"),
        "payment_means_10" to mapOf("fr" to "Espèces", "en" to "Cash", "de" to "Bargeld", "es" to "Efectivo"),
        "payment_means_paypal" to mapOf("fr" to "PayPal", "en" to "PayPal", "de" to "PayPal", "es" to "PayPal"),
        "payment_means_stripe" to mapOf("fr" to "Stripe", "en" to "Stripe", "de" to "Stripe", "es" to "Stripe"),
        "payment_means_other" to mapOf("fr" to "Autre", "en" to "Other", "de" to "Andere", "es" to "Otro"),
    )

    /** Look up a hardcoded locale fallback; null when the key/lang combo isn't known. */
    fun localeFallback(key: String, languageCode: String?): String? =
        LOCALE_FALLBACKS[key]?.get(languageCode)
}

/**
 * Parse une fois par recomposition de la racine du template (`snapshotJson` change
 * rarement, donc le `remember` est gratuit après la première lecture).
 */
@Composable
fun rememberDocumentLabels(snapshotJson: String?): Map<String, String>? =
    remember(snapshotJson) { DocumentLabels.parseSnapshot(snapshotJson) }

/**
 * Helper de remplacement pour `stringResource(...)` dans les templates document.
 * Retourne la valeur snapshotée si elle existe, sinon retombe sur
 * `stringResource(fallback)` (= locale courant — back-compat pour les docs antérieurs
 * à la feature).
 */
@Composable
fun documentLabel(
    snapshot: Map<String, String>?,
    key: String,
    fallback: StringResource,
): String = snapshot?.get(key) ?: stringResource(fallback)
