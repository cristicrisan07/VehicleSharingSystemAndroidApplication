package com.example.vehiclesharingsystemandroidapplication.model

data class RentalSubscription(
    val id: String,
    var name: String,
    var kilometerLimit: String,
    var rentalPrice: RentalPrice
)
