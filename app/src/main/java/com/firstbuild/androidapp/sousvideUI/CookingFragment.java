package com.firstbuild.androidapp.sousvideUI;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
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
    private TimerCycle timerCycle;
    private TextView textKindOfTimer;

    private Handler handlerUpdateCards;
    private Runnable runnable;
    private TextView textTitle;
    private TextView textDoneTime;


    public CookingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sousvide_cooking, container, false);

        textTitle = (TextView) view.findViewById(R.id.text_title);
        textKindOfTimer = (TextView) view.findViewById(R.id.text_kind_of_timer);
        textDoneTime = (TextView) view.findViewById(R.id.text_will_be_done);
        textRemainTimerH = ((TextView) view.findViewById(R.id.text_timer_remain_h));
        textRemainTimerM = ((TextView) view.findViewById(R.id.text_timer_remain_m));
        timerCycle = (TimerCycle) view.findViewById(R.id.timer_cycle);
        view.findViewById(R.id.btn_complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        textTitle.setText("Now Cooking");
        textKindOfTimer.setText("Time Remaining");
        textDoneTime.setText("Fool will be done at");

        timerCycle.startSpinning();


        handlerUpdateCards = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                updateCookingComplete();
            }
        };

        handlerUpdateCards.postDelayed(runnable, 12000);

        return view;
    }


    private void updateCookingComplete() {
        textTitle.setText("Cooking Complete!");
        textKindOfTimer.setText("Finished");
        textDoneTime.setText("Session ended at");

    }


}
