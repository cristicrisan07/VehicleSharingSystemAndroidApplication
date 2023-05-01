package com.example.vehiclesharingsystemandroidapplication.view.data

import com.example.vehiclesharingsystemandroidapplication.model.Driver
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener

class RegisterRepository(val dataSource: RegisterDataSource) {
    fun register(driver: Driver, volleyListener: VolleyListener) {
        dataSource.register(driver,volleyListener)
    }
}