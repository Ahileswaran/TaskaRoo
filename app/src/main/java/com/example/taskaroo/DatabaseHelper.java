package com.example.taskaroo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskDB";
    public static final String TABLE_NAME = "tasks";
    public static final String COL_ID = "id";
    public static final String COL_TASK_NAME = "task_name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE = "date";
    public static final String COL_TIME = "time";

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
                COL_TIME + " TEXT)";
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



}
