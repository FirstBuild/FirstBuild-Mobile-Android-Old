package com.firstbuild.androidapp.sousvideUI;


import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CookingFragment extends Fragment {


    private TextView textRemainTimerH;
    private TextView textRemainTimerM;

    public CookingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sousvide_cooking, container, false);

        textRemainTimerH = ((TextView) view.findViewById(R.id.text_timer_remain_h));
        textRemainTimerM = ((TextView) view.findViewById(R.id.text_timer_remain_m));

        return view;
    }


}
