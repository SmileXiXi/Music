package com.example.mymusicplayer;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        Log.d(TAG, "addData: add" + music.getID());
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
        String sql = "insert OR IGNORE INTO "+table+" ('name', 'artist', 'data', '_id') VALUES ('"
                + music.getTitle() + "', '"
                + music.getArtist() + "', '"
                + music.getData() + "', "
                + music.getID() + ")";
        db.execSQL(sql);
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
    public void removeAll(String table){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from " + table);
    }
}
