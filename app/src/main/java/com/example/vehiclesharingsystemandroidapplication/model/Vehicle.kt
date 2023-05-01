package com.example.vehiclesharingsystemandroidapplication.model


import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(var vin:String,
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
              var price: RentalPrice) :Parcelable
