package com.example.vehiclesharingsystemandroidapplication.view

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.TextView
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle
import org.w3c.dom.Text
import kotlin.math.max

class VehicleDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_details)

        var window = this.window
        window.attributes.gravity = Gravity.BOTTOM
        window.attributes.width = MATCH_PARENT
        this.setFinishOnTouchOutside(true)

        val vehicle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.intent.extras!!.getParcelable("vehicle",Vehicle::class.java)
        } else {
            this.intent.extras!!.getParcelable("vehicle")
        }
        val firstLine = findViewById<TextView>(R.id.manufacturerModelAndYearlTextView)
        val seats = findViewById<TextView>(R.id.seatsTextView)
        val maximumMass = findViewById<TextView>(R.id.maximumMassTextView)
        val hp = findViewById<TextView>(R.id.hpTextView)
        val torque = findViewById<TextView>(R.id.torqueTextView)
        val rangeLeft = findViewById<TextView>(R.id.rangeLeftTextView)
        val price = findViewById<TextView>(R.id.priceTextView)
        val startRentalButton = findViewById<Button>(R.id.startRentalSessionButton)

        if (vehicle != null) {
            firstLine.text = getString(R.string.manufacturerModelYearText,vehicle.manufacturer,vehicle.model,vehicle.yearOfManufacture)
            seats.text = vehicle.numberOfSeats
            maximumMass.text = vehicle.maximumAuthorisedMassInKg
            hp.text = vehicle.horsePower
            torque.text = vehicle.torque
            rangeLeft.text = vehicle.rangeLeftInKm
            price.text = vehicle.price.toString()
        }

        startRentalButton.setOnClickListener {
            val intent = Intent(this, RentalSessionActivity::class.java)
            intent.putExtra("vehicle",vehicle)
            startActivity(intent)
        }




    }
}