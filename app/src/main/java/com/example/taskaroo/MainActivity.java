package com.example.taskaroo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import kotlinx.coroutines.scheduling.Task;

public class MainActivity extends AppCompatActivity {

    private LinearLayout taskContainer;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        taskContainer = findViewById(R.id.taskContainer);
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Set click listener for the FAB to open AddTaskActivity
        fabAddTask.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Display tasks in the container
        displayTasks();
    }

    private void displayTasks() {
        // Retrieve tasks from the database
        List<Task> tasks = databaseHelper.getAllTasks();

        // Clear existing views from the container
        taskContainer.removeAllViews();

        // Add each task to the container
        for (Task task : tasks) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            TaskView taskView = new TaskView(this);
            taskView.setTask(task);
            taskContainer.addView(taskView, layoutParams);
        }
    }
}
