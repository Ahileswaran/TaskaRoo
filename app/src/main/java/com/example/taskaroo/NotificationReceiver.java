package com.example.taskaroo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

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
            // Permissions are not granted, inform the user
            Toast.makeText(context, "Permissions need to be granted to show notifications", Toast.LENGTH_SHORT).show();
            // You can implement your own logic to request permissions here
        }
    }


    private boolean checkPermissions(Context context) {
        // Check if the necessary permission for showing notifications is granted
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }


    @SuppressLint("MissingPermission")
    private void createNotification(Context context, String taskName, String description) {
        // Intent to start MainActivity when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "taskaroo_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Task Reminder")
                .setContentText("Task: " + taskName + "\nDescription: " + description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)  // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true); // Automatically removes the notification when it is tapped

        // Set notification sound
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(notificationSound);

        // Set vibration pattern
        long[] vibrationPattern = {0, 1000, 500, 1000}; // Vibrate for 1s, wait for 0.5s, vibrate for 1s
        builder.setVibrate(vibrationPattern);

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}
