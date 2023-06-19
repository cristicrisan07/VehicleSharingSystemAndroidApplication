package com.example.vehiclesharingsystemandroidapplication.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.android.volley.toolbox.JsonArrayRequest
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.databinding.ActivityMapsBinding
import com.example.vehiclesharingsystemandroidapplication.model.RentalSession
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle
import com.example.vehiclesharingsystemandroidapplication.service.DriverService
import com.example.vehiclesharingsystemandroidapplication.service.DtoConverter
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.service.SingletonRQ
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoginActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.LocalDateTime
import java.util.TreeMap
import java.util.TreeSet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,ActivityCompat.OnRequestPermissionsResultCallback,VolleyListener{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val TAG: String = MapsActivity::class.java.simpleName
    private var session: Session? = null
    private lateinit var currentRentalSession:RentalSession
    private var resultLauncher = setBehaviourForResultActivities()
    private var vehicles = HashSet<Vehicle>()
    private var markers = HashMap<String,Marker>()
    private lateinit var token : String
    private val dtoConverter = DtoConverter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
       val success= mMap.setMapStyle(
            MapStyleOptions(
                resources
            .getString(R.string.style_json))
        )
        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }

        setUpMap()
        //mMap.uiSettings.isZoomControlsEnabled = true

        // In this case, the user has already given the permissions, thus there is a need to check them.
        runBlocking {
            if (ActivityCompat.checkSelfPermission(this@MapsActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this@MapsActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        moveCameraToCurrentLocation()
                        enableCurrentLocationButton()
            }
        }

        session = Session(this)
        val username =session!!.getUsername()

        mMap.setOnMarkerClickListener { marker ->

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 11.0f))
            val intent = Intent(this, VehicleDetailsActivity::class.java)
            intent.putExtra("vehicle", vehicles.find { it.location == marker.position })
            resultLauncher.launch(intent)
            return@setOnMarkerClickListener true
        }

        val userMenu: ImageButton = findViewById(R.id.userMenu)
        val goBackToLoginFromMapsButton = findViewById<Button>(R.id.goBackToLoginFromMapsButton)

        if(username.isNullOrEmpty()){
            userMenu.visibility = INVISIBLE
            goBackToLoginFromMapsButton.visibility = VISIBLE
            goBackToLoginFromMapsButton.setOnClickListener {
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
                finish()
                setResult(RESULT_OK)
            }
        }else {
            DriverService.getAndSetDriverSubscriptionFromServer(session!!,this@MapsActivity)
            goBackToLoginFromMapsButton.visibility = INVISIBLE
            token = session!!.getToken().toString()
            userMenu.visibility = VISIBLE
            val rentalSessionActivityImageButton =
                findViewById<ImageButton>(R.id.currentRentalActivityImageButton)

            if (session!!.getCurrentRentalSession().isNullOrBlank()) {
                DriverService.getAndSetCurrentRentalSession(session!!, this, this as VolleyListener)
            } else {
                handleCurrentRentalSession()
            }

            rentalSessionActivityImageButton.setOnClickListener {
                val intent = Intent(this, RentalSessionActivity::class.java)
                startActivity(intent)
            }

            userMenu.setOnClickListener {
                val intent = Intent(this, UserMenuActivity::class.java)
                startActivity(intent)
            }

        }
        lifecycleScope.launch {
            updateVehiclesOnMapFromServer()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private suspend fun updateVehiclesOnMapFromServer(){
        while(true) {
            val stringRequest: JsonArrayRequest = object : JsonArrayRequest(
                Method.GET,
                this.getString(R.string.getAllVehicles),
                null,
                { response ->
                    if (response.length() == 0) {
                        Toast.makeText(applicationContext, "No cars available", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        if (vehicles.size == 0) {
                            for (i in 0 until response.length()) {
                                vehicles.add(dtoConverter.fromDTOtoVehicle(response.getJSONObject(i)))
                            }
                            for (vehicle in vehicles) {
                                val marker =
                                    mMap.addMarker(MarkerOptions().position(vehicle.location))
                                if (marker != null) {
                                    markers[vehicle.vin] = marker
                                }
                            }
                        } else {
                            val newVehiclesArray = HashSet<Vehicle>()
                            for (i in 0 until response.length()) {
                                val veh = dtoConverter.fromDTOtoVehicle(response.getJSONObject(i))
                                if (newVehiclesArray.add(veh)) {
                                    if (!markers.containsKey(veh.vin)) {
                                        mMap.addMarker(MarkerOptions().position(veh.location))
                                            ?.let { markers.putIfAbsent(veh.vin, it) }
                                    }
                                    else{
                                        if(markers[veh.vin]!!.position != veh.location){
                                            markers[veh.vin]!!.remove()
                                            mMap.addMarker(MarkerOptions().position(veh.location))
                                                ?.let {
                                                    //markers.putIfAbsent(veh.vin, it)
                                                    markers.put(veh.vin,it)
                                                }
                                        }
                                    }
                                }
                            }
                            vehicles = newVehiclesArray
                            markers.forEach { marker ->
                                val v = vehicles.find { it.location == marker.value.position }
                                if (v == null) {
                                    marker.value.remove()
                                }
                            }
                            markers = markers.filter { marker -> vehicles.find { it.location == marker.value.position } != null } as HashMap<String, Marker>

                        }
                    }
                },
                { error ->
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["Content-Type"] = "application/json"
                    params["Accept"] = "application/json"
                    return params
                }

            }
            SingletonRQ.getInstance(this@MapsActivity).addToRequestQueue(stringRequest)
            delay(60000L)
        }
    }

    private fun handleCurrentRentalSession(){
        session!!.getCurrentRentalSession()?.let {
            if (it.isNotEmpty()) {
                currentRentalSession = RentalSession.fromJSONString(it)
                if (currentRentalSession.endTime != null) {
                    if (session!!.getActiveSubscription() == null) {
                        val intent = Intent(this, PaymentActivity::class.java)
                        intent.putExtra("amount", currentRentalSession.cost)
                        resultLauncher.launch(intent)
                    } else {
                        DriverService.payForRentalSession(
                            session!!,
                            this,
                            currentRentalSession.id,
                            "",
                            currentRentalSession.cost,
                            this as VolleyListener
                        )
                        val intent = Intent(
                            this,
                            AfterRentalSessionCompletionActivity::class.java
                        )
                        startActivity(intent)
                    }
                } else {
                    findViewById<ImageButton>(R.id.currentRentalActivityImageButton).visibility =
                        VISIBLE
                }
            }
        }
    }

    private fun setBehaviourForResultActivities(): ActivityResultLauncher<Intent> {
       return registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val data: Intent = result.data!!
                if(data.getStringExtra("status").toString() == this.getString(R.string.PAYMENT_OK)){
                    data.getStringExtra("encryptedCardNumber")?.let { card_nb ->
                        fusedLocationClient.
                        getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
                            .addOnCompleteListener {
                                DriverService.payForRentalSession(session!!,
                                    this,
                                    currentRentalSession.id,
                                    card_nb,
                                    currentRentalSession.cost,
                                    this as VolleyListener)
                                val intent = Intent(this,
                                    AfterRentalSessionCompletionActivity::class.java)
                                startActivity(intent)
                            }
                    }
                }else{
                    if(data.getStringExtra("status").toString() == this.getString(R.string.RENTAL_STARTED_OK)){
                        findViewById<ImageButton>(R.id.currentRentalActivityImageButton).visibility = VISIBLE
                        val veh = session!!.getLastRentedVehicle()
                            ?.let { Vehicle.fromJsonString(it) }
                        markers[veh!!.vin]?.remove()
                        markers.remove(veh.vin)

                    }else{
                        if(data.getStringExtra("status").toString() == this.getString(R.string.LOGOUT_OK)){
                            finish()
                            setResult(Activity.RESULT_OK)
                        }
                    }
                }
            }
        }
    }

    private fun moveCameraToCurrentLocation(){
        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null).addOnCompleteListener {
            if (it.result == null) {
                Toast.makeText(this,"Please enable location services",Toast.LENGTH_LONG).show()
            } else {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.result.latitude,
                            it.result.longitude
                        ), 11.0f
                    )
                )
            }
        }
    }

    private fun enableCurrentLocationButton(){
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)

        }
        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
        return
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                moveCameraToCurrentLocation()
                enableCurrentLocationButton()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun requestFinished(result: Result<Any>) {
        if (result is Result.Success) {
            handleCurrentRentalSession()
        }
    }



    override fun onResume() {
        if(session != null){
            if(session!!.getCurrentRentalSession().isNullOrEmpty()) {
                findViewById<ImageButton>(R.id.currentRentalActivityImageButton).visibility =
                    INVISIBLE
                if(!session!!.getLastRentedVehicle().isNullOrEmpty()){
                    session!!.setLastRentedVehicle(null)
               }
            }
        }
        super.onResume()
    }
}