package com.example.vehiclesharingsystemandroidapplication.view.data

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.service.SingletonRQ
import com.example.vehiclesharingsystemandroidapplication.view.data.model.LoggedInUser
import com.example.vehiclesharingsystemandroidapplication.view.ui.login.VolleyListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource(context: Context) {
    var currentContext = context
    fun login(username: String, password: String,volleyListener: VolleyListener) {
         try {

            val stringRequest: StringRequest = object: StringRequest(
                Method.POST,
                currentContext.getString(R.string.loginPath),
                { response->
                    volleyListener.requestFinished(Result.Success(LoggedInUser("id","name")))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(IOException("Error logging in", error)))
                }){

//                override fun getBody(): ByteArray {
//                    val jsonObject = JSONObject()
//                    jsonObject.put("username","cccc")
//                    jsonObject.put("password","pass")
//                    jsonObject.put("phoneNumber","username")
//                    jsonObject.put("emailAddress","username")
//                    jsonObject.put("accountType","username")
//                    return jsonObject.toString().toByteArray(Charset.forName("utf-8"))
//                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params=HashMap<String,String>();
                    params["Content-Type"] = "application/json"
                    params["Accept"] = "application/json"
                    params["Authorization"] = "Bearer "+ "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjY2NjIiwiaWF0IjoxNjgwNDYyNDY5LCJleHAiOjQ4Mzk3MzE5MTE4NTR9.8I6ezXC087YsqKwj7MiaYh3EYY-bXYIKDZLhgzeuHME"
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