package com.example.mymusicplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zhao~pc on 2017/3/18.
 */

public class FragSetting extends Fragment {
    private View view;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getActivity().getLayoutInflater().inflate(R.layout.frag_setting,
                (ViewGroup) getActivity().findViewById(R.id.view_pager), false);
    }

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
}
