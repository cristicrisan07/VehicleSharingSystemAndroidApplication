package com.example.vehiclesharingsystemandroidapplication.view

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import com.example.vehiclesharingsystemandroidapplication.R

class AfterRentalSessionCompletionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_rental_session_completion)

        val continueToMapButton = findViewById<Button>(R.id.continueToMapButton)
        continueToMapButton.setOnClickListener {
            finish()
            setResult(RESULT_OK)
        }
    }
}