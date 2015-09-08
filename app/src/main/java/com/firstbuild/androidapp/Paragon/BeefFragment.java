package com.firstbuild.androidapp.Paragon;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firstbuild.androidapp.ParagonValues;
import com.firstbuild.androidapp.R;
import com.firstbuild.commonframework.bleManager.BleManager;

import java.nio.ByteBuffer;

/**
 * A simple {@link Fragment} subclass.
 */
public class BeefFragment extends Fragment implements View.OnTouchListener {

    private final int DONENESS_R  = 0;
    private final int DONENESS_MR = 1;
    private final int DONENESS_M  = 2;
    private final int DONENESS_MW = 3;
    private final int DONENESS_W  = 4;

    private String TAG = BeefFragment.class.getSimpleName();
    private int      xDelta;
    private int      yDelta;
    private View     containerThickness;
    private View     containerDoneness;
    private View     imgMeat;
    private View     knobDoneness;
    private TextView textDoneness;
    private TextView textThickness;
    private int thicknessIndex = 0;

    private float    setThickness;
    private int      setDoneness;
    private TextView textTargetTempTime;

    public BeefFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_beef, container, false);

        containerThickness = view.findViewById(R.id.container_thickness);
        containerDoneness = view.findViewById(R.id.container_doneness);
        knobDoneness = view.findViewById(R.id.doneness_knob);
        imgMeat = view.findViewById(R.id.img_meat);
        textDoneness = (TextView) view.findViewById(R.id.text_doneness);
        textThickness = (TextView) view.findViewById(R.id.text_thickness);
        textTargetTempTime = (TextView) view.findViewById(R.id.textTargetTempTime);

        view.findViewById(R.id.thickness_knob).setOnTouchListener(this);
        view.findViewById(R.id.doneness_knob).setOnTouchListener(this);
        view.findViewById(R.id.btn_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParagonMainActivity activity = (ParagonMainActivity) getActivity();


                calculateTimeTemp();

                //TODO: Should convert the target temp value properly.
                ByteBuffer valueBuffer = ByteBuffer.allocate(2);

                short setValue = (short) (activity.getTargetTemp() * 100);

                valueBuffer.putShort(setValue);
                BleManager.getInstance().writeCharateristics(ParagonValues.CHARACTERISTIC_TARGET_TEMPERATURE, valueBuffer.array());

                valueBuffer.clear();
                valueBuffer.putShort((short)(activity.getTargetTime()));
                BleManager.getInstance().writeCharateristics(ParagonValues.CHARACTERISTIC_COOK_TIME, valueBuffer.array());

                ((ParagonMainActivity) getActivity()).nextStep(ParagonMainActivity.ParagonSteps.STEP_SOUSVIDE_READY_PREHEAT);
            }
        });

        view.findViewById(R.id.doneness_r).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDonenessChanged(v);
                updateUiDoneness(DONENESS_R);
            }
        });
        view.findViewById(R.id.doneness_mr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDonenessChanged(v);
                updateUiDoneness(DONENESS_MR);
            }
        });
        view.findViewById(R.id.doneness_m).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDonenessChanged(v);
                updateUiDoneness(DONENESS_M);
            }
        });
        view.findViewById(R.id.doneness_mw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDonenessChanged(v);
                updateUiDoneness(DONENESS_MW);
            }
        });
        view.findViewById(R.id.doneness_w).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDonenessChanged(v);
                updateUiDoneness(DONENESS_W);
            }
        });

        return view;
    }

    private void calculateTimeTemp() {
        ParagonMainActivity activity = (ParagonMainActivity) getActivity();

        float thickness = Float.parseFloat(getResources().getStringArray(R.array.string_thickness)[thicknessIndex]);
        float setTargetTemp = 0.0f;
        int setTargetTime = 0;


        switch (setDoneness) {
            case DONENESS_R:
                setTargetTemp = 134.5f;

                if (thickness <= 0.2) {
                    setTargetTime = 60;
                }
                else if (0.2 < thickness && thickness <= 0.4) {
                    setTargetTime = 60 + 15;
                }
                else if (0.4 < thickness && thickness <= 0.6) {
                    setTargetTime = 60 + 30;

                }
                else if (0.6 < thickness && thickness <= 0.8) {
                    setTargetTime = 60 + 45;

                }
                else if (0.8 < thickness && thickness <= 1.2) {
                    setTargetTime = 60 * 2;
                }
                else if (1.2 < thickness && thickness <= 1.4) {
                    setTargetTime = 60 * 2 + 15;
                }
                else if (1.2 < thickness && thickness <= 1.6) {
                    setTargetTime = 60 * 2 + 30;
                }
                else if (1.2 < thickness && thickness <= 2.0) {
                    setTargetTime = 60 * 3 + 7;
                }
                else if (1.2 < thickness && thickness <= 2.15) {
                    setTargetTime = 60 * 3 + 45;
                }
                else if (1.2 < thickness && thickness <= 2.35) {
                    setTargetTime = 60 * 4 + 15;
                }
                else if (1.2 < thickness && thickness <= 2.5) {
                    setTargetTime = 60 * 4 + 45;
                }
                else if (1.2 < thickness && thickness <= 2.75) {
                    setTargetTime = 60 * 5 + 15;
                }

                break;

            case DONENESS_MR:
                setTargetTemp = 143.5f;

                if (thickness <= 0.2) {
                    setTargetTime = 25;
                }
                else if (0.2 < thickness && thickness <= 0.4) {
                    setTargetTime = 30;
                }
                else if (0.4 < thickness && thickness <= 0.6) {
                    setTargetTime = 45;
                }
                else if (0.6 < thickness && thickness <= 0.8) {
                    setTargetTime = 55;
                }
                else if (0.8 < thickness && thickness <= 1.2) {
                    setTargetTime = 60 * 30;
                }
                else if (1.2 < thickness && thickness <= 1.4) {
                    setTargetTime = 60 * 30;
                }
                else if (1.2 < thickness && thickness <= 1.6) {
                    setTargetTime = 60 * 45;
                }
                else if (1.2 < thickness && thickness <= 2.0) {
                    setTargetTime = 60 * 2 + 31;
                }
                else if (1.2 < thickness && thickness <= 2.15) {
                    setTargetTime = 60 * 2 + 45;
                }
                else if (1.2 < thickness && thickness <= 2.35) {
                    setTargetTime = 60 * 3;
                }
                else if (1.2 < thickness && thickness <= 2.5) {
                    setTargetTime = 60 * 3 + 15;
                }
                else if (1.2 < thickness && thickness <= 2.75) {
                    setTargetTime = 60 * 3 + 45;
                }

                break;

            case DONENESS_M:
                setTargetTemp = 143.5f;

                if (thickness <= 0.2) {
                    setTargetTime = 25;
                }
                else if (0.2 < thickness && thickness <= 0.4) {
                    setTargetTime = 30;
                }
                else if (0.4 < thickness && thickness <= 0.6) {
                    setTargetTime = 45;
                }
                else if (0.6 < thickness && thickness <= 0.8) {
                    setTargetTime = 55;
                }
                else if (0.8 < thickness && thickness <= 1.2) {
                    setTargetTime = 60 * 30;
                }
                else if (1.2 < thickness && thickness <= 1.4) {
                    setTargetTime = 60 * 30;
                }
                else if (1.2 < thickness && thickness <= 1.6) {
                    setTargetTime = 60 * 45;
                }
                else if (1.2 < thickness && thickness <= 2.0) {
                    setTargetTime = 60 * 2 + 31;
                }
                else if (1.2 < thickness && thickness <= 2.15) {
                    setTargetTime = 60 * 2 + 45;
                }
                else if (1.2 < thickness && thickness <= 2.35) {
                    setTargetTime = 60 * 3;
                }
                else if (1.2 < thickness && thickness <= 2.5) {
                    setTargetTime = 60 * 3 + 15;
                }
                else if (1.2 < thickness && thickness <= 2.75) {
                    setTargetTime = 60 * 3 + 45;
                }
                break;

            case DONENESS_MW:
                setTargetTemp = 143.5f;

                if (thickness <= 0.2) {
                    setTargetTime = 25;
                }
                else if (0.2 < thickness && thickness <= 0.4) {
                    setTargetTime = 30;
                }
                else if (0.4 < thickness && thickness <= 0.6) {
                    setTargetTime = 45;
                }
                else if (0.6 < thickness && thickness <= 0.8) {
                    setTargetTime = 55;
                }
                else if (0.8 < thickness && thickness <= 1.2) {
                    setTargetTime = 60 * 30;
                }
                else if (1.2 < thickness && thickness <= 1.4) {
                    setTargetTime = 60 * 30;
                }
                else if (1.2 < thickness && thickness <= 1.6) {
                    setTargetTime = 60 * 45;
                }
                else if (1.2 < thickness && thickness <= 2.0) {
                    setTargetTime = 60 * 2 + 31;
                }
                else if (1.2 < thickness && thickness <= 2.15) {
                    setTargetTime = 60 * 2 + 45;
                }
                else if (1.2 < thickness && thickness <= 2.35) {
                    setTargetTime = 60 * 3;
                }
                else if (1.2 < thickness && thickness <= 2.5) {
                    setTargetTime = 60 * 3 + 15;
                }
                else if (1.2 < thickness && thickness <= 2.75) {
                    setTargetTime = 60 * 3 + 45;
                }
                break;

            case DONENESS_W:
                setTargetTemp = 143.5f;

                if (thickness <= 0.2) {
                    setTargetTime = 25;
                }
                else if (0.2 < thickness && thickness <= 0.4) {
                    setTargetTime = 30;
                }
                else if (0.4 < thickness && thickness <= 0.6) {
                    setTargetTime = 45;
                }
                else if (0.6 < thickness && thickness <= 0.8) {
                    setTargetTime = 55;
                }
                else if (0.8 < thickness && thickness <= 1.2) {
                    setTargetTime = 60 * 30;
                }
                else if (1.2 < thickness && thickness <= 1.4) {
                    setTargetTime = 60 * 30;
                }
                else if (1.2 < thickness && thickness <= 1.6) {
                    setTargetTime = 60 * 45;
                }
                else if (1.2 < thickness && thickness <= 2.0) {
                    setTargetTime = 60 * 2 + 31;
                }
                else if (1.2 < thickness && thickness <= 2.15) {
                    setTargetTime = 60 * 2 + 45;
                }
                else if (1.2 < thickness && thickness <= 2.35) {
                    setTargetTime = 60 * 3;
                }
                else if (1.2 < thickness && thickness <= 2.5) {
                    setTargetTime = 60 * 3 + 15;
                }
                else if (1.2 < thickness && thickness <= 2.75) {
                    setTargetTime = 60 * 3 + 45;
                }
                break;

        }


        activity.setTargetTime(setTargetTime);
        activity.setTargetTemp(setTargetTemp);

        textTargetTempTime.setText(setTargetTime / 60 + "H " + setTargetTime % 60 + "MIN | " + setTargetTemp + "℉");


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

    public void onDonenessChanged(View v) {
        RelativeLayout.LayoutParams layoutParamsKnob = (RelativeLayout.LayoutParams) knobDoneness.getLayoutParams();

        layoutParamsKnob.leftMargin = (int) v.getX();
        knobDoneness.setLayoutParams(layoutParamsKnob);
    }

    private void updateUiDoneness(int doneness) {
        setDoneness = doneness;
        String strDoneness = getResources().getStringArray(R.array.string_doneness)[setDoneness];
        textDoneness.setText(strDoneness);

        calculateTimeTemp();
    }

    private void updateDonenessText(String text) {
        textDoneness.setText(text);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                xDelta = X - lParams.leftMargin;
                yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                int posX = X - xDelta + (v.getWidth() / 2);

                // When release on the donness knob, snap on close donness slot.
                if (v.getId() == R.id.doneness_knob) {
                    int widthGrid = (int) (containerDoneness.getWidth() / 5);
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();

                    if (posX < widthGrid) {
                        layoutParams.leftMargin = 0;
                        updateUiDoneness(DONENESS_R);
                    }
                    else if (widthGrid <= posX && posX < widthGrid * 2) {
                        layoutParams.leftMargin = widthGrid;
                        updateUiDoneness(DONENESS_MR);
                    }
                    else if (widthGrid * 2 <= posX && posX < widthGrid * 3) {
                        layoutParams.leftMargin = widthGrid * 2;
                        updateUiDoneness(DONENESS_M);
                    }
                    else if (widthGrid * 3 <= posX && posX < widthGrid * 4) {
                        layoutParams.leftMargin = widthGrid * 3;
                        updateUiDoneness(DONENESS_MW);
                    }
                    else {
                        layoutParams.leftMargin = widthGrid * 4;
                        updateUiDoneness(DONENESS_W);
                    }

                    layoutParams.rightMargin = -250;
                    v.setLayoutParams(layoutParams);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:

                if (v.getId() == R.id.thickness_knob) {
//                    Log.d(TAG, "ACTION_MOVE " + Y + ", " + yDelta + ", " + (Y - yDelta));
                    int posY = Y - yDelta;
                    int maxHeight = (containerThickness.getHeight() - v.getHeight());

                    // Slide for thicknessBack knob
                    if (0 < posY && posY < (containerThickness.getHeight() - v.getHeight())) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        layoutParams.topMargin = posY;
                        layoutParams.bottomMargin = -250;
                        v.setLayoutParams(layoutParams);

                        RelativeLayout.LayoutParams layoutParamsMeat = (RelativeLayout.LayoutParams) imgMeat.getLayoutParams();

                        layoutParamsMeat.height = containerThickness.getHeight() - (posY) - (v.getHeight() / 2);
                        imgMeat.setLayoutParams(layoutParamsMeat);

                        thicknessIndex = (int) (posY / (maxHeight / 8.0));
//                        Log.d(TAG, "Thickness Index  :" + thicknessIndex);

                        String[] arrayThickness = getResources().getStringArray(R.array.string_thickness);

                        textThickness.setText(arrayThickness[thicknessIndex] + " Inches Thick");
                        setThickness = Float.parseFloat(arrayThickness[thicknessIndex]);

                        calculateTimeTemp();
                    }

                }
                else if (v.getId() == R.id.doneness_knob) {           // Slide for doneness knob.
//                    Log.d(TAG, "ACTION_MOVE " + X + ", " + xDelta + ", " + (X - xDelta));

                    if (0 < X - xDelta && X - xDelta < (containerDoneness.getWidth() - v.getWidth())) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        layoutParams.leftMargin = X - xDelta;
                        layoutParams.rightMargin = -250;
                        v.setLayoutParams(layoutParams);
                    }
                }

                break;
        }
        containerThickness.invalidate();
        return true;
    }
}
