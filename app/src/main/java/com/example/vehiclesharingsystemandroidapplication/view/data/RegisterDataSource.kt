package com.example.vehiclesharingsystemandroidapplication.view.data

import android.content.Context
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.Driver
import com.example.vehiclesharingsystemandroidapplication.service.SingletonRQ
import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class RegisterDataSource(context: Context) {
    var currentContext = context

    fun register(driver: Driver,volleyListener: VolleyListener) {
        try {

            val stringRequest: StringRequest = object: StringRequest(
                Method.POST,
                currentContext.getString(R.string.registerPath),
                { response->
                    volleyListener.requestFinished(Result.Success(LoggedInUser(driver.account.username,response)))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(IOException("Error logging in", error)))
                }){

                override fun getBody(): ByteArray {
                    val accountDTO = JSONObject()
                    accountDTO.put("username",driver.account.username)
                    accountDTO.put("password",driver.account.password)
                    accountDTO.put("phoneNumber",driver.account.phoneNumber)
                    accountDTO.put("emailAddress",driver.account.emailAddress)
                    accountDTO.put("accountType",currentContext.getString(R.string.account_type_driver))

                    val driverDTO = JSONObject();
                    driverDTO.put("firstName",driver.firstName)
                    driverDTO.put("lastName",driver.lastName)
                    driverDTO.put("account",accountDTO)

                    return driverDTO.toString().toByteArray(Charset.forName("utf-8"))
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params=HashMap<String,String>()
                    params["Content-Type"] = "application/json"
                    params["Accept"] = "application/json"

                    return params
                }

            }

            SingletonRQ.getInstance(currentContext).addToRequestQueue(stringRequest)

        } catch (e: Throwable) {
            Toast.makeText(currentContext,e.toString(), Toast.LENGTH_LONG).show()
        }
    }

}