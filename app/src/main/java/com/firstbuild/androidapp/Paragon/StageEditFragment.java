package com.firstbuild.androidapp.paragon;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StageEditFragment extends Fragment {

    private String TAG = StageEditFragment.class.getSimpleName();

    private TextView textTime;
    private TextView textTemp;
    private TextView textSpeed;
    private EditText editDirection;

    private NumberPicker pickerTimeHour;
    private NumberPicker pickerTimeMin;
    private NumberPicker pickerTemp;
    private NumberPicker pickerSpeed;


    public StageEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_stage_edit, container, false);

        textTime = (TextView) view.findViewById(R.id.text_time);
        textTemp = (TextView) view.findViewById(R.id.text_temp);
        textSpeed = (TextView) view.findViewById(R.id.text_speed);
        editDirection = (EditText) view.findViewById(R.id.edit_directions);

        final View layoutPickerTime = view.findViewById(R.id.layout_picker_time);
        final View layoutPickerTemp = view.findViewById(R.id.layout_picker_temp);
        final View layoutPickerSpeed = view.findViewById(R.id.layout_picker_speed);

        pickerTimeHour = (NumberPicker) view.findViewById(R.id.picker_hour);
        pickerTimeMin = (NumberPicker) view.findViewById(R.id.picker_min);
        pickerTemp = (NumberPicker) view.findViewById(R.id.picker_temp);
        pickerSpeed = (NumberPicker) view.findViewById(R.id.picker_speed);

        textTime.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View v) {
                layoutPickerTime.setVisibility(View.VISIBLE);
                layoutPickerTemp.setVisibility(View.GONE);
                layoutPickerSpeed.setVisibility(View.GONE);
            }
        });

        textTemp.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View v) {
                layoutPickerTime.setVisibility(View.GONE);
                layoutPickerTemp.setVisibility(View.VISIBLE);
                layoutPickerSpeed.setVisibility(View.GONE);
            }
        });

        textSpeed.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View v) {
                layoutPickerTime.setVisibility(View.GONE);
                layoutPickerTemp.setVisibility(View.GONE);
                layoutPickerSpeed.setVisibility(View.VISIBLE);
            }
        });


        editDirection.setOnClickListener(new EditText.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPickerTime.setVisibility(View.GONE);
                layoutPickerTemp.setVisibility(View.GONE);
                layoutPickerSpeed.setVisibility(View.GONE);
            }
        });


        view.findViewById(R.id.btn_add_stage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        view.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        pickerTimeHour.setMinValue(0);
        pickerTimeHour.setMaxValue(12);
        pickerTimeHour.setWrapSelectorWheel(true);
        pickerTimeHour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerTimeHour.setValue(0);

        pickerTimeMin.setMinValue(0);
        pickerTimeMin.setMaxValue(59);
        pickerTimeMin.setWrapSelectorWheel(true);
        pickerTimeMin.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerTimeMin.setValue(30);

        pickerTemp.setMinValue(50);
        pickerTemp.setMaxValue(200);
        pickerTemp.setWrapSelectorWheel(true);
        pickerTemp.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerTemp.setValue(100);

        pickerSpeed.setMinValue(1);
        pickerSpeed.setMaxValue(10);
        pickerSpeed.setWrapSelectorWheel(true);
        pickerSpeed.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickerSpeed.setValue(5);

        pickerTimeHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                makeTimeText(newVal, pickerTimeMin.getValue());
            }
        });

        pickerTimeMin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                makeTimeText(pickerTimeHour.getValue(), newVal);
            }
        });

        pickerTemp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                makeTempText(newVal);
            }
        });

        pickerSpeed.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.d(TAG, "speed changed " + newVal);
                makeSpeedText(newVal);
            }
        });


        setInitialValues();

        return view;
    }


    private void setInitialValues() {
        pickerTimeHour.setValue(0);
        pickerTimeMin.setValue(30);
        pickerTemp.setValue(100);
        pickerSpeed.setValue(10);

    }


    private void makeTempText(int temp) {
        textTemp.setText(temp + "℉");
    }


    private void makeTimeText(int hour, int min) {
        textTime.setText(hour + "H:" + min + "M");
    }


    private void makeSpeedText(int speed) {
        textSpeed.setText(speed);
    }


}
