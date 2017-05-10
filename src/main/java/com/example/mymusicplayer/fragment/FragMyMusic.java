package com.example.mymusicplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.example.mymusicplayer.R;
import com.example.mymusicplayer.activity.AllMusicActivity;
import com.example.mymusicplayer.activity.LoveMusicActivity;
import com.example.mymusicplayer.activity.RecentMusicActivity;

/**
 * Created by zhao~pc on 2017/3/18.
 */

public class FragMyMusic extends Fragment implements View.OnClickListener{
    private View view;
    private ImageButton  imageButton1, imageButton2, imageButton3, imageButton4;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getActivity().getLayoutInflater().inflate(R.layout.frag_my_music,
                (ViewGroup) getActivity().findViewById(R.id.view_pager), false);
        imageButton1 = (ImageButton) view.findViewById(R.id.bt_allmusic);
        imageButton1.setOnClickListener(this);
        imageButton2 = (ImageButton) view.findViewById(R.id.bt_mylove);
        imageButton2.setOnClickListener(this);
        imageButton3 = (ImageButton) view.findViewById(R.id.bt_recentmusic);
        imageButton3.setOnClickListener(this);
        imageButton4 = (ImageButton) view.findViewById(R.id.bt_comment);
        imageButton4.setOnClickListener(this);

    }

    private static final String TAG = "FragMyMusic";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        if (viewGroup != null){
            viewGroup.removeAllViewsInLayout();
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_allmusic:
                AllMusicActivity.actionStart(getContext());
                break;
            case R.id.bt_recentmusic:
                RecentMusicActivity.actionStart(getContext());
                break;
            case R.id.bt_mylove:
                LoveMusicActivity.actionStart(getContext());
                break;
            case R.id.bt_comment:
                break;
            default:
                break;
        }
    }
}
