package com.example.mymusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllMusicActivity extends AppCompatActivity implements PlayerLayout.ControlCallBack{
    private PlayerLayout playerLayout;
    private List<Map<String, String>> musicList;
    private List<Music> list;
    private TextView textName, textArtist;
    private ImageView imagePre, imagePlayorPause, imageNext;
    private ProgressBar progressBar;

    private SharedPreferences shp;
    private SharedPreferences.Editor editor;
    private int index;
    private int control;
    private boolean playOrPause;

    private MediaPlayer mediaPlayer;


    public static void actionStart(Context context){
        Intent intent = new Intent(context, AllMusicActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_music);
        ActivityCollector.addActivity(this);

        MyReceiver myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.CHANGE_TEXT);
        registerReceiver(myReceiver,intentFilter);

        MusicDataUtils.getAllMusic(AllMusicActivity.this);
        musicList = MusicDataUtils.allMusicMap;
        list = MusicDataUtils.allMusic;

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //listView加载歌曲数据
        playerLayout = (PlayerLayout) findViewById(R.id.player_layout_all_music);
        playerLayout.setControlCallBack(this);
        initPlayerLayout();
        ListView listView = (ListView) findViewById(R.id.all_music_list_view);
        SimpleAdapter adapter = new SimpleAdapter(this, musicList,
                R.layout.all_music_list_item, new String[]{"name", "artist"},
                new int[]{R.id.music_name, R.id.music_artist});
        listView.setAdapter(adapter);

        ((TransferPlayerLayout)getApplication()).setPlayerLayout(list);

        //listView设置item点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music = list.get(position);

                textName.setText(music.getTitle());
                textArtist.setText(music.getArtist());
                imagePlayorPause.setImageResource(R.drawable.player_btn_kai);
                shp = getSharedPreferences("data", MODE_PRIVATE);
                editor = shp.edit();
                editor.putBoolean("playorpause", true);
                editor.putInt("y", 1);
                editor.commit();
                y = 1;

                Intent intent= new Intent(MusicService.MEDIA_ACTION);
                intent.putExtra("index",position);
                intent.putExtra("playorpause", true);
                intent.putExtra("control", 1);
                sendBroadcast(intent);
            }
        });

        //开启服务 播放音乐
        Intent serviceIntent = new Intent(AllMusicActivity.this, MusicService.class);
        startService(serviceIntent);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MyDBManage myDBManage = new MyDBManage(AllMusicActivity.this, "MusicStore.db");
                myDBManage.addData("LoveMusic", list.get(position));
                Toast.makeText(AllMusicActivity.this, "收藏成功",Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }



    //PlayerLayout内部控件 初始化
    public void initPlayerLayout(){
        textName = (TextView) playerLayout.findViewById(R.id.player_music_name);
        textArtist = (TextView) playerLayout.findViewById(R.id.player_music_artist);
        progressBar = (ProgressBar) playerLayout.findViewById(R.id.progress_bar);
        imagePre = (ImageView) playerLayout.findViewById(R.id.player_btn_shang);
        imagePlayorPause = (ImageView) playerLayout.findViewById(R.id.player_btn_pauseorplay);
        imageNext = (ImageView) playerLayout.findViewById(R.id.player_btn_xia);
    }

    private static final String TAG = "AllMusicActivity";


    @Override
    protected void onResume() {
        super.onResume();
        shp = getSharedPreferences("data", MODE_PRIVATE);
        playOrPause = shp.getBoolean("playorpause", false);
        index = shp.getInt("index",0);
        control = shp.getInt("control",1);
        Log.d("MainActivity", "onResume: index ="+index+";;;;;;playOrPause = " + playOrPause);
        textName.setText(list.get(index).getTitle());
        textArtist.setText(list.get(index).getArtist());
        if (playOrPause){
            imagePlayorPause.setImageResource(R.drawable.player_btn_kai);
        }else {
            imagePlayorPause.setImageResource(R.drawable.player_btn_ting);
        }
        if (mediaPlayer != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(600);
                            int max = shp.getInt("max", 0);
                            int now = shp.getInt("now", 0);
                            progressBar.setMax(max);
                            progressBar.setProgress(now);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    Intent intent = new Intent(MusicService.MEDIA_ACTION);
    @Override
    public void next() {
        shp = getSharedPreferences("data", MODE_PRIVATE);
        playOrPause = shp.getBoolean("playorpause", false);
        index = shp.getInt("index",0);
        control = shp.getInt("control",1);
        imagePlayorPause.setImageResource(R.drawable.player_btn_kai);
        if (index == list.size()-1){
            index = 0;
        }else {
            index = index + 1;
        }
        intent.putExtra("index", index);
        intent.putExtra("control", 1);
        textName.setText(list.get(index).getTitle());
        textArtist.setText(list.get(index).getArtist());
        sendBroadcast(intent);
    }

    @Override
    public void pre() {
        shp = getSharedPreferences("data", MODE_PRIVATE);
        playOrPause = shp.getBoolean("playorpause", false);
        index = shp.getInt("index",0);
        control = shp.getInt("control",1);
        imagePlayorPause.setImageResource(R.drawable.player_btn_kai);
        if (index == 0){
            index = list.size()-1;
        }else {index = index - 1;}
        intent.putExtra("index", index);
        intent.putExtra("control", 1);
        textName.setText(list.get(index).getTitle());
        textArtist.setText(list.get(index).getArtist());
        sendBroadcast(intent);
    }
    int y = 0,x;
    @Override
    public void pauseOrPlay() {
        shp = getSharedPreferences("data", MODE_PRIVATE);
        playOrPause = shp.getBoolean("playorpause", false);
        index = shp.getInt("index",0);
        control = shp.getInt("control",1);
        x = shp.getInt("x", 0);
        if (y == 0 && x == 0){
            imagePlayorPause.setImageResource(R.drawable.player_btn_kai);
            intent.putExtra("index", index);
            intent.putExtra("control", 1);
            intent.putExtra("playorpause", true);
            sendBroadcast(intent);
            shp = getSharedPreferences("data", MODE_PRIVATE);
            editor = shp.edit();
            editor.putInt("y", 1);
            editor.commit();
            y = 1;
        }else {
            if (playOrPause){
                imagePlayorPause.setImageResource(R.drawable.player_btn_ting);
                intent.putExtra("playorpause", false);
                intent.putExtra("control", 2);
                intent.putExtra("index", index);
                sendBroadcast(intent);
            }else {
                imagePlayorPause.setImageResource(R.drawable.player_btn_kai);
                intent.putExtra("playorpause", true);
                intent.putExtra("control", 3);
                intent.putExtra("index", index);
                Log.d("MainActivity", "pauseOrPlay:   ");
                sendBroadcast(intent);
            }
        }

    }

    /**
     * 广播接收器
     */
    private int count;
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            count = intent.getIntExtra("count", index);
            textName.setText(list.get(count).getTitle());
            textArtist.setText(list.get(count).getArtist());
        }
    }
}
