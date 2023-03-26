package com.example.vehiclesharingsystemandroidapplication.model

import java.util.Currency
import java.util.concurrent.TimeUnit


data class RentalPrice (
    var value: Double,
    var currency: Currency,
    var timeUnit: TimeUnit
)
