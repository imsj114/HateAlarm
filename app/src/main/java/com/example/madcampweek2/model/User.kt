package com.example.madcampweek2.model

import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

class User(var lat: Double = 0.0, var lng: Double = 0.0) {
    fun toLatLng() : LatLng{
        return LatLng(lat, lng)
    }

}