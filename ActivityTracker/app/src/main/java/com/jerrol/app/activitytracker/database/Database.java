package com.jerrol.app.activitytracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Jerrol on 5/7/2017.
 */

public class Database extends SQLiteOpenHelper implements BaseColumns {

    private static final String DATABASE_NAME = "Database.db";
    private static final int DATABASE_VERSION = 1;

    public interface TABLE {
        String PROJECT = "TBL_PROJECT";
        String TASKS_LIST = "TBL_TASKS_LIST";
        String TASKS_ITEM = "TBL_TASKS_ITEM";
    }

    public interface TBL_PROJECT {
        String TITLE = "PROJECT_TITLE";
        String DESCRIPTION = "PROJECT_DESC";
    }
    public interface TBL_TASK_LIST {
        String PROJECT_ID = "PROJECT_ID";
        String NAME = "TASK_LIST_NAME";
    }

    public interface TBL_TASK_ITEM {
        String TASK_ID = "TASK_ID";
        String NAME = "TASK_ITEM_NAME";
        String DESCRIPTION = "DESCRIPTION";
        String DUE_DATE = "TASK_DUE_DATE";
    }

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE.PROJECT + "("
                + Database._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TBL_PROJECT.TITLE + " TEXT,"
                + TBL_PROJECT.DESCRIPTION + " TEXT)"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE.TASKS_LIST + "("
                + Database._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TBL_TASK_LIST.NAME + " TEXT,"
                + TBL_TASK_LIST.PROJECT_ID + " TEXT)"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE.TASKS_ITEM + "("
                + Database._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TBL_TASK_ITEM.TASK_ID + " TEXT,"
                + TBL_TASK_ITEM.NAME + " TEXT,"
                + TBL_TASK_ITEM.DESCRIPTION + " TEXT,"
                + TBL_TASK_ITEM.DUE_DATE + " TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DELETE TABLE IF EXISTS " + TABLE.PROJECT);
        db.execSQL("DELETE TABLE IF EXISTS " + TABLE.TASKS_LIST);
        db.execSQL("DELETE TABLE IF EXISTS " + TABLE.TASKS_ITEM);
    }

    private SQLiteDatabase dbWrite() { return this.getWritableDatabase(); }
    private SQLiteDatabase dbRead() { return this.getReadableDatabase(); }

    public long insertProject(String title, String description) {
        ContentValues values = new ContentValues();
        values.put(TBL_PROJECT.TITLE, title);
        values.put(TBL_PROJECT.DESCRIPTION, description);

        return dbWrite().insert(TABLE.PROJECT, null, values);
    }

    public long insertTaskList(String projectId, String title) {
        ContentValues values = new ContentValues();
        values.put(TBL_TASK_LIST.PROJECT_ID, projectId);
        values.put(TBL_TASK_LIST.NAME, title);

        return dbWrite().insert(TABLE.TASKS_LIST, null, values);
    }

    public long insertTaskListItem(String taskId, String name) {
        ContentValues values = new ContentValues();
        values.put(TBL_TASK_ITEM.TASK_ID, taskId);
        values.put(TBL_TASK_ITEM.NAME, name);

        return dbWrite().insert(TABLE.TASKS_ITEM, null, values);
    }

    public Cursor getProjectList() {
        Cursor cursor = dbRead().query(TABLE.PROJECT, null, null, null, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getTaskList(String projectId) {
        Cursor cursor = dbRead().query(TABLE.TASKS_LIST, null, TBL_TASK_LIST.PROJECT_ID + " = ?", new String[] {projectId}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getTaskListItem(String taskId) {
        Cursor cursor = dbRead().query(TABLE.TASKS_ITEM, null, TBL_TASK_ITEM.TASK_ID + " = ?", new String[] {taskId}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }
}
