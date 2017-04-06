package com.example.mymusicplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by zhao~pc on 2017/3/18.
 */

public class FragMyMusic extends Fragment implements View.OnClickListener{
    private View view;
    private RelativeLayout relativeLayout1,relativeLayout2,relativeLayout3,relativeLayout4;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getActivity().getLayoutInflater().inflate(R.layout.frag_my_music,
                (ViewGroup) getActivity().findViewById(R.id.view_pager), false);

        relativeLayout1 = (RelativeLayout) view.findViewById(R.id.all_music_layout);
        relativeLayout1.setOnClickListener(this);
        Log.d(TAG, "relativewLayout="+ relativeLayout1.toString() );

        relativeLayout3 = (RelativeLayout) view.findViewById(R.id.recent_music_layout);
        relativeLayout3.setOnClickListener(this);

        relativeLayout2 = (RelativeLayout) view.findViewById(R.id.my_love_layout);
        relativeLayout2.setOnClickListener(this);
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
            case R.id.all_music_layout:
                AllMusicActivity.actionStart(getContext());
                break;
            case R.id.recent_music_layout:
                RecentMusicActivity.actionStart(getContext());
                break;
            case R.id.my_love_layout:
                LoveMusicActivity.actionStart(getContext());
                break;

        }
    }
}
