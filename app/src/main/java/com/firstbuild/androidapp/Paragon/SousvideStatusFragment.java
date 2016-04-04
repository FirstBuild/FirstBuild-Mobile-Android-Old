package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.StageInfo;
import com.firstbuild.androidapp.productManager.ProductInfo;
import com.firstbuild.androidapp.productManager.ProductManager;
import com.firstbuild.commonframework.bleManager.BleManager;
import com.firstbuild.viewUtil.gridCircleView;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class SousvideStatusFragment extends Fragment {

    private String TAG = "SousvideStatusFragment";
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
    private ParagonMainActivity attached = null;
    private byte previousCookState = (byte) 0xff;
    private float initialTemp = 0;

    public SousvideStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        attached = (ParagonMainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView IN");

        ((ParagonMainActivity) getActivity()).setTitle("Active");

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
                ProductInfo product = ProductManager.getInstance().getCurrent();

                valueBuffer.put((byte) 0x01);
                BleManager.getInstance().writeCharacteristics(product.bluetoothDevice, ParagonValues.CHARACTERISTIC_START_HOLD_TIMER, valueBuffer.array());

//                updateCookState(ParagonValues.COOK_STATE_COOKING);
                view.setVisibility(View.GONE);

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


        ProductInfo product = ProductManager.getInstance().getCurrent();
        previousCookState = product.getErdCookState();

        initialTemp = product.getErdCurrentTemp();

        updateCookState();
        updateUiCurrentTemp();
        updateUiElapsedTime();

        return view;
    }

    /**
     * Update cooking state, Off -> Heating -> Ready -> Cooking -> Done
     */
    public void updateCookState() {
        ProductInfo product = ProductManager.getInstance().getCurrent();
        byte state = product.getErdCookState();
        StageInfo stageInfo = product.getErdRecipeConfig().getStage(0);
        int time = 0;

        if(stageInfo != null){
            time = stageInfo.getTime();
        }

        Log.d(TAG, "updateCookState IN " + state);

        if(previousCookState != state){
            // for the batter UI on circle view.
            previousCookState = state;
            product.setErdElapsedTime(ProductInfo.INITIAL_ELAPSED_TIME);
        }
        else{
            //do nothing.
        }

        if(time == 0){
            progressDots[0].setVisibility(View.GONE);
            progressDots[1].setVisibility(View.GONE);
            progressDots[2].setVisibility(View.VISIBLE);
            progressDots[3].setVisibility(View.VISIBLE);
        }
        else{
            progressDots[0].setVisibility(View.VISIBLE);
            progressDots[1].setVisibility(View.VISIBLE);
            progressDots[2].setVisibility(View.VISIBLE);
            progressDots[3].setVisibility(View.VISIBLE);
        }


        switch (state) {
            case ParagonValues.COOK_STATE_OFF:
                attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_COOKING_MODE);
                break;

            case ParagonValues.COOK_STATE_HEATING:
                if (time == 0) {
                    progressDots[2].setImageResource(R.drawable.ic_step_dot_current);
                    progressDots[3].setImageResource(R.drawable.ic_step_dot_todo);
                }
                else{
                    progressDots[0].setImageResource(R.drawable.ic_step_dot_current);
                    progressDots[1].setImageResource(R.drawable.ic_step_dot_todo);
                    progressDots[2].setImageResource(R.drawable.ic_step_dot_todo);
                    progressDots[3].setImageResource(R.drawable.ic_step_dot_todo);
                }

                layoutStatus.setVisibility(View.VISIBLE);
                imgStatus.setVisibility(View.GONE);
                btnContinue.setVisibility(View.GONE);
                btnComplete.setVisibility(View.GONE);

                textLabelCurrent.setText("Current:");
                textTempTarget.setText(Html.fromHtml("Target: " + stageInfo.getTemp() + "<small>℉</small>"));
                textExplanation.setVisibility(View.GONE);

                updateUiCurrentTemp();
                break;

            case ParagonValues.COOK_STATE_READY:
                textStatusName.setText("READY TO COOK");
                if (time == 0) {
                    progressDots[2].setImageResource(R.drawable.ic_step_dot_current);
                    progressDots[3].setImageResource(R.drawable.ic_step_dot_todo);
                }
                else {
                    progressDots[0].setImageResource(R.drawable.ic_step_dot_done);
                    progressDots[1].setImageResource(R.drawable.ic_step_dot_current);
                    progressDots[2].setImageResource(R.drawable.ic_step_dot_todo);
                    progressDots[3].setImageResource(R.drawable.ic_step_dot_todo);
                }

                layoutStatus.setVisibility(View.GONE);
                imgStatus.setImageResource(R.drawable.img_ready_to_cook);
                imgStatus.setVisibility(View.VISIBLE);
                btnContinue.setVisibility(View.VISIBLE);
                btnComplete.setVisibility(View.GONE);

                textTempTarget.setText(Html.fromHtml("Target: " + stageInfo.getTemp() + "<small>℉</small>"));
                imgStatus.setImageResource(R.drawable.img_ready_to_cook);

                circle.setGridValue(1.0f);
                circle.setBarValue(0.0f);
                circle.setDashValue(0.0f);
                circle.setColor(R.color.colorParagonAccent);

                textExplanation.setVisibility(View.VISIBLE);
                textExplanation.setText(R.string.fragment_soudvide_status_explanation_placefood);
                break;

            case ParagonValues.COOK_STATE_COOKING:
                textStatusName.setText("COOKING");

                if(time == 0){
                    progressDots[2].setImageResource(R.drawable.ic_step_dot_done);
                    progressDots[3].setImageResource(R.drawable.ic_step_dot_current);
                    textLabelCurrent.setText("Current:");
                }
                else{
                    progressDots[0].setImageResource(R.drawable.ic_step_dot_done);
                    progressDots[1].setImageResource(R.drawable.ic_step_dot_done);
                    progressDots[2].setImageResource(R.drawable.ic_step_dot_current);
                    progressDots[3].setImageResource(R.drawable.ic_step_dot_todo);
                    textLabelCurrent.setText("Food ready at");
                }

                layoutStatus.setVisibility(View.VISIBLE);
                imgStatus.setVisibility(View.GONE);
                btnContinue.setVisibility(View.GONE);
                btnComplete.setVisibility(View.GONE);

                textTempTarget.setText(Html.fromHtml(stageInfo.getTemp() + "<small>℉</small>"));

                textTempCurrent.setText("");

                circle.setGridValue(1.0f);
                circle.setBarValue(0.0f);
                circle.setDashValue(0.0f);
                circle.setColor(R.color.colorParagonAccent);

                textExplanation.setVisibility(View.GONE);
                break;

            case ParagonValues.COOK_STATE_DONE:
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
                circle.setBarValue(1.0f);
                circle.setColor(R.color.colorParagonAccent);

                textExplanation.setVisibility(View.VISIBLE);
                textExplanation.setText("");
                break;

            default:
                Log.d(TAG, "Error in onCookState :" + state);
                break;
        }

    }



    /**
     * Update UI current temperature compare with set temperature.r
     */
    public void updateUiCurrentTemp() {
        StageInfo stageInfo = ProductManager.getInstance().getCurrent().getErdRecipeConfig().getStage(0);
        ProductInfo product = ProductManager.getInstance().getCurrent();
        byte state = product.getErdCookState();

        if (state == ParagonValues.COOK_STATE_HEATING)  {
            float currentTemp = product.getErdCurrentTemp();
            float targetTemp = stageInfo.getTemp();

            textTempCurrent.setText(Math.round(currentTemp) + "℉");

            if (currentTemp > targetTemp) {
                circle.setColor(R.color.colorAccent);
                textStatusName.setText("COOLING");
            }
            else {
                circle.setColor(R.color.colorParagonAccent);
                textStatusName.setText("PREHEATING");
            }

            float ratioTemp = (currentTemp - initialTemp) / (targetTemp - initialTemp);

            if(ratioTemp > 1.0f){
                ratioTemp = 1.0f - ratioTemp;
            }

            ratioTemp = Math.max(ratioTemp, 0.0f);

            circle.setGridValue(ratioTemp);
        }
        else if(state == ParagonValues.COOK_STATE_COOKING){
            circle.setColor(R.color.colorParagonAccent);
            if(stageInfo.getTime() == 0){
                float currentTemp = product.getErdCurrentTemp();
                textTempCurrent.setText(Math.round(currentTemp) + "℉");
            }

        }
        else {
            circle.setColor(R.color.colorParagonAccent);
        }
    }

    /**
     * Update UI current elapsed time. Elapsed time is increase from 0 until selected cook time.
     */
    public void updateUiElapsedTime() {

        ProductInfo productInfo = ProductManager.getInstance().getCurrent();
        int elapsedTime = productInfo.getErdElapsedTime();
        byte state = productInfo.getErdCookState();

        Log.d(TAG, "updateUiElapsedTime :" + elapsedTime);
        StageInfo stageInfo = ProductManager.getInstance().getCurrent().getErdRecipeConfig().getStage(0);

        if (state == ParagonValues.COOK_STATE_COOKING) {
            float ratioTime = (float) elapsedTime / (float) stageInfo.getTime();

            ratioTime = Math.min(ratioTime, 1.0f);

            circle.setBarValue(1.0f - ratioTime);

            textTempCurrent.setText(updateReadyTime(elapsedTime));

        }
        else if (state == ParagonValues.COOK_STATE_DONE) {
            float ratioTime = (float) elapsedTime / (float) stageInfo.getMaxTime();

            ratioTime = Math.min(ratioTime, 1.0f);

            circle.setDashValue(1.0f - ratioTime);

            if (elapsedTime == 0) {
                layoutStatus.setVisibility(View.GONE);
                imgStatus.setImageResource(R.drawable.img_cook_done);
                imgStatus.setVisibility(View.VISIBLE);

                textExplanation.setText("Take food out now");
            }
            else {
                layoutStatus.setVisibility(View.VISIBLE);
                imgStatus.setVisibility(View.GONE);

                CharSequence text = "Food can stay in until " + updateReadyTime(elapsedTime);
                textExplanation.setText(text);
            }
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
    }

    /**
     * Calculate end time base on current phone's time.
     * @param elapsedMin remaining time.
     * @return text of time.
     */
    private CharSequence updateReadyTime(int elapsedMin) {
        CharSequence stringTime = "";
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm aa");

        now.add(Calendar.MINUTE, elapsedMin);
        stringTime = sdf.format(now.getTime());

        return stringTime;
    }


    public void updateCookConfig() {
        ProductInfo product = ProductManager.getInstance().getCurrent();
        StageInfo stageInfo = product.getErdRecipeConfig().getStage(0);

        textTempTarget.setText(Html.fromHtml("Target: " + stageInfo.getTemp() + "<small>℉</small>"));
    }

}
