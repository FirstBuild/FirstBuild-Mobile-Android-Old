/**
 * @file HotWaterCircleView.java
 * @brief Turing animated circle use for hotwater status and oven running.
 * @author Hollis Kim(320006828)
 * @date Aug/26/2015
 * Copyright (c) 2014 General Electric Corporation - Confidential - All rights reserved.
 */
package com.firstbuild.androidapp.Paragon.

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.ge.kitchen.R;

public class SpinningCircleView extends View {
    protected int layout_width;
    protected int layout_height;
    protected RectF rectOuter = new RectF();
    protected Paint circlePaint = new Paint();
    protected Paint circleBackPaint = new Paint();
    protected int thickness = 5;
    protected int progress = 0;
    protected boolean isSpinning = true;
    protected int spinSpeed = 3;
    protected int delayMillis = 0;
    protected int barLength = 180;
    protected boolean isFullCircle = false;
    private int circleColor;

    public SpinningCircleView(Context context) {
        super(context);
    }

    public SpinningCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpinningCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        int size =  widthWithoutPadding > heightWithoutPadding? heightWithoutPadding : widthWithoutPadding ;

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        layout_width = w;
        layout_height = h;

        int minValue = Math.min(layout_width, layout_height);

        rectOuter = new RectF(0,0,minValue,minValue);
        rectOuter.inset(thickness, thickness);

        circlePaint.setColor(circleColor);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(thickness * 2);

        circleBackPaint.setColor(getResources().getColor(R.color.colorDivider));
        circleBackPaint.setAntiAlias(true);
        circleBackPaint.setStyle(Paint.Style.STROKE);
        circleBackPaint.setStrokeWidth(thickness * 2);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(rectOuter, 0.0f, 360.0f, false, circleBackPaint);

        if(isSpinning){
            canvas.drawArc(rectOuter, progress, barLength, false, circlePaint);

            scheduleRedraw();
        }
        else if(isFullCircle){
            canvas.drawArc(rectOuter, 0.0f, 360.0f, false, circlePaint);
        }
    }

    private void scheduleRedraw() {
        progress += spinSpeed;
        if (progress > 360) {
            progress = 0;
        }
        postInvalidateDelayed(delayMillis);
    }

    public boolean isSpinning() {
        return isSpinning;
    }

    public void stopSpinning(){
        isSpinning = false;
        progress = 0;
        postInvalidate();
    }

    public void startSpinning() {
        isSpinning = true;
        postInvalidate();
    }

    public void fullCircle() {
        isFullCircle = true;
        isSpinning = false;
        postInvalidate();
    }

    public void setCirclePaint(int circlePaintColor) {
        circleColor = circlePaintColor;
    }
}
