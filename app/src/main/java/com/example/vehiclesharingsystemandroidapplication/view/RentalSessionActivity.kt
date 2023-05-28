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
import androidx.lifecycle.lifecycleScope
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.RentalSession
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle
import com.example.vehiclesharingsystemandroidapplication.service.DriverService
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoginResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalField
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

class RentalSessionActivity : AppCompatActivity(),VolleyListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var distanceTravelledTextView:TextView
    private lateinit var price:TextView
    private lateinit var amountToPayTextView:TextView
    private lateinit var vehicle:Vehicle
    private lateinit var currentRentalSession:RentalSession
    private lateinit var session: Session
    private lateinit var resultLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private var count: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rental_session)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        session = Session(this)
        currentRentalSession = RentalSession.fromJSONString(session.getCurrentRentalSession()!!)
        vehicle = currentRentalSession.vehicle

        price = findViewById(R.id.priceTextViewRentalSession)
        distanceTravelledTextView = findViewById(R.id.distanceTravelledTextView)
        amountToPayTextView = findViewById(R.id.amountToPayTextView)

        lifecycleScope.launch {
        startRentalInformationLiveUpdates()
        }

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                if(data!!.getStringExtra("status").toString() == this.getString(R.string.PAYMENT_OK)){
                    data.getStringExtra("encryptedCardNumber")?.let { card_nb ->
                        DriverService.payForRentalSession(session,
                        this,
                        currentRentalSession.id,
                        card_nb,
                        currentRentalSession.cost,
                        this as VolleyListener)
                    }
                }
            }
        }

        val finishRentalSessionButton = findViewById<Button>(R.id.finishRentalSessionButton)
        finishRentalSessionButton.setOnClickListener {
            count=false
            val now = LocalDateTime.now()
            currentRentalSession.cost = Duration.between(currentRentalSession.startTime,now).toMinutes().toDouble() * currentRentalSession.vehicle.price.value
            currentRentalSession.endTime = now
            session.setCurrentRentalSession(currentRentalSession.toJsonString())
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnCompleteListener {
                currentRentalSession.vehicle.location =  LatLng(
                    it.result.latitude,
                    it.result.longitude
                )
                DriverService.endDriverRentalSession(
                    session,
                    this,
                    currentRentalSession,
                    this as VolleyListener,
                )
            }
        }
    }

    private suspend fun startRentalInformationLiveUpdates() {
        price.text = vehicle.price.toString()
        distanceTravelledTextView.text = "-1"

        while (count) {
            distanceTravelledTextView.text = (distanceTravelledTextView.text.toString()
                .toInt() + 1).toString()
            amountToPayTextView.text =
                (Duration.between(currentRentalSession.startTime, LocalDateTime.now())
                    .toMinutes() * currentRentalSession.vehicle.price.value).toString()
            delay(10000L) //for demonstration purposes only. set delay to 60000L in production.
        }
    }




    override fun requestFinished(result: Result<Any>) {
        if (result is Result.Success) {
            val resultData = result.data as String
            val resultJSONObject = JSONObject(resultData)
            if (resultJSONObject.getString("source") == "rentalSessionEndCall"){
                val response = resultJSONObject.getString("response")
                if (response.contains("SUCCESS")) {
                    if (session.getActiveSubscription() == "") {
                        val intent = Intent(this, PaymentActivity::class.java)
                        intent.putExtra("amount",currentRentalSession.cost)
                        resultLauncher.launch(intent)
                    } else {
                        DriverService.payForRentalSession(session,
                            this,
                            currentRentalSession.id,
                            "",
                            currentRentalSession.cost,
                            this as VolleyListener)
                        val intent = Intent(this, AfterRentalSessionCompletionActivity::class.java)
                        startActivity(intent)

                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                } else {
                    Toast.makeText(this, resultData, Toast.LENGTH_LONG).show()
                }
            }else{
                if (resultJSONObject.getString("response") == "SUCCESS") {
                    val intent = Intent(this, AfterRentalSessionCompletionActivity::class.java)
                    startActivity(intent)

                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }

        } else {
            Toast.makeText(this,result.toString(),Toast.LENGTH_LONG).show()
        }
    }
}
