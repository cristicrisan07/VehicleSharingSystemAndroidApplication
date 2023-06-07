package com.example.vehiclesharingsystemandroidapplication.service

import android.content.Context
import android.widget.Toast
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.RentalSession
import com.example.vehiclesharingsystemandroidapplication.model.Vehicle
import com.example.vehiclesharingsystemandroidapplication.view.data.Result
import com.example.vehiclesharingsystemandroidapplication.view.ui.VolleyListener
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.nio.charset.Charset
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DriverService {
    companion object {
         fun setSessionWithUsernameAndToken(session: Session, username: String, token: String){
            session.setUsername(username)
            session.setToken(token)
        }
        fun getAndSetDriverSubscriptionFromServer(session: Session,context: Context) {
            val token = session.getToken()
            val stringRequest: JsonObjectRequest = object: JsonObjectRequest(
                Method.GET,
                context.getString(R.string.getDriverSubscription)+session.getUsername(),
                null,
                { response->
                    if(response.getString("startDate") != "") {
                        session.setActiveSubscription(response.toString())
                    }else{
                        if(response.getString("id") == "SUBSCRIPTION_EXPIRED"){
                            Toast.makeText(context, "Your subscription has expired", Toast.LENGTH_SHORT).show()
                            session.setActiveSubscription(null)
                        }
                    }

                },
                {error->
                    Toast.makeText(context, "Could not fetch subscription status", Toast.LENGTH_SHORT).show()
                }){
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

        fun startDriverRentalSession(session: Session,context: Context, vehicle:Vehicle,volleyListener: VolleyListener){
            val token = session.getToken()
            val rentalSessionDTO = JSONObject()
            val startTime = LocalDateTime.now()
            rentalSessionDTO.put("driverUsername",session.getUsername())
            rentalSessionDTO.put("vehicleVIN",vehicle.vin)
            rentalSessionDTO.put("startTime", startTime.toString())
            rentalSessionDTO.put("endTime", "")
            rentalSessionDTO.put("location","")
            rentalSessionDTO.put("cost", "")

            val stringRequest: StringRequest = object: StringRequest(
                Method.POST,
                context.getString(R.string.startRentalSession),
                { response->
                    val res = response.split("SUCCESS:")[1]
                    val rentalSession = RentalSession(res,vehicle, startTime,null,0.0)
                    session.setCurrentRentalSession(rentalSession.toJsonString())
                    session.setLastRentedVehicle(vehicle.toJsonString())
                    volleyListener.requestFinished(Result.Success(response))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(Exception(error)))
                }){
                override fun getBody(): ByteArray {
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

        fun endDriverRentalSession(session: Session,context: Context, currentRentalSession: RentalSession,volleyListener: VolleyListener){
            val token = session.getToken()
            val stringRequest: StringRequest = object: StringRequest(
                Method.PUT,
                context.getString(R.string.endRentalSession),
                {response->
                    val enhancedResponse = JSONObject()
                    enhancedResponse.put("response",response)
                    enhancedResponse.put("source","rentalSessionEndCall")
                    volleyListener.requestFinished(Result.Success(enhancedResponse.toString()))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(Exception(error)))
                }){
                override fun getBody(): ByteArray {
                    val locationJSON = JSONObject()
                    locationJSON.put("lat",currentRentalSession.vehicle.location.latitude)
                    locationJSON.put("lng",currentRentalSession.vehicle.location.longitude)
                    val rentalSessionDTO = JSONObject()
                    rentalSessionDTO.put("driverUsername",session.getUsername())
                    rentalSessionDTO.put("vehicleVIN",currentRentalSession.vehicle.vin)
                    rentalSessionDTO.put("startTime", "")
                    rentalSessionDTO.put("endTime",currentRentalSession.endTime.toString())
                    rentalSessionDTO.put("location",locationJSON.toString())
                    rentalSessionDTO.put("cost",currentRentalSession.cost.toString())

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

        fun payForRentalSession(session: Session,context: Context,rentalSessionIdToPayFor: String, encryptedCardNumber: String, cost: Double,volleyListener: VolleyListener){
            val token = session.getToken()
            val stringRequest: StringRequest = object: StringRequest(
                Method.PUT,
                context.getString(R.string.payRentalSession),
                { response->
                    session.setCurrentRentalSession(null)
                    val enhancedResponse = JSONObject()
                    enhancedResponse.put("response",response)
                    enhancedResponse.put("source","payment")
                    volleyListener.requestFinished(Result.Success(enhancedResponse.toString()))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(Exception(error)))
                }){
                override fun getBody(): ByteArray {
                    val paymentDTO = JSONObject()
                    paymentDTO.put("rentalSessionId",rentalSessionIdToPayFor)
                    paymentDTO.put("encryptedCardNumber",encryptedCardNumber)
                    paymentDTO.put("cost",cost.toString())

                    return paymentDTO.toString().toByteArray(Charset.forName("utf-8"))
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

        fun getAndSetCurrentRentalSession(session: Session, context: Context, volleyListener: VolleyListener){
            val token = session.getToken()
            val stringRequest: JsonObjectRequest = object: JsonObjectRequest(
                Method.GET,
                context.getString(R.string.getCurrentRentalSession)+session.getUsername(),
                null,
                { response->
                        session.setCurrentRentalSession(response.toString())
                        volleyListener.requestFinished(Result.Success(response))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(Exception(error)))
                }) {
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

        fun sendDrivingLicense(session: Session, context: Context,photoFront: String, photoBack: String, photoID: String, volleyListener: VolleyListener){
            val token = session.getToken()
            val stringRequest: StringRequest = object: StringRequest(
                Method.POST,
                context.getString(R.string.addDrivingLicenseToDriver),
                { response->
                    volleyListener.requestFinished(Result.Success(response))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(Exception(error)))
                }){
                override fun getBody(): ByteArray {
                    val identityValidationDocumentDTO = JSONObject()
                    identityValidationDocumentDTO.put("username",session.getUsername())
                    identityValidationDocumentDTO.put("photoFront",photoFront)
                    identityValidationDocumentDTO.put("photoBack",photoBack)
                    identityValidationDocumentDTO.put("photoIDCard",photoID)
                    return identityValidationDocumentDTO.toString().toByteArray(Charset.forName("utf-8"))
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

        fun getDocumentSubmissionStatusFromServer(session: Session, context: Context, volleyListener: VolleyListener){
            val token = session.getToken()
            val stringRequest: StringRequest = object: StringRequest(
                Method.POST,
                context.getString(R.string.getDocumentSubmissionStatus),
                { response->
                    val enhancedResponse = JSONObject()
                    enhancedResponse.put("response",response)
                    enhancedResponse.put("source","document_submission_status")
                    volleyListener.requestFinished(Result.Success(enhancedResponse.toString()))
                },
                {error->
                    volleyListener.requestFinished(Result.Error(Exception(error)))
                }){
                override fun getBody(): ByteArray {
                    return session.getUsername()!!.toByteArray(Charset.forName("utf-8"))
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
        fun getDocumentValidationStatusFromServer(session: Session, context: Context,volleyListener: VolleyListener){
            val token = session.getToken()
            val stringRequest: StringRequest = object: StringRequest(
                Method.POST,
                context.getString(R.string.getDocumentValidationStatus),
                { response->
                    session.setDocumentsValidationStatus(response)
                    val enhancedResponse = JSONObject()
                    enhancedResponse.put("response",response)
                    enhancedResponse.put("source","document_validation_status")
                    volleyListener.requestFinished(Result.Success(enhancedResponse.toString()))
                },
                {error->
                    Toast.makeText(context,error.toString(), Toast.LENGTH_LONG).show()
                }){
                override fun getBody(): ByteArray {
                    return session.getUsername()!!.toByteArray(Charset.forName("utf-8"))
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