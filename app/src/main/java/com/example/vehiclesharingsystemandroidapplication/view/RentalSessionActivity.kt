package com.example.vehiclesharingsystemandroidapplication.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle
import com.example.vehiclesharingsystemandroidapplication.service.DriverService
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoginResult

class RentalSessionActivity : AppCompatActivity(),VolleyListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rental_session)

        val session= Session(this)
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

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                if(data!!.getStringExtra("status").toString() == this.getString(R.string.PAYMENT_OK)){
                    data.getStringExtra("encryptedCardNumber")?.let { it ->
                        DriverService.endDriverRentalSession(session,this,it,vehicle!!.vin,180.0,this as VolleyListener)
                    }
                }
            }
        }

        val finishRentalSessionButton = findViewById<Button>(R.id.finishRentalSessionButton)
        finishRentalSessionButton.setOnClickListener {
            if(session.getActiveSubscription() == null){
                val intent = Intent(this,PaymentActivity::class.java)
                resultLauncher.launch(intent)
            }
        }
    }

    override fun requestFinished(result: Result<Any>) {
        if (result is Result.Success) {
                val resultData = result.data as String
                    if(resultData == "SUCCESS"){
                        startActivity(Intent(this,AfterRentalSessionCompletionActivity::class.java))
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    else{
                        Toast.makeText(this,resultData,Toast.LENGTH_LONG).show()
                    }

        } else {
            Toast.makeText(this,result.toString(),Toast.LENGTH_LONG).show()
        }
    }
}
