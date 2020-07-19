package com.example.madcampweek2.ui.maps

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.madcampweek2.MainActivity
import com.example.madcampweek2.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.Marker

const val TAG = "TAG_TrackingService"


class TrackingService : Service() {
    private val binder = LocationServiceBinder()
    private val CHANNEL_ID = "ForegroundService Kotlin"
    private lateinit var locationCallback: LocationCallback
    private var mLocationManager: LocationManager? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLastLocation: Location

    internal var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.i(TAG, "- onLocationResult()")
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Log.i(TAG, "Location: " + location.getLatitude() + " " + location.getLongitude())
                Toast.makeText(applicationContext, "Location: " + location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_LONG).show()
                mLastLocation = location
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i(TAG, "- onBind()")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "- onCreate()")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //do heavy work on a background thread
        Log.i(TAG, "- onStartCommand()")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service Kotlin Example")
            .setContentText("Tracking mode ON")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        return START_NOT_STICKY
    }


    fun startTracking() {
        Log.i(TAG, "- startTracking()")

        //stopSelf()
        val INTERVAL = 5L
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = INTERVAL
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("TAG", "!!!!!!!need permission!!!!!!!")
            // Todo: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
            mLocationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopTracking() {
        Log.i(TAG, "- stopTracking()")
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "- onDestroy()")
    }


    inner class LocationServiceBinder : Binder() {
        fun getService(): TrackingService {
            return this@TrackingService
        }
    }

}