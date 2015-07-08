package com.firstbuild.androidapp.sousvideUI;


import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.firstbuild.androidapp.Paragon.ParagonMainActivity;
import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreheatingFragment extends Fragment {

    private String TAG = PreheatingFragment.class.getSimpleName();

    private Handler handlerUpdateCards;
    private Runnable runnable;
    private View imgPulse;
    private View containerCurrentTemp;
    private float scaleTempIndicator = 10.0f;

    public PreheatingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sousvide_preheating, container, false);

        imgPulse = view.findViewById(R.id.img_pulse);
        containerCurrentTemp = view.findViewById(R.id.container_current_temp);

        view.findViewById(R.id.layout_current_temp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_content, new ReadyToCookFragment()).
                        addToBackStack(null).
                        commit();
            }
        });

        handlerUpdateCards = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                updatePulse();          // Update UI base on new ERDs every second.
                handlerUpdateCards.postDelayed(runnable, 30);
            }
        };

        handlerUpdateCards.postDelayed(runnable, 1);


        return view;
    }

    private void updatePulse() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imgPulse.getLayoutParams();
//        Log.d(TAG, "updatePulse" + ", container Y :"+containerCurrentTemp.getY()+", pluse top :"+layoutParams.topMargin);

        if(layoutParams.topMargin < -imgPulse.getHeight()){
            layoutParams.topMargin = containerCurrentTemp.getHeight();
        }
        else{
            layoutParams.topMargin -= 10;
        }

        imgPulse.setLayoutParams(layoutParams);
    }

    public void updateUiCurrentTemp(){
//        int delta = ((ParagonMainActivity)getActivity()).targetTemp - ((ParagonMainActivity)getActivity()).currentTemp;

//        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) containerCurrentTemp.getLayoutParams();
//        lParams.topMargin = (int)(delta * scaleTempIndicator);
//
//        containerCurrentTemp.invalidate();
    }

}
