package com.example.madcampweek2.ui.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
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
        fab.setOnClickListener(this)

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
        Log.d(TAG, "onClick")
        if(!isTrackingMode){
            isTrackingMode = true
            val intent = Intent(context, TrackingService::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                Log.d(TAG, "startForegroundService")
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        }else{
            isTrackingMode = false
            val intent = Intent(context, TrackingService::class.java)
            context!!.stopService(intent)
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