package com.firstbuild.viewutil;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;


public class StyledNumberPicker extends NumberPicker {
    private String TAG = StyledNumberPicker.class.getSimpleName();
    private int adjustValue = 0;

    public StyledNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);

    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
        updateView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
        setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
        setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }

    private void updateView(View view) {
        if (view instanceof EditText) {

            Typeface tf = null;
            try {
                tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/geInspiraMedium.ttf");
            } catch (Exception e) {
                Log.e(TAG, "Could not get typeface: " + e.getMessage());
            }

            ((EditText) view).setTypeface(tf);
            ((EditText) view).setTextSize(24);
        }
    }

    @Override
    public int getValue() {
        int value = super.getValue();

        return value + adjustValue;
    }

    @Override
    public void setValue(int value) {
        value -= adjustValue;

        super.setValue(value);
    }

    public void setRange(int minValue, int maxValue) {
        Log.d(TAG, "setRange " + minValue + ", " + maxValue);


        if (minValue < 0) {
            adjustValue = minValue;

            setMinValue(0);
            setMaxValue(maxValue - adjustValue);
            setWrapSelectorWheel(false);

//            setFormatter(new Formatter() {
//                @Override
//                public String format(int value) {
//                    GeLog.print(GeLog.debug, "StyledNumberPicker.format, " + "value:"+value+", adjustValue :"+adjustValue+", "+Integer.toString(value + adjustValue));
//
//                    return Integer.toString(value + adjustValue);
//                }
//            });
        } else {
            setMinValue(minValue);
            setMaxValue(maxValue);
        }
    }
}
