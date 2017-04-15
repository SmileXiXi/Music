package com.example.mymusicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {
    private SharedPreferences shp;

    private int index;
    private List<Music> list;

    private RemoteViews remoteView;
    private boolean isPlaying;

    public static final String WIDGET_ACTION = "com.example.mymusicplayer.widgetaction";
    @Override
    public void onReceive(final Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MusicService.class);
        context.startService(serviceIntent);
        String action = intent.getAction();
        Log.d("harvic", "action:"+action);

        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
            Uri data = intent.getData();
            int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
            switch (buttonId) {
                case R.id.widget_player_btn_shang:
                    pushAction(context, 1);
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            setWidgetControl(context);
                        }
                    }, 100);
                    break;
                case R.id.widget_player_btn_pauseorplay:
                    pushAction(context, 2);
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            setWidgetControl(context);
                        }
                    }, 100);
                    break;
                case R.id.widget_player_btn_xia:
                    pushAction(context, 3);
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            setWidgetControl(context);
                        }
                    }, 100);
                    break;
            }
        } else if (intent.getAction().equals(MusicService.CHANGE_WIDGET)){
            setWidgetControl(context);
        }
        super.onReceive(context, intent);
    }
    private void pushAction(Context context, int ACTION) {
        Intent actionIntent = new Intent(WIDGET_ACTION);
        actionIntent.putExtra("actionId", ACTION);
        context.sendBroadcast(actionIntent);
    }
    private static final String TAG = "AppWidget";
    private void setWidgetControl(Context context){
        Log.d(TAG, "setWidgetControl: setWidgetControl");
        Log.d(TAG, "setWidgetControl: pre");
        if (remoteView == null){
            remoteView = new RemoteViews(context.getPackageName(),R.layout.app_widget);
        }
        shp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        index = shp.getInt("index", 0);
        Log.d(TAG, "setWidgetControl: index = " + index);
        isPlaying = shp.getBoolean("isPlaying", false);
        MusicDataUtils.getAllMusic(context);
        list = MusicDataUtils.allMusic;
        Log.d(TAG,"remoteView = " + remoteView + " list = " + list);
        remoteView.setTextViewText(R.id.widget_player_music_name, list.get(index).getTitle());
        remoteView.setTextViewText(R.id.widget_player_music_artist, list.get(index).getArtist());

        if (isPlaying){
            remoteView.setImageViewResource(R.id.widget_player_btn_pauseorplay, R.drawable.player_btn_kai);
        }else {
            remoteView.setImageViewResource(R.id.widget_player_btn_pauseorplay, R.drawable.player_btn_ting);
        }
        updateWidget(context);
    }
    public void updateWidget(Context context){
        Log.d(TAG, "updateWidget: updateWidget");
        ComponentName componentName = new ComponentName(context, AppWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(componentName, remoteView);
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        MusicDataUtils.getAllMusic(context);
        list = MusicDataUtils.allMusic;
        pushUpdate(context, appWidgetManager);
        setWidgetControl(context);
        
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        remoteView.setOnClickPendingIntent(R.id.widget_layout, contentIntent);


        ComponentName componentName = new ComponentName(context, AppWidget.class);
        appWidgetManager.updateAppWidget(componentName, remoteView);

    }
    public PendingIntent getPendingIntent(Context context, int buttonId){
        Intent intent = new Intent();
        intent.setClass(context, AppWidget.class);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setData(Uri.parse("harvic:" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pi;
    }
    private void pushUpdate(Context context,AppWidgetManager appWidgetManager) {

        remoteView = new RemoteViews(context.getPackageName(),R.layout.app_widget);
        remoteView.setOnClickPendingIntent(R.id.widget_player_btn_shang, getPendingIntent(context, R.id.widget_player_btn_shang));
        remoteView.setOnClickPendingIntent(R.id.widget_player_btn_pauseorplay, getPendingIntent(context, R.id.widget_player_btn_pauseorplay));
        remoteView.setOnClickPendingIntent(R.id.widget_player_btn_xia, getPendingIntent(context, R.id.widget_player_btn_xia));

        ComponentName componentName = new ComponentName(context, AppWidget.class);
        appWidgetManager.updateAppWidget(componentName, remoteView);
    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "onDisabled: ");
        // Enter relevant functionality for when the last widget is disabled
    }
}

