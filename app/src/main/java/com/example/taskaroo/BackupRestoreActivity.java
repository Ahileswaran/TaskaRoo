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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private static final int REQUEST_CODE_BACKUP_FILE = 2;
    private static final int REQUEST_CODE_RESTORE_FILE = 3;

    private Button buttonBackupToStorage;
    private Button buttonRestoreFromStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_activity);

        buttonBackupToStorage = findViewById(R.id.buttonBackupToStorage);
        buttonRestoreFromStorage = findViewById(R.id.buttonRestoreFromStorage);

        buttonBackupToStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestStoragePermission(REQUEST_CODE_BACKUP_FILE);
            }
        });

        buttonRestoreFromStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestStoragePermission(REQUEST_CODE_RESTORE_FILE);
            }
        });
    }

    private void checkAndRequestStoragePermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            if (requestCode == REQUEST_CODE_BACKUP_FILE) {
                backupToStorage();
            } else if (requestCode == REQUEST_CODE_RESTORE_FILE) {
                restoreFromStorage();
            }
        }
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

    private void performBackup(Uri backupUri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = new FileInputStream(getDatabasePath(DatabaseHelper.getCustomDatabaseName()));

            // Log database schema info
            logDatabaseInfo();

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
            OutputStream outputStream = new FileOutputStream(getDatabasePath(DatabaseHelper.getCustomDatabaseName()));

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
        SQLiteDatabase db = new DatabaseHelper(this).getReadableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA table_info(your_table_name)", null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("type"));
            Log.d("DB_INFO", "Column name: " + name + " Type: " + type);
        }
        cursor.close();
        db.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CODE_BACKUP_FILE) {
                backupToStorage();
            } else if (requestCode == REQUEST_CODE_RESTORE_FILE) {
                restoreFromStorage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == REQUEST_CODE_BACKUP_FILE) {
                performBackup(uri);
            } else if (requestCode == REQUEST_CODE_RESTORE_FILE) {
                performRestore(uri);
            }
        }
    }
}
