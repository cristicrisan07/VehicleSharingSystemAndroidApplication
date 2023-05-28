package com.example.vehiclesharingsystemandroidapplication.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoginActivity

class UserMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_menu)

        val session = Session(this)

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

            val resultIntent = Intent()
            resultIntent.putExtra("status",this.getString(R.string.LOGOUT_OK))
            setResult(Activity.RESULT_OK,resultIntent)
            this.finish()
        }
    }
}