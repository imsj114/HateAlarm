package com.example.madcampweek2.ui.maps

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.DialogInterface
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.madcampweek2.R
import com.example.madcampweek2.model.MapUser
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar


class MapsFragment : Fragment() , View.OnClickListener{
    private val TAG = "TAG_Map"
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val DEFAULT_ZOOM: Float = 16.0F
    private var ALERT_RADIUS = 5.0
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
    private var firstTime = true

    lateinit var sp: SharedPreferences

    lateinit var fab_open : Animation
    lateinit var fab_close : Animation
    lateinit var fab: FloatingActionButton
    lateinit var fab1: FloatingActionButton
    lateinit var fab2: FloatingActionButton
    lateinit var textView: TextView
    var isFabOpen = false
    var isBound = false
    var isTrackingMode = false
    var myLastLocation: LatLng? = null
    var mediaPlayer: MediaPlayer? = null

    private val model: MapsViewModel by activityViewModels()

    private val callback = OnMapReadyCallback { map ->
        mMap = map
        updateLocationUI()

        model.getUsers().observe(viewLifecycleOwner, Observer<List<MapUser>>{ list ->
            mMap!!.clear()
            list.map{
                mMap!!.addMarker(MarkerOptions().apply {
                    position(it.toLatLng())
                    title(it.name)
                    //if(!it.blocked) icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    if(it.blocked){
                        if(it.online) icon(BitmapDescriptorFactory.fromResource(R.drawable.onlinehulkdiv3))
                        else icon(BitmapDescriptorFactory.fromResource(R.drawable.offlinhulkdiv3))
                    }else{
                        if(it.online) icon(BitmapDescriptorFactory.fromResource(R.drawable.onlineironmandiv3))
                        else icon(BitmapDescriptorFactory.fromResource(R.drawable.offlineironmandiv3))
                    }

                })
                if(myLastLocation != null && it.blocked && it.online){
                    val r= it.getDistanceFrom(myLastLocation!!)
                    if( r < ALERT_RADIUS) {
                        Snackbar.make(activity?.findViewById(R.id.nav_host_fragment)!!, "ALERT!! distance: $r", Snackbar.LENGTH_SHORT).show()
                        mediaPlayer = MediaPlayer.create(activity, R.raw.siren0)
                        mediaPlayer!!.start()
                    }
                }
            }
        })
        model.getMyLocation().observe(viewLifecycleOwner, Observer<LatLng?>{
            if(firstTime){
                setDeviceLocation(it)
                firstTime = false
            }
            myLastLocation = it
        })
        model.getIsOnline().observe(viewLifecycleOwner, Observer {isOnline ->
            if(isOnline){
                textView.text = "online"
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            } else{
                textView.text = "offline"
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_maps, container, false)

        fab_open =
            AnimationUtils.loadAnimation(activity, R.anim.fab_open)
        fab_close =
            AnimationUtils.loadAnimation(activity, R.anim.fab_close)

        fab = view.findViewById(R.id.fab_map) as FloatingActionButton
        fab1 = view.findViewById(R.id.fab_map1) as FloatingActionButton
        fab2 = view.findViewById(R.id.fab_map2) as FloatingActionButton
        textView = view.findViewById(R.id.map_text)

        fab.setOnClickListener(this)
        fab1.setOnClickListener(this)
        fab2.setOnClickListener(this)

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

    // Fab open/close switch
    private fun switchFab() {
        if (isFabOpen) {
            fab.setImageResource(R.drawable.ic_baseline_add_circle_24)
            fab1.startAnimation(fab_close)
            fab2.startAnimation(fab_close)
            fab1.setClickable(false)
            fab2.setClickable(false)
            isFabOpen = false
        } else {
            fab.setImageResource(R.drawable.ic_baseline_cancel_24)
            fab1.startAnimation(fab_open)
            fab2.startAnimation(fab_open)
            fab1.setClickable(true)
            fab2.setClickable(true)
            isFabOpen = true
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab_map -> switchFab()
            R.id.fab_map1 -> {
                switchFab()
                Log.i(TAG, isTrackingMode.toString())
                model.changeSocketServiceState()
            }
            R.id.fab_map2 -> {
                switchFab()
                //model.removeBlacklist("test_uid")
                findNavController().navigate(R.id.navigation_user_list)
            }
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val name: String = className.className
            if (name.endsWith("TrackingService")) {
                Log.i(TAG, "CONNECT")
                gpsService = (service as TrackingService.LocationServiceBinder).getService()
                gpsService!!.startTracking()
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            if (className.className == "TrackingService") {

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
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }

    private fun setDeviceLocation(myLocation: LatLng?) {
        /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        mMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    myLocation!!.latitude,
                    myLocation!!.longitude
                ), DEFAULT_ZOOM
            )
        )
            ?: {
                Log.d(TAG, "Current location is null. Using defaults.")
                mMap!!.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        mDefaultLocation,
                        DEFAULT_ZOOM
                    )
                )
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
            }()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.settings -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("접근 반경 (단위: m)")
                val input = EditText(requireContext())
                input.inputType = InputType.TYPE_CLASS_NUMBER
                builder.setView(input)
                builder.setPositiveButton("확인") { p0, p1 ->
                    ALERT_RADIUS = input.text.toString().toDoubleOrNull() ?: 5.0
                    Log.i(TAG, ">>> $ALERT_RADIUS")
                }
                builder.setNegativeButton("취소") { p0, p1 -> p0?.cancel() }
                builder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}