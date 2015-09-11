package com.firstbuild.androidapp.Paragon;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.viewUtil.gridCircleView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SousvideStatusFragment extends Fragment {

    private String TAG = "SousvideStatusFragment";

    public enum COOK_STATE {
        STATE_PREHEAT,
        STATE_READY_TO_COOK,
        STATE_COOKING,
        STATE_DONE,
    }

    private gridCircleView circle;
    private ImageView[] progressDots = new ImageView[4];
    private View layoutStatus;
    private ImageView imgStatus;
    private TextView textTempCurrent;
    private TextView textTempTarget;
    private TextView textStatusName;
    private TextView textLabelCurrent;
    private View btnContinue;
    private COOK_STATE cookState = COOK_STATE.STATE_PREHEAT;

    public SousvideStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView IN");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sousvide_circle, container, false);

        circle = (gridCircleView) view.findViewById(R.id.circle);
        circle.setBarValue(0);
        circle.setGridValue(0);
        circle.setDashValue(0);

        textStatusName = (TextView) view.findViewById(R.id.text_status_name);
        textLabelCurrent = (TextView) view.findViewById(R.id.text_label_current);

        progressDots[0] = (ImageView) view.findViewById(R.id.progress_dot_1);
        progressDots[1] = (ImageView) view.findViewById(R.id.progress_dot_2);
        progressDots[2] = (ImageView) view.findViewById(R.id.progress_dot_3);
        progressDots[3] = (ImageView) view.findViewById(R.id.progress_dot_4);

        textTempCurrent = (TextView) view.findViewById(R.id.text_temp_current);
        textTempTarget = (TextView) view.findViewById(R.id.text_temp_target);

        layoutStatus = view.findViewById(R.id.layout_status);
        imgStatus = (ImageView) view.findViewById(R.id.img_status);

        layoutStatus.setVisibility(View.VISIBLE);
        imgStatus.setVisibility(View.GONE);

        btnContinue = view.findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateUiCookState(COOK_STATE.STATE_COOKING);
            }
        });

        //TODO: remove code below after debug.
        view.findViewById(R.id.progress_dot_2).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                updateCookStatus(false);
            }
        });

        updateUiCurrentTemp();

        UpdateUiCookState(COOK_STATE.STATE_PREHEAT);

        return view;
    }


    public void updateCookStatus(boolean isPreaheat) {
        Log.d(TAG, "updateCookStatus IN " + isPreaheat);

        if (isPreaheat) {
            UpdateUiCookState(COOK_STATE.STATE_PREHEAT);
        }
        else if(cookState == COOK_STATE.STATE_PREHEAT){
            UpdateUiCookState(COOK_STATE.STATE_READY_TO_COOK);
        }
        else{
            //nothing.
        }

    }


    /**
     * Update UI progress bar top of the screen and on circle view.
     */
    public void UpdateUiCookState(COOK_STATE state) {
        Log.d(TAG, "UpdateUiCookState " + state);

        if(cookState != state){
            cookState = state;

            switch (cookState) {
                case STATE_PREHEAT:
                    textStatusName.setText("PREHEATING");
                    progressDots[0].setImageResource(R.drawable.ic_step_dot_current);
                    progressDots[1].setImageResource(R.drawable.ic_step_dot_todo);
                    progressDots[2].setImageResource(R.drawable.ic_step_dot_todo);
                    progressDots[3].setImageResource(R.drawable.ic_step_dot_todo);

                    layoutStatus.setVisibility(View.VISIBLE);
                    imgStatus.setVisibility(View.GONE);
                    btnContinue.setVisibility(View.GONE);

                    textLabelCurrent.setText("Current:");
                    textTempCurrent.setText("");
                    break;

                case STATE_READY_TO_COOK:
                    textStatusName.setText("READY TO COOK");
                    progressDots[0].setImageResource(R.drawable.ic_step_dot_done);
                    progressDots[1].setImageResource(R.drawable.ic_step_dot_current);
                    progressDots[2].setImageResource(R.drawable.ic_step_dot_todo);
                    progressDots[3].setImageResource(R.drawable.ic_step_dot_todo);

                    layoutStatus.setVisibility(View.GONE);
                    imgStatus.setVisibility(View.VISIBLE);
                    btnContinue.setVisibility(View.VISIBLE);

                    imgStatus.setImageResource(R.drawable.img_ready_to_cook);

                    circle.setGridValue(1.0f);
                    break;

                case STATE_COOKING:
                    textStatusName.setText("COOKING");
                    progressDots[0].setImageResource(R.drawable.ic_step_dot_done);
                    progressDots[1].setImageResource(R.drawable.ic_step_dot_done);
                    progressDots[2].setImageResource(R.drawable.ic_step_dot_current);
                    progressDots[3].setImageResource(R.drawable.ic_step_dot_todo);

                    layoutStatus.setVisibility(View.VISIBLE);
                    imgStatus.setVisibility(View.GONE);
                    btnContinue.setVisibility(View.GONE);

                    textTempTarget.setText(((ParagonMainActivity) getActivity()).getTargetTemp() + "℉");
                    textLabelCurrent.setText("Food ready at");
                    textTempCurrent.setText("");

                    circle.setGridValue(1.0f);
                    break;

                case STATE_DONE:
                    textStatusName.setText("DONE");
                    progressDots[0].setImageResource(R.drawable.ic_step_dot_done);
                    progressDots[1].setImageResource(R.drawable.ic_step_dot_done);
                    progressDots[2].setImageResource(R.drawable.ic_step_dot_done);
                    progressDots[3].setImageResource(R.drawable.ic_step_dot_current);

                    layoutStatus.setVisibility(View.VISIBLE);
                    imgStatus.setVisibility(View.GONE);
                    btnContinue.setVisibility(View.GONE);

                    textTempTarget.setText(((ParagonMainActivity) getActivity()).getTargetTemp() + "℉");
                    textLabelCurrent.setText("Food is");
                    textTempCurrent.setText("READY");

                    circle.setGridValue(1.0f);
                    break;

                default:
                    break;
            }

        }
        else{
            //do nothing.
        }


    }

    /**
     * Update UI current temperature compare with set temperature.r
     */
    public void updateUiCurrentTemp() {
        ParagonMainActivity activity = (ParagonMainActivity) getActivity();

        if (cookState == COOK_STATE.STATE_PREHEAT) {
            textTempCurrent.setText(Math.round(activity.getCurrentTemp()) + "℉");
            textTempTarget.setText("Target: " + activity.getTargetTemp() + "℉");

            float ratioTemp = activity.getCurrentTemp() / activity.getTargetTemp();

            ratioTemp = Math.min(ratioTemp, 1.0f);
            circle.setGridValue(ratioTemp);
        }
        else {
            //do nothing.
        }
    }


    public void updateUiElapsedTime() {

        if (cookState == COOK_STATE.STATE_COOKING) {

        }
        else {
            //do nothing
        }
    }


}
