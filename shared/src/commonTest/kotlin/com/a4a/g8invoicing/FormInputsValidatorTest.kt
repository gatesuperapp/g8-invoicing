package com.a4a.g8invoicing

import com.a4a.g8invoicing.ui.shared.FormInputsValidator
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FormInputsValidatorTest {

    // Name validation tests
    @Test
    fun validateName_validName_returnsNull() {
        val result = FormInputsValidator.validateName("Jean Dupont")
        assertNull(result)
    }

    @Test
    fun validateName_emptyString_returnsError() {
        val result = FormInputsValidator.validateName("")
        assertNotNull(result)
    }

    @Test
    fun validateName_null_returnsError() {
        val result = FormInputsValidator.validateName(null)
        assertNotNull(result)
    }

    @Test
    fun validateName_singleCharacter_returnsNull() {
        val result = FormInputsValidator.validateName("A")
        assertNull(result)
    }

    @Test
    fun validateName_withNumbers_returnsNull() {
        val result = FormInputsValidator.validateName("Client 123")
        assertNull(result)
    }

    // Email validation tests
    @Test
    fun validateEmail_validEmail_returnsNull() {
        val result = FormInputsValidator.validateEmail("test@example.com")
        assertNull(result)
    }

    @Test
    fun validateEmail_validEmailWithSubdomain_returnsNull() {
        val result = FormInputsValidator.validateEmail("user@mail.example.com")
        assertNull(result)
    }

    @Test
    fun validateEmail_validEmailWithPlus_returnsNull() {
        val result = FormInputsValidator.validateEmail("user+tag@example.com")
        assertNull(result)
    }

    @Test
    fun validateEmail_validEmailWithDots_returnsNull() {
        val result = FormInputsValidator.validateEmail("first.last@example.com")
        assertNull(result)
    }

    @Test
    fun validateEmail_emptyString_returnsNull() {
        // Empty email is allowed (not required field)
        val result = FormInputsValidator.validateEmail("")
        assertNull(result)
    }

    @Test
    fun validateEmail_null_returnsNull() {
        // Null email is allowed (not required field)
        val result = FormInputsValidator.validateEmail(null)
        assertNull(result)
    }

    @Test
    fun validateEmail_noAtSign_returnsError() {
        val result = FormInputsValidator.validateEmail("testexample.com")
        assertNotNull(result)
    }

    @Test
    fun validateEmail_noDomain_returnsError() {
        val result = FormInputsValidator.validateEmail("test@")
        assertNotNull(result)
    }

    @Test
    fun validateEmail_noLocalPart_returnsError() {
        val result = FormInputsValidator.validateEmail("@example.com")
        assertNotNull(result)
    }

    @Test
    fun validateEmail_spacesInEmail_returnsError() {
        val result = FormInputsValidator.validateEmail("test @example.com")
        assertNotNull(result)
    }

    @Test
    fun validateEmail_multipleAtSigns_returnsError() {
        val result = FormInputsValidator.validateEmail("test@@example.com")
        assertNotNull(result)
    }

    @Test
    fun validateEmail_noTld_returnsError() {
        val result = FormInputsValidator.validateEmail("test@example")
        assertNotNull(result)
    }
}
