package com.example.vehiclesharingsystemandroidapplication.view.ui.register

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vehiclesharingsystemandroidapplication.view.data.LoginDataSource
import com.example.vehiclesharingsystemandroidapplication.view.data.LoginRepository
import com.example.vehiclesharingsystemandroidapplication.view.data.RegisterDataSource
import com.example.vehiclesharingsystemandroidapplication.view.data.RegisterRepository
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoginViewModel

class RegisterViewModelFactory(context: Context) : ViewModelProvider.Factory {

    var currentContext = context;
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(
                registerRepository = RegisterRepository(
                    dataSource = RegisterDataSource(currentContext)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}