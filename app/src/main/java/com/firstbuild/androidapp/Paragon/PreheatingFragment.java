package com.firstbuild.androidapp.Paragon;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.sousvideUI.ReadyToCookFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreheatingFragment extends Fragment {

    private String TAG = PreheatingFragment.class.getSimpleName();

    private Handler handlerUpdateCards;
    private Runnable runnable;
    private View imgPulse;
    private RelativeLayout containerCurrentTemp;
    private TextView textCurrentTemp;

    private float scaleTempIndicator = 10.0f;
    private TextView textTargetTemp;

    public PreheatingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sousvide_preheating, container, false);

        imgPulse = view.findViewById(R.id.img_pulse);
        containerCurrentTemp = (RelativeLayout) view.findViewById(R.id.container_current_temp);

        textTargetTemp = (TextView) view.findViewById(R.id.text_target_temp);


        textTargetTemp.setText(((ParagonMainActivity) getActivity()).getTargetTemp() + "℉");
        textCurrentTemp = (TextView) view.findViewById(R.id.text_current_temp);

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

        if (layoutParams.topMargin < -imgPulse.getHeight()) {
            layoutParams.topMargin = containerCurrentTemp.getHeight();
        }
        else {
            layoutParams.topMargin -= 10;
        }

        imgPulse.setLayoutParams(layoutParams);
    }

    public void updateUiCurrentTemp() {
        ParagonMainActivity activity = (ParagonMainActivity) getActivity();

        int delta = (int)(activity.getTargetTemp()) - (int)(activity.getCurrentTemp());

        if(delta >= 0){
            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) containerCurrentTemp.getLayoutParams();
            containerCurrentTemp.startAnimation(makeAnimation(lParams.topMargin, (int) (delta * scaleTempIndicator)));
        }
        else{
            // Translate limit is not up over the top.
        }


        textCurrentTemp.setText(activity.getCurrentTemp() + "℉");
    }

    private TranslateAnimation makeAnimation(final int fromMargin, final int toMargin) {
        TranslateAnimation animation =
                new TranslateAnimation(0, 0, 0, dipsToPixels(toMargin - fromMargin));
        animation.setDuration(250);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                // Cancel the animation to stop the menu from popping back.
                containerCurrentTemp.clearAnimation();

                LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) containerCurrentTemp.getLayoutParams();
                lParams.topMargin = dipsToPixels(toMargin);
                containerCurrentTemp.requestLayout();
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });

        return animation;
    }


    private int dipsToPixels(int dips) {
//        final float scale = getResources().getDisplayMetrics().density;
//        return (int)(dips * scale + 0.5f);

        return dips;
    }
}
