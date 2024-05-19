package com.example.taskaroo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class StartActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Hide the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView imageView = findViewById(R.id.imageView);

        // Load the first GIF
        Glide.with(this).load(R.drawable.do_animation).into(imageView);

        // Handler to switch to the second GIF after the first one completes
        new Handler().postDelayed(() -> {
            Glide.with(this).load(R.drawable.taskaroo).into(imageView);

            // Handler to start MainActivity after the second GIF
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            }, 5000); // Time in milliseconds for the second GIF
        }, 3000); // Time in milliseconds for the first GIF

        // Adding a touch listener to switch to MainActivity when the screen is touched
        imageView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Start MainActivity
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close this activity
                    return true;
            }
            return false;
        });
    }
}
