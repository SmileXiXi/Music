package com.example.mymusicplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
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

public class PlayActivity extends Activity implements View.OnClickListener{
    private TextView textName, textArtist, progressTime, maxTime;
    private ImageView imagePre, imagePlayorPause, imageNext, back, collext;
    private SeekBar seekBar;
    private SharedPreferences shp;
    private int index, max, now;
    private boolean isPlaying;
    private List<Music> list;
    private MusicService.MyBinder myBinder;
    private static final String TAG = "PlayActivity";
    public static final int UPDATE_TIME = 1;
    public static void startAction(Context context) {
        Intent intent = new Intent(context, PlayActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ActivityCollector.addActivity(this);
        Log.d(TAG, "onCreate: ");
        Intent service = new Intent(PlayActivity.this, MusicService.class);
        bindService(service, connection, BIND_AUTO_CREATE);

        MusicDataUtils.getAllMusic(PlayActivity.this);
        list = MusicDataUtils.allMusic;
        initControl();
        setSeek();
        setPlayerControl();
        setControlClickListener();
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
    @Override
    protected void onDestroy() {
        unbindService(connection);

        super.onDestroy();
        ActivityCollector.removeActivity(this);
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
        setPlayerControl();
        openThread(true);
        super.onResume();
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

    //初始化控件
    public void initControl(){
        Log.d(TAG, "initControl: ");
        textArtist = (TextView) findViewById(R.id.play_music_artist);
        textName = (TextView) findViewById(R.id.play_music_name);
        back = (ImageView) findViewById(R.id.back);
        collext = (ImageView) findViewById(R.id.collext);
        imagePre = (ImageView) findViewById(R.id.play_pre);
        imagePlayorPause = (ImageView) findViewById(R.id.play_playorpause);
        imageNext = (ImageView) findViewById(R.id.play_next);
        seekBar = (SeekBar) findViewById(R.id.progress_bar);
        progressTime = (TextView) findViewById(R.id.progress_time);
        maxTime = (TextView) findViewById(R.id.max_time);
    }
    private void setPlayerControl(){
        Log.d(TAG, "setPlayerControl: ");
        shp = getSharedPreferences("data", MODE_PRIVATE);
        index = shp.getInt("index", 0);
        isPlaying = shp.getBoolean("isPlaying", false);
        textName.setText(list.get(index).getTitle());
        textArtist.setText(list.get(index).getArtist());
        if (isPlaying){
            imagePlayorPause.setImageResource(R.drawable.player_play);
        }else {
            imagePlayorPause.setImageResource(R.drawable.player_pause);
        }
    }
    private void setControlClickListener(){
        Log.d(TAG, "setControlClickListener: ");
        back.setOnClickListener(this);
        collext.setOnClickListener(this);
        imagePre.setOnClickListener(this);
        imagePlayorPause.setOnClickListener(this);
        imageNext.setOnClickListener(this);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MusicService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.collext:
                shp = getSharedPreferences("data",MODE_PRIVATE);
                index = shp.getInt("index", 0);
                AlertDialog.Builder dialog = new AlertDialog.Builder(PlayActivity.this);
                dialog.setTitle("收藏");
                dialog.setMessage("是否收藏？");
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyDBManage myDBManage = new MyDBManage(PlayActivity.this, "MusicStore.db");
                        myDBManage.addData("LoveMusic", list.get(index));
                        Toast.makeText(PlayActivity.this, "收藏成功",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                dialog.show();
                break;
            case R.id.play_pre:
                myBinder.pre();
                setPlayerControl();
                shp = getSharedPreferences("data", MODE_PRIVATE);
                if (shp.getBoolean("isFirstClick", true)){openThread(getBoolean());}
                break;
            case R.id.play_playorpause:
                shp = getSharedPreferences("data", MODE_PRIVATE);
                if (shp.getBoolean("isFirstClick", true)){openThread(!getBoolean());}
                myBinder.playOrPause();
                setPlayerControl();
                break;
            case R.id.play_next:
                myBinder.next();
                setPlayerControl();
                shp = getSharedPreferences("data", MODE_PRIVATE);
                if (shp.getBoolean("isFirstClick", true)){openThread(getBoolean());}
        }
    }
}
