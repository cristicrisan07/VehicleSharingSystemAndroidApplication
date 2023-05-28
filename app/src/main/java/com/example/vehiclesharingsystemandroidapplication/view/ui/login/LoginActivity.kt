package com.example.vehiclesharingsystemandroidapplication.view.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.vehiclesharingsystemandroidapplication.databinding.ActivityLoginBinding

import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.Driver
import com.example.vehiclesharingsystemandroidapplication.model.RentalSession
import com.example.vehiclesharingsystemandroidapplication.service.DriverService
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.MapsActivity
import com.example.vehiclesharingsystemandroidapplication.view.RegisterActivity
import com.example.vehiclesharingsystemandroidapplication.view.VerifyIdentityActivity
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener

class LoginActivity : AppCompatActivity(),VolleyListener {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val register = binding.register
        val loading = binding.loading
        session = Session(this)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(this))[LoginViewModel::class.java]

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            login.isEnabled = loginState.isDataValid
            register!!.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                if(loginResult.success is LoggedInUser) {
                    DriverService.setSessionWithUsernameAndToken(session,username.text.toString(), loginResult.success.token)
                    DriverService.getAndSetDriverSubscriptionFromServer(session,this@LoginActivity)
                    updateUiWithUser(loginResult.success)
                }else{
                    if(loginResult.success is String){
                        showUsernameTaken(loginResult.success)
                    }
                }
            }

            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }

            register!!.setOnClickListener {
                loginViewModel.checkUsername(username.text.toString(), password.text.toString())
            }
        }

        val continueAsGuestButton = findViewById<Button>(R.id.guestButton)
        continueAsGuestButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
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
        if(session.getDocumentSubmissionStatus()){
            if(session.getDocumentsValidationStatus() != "VALID"){
            DriverService.getDocumentValidationStatusFromServer(session,this)
            }
            startActivity(Intent(this,MapsActivity::class.java))
        }else {
            DriverService.getDocumentSubmissionStatusFromServer(session,this,this as VolleyListener)
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
    private fun showUsernameTaken( str: String) {
        Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT).show()
    }

    override fun requestFinished(result: Result<Any>) {
        if (result is Result.Success) {
            val resultData = result.data as String
            if(resultData == "SUBMITTED"){
                DriverService.getDocumentValidationStatusFromServer(session,this)
                session.setDocumentSubmissionStatus(true)
                startActivity(Intent(this,MapsActivity::class.java))

            }else{
                startActivity(Intent(this,VerifyIdentityActivity::class.java))
                session.setDocumentSubmissionStatus(false)
            }

            finish()
            setResult(RESULT_OK)
        }else{
            Toast.makeText(this,result.toString(), Toast.LENGTH_LONG).show()
        }
    }
}


/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}