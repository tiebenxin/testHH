package com.example.nim_lib.controll;

/**
 * Created by huangjun on 2015/5/12.
 */
public class AVChatProfile {

    private final String TAG = "AVChatProfile";
    // 是否显示音视频浮动按钮
    private boolean isAVMinimize = false;
    // 是否正在拨打电话
    private boolean isCallIng = false;
    // 电话是否接通
    private boolean isCallEstablished = false;
    private static String account;
    // 通话类型
    private int chatType;

    public static AVChatProfile getInstance() {
        return InstanceHolder.instance;
    }

    public boolean isAVMinimize() {
        return isAVMinimize;
    }

    public void setAVMinimize(boolean AVMinimize) {
        isAVMinimize = AVMinimize;
    }

    public boolean isCallIng() {
        return isCallIng;
    }

    public void setCallIng(boolean callIng) {
        isCallIng = callIng;
    }

    private static class InstanceHolder {
        public final static AVChatProfile instance = new AVChatProfile();
    }

    public boolean isCallEstablished() {
        return isCallEstablished;
    }

    public void setCallEstablished(boolean callEstablished) {
        isCallEstablished = callEstablished;
    }

    public int isChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }
    //    public void launchActivity(final AVChatData data, final String displayName, final int source) {
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                // 启动，如果 task正在启动，则稍等一下
//                if (!AVChatKit.isMainTaskLaunching()) {
//                    launchActivityTimeout();
////                    AVChatActivity.incomingCall(AVChatKit.getContext(), data, displayName, source);
//                } else {
//                    launchActivity(data, displayName, source);
//                }
//            }
//        };
//        Handlers.sharedHandler(AVChatKit.getContext()).postDelayed(runnable, 200);
//    }
//
//    public void activityLaunched() {
//        Handler handler = Handlers.sharedHandler(AVChatKit.getContext());
//        handler.removeCallbacks(launchTimeout);
//    }
//
//    // 有些设备（比如OPPO、VIVO）默认不允许从后台broadcast receiver启动activity
//    // 增加启动activity超时机制
//    private void launchActivityTimeout() {
//        Handler handler = Handlers.sharedHandler(AVChatKit.getContext());
//        handler.removeCallbacks(launchTimeout);
//        handler.postDelayed(launchTimeout, 3000);
//    }
//
//    private Runnable launchTimeout = new Runnable() {
//        @Override
//        public void run() {
//            // 如果未成功启动，就恢复av chatting -> false
//            setAVChatting(false);
//        }
//    };

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        AVChatProfile.account = account;
    }
}