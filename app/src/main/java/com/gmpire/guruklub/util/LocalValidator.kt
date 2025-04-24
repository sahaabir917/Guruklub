package com.gmpire.guruklub.util

import android.util.Patterns
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

class LocalValidator  {
    companion object {

        fun isEmailValid(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.toRegex().matches(email);
        }
        fun isPhoneValid(phone: String): Boolean {
            val phoneUtil = PhoneNumberUtil.getInstance()
            try {
                val numberProto = phoneUtil.parse(phone, "BD")
                return phoneUtil.isValidNumber(numberProto)
            } catch (e: NumberParseException) {
                System.err.println("NumberParseException was thrown: $e")
            }
            return false
        }

    }
}