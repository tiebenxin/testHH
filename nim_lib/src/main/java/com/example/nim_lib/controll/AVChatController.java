package com.example.nim_lib.controll;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.example.nim_lib.action.VideoAction;
import com.example.nim_lib.config.AVChatConfigs;
import com.example.nim_lib.constant.AVChatExitCode;
import com.example.nim_lib.module.AVSwitchListener;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.video.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.video.AVChatSurfaceViewRenderer;
import com.netease.nimlib.sdk.avchat.video.AVChatVideoCapturerFactory;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.RunUtils;
import net.cb.cb.library.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-10-18
 * @updateAuthor
 * @updateDate
 * @description 音视频控制器：用于实现音视频拨打接听，音视频切换的具体功能实现
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class AVChatController {

    private Context context;
    private final String TAG = AVChatController.class.getName();
    private VideoAction mVideoAction;

    private AVChatCameraCapturer mVideoCapturer;

    public AVChatController(Context c, VideoAction videoAction) {
        context = c;
        mVideoAction = videoAction;
    }

    /**
     * 切换摄像头（主要用于前置和后置摄像头切换）
     */
    public void switchCamera() {
        if (mVideoCapturer != null) {
            mVideoCapturer.switchCamera();
        }
    }

    /**
     * 挂断
     *
     * @param chatId     网易ID
     * @param type
     * @param avChatType AVChatType.VIDEO AVChatType.AUDIO\e
     */
    public void hangUp2(long chatId, int type, int avChatType, Long toUId) {
        if ((type == AVChatExitCode.HANGUP || type == AVChatExitCode.PEER_NO_RESPONSE
                || type == AVChatExitCode.CANCEL || type == AVChatExitCode.REJECT)) {
            AVChatManager.getInstance().hangUp2(chatId, new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
//                    AVChatProfile.getInstance().setAVChatting(false);
                    AVChatProfile.getInstance().setCallIng(false);
                    PlayerManager.getManager().stop();
                    AVChatSoundPlayer.instance().stop();
                    auVideoHandup(toUId, avChatType, getUUID());
                    if (context != null && !((Activity) context).isFinishing()) {
                        ((Activity) context).finish();
                    }
                    Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailed(int code) {
                    Log.d(TAG, "onSuccess");
                    if (context != null && !((Activity) context).isFinishing()) {
                        ToastUtil.show(context, AVChatExitCode.getCodeString(code));
                        ((Activity) context).finish();
                    }
                }

                @Override
                public void onException(Throwable throwable) {
                    Log.d(TAG, "throwable");
                    if (context != null && !((Activity) context).isFinishing()) {
                        ((Activity) context).finish();
                    }
                }
            });
        }
        if (avChatType == AVChatType.VIDEO.getValue()) {
            // 如果是视频通话，关闭视频模块
            AVChatManager.getInstance().disableVideo();
            // 如果是视频通话，需要先关闭本地预览
            AVChatManager.getInstance().stopVideoPreview();
        }
        //销毁音视频引擎和释放资源
        AVChatManager.getInstance().disableRtc();
    }

    /**
     * 拨打音视频
     *
     * @param account       网易ID
     * @param callTypeEnum  VIDEO、VOICE
     * @param largeRender
     * @param avChatConfigs
     * @param friend        通话接收人uid
     * @param roomId        网易房间id
     * @param callBack
     */
    public void outGoingCalling(String account, final AVChatType callTypeEnum, AVChatSurfaceViewRenderer largeRender,
                                AVChatConfigs avChatConfigs, Long friend, String roomId, AVChatCallback<AVChatData> callBack) {
        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        Map<String, String> map = new HashMap<>();
        map.put("friend", friend + "");
        map.put("roomId", roomId);
        // 附加字段
        notifyOption.extendMessage = new Gson().toJson(map);
        // 是否兼容WebRTC模式
//        notifyOption.webRTCCompat = webrtcCompat;
//        //默认forceKeepCalling为true，开发者如果不需要离线持续呼叫功能可以将forceKeepCalling设为false
//        notifyOption.forceKeepCalling = false;
        // 开启音视频引擎
        AVChatManager.getInstance().enableRtc();

        if (avChatConfigs == null) {
            avChatConfigs = new AVChatConfigs(context);
            try {
                // 设置自己需要的可选参数
                AVChatManager.getInstance().setParameters(avChatConfigs.getAvChatParameters());
            }catch (IllegalArgumentException e){

            }
        }
        // 视频通话
        if (callTypeEnum == AVChatType.VIDEO) {
            // 激活视频模块
            AVChatManager.getInstance().enableVideo();

            // 创建视频采集模块并且设置到系统中
            if (mVideoCapturer == null) {
                mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer(true, true);
                AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
            }

            if (largeRender == null) {
                largeRender = new AVChatSurfaceViewRenderer(context);
                // 设置本地预览画布
                AVChatManager.getInstance().setupLocalVideoRender(largeRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            }

            // 开始视频预览
            AVChatManager.getInstance().startVideoPreview();
        }
        // 呼叫
        AVChatManager.getInstance().call2(account, callTypeEnum, notifyOption, callBack);
    }

    /**
     * 接听来电 告知服务器，以便通知其他端
     *
     * @param chatId        网易ID
     * @param callTypeEnum  VIDEO、VOICE
     * @param avChatConfigs
     * @param callback
     */
    public void receiveInComingCall(long chatId, final AVChatType callTypeEnum, AVChatConfigs avChatConfigs, AVChatCallback<Void> callback) {
        // 开启音视频引擎
        AVChatManager.getInstance().enableRtc();
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer(true, true);
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
        }
        if (avChatConfigs == null) {
            avChatConfigs = new AVChatConfigs(context);
            //设置自己需要的可选参数
            AVChatManager.getInstance().setParameters(avChatConfigs.getAvChatParameters());
        }

        if (callTypeEnum == AVChatType.VIDEO) {
            // 激活视频模块
            AVChatManager.getInstance().enableVideo();
            // 开启视频预览
            AVChatManager.getInstance().startVideoPreview();
        }

        AVChatManager.getInstance().accept2(chatId, callback);
    }

    /**
     * 拒绝来电
     *
     * @param avChatType VIDEO、VOICE
     */
    public void handleAcceptFailed(AVChatType avChatType) {
        if (avChatType == AVChatType.VIDEO) {
            AVChatManager.getInstance().stopVideoPreview();
            AVChatManager.getInstance().disableVideo();
        }
        AVChatManager.getInstance().disableRtc();
        if (!((Activity) context).isFinishing()) {
            ((Activity) context).finish();
        }
//        closeSessions(AVChatExitCode.CANCEL);
    }

    /**
     * 设置扬声器是否开启
     */
    public void toggleSpeaker() {
        AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled());
    }

    /**
     * 音频开关
     */
    public void toggleMute() {
        if (!AVChatManager.getInstance().isLocalAudioMuted()) { // isMute是否处于静音状态
            // 关闭音频
            AVChatManager.getInstance().muteLocalAudio(true);
        } else {
            // 打开音频
            AVChatManager.getInstance().muteLocalAudio(false);
        }
    }

    /**
     * ********************* 音视频切换 ***********************
     */

    /**
     * 发送视频切换为音频命令
     *
     * @param chatId
     * @param avSwitchListener
     */
    public void switchVideoToAudio(long chatId, final AVSwitchListener avSwitchListener) {
        AVChatManager.getInstance().sendControlCommand(chatId, AVChatControlCommand.SWITCH_VIDEO_TO_AUDIO, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.getLog().d(TAG, "videoSwitchAudio onSuccess");
                //关闭视频
                AVChatManager.getInstance().stopVideoPreview();
                AVChatManager.getInstance().disableVideo();

                // 界面布局切换。
                avSwitchListener.onVideoToAudio();
            }

            @Override
            public void onFailed(int code) {
                LogUtil.getLog().d(TAG, "videoSwitchAudio onFailed");
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.getLog().d(TAG, "videoSwitchAudio onException");
            }
        });
    }

    /**
     * 点对点语音挂断(已完成)
     *
     * @param friend 通话接收人uid
     * @param type   通话类型(1:音频|2:视频)
     * @param roomId 网易房间id
     */
    public void auVideoHandup(Long friend, int type, String roomId) {
        new RunUtils(new RunUtils.Enent() {
            @Override
            public void onRun() {

            }

            @Override
            public void onMain() {
                mVideoAction.auVideoHandup(friend, type, roomId, new CallBack<ReturnBean>() {
                    @Override
                    public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                        super.onResponse(call, response);
                        LogUtil.getLog().i(TAG,"点对点语音挂断(已完成)");
                    }

                    @Override
                    public void onFailure(Call<ReturnBean> call, Throwable t) {
                        super.onFailure(call, t);
                    }
                });
            }
        }).run();
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /***
     * 清理通知栏
     */
    public void taskClearNotification(Context context) {
        Log.i("VideoActivity","taskClearNotification");
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

}
