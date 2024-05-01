package com.example.taskaroo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextTaskName;
    private EditText editTextDescription;
    private EditText editTextDate;
    private EditText editTextTime;
    private Button buttonSave;
    private Button buttonCancel;
    private Button buttonReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Save the task to local storage (SQLite) using DatabaseHelper
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        long result = databaseHelper.addTask(taskName, description, date, time);

        if (result != -1) {
            Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
            // Display the task in the MainActivity window
            // You can implement this part based on your UI design
            // You may need to pass the task details to MainActivity
            // through Intent or use a ViewModel to share data between activities
        } else {
            Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
        }

        // Show notification if needed
       // if (/* condition to check if notification is required */) {
       //     showNotification();
       // }

        // Finish the activity and go back to MainActivity
        finish();
    }

    private void cancelTask() {
        // Prompt user confirmation with Yes or No
        // If Yes, go back to MainActivity
        // If No, do nothing
        // You can use an AlertDialog or DialogFragment for confirmation
    }

    private void resetFields() {
        editTextTaskName.setText("");
        editTextDescription.setText("");
        editTextDate.setText("");
        editTextTime.setText("");
    }

    private void showNotification() {
        // NotificationManager code here...
        // You can create and show a notification using NotificationCompat
    }
}
