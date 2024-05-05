package com.example.taskaroo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupRestoreActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_BACKUP_RESTORE = 2;
    private static final int REQUEST_CODE_BACKUP_FILE = 3;
    private static final int REQUEST_CODE_RESTORE_FILE = 4;

    private Button buttonBackupToGoogle;
    private Button buttonBackupToStorage;
    private Button buttonRestoreFromStorage;
    private Button buttonRestoreFromGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Inflate the custom layout for the ActionBar logo
        View actionBarLogo = getLayoutInflater().inflate(R.layout.action_bar_logo, null);

        // Set the custom layout as the logo in the ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(actionBarLogo);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_activity);

        buttonBackupToGoogle = findViewById(R.id.buttonBackupToGoogle);
        buttonBackupToStorage = findViewById(R.id.buttonBackupToStorage);
        buttonRestoreFromStorage = findViewById(R.id.buttonRestoreFromStorage);
        buttonRestoreFromGoogle = findViewById(R.id.buttonRestoreFromGoogle);

        buttonBackupToGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestStoragePermission(REQUEST_CODE_BACKUP_RESTORE);
            }
        });

        buttonBackupToStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupToStorage();
            }
        });

        buttonRestoreFromStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestStoragePermission(REQUEST_CODE_RESTORE_FILE);
            }
        });

        buttonRestoreFromGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreFromGoogleDrive();
            }
        });
    }

    private void checkAndRequestStoragePermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(
                BackupRestoreActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    BackupRestoreActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
        } else {
            if (requestCode == REQUEST_CODE_BACKUP_RESTORE) {
                backupToGoogleDrive();
            } else if (requestCode == REQUEST_CODE_RESTORE_FILE) {
                restoreFromStorage();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                backupToGoogleDrive();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void backupToGoogleDrive() {
        // Implement backup to Google Drive using Google Drive API
        // This might involve authentication and file handling
        // For demonstration purposes, we'll show a toast message
        Toast.makeText(this, "Backup to Google Drive initiated", Toast.LENGTH_SHORT).show();
    }

    private void backupToStorage() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "taskaroo_backup.db");
        startActivityForResult(intent, REQUEST_CODE_BACKUP_FILE);
    }

    private void restoreFromStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_RESTORE_FILE);
    }

    private void restoreFromGoogleDrive() {
        // Implement restore from Google Drive using Google Drive API
        // This might involve authentication and file handling
        // For demonstration purposes, we'll show a toast message
        Toast.makeText(this, "Restore from Google Drive initiated", Toast.LENGTH_SHORT).show();
    }

    private void performBackup(Uri backupUri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = new FileInputStream(getDatabasePath(DatabaseHelper.getCustomDatabaseName()));
            OutputStream outputStream = resolver.openOutputStream(backupUri);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Toast.makeText(this, "Backup completed", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error backing up database", Toast.LENGTH_SHORT).show();
        }
    }

    private void performRestore(Uri restoreUri) {
        ContentResolver resolver = getContentResolver();

        try (InputStream inputStream = resolver.openInputStream(restoreUri)) {
            if (inputStream != null) {
                String currentDBPath = getDatabasePath(DatabaseHelper.getCustomDatabaseName()).getAbsolutePath();

                try (FileOutputStream outputStream = new FileOutputStream(currentDBPath)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    outputStream.flush();
                    outputStream.close();

                    Toast.makeText(this, "File restored", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error restoring file", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_BACKUP_FILE) {
                Uri backupUri = data.getData();
                performBackup(backupUri);
            } else if (requestCode == REQUEST_CODE_RESTORE_FILE) {
                Uri restoreUri = data.getData();
                performRestore(restoreUri);
            }
        }
    }
}
