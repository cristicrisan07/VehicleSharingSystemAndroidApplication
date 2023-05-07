package com.example.vehiclesharingsystemandroidapplication.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.ApplicationAccount
import com.example.vehiclesharingsystemandroidapplication.model.Driver
import com.example.vehiclesharingsystemandroidapplication.service.DriverService
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoggedInUserView
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.afterTextChanged
import com.example.vehiclesharingsystemandroidapplication.view.ui.register.RegisterViewModel
import com.example.vehiclesharingsystemandroidapplication.view.ui.register.RegisterViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerViewModel = ViewModelProvider(this, RegisterViewModelFactory(this))[RegisterViewModel::class.java]

        val username = this.intent.extras!!.getString("username").orEmpty()
        val password = this.intent.extras!!.getString("password").orEmpty()
        val firstName = findViewById<EditText>(R.id.firstName)
        val lastName = findViewById<EditText>(R.id.last_name)
        val phoneNumber = findViewById<EditText>(R.id.phone_number_text)
        val email = findViewById<EditText>(R.id.email_text)
        val loading = findViewById<ProgressBar>(R.id.loading)
        val session = Session(this)

        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            loading.visibility = View.VISIBLE
            registerViewModel.register(Driver(ApplicationAccount(username, password,phoneNumber.text.toString(),email.text.toString()),firstName.text.toString(),lastName.text.toString(),null,null))
        }

        registerViewModel.registerFormState.observe(this@RegisterActivity, Observer {
            val registerState = it ?: return@Observer

            registerButton.isEnabled = registerState.isDataValid

            if(registerState.firstNameError != null){
                firstName.error = getString(registerState.firstNameError)
            }
            if(registerState.lastNameError != null){
                lastName.error = getString(registerState.lastNameError)
            }
            if(registerState.phoneNumberError != null){
                phoneNumber.error = getString(registerState.phoneNumberError)
            }
            if(registerState.emailAddressError != null){
                email.error = getString(registerState.emailAddressError)
            }

        })

        registerViewModel.registerResult.observe(this@RegisterActivity, Observer {
            val registerResult = it ?: return@Observer

            if(registerResult.error != null){
                showRegisterFailed(registerResult.error)
            }
            if(registerResult.success != null){
                if(registerResult.success is LoggedInUser) {
                    DriverService.setSessionWithUsernameAndToken(session,username, registerResult.success.token)
                    DriverService.setDriverSubscriptionFromServer(session,this@RegisterActivity)
                    updateUiWithUser(registerResult.success)
                }
            }
        })

        firstName.afterTextChanged {
            registerViewModel.registerDataChanged(firstName.text.toString(),lastName.text.toString(), email.text.toString(),phoneNumber.text.toString())
        }
        lastName.afterTextChanged {
            registerViewModel.registerDataChanged(firstName.text.toString(),lastName.text.toString(), email.text.toString(),phoneNumber.text.toString())
        }
        phoneNumber.afterTextChanged {
            registerViewModel.registerDataChanged(firstName.text.toString(),lastName.text.toString(), email.text.toString(),phoneNumber.text.toString())
        }
        email.afterTextChanged {
            registerViewModel.registerDataChanged(firstName.text.toString(),lastName.text.toString(), email.text.toString(),phoneNumber.text.toString())
        }

    }
    private fun showRegisterFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun updateUiWithUser(model: LoggedInUser) {
        val welcome = getString(R.string.welcome)
        val displayName = model.username
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}