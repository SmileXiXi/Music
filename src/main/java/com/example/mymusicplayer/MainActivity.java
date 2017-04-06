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
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.SocketHandler;

public class MainActivity extends FragmentActivity implements PlayerLayout.ControlCallBack{
    private ViewPager viewPager;
    private FragMyMusic fragMyMusic;
    private FragMusicOnline fragMusicOnline;
    private FragSetting fragSetting;
    private ArrayList<Fragment> fragmentList;
    private TextView textView1, textView2, textView3;
    private List<Music> list;
    private TextView textName, textArtist;
    private ImageView imagePre, imagePlayorPause, imageNext;
    private ProgressBar progressBar;
    private SharedPreferences shp;
    private SharedPreferences.Editor editor;
    private int index;
    private boolean playOrPause;
    private int control;

    private NotificationManager manager;
    private Notification notification;
    private RemoteViews remoteView;

    private PlayerLayout playerLayout;
    public static void startAction(Context context){
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        shp = getSharedPreferences("data", MODE_PRIVATE);
        editor = shp.edit();
        editor.putInt("x", 0);
        editor.putInt("y", 0);
        editor.putBoolean("playorpause", false);
        editor.commit();
        Log.d(TAG, "onDestroy:   onDestroy");
        ActivityCollector.removeActivity(this);
    }
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCollector.addActivity(this);

        MyReceiver myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.CHANGE_TEXT);
        registerReceiver(myReceiver,intentFilter);

        MusicDataUtils.getAllMusic(MainActivity.this);
        list = MusicDataUtils.allMusic;

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        fragMyMusic = new FragMyMusic();
        fragMusicOnline = new FragMusicOnline();
        fragSetting = new FragSetting();
        fragmentList = new ArrayList<>();
        fragmentList.add(fragMyMusic);
        fragmentList.add(fragMusicOnline);
        fragmentList.add(fragSetting);
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        initTextView();
        setTitleSelectedColor(0);
        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());

        playerLayout = (PlayerLayout) findViewById(R.id.player_layout_main_activity);
        initPlayerLayout();

        initNotification();

        playerLayout.setControlCallBack(this);
        //开启服务 播放音乐
        Intent serviceIntent = new Intent(MainActivity.this, MusicService.class);
        startService(serviceIntent);

    }

    //初始化notification
    public void initNotification(){
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
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
        PendingIntent preIntent = PendingIntent.getBroadcast(MainActivity.this,
                1, notiIntent, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_player_btn_shang, preIntent);

        notiIntent.putExtra(BUTTON_ACTION, 2);
        PendingIntent playOrPauseIntent = PendingIntent.getBroadcast(MainActivity.this,
                2, notiIntent, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_player_btn_pauseorplay, playOrPauseIntent);

        notiIntent.putExtra(BUTTON_ACTION, 3);
        PendingIntent nextIntent = PendingIntent.getBroadcast(MainActivity.this,
                3, notiIntent, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_player_btn_xia, nextIntent);

        notiIntent.putExtra(BUTTON_ACTION, 4);
        PendingIntent exitIntent = PendingIntent.getBroadcast(MainActivity.this,
                4, notiIntent, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_exit, exitIntent);

        setNotiControl();
        remoteView.setImageViewResource(R.id.noti_player_btn_pauseorplay, R.drawable.player_btn_ting);
        manager.notify(1, notification);
        IntentFilter filter = new IntentFilter(MainActivity.class.getSimpleName());
        MyReceiver receiver = new MyReceiver();
        registerReceiver(receiver, filter);
    }
    public static final String BUTTON_ACTION = "com.example.mymusicplayer.Action";
    //设置notification中控件
    public void setNotiControl(){
        shp = getSharedPreferences("data", MODE_PRIVATE);
        index = shp.getInt("inde5x", 0);
        playOrPause = shp.getBoolean("playorpause", false);
        remoteView.setTextViewText(R.id.noti_player_music_name, list.get(index).getTitle());
        remoteView.setTextViewText(R.id.noti_player_music_artist, list.get(index).getArtist());
        if (playOrPause){
            remoteView.setImageViewResource(R.id.noti_player_btn_pauseorplay, R.drawable.player_btn_ting);
        }else {
            remoteView.setImageViewResource(R.id.noti_player_btn_pauseorplay, R.drawable.player_btn_kai);
        }
        manager.notify(1, notification);
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
    /**
     * Title 页卡标题选中颜色设置
     */
    public void setTitleSelectedColor(int index){
        clearSelected();
        switch (index){
            case 0:
                textView1.setTextColor(0xff6495ED);
                break;
            case 1:
                textView2.setTextColor(0xff6495ED);
                break;

            case 2:
                textView3.setTextColor(0xff6495ED);
                break;
            default:
        }
    }

    private void clearSelected() {
        textView1.setTextColor(0xff969696);
        textView2.setTextColor(0xff969696);
        textView3.setTextColor(0xff969696);
    }

    /**
     * Title 页卡初始化、点击事件
     */
    private void initTextView() {
        textView1 = (TextView) findViewById(R.id.text1);
        textView2 = (TextView) findViewById(R.id.text2);
        textView3 = (TextView) findViewById(R.id.text3);

        textView1.setOnClickListener(new MyOnClickListener(0));
        textView2.setOnClickListener(new MyOnClickListener(1));
        textView3.setOnClickListener(new MyOnClickListener(2));
    }

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

    Intent serviceIntent = new Intent(MusicService.MEDIA_ACTION);
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
        serviceIntent.putExtra("index", index);
        serviceIntent.putExtra("control", control);
        textName.setText(list.get(index).getTitle());
        textArtist.setText(list.get(index).getArtist());
        sendBroadcast(serviceIntent);
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
        serviceIntent.putExtra("index", index);
        serviceIntent.putExtra("control", control);
        textName.setText(list.get(index).getTitle());
        textArtist.setText(list.get(index).getArtist());
        sendBroadcast(serviceIntent);
    }
    int x = 0, y;
    @Override
    public void pauseOrPlay() {
        shp = getSharedPreferences("data", MODE_PRIVATE);
        playOrPause = shp.getBoolean("playorpause", false);
        index = shp.getInt("index",0);
        control = shp.getInt("control",1);
        y = shp.getInt("y", 0);
        if (x == 0 && y==0){
            imagePlayorPause.setImageResource(R.drawable.player_btn_kai);
            serviceIntent.putExtra("index", index);
            serviceIntent.putExtra("control", 1);
            serviceIntent.putExtra("playorpause", true);
            sendBroadcast(serviceIntent);
            shp = getSharedPreferences("data", MODE_PRIVATE);
            editor = shp.edit();
            editor.putInt("x", 1);
            editor.commit();
            x = 1;
        }else {
            if (playOrPause){
                imagePlayorPause.setImageResource(R.drawable.player_btn_ting);
                serviceIntent.putExtra("playorpause", false);
                serviceIntent.putExtra("control", 2);
                serviceIntent.putExtra("index", index);
                sendBroadcast(serviceIntent);
            }else {
                imagePlayorPause.setImageResource(R.drawable.player_btn_kai);
                serviceIntent.putExtra("playorpause", true);
                serviceIntent.putExtra("control", 3);
                serviceIntent.putExtra("index", index);
                Log.d("MainActivity", "pauseOrPlay:   ");
                sendBroadcast(serviceIntent);
            }
        }
    }

    /**
     * ViewPage 切换页面监听类 Title标题颜色变化
     */
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            setTitleSelectedColor(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    }

    /**
     * ViewPager 适配器类
     */
    public class MyViewPagerAdapter extends FragmentPagerAdapter{

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    /**
     * Title 监听类
     */
    private class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            viewPager.setCurrentItem(index);
            setTitleSelectedColor(index);
        }
    }
    /**
     * 广播接收器
     */
    private int count;
    private class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            setNotiControl();
            if (intent.getAction().equals(MainActivity.class.getSimpleName())){
                switch (intent.getIntExtra(BUTTON_ACTION, 0)){
                    case 1:
                        shp = getSharedPreferences("data", MODE_PRIVATE);
                        playOrPause = shp.getBoolean("playorpause", false);
                        index = shp.getInt("index",0);
                        control = shp.getInt("control",1);
                        if (index == 0){
                            index = list.size()-1;
                        }else {index = index - 1;}
                        serviceIntent.putExtra("index", index);
                        serviceIntent.putExtra("control", control);
                        setNotiControl();
                        sendBroadcast(serviceIntent);
                        break;
                    case 2:
                        shp = getSharedPreferences("data", MODE_PRIVATE);
                        playOrPause = shp.getBoolean("playorpause", false);
                        index = shp.getInt("index",0);
                        control = shp.getInt("control",1);
                        y = shp.getInt("y", 0);
                        if (x == 0 && y==0){
                            setNotiControl();
                            serviceIntent.putExtra("index", index);
                            serviceIntent.putExtra("control", 1);
                            serviceIntent.putExtra("playorpause", true);
                            sendBroadcast(serviceIntent);
                            shp = getSharedPreferences("data", MODE_PRIVATE);
                            editor = shp.edit();
                            editor.putInt("x", 1);
                            editor.commit();
                            x = 1;
                        }else {
                            if (playOrPause){
                                setNotiControl();
                                serviceIntent.putExtra("playorpause", false);
                                serviceIntent.putExtra("control", 2);
                                serviceIntent.putExtra("index", index);
                                sendBroadcast(serviceIntent);
                            }else {
                                setNotiControl();
                                serviceIntent.putExtra("playorpause", true);
                                serviceIntent.putExtra("control", 3);
                                serviceIntent.putExtra("index", index);
                                Log.d("MainActivity", "pauseOrPlay:   ");
                                sendBroadcast(serviceIntent);
                            }
                        }
                        break;
                    case 3:
                        shp = getSharedPreferences("data", MODE_PRIVATE);
                        playOrPause = shp.getBoolean("playorpause", false);
                        index = shp.getInt("index",0);
                        control = shp.getInt("control",1);
                        if (index == list.size()-1){
                            index = 0;
                        }else {
                            index = index + 1;
                        }
                        serviceIntent.putExtra("index", index);
                        serviceIntent.putExtra("control", control);
                        setNotiControl();
                        sendBroadcast(serviceIntent);
                        break;
                    case 4:
                        ActivityCollector.finishAll();
                        manager.cancel(1);
                        break;
                    default:
                }
            }else {
                count = intent.getIntExtra("count", index);
                textName.setText(list.get(count).getTitle());
                textArtist.setText(list.get(count).getArtist());
            }

        }
    }
}
