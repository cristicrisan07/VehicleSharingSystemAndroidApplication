package com.example.vehiclesharingsystemandroidapplication.view.ui.login
import com.example.vehiclesharingsystemandroidapplication.view.data.Result

import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser

interface VolleyListener {
    fun requestFinished(result: Result<LoggedInUser>)
}