package com.a4a.g8invoicing.ui.shared

/**
 * Validates form inputs.
 * NOTE: Validation messages are hardcoded here because this is not a Composable.
 * To support translation, refactor to return error types (enum/sealed class)
 * and resolve strings in the UI layer with stringResource().
 */
object FormInputsValidator {
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
        return if(!input.isNullOrEmpty() && !EMAIL_REGEX.matches(input))
            VALIDATION_EMAIL_INVALID
        else null
    }
}
