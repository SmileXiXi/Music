package com.example.mymusicplayer;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


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
    private SeekBar seekBar;
    private SharedPreferences shp;
    private SharedPreferences.Editor editor;
    private int index;

    private MyReceiver myReceiver;
    private PlayerLayout playerLayout;
    public static void startAction(Context context){
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再次点击退出应用",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            ActivityCollector.finishAll();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        Log.d(TAG, "onDestroy:  onDestroy");
        unbindService(connection);
        shp = getSharedPreferences("data", MODE_PRIVATE);
        editor = shp.edit();
        editor.putBoolean("isPlaying", false);
        editor.putBoolean("isFirstClick", true);
        editor.putInt("now", 0);
        editor.commit();
        myBinder.pause();
        myBinder.cancelNoti();
        myBinder.stopMusicService(MainActivity.this);
        ActivityCollector.finishAll();
        //android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCollector.addActivity(this);
        Log.d(TAG, "onCreate: ");
        Intent service = new Intent(MainActivity.this, MusicService.class);
        bindService(service, connection, BIND_AUTO_CREATE);

        myReceiver = new MyReceiver();
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
        playerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayActivity.startAction(MainActivity.this);
            }
        });
        initPlayerLayout();
        setSeek();
        playerLayout.setControlCallBack(this);
        //开启服务 播放音乐
        Intent serviceIntent = new Intent(MainActivity.this, MusicService.class);
        startService(serviceIntent);

    }
    public void setSeek(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

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
        Log.d(TAG, "onResume: ");
        setPlayerControl();

        shp = getSharedPreferences("data", MODE_PRIVATE);
        if (!shp.getBoolean("isPlaying", true)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean needRun = true;
                    while (needRun) {
                        try {
                            Thread.sleep(1000);
                            int now = shp.getInt("now", 0);
                            int max = shp.getInt("max", 0);
                            seekBar.setMax(max);
                            seekBar.setProgress(now);

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
        super.onResume();
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
    public void next() {
        myBinder.next();
        setPlayerControl();
    }
    @Override
    public void pre() {
        myBinder.pre();
        setPlayerControl();
    }
    @Override
    public void pauseOrPlay() {
        myBinder.playOrPause();
        setPlayerControl();
    }
    //点击后，设置player控件
    boolean isPlaying;
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
        private int titleIndex;

        public MyOnClickListener(int i) {
            titleIndex = i;
        }

        public void onClick(View v) {
            viewPager.setCurrentItem(titleIndex);
            setTitleSelectedColor(titleIndex);
        }
    }
    /**
     * 广播接收器
     */

    private class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            setPlayerControl();
        }
    }
}
