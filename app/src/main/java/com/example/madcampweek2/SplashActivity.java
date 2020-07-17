package com.example.madcampweek2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.madcampweek2.ui.LoginActivity;
import com.facebook.AccessToken;

public class SplashActivity extends Activity {
    ImageView imageView;            // Logo ImageView
    Animation imageAni;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imageView = (ImageView) findViewById(R.id.splash_logo);
        imageAni = AnimationUtils.loadAnimation(this, R.anim.anim_splash_imageview);
        imageView.startAnimation(imageAni);

        imageAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                if(isLoggedIn){
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }else{
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

    }
}