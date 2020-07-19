package com.example.madcampweek2.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
            override fun onSuccess(loginResult: LoginResult?) {
                // App code
                Log.d(TAG, "(button) onSuccess")
                Log.d(TAG, AccessToken.getCurrentAccessToken().toString())
                Log.d(TAG, Profile.getCurrentProfile()?.toString() ?: "none")
                Log.d(TAG, Profile.getCurrentProfile()?.id ?: "none")
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
            override fun onCancel() {
                // App code
            }
            override fun onError(exception: FacebookException) {
                // App code
            }
        })

//        LoginManager.getInstance().registerCallback(callbackManager,
//            object : FacebookCallback<LoginResult?> {
//                override fun onSuccess(loginResult: LoginResult?) {
//                    // App code
//                    Log.d(TAG, "(manager) onSuccess")
//                }
//                override fun onCancel() {
//                    // App code
//                }
//                override fun onError(exception: FacebookException) {
//                    // App code
//                }
//            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}