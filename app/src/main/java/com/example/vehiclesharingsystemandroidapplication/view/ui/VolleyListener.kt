package com.example.vehiclesharingsystemandroidapplication.view.ui
import com.example.vehiclesharingsystemandroidapplication.view.data.Result

import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser

interface VolleyListener {
    fun requestFinished(result: Result<Any>)
}