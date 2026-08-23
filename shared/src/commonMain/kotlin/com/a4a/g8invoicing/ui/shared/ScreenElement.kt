package com.a4a.g8invoicing.ui.shared

enum class ScreenElement {
    DOCUMENT_HEADER,
    DOCUMENT_NUMBER,
    DOCUMENT_DATE,
    DOCUMENT_DUE_DATE,
    DOCUMENT_ISSUER,
    DOCUMENT_CLIENT,
    DOCUMENT_REFERENCE,
    DOCUMENT_FREE_FIELD,
    DOCUMENT_PRODUCT,
    DOCUMENT_CURRENCY,
    CLIENT_OR_ISSUER_NAME,
    CLIENT_OR_ISSUER_FIRST_NAME,
    CLIENT_OR_ISSUER_EMAIL_1,
    CLIENT_OR_ISSUER_EMAIL_2,
    CLIENT_OR_ISSUER_EMAIL_3,
    CLIENT_OR_ISSUER_EMAIL_4,
    CLIENT_OR_ISSUER_ADDRESS_TITLE_1,
    CLIENT_OR_ISSUER_ADDRESS_TITLE_2,
    CLIENT_OR_ISSUER_ADDRESS_TITLE_3,
    CLIENT_OR_ISSUER_ADDRESS_LINE_1_1,
    CLIENT_OR_ISSUER_ADDRESS_LINE_1_2,
    CLIENT_OR_ISSUER_ADDRESS_LINE_1_3,
    CLIENT_OR_ISSUER_ADDRESS_LINE_2_1,
    CLIENT_OR_ISSUER_ADDRESS_LINE_2_2,
    CLIENT_OR_ISSUER_ADDRESS_LINE_2_3,
    CLIENT_OR_ISSUER_ZIP_1,
    CLIENT_OR_ISSUER_ZIP_2,
    CLIENT_OR_ISSUER_ZIP_3,
    CLIENT_OR_ISSUER_CITY_1,
    CLIENT_OR_ISSUER_CITY_2,
    CLIENT_OR_ISSUER_CITY_3,
    CLIENT_OR_ISSUER_COUNTRY_1,
    CLIENT_OR_ISSUER_COUNTRY_2,
    CLIENT_OR_ISSUER_COUNTRY_3,
    CLIENT_OR_ISSUER_PHONE,
    CLIENT_OR_ISSUER_NOTES,
    ISSUER_LOGO,
    ISSUER_PAYMENT_IBAN,
    ISSUER_PAYMENT_BIC,
    // Whole bank list on the master issuer. Fires with List<IssuerBankState>
    // (edits, adds, removes go through the same event). Codes-derived fields
    // like the payment picker on documents pick from this list.
    ISSUER_BANKS,
    // Country picker anchor per bank row. Not persisted as a distinct state —
    // the picked country lives on the corresponding IssuerBankState.countryCode
    // and is written back via the ISSUER_BANKS event.
    ISSUER_BANK_COUNTRY,
    ISSUER_VAT_EXEMPT,
    ISSUER_INTRA_EU_SALES,
    // Client-side B2B/B2C chip picker. Value = ClientType? (null = "no answer",
    // fired when the user re-taps the active chip to clear the choice).
    CLIENT_TYPE,
    CLIENT_OR_ISSUER_IDENTIFICATION1,
    CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL,
    CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE,
    CLIENT_OR_ISSUER_IDENTIFICATION2,
    CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL,
    CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE,
    CLIENT_OR_ISSUER_IDENTIFICATION3,
    CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL,
    CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE,
    DOCUMENT_CLIENT_OR_ISSUER_NAME,
    DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME,
    DOCUMENT_CLIENT_OR_ISSUER_EMAIL_1,
    DOCUMENT_CLIENT_OR_ISSUER_EMAIL_2,
    DOCUMENT_CLIENT_OR_ISSUER_EMAIL_3,
    DOCUMENT_CLIENT_OR_ISSUER_EMAIL_4,
    DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_1,
    DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_2,
    DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_3,
    DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_1,
    DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_2,
    DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_3,
    DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_1,
    DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_2,
    DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_3,
    DOCUMENT_CLIENT_OR_ISSUER_ZIP_1,
    DOCUMENT_CLIENT_OR_ISSUER_ZIP_2,
    DOCUMENT_CLIENT_OR_ISSUER_ZIP_3,
    DOCUMENT_CLIENT_OR_ISSUER_CITY_1,
    DOCUMENT_CLIENT_OR_ISSUER_CITY_2,
    DOCUMENT_CLIENT_OR_ISSUER_CITY_3,
    DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_1,
    DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_2,
    DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_3,
    DOCUMENT_CLIENT_OR_ISSUER_PHONE,
    DOCUMENT_CLIENT_OR_ISSUER_NOTES,
    DOCUMENT_ISSUER_LOGO,
    DOCUMENT_ISSUER_PAYMENT_IBAN,
    DOCUMENT_ISSUER_PAYMENT_BIC,
    // Fired by the payment-means picker when the user swaps the doc's frozen
    // bank via the IBAN dropdown. Value = IssuerBankState. Handler updates
    // documentIssuer.paymentIban/paymentBic + persists via a dedicated write
    // (autoSave only touches the invoice row, not DocumentClientOrIssuer).
    DOCUMENT_ISSUER_BANK_PICKED,
    DOCUMENT_ISSUER_VAT_EXEMPT,
    DOCUMENT_ISSUER_INTRA_EU_SALES,
    DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1,
    DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL,
    DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE,
    DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2,
    DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL,
    DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE,
    DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3,
    DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL,
    DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE,
    PRODUCT_NAME,
    PRODUCT_DESCRIPTION,
    PRODUCT_DEFAULT_PRICE_WITH_TAX,
    PRODUCT_DEFAULT_PRICE_WITHOUT_TAX,
    PRODUCT_OTHER_PRICE_WITH_TAX,
    PRODUCT_OTHER_PRICE_WITHOUT_TAX,
    PRODUCT_OTHER_PRICE_CLIENTS,
    PRODUCT_TAX_RATE,
    PRODUCT_UNIT,
    PRODUCT_UNIT_CODE,
    PRODUCT_TYPE,
    DOCUMENT_PRODUCT_NAME,
    DOCUMENT_PRODUCT_DESCRIPTION,
    DOCUMENT_PRODUCT_PRICE_WITH_TAX,
    DOCUMENT_PRODUCT_PRICE_WITHOUT_TAX,
    DOCUMENT_PRODUCT_TAX_RATE,
    DOCUMENT_PRODUCT_UNIT,
    DOCUMENT_PRODUCT_UNIT_CODE,
    DOCUMENT_PRODUCT_TYPE,
    DOCUMENT_PRODUCT_QUANTITY,
    DOCUMENT_PRODUCT_DISCOUNT,
    DOCUMENT_FOOTER,
    // Row identifier for the payment-means picker (used as navigation trigger
    // in the doc-edit form list). Not fired as a value-change event any more —
    // the label refactor unified codes + free text under DOCUMENT_PAYMENT_MEANS_LABEL.
    DOCUMENT_PAYMENT_MEANS,
    // Structured payment-means label: fires with List<PaymentLabelSegment>.
    // Chip toggles append/remove Token segments; the "Modifier le texte" modal
    // edits the surrounding Free segments. Codes (BT-81) are derived from
    // the tokens present so both fields stay in sync via a single event.
    DOCUMENT_PAYMENT_MEANS_LABEL,
    // Eye toggle in the payment-means picker. true = the whole block is hidden
    // on preview + PDF (codes stay persisted regardless).
    DOCUMENT_PAYMENT_MEANS_HIDDEN,
    // "Autre" chip toggle. Fires with Boolean. OTHER is UI-only — never renders
    // on the invoice, never contributes a Token to segments, never exports to
    // Factur-X. Stored in a dedicated flag on the doc state.
    DOCUMENT_PAYMENT_MEANS_OTHER,
    // "Afficher les coordonnées bancaires" switch inside the payment-means
    // picker. true = IBAN/BIC line skipped on preview + PDF (frozen bank on
    // DocumentClientOrIssuer stays populated regardless).
    DOCUMENT_PAYMENT_BANK_HIDDEN,
    // Structured bank-details label. Fires with List<PaymentBankSegment>.
    DOCUMENT_PAYMENT_BANK_LABEL,
    // BT-20 — free-text payment terms description (LME mentions + "30j net"…).
    // Per-invoice ; not on BL / avoir / devis.
    DOCUMENT_PAYMENT_TERMS,
    SETTINGS_ACCOUNT,
    ELSE
}
