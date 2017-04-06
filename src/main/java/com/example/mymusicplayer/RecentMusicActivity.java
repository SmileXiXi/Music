package com.example.mymusicplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecentMusicActivity extends AppCompatActivity {

    private ListView listView;
    private List<Map<String,String>> listMap = new ArrayList();
    private SimpleAdapter adapter;
    public static void actionStart(Context context){
        Intent intent = new Intent(context, RecentMusicActivity.class);
        context.startActivity(intent);}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_music);
        ActivityCollector.addActivity(this);

        listView = (ListView) findViewById(R.id.recent_music_list_view);
        MyDBManage myDBManage = new MyDBManage(RecentMusicActivity.this, "MusicStore.db");
        Cursor cursor = myDBManage.getCursor("RecentMusic");
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            map.put("name", name);
            map.put("artist", artist);
            listMap.add(map);}
        adapter = new SimpleAdapter(this, listMap,
                R.layout.all_music_list_item, new String[]{"name", "artist"},
                new int[]{R.id.music_name, R.id.music_artist});
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MyDBManage myDBManage = new MyDBManage(RecentMusicActivity.this, "MusicStore.db");
                myDBManage.delete("RecentMusic", listMap.get(position).get("name").toString());
                listMap.removeAll(listMap);

                Cursor cursor = myDBManage.getCursor("RecentMusic");
                while (cursor.moveToNext()) {
                    Map<String, String> map = new HashMap<>();
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String artist = cursor.getString(cursor.getColumnIndex("artist"));
                    map.put("name", name);
                    map.put("artist", artist);
                    listMap.add(map);}
                //listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                Toast.makeText(RecentMusicActivity.this, "删除成功",Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
