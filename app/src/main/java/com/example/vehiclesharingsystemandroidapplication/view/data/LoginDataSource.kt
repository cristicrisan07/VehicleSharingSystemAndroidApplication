package com.example.vehiclesharingsystemandroidapplication.view.data

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.android.volley.toolbox.StringRequest
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.service.SingletonRQ
import com.example.vehiclesharingsystemandroidapplication.view.RegisterActivity
import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class LoginDataSource(context: Context) {
    var currentContext = context
    fun login(username: String, password: String,volleyListener: VolleyListener) {
         try {

            val stringRequest: StringRequest = object: StringRequest(
                Method.POST,
                currentContext.getString(R.string.loginPath),
                { response->
                    volleyListener.requestFinished(Result.Success(LoggedInUser(username,response)))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(IOException("Error logging in", error)))
                }){

                override fun getBody(): ByteArray {
                    val accountDTO = JSONObject()
                    accountDTO.put("username",username)
                    accountDTO.put("password",password)
                    accountDTO.put("accountType",currentContext.getString(R.string.account_type_driver))
                    return accountDTO.toString().toByteArray(Charset.forName("utf-8"))

                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params=HashMap<String,String>();
                    params["Content-Type"] = "application/json"
                    params["Accept"] = "application/json"
                    //params["Authorization"] = "Bearer "+ "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjY2NjIiwiaWF0IjoxNjgwNDYyNDY5LCJleHAiOjQ4Mzk3MzE5MTE4NTR9.8I6ezXC087YsqKwj7MiaYh3EYY-bXYIKDZLhgzeuHME"
                    return params
                }
            }
            SingletonRQ.getInstance(currentContext).addToRequestQueue(stringRequest)

        } catch (e: Throwable) {
             Toast.makeText(currentContext,e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    fun checkUsername(username: String,password: String,volleyListener: VolleyListener){
        try {

            val stringRequest: StringRequest = object: StringRequest(
                Method.POST,
                currentContext.getString(R.string.checkUsernamePath),
                { response->

                    if(response.equals("DOESN'T EXIST")) {
                        val intent = Intent(currentContext, RegisterActivity::class.java)
                        intent.putExtra("username", username)
                        intent.putExtra("password", password)
                        startActivity(currentContext, intent, null)
                    }else{
                        volleyListener.requestFinished(Result.Success("Username already exists"))
                    }
                },
                {error->
                    volleyListener.requestFinished(Result.Error(IOException("Error checking the username", error)))
                }){

                override fun getBody(): ByteArray {
                    return username.toByteArray(Charset.forName("utf-8"))
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

    fun logout() {
        // TODO: revoke authentication
    }
}