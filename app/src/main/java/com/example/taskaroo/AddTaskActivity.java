package com.example.taskaroo;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextTaskName;
    private EditText editTextDescription;
    private EditText editTextDate;
    private EditText editTextTime;
    private Button buttonSave;
    private Button buttonCancel;
    private Button buttonReset;

    private DatabaseHelper databaseHelper;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // Inflate the custom layout for the ActionBar logo
        View actionBarLogo = getLayoutInflater().inflate(R.layout.action_bar_logo, null);

        // Set the custom layout as the logo in the ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(actionBarLogo);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task_activity);

        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        buttonSave = findViewById(R.id.buttonSaveTask);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonReset = findViewById(R.id.buttonReset);

        editTextDate.setOnClickListener(v -> showDatePickerDialog());
        editTextTime.setOnClickListener(v -> showTimePickerDialog());

        buttonSave.setOnClickListener(v -> saveTask());
        buttonCancel.setOnClickListener(v -> cancelTask());
        buttonReset.setOnClickListener(v -> resetFields());

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

    private void saveTask() {
        String taskName = editTextTaskName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();

        if (taskName.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Task name, date, and time are required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long result;
        if (currentTask != null) {
            // Update existing task
            currentTask.setName(taskName);
            currentTask.setDescription(description);
            currentTask.setDate(date);
            currentTask.setTime(time);
            result = databaseHelper.updateTask(currentTask);
        } else {
            // Add new task
            // Modified to set COL_COMPLETED to 0 for new tasks
            result = databaseHelper.addTask(taskName, description, date, time, 0);
        }

        if (result != -1) {
            Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
        }

        finish();
    }


    private void scheduleNotification(String taskName, String description, String date, String time) {
        try {
            // Combine date and time strings and parse into Date object
            String dateTimeString = date + " " + time;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date dateTime = sdf.parse(dateTimeString);

            // Create Calendar object and set the time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateTime);

            // Get AlarmManager service
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            // Create intent for the notification
            Intent notificationIntent = new Intent(this, NotificationReceiver.class);
            notificationIntent.putExtra("task_name", taskName);
            notificationIntent.putExtra("description", description);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Schedule the notification
            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void cancelTask() {
        // Implement cancel task functionality
        // You can show a confirmation dialog here
        finish();
    }

    private void resetFields() {
        editTextTaskName.setText("");
        editTextDescription.setText("");
        editTextDate.setText("");
        editTextTime.setText("");
    }

    private void fillTaskData(Task task) {
        if (task != null) {
            editTextTaskName.setText(task.getName());
            editTextDescription.setText(task.getDescription());
            editTextDate.setText(task.getDate());
            editTextTime.setText(task.getTime());
        }
    }

}
