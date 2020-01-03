package net.cb.cb.library.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/***
 * 播放图片动画
 */
public class AnimationPic {
    private ValueAnimator animator;
    private int[] res;
    private ImageView view;
    private int defRes;
    private long time;
    private String tag="";

    public void init(final String tag,final ImageView mview, final int[] res,int defRes, long time) {
        if(animator!=null){
            stop(this.tag,this.view);
        }
        this.res = res;
        this.view = mview;
        this.defRes=defRes;
       this.time=time;
       this.tag=tag;
        play();
        Log.d("xxx", "init: "+tag+"   "+mview);
        animator.start();
    }

    private void play(){
        animator = ValueAnimator.ofInt(0, res.length);
        animator.setDuration(time);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int v = (int) animation.getAnimatedValue();
                v=v>=res.length?res.length-1:v;
                view.setImageResource(res[v]);
              //  Log.d("xxx", "addUpdateListener: ");
            }
        });
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                view.setImageResource(defRes);
            }
        });
    }

    public void start(String tag,ImageView mview){
        this.view = mview;
        this.tag=tag;
        Log.d("xxx", "onCreate: "+tag+"   "+mview);
        play();
        animator.start();
    }

    public void stop(String tag,ImageView mview){

        if(animator==null)
            return;

        if(this.tag.equals(tag)||mview== this.view){
            Log.d("xxx", "stop: "+tag+"   "+mview);
            view.setImageResource(defRes);
            this.view=mview;
            animator.cancel();
           view.setImageResource(defRes);
        }


    }
}
