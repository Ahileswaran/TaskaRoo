package com.example.taskaroo;

import android.annotation.SuppressLint;
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

import androidx.appcompat.app.AppCompatActivity;

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
    private EditText editTextReminders;
    private DatabaseHelper databaseHelper;
    private Task currentTask;
    private int completed;
    private View editTextNumberOfNotifications;

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

    //For select date
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

    //For select time
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

    //Save Task
    private void saveTask() {
        String taskName = editTextTaskName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();
        int numberOfNotifications = Integer.parseInt(editTextReminder.getText().toString().trim());


        if (taskName.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Task name, date, and time are required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentTask != null) {
            // Update existing task
            currentTask.setName(taskName);
            currentTask.setDescription(description);
            currentTask.setDate(date);
            currentTask.setTime(time);
            currentTask.setNumberOfNotifications(numberOfNotifications);

            // Update task with the new numberOfNotifications directly
            databaseHelper.updateTask(currentTask);

        } else {
            // Add new task
            long result = databaseHelper.addTask(taskName, description, date, time, numberOfNotifications, completed);

            // Retrieve the newly added task from the database
            currentTask = databaseHelper.getLastAddedTask();
        }

        if (!date.isEmpty() && !time.isEmpty()) {
            scheduleNotification(taskName, description, date, time, numberOfNotifications);
        }

        Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
        finish();

    }

    //Schedule Notification for the task
    private void scheduleNotification(String taskName, String description, String date, String time, int numberOfNotifications) {
        // Set the first notification displayed flag for the task

        try {
            // Combine date and time strings and parse into Date object
            String dateTimeString = date + " " + time;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
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

            // Create multiple pending intents for each notification
            for (int i = 0; i < numberOfNotifications; i++) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, i, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                // Schedule the notification
                if (alarmManager != null) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + (i * AlarmManager.INTERVAL_HOUR), pendingIntent);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    //Cancel button method
    private void cancelTask() {
        // Implement cancel task functionality
        // You can show a confirmation dialog here
        finish();
    }

    //Reset field method
    private void resetFields() {
        editTextTaskName.setText("");
        editTextDescription.setText("");
        editTextDate.setText("");
        editTextTime.setText("");
        editTextReminder.setText("");
    }
    //Field tasks
    @SuppressLint("WrongViewCast")
    private void fillTaskData(Task task) {
        if (task != null) {
            editTextTaskName.setText(task.getName());
            editTextDescription.setText(task.getDescription());
            editTextDate.setText(task.getDate());
            editTextTime.setText(task.getTime());
            editTextReminder.setText(String.valueOf(task.getNumberOfNotifications()));
        }
    }


}
