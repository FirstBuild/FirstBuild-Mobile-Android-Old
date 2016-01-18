package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.commonframework.bleManager.BleManager;

import java.nio.ByteBuffer;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuickStartFragment extends Fragment {


    private View layoutPickerMin;
    private View layoutPickerMax;
    private View layoutPickerTemp;
    private TextView textTimeMin;
    private TextView textTimeMax;
    private TextView textTemp;
    private NumberPicker pickerMinHour;
    private NumberPicker pickerMinMin;
    private NumberPicker pickerMaxHour;
    private NumberPicker pickerMaxMin;
    private NumberPicker pickerTemp;
    private ParagonMainActivity attached = null;

    public QuickStartFragment() {
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quick_start, container, false);

        layoutPickerMin = view.findViewById(R.id.layout_picker_min);
        layoutPickerMax = view.findViewById(R.id.layout_picker_max);
        layoutPickerTemp = view.findViewById(R.id.layout_picker_temp);

        textTimeMin = (TextView) view.findViewById(R.id.text_time_min);
        textTimeMax = (TextView) view.findViewById(R.id.text_time_max);
        textTemp = (TextView) view.findViewById(R.id.text_temp);

        pickerMinHour = (NumberPicker) view.findViewById(R.id.picker_min_hour);
        pickerMinMin = (NumberPicker) view.findViewById(R.id.picker_min_min);
        pickerMaxHour = (NumberPicker) view.findViewById(R.id.picker_max_hour);
        pickerMaxMin = (NumberPicker) view.findViewById(R.id.picker_max_min);
        pickerTemp = (NumberPicker) view.findViewById(R.id.picker_temp);

        layoutPickerMin.setVisibility(View.GONE);
        layoutPickerMax.setVisibility(View.GONE);
        layoutPickerTemp.setVisibility(View.GONE);

        textTimeMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentValue = (String) textTimeMin.getText();

                String hourValue = currentValue.split("H:")[0];
                String minValue = currentValue.split("H:")[1].split("M")[0];

                pickerMinHour.setValue(Integer.parseInt(hourValue));
                pickerMinMin.setValue(Integer.parseInt(minValue));

                layoutPickerMin.setVisibility(View.VISIBLE);
                layoutPickerMax.setVisibility(View.GONE);
                layoutPickerTemp.setVisibility(View.GONE);
            }
        });

        textTimeMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentValue = (String) textTimeMax.getText();

                String hourValue = currentValue.split("H:")[0];
                String minValue = currentValue.split("H:")[1].split("M")[0];

                pickerMinHour.setValue(Integer.parseInt(hourValue));
                pickerMinMin.setValue(Integer.parseInt(minValue));

                layoutPickerMin.setVisibility(View.GONE);
                layoutPickerMax.setVisibility(View.VISIBLE);
                layoutPickerTemp.setVisibility(View.GONE);
            }
        });

        textTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentValue = (String) textTemp.getText();

                String tempValue = currentValue.split("℉")[0];

                pickerTemp.setValue(Integer.parseInt(tempValue));

                layoutPickerMin.setVisibility(View.GONE);
                layoutPickerMax.setVisibility(View.GONE);
                layoutPickerTemp.setVisibility(View.VISIBLE);
            }
        });


        pickerMinHour.setMinValue(0);
        pickerMinHour.setMaxValue(12);
        pickerMinHour.setWrapSelectorWheel(false);
        pickerMinHour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerMinHour.setValue(0);

        pickerMinMin.setMinValue(0);
        pickerMinMin.setMaxValue(59);
        pickerMinMin.setWrapSelectorWheel(false);
        pickerMinMin.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerMinMin.setValue(30);

        pickerMaxHour.setMinValue(0);
        pickerMaxHour.setMaxValue(12);
        pickerMaxHour.setWrapSelectorWheel(false);
        pickerMaxHour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerMaxHour.setValue(0);

        pickerMaxMin.setMinValue(0);
        pickerMaxMin.setMaxValue(59);
        pickerMaxMin.setWrapSelectorWheel(false);
        pickerMaxMin.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerMaxMin.setValue(0);

        pickerTemp.setMinValue(ParagonValues.QUICK_MIN_TEMP);
        pickerTemp.setMaxValue(ParagonValues.QUICK_MAX_TEMP);
        pickerTemp.setWrapSelectorWheel(false);
        pickerTemp.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerTemp.setValue(ParagonValues.QUICK_MIN_TEMP);


        pickerMinHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                makeTempMinText(newVal, pickerMinMin.getValue());
            }
        });

        pickerMinMin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                makeTempMinText(pickerMinHour.getValue(), newVal);
            }
        });

        pickerMaxHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                makeTempMaxText(newVal, pickerMaxMin.getValue());
            }
        });

        pickerMaxMin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                makeTempMaxText(pickerMaxHour.getValue(), newVal);
            }
        });

        pickerTemp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                makeTempText(newVal);
            }
        });


        view.findViewById(R.id.btn_continue).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                attached.checkGoodToGo();
            }
        });

        return view;
    }

    public void goodToGo() {

        RecipeManager.getInstance().getCurrentStage().setTime(pickerMinHour.getValue() * 60 + pickerMinMin.getValue());
        RecipeManager.getInstance().getCurrentStage().setMaxTime(pickerMaxHour.getValue() * 60 + pickerMaxMin.getValue());
        RecipeManager.getInstance().getCurrentStage().setTemp(pickerTemp.getValue());
        RecipeManager.getInstance().getCurrentStage().setSpeed(10);
        RecipeManager.getInstance().sendCurrentStages();

        attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_GETREADY);
    }

    private void makeTempText(int temp) {
        textTemp.setText(temp + "℉");
    }


    private void makeTempMaxText(int hour, int min) {
        textTimeMax.setText(hour + "H:" + min + "M");
    }


    private void makeTempMinText(int hour, int min) {
        textTimeMin.setText(hour + "H:" + min + "M");
    }


}
