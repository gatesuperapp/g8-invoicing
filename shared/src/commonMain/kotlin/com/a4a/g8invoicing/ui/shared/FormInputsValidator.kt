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
    private const val VALIDATION_EMAIL_INVALID = "L'e-mail n'est pas valide"

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
     * Boolean variant: returns true only when the trimmed input is non-empty,
     * fits the RFC-5321 length cap, and matches the email regex. Use this at
     * sites where empty input must be treated as invalid (e.g. submit button
     * enable state), as opposed to [validateEmail] which treats empty as OK
     * for optional fields.
     */
    fun isEmailValid(input: String?): Boolean {
        val trimmed = input?.trim().orEmpty()
        if (trimmed.isEmpty() || trimmed.length > EMAIL_MAX_LENGTH) return false
        return isEmailFormatValid(trimmed)
    }

    private fun isEmailFormatValid(trimmed: String): Boolean =
        EMAIL_REGEX.matches(trimmed)
}
