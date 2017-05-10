package com.example.mymusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.mymusicplayer.activity.MainActivity;
import com.example.mymusicplayer.activity.PlayActivity;
import com.example.mymusicplayer.database.MyDBManage;
import com.example.mymusicplayer.localmusic.Music;
import com.example.mymusicplayer.localmusic.MusicDataUtils;

import java.io.IOException;
import java.util.List;

public class MusicService extends Service{
    public static final String CHANGE_TEXT = "com.example.action.CHANGE_TEXT";
    private MediaPlayer mediaPlayer;
    private List<Music> list;

    private int index;

    private SharedPreferences shp;
    private SharedPreferences.Editor editor;
    private int max;
    private int now;

    private NotificationManager manager;
    private Notification notification;
    private RemoteViews remoteView;
    private boolean isPlaying;
    private MyReceiver receiverNoti, receiverWidget;
    private Handler handler = new Handler();
    public MusicService() {}
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: onCreate");
        Exception e = new Exception(TAG);
        e.printStackTrace();
        MusicDataUtils.getAllMusic(MusicService.this);
        list = MusicDataUtils.allMusic;
        initNotification();
        mediaPlayer = new MediaPlayer();

        receiverWidget = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppWidget.WIDGET_ACTION);
        registerReceiver(receiverWidget, intentFilter);
        super.onCreate();
    }

    //初始化notification
    public void initNotification(){
        Log.d(TAG, "initNotification: initNotification");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, PlayActivity.class), 0);
        remoteView = new RemoteViews(this.getPackageName(), R.layout.notification_layout);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(this)
                .setCustomContentView(remoteView)
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(contentIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.icon))
                .build();
        manager.notify(1, notification);

        Intent notiIntent = new Intent(MainActivity.class.getSimpleName());
        notiIntent.putExtra(BUTTON_ACTION, 1);
        PendingIntent preIntent = PendingIntent.getBroadcast(MusicService.this,
                1, notiIntent, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_player_btn_shang, preIntent);

        notiIntent.putExtra(BUTTON_ACTION, 2);
        PendingIntent playOrPauseIntent = PendingIntent.getBroadcast(MusicService.this,
                2, notiIntent, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_player_btn_pauseorplay, playOrPauseIntent);

        notiIntent.putExtra(BUTTON_ACTION, 3);
        PendingIntent nextIntent = PendingIntent.getBroadcast(MusicService.this,
                3, notiIntent, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_player_btn_xia, nextIntent);

        notiIntent.putExtra(BUTTON_ACTION, 4);
        PendingIntent exitIntent = PendingIntent.getBroadcast(MusicService.this,
                4, notiIntent, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_exit, exitIntent);

        setNotiControl();
        remoteView.setImageViewResource(R.id.noti_player_btn_pauseorplay, R.drawable.player_btn_ting);
        manager.notify(1, notification);
        IntentFilter filter = new IntentFilter(MainActivity.class.getSimpleName());
        receiverNoti = new MyReceiver();
        registerReceiver(receiverNoti, filter);
    }

    public static final String BUTTON_ACTION = "com.example.mymusicplayer.Action";
    //设置notification中控件
    public void setNotiControl(){
        shp = getSharedPreferences("data", MODE_PRIVATE);
        index = shp.getInt("index", 0);
        isPlaying = shp.getBoolean("isPlaying", false);
        remoteView.setTextViewText(R.id.noti_player_music_name, list.get(index).getTitle());
        remoteView.setTextViewText(R.id.noti_player_music_artist, list.get(index).getArtist());
        if (isPlaying){
            remoteView.setImageViewResource(R.id.noti_player_btn_pauseorplay, R.drawable.player_btn_kai);
        }else {
            remoteView.setImageViewResource(R.id.noti_player_btn_pauseorplay, R.drawable.player_btn_ting);
        }
        manager.notify(1, notification);
    }

    private static final String TAG = "MusicService";
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:  onDestroy");
        unregisterReceiver(receiverNoti);
        unregisterReceiver(receiverWidget);
        shp = getSharedPreferences("data", MODE_PRIVATE);
        editor = shp.edit();
        editor.putBoolean("isPlaying", false);
        editor.putBoolean("isFirstClick", true);
        editor.commit();
        myBinder.pushAction();
        mediaPlayer.stop();
        mediaPlayer.release();
        manager.cancel(1);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return super.onStartCommand(intent, flags, startId);
    }

    boolean isFirstClick;
    public static final String CHANGE_WIDGET = "com.example.action.CHANGE_WIDGET";
    /**
     * playMusic方法，用于MediaPlayer初始化，并开始播放。
     */
    public void playMusic(String path){
        try{
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();

            remoteView.setImageViewResource(R.id.noti_player_btn_pauseorplay, R.drawable.player_btn_kai);
            manager.notify(1, notification);

            shp = getSharedPreferences("data",MODE_PRIVATE);
            index = shp.getInt("index",0);
            MyDBManage myDBManage = new MyDBManage(MusicService.this, "MusicStore.db");
            myDBManage.addData("RecentMusic", list.get(index));
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean needRun = true;
                while (needRun) {
                    try {
                        Thread.sleep(1000);
                        now = mediaPlayer.getCurrentPosition();
                        max = mediaPlayer.getDuration();
                        shp = getSharedPreferences("data", MODE_PRIVATE);
                        editor = shp.edit();
                        editor.putInt("now", now);
                        editor.putInt("max", max);
                        editor.commit();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e){
                        needRun = false;
                    }
                }
            }
        }).start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                index++;
                if (index == list.size()){index = 0;}
                shp = getSharedPreferences("data", MODE_PRIVATE);
                editor = shp.edit();
                editor.putInt("index", index);
                editor.commit();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playMusic(list.get(index).getData());
                        setNotiControl();
                        Log.d(TAG, "onCompletion: onCompletion  index = " + index);
                        sendBroadcast(serviceIntent);
                    }
                },50);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {// 错误处理事件
            @Override public boolean onError(MediaPlayer player, int arg1, int arg2) {
                mediaPlayer.reset();
                return false;
            }
        });
    }

    /**
     * 自定义MyBinder类
     */
    private MyBinder  myBinder= new MyBinder();
    public class MyBinder extends Binder{
        private void pushAction() {
            Intent actionIntent = new Intent(CHANGE_WIDGET);
            sendBroadcast(actionIntent);}
        public void setSeekTo(int position){
            mediaPlayer.seekTo(position);
        }
        public void pre(){
            Log.d(TAG, "pre: pre");
            shp = getSharedPreferences("data",MODE_PRIVATE);
            index = shp.getInt("index",0);
            if (index == 0 ){
                playMusic(list.get(list.size()-1).getData());
                index = list.size() - 1;
            }else {
                playMusic(list.get(index - 1).getData());
                index = index -1;
            }
            editor = shp.edit();
            editor.putInt("index", index);
            editor.putBoolean("isPlaying", true);
            editor.commit();
            pushAction();
            setNotiControl();
        }
        public void next(){
            shp = getSharedPreferences("data",MODE_PRIVATE);
            index = shp.getInt("index",0);
            if (index == list.size() - 1){
                playMusic(list.get(0).getData());
                index = 0;
            }else {
                playMusic(list.get(index + 1).getData());
                index = index + 1;
            }
            editor = shp.edit();
            editor.putInt("index", index);
            editor.putBoolean("isPlaying", true);
            editor.commit();
            pushAction();
            setNotiControl();
        }
        private static final String TAG = "MyBinder";
        public void play(){
            Log.d(TAG, "play:  play");
            shp = getSharedPreferences("data",MODE_PRIVATE);
            index = shp.getInt("index",0);
            playMusic(list.get(index).getData());
            setNotiControl();
        }
        public void pause(){
            Log.d(TAG, "pause: ");
            remoteView.setImageViewResource(R.id.noti_player_btn_pauseorplay, R.drawable.player_btn_ting);
            manager.notify(1, notification);
            mediaPlayer.pause();
        }
        public void playOrPause(){
            shp = getSharedPreferences("data",MODE_PRIVATE);
            isFirstClick = shp.getBoolean("isFirstClick", true);
            index = shp.getInt("index", 0);
            if (mediaPlayer.isPlaying()){
                pause();
            } else {
                if (isFirstClick){
                    playMusic(list.get(index).getData());
                }else {
                    remoteView.setImageViewResource(R.id.noti_player_btn_pauseorplay, R.drawable.player_btn_kai);
                    manager.notify(1, notification);
                    mediaPlayer.start();}
            }
            editor = shp.edit();
            editor.putBoolean("isFirstClick", false);
            editor.putBoolean("isPlaying", mediaPlayer.isPlaying());
            editor.commit();
            pushAction();
        }
        public void cancelNoti(){
            Log.d(TAG, "cancelNoti: ");
            manager.cancel(1);}
        public boolean isMusicPlaying(){
            if (mediaPlayer == null){return false;}
            return mediaPlayer.isPlaying();
        }
    }

    private Intent serviceIntent = new Intent(CHANGE_TEXT);
    private class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppWidget.WIDGET_ACTION)){
               int actionId = intent.getIntExtra("actionId", 0);
                switch (actionId){
                    case 1:
                        myBinder.pre();
                        break;
                    case 2:
                        myBinder.playOrPause();
                        break;
                    case 3:
                        myBinder.next();
                        break;
                    default:
                        break;
                }
            }
            //setNotiControl();
            switch (intent.getIntExtra(BUTTON_ACTION, 0)){
                case 1:
                    myBinder.pre();
                    setNotiControl();
                    sendBroadcast(serviceIntent);
                    break;
                case 2:
                    myBinder.playOrPause();
                    setNotiControl();
                    sendBroadcast(serviceIntent);
                    break;
                case 3:
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myBinder.next();
                            setNotiControl();
                            sendBroadcast(serviceIntent);
                        }
                    },50);
                    break;
                case 4:
                    Log.d(TAG, "onReceive: onReceive");
                    myBinder.cancelNoti();
                    ActivityCollector.finishAll();
                    break;
                default:
                    break;
            }
        }
    }
}

