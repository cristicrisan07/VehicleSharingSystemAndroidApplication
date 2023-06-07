package com.example.vehiclesharingsystemandroidapplication.service

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Session(context: Context?){
    private var prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(context!!)

    fun setUsername(username: String?) {
        prefs!!.edit().putString("username", username).apply()
    }

    fun getUsername(): String? {
        return prefs!!.getString("username", "")
    }

    fun setToken(token: String?) {
        prefs!!.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return prefs!!.getString("token", "")
    }

    fun setActiveSubscription(jsonStringActiveSubscription: String?){
        prefs!!.edit().putString("activeSubscription",jsonStringActiveSubscription).apply()
    }

    fun getActiveSubscription():String?{
        return prefs!!.getString("activeSubscription",null)
    }

    fun setCurrentRentalSession(jsonStringRentalSession: String?){
        prefs!!.edit().putString("currentRentalSession",jsonStringRentalSession).apply()
    }

    fun getCurrentRentalSession(): String? {
        return prefs!!.getString("currentRentalSession", "")
    }

    fun setLastRentedVehicle(jsonStringVehicle: String?){
        prefs!!.edit().putString("lastRentedVehicle",jsonStringVehicle).apply()
    }

    fun getLastRentedVehicle(): String? {
        return prefs!!.getString("lastRentedVehicle", "")
    }

    fun setDocumentSubmissionStatus(status: Boolean){
        prefs!!.edit().putBoolean("submittedDocumentsStatus",status).apply()
    }

    fun getDocumentSubmissionStatus():Boolean{
        return prefs!!.getBoolean("submittedDocumentsStatus",false)
    }

    fun setDocumentsValidationStatus(status: String?){
        prefs!!.edit().putString("documentsValidationStatus",status).apply()
    }

    fun getDocumentsValidationStatus(): String? {
        return prefs!!.getString("documentsValidationStatus","PENDING_VALIDATION")
    }
}