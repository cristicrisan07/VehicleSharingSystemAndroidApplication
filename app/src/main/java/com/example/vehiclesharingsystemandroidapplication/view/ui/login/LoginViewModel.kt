package com.example.vehiclesharingsystemandroidapplication.view.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.vehiclesharingsystemandroidapplication.view.data.LoginRepository
import com.example.vehiclesharingsystemandroidapplication.view.data.Result

import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.service.Validator
import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import kotlin.reflect.typeOf

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel(), VolleyListener {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        loginRepository.login(username, password,this as VolleyListener)
    }

    fun checkUsername(username: String, password: String) {
        loginRepository.checkUsername(username, password,this as VolleyListener)
    }

    fun loginDataChanged(username: String, password: String) {
        if (!Validator.isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!Validator.isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    override fun requestFinished(result: Result<Any>) {
        if (result is Result.Success) {
            if(result.data is LoggedInUser) {
                val resultData = result.data
                _loginResult.value =
                    LoginResult(success = resultData)
            }
            else{
                if(result.data is String) {
                    val resultData = result.data
                    _loginResult.value =
                        LoginResult(success = resultData)
                }
            }
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

}