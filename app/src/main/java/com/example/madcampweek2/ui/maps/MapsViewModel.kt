package com.example.madcampweek2.ui.maps

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.madcampweek2.model.User
import com.facebook.Profile
import com.google.android.gms.maps.model.LatLng
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject

class MapsViewModel(application: Application) : AndroidViewModel(application) {
    val _users =  MutableLiveData<List<User>>()
    val _locations: LiveData<List<LatLng>> = Transformations.map(_users) {
        it.map{ user -> user.toLatLng() }
    }
    val _me = MutableLiveData<User>()
    val _myLocation: LiveData<LatLng> = Transformations.map(_me) {
        it.toLatLng()
    }
    private val RECEIVE_USERS = "location"

    private lateinit var socketService: SocketService
    private var mBound: Boolean = false

    val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as SocketService.SocketBinder
            socketService = binder.getService()
            mBound = true

            socketService.subscribe(RECEIVE_USERS, Emitter.Listener {args ->
                Log.i(SocketService.TAG, "(MapsViewModel) listened location update")
                val data: String = args[0].toString()
                val jsonArray = JSONArray(data)
                val arr = mutableListOf<User>()
                for(i in 0 until jsonArray.length()){
                    val jsonObject = jsonArray[i] as JSONObject
                    val lat = jsonObject.getDouble("lat")
                    val lng = jsonObject.getDouble("lng")
                    arr.add(User(lat, lng))
                }
                _users.postValue(arr)
            })
            socketService.sendMessage("online", Profile.getCurrentProfile()?.id ?: "idError")
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    init{
        val intent = Intent(application, SocketService::class.java)
        application.startService(intent)
        application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun getUsers(): LiveData<List<User>> = _users
    fun getMe(): LiveData<User> = _me
    fun getLatLng(): LiveData<List<LatLng>> = _locations
    fun getMyLocation(): LiveData<LatLng> = _myLocation

    fun setUsers(arr: List<User>) = _users.apply{ value = arr }
}