package com.example.madcampweek2.ui.maps

import android.app.ActivityManager
import android.app.Application
import android.app.Service
import android.content.*
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.madcampweek2.model.MapUser
import com.facebook.Profile
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MapsViewModel(application: Application) : AndroidViewModel(application) {
    val _users =  MutableLiveData<List<MapUser>>()
    val _locations: LiveData<List<LatLng>> = Transformations.map(_users) {
        it.map{ user -> user.toLatLng() }
    }
    val _myLocation = MutableLiveData<LatLng?>()
    val _isOnline = MutableLiveData<Boolean>(false)
    val _blacklist = MutableLiveData<List<String>>()
    val _users_blacklist=  MediatorLiveData<List<MapUser>>()

    private val RECEIVE_USERS = "location"
    private val BROADCAST_MY_LOCATION_CHANGED = "com.example.madcampweek2"
    var myUid = ""
    var myName = ""
    var myImageURI = ""

    private lateinit var socketService: SocketService
    private lateinit var trackerService: TrackingService
    private var socketBound: Boolean = false
    private var trackerBound: Boolean = false
    val _application = application

    private fun combineData(liveUsers: LiveData<List<MapUser>>, liveBlacklist: LiveData<List<String>>): List<MapUser> {
        val users = liveUsers.value ?: listOf()
        val blacklist = liveBlacklist.value
        if (blacklist != null) {
            for(id in blacklist){
                for(user in users){
                    if(user.uid == id) user.blocked = true
                }
            }
        }
        return users
    }

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
            //_isOnline.postValue(true)

            socketService.subscribe(Socket.EVENT_CONNECT, Emitter.Listener {
                _isOnline.postValue(true)
            })

            socketService.subscribe(Socket.EVENT_DISCONNECT, Emitter.Listener {
                _isOnline.postValue(false)
            })

            socketService.subscribe(RECEIVE_USERS, Emitter.Listener {args ->
                Log.i(SocketService.TAG, "(MapsViewModel) listened location update")
                val data: String = args[0].toString()
                val jsonArray = JSONArray(data)
                val arr = mutableListOf<MapUser>()
                for(i in 0 until jsonArray.length()){
                    val jsonObject = jsonArray[i] as JSONObject
                    val lat = jsonObject.getDouble("lat")
                    val lng = jsonObject.getDouble("lng")
                    val name = jsonObject.getString("name")
                    val uid = jsonObject.getString("uid")
                    val online = jsonObject.getBoolean("online")
                    val imageURI = jsonObject.getString("imageURI")
                    if(uid != myUid){
                        arr.add(MapUser(lat=lat, lng=lng, uid=uid, name=name, online=online, imageURI=imageURI))
                    }
                }
                _users.postValue(arr)
            })

            socketService.subscribe("blacklist", Emitter.Listener {args ->
                // Get user's blacklist from server
                if(args[0] != null){
                    Log.i(SocketService.TAG, "(MapsViewModel) listened blacklist update")
                    val data: String = args[0].toString()
                    val jsonArray = JSONArray(data)
                    val arr = mutableListOf<String>()
                    for(i in 0 until jsonArray.length()){
                        val s = jsonArray[i] as String
                        arr.add(s)
                    }
                    _blacklist.postValue(arr)
                }
            })


            val preJsonObject = JsonObject()
            preJsonObject.addProperty("uid",  myUid)
            preJsonObject.addProperty("name",  myName)
            preJsonObject.addProperty("imageURI", myImageURI)
            var jsonObject: JSONObject? = null
            try {
                jsonObject = JSONObject(preJsonObject.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            socketService.sendMessage("online", jsonObject!!)
            socketService.sendMessage("get_blacklist", "")
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            socketBound = false
            _isOnline.postValue(false)
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
        myImageURI = Profile.getCurrentProfile().getProfilePictureUri(200, 200)!!.toString()

        if(isMyServiceRunning(SocketService::class.java)){
            startSocket()
        }

        _users_blacklist.addSource(_users) {
            _users_blacklist.value = combineData(_users, _blacklist)
        }
        _users_blacklist.addSource(_blacklist) {
            _users_blacklist.value = combineData(_users, _blacklist)
        }
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
            _isOnline.postValue(false)
        }
        if(!trackerBound){
            _application.unbindService(trackerConnection)
            trackerBound = false
        }
        _users.postValue(listOf())
    }

    fun getUsers(): LiveData<List<MapUser>> = _users_blacklist
    fun getLatLng(): LiveData<List<LatLng>> = _locations
    fun getMyLocation(): LiveData<LatLng?> = _myLocation
    fun getIsOnline(): LiveData<Boolean> = _isOnline
    fun setUsers(arr: List<MapUser>) = _users.apply{ value = arr }

    fun addBlacklist(uid: String) = run {
        socketService.sendMessage("add_blacklist", uid)
        socketService.sendMessage("get_blacklist", "")
    }
    fun removeBlacklist(uid: String) = run {
        socketService.sendMessage("remove_blacklist", uid)
        socketService.sendMessage("get_blacklist", "")
    }

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