package com.example.madcampweek2.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

class MapUser(var lat: Double = 0.0, var lng: Double = 0.0, var name: String = "{no name}") : Parcelable{
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
            writeDouble(this@MapUser.lat)
            writeDouble(this@MapUser.lng)
            writeString(this@MapUser.name)
        }
    }

    override fun describeContents(): Int = 0

    fun getDistanceFrom(latlng: LatLng): Double {
        return SphericalUtil.computeDistanceBetween(latlng, this.toLatLng())
    }

    companion object CREATOR : Parcelable.Creator<MapUser> {
        override fun createFromParcel(parcel: Parcel): MapUser {
            return MapUser(parcel)
        }

        override fun newArray(size: Int): Array<MapUser?> {
            return arrayOfNulls(size)
        }
    }
}