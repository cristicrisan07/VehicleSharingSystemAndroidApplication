package com.example.vehiclesharingsystemandroidapplication.view

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.databinding.ActivityMapsBinding
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle
import com.example.vehiclesharingsystemandroidapplication.service.DtoConverter
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.service.SingletonRQ
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpMap()
        mMap.uiSettings.isZoomControlsEnabled = true
        // Add a marker in Sydney and move the camera


        val dtoConverter = DtoConverter()
        var vehicles = ArrayList<Vehicle>()
        val session = Session(this)
        val username =session.getUsername()
        val token = session.getToken()

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

    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
    }

}