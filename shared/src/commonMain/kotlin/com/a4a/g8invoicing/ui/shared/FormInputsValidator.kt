package com.a4a.g8invoicing.ui.shared

import com.a4a.g8invoicing.data.util.DefaultStrings

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

    fun validateName(input: String?): String? {
        return if(input.isNullOrEmpty())
            DefaultStrings.VALIDATION_NAME_REQUIRED
        else null
    }

    fun validateEmail(input: String?): String? {
        return if(!input.isNullOrEmpty() && !EMAIL_REGEX.matches(input))
            DefaultStrings.VALIDATION_EMAIL_INVALID
        else null
    }
}
