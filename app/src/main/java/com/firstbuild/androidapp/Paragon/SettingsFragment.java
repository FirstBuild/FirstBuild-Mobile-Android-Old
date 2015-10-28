package com.firstbuild.androidapp.paragon;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.commonframework.bleManager.BleManager;

import java.nio.ByteBuffer;

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
    private int setTargetTimeMin = 0;
    private int setTargetTimeMax = 0;

    private TextView textSetTimeMin;
    private TextView textSetTimeMax;
    private TextView textSetTemp;

    public SettingsFragment() {
        // Required empty public constructor
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

                setThickness = progress;
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
                ParagonMainActivity activity = (ParagonMainActivity) getActivity();

                activity.setTargetTime(setTargetTimeMin, setTargetTimeMax);
                activity.setTargetTemp(setTargetTemp);

                ByteBuffer valueBuffer = ByteBuffer.allocate(40);

                // Not support multi stage for SousVide.
                valueBuffer.put(8 , (byte) 10);
                valueBuffer.putShort(1, (short)(setTargetTimeMin));
                valueBuffer.putShort(3, (short)(setTargetTimeMax));
                valueBuffer.putShort(5, (short) ((setTargetTemp) * 100));

//                for(int i = 0; i < 5; i++){
//                    valueBuffer.put(8 * i, (byte) 10);
//                    valueBuffer.putShort(1+8*i, (short)(setTargetTimeMin +i));
//                    valueBuffer.putShort(3+8*i, (short)(setTargetTimeMin +i));
//                    valueBuffer.putShort(5 + 8 * i, (short) ((setTargetTemp + i) * 100));
//
//                    if(i == 4){
//                        valueBuffer.put(7+8*i, (byte) 0x02);
//                    }
//                    else{
//                        valueBuffer.put(7+8*i, (byte) 0x01);
//                    }
//
//                }

                BleManager.getInstance().writeCharateristics(ParagonValues.CHARACTERISTIC_COOK_CONFIGURATION, valueBuffer.array());
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

    private void calculateTimeTemp() {
        ParagonMainActivity activity = (ParagonMainActivity) getActivity();


        switch (setDoneness) {
            case DONENESS_R:
                setTargetTemp = 134.5f;

                if (setThickness <= 0.2) {
                    setTargetTimeMin = 60;
                }
                else if (0.2 < setThickness && setThickness <= 0.4) {
                    setTargetTimeMin = 60 + 15;
                }
                else if (0.4 < setThickness && setThickness <= 0.6) {
                    setTargetTimeMin = 60 + 30;

                }
                else if (0.6 < setThickness && setThickness <= 0.8) {
                    setTargetTimeMin = 60 + 45;

                }
                else if (0.8 < setThickness && setThickness <= 1.2) {
                    setTargetTimeMin = 60 * 2;
                }
                else if (1.2 < setThickness && setThickness <= 1.4) {
                    setTargetTimeMin = 60 * 2 + 15;
                }
                else if (1.2 < setThickness && setThickness <= 1.6) {
                    setTargetTimeMin = 60 * 2 + 30;
                }
                else if (1.2 < setThickness && setThickness <= 2.0) {
                    setTargetTimeMin = 60 * 3 + 7;
                }
                else if (1.2 < setThickness && setThickness <= 2.15) {
                    setTargetTimeMin = 60 * 3 + 45;
                }
                else if (1.2 < setThickness && setThickness <= 2.35) {
                    setTargetTimeMin = 60 * 4 + 15;
                }
                else if (1.2 < setThickness && setThickness <= 2.5) {
                    setTargetTimeMin = 60 * 4 + 45;
                }
                else if (1.2 < setThickness && setThickness <= 2.75) {
                    setTargetTimeMin = 60 * 5 + 15;
                }

                break;

            case DONENESS_MR:
                setTargetTemp = 143.5f;

                if (setThickness <= 0.2) {
                    setTargetTimeMin = 25;
                }
                else if (0.2 < setThickness && setThickness <= 0.4) {
                    setTargetTimeMin = 30;
                }
                else if (0.4 < setThickness && setThickness <= 0.6) {
                    setTargetTimeMin = 45;
                }
                else if (0.6 < setThickness && setThickness <= 0.8) {
                    setTargetTimeMin = 55;
                }
                else if (0.8 < setThickness && setThickness <= 1.2) {
                    setTargetTimeMin = 60 * 30;
                }
                else if (1.2 < setThickness && setThickness <= 1.4) {
                    setTargetTimeMin = 60 * 30;
                }
                else if (1.2 < setThickness && setThickness <= 1.6) {
                    setTargetTimeMin = 60 * 45;
                }
                else if (1.2 < setThickness && setThickness <= 2.0) {
                    setTargetTimeMin = 60 * 2 + 31;
                }
                else if (1.2 < setThickness && setThickness <= 2.15) {
                    setTargetTimeMin = 60 * 2 + 45;
                }
                else if (1.2 < setThickness && setThickness <= 2.35) {
                    setTargetTimeMin = 60 * 3;
                }
                else if (1.2 < setThickness && setThickness <= 2.5) {
                    setTargetTimeMin = 60 * 3 + 15;
                }
                else if (1.2 < setThickness && setThickness <= 2.75) {
                    setTargetTimeMin = 60 * 3 + 45;
                }

                break;

            case DONENESS_M:
                setTargetTemp = 143.5f;

                if (setThickness <= 0.2) {
                    setTargetTimeMin = 25;
                }
                else if (0.2 < setThickness && setThickness <= 0.4) {
                    setTargetTimeMin = 30;
                }
                else if (0.4 < setThickness && setThickness <= 0.6) {
                    setTargetTimeMin = 45;
                }
                else if (0.6 < setThickness && setThickness <= 0.8) {
                    setTargetTimeMin = 55;
                }
                else if (0.8 < setThickness && setThickness <= 1.2) {
                    setTargetTimeMin = 60 * 30;
                }
                else if (1.2 < setThickness && setThickness <= 1.4) {
                    setTargetTimeMin = 60 * 30;
                }
                else if (1.2 < setThickness && setThickness <= 1.6) {
                    setTargetTimeMin = 60 * 45;
                }
                else if (1.2 < setThickness && setThickness <= 2.0) {
                    setTargetTimeMin = 60 * 2 + 31;
                }
                else if (1.2 < setThickness && setThickness <= 2.15) {
                    setTargetTimeMin = 60 * 2 + 45;
                }
                else if (1.2 < setThickness && setThickness <= 2.35) {
                    setTargetTimeMin = 60 * 3;
                }
                else if (1.2 < setThickness && setThickness <= 2.5) {
                    setTargetTimeMin = 60 * 3 + 15;
                }
                else if (1.2 < setThickness && setThickness <= 2.75) {
                    setTargetTimeMin = 60 * 3 + 45;
                }
                break;

            case DONENESS_MW:
                setTargetTemp = 143.5f;

                if (setThickness <= 0.2) {
                    setTargetTimeMin = 25;
                }
                else if (0.2 < setThickness && setThickness <= 0.4) {
                    setTargetTimeMin = 30;
                }
                else if (0.4 < setThickness && setThickness <= 0.6) {
                    setTargetTimeMin = 45;
                }
                else if (0.6 < setThickness && setThickness <= 0.8) {
                    setTargetTimeMin = 55;
                }
                else if (0.8 < setThickness && setThickness <= 1.2) {
                    setTargetTimeMin = 60 * 30;
                }
                else if (1.2 < setThickness && setThickness <= 1.4) {
                    setTargetTimeMin = 60 * 30;
                }
                else if (1.2 < setThickness && setThickness <= 1.6) {
                    setTargetTimeMin = 60 * 45;
                }
                else if (1.2 < setThickness && setThickness <= 2.0) {
                    setTargetTimeMin = 60 * 2 + 31;
                }
                else if (1.2 < setThickness && setThickness <= 2.15) {
                    setTargetTimeMin = 60 * 2 + 45;
                }
                else if (1.2 < setThickness && setThickness <= 2.35) {
                    setTargetTimeMin = 60 * 3;
                }
                else if (1.2 < setThickness && setThickness <= 2.5) {
                    setTargetTimeMin = 60 * 3 + 15;
                }
                else if (1.2 < setThickness && setThickness <= 2.75) {
                    setTargetTimeMin = 60 * 3 + 45;
                }
                break;

            case DONENESS_W:
                setTargetTemp = 143.5f;

                if (setThickness <= 0.2) {
                    setTargetTimeMin = 25;
                }
                else if (0.2 < setThickness && setThickness <= 0.4) {
                    setTargetTimeMin = 30;
                }
                else if (0.4 < setThickness && setThickness <= 0.6) {
                    setTargetTimeMin = 45;
                }
                else if (0.6 < setThickness && setThickness <= 0.8) {
                    setTargetTimeMin = 55;
                }
                else if (0.8 < setThickness && setThickness <= 1.2) {
                    setTargetTimeMin = 60 * 30;
                }
                else if (1.2 < setThickness && setThickness <= 1.4) {
                    setTargetTimeMin = 60 * 30;
                }
                else if (1.2 < setThickness && setThickness <= 1.6) {
                    setTargetTimeMin = 60 * 45;
                }
                else if (1.2 < setThickness && setThickness <= 2.0) {
                    setTargetTimeMin = 60 * 2 + 31;
                }
                else if (1.2 < setThickness && setThickness <= 2.15) {
                    setTargetTimeMin = 60 * 2 + 45;
                }
                else if (1.2 < setThickness && setThickness <= 2.35) {
                    setTargetTimeMin = 60 * 3;
                }
                else if (1.2 < setThickness && setThickness <= 2.5) {
                    setTargetTimeMin = 60 * 3 + 15;
                }
                else if (1.2 < setThickness && setThickness <= 2.75) {
                    setTargetTimeMin = 60 * 3 + 45;
                }
                break;

        }

        textSetTemp.setText(setTargetTemp + "℉");
        textSetTimeMin.setText(setTargetTimeMin / 60 + "H : " + setTargetTimeMin % 60 + "M");


        //            @0.2:@[@1,@00],
//            @0.4:@[@1,@15],
//            @0.6:@[@1,@30],
//            @0.8:@[@1,@45],
//            @1.2:@[@2,@00],
//            @1.4:@[@2,@15],
//            @1.6:@[@2,@30],
//            @2.0:@[@3,@07],
//            @2.15:@[@3,@45],
//            @2.35:@[@4,@15],
//            @2.5:@[@4,@45],
//            @2.75:@[@5,@15]


//        @143.5: @{
//            @0.2:@[@0,@25],
//            @0.4:@[@0,@30],
//            @0.6:@[@0,@45],
//            @0.8:@[@0,@55],
//            @1.2:@[@1,@30],
//            @1.4:@[@1,@30],
//            @1.6:@[@1,@45],
//            @2.0:@[@2,@31],
//            @2.15:@[@2,@45],
//            @2.35:@[@3,@00],
//            @2.5:@[@3,@15],
//            @2.75:@[@3,@45]
//        },
//        @151.0: @{
//            @0.2:@[@0,@13],
//            @0.4:@[@0,@25],
//            @0.6:@[@0,@35],
//            @0.8:@[@0,@45],
//            @1.2:@[@1,@15],
//            @1.4:@[@1,@15],
//            @1.6:@[@1,@30],
//            @2.0:@[@2,@28],
//            @2.15:@[@2,@15],
//            @2.35:@[@2,@30],
//            @2.5:@[@2,@45],
//            @2.75:@[@3,@15]
//        },
//        @160: @{
//            @0.2:@[@0,@13],
//            @0.4:@[@0,@25],
//            @0.6:@[@0,@35],
//            @0.8:@[@0,@45],
//            @1.2:@[@1,@15],
//            @1.4:@[@1,@15],
//            @1.6:@[@1,@30],
//            @2.0:@[@2,@28],
//            @2.15:@[@2,@15],
//            @2.35:@[@2,@30],
//            @2.5:@[@2,@45],
//            @2.75:@[@3,@15]
//        },
    }



}
