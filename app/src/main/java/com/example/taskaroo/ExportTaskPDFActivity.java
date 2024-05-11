package com.example.taskaroo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportTaskPDFActivity extends AppCompatActivity {

    private CalendarAdapter calendarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);

        RecyclerView recyclerView = findViewById(R.id.calendarRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 7));

        List<String> daysOfMonth = generateDaysOfMonth();

        // Create an empty list of tasks
        List<Task> tasks = new ArrayList<>();

        // Pass both lists to the adapter
        calendarAdapter = new CalendarAdapter(daysOfMonth, tasks);

        recyclerView.setAdapter(calendarAdapter);

        // Find the generate calendar button
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button generateCalendarButton = findViewById(R.id.generateCalendarButton);
        generateCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Export tasks to the calendar
                exportTasksToCalendar();
            }
        });
    }

    private List<String> generateDaysOfMonth() {
        List<String> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // Set to the first day of the month
        calendar.set(year, month, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= daysInMonth; i++) {
            days.add(String.valueOf(i));
        }
        return days;
    }

    private void exportTasksToCalendar() {
        // Get the list of tasks from the database
        List<Task> tasks = getTasksFromDatabase();

        // Iterate over the tasks and pin them to the calendar
        for (Task task : tasks) {
            // Get the date of the task and convert it to the corresponding position in the RecyclerView
            int position = calculatePositionForTask(task.getDate());

            // Update the calendar adapter to display the task on the corresponding date
            calendarAdapter.pinTask(position, task);
        }
    }

    // Replace this method with your actual database access logic
    private List<Task> getTasksFromDatabase() {
        // Dummy implementation for demonstration purposes
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task()); // Example task with today's date
        tasks.add(new Task()); // Example task with tomorrow's date
        return tasks;
    }

    // Replace this method with your actual position calculation logic
    private int calculatePositionForTask(String dateString) {
        // Dummy implementation: convert the date string to a Date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date taskDate = dateFormat.parse(dateString);
            // Calculate position based on the day of the month
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(taskDate);
            return calendar.get(Calendar.DAY_OF_MONTH) - 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Handle parsing error
        }
    }

}
