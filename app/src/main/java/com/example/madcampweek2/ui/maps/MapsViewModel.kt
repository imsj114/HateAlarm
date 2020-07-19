package com.example.madcampweek2.ui.maps

import android.app.ActivityManager
import android.app.Application
import android.app.Service
import android.content.*
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.madcampweek2.model.User
import com.facebook.Profile
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.Socket

class MapsViewModel(application: Application) : AndroidViewModel(application) {
    val _users =  MutableLiveData<List<User>>()
    val _locations: LiveData<List<LatLng>> = Transformations.map(_users) {
        it.map{ user -> user.toLatLng() }
    }
    val _myLocation = MutableLiveData<LatLng?>()
    private val RECEIVE_USERS = "location"
    private val BROADCAST_MY_LOCATION_CHANGED = "com.example.madcampweek2"
    var myUid = ""
    var myName = ""

    private lateinit var socketService: SocketService
    private lateinit var trackerService: TrackingService
    private var socketBound: Boolean = false
    private var trackerBound: Boolean = false
    val _application = application

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BROADCAST_MY_LOCATION_CHANGED -> {
                    // my location got updated
                    Log.i(TAG, "updated my location")
                    _myLocation.value = intent.getParcelableExtra("lastLocation")
                }
            }
        }
    }

    private val socketConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as SocketService.SocketBinder
            socketService = binder.getService()
            socketBound = true

            socketService.subscribe(RECEIVE_USERS, Emitter.Listener {args ->
                Log.i(SocketService.TAG, "(MapsViewModel) listened location update")
                val data: String = args[0].toString()
                val jsonArray = JSONArray(data)
                val arr = mutableListOf<User>()
                for(i in 0 until jsonArray.length()){
                    val jsonObject = jsonArray[i] as JSONObject
                    val lat = jsonObject.getDouble("lat")
                    val lng = jsonObject.getDouble("lng")
                    val name = jsonObject.getString("name")
                    val uid = jsonObject.getString("uid")
                    if(uid != myUid){
                        arr.add(User(lat, lng, name))
                    }
                }
                _users.postValue(arr)
            })
            val preJsonObject = JsonObject()
            preJsonObject.addProperty("uid",  myUid)
            preJsonObject.addProperty("name",  myName)
            var jsonObject: JSONObject? = null
            try {
                jsonObject = JSONObject(preJsonObject.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            socketService.sendMessage("online", jsonObject!!)
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            socketBound = false
        }
    }

    private val trackerConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as TrackingService.LocationServiceBinder
            trackerService = binder.getService()
            trackerBound = true

            LocalBroadcastManager.getInstance(application).registerReceiver(locationReceiver, IntentFilter(BROADCAST_MY_LOCATION_CHANGED))

        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            trackerBound = false
        }
    }

    init{
        myUid = Profile.getCurrentProfile().id!!
        myName = Profile.getCurrentProfile().name!!
    }

    override fun onCleared() {
        LocalBroadcastManager.getInstance(_application).unregisterReceiver(locationReceiver)

        if(socketBound) _application.unbindService(socketConnection)
        if(trackerBound){
            Log.i(TAG, "unbind from mapsviewmodel")
            _application.unbindService(trackerConnection)
        }


        super.onCleared()
    }

    fun changeSocketServiceState() {
        val prefs =  _application.getSharedPreferences("PREF", Service.MODE_PRIVATE)
        val isServiceOn = socketBound //isMyServiceRunning(SocketService::class.java)
        Log.i(TAG, "isServiceOn : $isServiceOn")
        if(isServiceOn){
            endSocket()
        }else{
            Log.i(TAG, "isServiceOn : $socketBound, $trackerBound")
            startSocket()
        }
    }

    private fun startSocket() {
        if(!socketBound){
            val socketIntent = Intent(_application, SocketService::class.java)
            ContextCompat.startForegroundService(_application, socketIntent)
            _application.bindService(socketIntent, socketConnection, Context.BIND_AUTO_CREATE)
        }
        if(!trackerBound){
            val trackerIntent = Intent(_application, TrackingService::class.java)
            _application.bindService(trackerIntent, trackerConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun endSocket() {
        if(socketBound){
            _application.unbindService(socketConnection)
            socketService.disconnect()
            socketBound = false
        }
        if(!trackerBound){
            _application.unbindService(trackerConnection)
            trackerBound = false
        }
        _users.postValue(listOf())
    }

    fun getUsers(): LiveData<List<User>> = _users
    fun getLatLng(): LiveData<List<LatLng>> = _locations
    fun getMyLocation(): LiveData<LatLng?> = _myLocation

    fun setUsers(arr: List<User>) = _users.apply{ value = arr }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager: ActivityManager = _application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.getClassName()) {
                return true
            }
        }
        return false
    }

}