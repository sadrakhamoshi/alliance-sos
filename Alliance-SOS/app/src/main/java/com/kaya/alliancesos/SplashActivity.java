package com.kaya.alliancesos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    TextView textView;
    Animation anim;
    ImageView imageView;
    View view1, view2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        textView = findViewById(R.id.splash_text);
        imageView = findViewById(R.id.splash_image);
        view1 = findViewById(R.id.view_1);
        view2 = findViewById(R.id.view_2);
        anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(getApplicationContext(), LogInPage.class));
                finish();
                // HomeActivity.class is the activity to go after showing the splash screen.
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        textView.startAnimation(anim);
        imageView.startAnimation(anim);
        view1.startAnimation(anim);
        view2.startAnimation(anim);
    }
}