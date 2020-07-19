package com.example.madcampweek2.ui.maps

import android.app.Service
import android.content.*
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.Profile
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject

class SocketService : Service(){
    companion object {
        val instance : SocketService = SocketService()
        const val TAG = "TAG_SocketService"
        const val BROADCAST_MY_LOCATION_CHANGED = "com.example.madcampweek2"
    }
    val mSocket: Socket = IO.socket("http://192.249.19.243:880")
    val binder = SocketBinder()
    lateinit var trackerService: TrackingService
    var trackerBound = false
    var uid = ""

    inner class SocketBinder : Binder() {
        fun getService(): SocketService = this@SocketService
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BROADCAST_MY_LOCATION_CHANGED -> {
                    // my location got updated
                    Log.i(TAG, "sent my location")
                    val latlng: LatLng? = intent.getParcelableExtra("lastLocation")
                    val preJsonObject = JsonObject()
                    preJsonObject.addProperty("lat",  "${latlng?.latitude ?: 0.0}")
                    preJsonObject.addProperty("lng",  "${latlng?.longitude ?: 0.0}")
                    preJsonObject.addProperty("uid",  uid)
                    var jsonObject: JSONObject? = null
                    try {
                        jsonObject = JSONObject(preJsonObject.toString())
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    Log.i(TAG, jsonObject.toString())
                    sendMessage("user_location", jsonObject!!)
                }
            }
        }
    }

    private val trackerConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as TrackingService.LocationServiceBinder
            trackerService = binder.getService()
            trackerBound = true

            LocalBroadcastManager.getInstance(getApplication()).registerReceiver(locationReceiver, IntentFilter(BROADCAST_MY_LOCATION_CHANGED))

        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            trackerBound = false
        }
    }


    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        Log.i(TAG, "onCreate")
        mSocket.connect()
        mSocket.on(Socket.EVENT_CONNECT, onConnect)

        val trackerIntent = Intent(application, TrackingService::class.java)
        application.bindService(trackerIntent, trackerConnection, Context.BIND_AUTO_CREATE)

        uid = Profile.getCurrentProfile()?.id!!
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(application).registerReceiver(locationReceiver, IntentFilter(BROADCAST_MY_LOCATION_CHANGED))
        super.onDestroy()
    }

    val onConnect: Emitter.Listener = Emitter.Listener {
        Log.i(TAG, "Socket is connected")
    }

    fun disconnect(){
        mSocket.disconnect()
        stopSelf()
    }

    fun sendMessage(invokedMethodName: String, data: JSONObject){
        mSocket.emit(invokedMethodName, data)
    }

    fun sendMessage(invokedMethodName: String, data: String){
        mSocket.emit(invokedMethodName, data)
    }

    fun subscribe(listenMethodName: String, listener: Emitter.Listener){
        Log.i(TAG, "subscribed method $listenMethodName")
        mSocket.on(listenMethodName, listener)
    }
}