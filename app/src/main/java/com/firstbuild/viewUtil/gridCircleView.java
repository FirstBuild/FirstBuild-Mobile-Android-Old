/**
 * @file HotWaterCircleView.java
 * @brief Turing animated circle use for hotwater status and oven running.
 * @author Hollis Kim(320006828)
 * @date Aug/26/2015
 * Copyright (c) 2014 General Electric Corporation - Confidential - All rights reserved.
 */
package com.firstbuild.viewUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.firstbuild.androidapp.R;


public class gridCircleView extends View {

    private static final float LENGTH_GRID = 1.5f;
    private static final int THICKNESS_GRID = 10;
    private static final int THICKNESS_BAR = 30;
    private static final int THICKNESS_DASH = 8;
    private static final int THICKNESS_BACK = 5;


    private RectF rectGrid = new RectF();
    private RectF rectBar = new RectF();
    private RectF rectDash = new RectF();
    private RectF rectBack = new RectF();

    private Paint gridPaint = new Paint();
    private Paint circleBackPaint = new Paint();
    private Paint barPaint = new Paint();
    private Paint dashPaint = new Paint();
    private Paint gridBackPaint = new Paint();

    private int progress = 0;
    private boolean isSpinning = true;
    private int spinSpeed = 3;
    private int delayMillis = 0;
    private int barLength = 180;
    private boolean isFullCircle = false;
    private int circleColor;
    private int NUM_GRID = 100;

    private float gridValue = 0;
    private float barValue = 0;
    private float dashValue = 0;
    private int viewWidth;


    public gridCircleView(Context context) {
        super(context);
    }

    public gridCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public gridCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        int size = widthWithoutPadding > heightWithoutPadding ? heightWithoutPadding : widthWithoutPadding;

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = w;
        int minValue = Math.min(w, h);

        rectBar = new RectF(0, 0, minValue, minValue);
        rectBar.inset(THICKNESS_BAR, THICKNESS_BAR);

        rectBack = new RectF(0, 0, minValue, minValue);
        rectBack.inset(THICKNESS_BACK /2+ THICKNESS_BAR, THICKNESS_BACK /2+ THICKNESS_BAR);

        rectGrid = new RectF(0, 0, minValue, minValue);
        rectGrid.inset(THICKNESS_GRID + THICKNESS_BACK + THICKNESS_BAR, THICKNESS_GRID + THICKNESS_BACK + THICKNESS_BAR);

        rectDash = new RectF(0, 0, minValue, minValue);
        rectDash.inset(THICKNESS_DASH / 2 + THICKNESS_BAR, THICKNESS_DASH / 2 + THICKNESS_BAR);

        gridBackPaint.setColor(getResources().getColor(R.color.colorDivider));
        gridBackPaint.setAntiAlias(true);
        gridBackPaint.setStyle(Paint.Style.STROKE);
        gridBackPaint.setStrokeWidth(THICKNESS_GRID * 2);

        gridPaint.setColor(getResources().getColor(R.color.colorParagonAccent));
        gridPaint.setAntiAlias(true);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(THICKNESS_GRID * 2);

        barPaint.setColor(getResources().getColor(R.color.colorParagonAccent));
        barPaint.setAntiAlias(true);
        barPaint.setStyle(Paint.Style.STROKE);
        barPaint.setStrokeWidth(THICKNESS_BAR * 2);

        circleBackPaint.setColor(getResources().getColor(R.color.colorDivider));
        circleBackPaint.setAntiAlias(true);
        circleBackPaint.setStyle(Paint.Style.STROKE);
        circleBackPaint.setStrokeWidth(THICKNESS_BACK * 2);

        dashPaint.setColor(getResources().getColor(R.color.colorOrangeAccent));
        dashPaint.setAntiAlias(true);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{30, 7}, 0));
        dashPaint.setStrokeWidth(THICKNESS_DASH * 2);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background.
        canvas.drawArc(rectBack, 0.0f, 360.0f, false, circleBackPaint);

        // Draw grid.
        for (int i = 0; i < NUM_GRID; i++){
            float start = i * 360.f / NUM_GRID - 90;

            if(i >= gridValue * NUM_GRID){
                canvas.drawArc(rectGrid, start, LENGTH_GRID, false, gridBackPaint);
            }
            else{
                canvas.drawArc(rectGrid, start, LENGTH_GRID, false, gridPaint);
            }

        }
        // Draw bar.
        canvas.drawArc(rectBar, -90, 360.f * barValue, false, barPaint);

        // Draw dash
        canvas.drawArc(rectDash, -90, 360.f * dashValue, false, dashPaint);

        canvas.drawLine(viewWidth/2, 0, viewWidth/2, THICKNESS_BAR * 2, circleBackPaint );
    }

    public void setGridValue(float gridValue) {
        this.gridValue = gridValue;
        postInvalidate();
    }

    public void setBarValue(float barValue) {
        this.barValue = barValue;
        postInvalidate();
    }

    public void setDashValue(float dashValue) {
        this.dashValue = dashValue;
        postInvalidate();
    }

    public void setColor(int color){
        gridPaint.setColor(getResources().getColor(color));
    }



}
