package com.example.vehiclesharingsystemandroidapplication.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.vehiclesharingsystemandroidapplication.R

class UserMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_menu)

        val subsSelectionImBtn = findViewById<ImageButton>(R.id.userMenuSubscriptionsImageButton)
        subsSelectionImBtn.setOnClickListener {
            val intent = Intent(this, SubscriptionSelectionActivity::class.java)
            startActivity(intent)
        }
    }
}