package com.yanlong.im.notify;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.yanlong.im.user.ui.SplashActivity;
import com.yanlong.im.utils.socket.MsgBean;

import net.cb.cb.library.event.EventFactory;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * 自定义JPush message 接收器,包括操作tag/alias的结果返回(仅仅包含tag/alias新接口部分)
 */
public class MyJPushMessageReceiver extends JPushMessageReceiver {

    @Override
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        TagAliasOperatorHelper.getInstance().onTagOperatorResult(context, jPushMessage);
        super.onTagOperatorResult(context, jPushMessage);
    }

    @Override
    public void onCheckTagOperatorResult(Context context, JPushMessage jPushMessage) {
        TagAliasOperatorHelper.getInstance().onCheckTagOperatorResult(context, jPushMessage);
        super.onCheckTagOperatorResult(context, jPushMessage);
    }

    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        TagAliasOperatorHelper.getInstance().onAliasOperatorResult(context, jPushMessage);
        super.onAliasOperatorResult(context, jPushMessage);
    }

    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        TagAliasOperatorHelper.getInstance().onMobileNumberOperatorResult(context, jPushMessage);
        super.onMobileNumberOperatorResult(context, jPushMessage);
    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageArrived(context, notificationMessage);
        LogUtil.getLog().i("MyJPushMessageReceiver", "来消息了");
        String extras = notificationMessage.notificationExtras;
        // 如果是音视频通知，先停掉极光在打开，是为了解决小米手机进来通知铃声跟音视频铃声重复播放问题
        if (StringUtil.isNotNull(extras)) {
            Map<String, Double> map = new Gson().fromJson(extras, Map.class);
            Double msgType = map.get("msg_type");
            if (msgType == MsgBean.MessageType.P2P_AU_VIDEO_DIAL.getNumber()) {
                EventBus.getDefault().post(new EventFactory.StopJPushResumeEvent());
            }
        }
//        String title = notificationMessage.notificationTitle;
//        LogUtil.getLog().v("MyJPushMessageReceiver",title+"");
//
//        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//        String text = km.inKeyguardRestrictedInputMode() ? "锁屏了" : "屏幕亮着的";
//        if (km.inKeyguardRestrictedInputMode()) {
//            //判断是否锁屏
//            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
//            wl.acquire(); // 点亮屏幕
//            wl.release(); // 释放
//
//
//            Intent alarmIntent = new Intent(context, MessageActivity.class);
//            //在广播中启动Activity的context可能不是Activity对象，所以需要添加NEW_TASK的标志，否则启动时可能会报错。
//            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(alarmIntent); //启动显示锁屏消息的activity
//        }
    }


    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageOpened(context, notificationMessage);
        LogUtil.getLog().i("MyJPushMessageReceiver", "onNotifyMessageOpened");
        Intent intent = new Intent(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
