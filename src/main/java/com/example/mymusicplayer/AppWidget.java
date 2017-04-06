package com.example.mymusicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {
    private SharedPreferences shp;
    private SharedPreferences.Editor editor;
    private int index;
    private boolean playOrPause;
    private int control;
    private Intent serviceIntent = new Intent(MusicService.MEDIA_ACTION);
    private List<Music> list;
    private int x = 0,y;

    public static final String ACTION_PRE = "pre";
    public static final String ACTION_PLAYORPAUSE = "playorpause";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_EXIT = "exit";
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null){return;}
        String action = intent.getAction();
        if (action.equals(ACTION_PRE)){
            shp = context.getSharedPreferences("data", MainActivity.MODE_PRIVATE);
            playOrPause = shp.getBoolean("playorpause", false);
            index = shp.getInt("index", 0);
            control = shp.getInt("control", 1);
            if (index == 0) {
                index = list.size() - 1;
            } else {
                index = index - 1;
            }
            serviceIntent.putExtra("index", index);
            serviceIntent.putExtra("control", control);
            context.sendBroadcast(serviceIntent);
        }
        if (action.equals(ACTION_PLAYORPAUSE)){
            shp = context.getSharedPreferences("data", context.MODE_PRIVATE);
            playOrPause = shp.getBoolean("playorpause", false);
            index = shp.getInt("index", 0);
            control = shp.getInt("control", 1);
            y = shp.getInt("y", 0);
            if (x == 0 && y == 0) {
                serviceIntent.putExtra("index", index);
                serviceIntent.putExtra("control", 1);
                serviceIntent.putExtra("playorpause", true);
                context.sendBroadcast(serviceIntent);
                shp = context.getSharedPreferences("data", context.MODE_PRIVATE);
                editor = shp.edit();
                editor.putInt("x", 1);
                editor.commit();
                x = 1;
            } else {
                if (playOrPause) {
                    serviceIntent.putExtra("playorpause", false);
                    serviceIntent.putExtra("control", 2);
                    serviceIntent.putExtra("index", index);
                    context.sendBroadcast(serviceIntent);
                } else {
                    serviceIntent.putExtra("playorpause", true);
                    serviceIntent.putExtra("control", 3);
                    serviceIntent.putExtra("index", index);
                    Log.d("MainActivity", "pauseOrPlay:   ");
                    context.sendBroadcast(serviceIntent);
                }
            }
        }
        if (action.equals(ACTION_NEXT)){
            shp = context.getSharedPreferences("data", context.MODE_PRIVATE);
            playOrPause = shp.getBoolean("playorpause", false);
            index = shp.getInt("index", 0);
            control = shp.getInt("control", 1);
            if (index == list.size() - 1) {
                index = 0;
            } else {
                index = index + 1;
            }
            serviceIntent.putExtra("index", index);
            serviceIntent.putExtra("control", control);
            context.sendBroadcast(serviceIntent);
        }
        if (action.equals(ACTION_EXIT)){
            ActivityCollector.finishAll();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        MusicDataUtils.getAllMusic(context);
        list = MusicDataUtils.allMusic;

        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        Intent preIn = new Intent(ACTION_PRE);
        PendingIntent preIntent = PendingIntent.getBroadcast(context,
                1, preIn, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_player_btn_shang, preIntent);

        Intent playOrPauseIn = new Intent(ACTION_PLAYORPAUSE);
        PendingIntent playOrPauseIntent = PendingIntent.getBroadcast(context,
                2, playOrPauseIn, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_player_btn_pauseorplay, playOrPauseIntent);

        Intent nextIn = new Intent(ACTION_NEXT);
        PendingIntent nextIntent = PendingIntent.getBroadcast(context,
                3, nextIn, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_player_btn_xia, nextIntent);

        Intent exitIn = new Intent(ACTION_EXIT);
        PendingIntent exitIntent = PendingIntent.getBroadcast(context,
                4, exitIn, 0);
        remoteView.setOnClickPendingIntent(R.id.noti_exit, exitIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteView);

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

