package com.example.madcampweek2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

public class MainActivity : AppCompatActivity() {

    val READ_CONTACTS_PERMISSON = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_contact, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //getPermission(Manifest.permission.READ_CONTACTS, READ_CONTACTS_PERMISSON)

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