package com.example.taskaroo;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextTaskName;
    private EditText editTextDescription;
    private EditText editTextDate;
    private EditText editTextTime;
    private EditText editTextReminder;

    private Button buttonSave;
    private Button buttonCancel;
    private Button buttonReset;

    private ImageButton buttonCamera;
    private ImageButton buttonMap;

    private ImageView imageViewCamera;
    private ImageView imageViewMap;

    private TextView textViewLocation;

    private DatabaseHelper databaseHelper;
    private Task currentTask;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_MAP_LOCATION = 2;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private byte[] cameraImage;
    private byte[] mapInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task_activity);

        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextReminder = findViewById(R.id.editTextReminder);
        buttonSave = findViewById(R.id.buttonSaveTask);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonReset = findViewById(R.id.buttonReset);

        buttonCamera = findViewById(R.id.buttonCamera);
        buttonMap = findViewById(R.id.buttonMap);
        imageViewCamera = findViewById(R.id.imageViewCamera);
        imageViewMap = findViewById(R.id.imageViewMap);

        textViewLocation = findViewById(R.id.textViewLocation);

        // Set click listener for imageViewMap
        imageViewMap.setOnClickListener(v -> imageViewMapClick());

        //Open the camera image in the image viewer apps
        imageViewCamera.setOnClickListener(v -> imageViewCameraClick());


        editTextDate.setOnClickListener(v -> showDatePickerDialog());
        editTextTime.setOnClickListener(v -> showTimePickerDialog());

        buttonSave.setOnClickListener(v -> saveTask());
        buttonCancel.setOnClickListener(v -> cancelTask());
        buttonReset.setOnClickListener(v -> resetFields());

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        buttonMap.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                selectMapLocation();
            }
        });

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Get the task ID from the intent
        int taskId = getIntent().getIntExtra("task_id", -1);
        if (taskId != -1) {
            // Load task data
            currentTask = databaseHelper.getTaskById(taskId);
            fillTaskData(currentTask);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void selectMapLocation() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        startActivityForResult(mapIntent, REQUEST_MAP_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            cameraImage = getBitmapAsByteArray(imageBitmap);

            // Display the captured image in the ImageView
            imageViewCamera.setVisibility(View.VISIBLE);
            imageViewCamera.setImageBitmap(imageBitmap);
        }
        if (requestCode == REQUEST_MAP_LOCATION && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                double lat = data.getDoubleExtra("selected_lat", 0);
                double lng = data.getDoubleExtra("selected_lng", 0);
                mapInfo = getMapInfoAsByteArray(lat, lng);

                // Call the method to set location information
                setLocationInfo(lat, lng);
            }
        }
    }

    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    private byte[] getMapInfoAsByteArray(double lat, double lng) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * 2);
        buffer.putDouble(lat);
        buffer.putDouble(lng);
        return buffer.array();
    }

    private void setLocationInfo(double latitude, double longitude) {
        // Set the text with latitude and longitude information
        String locationInfo = "Location: Lat " + latitude + ", Long " + longitude;
        textViewLocation.setText(locationInfo);

        // Make the imageViewMap and textViewLocation visible
        imageViewMap.setVisibility(View.VISIBLE);
        textViewLocation.setVisibility(View.VISIBLE);
    }

    // For selecting date
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    String date = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editTextDate.setText(date);
                }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    // For selecting time
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = selectedHour + ":" + selectedMinute;
                    editTextTime.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    // Save Task
    private void saveTask() {
        try {
            String taskName = editTextTaskName.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String date = editTextDate.getText().toString().trim();
            String time = editTextTime.getText().toString().trim();
            String reminderText = editTextReminder.getText().toString().trim();

            if (taskName.isEmpty() || date.isEmpty() || time.isEmpty() || reminderText.isEmpty()) {
                throw new IllegalArgumentException("Task name, date, time, and reminders are required fields");
            }

            int numberOfNotifications = Integer.parseInt(reminderText);

            if (currentTask != null) {
                // Update existing task
                currentTask.setName(taskName);
                currentTask.setDescription(description);
                currentTask.setDate(date);
                currentTask.setTime(time);
                currentTask.setNumberOfNotifications(numberOfNotifications);
                currentTask.setMapInfo(mapInfo);
                currentTask.setCameraInfo(cameraImage);

                databaseHelper.updateTask(currentTask);
            } else {
                // Add new task
                long taskId = databaseHelper.addTask(taskName, description, date, time, numberOfNotifications, 0, mapInfo, cameraImage);
                currentTask = databaseHelper.getTaskById((int) taskId);
            }

            if (!date.isEmpty() && !time.isEmpty()) {
                scheduleNotification(taskName, description, date, time, numberOfNotifications);
            }

            Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "An error occurred while saving the task", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Schedule Notification for the task
    private void scheduleNotification(String taskName, String description, String date, String time, int numberOfNotifications) {
        try {
            String dateTimeString = date + " " + time;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date dateTime = dateFormat.parse(dateTimeString);
            long dateTimeMillis = dateTime.getTime();

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            for (int i = 1; i <= numberOfNotifications; i++) {
                long triggerTime = dateTimeMillis - i * 60 * 1000; // Remind before i minutes

                Intent intent = new Intent(this, NotificationReceiver.class);
                intent.putExtra("task_name", taskName);
                intent.putExtra("task_description", description);
                intent.putExtra("task_id", currentTask.getId());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, currentTask.getId() * 1000 + i, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                if (alarmManager != null) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void cancelTask() {
        finish();
    }

    private void resetFields() {
        editTextTaskName.setText("");
        editTextDescription.setText("");
        editTextDate.setText("");
        editTextTime.setText("");
        editTextReminder.setText("");
        imageViewCamera.setVisibility(View.GONE);
        imageViewMap.setVisibility(View.GONE);
        cameraImage = null;
        mapInfo = null;
        textViewLocation.setVisibility(View.GONE);
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return false;
        } else {
            return true;
        }
    }

    private void fillTaskData(Task task) {
        editTextTaskName.setText(task.getName());
        editTextDescription.setText(task.getDescription());
        editTextDate.setText(task.getDate());
        editTextTime.setText(task.getTime());
        editTextReminder.setText(String.valueOf(task.getNumberOfNotifications()));

        // Display the saved camera image
        if (task.getCameraInfo() != null) {
            Bitmap cameraBitmap = BitmapFactory.decodeByteArray(task.getCameraInfo(), 0, task.getCameraInfo().length);
            imageViewCamera.setVisibility(View.VISIBLE);
            imageViewCamera.setImageBitmap(cameraBitmap);
        }

        // Display the saved map location
        if (task.getMapInfo() != null && task.getMapInfo().length > 0) {
            ByteBuffer buffer = ByteBuffer.wrap(task.getMapInfo());
            double lat = buffer.getDouble();
            double lng = buffer.getDouble();
            String locationInfo = "Location: Lat " + lat + ", Long " + lng;
            textViewLocation.setText(locationInfo);
            textViewLocation.setVisibility(View.VISIBLE);
            imageViewMap.setVisibility(View.VISIBLE);
        } else {
            textViewLocation.setVisibility(View.GONE);
            imageViewMap.setVisibility(View.GONE);
        }
    }

    // Method to handle imageViewMap click event
    public void imageViewMapClick() {
        String locationText = textViewLocation.getText().toString().trim();
        if (!locationText.isEmpty()) {
            String[] parts = locationText.split(":");
            if (parts.length == 2) {
                String[] coordinates = parts[1].trim().split(",");
                if (coordinates.length == 2) {
                    double latitude = Double.parseDouble(coordinates[0].trim().substring(4)); // Extract latitude
                    double longitude = Double.parseDouble(coordinates[1].trim().substring(5)); // Extract longitude

                    // Log the extracted latitude and longitude
                    Log.d("Latitude", String.valueOf(latitude));
                    Log.d("Longitude", String.valueOf(longitude));

                    // Create a URI with the latitude and longitude
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    mapIntent.setPackage("com.google.android.apps.maps"); // Open in Google Maps app
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        // If Google Maps app is not available, open in a browser
                        mapIntent.setData(Uri.parse("https://www.google.com/maps?q=" + latitude + "," + longitude));
                        startActivity(mapIntent);
                    }
                    return;
                }
            }
        }
        // If location information is not available or formatted incorrectly, show a message
        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
    }

    // Method to handle imageViewCamera click event
    public void imageViewCameraClick() {
        if (currentTask != null && currentTask.getCameraInfo() != null) {
            // Pass the image byte array to DisplayImageActivity
            Intent intent = new Intent(this, DisplayImageActivity.class);
            intent.putExtra("imageByteArray", currentTask.getCameraInfo());
            startActivity(intent);
        } else {
            // If the camera image is null or task is null, show a message
            Toast.makeText(this, "No image available", Toast.LENGTH_SHORT).show();
        }
    }



}

