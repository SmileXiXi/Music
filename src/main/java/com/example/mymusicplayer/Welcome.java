package com.example.mymusicplayer;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);
        ActivityCollector.addActivity(this);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.startAction(Welcome.this);
                finish();
            }
        }, 2000);
    }

    private static final String TAG = "Welcome";
    private SharedPreferences shp;
    private SharedPreferences.Editor editor;
    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy:  onDestroy");
        shp = getSharedPreferences("data", MODE_PRIVATE);
        editor = shp.edit();
        editor.putBoolean("isPlaying", false);
        editor.putBoolean("isFirstClick", true);
        editor.commit();
        ActivityCollector.removeActivity(this);
        super.onDestroy();
    }
}
