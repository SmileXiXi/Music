package com.example.mymusicplayer;

import android.app.Application;

import java.util.List;

/**
 * Created by zhao~pc on 2017/3/22.
 */

public class MyApplication extends Application{
    PlayerLayout playerLayout;
    List<Music> list;
    public static MyApplication transferPlayerLayout;

    @Override
    public void onCreate() {
        super.onCreate();
        transferPlayerLayout = this;
    }

    public static MyApplication getApplication(){
        return transferPlayerLayout;
    }

    public void setPlayerLayout(PlayerLayout playerLayout, List<Music> list){
        this.playerLayout = playerLayout;
        this.list = list;
    }
    public void setPlayerLayout(PlayerLayout playerLayout) {
        this.playerLayout = playerLayout;
    }
    public void setPlayerLayout(List<Music> list){
        this.list = list;
    }
    public PlayerLayout getPlayerLayout(){
        return playerLayout;
    }

    public List<Music> getList() {
        return list;
    }
}
