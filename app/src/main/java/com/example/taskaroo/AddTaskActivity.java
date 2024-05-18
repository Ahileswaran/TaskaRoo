package com.example.taskaroo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

    private Button buttonCamera;
    private Button buttonMap;
    private DatabaseHelper databaseHelper;
    private Task currentTask;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_MAP_LOCATION = 2;

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

        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        }
        if (requestCode == REQUEST_MAP_LOCATION && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                double lat = data.getDoubleExtra("selected_lat", 0);
                double lng = data.getDoubleExtra("selected_lng", 0);
                mapInfo = getMapInfoAsByteArray(lat, lng);
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date dateTime = sdf.parse(dateTimeString);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateTime);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Intent notificationIntent = new Intent(this, NotificationReceiver.class);
            notificationIntent.putExtra("task_name", taskName);
            notificationIntent.putExtra("description", description);

            for (int i = 0; i < numberOfNotifications; i++) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, i, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (alarmManager != null) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + (i * AlarmManager.INTERVAL_HOUR), pendingIntent);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // Cancel Task
    private void cancelTask() {
        finish();
    }

    // Reset fields
    private void resetFields() {
        editTextTaskName.setText("");
        editTextDescription.setText("");
        editTextDate.setText("");
        editTextTime.setText("");
        editTextReminder.setText("");
    }

    // Fill task data
    private void fillTaskData(Task task) {
        editTextTaskName.setText(task.getName());
        editTextDescription.setText(task.getDescription());
        editTextDate.setText(task.getDate());
        editTextTime.setText(task.getTime());
        editTextReminder.setText(String.valueOf(task.getNumberOfNotifications()));
    }
}
