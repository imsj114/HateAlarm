package com.example.madcampweek2.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

class User(var lat: Double = 0.0, var lng: Double = 0.0, var name: String = "{no name}") : Parcelable{
    constructor(parcel: Parcel) : this() {
        parcel.run{
            lat = readDouble()
            lng = readDouble()
            name = readString() ?: "{no name}"
        }
    }

    fun toLatLng() : LatLng{
        return LatLng(lat, lng)
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.run{
            writeDouble(this@User.lat)
            writeDouble(this@User.lng)
            writeString(this@User.name)
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}