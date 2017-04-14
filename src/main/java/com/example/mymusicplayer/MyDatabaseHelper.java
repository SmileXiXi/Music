package com.example.mymusicplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by zhao~pc on 2017/3/26.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    
    public static final String CREATE_RECENT_MUSIC = "create table RecentMusic("
            + "id integer,"
            + "name text,"
            + "artist text,"
            + "data text,"
            + "_id integer primary key)";
    public static final String CREATE_LOVE_MUSIC = "create table LoveMusic("
            + "id integer,"
            + "name text,"
            + "artist text,"
            + "data text,"
            + "_id integer primary key)";
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG,"CREATE_RECENT_MUSIC = " + CREATE_RECENT_MUSIC + "　CREATE_LOVE_MUSIC = " + CREATE_LOVE_MUSIC);
        db.execSQL(CREATE_RECENT_MUSIC);
        Log.d(TAG, "onCreate: CREATE_RECENT_MUSIC");
        db.execSQL(CREATE_LOVE_MUSIC);
    }

    private static final String TAG = "MyDatabaseHelper";
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
