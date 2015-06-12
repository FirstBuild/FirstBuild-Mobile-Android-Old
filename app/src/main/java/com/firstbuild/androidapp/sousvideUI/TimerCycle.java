package com.firstbuild.androidapp.sousvideUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.firstbuild.androidapp.R;


public class TimerCycle extends View {
    private int layout_width;
    private int layout_height;
    private RectF rectOuter = new RectF();
    private Paint circleFrontPaint = new Paint();
    private Paint circleWideBackPaint = new Paint();
    private Paint circleDiarkPaint = new Paint();
    private Paint indicatorPaint = new Paint();
    private int thickness = 50;
    private int progress = 0;
    private boolean isSpinning = true;
    private int margin = 20;
    private int spinSpeed = 1;
    private int delayMillis = 0;
    private int indicatorThickness = 20;
    private boolean isFullCircle = false;
    private float timeRatio = 0.0f;
    private float alphaPluse = 200.0f;
    private float alphaPluseDirection = 6.0f;

    private final int circleAlphaMax = 200;
    private final int circleAlphaMin = 10;

    public TimerCycle(Context context) {
        super(context);
    }

    public TimerCycle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimerCycle(Context context, AttributeSet attrs, int defStyleAttr) {
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

        layout_width = w;
        layout_height = h;

        int minValue = Math.min(layout_width, layout_height);

        rectOuter = new RectF(0, 0, minValue, minValue);
        rectOuter.inset(thickness + margin, thickness + margin);

        circleFrontPaint.setColor(0xFFFFFFFF);
        circleFrontPaint.setAntiAlias(true);
        circleFrontPaint.setStyle(Paint.Style.STROKE);
        circleFrontPaint.setStrokeWidth(thickness);

        circleDiarkPaint.setColor(0);
        circleDiarkPaint.setAntiAlias(true);
        circleDiarkPaint.setAlpha(25);
        circleDiarkPaint.setStyle(Paint.Style.STROKE);
        circleDiarkPaint.setStrokeWidth(thickness);

        circleWideBackPaint.setColor(getResources().getColor(R.color.colorHighlight));
        circleWideBackPaint.setAlpha(204);
        circleWideBackPaint.setAntiAlias(true);
        circleWideBackPaint.setStyle(Paint.Style.STROKE);
        circleWideBackPaint.setStrokeWidth(thickness * 2);

        indicatorPaint.setColor(0xFFFFFFFF);
        indicatorPaint.setAntiAlias(true);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeWidth(indicatorThickness);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        circleFrontPaint.setAlpha(255);
        circleWideBackPaint.setAlpha(Math.min(circleAlphaMax, (int)alphaPluse));
        canvas.drawArc(rectOuter, 0.0f, 360.0f, false, circleWideBackPaint);
        canvas.drawArc(rectOuter, 0.0f, 360.0f, false, circleDiarkPaint);

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        {
            canvas.rotate(-90.0f, rectOuter.centerX(), rectOuter.centerY());
            canvas.drawArc(rectOuter, 0.0f, timeRatio * 360.0f, false, circleFrontPaint);

            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            {
                canvas.rotate(timeRatio * 360.0f, rectOuter.centerX(), rectOuter.centerY());
                canvas.drawLine(rectOuter.centerX() + (rectOuter.width() / 2) - (thickness + margin), rectOuter.centerY(),
                        rectOuter.centerX() + (rectOuter.width() / 2) + (thickness + margin), rectOuter.centerY(), indicatorPaint);

            }
            canvas.restore();

        }
        canvas.restore();

//        circleFrontPaint.setAlpha(100);
//        canvas.drawArc(rectOuter, progress, 1.0f, false, circleFrontPaint);

        scheduleRedraw();
    }

    private void scheduleRedraw() {
        progress += spinSpeed;
        timeRatio += 0.002f;
        alphaPluse += alphaPluseDirection;

        if (progress > 360) {
            progress = 0;
        }

        if (timeRatio > 1.0f) {
            timeRatio = 1.0f;
        }

        if(alphaPluse < circleAlphaMin || alphaPluse >= 300){
            alphaPluseDirection *= -1;
        }

        postInvalidateDelayed(delayMillis);
    }

    public boolean isSpinning() {
        return isSpinning;
    }

    public void stopSpinning() {
        isSpinning = false;
        progress = 0;
        postInvalidate();
    }

    public void startSpinning() {
        isSpinning = true;
        postInvalidate();
    }

    /**
     * @param timeRatio Time ratio. The value is from 0.0 to 1.0
     */
    public void setTime(float timeRatio) {
        timeRatio = timeRatio;
    }

    public void fullCircle() {
        isFullCircle = true;
        isSpinning = false;
        postInvalidate();
    }
}
