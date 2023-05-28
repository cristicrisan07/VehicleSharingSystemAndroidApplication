package com.example.vehiclesharingsystemandroidapplication.model


import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
class Vehicle(var vin:String,
              var registrationNumber:String,
              var manufacturer: String,
              var model: String,
              var rangeLeftInKm: String,
              var yearOfManufacture: String,
              var horsePower: String,
              var torque: String,
              var maximumAuthorisedMassInKg:String,
              var numberOfSeats: String,
              var location: LatLng,
              var price: RentalPrice) :Parcelable {

    fun toJsonString(): String {
        val jsonObject = JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lng", location.longitude)

        jsonObject.put("vin", vin)
        jsonObject.put("registrationNumber", registrationNumber)
        jsonObject.put("manufacturer", manufacturer)
        jsonObject.put("model", model)
        jsonObject.put("rangeLeftInKm", rangeLeftInKm)
        jsonObject.put("yearOfManufacture", yearOfManufacture)
        jsonObject.put("horsePower", horsePower)
        jsonObject.put("torque", torque)
        jsonObject.put("maximumAuthorisedMassInKg", maximumAuthorisedMassInKg)
        jsonObject.put("numberOfSeats", numberOfSeats)
        jsonObject.put("location", locationJSON)
        jsonObject.put("rentalPriceDTO", price.toJsonObject())
        return jsonObject.toString()
    }

    override fun equals(other: Any?): Boolean {
        val vehicle = other as? Vehicle
        return if(vehicle != null) vin == vehicle.vin else return super.equals(other)
    }

    companion object {
        fun fromJsonString(jsonString: String):Vehicle {
            val jsonObject = JSONObject(jsonString)
            val locationString = jsonObject.getString("location")
            val locationJSONObject = JSONObject(locationString)
            val location = LatLng(locationJSONObject.getDouble("lat"),locationJSONObject.getDouble("lng"))
            return Vehicle(jsonObject.getString("vin"),
                jsonObject.getString("registrationNumber"),
                jsonObject.getString("manufacturer"),
                jsonObject.getString("model"),
                jsonObject.getString("rangeLeftInKm"),
                jsonObject.getString("yearOfManufacture"),
                jsonObject.getString("horsePower"),
                jsonObject.getString("torque"),
                jsonObject.getString("maximumAuthorisedMassInKg"),
                jsonObject.getString("numberOfSeats"),
                location,
                RentalPrice.fromJSONObject(jsonObject.getJSONObject("rentalPriceDTO"))
            )
        }
    }
}
