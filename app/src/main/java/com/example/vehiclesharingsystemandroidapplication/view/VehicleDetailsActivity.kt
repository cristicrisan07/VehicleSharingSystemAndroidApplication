package com.example.vehiclesharingsystemandroidapplication.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle
import com.example.vehiclesharingsystemandroidapplication.service.DriverService
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import org.w3c.dom.Text
import kotlin.math.max

class VehicleDetailsActivity : AppCompatActivity(),VolleyListener {
    private lateinit var vehicle: Vehicle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_details)

        val window = this.window
        window.attributes.gravity = Gravity.BOTTOM
        window.attributes.width = MATCH_PARENT
        this.setFinishOnTouchOutside(true)

        val session = Session(this)

        vehicle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            this.intent.extras?.getParcelable("vehicle",Vehicle::class.java)!!
                else
            this.intent.extras?.getParcelable("vehicle")!!

        val firstLine = findViewById<TextView>(R.id.manufacturerModelAndYearlTextView)
        val seats = findViewById<TextView>(R.id.seatsTextView)
        val maximumMass = findViewById<TextView>(R.id.maximumMassTextView)
        val hp = findViewById<TextView>(R.id.hpTextView)
        val torque = findViewById<TextView>(R.id.torqueTextView)
        val rangeLeft = findViewById<TextView>(R.id.rangeLeftTextView)
        val price = findViewById<TextView>(R.id.priceTextView)
        val startRentalButton = findViewById<Button>(R.id.startRentalSessionButton)

        firstLine.text = getString(R.string.manufacturerModelYearText,vehicle.manufacturer,vehicle.model,vehicle.yearOfManufacture)
        seats.text = vehicle.numberOfSeats
        maximumMass.text = vehicle.maximumAuthorisedMassInKg
        hp.text = vehicle.horsePower
        torque.text = vehicle.torque
        rangeLeft.text = vehicle.rangeLeftInKm
        price.text = vehicle.price.toString()

        if(!session.getUsername().isNullOrEmpty()){
            if(session.getDocumentsValidationStatus() == "VALID") {
                if (session.getCurrentRentalSession().isNullOrEmpty()) {
                    startRentalButton.setOnClickListener {
                        DriverService.startDriverRentalSession(
                            Session(this),
                            this,
                            vehicle,
                            this as VolleyListener
                        )
                    }
                } else{
                    startRentalButton.isEnabled = false
                    startRentalButton.text = getString(R.string.cannotRentAnotherVehicle)
                }
            }else{
                startRentalButton.isEnabled = false
                startRentalButton.text = getString(R.string.validation_in_progress)
            }
        }else{
            startRentalButton.isEnabled = false
            startRentalButton.text = getString(R.string.loginToStartRenting)
        }


    }

    override fun requestFinished(result: Result<Any>) {
        if (result is Result.Success) {
            val resultData = result.data as String
            if(resultData.contains("SUCCESS")){
                val intent = Intent(this, RentalSessionActivity::class.java)
                startActivity(intent)

                val resultIntent = Intent()
                resultIntent.putExtra("status",this.getString(R.string.RENTAL_STARTED_OK))
                setResult(Activity.RESULT_OK,resultIntent)
                this.finish()
            }
            else{
                Toast.makeText(this,resultData, Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(this,result.toString(), Toast.LENGTH_LONG).show()
        }
    }
}