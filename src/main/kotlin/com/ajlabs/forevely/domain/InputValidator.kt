package com.ajlabs.forevely.domain

import co.touchlab.kermit.Logger
import com.ajlabs.forevely.domain.InputValidator.Companion.REGEX_EMAIL
import com.ajlabs.forevely.domain.InputValidator.Companion.REGEX_PASSWORD
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

interface InputValidator {
    fun isValidEmail(email: String): Boolean
    fun isValidPassword(password: String): Boolean
    fun isEmpty(input: String): Boolean
    fun isNotEmpty(input: String): Boolean
    fun isValidPhone(phone: String, countryCode: String): Boolean
    fun isValidDate(date: String): Boolean

    companion object {
        const val REGEX_EMAIL =
            "^[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+(\\.[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+)*[^.]" +
                "@(?!\\.)([A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"
        const val REGEX_PASSWORD = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{8,}$"
    }
}

class InputValidatorImpl(
    private val logger: Logger,
) : InputValidator {
    override fun isValidEmail(email: String): Boolean {
        return try {
            InternetAddress(email).validate()

            val cleanedEmail = email.replace("\\s".toRegex(), "")
            Regex(REGEX_EMAIL).matches(cleanedEmail) &&
                isNotEmpty(cleanedEmail)
        } catch (e: AddressException) {
            logger.e(e) { "Invalid email: $email" }
            false
        }
    }

    override fun isValidPassword(password: String): Boolean {
        return Regex(REGEX_PASSWORD).matches(password) &&
            isNotEmpty(password)
    }

    override fun isEmpty(input: String): Boolean {
        return input.trim().isEmpty()
    }

    override fun isNotEmpty(input: String): Boolean {
        return input.trim().isNotEmpty()
    }

    override fun isValidPhone(phone: String, countryCode: String): Boolean {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val numberProto = phoneUtil.parse(phone, countryCode)
            phoneUtil.isValidNumber(numberProto)
        } catch (e: NumberParseException) {
            logger.e(e) { "Invalid phone number: $phone" }
            false
        }
    }

    override fun isValidDate(date: String): Boolean {
        return try {
            val formatter = DateTimeFormatter
                .ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT)
            LocalDate.parse(date, formatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }
}
