package com.example.taskaroo;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;
    private List<Task> taskList;
    private AlertDialog deleteConfirmationDialog;
    private DrawerLayout drawerLayout;

    // Define notification ID constant
    private static final int NOTIFICATION_ID = 1001;
    // Define permission request code
    private static final int REQUEST_CODE_PERMISSION_NOTIFICATION = 101;

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

        // Check and request notification permission
        enableNotification();
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
        int id = item.getItemId();

        if (id == R.id.nav_backup_restore) {
            startActivity(new Intent(this, BackupRestoreActivity.class));
        } else if (id == R.id.nav_select_theme) {
            toggleTheme();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void deleteTask(Task task) {
        databaseHelper.deleteTask(task.getId());
        displayTasks();
    }

    public void onCheckButtonClick(View view) {
        Toast.makeText(this, "Task completed successfully!", Toast.LENGTH_SHORT).show();
    }

    private void deleteTask(int taskId) {
        databaseHelper.deleteTask(taskId);
        displayTasks();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createNotification();
            } else {
                showNotificationPermissionDialog();
            }
        }
    }

    private void enableNotification() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_PERMISSION_NOTIFICATION);
        } else {
            createNotification();
        }
    }

    private void createNotification() {
        // Implementation of creating notifications...
    }

    private void showNotificationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notification Permission Required")
                .setMessage("To enable notifications, please grant the notification permission in the app settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    navigateToAppSettings();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void navigateToAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void createNotification(Task task) {
        Date currentDate = new Date();
        String taskDateTimeString = task.getDate() + " " + task.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date taskDateTime = dateFormat.parse(taskDateTimeString);
            if (currentDate.equals(taskDateTime)) {
                String notificationTitle = "Task Reminder: " + task.getName();
                String notificationContent = "Date: " + task.getDate() + "\nTime: " + task.getTime();

                // Create the intent to open the MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Build the notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "task_channel")
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationContent)
                        .setSmallIcon(android.R.drawable.ic_dialog_info) // Use a traditional notification icon
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                // Show the notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void onCheckClicked(View view) {
        int taskId = (int) view.getTag();
        Toast.makeText(this, "Task completed successfully!", Toast.LENGTH_SHORT).show();
        deleteTask(taskId);
    }

    private void toggleTheme() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                recreate();
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                recreate();
                break;
            default:
                break;
        }
    }
}
