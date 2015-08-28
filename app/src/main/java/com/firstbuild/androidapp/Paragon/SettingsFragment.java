package com.firstbuild.androidapp.Paragon;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {


    private TextView textThickness;
    private SeekBar  seekBarThickness;
    private TextView textDoneness;
    private SeekBar  seekBarDoneness;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sousvide_settings, container, false);

        textThickness = (TextView) view.findViewById(R.id.text_thickness);
        seekBarThickness = (SeekBar) view.findViewById(R.id.seekbar_thickness);
        seekBarThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String selectedValue = getResources().getStringArray(R.array.string_thickness)[progress];
                textThickness.setText(selectedValue);
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
                textDoneness.setText(selectedValue);
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
                ((ParagonMainActivity) getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_GETREADY);
            }
        });

        view.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        return view;
    }


}
