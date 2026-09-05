package com.a4a.g8invoicing.ui.shared

/**
 * Validates form inputs.
 * NOTE: Validation messages are hardcoded here because this is not a Composable.
 * To support translation, refactor to return error types (enum/sealed class)
 * and resolve strings in the UI layer with stringResource().
 */
object FormInputsValidator {
    // RFC 5321 caps the full address at 254 characters.
    const val EMAIL_MAX_LENGTH = 254

    // Simple email regex pattern for KMP
    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )

    // TODO: Move to Compose Resources when refactoring to error types
    private const val VALIDATION_NAME_REQUIRED = "Le nom est obligatoire"
    const val VALIDATION_EMAIL_INVALID = "L'e-mail n'est pas valide"
    private const val VALIDATION_COMPANY_ID_LABEL_MISSING =
        "Le libellé de ce n° d'identification doit être rempli."

    /**
     * Consolidated version of [VALIDATION_COMPANY_ID_LABEL_MISSING] surfaced in
     * the pre-save error modal (see [FormValidationErrorDialog]): several slots
     * with a missing label collapse to a single line. Kept as a distinct
     * constant so the modal-side substitution is a table lookup with no fuzzy
     * matching on wording.
     */
    const val VALIDATION_COMPANY_ID_LABEL_MISSING_MODAL =
        "Un des libellés de numéro d'identification n'est pas rempli."

    /** Modal-side rewrites of inline messages that need aggregation. */
    val MODAL_MESSAGE_OVERRIDES: Map<String, String> = mapOf(
        VALIDATION_COMPANY_ID_LABEL_MISSING to VALIDATION_COMPANY_ID_LABEL_MISSING_MODAL,
    )

    fun validateName(input: String?): String? {
        return if(input.isNullOrEmpty())
            VALIDATION_NAME_REQUIRED
        else null
    }

    fun validateEmail(input: String?): String? {
        val trimmed = input?.trim()
        return if(!trimmed.isNullOrEmpty() && !isEmailFormatValid(trimmed))
            VALIDATION_EMAIL_INVALID
        else null
    }

    /**
     * Fires when the user typed something in a company-id VALUE slot but
     * actively cleared the matching LABEL slot. Both slots empty is fine
     * (unused slot), and a null label means the user never touched the field
     * — the default resource text ("N° SIRET" etc.) is visible in place of a
     * placeholder, so we treat it as filled and stay silent.
     */
    fun validateCompanyIdLabelForFilledValue(label: String?, value: String?): String? {
        val hasValue = !value.isNullOrBlank()
        // null → never touched → default resource label is displayed → OK
        // ""   → actively cleared → truly empty → not OK
        val labelExplicitlyEmpty = label != null && label.isBlank()
        return if (hasValue && labelExplicitlyEmpty) VALIDATION_COMPANY_ID_LABEL_MISSING else null
    }

    /**
     * Boolean variant: returns true only when the trimmed input is non-empty,
     * fits the RFC-5321 length cap, contains no control characters, and matches
     * the email regex. Use this at sites where empty input must be treated as
     * invalid (e.g. submit button enable state), as opposed to [validateEmail]
     * which treats empty as OK for optional fields.
     */
    fun isEmailValid(input: String?): Boolean {
        val trimmed = input?.trim().orEmpty()
        if (trimmed.isEmpty() || trimmed.length > EMAIL_MAX_LENGTH) return false
        // The regex below already excludes \r and \n (they aren't in any
        // character class), but this explicit check makes the intent visible:
        // a CR or LF here would smuggle SMTP headers when the address ends up
        // in a mail To: field. Belt-and-suspenders survives future regex edits.
        if (trimmed.any { it.isISOControl() }) return false
        return isEmailFormatValid(trimmed)
    }

    private fun isEmailFormatValid(trimmed: String): Boolean =
        EMAIL_REGEX.matches(trimmed)
}
