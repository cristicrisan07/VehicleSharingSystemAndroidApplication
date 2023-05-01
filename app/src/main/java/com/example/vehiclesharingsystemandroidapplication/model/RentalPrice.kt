package com.example.vehiclesharingsystemandroidapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Currency
import java.util.concurrent.TimeUnit

@Parcelize
data class RentalPrice (
    var value: Double,
    var currency: String,
    var timeUnit: String


) : Parcelable {
    override fun toString(): String {
        return "$value $currency/$timeUnit"
    }
}
