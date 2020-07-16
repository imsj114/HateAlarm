package com.example.madcampweek2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {

    TextView textView;              // TextView
    ImageView imageView;            // Logo ImageView
    Animation textAni, imageAni, swipeUpAni, swipeDownAni;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        textView = (TextView) findViewById(R.id.splash_text);
        imageView = (ImageView) findViewById(R.id.splash_logo);
        textAni = AnimationUtils.loadAnimation(this, R.anim.anim_splash_textview);
        imageAni = AnimationUtils.loadAnimation(this, R.anim.anim_splash_imageview);
        swipeUpAni = AnimationUtils.loadAnimation(this, R.anim.anim_splash_out_top);
        swipeDownAni = AnimationUtils.loadAnimation(this, R.anim.anim_splash_in_down);
        textView.startAnimation(textAni);
        imageView.startAnimation(imageAni);
        imageAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(this, MainActivity.class));
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
//        animation = AnimationUtils.loadAnimation(this, R.anim.ani);
//        // Wait for fixed time designated in Thread.sleep
//        // without this part, it finished right after the main activity loading's done
//        try{
//            Thread.sleep(4000);
//        } catch (InterruptedException e){
//            e.printStackTrace();
//        }
//
//        // Wait for MainActivity loading
//        startActivity(new Intent(this, MainActivity.class));
//        finish();           // Terminates splash screen
    }
}