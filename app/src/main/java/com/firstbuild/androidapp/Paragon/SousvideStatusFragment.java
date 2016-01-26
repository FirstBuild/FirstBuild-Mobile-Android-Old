package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.androidapp.paragon.dataModel.StageInfo;
import com.firstbuild.androidapp.productManager.ProductInfo;
import com.firstbuild.androidapp.productManager.ProductManager;
import com.firstbuild.viewUtil.gridCircleView;

import java.nio.ByteBuffer;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class SousvideStatusFragment extends Fragment {

    private String TAG = "SousvideStatusFragment";
    private int countDown = 10;

    public enum COOK_STATE {
        STATE_NONE,
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
    private TextView textExplanation;
    private View btnContinue;
    private View btnComplete;
    private COOK_STATE cookState = COOK_STATE.STATE_NONE;

    private ParagonMainActivity attached = null;

    public SousvideStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        attached = (ParagonMainActivity)getActivity();
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
        textExplanation = (TextView) view.findViewById(R.id.text_explanation);

        textExplanation.setVisibility(View.GONE);

        layoutStatus = view.findViewById(R.id.layout_status);
        imgStatus = (ImageView) view.findViewById(R.id.img_status);

        layoutStatus.setVisibility(View.VISIBLE);
        imgStatus.setVisibility(View.GONE);

        btnContinue = view.findViewById(R.id.btn_continue);
        btnContinue.setVisibility(View.GONE);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ByteBuffer valueBuffer = ByteBuffer.allocate(1);

                valueBuffer.put((byte) 0x01);
//                BleManager.getInstance().writeCharateristics(ParagonValues.CHARACTERISTIC_START_HOLD_TIMER, valueBuffer.array());

                UpdateUiCookState(COOK_STATE.STATE_COOKING);

            }
        });

        btnComplete = view.findViewById(R.id.btn_complete);
        btnComplete.setVisibility(View.GONE);
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ParagonMainActivity) getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_COMPLETE);
            }
        });


        updateUiCurrentTemp();

        UpdateUiCookState(COOK_STATE.STATE_PREHEAT);

        return view;
    }


    /**
     * Update cooking state, Off -> Heating -> Ready -> Cooking -> Done
     *
     */
    public void updateCookState() {
        byte state = ProductManager.getInstance().getCurrent().getErdCookState();
        Log.d(TAG, "updateCookState IN " + state);

        switch (state) {
            case ParagonValues.COOK_STATE_OFF:
                attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_COOKING_MODE);
                break;

            case ParagonValues.COOK_STATE_HEATING:
                UpdateUiCookState(COOK_STATE.STATE_PREHEAT);
                break;

            case ParagonValues.COOK_STATE_READY:
                UpdateUiCookState(COOK_STATE.STATE_READY_TO_COOK);
                break;

            case ParagonValues.COOK_STATE_COOKING:
                UpdateUiCookState(COOK_STATE.STATE_COOKING);
                break;

            case ParagonValues.COOK_STATE_DONE:
                UpdateUiCookState(COOK_STATE.STATE_DONE);
                break;

            default:
                Log.d(TAG, "Error in onCookState :" + state);
                break;
        }

    }


    /**
     * Update UI progress bar top of the screen and on circle view.
     */
    public void UpdateUiCookState(COOK_STATE state) {
        Log.d(TAG, "UpdateUiCookState " + state);

        StageInfo stageInfo = ProductManager.getInstance().getCurrent().getErdRecipeConfig().getStage(0);

        if (cookState != state) {
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
                    btnComplete.setVisibility(View.GONE);

                    textLabelCurrent.setText("Current:");
                    textTempTarget.setText(Html.fromHtml("Target: " + stageInfo.getTemp() + "<small>℉</small>"));
                    textExplanation.setVisibility(View.GONE);
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
                    btnComplete.setVisibility(View.GONE);

                    textTempTarget.setText(Html.fromHtml("Target: " + stageInfo.getTemp() + "<small>℉</small>"));
                    imgStatus.setImageResource(R.drawable.img_ready_to_cook);

                    circle.setGridValue(1.0f);

                    textExplanation.setVisibility(View.VISIBLE);
                    textExplanation.setText(R.string.fragment_soudvide_status_explanation_placefood);
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
                    btnComplete.setVisibility(View.GONE);

                    textTempTarget.setText(Html.fromHtml(stageInfo.getTemp() + "<small>℉</small>"));

                    textLabelCurrent.setText("Food ready at");

                    textTempCurrent.setText("");

                    circle.setGridValue(1.0f);

                    textExplanation.setVisibility(View.GONE);
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
                    btnComplete.setVisibility(View.VISIBLE);

                    textTempTarget.setText(Html.fromHtml(stageInfo.getTemp() + "<small>℉</small>"));
                    textLabelCurrent.setText("Food is");
                    textTempCurrent.setText("READY");

                    circle.setGridValue(1.0f);

                    textExplanation.setVisibility(View.VISIBLE);
                    textExplanation.setText(R.string.fragment_soudvide_status_explanation_donekeep);
                    break;

                default:
                    break;
            }

        }
        else {
            //do nothing.
        }


    }

    /**
     * Update UI current temperature compare with set temperature.r
     */
    public void updateUiCurrentTemp() {
        StageInfo stageInfo = ProductManager.getInstance().getCurrent().getErdRecipeConfig().getStage(0);

        if (cookState == COOK_STATE.STATE_PREHEAT) {
            float currentTemp = ProductManager.getInstance().getCurrent().getErdCurrentTemp();
            textTempCurrent.setText(Math.round(currentTemp) + "℉");

            float ratioTemp = currentTemp / (float)stageInfo.getTemp();

            ratioTemp = Math.min(ratioTemp, 1.0f);
            circle.setGridValue(ratioTemp);
        }
        else {
            //do nothing.
        }
    }


    /**
     * Update UI current elapsed time. Elapsed time is increase from 0 until selected cook time.
     *
     */
    public void updateUiElapsedTime() {

        ProductInfo productInfo = ProductManager.getInstance().getCurrent();
        int elapsedTime = productInfo.getErdElapsedTime();

        Log.d(TAG, "updateUiElapsedTime :" + elapsedTime);
        StageInfo stageInfo = ProductManager.getInstance().getCurrent().getErdRecipeConfig().getStage(0);

        if (cookState == COOK_STATE.STATE_COOKING) {
            float ratioTime = (float) elapsedTime / (float) stageInfo.getTime();

            ratioTime = Math.min(ratioTime, 1.0f);

            circle.setBarValue(1.0f - ratioTime);

            updateReadyTime(elapsedTime);
        }
        else {
            //do nothing
        }
    }


    /**
     * Update new stage get from BLE master.
     */
    public void updateCookStage() {
        Log.d(TAG, "updateCookStage. Error sousvide is support only one stage.");
//        int newStage = ProductManager.getInstance().getCurrent().getErdCookStage();
//        RecipeManager.getInstance().setCurrentStage(newStage-1);
    }


    private void updateReadyTime(int minute){
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, minute);
        String timeText = String.format( "%d:%02d", now.get(Calendar.HOUR), now.get(Calendar.MINUTE));
        String ampm = "";

        if(now.get(Calendar.AM_PM) == Calendar.AM){
            ampm = "AM";
        }
        else{
            ampm = "PM";
        }

        textTempCurrent.setText(Html.fromHtml(timeText + "<small>"+ampm+"</small>"));
    }


}
