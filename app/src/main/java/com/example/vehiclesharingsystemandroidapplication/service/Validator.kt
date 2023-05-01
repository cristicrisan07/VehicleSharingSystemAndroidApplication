package com.example.vehiclesharingsystemandroidapplication.service

import android.util.Patterns
import java.util.regex.Pattern

class Validator {

    companion object{
        fun isEmailValid(email: String): Boolean {
            return if (email.contains('@')) {
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
            } else {
                email.isNotBlank()
            }
        }

        fun isPhoneNumberValid(phoneNumber: String): Boolean {
            return Patterns.PHONE.matcher(phoneNumber).matches()
        }

        fun isNameValid(name: String): Boolean{
            return name.length in 2..29
        }

         fun isUserNameValid(username: String): Boolean {
            return username.length in 3 ..29
        }

         fun isPasswordValid(password: String): Boolean {
             var valid = true

             if (password.length < 8) {
                 valid = false
             }
             var exp = ".*[0-9].*"
             var pattern = Pattern.compile(exp, Pattern.CASE_INSENSITIVE)
             var matcher = pattern.matcher(password)
             if (!matcher.matches()) {
                 valid = false
             }

             exp = ".*[A-Z].*"
             pattern = Pattern.compile(exp)
             matcher = pattern.matcher(password)
             if (!matcher.matches()) {
                 valid = false
             }

             exp = ".*[a-z].*"
             pattern = Pattern.compile(exp)
             matcher = pattern.matcher(password)
             if (!matcher.matches()) {
                 valid = false
             }

             exp = ".*[~!@#\$%\\^&*()\\-_=+\\|\\[{\\]};:'\",<.>/?].*"
             pattern = Pattern.compile(exp)
             matcher = pattern.matcher(password)
             if (!matcher.matches()) {
                 valid = false
             }
             return valid
        }
    }
}