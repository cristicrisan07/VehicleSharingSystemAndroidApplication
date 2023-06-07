package com.example.vehiclesharingsystemandroidapplication.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.ActiveSubscription
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoginActivity
import org.json.JSONObject

class UserMenuActivity : AppCompatActivity() {
    private lateinit var session:Session
    private lateinit var subscriptionText: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_menu)

        session = Session(this)
        setLabelText()

        val subsSelectionImBtn = findViewById<ImageButton>(R.id.userMenuSubscriptionsImageButton)
        subsSelectionImBtn.setOnClickListener {
            val intent = Intent(this, SubscriptionSelectionActivity::class.java)
            startActivity(intent)
        }



        findViewById<ImageButton>(R.id.userMenuLogOutImageButton).setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            session.setLastRentedVehicle(null)
            session.setUsername(null)
            session.setToken(null)
            session.setActiveSubscription(null)
            session.setCurrentRentalSession(null)
            session.setDocumentsValidationStatus(null)
            session.setDocumentSubmissionStatus(false)

            val resultIntent = Intent()
            resultIntent.putExtra("status",this.getString(R.string.LOGOUT_OK))
            setResult(Activity.RESULT_OK,resultIntent)
            this.finish()
        }
    }

    private fun setLabelText(){
        val usernameAndSubscriptionLabel = findViewById<TextView>(R.id.userMenuUsernameTextView)
        val activeSubscription = if(session.getActiveSubscription()!=null)
            session.getActiveSubscription()?.let { JSONObject(it) }
                ?.let {
                    ActiveSubscription.fromJSONObject(
                        it
                    )
                } else null

        subscriptionText = if(activeSubscription !=null)
            "\nYour ${activeSubscription.subscription.name} " +
                    "subscription is available until" +
                    activeSubscription.endDate.toString() else ""
        usernameAndSubscriptionLabel.text = "Hello " + session.getUsername() + subscriptionText

    }

    override fun onResume() {
        setLabelText()
        super.onResume()
    }
}