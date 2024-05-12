package com.example.taskaroo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // Initialize notification channel
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Taskaroo Channel";
            String description = "Channel for Taskaroo notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("taskaroo_channel", name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayTasks();  // Refresh the list each time the activity resumes
    }

    private void displayTasks() {
        taskList.clear();
        taskList.addAll(databaseHelper.getAllTasks());

        // Iterate through each task and retrieve completion status from the database
        for (Task task : taskList) {
            // Retrieve completion status from the database based on task ID
            Task dbTask = databaseHelper.getTaskById(task.getId());
            if (dbTask != null) {
                // Update completion status in the task object
                task.setCompleted(dbTask.isCompleted());
            }
        }

        // Sort tasks based on due date
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                // Parse due dates of tasks
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                try {
                    Date dueDate1 = sdf.parse(task1.getDate());
                    Date dueDate2 = sdf.parse(task2.getDate());
                    // Compare due dates
                    return dueDate1.compareTo(dueDate2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

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
        }else if(id == R.id.nav_export_pdf){
            startActivity((new Intent(this, ExportTaskPDFActivity.class)));
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
