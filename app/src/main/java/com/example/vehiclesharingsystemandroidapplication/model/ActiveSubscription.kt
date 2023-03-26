package com.example.vehiclesharingsystemandroidapplication.model

import java.sql.Timestamp

data class ActiveSubscription (
    var subscription: RentalSubscription,
    var startData: Timestamp
    )