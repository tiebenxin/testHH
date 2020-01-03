package net.cb.cb.library.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.baoyz.widget.PullRefreshLayout;
import com.baoyz.widget.RefreshDrawable;

import net.cb.cb.library.utils.DensityUtil;

class CustomDrawable extends RefreshDrawable {
    private Paint paint = new Paint();
    private float width, height, midx, midy;
    private int osize = 15;
    private float opadding = 50;
    private long animtime = 500;

    public CustomDrawable(Context context, final PullRefreshLayout layout) {
        super(context, layout);
        osize = DensityUtil.dip2px(context, 4);
        opadding = DensityUtil.dip2px(context, 16);
        width = context.getResources().getDisplayMetrics().widthPixels;
        midx = (width + osize) / 2;
        layout.post(new Runnable() {
            @Override
            public void run() {
                width = layout.getMeasuredWidth();
                midx = (width + osize) / 2;
            }
        });


    }

    @Override
    public void setPercent(float percent) {

    }

    @Override
    public void setColorSchemeColors(int[] colorSchemeColors) {

    }

    @Override
    public void offsetTopAndBottom(int offset) {
        Log.i("", ">>>offsetTopAndBottom: " + offset);

        midy += offset / 2;

        invalidateSelf();
    }

    @Override
    public void start() {
        startAnim();
    }

    @Override
    public void stop() {
        //midy=0;
        stopAnim();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private int[] colors = {0x990000FF, 0x9900FF00, 0x99FF0000};

    @Override
    public void draw(@NonNull Canvas canvas) {
        if(midy<30){
            return;
        }
        paint.setColor(colors[0]);
        canvas.drawCircle(midx - opadding + animValue, midy, osize, paint);
        paint.setColor(colors[1]);
        canvas.drawCircle(midx, midy, osize, paint);
        paint.setColor(colors[2]);
        canvas.drawCircle(midx + opadding - animValue, midy, osize, paint);
    }

    private float animValue = 0;
    private ValueAnimator animator;

    public void startAnim() {
        if (animator != null) {
            return;
        }

        animator = ValueAnimator.ofFloat(0f, opadding);
        animator.setDuration(animtime);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                animValue = (Float) animation.getAnimatedValue();


                invalidateSelf();

                //    invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            int i = 0;

            @Override
            public void onAnimationStart(Animator animator) {
                i = 0;
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                i++;
                if (i % 2 == 0) {
                    return;
                }
                int temp = colors[2];
                colors[2] = colors[1];
                colors[1] = colors[0];
                colors[0] = temp;

            }
        });
        animator.start();
    }


    public void stopAnim() {
        if (animator == null)
            return;
        animator.end();
        animator.cancel();
        animator = null;
    }
}
