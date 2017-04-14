package com.example.mymusicplayer;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhao~pc on 2017/3/22.
 */

public class MusicDataUtils {

    public static List<Music> allMusic = new ArrayList<>();
    public static List<Map<String,String>> allMusicMap = new ArrayList<>();
    public static void getAllMusic(Context context) {
        try {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.YEAR,
                            MediaStore.Audio.Media.MIME_TYPE,
                            MediaStore.Audio.Media.SIZE,
                            MediaStore.Audio.Media.ALBUM_ID,
                            MediaStore.Audio.Media.DATA},
                    null, null, null);
            assert cursor != null;
            String name;
            while (cursor.moveToNext()) {
                name = cursor.getString(2);
                if (name.contains("'")){
                    name.replace("'","''");
                }
                Music temp = new Music();
                temp.setID(cursor.getInt(0));
                temp.setTitle(name);
                temp.setDuration(cursor.getInt(3));
                temp.setArtist(cursor.getString(4));
                temp.setData(cursor.getString(10));
                allMusic.add(temp);

                Map<String, String> map = new HashMap<>();
                map.put("name", cursor.getString(2));
                map.put("artist", cursor.getString(4));
                allMusicMap.add(map);
            }
            cursor.close();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
