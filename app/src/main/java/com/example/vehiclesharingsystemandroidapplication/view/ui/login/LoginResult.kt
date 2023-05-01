package com.example.vehiclesharingsystemandroidapplication.view.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: Any? = null,
    val error: Int? = null
)