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
        prefs!!.edit().putString("username", token).apply()
    }

    fun getToken(): String? {
        return prefs!!.getString("username", "")
    }
}