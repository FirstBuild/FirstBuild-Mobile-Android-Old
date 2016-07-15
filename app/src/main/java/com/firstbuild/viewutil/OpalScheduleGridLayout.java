package com.firstbuild.viewutil;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.util.SparseArrayCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;

/**
 * Created by hans on 16. 7. 14..
 */
public class OpalScheduleGridLayout extends GridLayout{

    public interface GridDiagonalDragDector {
        void onDiagonalDragDetected(View start, View end);
    }

    private static final String TAG = OpalScheduleGridLayout.class.getSimpleName();

    private SparseArrayCompat<Rect> childRectArray = new SparseArrayCompat<>();

    private View dragStartView;
    private View dragFinishView;

    private GridDiagonalDragDector detector;

    public OpalScheduleGridLayout(Context context) {
        super(context);
    }

    public OpalScheduleGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OpalScheduleGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OpalScheduleGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        if(childRectArray.size() == 0) {
            calcualteChildsHitRect();
        }

        switch(event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                dragStartView = getChildrenUnderCurrentDownPoint(event);

                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                if(dragStartView != null) {
                    dragFinishView = getChildrenUnderCurrentDownPoint(event);

                    if(dragFinishView != null &&
                            dragStartView.getId() != dragFinishView.getId() &&
                            indexOfChild(dragStartView) < indexOfChild(dragFinishView)) {
                        detector.onDiagonalDragDetected(dragStartView, dragFinishView);
                    }
                }

                dragStartView = null;
                dragFinishView = null;

                break;

            case MotionEvent.ACTION_CANCEL:
                dragStartView = null;
                dragFinishView = null;
                break;
        }

        return super.onInterceptTouchEvent(event);
    }

    private View getChildrenUnderCurrentDownPoint(MotionEvent event) {

        View found = null;

        for(int i=0; i < getChildCount(); i++) {

            View child = getChildAt(i);

            Rect rect = getChildHitRect(child.getId());

            if(rect != null &&
                    rect.contains((int)event.getX(), (int)event.getY())) {
                found = child;
                break;
            }
        }

        return found;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        // if size changes , invalidate the calculated child rect
        if(w != oldw && h != oldh) {
            childRectArray.clear();
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void calcualteChildsHitRect() {
        // calculate the all children rects and save for a later use
        for(int i = 0; i < getChildCount() ; i++) {
            int key = getChildAt(i).getId();
            Rect hitRect;
            if(childRectArray.get(key) == null) {
                hitRect = new Rect();
                getChildAt(i).getHitRect(hitRect);
            } else {
                hitRect = childRectArray.get(key);
                getChildAt(i).getHitRect(hitRect);
            }

            childRectArray.put(key, hitRect);
        }

        Log.d(TAG, "calculated Child Hit Rect : " + childRectArray.toString());
    }

    private Rect getChildHitRect(int viewId) {
        return childRectArray.get(viewId);
    }

    public void setDiagonalDragDetector(GridDiagonalDragDector detector) {
        this.detector = detector;
    }
}
