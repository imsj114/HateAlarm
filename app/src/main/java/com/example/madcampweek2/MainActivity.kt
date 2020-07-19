package com.example.madcampweek2

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.ArrayAdapter
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.madcampweek2.ui.MainViewModel
import com.example.madcampweek2.model.Contact
import com.google.android.material.bottomnavigation.BottomNavigationView


public class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val READ_CONTACTS_PERMISSON = 1
    val viewModel : MainViewModel by viewModels()

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

        val button = findViewById<Button>(R.id.button_test)
        button.setOnClickListener(this)

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

    override fun onClick(p0: View?) {
        viewModel._contacts.value = listOf(
            Contact().apply{
                name = "Ryan"
                phoneNumber = "010-1234-5678"
                profile = R.drawable.ryan
            },
            Contact().apply{
                name = "Apeach"
                phoneNumber = "010-5678-1234"
                profile = R.drawable.apeach
            }
        )
    }
}