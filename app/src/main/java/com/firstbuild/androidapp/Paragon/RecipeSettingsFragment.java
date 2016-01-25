package com.firstbuild.androidapp.paragon;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.BuiltInRecipeSettingsInfo;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecipeSettingsFragment extends Fragment {

    private final int DONENESS_R = 0;
    private final int DONENESS_MR = 1;
    private final int DONENESS_M = 2;
    private final int DONENESS_MW = 3;
    private final int DONENESS_W = 4;
    private final float INTERVAL_THICKNESS = 0.25f;
    private final String TAG = RecipeSettingsFragment.class.getSimpleName();
    private TextView textThickness;
    private SeekBar seekBarThickness;
    private TextView textDoneness;
    private SeekBar seekBarDoneness;
    private float setThickness;
    private String setDoneness;
    private float setTargetTemp = 0.0f;
    private float setTargetTimeMin = 0;
    private float setTargetTimeMax = 0;
    private TextView textSetTimeMin;
    private TextView textSetTimeMax;
    private TextView textSetTemp;
    private ParagonMainActivity attached = null;

    public RecipeSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attached = (ParagonMainActivity) activity;
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


        if (attached.selectedBuiltInRecipe.thickness.isEmpty()) {
            view.findViewById(R.id.layout_thickness).setVisibility(View.GONE);
        }
        else {
            int numThickness = attached.selectedBuiltInRecipe.thickness.size();
            float minThickness = attached.selectedBuiltInRecipe.thickness.get(0);
            float maxThickness = attached.selectedBuiltInRecipe.thickness.get(numThickness - 1);

            seekBarThickness.setMax((int) ((maxThickness - minThickness) / INTERVAL_THICKNESS));
        }

        seekBarThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                String selectedValue = getResources().getStringArray(R.array.string_thickness)[progress];

                // Min thickness is 0.25, we have grid on every 0.25 thickness on seekbar.
                setThickness = (progress * 0.25f) + 0.25f;
                textThickness.setText(setThickness + "\"");

                calculateTimeTemp();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }


            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarThickness.setProgress(0);

        textDoneness = (TextView) view.findViewById(R.id.text_doneness);
        seekBarDoneness = (SeekBar) view.findViewById(R.id.seekbar_doneness);
        int numDoneness = attached.selectedBuiltInRecipe.doneness.size();

        if (numDoneness < 2) {
            view.findViewById(R.id.layout_doneness).setVisibility(View.GONE);
        }
        else {
            seekBarDoneness.setMax(attached.selectedBuiltInRecipe.doneness.size() - 1);
        }

        seekBarDoneness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setDoneness = attached.selectedBuiltInRecipe.doneness.get(progress);

                textDoneness.setText(setDoneness);

                calculateTimeTemp();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarDoneness.setProgress(0);

        view.findViewById(R.id.btn_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(setTargetTemp < ParagonValues.WARNING_TEMPERATURE &&
                        !attached.isShowFoodWarning()){

                    new MaterialDialog.Builder(getActivity())
                            .title("Reminder")
                            .content("Cooking below 140℉ increases your risks of foodborne illness")
                            .positiveText("Ok")
                            .neutralColor(R.color.colorParagonSecondaryText)
                            .neutralText("Don't show again")
                            .cancelable(false)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    attached.checkGoodToGo();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    attached.checkGoodToGo();
                                }

                                @Override
                                public void onNeutral(MaterialDialog dialog) {
                                    attached.saveShowFoodWarning();
                                    attached.checkGoodToGo();
                                }
                            })
                            .show();
                }
                else{
                    attached.checkGoodToGo();
                }

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
     * This table for Round
     * Top Round
     * Bottom Round
     * Blade (Flat Iron)
     * Cubed Flank Skirt Tip
     */
    private void calculateTimeTemp() {

        BuiltInRecipeSettingsInfo recipeSettings = attached.selectedBuiltInRecipe;

        int indexThickness = 0;
        int indexDoneness = 0;

        for (int i = 0; i < recipeSettings.thickness.size() - 1; i++) {
            Log.d(TAG, "--" +setThickness +", "+recipeSettings.thickness.get(i)+", "+recipeSettings.thickness.get(i + 1));
            if (recipeSettings.thickness.get(i) <= setThickness &&
                    setThickness <= recipeSettings.thickness.get(i + 1)) {
                indexThickness = i;
                break;
            }
        }

        for (int i = 0; i < recipeSettings.doneness.size(); i++) {
            if (recipeSettings.doneness.get(i).equals(setDoneness)) {
                indexDoneness = i;
                break;
            }
        }

        Log.d(TAG, "calculateTimeTemp indexDoness :" + indexDoneness + ", indexThickness :" + indexThickness);
        BuiltInRecipeSettingsInfo.RecipeSetting settings = recipeSettings.getRecipeSetting(indexDoneness, indexThickness);

        setTargetTemp = settings.temp;
        setTargetTimeMax = settings.timeMax;
        setTargetTimeMin = settings.timeMin;


        if (setTargetTemp == 0 && setTargetTimeMax == 0.0f && setTargetTimeMin == 0.0f) {

            textSetTemp.setText("--");
            textSetTimeMin.setText(Html.fromHtml("--" + "<small>H : </small>" + "--" + "<small>M</small>"));
            textSetTimeMax.setText(Html.fromHtml("--" + "<small>H : </small>" + "--" + "<small>M</small>"));

            new MaterialDialog.Builder(attached)
                    .content("Not recommended setting")
                    .positiveText("Ok")
                    .cancelable(true).show();
        }
        else {
            textSetTemp.setText(Html.fromHtml(setTargetTemp + "<small>℉</small>"));

            int timeH = (int) Math.floor(setTargetTimeMin);
            int timeM = (int) ((setTargetTimeMin - timeH) * 60);

            String hour = timeH + "";
            String minutes = String.format("%02d", timeM);
            textSetTimeMin.setText(Html.fromHtml(hour + "<small>H : </small>" + minutes + "<small>M</small>"));

            timeH = (int) Math.floor(setTargetTimeMax);
            timeM = (int) ((setTargetTimeMax - timeH) * 60);

            hour = timeH + "";
            minutes = String.format("%02d", timeM);
            textSetTimeMax.setText(Html.fromHtml(hour + "<small>H : </small>" + minutes + "<small>M</small>"));
        }
    }

    /**
     * Create recipe temprorally and put selected values.
     */
    private void initRecipeSetting() {
        // Now create a sousvide recipe and pointing the recipe as currentRecipe.
        RecipeManager.getInstance().createRecipeSousVide();


    }


    /**
     * Go to next step. this is came from checkGoodToGo of ParagonMainActivity.
     */
    public void goodToGo() {
        RecipeManager.getInstance().getCurrentStage().setTime((int) (setTargetTimeMin * 60));
        RecipeManager.getInstance().getCurrentStage().setMaxTime((int) (setTargetTimeMax * 60));
        RecipeManager.getInstance().getCurrentStage().setTemp((int) setTargetTemp);
        RecipeManager.getInstance().sendCurrentStages();

        ((ParagonMainActivity) getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_GETREADY);

    }
}
