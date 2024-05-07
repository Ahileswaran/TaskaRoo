package com.example.taskaroo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TaskDB";
    public static String getCustomDatabaseName() {
        return DATABASE_NAME;
    }
    public static final String TABLE_NAME = "tasks";
    public static final String COL_ID = "id";
    public static String getCustomDatabaseID(int taskId) {
        return COL_ID;
    }
    public static final String COL_TASK_NAME = "task_name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE = "date";
    public static final String COL_TIME = "time";

    public static final String COL_COMPLETED = "completed";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TASK_NAME + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_TIME + " TEXT, " +
                COL_COMPLETED + " INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public long updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_NAME, task.getName());
        values.put(COL_DESCRIPTION, task.getDescription());
        values.put(COL_DATE, task.getDate());
        values.put(COL_TIME, task.getTime());
        // Updating row
        return db.update(TABLE_NAME, values, COL_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
    }
    public long addTask(String taskName, String description, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TASK_NAME, taskName);
        contentValues.put(COL_DESCRIPTION, description);
        contentValues.put(COL_DATE, date);
        contentValues.put(COL_TIME, time);
        return db.insert(TABLE_NAME, null, contentValues);
    }
    @SuppressLint("Range")
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                task.setName(cursor.getString(cursor.getColumnIndex(COL_TASK_NAME)));
                task.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));
                task.setDate(cursor.getString(cursor.getColumnIndex(COL_DATE)));
                task.setTime(cursor.getString(cursor.getColumnIndex(COL_TIME)));
                taskList.add(task);
                Log.d("DatabaseHelper", "Task retrieved: " + task.getName());
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }
    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tasks", "id = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }


    @SuppressLint("Range")
    public Task getTaskById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] {COL_ID, COL_TASK_NAME, COL_DESCRIPTION, COL_DATE, COL_TIME}, COL_ID + " =?", new String[]{String.valueOf(id)}, null, null, null);
        Task task = null;
        if (cursor.moveToFirst()) {
            task = new Task();
            task.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
            task.setName(cursor.getString(cursor.getColumnIndex(COL_TASK_NAME)));
            task.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));
            task.setDate(cursor.getString(cursor.getColumnIndex(COL_DATE)));
            task.setTime(cursor.getString(cursor.getColumnIndex(COL_TIME)));
        }
        cursor.close();
        return task;
    }

    public int updateTaskCompletionStatus(int taskId, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMPLETED, completed ? 1 : 0); // 1 for completed, 0 for not completed
        // Updating row
        return db.update(TABLE_NAME, values, COL_ID + " = ?",
                new String[]{String.valueOf(taskId)});
    }



    public String getCompleteMessage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_DATE + ", " + COL_TIME + ", " + COL_COMPLETED +
                " FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") boolean completed = cursor.getInt(cursor.getColumnIndex(COL_COMPLETED)) == 1;
            @SuppressLint("Range") String dueDate = cursor.getString(cursor.getColumnIndex(COL_DATE));
            @SuppressLint("Range") String dueTime = cursor.getString(cursor.getColumnIndex(COL_TIME));

            // Parse the date and time to check if it is completed before the due date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            try {
                Date dueDateTime = sdf.parse(dueDate + " " + dueTime);
                Date now = new Date();

                if (completed) {
                    if (now.before(dueDateTime)) {
                        cursor.close();
                        return "Task completed on time!";
                    } else {
                        cursor.close();
                        return "Task completed, but was overdue!";
                    }
                } else {
                    cursor.close();
                    return "Task not completed yet.";
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return "Information unavailable.";
    }

}