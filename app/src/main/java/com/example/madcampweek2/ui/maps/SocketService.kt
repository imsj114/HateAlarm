package com.example.madcampweek2.ui.maps

import android.app.Service
import android.content.Intent
import android.location.GnssAntennaInfo
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.madcampweek2.R
import com.google.gson.JsonObject
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.lang.Exception
import java.net.URISyntaxException

class SocketService : Service() {
    companion object {
        val instance : SocketService = SocketService()
        const val TAG = "TAG_SocketService"
    }
    val mSocket: Socket = IO.socket("http://192.249.19.243:880")
    val binder = SocketBinder()

    inner class SocketBinder : Binder() {
        fun getService(): SocketService = this@SocketService
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        Log.i(TAG, "onCreate")
        mSocket.connect()
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    val onConnect: Emitter.Listener = Emitter.Listener {
        Log.i(TAG, "Socket is connected")
    }

    fun disconnect(){
        mSocket.disconnect()
        stopSelf()
    }

    fun sendMessage(invokedMethodName: String, data: JsonObject){
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