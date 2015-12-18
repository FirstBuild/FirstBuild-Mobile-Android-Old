package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private final int DONENESS_R  = 0;
    private final int DONENESS_MR = 1;
    private final int DONENESS_M  = 2;
    private final int DONENESS_MW = 3;
    private final int DONENESS_W  = 4;


    private TextView textThickness;
    private SeekBar  seekBarThickness;
    private TextView textDoneness;
    private SeekBar  seekBarDoneness;
    private float    setThickness;
    private int      setDoneness;
    private float setTargetTemp = 0.0f;
    private float setTargetTimeHour = 0;
    private float setTargetTimeMax = 0;

    private TextView textSetTimeMin;
    private TextView textSetTimeMax;
    private TextView textSetTemp;
    private ParagonMainActivity attached = null;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attached = (ParagonMainActivity)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sousvide_settings, container, false);

        textSetTimeMin = (TextView) view.findViewById(R.id.text_set_time_min);
        textSetTimeMax = (TextView) view.findViewById(R.id.text_set_time_max);
        textSetTemp = (TextView) view.findViewById(R.id.text_set_temp);

        textThickness = (TextView) view.findViewById(R.id.text_status_name);
        seekBarThickness = (SeekBar) view.findViewById(R.id.seekbar_thickness);
        seekBarThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String selectedValue = getResources().getStringArray(R.array.string_thickness)[progress];

                setThickness = Float.parseFloat(selectedValue);
                textThickness.setText(selectedValue + "\"");

                calculateTimeTemp();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }


            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarThickness.setProgress(1);

        textDoneness = (TextView) view.findViewById(R.id.text_doneness);
        seekBarDoneness = (SeekBar) view.findViewById(R.id.seekbar_doneness);
        seekBarDoneness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String selectedValue = getResources().getStringArray(R.array.string_doneness)[progress];

                setDoneness = progress;
                textDoneness.setText(selectedValue);

                calculateTimeTemp();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarDoneness.setProgress(1);

        view.findViewById(R.id.btn_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeManager.getInstance().getCurrentStage().setTime((int)(setTargetTimeHour *60));
                RecipeManager.getInstance().getCurrentStage().setMaxTime((int)(setTargetTimeMax*60));
                RecipeManager.getInstance().getCurrentStage().setTemp((int) setTargetTemp);
                RecipeManager.getInstance().sendCurrentStages();

                ((ParagonMainActivity) getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_GETREADY);
            }
        });

        view.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        initRecipeSetting();


        return view;
    }

    /**
     * Create recipe temprorally and put selected values.
     */
    private void initRecipeSetting() {
        // Now create a sousvide recipe and pointing the recipe as currentRecipe.
        RecipeManager.getInstance().createRecipeSousVide();


    }

    /**
     * This table for Round
     Top Round
     Bottom Round
     Blade (Flat Iron)
     Cubed Flank Skirt Tip
     */
    private void calculateTimeTemp() {

        switch (setDoneness) {
            case DONENESS_R:
                setTargetTemp = 0;
                setTargetTimeHour = 0;
                setTargetTimeMax = 0;
                break;

            case DONENESS_MR:
                setTargetTemp = 0;
                setTargetTimeHour = 0;
                setTargetTimeMax = 0;
                break;

            case DONENESS_M:
                setTargetTemp = 140;

                if (setThickness <= 0.75) {
                    setTargetTimeHour = 6;
                    setTargetTimeMax = 12;
                }
                else if (0.75 < setThickness && setThickness <= 1.25) {
                    setTargetTimeHour = 8;
                    setTargetTimeMax = 24;
                }
                else  { //if (1.25 < setThickness && setThickness <= 2)
                    setTargetTimeHour = 12;
                    setTargetTimeMax = 30;
                }
                break;

            case DONENESS_MW:
                setTargetTemp = 150;

                if (setThickness <= 0.75) {
                    setTargetTimeHour = 6;
                    setTargetTimeMax = 12;
                }
                else if (0.75 < setThickness && setThickness <= 1.25) {
                    setTargetTimeHour = 8;
                    setTargetTimeMax = 24;
                }
                else  { //if (1.25 < setThickness && setThickness <= 2)
                    setTargetTimeHour = 12;
                    setTargetTimeMax = 30;
                }
                break;

            case DONENESS_W:
                setTargetTemp = 160;

                if (setThickness <= 0.75) {
                    setTargetTimeHour = 4;
                    setTargetTimeMax = 6;
                }
                else if (0.75 < setThickness && setThickness <= 1.25) {
                    setTargetTimeHour = 6;
                    setTargetTimeMax = 10;
                }
                else  { //if (1.25 < setThickness && setThickness <= 2)
                    setTargetTimeHour = 8;
                    setTargetTimeMax = 12;
                }
                break;

        }


        if(setTargetTemp == 0 && setTargetTimeMax == 0 && setTargetTimeHour == 0){

            textSetTemp.setText("--");
            textSetTimeMin.setText(Html.fromHtml("--" + "<small>H : </small>" + "--" + "<small>M</small>"));
            textSetTimeMax.setText(Html.fromHtml("--" + "<small>H : </small>" + "--" + "<small>M</small>"));

            new MaterialDialog.Builder(attached)
                    .content("Not recommended setting")
                    .positiveText("Ok")
                    .cancelable(true).show();
        }
        else{
            textSetTemp.setText(Html.fromHtml(setTargetTemp + "<small>℉</small>"));

            int timeH = (int) Math.floor(setTargetTimeHour);
            int timeM = (int)((setTargetTimeHour - timeH)*60);

            String hour = timeH + "";
            String minutes = String.format("%02d", timeM);
            textSetTimeMin.setText(Html.fromHtml(hour + "<small>H : </small>" + minutes + "<small>M</small>"));

            timeH = (int) Math.floor(setTargetTimeMax);
            timeM = (int)((setTargetTimeMax - timeH)*60);

            hour = timeH + "";
            minutes = String.format("%02d", timeM);
            textSetTimeMax.setText(Html.fromHtml(hour + "<small>H : </small>" + minutes + "<small>M</small>"));
        }
    }



}
