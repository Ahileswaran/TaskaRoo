package com.example.taskaroo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TaskDB";
    public static final String TABLE_NAME = "tasks";
    public static final String COL_ID = "id";
    public static final String COL_TASK_NAME = "task_name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE = "date";
    public static final String COL_TIME = "time";
    public static final String COL_NUMBER_OF_NOTIFICATIONS = "number_of_notifications";
    public static final String COL_COMPLETED = "completed";
    private static final String COL_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public static String getCustomDatabaseName() {
        return DATABASE_NAME;
    }

    public static String getCustomDatabaseID(int taskId) {
        return COL_ID;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TASK_NAME + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_TIME + " TEXT, " +
                COL_NUMBER_OF_NOTIFICATIONS + " INTEGER, " +
                COL_COMPLETED + " INTEGER, " +
                COL_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addTask(String taskName, String description, String date, String time, int numberOfNotifications, int completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_NAME, taskName);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_DATE, date);
        values.put(COL_TIME, time);
        values.put(COL_NUMBER_OF_NOTIFICATIONS, numberOfNotifications);
        values.put(COL_COMPLETED, completed);
        return db.insert(TABLE_NAME, null, values);
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
                task.setNumberOfNotifications(cursor.getInt(cursor.getColumnIndex(COL_NUMBER_OF_NOTIFICATIONS)));
                task.setCompleted(cursor.getInt(cursor.getColumnIndex(COL_COMPLETED)) == 1);
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_NAME, task.getName());
        values.put(COL_DESCRIPTION, task.getDescription());
        values.put(COL_DATE, task.getDate());
        values.put(COL_TIME, task.getTime());
        values.put(COL_NUMBER_OF_NOTIFICATIONS, task.getNumberOfNotifications());
        values.put(COL_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COL_TIMESTAMP, getDateTime());
        return db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(task.getId())});
    }

    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    private String getDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    @SuppressLint("Range")
    public Task getTaskById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Task task = null;
        Cursor cursor = db.query(TABLE_NAME, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            task = new Task();
            task.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
            task.setName(cursor.getString(cursor.getColumnIndex(COL_TASK_NAME)));
            task.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));
            task.setDate(cursor.getString(cursor.getColumnIndex(COL_DATE)));
            task.setTime(cursor.getString(cursor.getColumnIndex(COL_TIME)));
            task.setNumberOfNotifications(cursor.getInt(cursor.getColumnIndex(COL_NUMBER_OF_NOTIFICATIONS)));
            task.setCompleted(cursor.getInt(cursor.getColumnIndex(COL_COMPLETED)) == 1);
            cursor.close();
        }
        return task;
    }

    public int updateTaskCompletionStatus(int taskId, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMPLETED, completed ? 1 : 0);
        if (completed) {
            values.put(COL_TIMESTAMP, getDateTime());
        }
        int rowsAffected = db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rowsAffected;
    }

    public String getCompleteMessage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_DATE + ", " + COL_TIME + ", " + COL_COMPLETED +
                " FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor != null && cursor.moveToFirst()) {
            int dateIndex = cursor.getColumnIndex(COL_DATE);
            int timeIndex = cursor.getColumnIndex(COL_TIME);
            int completedIndex = cursor.getColumnIndex(COL_COMPLETED);

            if (dateIndex != -1 && timeIndex != -1 && completedIndex != -1) {
                boolean completed = cursor.getInt(completedIndex) == 1;
                String dueDate = cursor.getString(dateIndex);
                String dueTime = cursor.getString(timeIndex);

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
        }

        if (cursor != null) {
            cursor.close();
        }
        return "Information unavailable.";
    }

    //Method to get the last added task from the database
    @SuppressLint("Range")
    public Task getLastAddedTask() {
        SQLiteDatabase db = this.getReadableDatabase();
        Task task = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            task = new Task();
            task.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
            task.setName(cursor.getString(cursor.getColumnIndex(COL_TASK_NAME)));
            task.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));
            task.setDate(cursor.getString(cursor.getColumnIndex(COL_DATE)));
            task.setTime(cursor.getString(cursor.getColumnIndex(COL_TIME)));
            task.setNumberOfNotifications(cursor.getInt(cursor.getColumnIndex(COL_NUMBER_OF_NOTIFICATIONS)));
            task.setCompleted(cursor.getInt(cursor.getColumnIndex(COL_COMPLETED)) == 1);
            cursor.close();
        }
        return task;
    }

}
