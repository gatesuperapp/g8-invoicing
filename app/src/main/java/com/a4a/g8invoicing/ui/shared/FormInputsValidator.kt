package com.a4a.g8invoicing.ui.shared

object FormInputsValidator {
    fun validateName(input: String?): String? {
        return if(input.isNullOrEmpty())
            "Le nom est obligatoire"
        else null
    }

    fun validateEmail(input: String?): String? {
        val trimmed = input?.trim()
        return if(!trimmed.isNullOrEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches())
            "L'e-mail n'est pas valide"
        else null
    }
}