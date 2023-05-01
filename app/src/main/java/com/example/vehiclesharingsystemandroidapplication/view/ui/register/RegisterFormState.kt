package com.example.vehiclesharingsystemandroidapplication.view.ui.register

data class RegisterFormState(
    val firstNameError: Int? = null,
    val lastNameError: Int? = null,
    val emailAddressError: Int? = null,
    val phoneNumberError: Int? = null,
    val isDataValid: Boolean = false
)
