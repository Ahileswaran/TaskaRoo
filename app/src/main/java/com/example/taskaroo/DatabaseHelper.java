package com.example.taskaroo;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskDB";
    private static final String TABLE_NAME = "tasks";
    private static final String COL_ID = "id";
    private static final String COL_TASK_NAME = "task_name";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DATE = "date";
    private static final String COL_TIME = "time";

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

    public long addTask(String taskName, String description, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TASK_NAME, taskName);
        contentValues.put(COL_DESCRIPTION, description);
        contentValues.put(COL_DATE, date);
        contentValues.put(COL_TIME, time);
        return db.insert(TABLE_NAME, null, contentValues);
    }
}
