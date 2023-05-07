package com.example.vehiclesharingsystemandroidapplication.model

import java.sql.Timestamp

data class ActiveSubscription (
    var subscription: RentalSubscription,
    var startDate: Timestamp,
    var endDate:Timestamp
    )