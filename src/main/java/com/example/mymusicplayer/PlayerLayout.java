package com.example.mymusicplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhao~pc on 2017/3/19.
 */

public class PlayerLayout extends LinearLayout implements View.OnClickListener{
    private TextView musicName,artist;
    private ImageView pauseOrPlay,next,pre;
    private Music music;
    public ControlCallBack controlCallBack;
    public AllMusicActivity activity;


    public interface ControlCallBack{
        void next();
        void pre();
        void pauseOrPlay();
    }

    public void setControlCallBack(ControlCallBack controlCallBack) {
        this.controlCallBack = controlCallBack;
    }

    public PlayerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.player_layout, this);
        musicName = (TextView) findViewById(R.id.player_music_name);
        artist = (TextView)findViewById(R.id.player_music_artist);
        next = (ImageView) findViewById(R.id.player_btn_xia);
        pre = (ImageView) findViewById(R.id.player_btn_shang);
        pauseOrPlay = (ImageView) findViewById(R.id.player_btn_pauseorplay);
        next.setOnClickListener(this);
        pre.setOnClickListener(this);
        pauseOrPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == next.getId()){
            if (controlCallBack != null){
                controlCallBack.next();
            }
        }else if (id == pre.getId()){
            if (controlCallBack != null){
                controlCallBack.pre();
            }
        }else if (id == pauseOrPlay.getId()){
            if (controlCallBack != null){
                controlCallBack.pauseOrPlay();
            }
        }
    }
}
