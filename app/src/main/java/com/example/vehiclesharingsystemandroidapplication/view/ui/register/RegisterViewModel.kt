package com.example.vehiclesharingsystemandroidapplication.view.ui.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.Driver
import com.example.vehiclesharingsystemandroidapplication.service.Validator
import com.example.vehiclesharingsystemandroidapplication.view.data.RegisterRepository
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoginFormState
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoginResult
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoggedInUserView

class RegisterViewModel(private val registerRepository: RegisterRepository): ViewModel(), VolleyListener{

    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    private val _registerResult = MutableLiveData<LoginResult>()
    val registerResult: LiveData<LoginResult> = _registerResult

    fun register(driver: Driver) {
        registerRepository.register(driver, this as VolleyListener)
    }

    fun registerDataChanged(firstName: String, lastName: String, emailAddress: String, phoneNumber: String) {
        if (!Validator.isEmailValid(emailAddress)) {
            _registerForm.value = RegisterFormState(emailAddressError = R.string.invalid_username)
        } else if (!Validator.isPhoneNumberValid(phoneNumber)) {
            _registerForm.value = RegisterFormState(phoneNumberError = R.string.invalid_phoneNumber)
        } else {
            if(!Validator.isNameValid(firstName)){
                _registerForm.value = RegisterFormState(firstNameError = R.string.invalid_name)
            }else {
                if(!Validator.isNameValid(lastName)){
                    _registerForm.value = RegisterFormState(lastNameError = R.string.invalid_name)
                }else {
                    _registerForm.value = RegisterFormState(isDataValid = true)
                }
            }
        }
    }

    override fun requestFinished(result: Result<Any>) {
        if (result is Result.Success) {
            if(result.data is LoggedInUser) {
                val resultData = result.data
                _registerResult.value =
                    LoginResult(success = resultData)
            }
        } else {
            _registerResult.value = LoginResult(error = R.string.login_failed)
        }
    }

}


