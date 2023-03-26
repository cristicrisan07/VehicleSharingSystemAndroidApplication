package com.example.vehiclesharingsystemandroidapplication.model

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng

data class Vehicle(
    var vin:String,
    var manufacturer: String,
    var model: String,
    var rangeLeftInKm: Int,
    var yearOfManufacture: Int,
    var horsePower: Int,
    var torque: Int,
    var maximumAuthorisedMassInKg:Int,
    var numberOfSeats: Int,
    var location: LatLng,
    var price: RentalPrice,
    var image: Bitmap
)
