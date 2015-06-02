package com.firstbuild.viewUtil;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import com.firstbuild.androidapp.R;

public class StyledEditText extends EditText {
	private static final String TAG = "StyledEditText";

    public StyledEditText(Context context) {
        super(context);
    }

    public StyledEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public StyledEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.StyledTextView);
        String customFont = a.getString(R.styleable.StyledTextView_typeface);
        if(customFont != null){
            setCustomFont(ctx, customFont+".ttf");
            a.recycle();
        }
    }

    public boolean setCustomFont(Context ctx, String asset) {    	
        Typeface tf = null;
        try {
        tf = Typeface.createFromAsset(ctx.getAssets(), asset);  
        } catch (Exception e) {
            Log.e(TAG, "Could not get typeface: " + e.getMessage());
            return false;
        }

        setTypeface(tf);  
        return true;
    }
}
