package com.example.vehiclesharingsystemandroidapplication.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.toolbox.JsonArrayRequest
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.databinding.ActivityMapsBinding
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle
import com.example.vehiclesharingsystemandroidapplication.service.DtoConverter
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.service.SingletonRQ
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.runBlocking


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,ActivityCompat.OnRequestPermissionsResultCallback{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val TAG: String = MapsActivity::class.java.simpleName

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
        mMap.uiSettings.isZoomControlsEnabled = true

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


        val dtoConverter = DtoConverter()
        val vehicles = ArrayList<Vehicle>()
        val session = Session(this)
        val username =session.getUsername()
        val token = session.getToken()
        val userMenu: ImageButton = findViewById(R.id.userMenu)


        val stringRequest: JsonArrayRequest = object: JsonArrayRequest(
            Method.GET,
            this.getString(R.string.getAllVehicles),
            null,
            { response->
                if(response.length() == 0){
                    Toast.makeText(applicationContext, "No cars available", Toast.LENGTH_SHORT).show()
                }
                else {
                    for (i in 0 until response.length()) {
                        vehicles.add(dtoConverter.fromDTOtoVehicle(response.getJSONObject(i)))
                    }
                    for (vehicle in vehicles) {
                        mMap.addMarker(MarkerOptions().position(vehicle.location))
                    }

                }
            },
            {error->
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }){

            override fun getHeaders(): MutableMap<String, String> {
                val params=HashMap<String,String>()
                params["Content-Type"] = "application/json"
                params["Accept"] = "application/json"
                params["Authorization"] = "Bearer $token"
                return params
            }

        }
        SingletonRQ.getInstance(this@MapsActivity).addToRequestQueue(stringRequest)

        mMap.setOnMarkerClickListener { marker ->
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.position,9.0f))
            val intent = Intent(this, VehicleDetailsActivity::class.java)
            intent.putExtra("vehicle",vehicles.find { it.location == marker.position})
            startActivity(intent)
            return@setOnMarkerClickListener true
        }

        userMenu.setOnClickListener {
            val intent = Intent(this, UserMenuActivity::class.java)
            startActivity(intent)
        }

    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun moveCameraToCurrentLocation(){
        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null).addOnCompleteListener {
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
}