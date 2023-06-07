package com.example.vehiclesharingsystemandroidapplication.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.example.vehiclesharingsystemandroidapplication.service.DriverService
import com.example.vehiclesharingsystemandroidapplication.service.Session
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.LoginActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity(),VolleyListener {
    private lateinit var session: Session
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = Session(this)
        if(session.getToken().isNullOrBlank()){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            setResult(Activity.RESULT_OK)
            finish()

        }else{
            if(session.getDocumentSubmissionStatus()){
                if(session.getDocumentsValidationStatus() == "INVALID") {
                    val intent = Intent(this, VerifyIdentityActivity::class.java)
                    startActivity(intent)
                    setResult(Activity.RESULT_OK)
                    //Complete and destroy login activity once successful
                    finish()
                }else {
                    if (session.getDocumentsValidationStatus() == "VALID") {
                        val intent = Intent(this, MapsActivity::class.java)
                        startActivity(intent)
                        setResult(Activity.RESULT_OK)
                        //Complete and destroy login activity once successful
                        finish()
                    } else {
                        DriverService.getDocumentValidationStatusFromServer(
                            session,
                            this,
                            this as VolleyListener
                        )
                    }
                }
            }else{
                DriverService.getDocumentSubmissionStatusFromServer(session,this,this as VolleyListener)
            }
        }

    }

    override fun requestFinished(result: Result<Any>) {
        if (result is Result.Success) {
            val resultData = result.data as String
            val resultJSONObject = JSONObject(resultData)
            val response = resultJSONObject.getString("response")
            if (resultJSONObject.getString("source") == "document_submission_status") {
                if (response == "SUBMITTED") {
                    DriverService.getDocumentValidationStatusFromServer(
                        session,
                        this,
                        this as VolleyListener
                    )
                    session.setDocumentSubmissionStatus(true)

                } else {
                    startActivity(Intent(this, VerifyIdentityActivity::class.java))
                    session.setDocumentSubmissionStatus(false)
                }
            } else {
                if(response == "INVALID"){
                    startActivity(Intent(this, VerifyIdentityActivity::class.java))
                }else{
                    startActivity(Intent(this, MapsActivity::class.java))
                }
                finish()
                setResult(RESULT_OK)
            }
        }else {
            Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show()
        }
    }
}