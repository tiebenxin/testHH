package net.cb.cb.library.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Mark on 2017/4/25.
 */

public class SmoothRecyclerView extends RecyclerView {

    private boolean isHorizontalScroll = false;
    private int mMin = 40;
    private int mStartX = 0;
    private int mStartY = 0;

    public SmoothRecyclerView(Context context) {
        super(context);
    }

    public SmoothRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SmoothRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                getParent().requestDisallowInterceptTouchEvent(true);
                mStartX = x;
                mStartY = y;

                break;
            case MotionEvent.ACTION_MOVE:
                int endX = x;
                int endY = y;

                if (Math.abs(endX - mStartX) > mMin && Math.abs(endX - mStartX) > Math.abs(endY - mStartY) && isHorizontalScroll == false) {
                    //最小的移动判断
                    isHorizontalScroll = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    if (isHorizontalScroll == false && Math.abs(endX - mStartX) < Math.abs(endY - mStartY)) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(true);
                isHorizontalScroll = false;
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

}
