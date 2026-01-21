package com.a4a.g8invoicing.data.util

/**
 * Default strings used in the app.
 * These can be overridden by platform-specific implementations.
 */
object DefaultStrings {
    const val INVOICE_DEFAULT_NUMBER = "FA-001"
    const val DELIVERY_NOTE_DEFAULT_NUMBER = "BL-001"
    const val CREDIT_NOTE_DEFAULT_NUMBER = "AV-001"
    const val DOCUMENT_DEFAULT_FOOTER = "Merci pour votre confiance !"
    const val CURRENCY = "EUR"

    // UI Strings
    const val FORM_LABEL_EDIT = "Modifier"
    const val PRODUCT_PRICE_WITHOUT_TAX = "HT"
    const val PRODUCT_PRICE_WITH_TAX = "TTC"

    // Alert Dialog Strings
    const val ALERT_DIALOG_DELETE_INVOICE_1 = "Es-tu sûr·e ? \n\nSi tu as déjà envoyé la facture, il n'est pas légal de la supprimer :"
    const val ALERT_DIALOG_DELETE_INVOICE_2 = "En savoir plus\n"
    const val ALERT_DIALOG_DELETE_INVOICE_3 = "\nDans tous les cas, la suppression est irréversible."
    const val ALERT_DIALOG_DELETE_URL = "https://www.the-gate.fr/supprimer-ou-annuler-une-facture"
    const val ALERT_DIALOG_DELETE_GENERAL = "Êtes vous sûr·e ? Cette action est irréversible."
    const val ALERT_DIALOG_DELETE_CONFIRM = "Oui, supprimer"

    // Validation Strings
    const val VALIDATION_NAME_REQUIRED = "Le nom est obligatoire"
    const val VALIDATION_EMAIL_INVALID = "L'e-mail n'est pas valide"
}
