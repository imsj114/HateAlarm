package com.example.madcampweek2.ui.maps

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.madcampweek2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MapsFragment : Fragment() , View.OnClickListener{
    private val TAG = "TAG_Map"
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val DEFAULT_ZOOM: Float = 16.0F
    private var isTrackingMode = false
    val placesClient = lazy {
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        Places.createClient(requireContext())
    }
    private var mMap: GoogleMap? = null
    private var mLocationPermissionGranted = true
    private var mLastKnownLocation : Location? = null
    private val mDefaultLocation = LatLng(-3.0, 151.0)
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var gpsService: TrackingService? = null

    private val callback = OnMapReadyCallback { map ->
        mMap = map
        updateLocationUI()
        getDeviceLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_maps, container, false)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_map)
        val fab2 = view.findViewById<FloatingActionButton>(R.id.fab_map2)
        fab.setOnClickListener(this)
        fab2.setOnClickListener(this)

        val intent = Intent(context, TrackingService::class.java)
        ContextCompat.startForegroundService(requireContext(), intent)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onClick(p0: View?) {

        when(p0?.id){
            R.id.fab_map -> {
                if(!isTrackingMode){
                    isTrackingMode = true
                    Log.i(TAG, "start tracking")
                    gpsService!!.startTracking()
                    //TrackingService.startService(requireContext(), "Foreground service now running..")
                }else{
                    isTrackingMode = false
                    Log.i(TAG, "stop tracking")
                    gpsService!!.stopTracking()
                }
            }
            R.id.fab_map2 -> {

            }
        }

    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val name: String = className.className
            if (name.endsWith("TrackingService")) {
                Log.i(TAG, "onServiceConnected")
                gpsService = (service as TrackingService.LocationServiceBinder).getService()
                Log.i(TAG, ">> (Fragment) $gpsService")
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            if (className.className == "TrackingService") {
                gpsService = null
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun getLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (mLocationPermissionGranted) {
                mMap!!.setMyLocationEnabled(true)
                mMap!!.getUiSettings().setMyLocationButtonEnabled(true)
            } else {
                mMap!!.setMyLocationEnabled(false)
                mMap!!.getUiSettings().setMyLocationButtonEnabled(false)
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }

    private fun getDeviceLocation() {
        /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()
                ) { task ->
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult()
                        mMap!!.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mLastKnownLocation!!.latitude,
                                    mLastKnownLocation!!.longitude
                                ), DEFAULT_ZOOM
                            )
                        )
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.getException())
                        mMap!!.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                mDefaultLocation,
                                DEFAULT_ZOOM
                            )
                        )
                        mMap!!.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }
}