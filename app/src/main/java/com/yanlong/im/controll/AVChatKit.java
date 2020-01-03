package com.yanlong.im.controll;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;

import com.example.nim_lib.config.Preferences;
import com.example.nim_lib.controll.AVChatProfile;
import com.example.nim_lib.ui.VideoActivity;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.yanlong.im.MainActivity;
import com.yanlong.im.R;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.utils.LogUtil;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-10-18
 * @updateAuthor
 * @updateDate
 * @description 云信音视频组件定制化入口
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class AVChatKit {

    public static AVChatKit INSTANCE = null;

    public static AVChatKit getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AVChatKit();
        }
        return INSTANCE;
    }

    private static final String TAG = AVChatKit.class.getSimpleName();

    private static Context context;

    private static String account;

    private static boolean mainTaskLaunching;


    private static UserDao userDao = new UserDao();

    private static SparseArray<Notification> notifications = new SparseArray<>();

    public void init(Context c) {
        context = c;
        registerAVChatIncomingCallObserver(true);
    }

    public static void setContext(Context context) {
        AVChatKit.context = context;
    }

    public static Context getContext() {
        return context;
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        AVChatKit.account = account;
    }

    public static void setMainTaskLaunching(boolean mainTaskLaunching) {
        mainTaskLaunching = mainTaskLaunching;
    }

    public static boolean isMainTaskLaunching() {
        return mainTaskLaunching;
    }

    /**
     * 获取通知栏提醒数组
     */
    public static SparseArray<Notification> getNotifications() {
        return notifications;
    }

    /**
     * 注册音视频来电观察者
     *
     * @param register 注册或注销
     */
    private void registerAVChatIncomingCallObserver(boolean register) {
        AVChatManager.getInstance().observeIncomingCall(inComingCallObserver, register);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
    }

    /**
     * 注册/注销网络来电.
     * 当收到对方来电请求时，会通知上层通话信息。
     * 用户可以选择 {@link AVChatManager#accept2(long, AVChatCallback)} 来接听电话，
     * 或者 {@link AVChatManager#hangUp2(long, AVChatCallback)}  来挂断电话。 通常在收到来电请求时，上层需要维持
     * 一个超时器，当超过一定时间没有操作时直接调用 {@link AVChatManager#hangUp2(long, AVChatCallback)} 来挂断。
     * 当用户当前有电话在进行时，如果收到来电请求，需要选择是接听当前电话和是继续原来的通话。如果接听当前来电，则需要
     * {@link AVChatManager#hangUp2(long, AVChatCallback)} 原来进行的电话，然后 {@link AVChatManager#accept2(long, AVChatCallback)}
     * 当前来电。 如果选择继续原来的通话，挂断当前来电，最好能够先发送一个正忙的指令给对方 {@link AVChatManager#sendControlCommand(long, byte, AVChatCallback)}，
     * 然后在再挂断 {@link AVChatManager#hangUp2(long, AVChatCallback)} 当前通话。
     *
     * @param observer 观察者，参数为被叫通话的基本信息
     * @param register {@code true} 注册监听，{@code false} 注销监听
     */
    private Observer<AVChatData> inComingCallObserver = new Observer<AVChatData>() {
        @Override
        public void onEvent(final AVChatData data) {
//            if (PhoneCallStateObserver.getInstance().getPhoneCallState() != PhoneCallStateObserver.PhoneCallStateEnum.IDLE
//                    || AVChatProfile.getInstance().isAVChatting()
//                    || AVChatManager.getInstance().getCurrentChatId() != 0) {
//                LogUtil.getLog().i(TAG, "reject incoming call data =" + data.toString() + " as local phone is not idle");
//                AVChatManager.getInstance().sendControlCommand(data.getChatId(), AVChatControlCommand.BUSY, null);// 用户正忙
//                return;
//            }
            LogUtil.getLog().i(TAG, "收到来电：" + data.getAccount());
//            AVChatProfile.getInstance().setAVChatting(true);
            AVChatProfile.getInstance().setCallIng(true);

            if (data != null) {
                getUserInfo(data);
            }
        }
    };

    /**
     * 用户状态变化
     */
    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
//            LogUtil.getLog().i(TAG,"网易云用户状态变化返回状态："+code);
        }
    };

    @SuppressLint("CheckResult")
    private void getUserInfo(AVChatData data) {
        Observable.just(0)
                .map(new Function<Integer, UserInfo>() {
                    @Override
                    public UserInfo apply(Integer integer) throws Exception {
                        UserInfo userInfo = null;
                        if (userDao != null) {
                            userInfo = userDao.findUserInfo(data.getAccount());
                        }
                        return userInfo;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(Observable.<UserInfo>empty())
                .subscribe(new Consumer<UserInfo>() {
                    @Override
                    public void accept(UserInfo userInfo) throws Exception {
                        String extra = data.getExtra();
                        LogUtil.getLog().e(TAG, "Extra Message->" + extra);

                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Preferences.AVCHATDATA, data);
                        if (userInfo != null) {
                            intent.putExtra(Preferences.USER_HEAD_SCULPTURE, userInfo.getHead());
                            if (!TextUtils.isEmpty(userInfo.getMkName())) {
                                intent.putExtra(Preferences.USER_NAME, userInfo.getMkName());
                            } else {
                                intent.putExtra(Preferences.USER_NAME, userInfo.getName());
                            }
                        }
                        if (!TextUtils.isEmpty(extra)) {
                            try {
                                Map<String, String> map = new Gson().fromJson(extra, Map.class);
                                String roomId = map.get("roomId");
                                Long friend = Long.parseLong(map.get("friend"));
                                intent.putExtra(Preferences.ROOM_ID, roomId);
                                intent.putExtra(Preferences.FRIEND, friend);
                            } catch (JsonSyntaxException exception) {

                            }
                        }
                        intent.putExtra(Preferences.VOICE_TYPE, CoreEnum.VoiceType.RECEIVE);
                        intent.putExtra(Preferences.AVCHA_TTYPE, data.getChatType().getValue());
                        intent.setClass(context, VideoActivity.class);
                        // TODO oppo 必须要改开机自启动，或开启悬浮窗权限才能生效 ，文章地址：https://www.jianshu.com/p/5f6d8379533b
                        context.startActivity(intent);
                        LogUtil.getLog().e(TAG, "startActivity");
                    }
                });

    }
}