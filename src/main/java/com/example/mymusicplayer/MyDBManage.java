package com.example.mymusicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Map;

/**
 * Created by zhao~pc on 2017/3/26.
 */

public class MyDBManage {
    private MyDatabaseHelper dbHelper;

    public MyDBManage(Context context, String dbName){
        dbHelper = new MyDatabaseHelper(context, dbName, null,1);
        dbHelper.getWritableDatabase();
    }

    private static final String TAG = "MyDBManage";
    public void addData(String table, Music music){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        Log.d(TAG, "addData:   name" + values.get("name"));
//        values.put("name", music.getTitle());
//        values.put("artist", music.getArtist());
//        values.put("data", music.getData());
//            //values.put("_id", music.getID());
//        db.replace(table, null, values);
//        String sql = "replace into "+table+" values ("+music.getTitle()+", "
//                +music.getArtist()+", c"+music.getData()+")";
//        Log.d(TAG,"addData sql = " + sql);
//        db.execSQL(sql);
        db.execSQL("INSERT INTO "+table+" VALUES ("
                + null + ", '"
                + music.getTitle() + "', '"
                + music.getArtist() + "', '"
                + music.getData() + "', "
                + music.getID() + ")"
        );
    }
    public void delete(String table, String name){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(table, "name = ?", new String[]{name});
    }
    public Cursor getCursor(String table){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(table, null, null, null, null, null,null);
        return cursor;
    }
}
