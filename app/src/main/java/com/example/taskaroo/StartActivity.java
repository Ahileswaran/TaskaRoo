package com.example.taskaroo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;



public class StartActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ImageView imageView = findViewById(R.id.imageView);

        // Load the first GIF
        Glide.with(this).load(R.drawable.do_animation).into(imageView);

        // Handler to switch to the second GIF after first completes
        new Handler().postDelayed(() -> {
            Glide.with(this).load(R.drawable.taskaroo).into(imageView);

            // Handler to start MainActivity after second GIF
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            }, 5000); // Time in milliseconds for the second GIF
        }, 3000); // Time in milliseconds for the first GIF
    }


}