package com.example.taskaroo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra("task_name");
        String description = intent.getStringExtra("description");

        // Check if the necessary permissions are granted
        if (checkPermissions(context)) {
            // Permissions are granted, proceed to create and show notification
            createNotification(context, taskName, description);
        } else {
            // Permissions are not granted, request them from the user
            // You can implement your own logic to request permissions here
        }
    }

    private boolean checkPermissions(Context context) {
        // Check if the necessary permissions are granted
        // Return true if all permissions are granted, otherwise false
        // You can implement your own logic to check permissions here
        return true; // For simplicity, always return true in this example
    }

    @SuppressLint("MissingPermission")
    private void createNotification(Context context, String taskName, String description) {
        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "taskaroo_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Task Reminder")
                .setContentText("Task: " + taskName + "\nDescription: " + description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}
