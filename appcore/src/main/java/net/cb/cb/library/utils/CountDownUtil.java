package net.cb.cb.library.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import net.cb.cb.library.R;


/**
 * Created  2018/11/23
 */

public class CountDownUtil {

    @SuppressLint("StaticFieldLeak")
    private static CountDownTimer  timer;
    private static boolean isTask = true;

    /**
     * 由于该倒计时类会存在不会显示0秒,且最后1秒实际是接近2秒的时间,因此处理时将剩余秒数多减了一秒
     * 在创建timer时,倒计时的秒数应该多加1秒,自动计时类计时时会产生毫秒值得误差,如果去整数的值
     * 在计算时可能会出现跳秒的情况(实际倒计时的秒数差的不大,就几十毫秒),为了给计秒做补偿,多加500毫秒
     * 保证误差同时也能保证计秒准确
     *
     * @param second      需要设置的倒计时秒数
     * @param view        倒计时运行时需要设置文本变化的控件TextView或者Button
     * @param defaultText 计时结束后view上显示的内容
     */
    public static void getTimer(int second, final View view, final String defaultText, final Context context, final CallTask callTask) {
        timer = new CountDownTimer(second * 1000 + 1500, 1000) {
            @SuppressLint("DefaultLocale")

            @Override
            public void onTick(long millisUntilFinished) {
                if(callTask != null && isTask){
                    isTask = false;
                    callTask.task();
                }
                long remainderTime = millisUntilFinished / 1000 - 1;
                //判断view是否是TextView,如果是就设置显示倒计时的文本(Button是TextView子类)
                //如果是TextView的话,设置显示倒计时同时设置view不可点击
                if (view instanceof TextView) {
                    ((TextView) view).setText(String.format("%ds", remainderTime)+"后重新发送");
                    ((TextView) view).setTextColor(ContextCompat.getColor(context, R.color.gray_250));
                }
                if (remainderTime == 0) {
                    //判断为1秒时,结束计时,并恢复view可以点击
                    onFinish();
                    cancel();
                }
            }

            @Override
            public void onFinish() {
                isTask = true;
                view.setClickable(true);
                if (view instanceof TextView) {
                    ((TextView) view).setText(defaultText);
                    ((TextView) view).setTextColor(ContextCompat.getColor(context, R.color.green_600));
                }
            }
        };
        //开启计时器
        timer.start();
        //设置不能被点击
        view.setClickable(false);
    }

    /**
     * 取消计时器计时
     */
    public static void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer.onFinish();
        }
    }

    public interface CallTask{
        void task();
    }


}
