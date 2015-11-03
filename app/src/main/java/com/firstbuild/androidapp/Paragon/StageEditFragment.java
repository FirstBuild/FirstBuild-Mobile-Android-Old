package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.androidapp.paragon.dataModel.StageInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class StageEditFragment extends Fragment {

    private String TAG = StageEditFragment.class.getSimpleName();

    private TextView textTime;
    private TextView textTemp;
    private TextView textSpeed;
    private EditText editDirection;

    private View layoutPickerTime;
    private View layoutPickerTemp;
    private View layoutPickerSpeed;

    private NumberPicker pickerTimeHour;
    private NumberPicker pickerTimeMin;
    private NumberPicker pickerTemp;
    private NumberPicker pickerSpeed;
    private Switch switchAutoTransition;
    private ParagonMainActivity attached = null;


    public StageEditFragment() {
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stage_edit, container, false);

        textTime = (TextView) view.findViewById(R.id.text_time);
        textTemp = (TextView) view.findViewById(R.id.text_temp);
        textSpeed = (TextView) view.findViewById(R.id.text_speed);
        editDirection = (EditText) view.findViewById(R.id.edit_directions);
        switchAutoTransition = (Switch) view.findViewById(R.id.switch_auto_transition);

        layoutPickerTime = view.findViewById(R.id.layout_picker_time);
        layoutPickerTemp = view.findViewById(R.id.layout_picker_temp);
        layoutPickerSpeed = view.findViewById(R.id.layout_picker_speed);

        pickerTimeHour = (NumberPicker) view.findViewById(R.id.picker_hour);
        pickerTimeMin = (NumberPicker) view.findViewById(R.id.picker_min);
        pickerTemp = (NumberPicker) view.findViewById(R.id.picker_temp);
        pickerSpeed = (NumberPicker) view.findViewById(R.id.picker_speed);

        textTime.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPickerTime.setVisibility(View.VISIBLE);
                layoutPickerTemp.setVisibility(View.GONE);
                layoutPickerSpeed.setVisibility(View.GONE);
            }
        });

        textTemp.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPickerTime.setVisibility(View.GONE);
                layoutPickerTemp.setVisibility(View.VISIBLE);
                layoutPickerSpeed.setVisibility(View.GONE);
            }
        });

        textSpeed.setOnClickListener(new TextView.OnClickListener() {
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
                onClickAddStage();
            }
        });


        view.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSave();
                attached.getFragmentManager().popBackStack();
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


        int index = RecipeManager.getInstance().getCurrentStageIndex();

        if(index == RecipeManager.INVALID_INDEX){
            attached.setTitle("New Stage");
        }
        else{
            attached.setTitle("Stage " + (index+1));
        }

        updateUi();

        return view;
    }


    private void updateUi() {
        StageInfo stageInfo = RecipeManager.getInstance().getCurrentStage();

        if (stageInfo == null) {
            // case for creating new stage.

            setInitialValues();
        }
        else {
            // case for modifying exist stage.

            pickerTimeHour.setValue(stageInfo.getTime() / 60);
            pickerTimeMin.setValue(stageInfo.getTime() % 60);
            pickerTemp.setValue(stageInfo.getTemp());
            pickerSpeed.setValue(stageInfo.getSpeed());
            switchAutoTransition.setChecked(stageInfo.isAutoTransition());
            editDirection.setText(stageInfo.getDirection());

            makeTimeText(pickerTimeHour.getValue(), pickerTimeMin.getValue());
            makeTempText(pickerTemp.getValue());
            makeSpeedText(pickerSpeed.getValue());
        }


    }


    private void onClickSave() {
        StageInfo stage = RecipeManager.getInstance().getCurrentStage();

        int time = pickerTimeHour.getValue() * 60 + pickerTimeMin.getValue();
        int temp = pickerTemp.getValue();
        int speed = pickerSpeed.getValue();
        boolean autoTransition = switchAutoTransition.isChecked();
        String direction = editDirection.getText().toString();

        if (stage == null) {
            // case for creating new stage.

            stage = new StageInfo(time, temp, speed, autoTransition, direction);

            RecipeManager.getInstance().getCurrentRecipe().addStage(stage);
        }
        else {
            // case for modifying exist stage.

            stage.setTime(time);
            stage.setTemp(temp);
            stage.setSpeed(speed);
            stage.setAutoTransition(autoTransition);
            stage.setDirection(direction);
        }

    }


    private void onClickAddStage() {
        onClickSave();

        // Reset current values of UI components.
        setInitialValues();

        // Reset current stage.
        RecipeManager.getInstance().setCurrentStage(RecipeManager.INVALID_INDEX);
    }


    private void setInitialValues() {
        attached.setTitle("New Stage");

        pickerTimeHour.setValue(0);
        pickerTimeMin.setValue(30);
        pickerTemp.setValue(100);
        pickerSpeed.setValue(10);
        switchAutoTransition.setChecked(false);
        editDirection.setText("");

        makeTimeText(pickerTimeHour.getValue(), pickerTimeMin.getValue());
        makeTempText(pickerTemp.getValue());
        makeSpeedText(pickerSpeed.getValue());

        layoutPickerTime.setVisibility(View.GONE);
        layoutPickerTemp.setVisibility(View.GONE);
        layoutPickerSpeed.setVisibility(View.GONE);
    }


    private void makeTempText(int temp) {
        textTemp.setText(temp + "℉");
    }


    private void makeTimeText(int hour, int min) {
        textTime.setText(hour + "H:" + min + "M");
    }


    private void makeSpeedText(int speed) {
        textSpeed.setText(speed + "");
    }


}
