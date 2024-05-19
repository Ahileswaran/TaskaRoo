package com.example.taskaroo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DisplayImageActivity extends AppCompatActivity {

    private ImageView imageView;
    private byte[] imageByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        // Get the image byte array from the intent
        imageByteArray = getIntent().getByteArrayExtra("imageByteArray");

        imageView = findViewById(R.id.imageView);

        // Load the image into the ImageView
        if (imageByteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            imageView.setImageBitmap(bitmap);
        }

        // Set click listener for the save button
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToDevice();
            }
        });
    }

    private void saveImageToDevice() {
        // Check if the image byte array is available
        if (imageByteArray != null) {
            // Convert the byte array to Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);

            // Save the image to the device storage
            String savedImagePath = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    bitmap,
                    "Task Image",
                    "Image captured for task"
            );

            // Show a toast message indicating whether the image was saved successfully
            if (savedImagePath != null) {
                Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image available", Toast.LENGTH_SHORT).show();
        }
    }
}
