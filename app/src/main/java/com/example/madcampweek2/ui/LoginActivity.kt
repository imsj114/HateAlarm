package com.example.madcampweek2.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.madcampweek2.MainActivity
import com.example.madcampweek2.R
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton


class LoginActivity : AppCompatActivity() {
    private val TAG = "TAG"
    private lateinit var callbackManager : CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)


        callbackManager = CallbackManager.Factory.create()
        val button = findViewById<LoginButton>(R.id.login_button)
        button.setPermissions("email")
        button.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            private var mProfileTracker: ProfileTracker? = null
            override fun onSuccess(loginResult: LoginResult?) {
                if (Profile.getCurrentProfile() == null) {
                    mProfileTracker = object : ProfileTracker() {
                        override fun onCurrentProfileChanged(
                            oldProfile: Profile?,
                            currentProfile: Profile
                        ) {
                            Log.d("facebook - profile", currentProfile.firstName)
                            mProfileTracker?.stopTracking()
                        }
                    }
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                } else {
                    val profile = Profile.getCurrentProfile()
                    Log.v("facebook - profile", profile.firstName)
                }
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
            override fun onCancel() {
                // App code
            }
            override fun onError(exception: FacebookException) {
                // App code
                exception.message?.let { Log.d("facebook - onError", it) }
            }
        })

//        LoginManager.getInstance().registerCallback(callbackManager,
////            object : FacebookCallback<LoginResult?> {
////                override fun onSuccess(loginResult: LoginResult?) {
////                    // App code
////                    Log.d(TAG, "(manager) onSuccess")
////                }
////                override fun onCancel() {
////                    // App code
////                }
////                override fun onError(exception: FacebookException) {
////                    // App code
////                }
//            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")
        if(callbackManager.onActivityResult(requestCode, resultCode, data)){

        }
    }
}