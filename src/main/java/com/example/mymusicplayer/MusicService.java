package com.example.mymusicplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class MusicService extends Service{
    public static final String MEDIA_ACTION = "org.crazyit.action.MEDIA_ACTION";
    public static final String CHANGE_TEXT ="com.example.action.CHANGE_TEXT";
    private MediaPlayer mediaPlayer;
    private MyReceiver serviceReceiver;
    private List<Music> list;

    private boolean playOrPause;
    private int index;
    private int control;

    private SharedPreferences shp;
    private SharedPreferences.Editor editor;
    private int max;
    private int now;

    public MusicService() {}
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        MusicDataUtils.getAllMusic(MusicService.this);
        list = MusicDataUtils.allMusic;

        serviceReceiver = new MyReceiver();
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(MEDIA_ACTION);
        registerReceiver(serviceReceiver, filter1);

        shp = getSharedPreferences("data", MODE_PRIVATE);
        editor = shp.edit();
        if(mediaPlayer != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try {
                            Thread.sleep(600);
                            max=mediaPlayer.getDuration();
                            now=mediaPlayer.getCurrentPosition();
                            editor.putInt("max", max);
                            editor.putInt("now", now);
                            editor.commit();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        unregisterReceiver(serviceReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * playMusic方法，用于MediaPlayer初始化，并开始播放。
     */
    public void playMusic(String path){
        try{

            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();

            shp = getSharedPreferences("data",MODE_PRIVATE);
            int z = shp.getInt("index",0);
            MyDBManage myDBManage = new MyDBManage(MusicService.this, "MusicStore.db");
            myDBManage.addData("RecentMusic", list.get(z));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                index++;
                if (index >= list.size()){index = 0;}
                playMusic(list.get(index).getData());
                shp = getSharedPreferences("data", MODE_PRIVATE);
                editor = shp.edit();
                editor.putInt("index", index);
                editor.commit();

                Intent intent = new Intent(CHANGE_TEXT);
                intent.putExtra("count", index);
                sendBroadcast(intent);
            }
        });
    }

    void play(){
        mediaPlayer.start();
        SendMessageToNotifaction(0);
    }

    void SendMessageToNotifaction(int state){
        switch (state){
            case 0:


        }
    }
    /**
     * 自定义广播接收器
     */
    public  class MyReceiver extends BroadcastReceiver{
        private static final String TAG = "MyReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            index = intent.getIntExtra("index",0);
            playOrPause = intent.getBooleanExtra("playorpause", false);
            Log.d("MusicService", "onReceive:::::: index = "+index +"playOrPause="+playOrPause);
            control = intent.getIntExtra("control", 1);

            shp = getSharedPreferences("data", MODE_PRIVATE);
            editor = shp.edit();
            Log.d("MusicService", "playMusic: index ="+index+";;;;;;playOrPause = " + playOrPause);
            editor.putInt("index", index);
            editor.putBoolean("playorpause", playOrPause);
            editor.apply();
            editor.commit();
            switch (control){
                case 1:
                    playMusic(list.get(index).getData());
                    break;
                case 2:
                    mediaPlayer.pause();
                    break;
                case 3:
                    mediaPlayer.start();
                    break;
                default:
            }
        }
    }
}

