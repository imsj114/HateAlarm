package com.example.madcampweek2

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.facebook.Profile
import com.bumptech.glide.Glide
import com.example.madcampweek2.ui.LoginActivity
import com.facebook.ProfileTracker


public class MainActivity : AppCompatActivity(){

    private val READ_CONTACTS_PERMISSON = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_contact, R.id.navigation_gallery, R.id.navigation_map))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val logout_button = findViewById<Button>(R.id.user_logout)
        logout_button.setOnClickListener{
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }

        //getPermission(Manifest.permission.READ_CONTACTS, READ_CONTACTS_PERMISSON)
        val login_status = findViewById<TextView>(R.id.login_status)
        val user_image = findViewById<ImageView>(R.id.user_image)
        val user_name = findViewById<TextView>(R.id.user_id)
        login_status.setText("User Profile")
        var mProfileTracker: ProfileTracker? = null
        if (Profile.getCurrentProfile() == null) {
            mProfileTracker = object : ProfileTracker() {
                override fun onCurrentProfileChanged(
                    oldProfile: Profile?,
                    currentProfile: Profile
                ) {
                    Log.d("facebook - profile", currentProfile.firstName)
                    mProfileTracker?.stopTracking()
                    Glide.with(this@MainActivity)
                        .load(Profile.getCurrentProfile()?.getProfilePictureUri(300,300))
                        .circleCrop()
                        .fitCenter()
                        .override(300, 300)
                        .placeholder(R.drawable.image_load)
                        .into(user_image)
                    user_name.setText(Profile.getCurrentProfile()!!.lastName + Profile.getCurrentProfile()!!.firstName)
                }
            }
            // no need to call startTracking() on mProfileTracker
            // because it is called by its constructor, internally.
        } else {
            val profile = Profile.getCurrentProfile()
            Log.v("facebook - profile", profile.firstName)
            Glide.with(this)
                .load(Profile.getCurrentProfile()?.getProfilePictureUri(300,300))
                .circleCrop()
                .fitCenter()
                .override(400, 400)
                .placeholder(R.drawable.image_load)
                .into(user_image)
            user_name.setText(Profile.getCurrentProfile()!!.lastName + Profile.getCurrentProfile()!!.firstName)
        }

    }

    fun getPermission(permissionId: String, permssionCode: Int) {
        val permissonCheck = ContextCompat.checkSelfPermission(this, permissionId)
        if (permissonCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                applicationContext,
                "$permissionId permission granted", Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                applicationContext,
                "$permissionId permission denied", Toast.LENGTH_SHORT
            ).show()
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionId)) {
                // Explanation for permission requirement
                Toast.makeText(
                    applicationContext,
                    "$permissionId 권한이 필요합니다.", Toast.LENGTH_SHORT
                ).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permissionId),
                    permssionCode
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permissionId),
                    permssionCode
                )
            }
        }
    }

}