package com.example.taskaroo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;
    private List<Task> taskList;
    private AlertDialog deleteConfirmationDialog;
    private DrawerLayout drawerLayout;

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
        taskAdapter = new TaskAdapter();
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(taskAdapter);

        // Display tasks
        displayTasks();

        // Set up click listener through interface
        taskAdapter.setOnTaskClickListener(task -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            intent.putExtra("task_id", task.getId());
            startActivity(intent);
        });

        // ItemTouchHelper for swiping tasks
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task removedTask = taskList.get(position);
                taskList.remove(position);
                taskAdapter.notifyItemRemoved(position);
                showDeleteConfirmationDialog(removedTask);
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(taskRecyclerView);

        // Navigation Drawer setup
        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set navigation item click listener
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayTasks();  // Refresh the list each time the activity resumes
    }

    private void displayTasks() {
        taskList.clear();
        taskList.addAll(databaseHelper.getAllTasks());
        taskAdapter.setTasks(taskList);
        taskAdapter.notifyDataSetChanged();
    }

    private void showDeleteConfirmationDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteTask(task);
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    displayTasks();
                    dialog.dismiss();
                });
        deleteConfirmationDialog = builder.create();
        deleteConfirmationDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deleteConfirmationDialog != null && deleteConfirmationDialog.isShowing()) {
            deleteConfirmationDialog.dismiss();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item clicks here
        return true;
    }

    private void deleteTask(Task task) {
        databaseHelper.deleteTask(task.getId());
        displayTasks();
    }


    public void onNotificationButtonClick(View view) {
        // Handle notification button click
        // This is where you can implement logic to set a notification for the task
        // You can use the task ID or any other identifier to determine which task's notification is being set
    }

    public void onCheckButtonClick(View view) {
        // Handle check button click
        // This is where you can implement logic to mark the task as completed
        // You can use the task ID or any other identifier to determine which task is being marked as completed
        Toast.makeText(this, "Task completed successfully!", Toast.LENGTH_SHORT).show();
    }
}
