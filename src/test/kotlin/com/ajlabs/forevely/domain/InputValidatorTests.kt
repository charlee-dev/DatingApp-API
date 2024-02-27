package com.ajlabs.forevely.domain

import co.touchlab.kermit.Logger.Companion.withTag
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InputValidatorTests {
    private val validator = InputValidatorImpl(logger = withTag("InputValidatorTests"))

    @Test
    fun `test valid Emails`() {
        val validEmails = listOf(
            "email@example.com",
            "firstname.lastname@example.com",
            "email@subdomain.example.com",
            "firstname+lastname@example.com",
            "1234567890@example.com",
            "email@example-one.com",
            "email@example.name",
            "email@example.museum",
            "email@example.co.jp",
            "firstname-lastname@example.com",
            "example@s.example",
        )

        validEmails.forEach {
            println("Testing valid: $it")
            assertTrue(validator.isValidEmail(it))
        }
    }

    @Test
    fun `test invalid Emails`() {
        val invalidEmails = listOf(
            "no-tld@domain",
            "plainaddress",
            "@no-local-part.com",
            "no-at-sign.net",
            ";beginning-with-semicolon@example.com",
            "middle-semicolon@example.co;uk",
            "trailing-semicolon@example.com;",
            "much.\"more\\ unusual\"@example.com",
            "very.unusual.\"@\".unusual.com@example.com",
            "very.\"(),:;<>[]\".VERY.\"very@\\ \"very\".unusual@strange.example.com",
            "admin@mailserver1",
        )

        invalidEmails.forEach {
            println("Testing invalid: $it")
            assertFalse(validator.isValidEmail(it))
        }
    }

    @Test
    fun `test valid Passwords`() {
        val validPasswords = listOf(
            "Valid123Password",
            "Another\$Valid1",
            "Y3tAnotherValidPassword",
            "Password1234",
            "Qwerty123!",
        )

        validPasswords.forEach {
            println("Testing valid: $it")
            assertTrue(validator.isValidPassword(it))
        }
    }

    @Test
    fun `test isValid Passwords`() {
        val invalidPasswords = listOf(
            "short",
            "nouppercase123",
            "NOLOWERCASE123",
            "NoDigits",
            "lowercaseonly",
            "UPPERCASEONLY",
            "1234567890",
            "         ", // Spaces only
            "",
        )

        invalidPasswords.forEach {
            println("Testing invalid: $it")
            assertFalse(validator.isValidPassword(it))
        }
    }

    @Test
    fun `test isEmpty and isNotEmpty with various strings`() {
        assertTrue(validator.isEmpty(""))
        assertTrue(validator.isEmpty("    "))
        assertTrue(validator.isEmpty("\n\t"))

        assertFalse(validator.isNotEmpty(""))
        assertFalse(validator.isNotEmpty("    "))
        assertFalse(validator.isNotEmpty("\n\t"))

        assertTrue(validator.isNotEmpty("not empty"))
        assertTrue(validator.isNotEmpty(" a ")) // Non-empty with spaces
        assertTrue(validator.isNotEmpty("\nnotEmpty\n")) // Non-empty with new lines
    }

    @Test
    fun `test valid phones`() {
        val validPhoneNumbers = listOf(
            Pair("+16502530000", "US"),  // United States
            Pair("+442071838750", "GB"), // United Kingdom
            Pair("+919167299999", "IN"), // India
            Pair("+815031234567", "JP"), // Japan
            Pair("+61236618300", "AU"),  // Australia
        )

        validPhoneNumbers.forEach { (number, country) ->
            assertTrue(validator.isValidPhone(number, country), "Expected valid: $number in $country")
        }
    }

    @Test
    fun `test invalid phones`() {
        val invalidPhoneNumbers = listOf(
            Pair("12345", "US"),            // Too short
            Pair("abcdefghij", "GB"),       // Non-numeric
            Pair("+1 234 5670 8900", "US"), // Too long for US
            Pair("+4420718387", "GB"),      // Too short for UK
            Pair("0412345678", "IN"),       // Missing country code for India
        )

        invalidPhoneNumbers.forEach { (number, country) ->
            assertFalse(validator.isValidPhone(number, country), "Expected invalid: $number in $country")
        }
    }

    @Test
    fun `test valid dates`() {
        val validDates = listOf(
            "2023-01-01",
            "2000-12-31",
            "1999-06-15",
        )

        validDates.forEach {
            println("Testing valid: $it")
            assertTrue(validator.isValidDate(it))
        }
    }

    @Test
    fun `test invalid dates`() {
        val invalidDates = listOf(
            "01-01-2023", // Wrong format
            "2023/01/01", // Wrong separator
            "2023-13-01", // Invalid month
            "2023-02-30", // Invalid day
            "23-01-01", // Incorrect year format
            "2023-1-1", // Single digit month and day
            "20230101", // No separators
        )

        invalidDates.forEach {
            println("Testing invalid: $it")
            assertFalse(validator.isValidDate(it))
        }
    }
}
