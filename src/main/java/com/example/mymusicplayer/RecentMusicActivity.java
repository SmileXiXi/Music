package com.example.mymusicplayer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
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

    private List<Music> list;
    private SharedPreferences shp;
    private SharedPreferences.Editor editor;

    private ImageView backImage;
    private Button emptyButton;
    public static void actionStart(Context context){
        Intent intent = new Intent(context, RecentMusicActivity.class);
        context.startActivity(intent);}

    private static final String TAG = "RecentMusicActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_music);
        ActivityCollector.addActivity(this);

        setRecentTitle();

        MusicDataUtils.getAllMusic(RecentMusicActivity.this);
        list = MusicDataUtils.allMusic;

        Intent service = new Intent(RecentMusicActivity.this, MusicService.class);
        bindService(service, connection, BIND_AUTO_CREATE);

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i<list.size() ; i++) {
                    if (listMap.get(position).get("name").equals(list.get(i).getTitle())){
                        Log.d(TAG, "onItemClick:  onItewClick   for");
                        shp = getSharedPreferences("data", MODE_PRIVATE);
                        editor = shp.edit();
                        editor.putInt("index", i);
                        editor.putBoolean("isPlaying", true);
                        editor.commit();
                    }
                }
                myBinder.play();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Map<String, String> dialogMap = listMap.get(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(RecentMusicActivity.this);
                dialog.setTitle("删除");
                dialog.setMessage("是否删除？");
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        MyDBManage myDBManage = new MyDBManage(RecentMusicActivity.this, "MusicStore.db");
                        myDBManage.delete("RecentMusic", dialogMap.get("name").toString());
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
                    }
                });
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                dialog.show();
                return true;
            }
        });
    }
    private void setRecentTitle(){
        backImage = (ImageView) findViewById(R.id.back);
        emptyButton = (Button) findViewById(R.id.empty);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(RecentMusicActivity.this);
                dialog.setTitle("清空");
                dialog.setMessage("是否清空？");
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyDBManage myDBManage = new MyDBManage(RecentMusicActivity.this, "MusicStore.db");
                        myDBManage.removeAll("RecentMusic");
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
                        Toast.makeText(RecentMusicActivity.this, "清空成功",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                dialog.show();
            }
        });
    }
    private MusicService.MyBinder myBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MusicService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        ActivityCollector.removeActivity(this);
    }
}
