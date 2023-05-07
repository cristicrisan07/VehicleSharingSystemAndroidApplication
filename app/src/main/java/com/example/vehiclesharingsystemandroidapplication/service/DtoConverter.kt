package com.example.vehiclesharingsystemandroidapplication.service

import com.example.vehiclesharingsystemandroidapplication.model.RentalPrice
import com.example.vehiclesharingsystemandroidapplication.model.RentalSubscription
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

class DtoConverter {

    private fun fromDTOtoRentalPrice(rentalPriceDTO: JSONObject):RentalPrice{
        return RentalPrice(rentalPriceDTO.getDouble("value"),
        rentalPriceDTO.getString("currency"),
        rentalPriceDTO.getString("timeUnit"))
    }

    private fun fromStringToLatLng(latLngDTO: String):LatLng{
        val jsonObject = JSONObject(latLngDTO)
        return LatLng(jsonObject.getDouble("lat"),jsonObject.getDouble("lng"))
    }

    fun fromDTOtoVehicle(vehicleDTO:JSONObject):Vehicle{
        return Vehicle(
            vehicleDTO.getString("vin"),
            vehicleDTO.getString("registrationNumber"),
            vehicleDTO.getString("manufacturer"),
            vehicleDTO.getString("model"),
            vehicleDTO.getString("rangeLeftInKm"),
            vehicleDTO.getString("yearOfManufacture"),
            vehicleDTO.getString("horsePower"),
            vehicleDTO.getString("torque"),
            vehicleDTO.getString("maximumAuthorisedMassInKg"),
            vehicleDTO.getString("numberOfSeats"),
            fromStringToLatLng(vehicleDTO.getString("location")),
            fromDTOtoRentalPrice(vehicleDTO.getJSONObject("rentalPriceDTO"))
        )
    }

    fun fromDTOtoSubscription(subscriptionDTO: JSONObject):RentalSubscription{
        return RentalSubscription(subscriptionDTO.getString("id"),
            subscriptionDTO.getString("name"),
            subscriptionDTO.getString("kilometersLimit"),
            fromDTOtoRentalPrice(subscriptionDTO.getJSONObject("rentalPriceDTO")))
    }
}