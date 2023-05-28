package com.example.vehiclesharingsystemandroidapplication.model

import org.json.JSONObject
import java.time.LocalDateTime

class RentalSession(
    var id:String,
    var vehicle: Vehicle,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime?,
    var cost: Double
    ){
    fun toJsonString(): String {
        val jsonObject = JSONObject()
        jsonObject.put("vehicle",vehicle.toJsonString())
        jsonObject.put("startTime",startTime.toString())
        jsonObject.put("endTime",endTime.toString())
        jsonObject.put("cost",cost)
        jsonObject.put("id",id)
        return jsonObject.toString()
    }
    companion object{
        fun fromJSONString(jsonString: String):RentalSession{
            val jsonObject = JSONObject(jsonString)
            val possibleEndTime = jsonObject.getString("endTime")
            val vehicle = Vehicle.fromJsonString(jsonObject.getString("vehicle"))
            return RentalSession(jsonObject.getString("id"),
                vehicle,
                LocalDateTime.parse(jsonObject.getString("startTime")),
                if(possibleEndTime == "null") null else LocalDateTime.parse(possibleEndTime),
                jsonObject.getDouble("cost"))
        }
    }
}