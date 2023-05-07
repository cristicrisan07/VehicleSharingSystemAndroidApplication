package com.example.vehiclesharingsystemandroidapplication.service

import android.content.Context
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import org.json.JSONObject
import java.lang.Exception
import java.nio.charset.Charset
import java.sql.Timestamp
import java.time.Instant

class DriverService {
    companion object {
         fun setSessionWithUsernameAndToken(session: Session, username: String, token: String){
            session.setUsername(username)
            session.setToken(token)
        }
        fun setDriverSubscriptionFromServer(session: Session,context: Context) {
            val token = session.getToken()
            val stringRequest: JsonArrayRequest = object: JsonArrayRequest(
                Method.GET,
                context.getString(R.string.getDriverSubscription)+session.getUsername(),
                null,
                { response->
                    if(response != null) {
                        session.setActiveSubscription(response.toString())
                    }
                },
                {}){
                override fun getHeaders(): MutableMap<String, String> {
                    val params=HashMap<String,String>()
                    params["Content-Type"] = "application/json"
                    params["Accept"] = "application/json"
                    params["Authorization"] = "Bearer $token"
                    return params
                }
            }
            SingletonRQ.getInstance(context).addToRequestQueue(stringRequest)
        }

        fun startDriverRentalSession(session: Session,context: Context, vehicleVin:String,volleyListener: VolleyListener){
            val token = session.getToken()
            val stringRequest: StringRequest = object: StringRequest(
                Method.POST,
                context.getString(R.string.endRentalSession),
                { response->
                    volleyListener.requestFinished(Result.Success(response))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(Exception(error)))
                }){
                override fun getBody(): ByteArray {
                    val rentalSessionDTO = JSONObject()
                    rentalSessionDTO.put("driverUsername",session.getUsername())
                    rentalSessionDTO.put("vehicleVIN",vehicleVin)
                    rentalSessionDTO.put("startTime",Timestamp.from(Instant.now()).toString())

                    return rentalSessionDTO.toString().toByteArray(Charset.forName("utf-8"))
                }
                override fun getHeaders(): MutableMap<String, String> {
                    val params=HashMap<String,String>()
                    params["Content-Type"] = "application/json"
                    params["Accept"] = "application/json"
                    params["Authorization"] = "Bearer $token"

                    return params
                }
            }
            SingletonRQ.getInstance(context).addToRequestQueue(stringRequest)
        }

        fun endDriverRentalSession(session: Session,context: Context,encryptedCardNumber: String?, vehicleVin:String,cost: Double,volleyListener: VolleyListener){
            val token = session.getToken()
            val stringRequest: StringRequest = object: StringRequest(
                Method.POST,
                context.getString(R.string.endRentalSession),
                { response->
                        volleyListener.requestFinished(Result.Success(response))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(Exception(error)))
                }){
                override fun getBody(): ByteArray {
                    val rentalSessionDTO = JSONObject()
                    rentalSessionDTO.put("driverUsername",session.getUsername())
                    rentalSessionDTO.put("vehicleVIN",vehicleVin)
                    rentalSessionDTO.put("endTime",Timestamp.from(Instant.now()).toString())
                    rentalSessionDTO.put("encryptedCardNumber",encryptedCardNumber.orEmpty())
                    rentalSessionDTO.put("cost",cost.toString())

                    return rentalSessionDTO.toString().toByteArray(Charset.forName("utf-8"))
                }
                override fun getHeaders(): MutableMap<String, String> {
                    val params=HashMap<String,String>()
                    params["Content-Type"] = "application/json"
                    params["Accept"] = "application/json"
                    params["Authorization"] = "Bearer $token"

                    return params
                }
            }
            SingletonRQ.getInstance(context).addToRequestQueue(stringRequest)
        }
    }
}