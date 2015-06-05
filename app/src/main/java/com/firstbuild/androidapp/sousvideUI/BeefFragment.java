package com.firstbuild.androidapp.sousvideUI;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.firstbuild.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BeefFragment extends Fragment implements View.OnTouchListener {

    private String TAG = BeefFragment.class.getSimpleName();

    private int xDelta;
    private int yDelta;
    private View containerThickness;
    private View containerDoneness;
    private View imgMeat;
    private View knobDoneness;

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

        view.findViewById(R.id.thickness_knob).setOnTouchListener(this);
        view.findViewById(R.id.doneness_knob).setOnTouchListener(this);
        view.findViewById(R.id.btn_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().
                        beginTransaction().
                        replace(R.id.frame_content, new ReadyFragment()).
                        addToBackStack(null).
                        commit();
            }
        });

        view.findViewById(R.id.doneness_r).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDonenessChanged(v);
            }
        });
        view.findViewById(R.id.doneness_mr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDonenessChanged(v);
            }
        });
        view.findViewById(R.id.doneness_m).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDonenessChanged(v);
            }
        });
        view.findViewById(R.id.doneness_mw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDonenessChanged(v);
            }
        });
        view.findViewById(R.id.doneness_w).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDonenessChanged(v);
            }
        });

        return view;
    }

    public void onDonenessChanged(View v) {
        RelativeLayout.LayoutParams layoutParamsKnob = (RelativeLayout.LayoutParams) knobDoneness.getLayoutParams();

        layoutParamsKnob.leftMargin = (int) v.getX();
        knobDoneness.setLayoutParams(layoutParamsKnob);
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
                    } else if (widthGrid <= posX && posX < widthGrid * 2) {
                        layoutParams.leftMargin = widthGrid;
                    } else if (widthGrid * 2 <= posX && posX < widthGrid * 3) {
                        layoutParams.leftMargin = widthGrid * 2;
                    } else if (widthGrid * 3 <= posX && posX < widthGrid * 4) {
                        layoutParams.leftMargin = widthGrid * 3;
                    } else {
                        layoutParams.leftMargin = widthGrid * 4;
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

                    // Slide for thickness knob
                    if (0 < Y - yDelta && Y - yDelta < (containerThickness.getHeight() - v.getHeight())) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        layoutParams.topMargin = Y - yDelta;
                        layoutParams.bottomMargin = -250;
                        v.setLayoutParams(layoutParams);

                        RelativeLayout.LayoutParams layoutParamsMeat = (RelativeLayout.LayoutParams) imgMeat.getLayoutParams();

                        layoutParamsMeat.height = containerThickness.getHeight() - (Y - yDelta) - (v.getHeight() / 2);
                        imgMeat.setLayoutParams(layoutParamsMeat);
                    }

                } else if (v.getId() == R.id.doneness_knob) {           // Slide for doneness knob.
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
