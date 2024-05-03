package com.example.taskaroo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class BackupRestoreActivity extends AppCompatActivity {

    private Button buttonBackupToGoogle;
    private Button buttonBackupToStorage;
    private Button buttonRestoreFromStorage;
    private Button buttonRestoreFromGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_activity); // Set the content view to backup_activity.xml

        // Initialize the backup and restore buttons
        buttonBackupToGoogle = findViewById(R.id.buttonBackupToGoogle);
        buttonBackupToStorage = findViewById(R.id.buttonBackupToStorage);
        buttonRestoreFromStorage = findViewById(R.id.buttonRestoreFromStorage);
        buttonRestoreFromGoogle = findViewById(R.id.buttonRestoreFromGoogle);

        // Set up listeners for the buttons
        buttonBackupToGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Backup to Google Drive
            }
        });

        buttonBackupToStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Backup to Storage
            }
        });

        buttonRestoreFromStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Restore from Storage
            }
        });

        buttonRestoreFromGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Restore from Google Drive
            }
        });
    }
}
