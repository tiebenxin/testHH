package net.cb.cb.library.view;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import net.cb.cb.library.utils.DensityUtil;


public class WaitView extends View {
    private Paint paint = new Paint();
    private float width, height, midx, midy;
    private int osize = 10;
    private float opadding = 40;
    private boolean isAutoPlay=false;
    private int[] colors = {0x990000FF, 0x9900FF00, 0x99FF0000};
    private long animtime=500;

    public void setAutoPlay(boolean autoPlay) {
        isAutoPlay = autoPlay;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        midx = (width + osize) / 2;
        midy = (height + osize) / 2;
    }

    public WaitView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        osize= DensityUtil.dip2px(context,4);
        opadding= DensityUtil.dip2px(context,16);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, net.cb.cb.library.R.styleable.WaitView);
        isAutoPlay= typedArray.getBoolean(net.cb.cb.library.R.styleable.WaitView_waitview_state,false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(colors[0]);
        canvas.drawCircle(midx - opadding+animValue, midy, osize, paint);
        paint.setColor(colors[1]);
        canvas.drawCircle(midx, midy, osize, paint);
        paint.setColor(colors[2]);
        canvas.drawCircle(midx + opadding-animValue, midy, osize, paint);



    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(!isAutoPlay){
            return;
        }

        if(visibility==VISIBLE){
            //判断是否显示
            startAnim();
        }else{
            stopAnim();
        }

    }

    private float animValue = 0;
    private ValueAnimator animator;

    public void startAnim() {
        if(animator!=null){
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




                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            int i=0;
            @Override
            public void onAnimationStart(Animator animator) {
                i=0;
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
                if(i%2==0){
                    return;
                }
                int temp=colors[2];
                colors[2]=colors[1];
                colors[1]=colors[0];
                colors[0]=temp;

            }
        });
        animator.start();
    }


    public void stopAnim() {
        if(animator==null)
            return;
        animator.end();
        animator.cancel();
        animator=null;
    }
}
