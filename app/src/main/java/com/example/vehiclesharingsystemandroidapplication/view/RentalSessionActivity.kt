package com.example.vehiclesharingsystemandroidapplication.view

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle

class RentalSessionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rental_session)

        val vehicle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.intent.extras!!.getParcelable("vehicle", Vehicle::class.java)
        } else {
            this.intent.extras!!.getParcelable("vehicle")
        }

        val price = findViewById<TextView>(R.id.priceTextViewRentalSession)
        val distanceTravelledTextView = findViewById<TextView>(R.id.distanceTravelledTextView)
        val amountToPayTextView = findViewById<TextView>(R.id.amountToPayTextView)

        if(vehicle!=null){
            price.text = vehicle.price.toString()
            distanceTravelledTextView.text = "15km"
            amountToPayTextView.text = "180 RON"
        }


    }
}