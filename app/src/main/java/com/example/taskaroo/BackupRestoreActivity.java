package com.example.taskaroo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BackupRestoreActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_BACKUP_FILE = 2;
    private static final int REQUEST_CODE_RESTORE_FILE = 3;

    private ActivityResultLauncher<Intent> backupLauncher;
    private ActivityResultLauncher<Intent> restoreLauncher;

    private int requestedAction = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_activity);

        Button buttonBackupToStorage = findViewById(R.id.buttonBackupToStorage);
        Button buttonRestoreFromStorage = findViewById(R.id.buttonRestoreFromStorage);

        backupLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        performBackup(result.getData().getData());
                    }
                });

        restoreLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        performRestore(result.getData().getData());
                    }
                });

        buttonBackupToStorage.setOnClickListener(v -> {
            requestedAction = REQUEST_CODE_BACKUP_FILE;
            checkAndRequestStoragePermission();
        });
        buttonRestoreFromStorage.setOnClickListener(v -> {
            requestedAction = REQUEST_CODE_RESTORE_FILE;
            checkAndRequestStoragePermission();
        });
    }

    private void checkAndRequestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            executeRequestedAction();
        }
    }

    private void executeRequestedAction() {
        if (requestedAction == REQUEST_CODE_BACKUP_FILE) {
            backupToStorage();
        } else if (requestedAction == REQUEST_CODE_RESTORE_FILE) {
            restoreFromStorage();
        }
    }

    private void backupToStorage() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "taskaroo_backup.db");
        backupLauncher.launch(intent);
    }

    private void restoreFromStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        restoreLauncher.launch(intent);
    }

    private void performBackup(Uri backupUri) {
        try {
            ContentResolver resolver = getContentResolver();
            Path dbPath = Paths.get(getDatabasePath(DatabaseHelper.getCustomDatabaseName()).getPath());
            InputStream inputStream = Files.newInputStream(dbPath);

            // Log database schema info
            logDatabaseInfo();

            OutputStream outputStream = resolver.openOutputStream(backupUri);
            if (outputStream == null) {
                throw new IOException("Output stream is null");
            }

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
            if (inputStream == null) {
                throw new IOException("Input stream is null");
            }
            Path dbPath = Paths.get(getDatabasePath(DatabaseHelper.getCustomDatabaseName()).getPath());
            OutputStream outputStream = Files.newOutputStream(dbPath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();

            Toast.makeText(this, "Restore completed", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error restoring database", Toast.LENGTH_SHORT).show();
        }
    }

    private void logDatabaseInfo() {
        try (DatabaseHelper dbHelper = new DatabaseHelper(this);
             SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery("PRAGMA table_info(your_table_name)", null)) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("type"));
                Log.d("DB_INFO", "Column name: " + name + " Type: " + type);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            executeRequestedAction();
        }
    }
}
