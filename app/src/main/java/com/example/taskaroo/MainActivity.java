package com.example.taskaroo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;
    private List<Task> taskList; // Declare taskList
    private AlertDialog deleteConfirmationDialog; // Declare AlertDialog variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Set click listener for the FAB to open AddTaskActivity
        fabAddTask.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Initialize RecyclerView
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this); // Provide the Context object
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(taskAdapter);

        // Display tasks
        displayTasks();

        // ItemTouchHelper for swiping tasks
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Remove the swiped task from the list
                int position = viewHolder.getAdapterPosition();
                Task removedTask = taskList.get(position);
                taskList.remove(position);
                taskAdapter.notifyItemRemoved(position);
                // Show confirmation dialog for task deletion
                showDeleteConfirmationDialog(removedTask);
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(taskRecyclerView);
    }

    // Method to display tasks
    private void displayTasks() {
        taskList.clear();
        taskList.addAll(databaseHelper.getAllTasks());
        taskAdapter.setTasks(taskList);
    }

    // Method to show delete confirmation dialog
    private void showDeleteConfirmationDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the task
                    deleteTask(task);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss the dialog
                    dialog.dismiss();
                });
        deleteConfirmationDialog = builder.create();
        deleteConfirmationDialog.show();
    }

    // Method to delete task
    private void deleteTask(Task task) {
        // Implement task deletion logic here
        // You need to remove the task from the database and update the UI
    }

    // Override onDestroy to dismiss the dialog if it's showing
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deleteConfirmationDialog != null && deleteConfirmationDialog.isShowing()) {
            deleteConfirmationDialog.dismiss();
        }
    }

    // Override onResume to refresh the task list when the activity resumes
    @Override
    protected void onResume() {
        super.onResume();
        displayTasks();  // Refresh the task list
    }
}
