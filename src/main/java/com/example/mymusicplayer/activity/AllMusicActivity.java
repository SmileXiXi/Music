package com.example.mymusicplayer.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusicplayer.ActivityCollector;
import com.example.mymusicplayer.localmusic.Music;
import com.example.mymusicplayer.localmusic.MusicDataUtils;
import com.example.mymusicplayer.MusicService;
import com.example.mymusicplayer.database.MyDBManage;
import com.example.mymusicplayer.PlayerLayout;
import com.example.mymusicplayer.R;

import java.util.List;
import java.util.Map;

public class AllMusicActivity extends AppCompatActivity implements PlayerLayout.ControlCallBack {
    private PlayerLayout playerLayout;
    private List<Map<String, String>> musicList;
    private List<Music> list;
    private TextView textName, textArtist, progressTime, maxTime;
    private ImageView imagePre, imagePlayorPause, imageNext, backImage;
    private SeekBar seekBar;
    private MyReceiver myReceiver;
    private SharedPreferences shp;
    private SharedPreferences.Editor editor;
    private int index, max, now;
    private MusicService.MyBinder myBinder;
    private static final String TAG = "AllMusicActivity";
    public static final int UPDATE_TIME = 1;
    boolean isPlaying;
    public static void actionStart(Context context){
        Intent intent = new Intent(context, AllMusicActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        Log.d(TAG, "onDestroy:  onDestroy");
        unbindService(connection);
        ActivityCollector.removeActivity(this);
        super.onDestroy();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_music);
        ActivityCollector.addActivity(this);

        Intent service = new Intent(AllMusicActivity.this, MusicService.class);
        bindService(service, connection, BIND_AUTO_CREATE);

        myReceiver = new MyReceiver();
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
        playerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayActivity.startAction(AllMusicActivity.this);
            }
        });
        initPlayerLayout();
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSeek();
        ListView listView = (ListView) findViewById(R.id.all_music_list_view);
        SimpleAdapter adapter = new SimpleAdapter(this, musicList,
                R.layout.all_music_list_item, new String[]{"name", "artist"},
                new int[]{R.id.music_name, R.id.music_artist});
        listView.setAdapter(adapter);

        //listView设置item点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: onItemClick");
                shp = getSharedPreferences("data", MODE_PRIVATE);
                editor = shp.edit();
                editor.putInt("index", position);
                editor.putBoolean("isPlaying", true);
                editor.commit();
                myBinder.play();
                setPlayerControl();
            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Music dialogMusic = list.get(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(AllMusicActivity.this);
                dialog.setTitle("收藏");
                dialog.setMessage("是否收藏？");
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyDBManage myDBManage = new MyDBManage(AllMusicActivity.this, "MusicStore.db");
                        myDBManage.addData("LoveMusic", dialogMusic);
                        Toast.makeText(AllMusicActivity.this, "收藏成功",Toast.LENGTH_SHORT).show();
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
    public void setSeek(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myBinder.setSeekTo(seekBar.getProgress());
            }
        });
    }

    //PlayerLayout内部控件 初始化
    public void initPlayerLayout(){
        textName = (TextView) playerLayout.findViewById(R.id.player_music_name);
        textArtist = (TextView) playerLayout.findViewById(R.id.player_music_artist);
        seekBar = (SeekBar) playerLayout.findViewById(R.id.progress_bar);
        imagePre = (ImageView) playerLayout.findViewById(R.id.player_btn_shang);
        imagePlayorPause = (ImageView) playerLayout.findViewById(R.id.player_btn_pauseorplay);
        imageNext = (ImageView) playerLayout.findViewById(R.id.player_btn_xia);
        progressTime = (TextView) playerLayout.findViewById(R.id.progress_time);
        maxTime = (TextView) playerLayout.findViewById(R.id.max_time);
        backImage = (ImageView) findViewById(R.id.back);
    }
    private boolean getBoolean(){
        if (myBinder == null){return false;}
        else {return myBinder.isMusicPlaying();}
    }
    private void openThread(Boolean bool){
        Log.d(TAG, "openThread: openThread" + getBoolean());
        shp = getSharedPreferences("data", MODE_PRIVATE);
        if (bool){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean needRun = true;
                    Log.d(TAG, "run: run");
                    while (needRun) {
                        try {
                            Thread.sleep(1000);
                            max = shp.getInt("max", 0);
                            now = shp.getInt("now", 0);
                            Message message = new Message();
                            message.what = UPDATE_TIME;
                            handler.sendMessage(message);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IllegalStateException e){
                            needRun = false;
                        }
                    }
                }
            }).start();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        setPlayerControl();
        openThread(getBoolean());
    }
    //handler 更新时间；
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case UPDATE_TIME:
                    seekBar.setMax(max);
                    seekBar.setProgress(now);
                    progressTime.setText(PlayerLayout.timeFormat(now));
                    maxTime.setText(PlayerLayout.timeFormat(max));
                    break;
                default:
                    break;
            }
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MusicService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };
    @Override
    public void next() {
        myBinder.next();
        setPlayerControl();
        shp = getSharedPreferences("data", MODE_PRIVATE);
        if (shp.getBoolean("isFirstClick", true)){openThread(getBoolean());}
    }
    @Override
    public void pre() {
        myBinder.pre();
        setPlayerControl();
        shp = getSharedPreferences("data", MODE_PRIVATE);
        if (shp.getBoolean("isFirstClick", true)){openThread(getBoolean());}
    }
    @Override
    public void pauseOrPlay() {
        shp = getSharedPreferences("data", MODE_PRIVATE);
        if (shp.getBoolean("isFirstClick", true)){openThread(!getBoolean());}
        myBinder.playOrPause();
        setPlayerControl();
    }
    //设置player控件

    private void setPlayerControl(){
        shp = getSharedPreferences("data", MODE_PRIVATE);
        index = shp.getInt("index", 0);
        isPlaying = shp.getBoolean("isPlaying", false);
        textName.setText(list.get(index).getTitle());
        textArtist.setText(list.get(index).getArtist());
        if (isPlaying){
            imagePlayorPause.setImageResource(R.drawable.player_btn_kai);
        }else {
            imagePlayorPause.setImageResource(R.drawable.player_btn_ting);
        }
    }

    /**
     * 广播接收器
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setPlayerControl();
        }
    }
}
