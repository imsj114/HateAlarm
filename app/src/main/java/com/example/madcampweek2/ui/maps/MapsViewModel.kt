package com.example.madcampweek2.ui.maps

import android.app.Application
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

        val socketIntent = Intent(application, SocketService::class.java)
        application.startService(socketIntent)
        application.bindService(socketIntent, socketConnection, Context.BIND_AUTO_CREATE)

        val trackerIntent = Intent(application, TrackingService::class.java)
        ContextCompat.startForegroundService(application, trackerIntent)
        application.bindService(trackerIntent, trackerConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        LocalBroadcastManager.getInstance(_application).registerReceiver(locationReceiver, IntentFilter(BROADCAST_MY_LOCATION_CHANGED))
        super.onCleared()
    }



    fun getUsers(): LiveData<List<User>> = _users
    fun getLatLng(): LiveData<List<LatLng>> = _locations
    fun getMyLocation(): LiveData<LatLng?> = _myLocation

    fun setUsers(arr: List<User>) = _users.apply{ value = arr }
}