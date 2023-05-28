package com.example.vehiclesharingsystemandroidapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
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
    fun toJsonObject():JSONObject{
        val rentalPriceJSON = JSONObject()
        rentalPriceJSON.put("value", value)
        rentalPriceJSON.put("currency", currency)
        rentalPriceJSON.put("timeUnit", timeUnit)
        return rentalPriceJSON
    }
    companion object{
        fun fromJSONObject(jsonObject: JSONObject):RentalPrice{
            return RentalPrice(jsonObject.getDouble("value"),
            jsonObject.getString("currency"),
            jsonObject.getString("timeUnit"))
        }
    }
}
