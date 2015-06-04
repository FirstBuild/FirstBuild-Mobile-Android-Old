package com.firstbuild.androidapp.sousvideUI;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private View imgMeat;

    public BeefFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_beef, container, false);

        containerThickness = view.findViewById(R.id.container_thickness);
        imgMeat = view.findViewById(R.id.img_meat);

        view.findViewById(R.id.thickness_knob).setOnTouchListener(this);
        view.findViewById(R.id.cook_knob).setOnTouchListener(this);
        view.findViewById(R.id.btn_continue).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });

        return view;
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
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:

                if (v.getId() == R.id.thickness_knob) {
//                    Log.d(TAG, "ACTION_MOVE " + Y + ", " + yDelta);

                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    layoutParams.topMargin = Y - yDelta;
                    layoutParams.bottomMargin = -250;
                    v.setLayoutParams(layoutParams);

                    RelativeLayout.LayoutParams layoutParamsMeat = (RelativeLayout.LayoutParams) imgMeat.getLayoutParams();

                    layoutParamsMeat.height = containerThickness.getHeight() - (Y - yDelta) - (v.getHeight() / 2);
                    imgMeat.setLayoutParams(layoutParamsMeat);
                } else if (v.getId() == R.id.cook_knob) {
//                    Log.d(TAG, "ACTION_MOVE " + X + ", " + xDelta);

                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    layoutParams.leftMargin = X - xDelta;
                    layoutParams.rightMargin = -250;
                    v.setLayoutParams(layoutParams);
                }

                break;
        }
        containerThickness.invalidate();
        return true;
    }
}
