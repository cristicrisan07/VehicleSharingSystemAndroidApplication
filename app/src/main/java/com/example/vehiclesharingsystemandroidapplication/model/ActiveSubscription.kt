package com.example.vehiclesharingsystemandroidapplication.model

import com.example.vehiclesharingsystemandroidapplication.service.DtoConverter
import org.json.JSONObject
import java.time.LocalDateTime

data class ActiveSubscription (
    var id:String,
    var subscription: RentalSubscription,
    var startDate: LocalDateTime,
    var endDate: LocalDateTime
    ){
    companion object{
        fun fromJSONObject(jsonObject: JSONObject):ActiveSubscription{
            val dtoConverter = DtoConverter()
            return ActiveSubscription(jsonObject.getString("id"),
                dtoConverter.fromDTOtoSubscription(jsonObject.getJSONObject("subscriptionDTO")),
                LocalDateTime.parse(jsonObject.getString("startDate")),
                LocalDateTime.parse(jsonObject.getString("endDate")))
        }
    }
}
