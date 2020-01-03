package com.yanlong.im.view;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.luck.picture.lib.tools.DateUtils;
import com.yanlong.im.R;

import net.cb.cb.library.utils.LogUtil;

/**
 * @创建人 shenxin
 * @创建时间 2019/11/5 0005 13:48
 */
public class CountDownView extends LinearLayout {
    private View view;
    private Context context;
    private ImageView imCountDown;
    private CountDownTimer timer;


    public CountDownView(Context context) {
        super(context);
        initView(context);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    //这是倒计时执行方法
    public void setRunTimer(long startTime, long endTime) {
        LogUtil.getLog().i("CountDownView","setRunTimer");
        timer = new CountDownTimer(endTime - startTime,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setImagePostion(startTime,endTime);
            }

            @Override
            public void onFinish() {
                timer.cancel();
            }
        }.start();
    }


    public void timerStop(){
        imCountDown.setImageResource(R.mipmap.icon_st_1);
        if(timer != null){
            timer.cancel();
        }
    }



    private void initView(Context context) {
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.view_count_down, this);
        imCountDown = view.findViewById(R.id.im_count_down);
        imCountDown.setImageResource(R.mipmap.icon_st_1);
    }


    public void setImagePostion(long startTime, long endTime) {
        long nowTime = DateUtils.getSystemTime();
        int time = (int) ((endTime - nowTime) / ((endTime - startTime) / 12));
        isME(time);
    }


    private void isME(int type){
        switch (type){
            case 12:
                imCountDown.setImageResource(R.mipmap.icon_st_1);
                break;
            case 11:
                imCountDown.setImageResource(R.mipmap.icon_st_1);
                break;
            case 10:
                imCountDown.setImageResource(R.mipmap.icon_st_3);
                break;
            case 9:
                imCountDown.setImageResource(R.mipmap.icon_st_4);
                break;
            case 8:
                imCountDown.setImageResource(R.mipmap.icon_st_5);
                break;
            case 7:
                imCountDown.setImageResource(R.mipmap.icon_st_6);
                break;
            case 6:
                imCountDown.setImageResource(R.mipmap.icon_st_7);
                break;
            case 5:
                imCountDown.setImageResource(R.mipmap.icon_st_8);
                break;
            case 4:
                imCountDown.setImageResource(R.mipmap.icon_st_9);
                break;
            case 3:
                imCountDown.setImageResource(R.mipmap.icon_st_10);
                break;
            case 2:
                imCountDown.setImageResource(R.mipmap.icon_st_11);
                break;
            case 1:
                imCountDown.setImageResource(R.mipmap.icon_st_12);
                break;
            case 0:
                imCountDown.setImageResource(R.mipmap.icon_st_12);
                break;
//            default:
//                imCountDown.setImageResource(R.mipmap.icon_st_1);
//                break;
        }
    }







}
