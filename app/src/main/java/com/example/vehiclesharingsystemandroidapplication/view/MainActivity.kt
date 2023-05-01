package com.example.vehiclesharingsystemandroidapplication.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = Session(this)
        if(session.getToken().isNullOrBlank()){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }else{
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        setResult(Activity.RESULT_OK)
        //Complete and destroy login activity once successful
        finish()
    }
}