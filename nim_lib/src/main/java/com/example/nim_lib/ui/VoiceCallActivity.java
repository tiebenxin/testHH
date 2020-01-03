//package com.example.nim_lib.ui;
//
//import android.Manifest;
//import android.graphics.Rect;
//import android.os.Bundle;
//import android.os.Handler;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.Toast;
//
//import com.bumptech.glide.Glide;
//import com.example.nim_lib.R;
//import com.example.nim_lib.config.AVChatConfigs;
//import com.example.nim_lib.config.Preferences;
//import com.example.nim_lib.constant.AVChatExitCode;
//import com.example.nim_lib.controll.AVChatController;
//import com.example.nim_lib.controll.AVChatSoundPlayer;
//import com.example.nim_lib.databinding.ActivityVoiceCallBinding;
//import com.example.nim_lib.module.AVChatTimeoutObserver;
//import com.example.nim_lib.module.SimpleAVChatStateObserver;
//import com.example.nim_lib.permission.BaseMPermission;
//import com.example.nim_lib.receiver.PhoneCallStateObserver;
//import com.example.nim_lib.util.GlideUtil;
//import com.example.nim_lib.util.ScreenUtil;
//import com.example.nim_lib.util.ViewUtils;
//import com.netease.nimlib.sdk.Observer;
//import com.netease.nimlib.sdk.avchat.AVChatCallback;
//import com.netease.nimlib.sdk.avchat.AVChatManager;
//import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
//import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
//import com.netease.nimlib.sdk.avchat.constant.AVChatType;
//import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
//import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
//import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
//import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
//import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
//import com.netease.nimlib.sdk.avchat.model.AVChatData;
//import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
//import com.netease.nimlib.sdk.avchat.video.AVChatSurfaceViewRenderer;
//import com.netease.nrtc.video.render.IVideoRender;
//
//import net.cb.cb.library.CoreEnum;
//import net.cb.cb.library.event.EventFactory;
//import net.cb.cb.library.utils.LogUtil;
//import net.cb.cb.library.utils.ToastUtil;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.util.List;
//import java.util.Locale;
//
///**
// * @version V1.0
// * @createAuthor （Geoff）
// * @createDate 2019-10-18
// * @updateAuthor
// * @updateDate
// * @description 语音呼叫、接听、通话
// * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
// */
//public class VoiceCallActivity extends BaseBindActivity<ActivityVoiceCallBinding> implements View.OnClickListener {
//
//    private final String TAG = VoiceCallActivity.class.getName();
//    // 用户姓名、头像
//    private String mUserName = "", mUserHeadSculpture = "";
//    // 网易ID
//    private String mNeteaseaccId;
//    // 0 呼叫 1 接收  2通话
//    private int mVoiceType;
//    // 音视频控制器：用于实现音视频拨打接听，音视频切换的具体功能实现
//    private AVChatController mAVChatController;
//    // 设置自己需要的可选参数
//    private AVChatConfigs mAvChatConfigs;
//    // 是否销毁RTC
//    private boolean destroyRTC = false;
//    // 通话数据
//    private AVChatData mAvChatData;
//
//    private final int TIME = 1000;
//    // 通话时间
//    private int mPassedTime = 0;
//    Handler mHandler = new Handler();
//    // 通话类型
//    private int mAVChatType;
//    private Long toUId = null;
//    private String toGid = null;
//
//    // 视频
//    //render
//    private AVChatSurfaceViewRenderer smallRender;
//    private AVChatSurfaceViewRenderer largeRender;
//    private final String[] BASIC_PERMISSIONS = new String[]{Manifest.permission.CAMERA,};
//    public boolean canSwitchCamera = false;
//    private IVideoRender remoteRender;
//    private IVideoRender localRender;
//    private String mLargeAccount; // 显示在大图像的用户id
//    private String mSmallAccount; // 显示在小图像的用户id
//    private String mAccount;// 来电账号
//    private TouchZoneCallback touchZoneCallback;
//    // state
//    private boolean mIsInComingCall = false;// is incoming call or outgoing call
//    private boolean localPreviewInSmallSize = true;
//    private boolean isPeerVideoOff = false;
//    private boolean isCallEstablished = false; // 电话是否接通
//    private boolean surfaceInit = false;
//    private boolean isReleasedVideo = false;
//    private boolean isForeground = false;// 判断是否到前台显示
//    // move
//    private int lastX, lastY;
//    private int inX, inY;
//    private Rect paddingRect;
//
//    // constant
//    private static final int PEER_CLOSE_CAMERA = 0;
//    private static final int LOCAL_CLOSE_CAMERA = 1;
//    private static final int AUDIO_TO_VIDEO_WAIT = 2;
//    private static final int TOUCH_SLOP = 10;
//    private String largeAccount; // 显示在大图像的用户id
//    private String smallAccount; // 显示在小图像的用户id
//
//    // touch zone
//    public interface TouchZoneCallback {
//        void onTouch();
//    }
//
//    @Override
//    protected int setView() {
//        return R.layout.activity_voice_call;
//    }
//
//    @Override
//    protected void init(Bundle savedInstanceState) {
//
//        mAvChatConfigs = new AVChatConfigs(this);
//        mAVChatController = new AVChatController(this,null);
//        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            mUserName = bundle.getString(Preferences.USER_NAME);
//            mUserHeadSculpture = bundle.getString(Preferences.USER_HEAD_SCULPTURE);
//            mNeteaseaccId = bundle.getString(Preferences.NETEASEACC_ID);
//            mVoiceType = bundle.getInt(Preferences.VOICE_TYPE);
//            mAvChatData = (AVChatData) bundle.getSerializable(Preferences.AVCHATDATA);
//            mAVChatType = bundle.getInt(Preferences.AVCHA_TTYPE);
//            toUId =  bundle.getLong(Preferences.TOUID);
//            toGid =  bundle.getString(Preferences.TOGID);
//            switch (mVoiceType) {
//                case CoreEnum.VoiceType.WAIT:
//                    bindingView.layoutVoiceWait.setVisibility(View.VISIBLE);
//                    if (mAVChatType == AVChatType.AUDIO.getValue()) {
//                        outGoingCalling(AVChatType.AUDIO);
//                    } else {
//                        outGoingCalling(AVChatType.VIDEO);
//                    }
//                    AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.CONNECTING);
//                    bindingView.txtWaitMsg.setText(getString(R.string.avchat_wait_recieve1));
//                    break;
//                case CoreEnum.VoiceType.RECEIVE:
//                    if (mAVChatType == AVChatType.VIDEO.getValue()) {
////                        bindingView.imgAnswer.setImageResource(); TODO
//                        bindingView.txtWaitMsg.setText(getString(R.string.avchat_audio_to_video_invitation));
//                    } else {
//                        bindingView.txtWaitMsg.setText(R.string.avchat_audio_invitation);
//                    }
//                    AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.PEER_REJECT);
//                    bindingView.layoutInvitationVoice.setVisibility(View.VISIBLE);
//                    bindingView.layoutVoiceWait.setVisibility(View.GONE);
//                    break;
//                case CoreEnum.VoiceType.CALLING:
//                    if (mAVChatType == AVChatType.VIDEO.getValue()) {
//                        bindingView.layoutAudio.setVisibility(View.GONE);
//                        bindingView.layoutInvitationVoice.setVisibility(View.GONE);
//                        bindingView.layoutVideo.layoutVideoRoot.setVisibility(View.VISIBLE);
//                    } else {
//                        bindingView.layoutVoiceIng.setVisibility(View.VISIBLE);
//                    }
//                    break;
//            }
//        }
//    }
//
//    @Override
//    protected void initEvent() {
//        bindingView.imgCancle.setOnClickListener(this);
//        bindingView.imgAnswer.setOnClickListener(this);
//        bindingView.imgHandUp.setOnClickListener(this);
//        bindingView.imgRefuse.setOnClickListener(this);
//        bindingView.cbHandsFree.setOnClickListener(this);
//        bindingView.cbMute.setOnClickListener(this);
//        bindingView.imgMinimize.setOnClickListener(this);
//        bindingView.layoutVideo.imgHandUp2.setOnClickListener(this);
//    }
//
//    @Override
//    protected void loadData() {
//        bindingView.txtName.setText(mUserName);
//        Glide.with(this).load(mUserHeadSculpture)
//                .apply(GlideUtil.headImageOptions()).into(bindingView.imgHeadPortrait);
//        registerObserves(true);
//
//        this.smallRender = new AVChatSurfaceViewRenderer(this);
//        this.largeRender = new AVChatSurfaceViewRenderer(this);
//    }
//
//    @Override
//    protected void onDestroy() {
//        AVChatSoundPlayer.instance().stop();
//        super.onDestroy();
//        registerObserves(false);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        isForeground = true;
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        isForeground = false;
//    }
//
//    /**
//     * 通话计时
//     */
//    Runnable runnable = new Runnable() {
//
//        @Override
//        public void run() {
//            mPassedTime++;
//            int hour = mPassedTime / 3600;
//            int min = mPassedTime % 3600 / 60;
//            int second = mPassedTime % 60;
//
//            if (!isFinishing()) {
//                mHandler.postDelayed(this, TIME);
//                if (hour > 0) {
//                    bindingView.txtLifeTime.setText(String.format(Locale.CHINESE, "%02d:%02d:%02d", hour, min, second));
//                } else {
//                    bindingView.txtLifeTime.setText(String.format(Locale.CHINESE, "%02d:%02d", min, second));
//                }
//            }
//        }
//    };
//
//    /**
//     * ********************** surface 初始化 **********************
//     */
//    private void findSurfaceView() {
//        if (surfaceInit) {
//            return;
//        }
//        bindingView.layoutVideo.touchZone.setOnTouchListener(touchListener);
//        bindingView.layoutVideo.smallSizePreviewLayout.setOnTouchListener(smallPreviewTouchListener);
//        surfaceInit = true;
//    }
//
//    private View.OnTouchListener touchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_UP && touchZoneCallback != null) {
//                touchZoneCallback.onTouch();
//            }
//
//            return true;
//        }
//    };
//
//    /**
//     * 对方接受后更新UI
//     */
//    private void onAudioAgree() {
//        AVChatSoundPlayer.instance().stop();
//        if (mAVChatType == AVChatType.VIDEO.getValue()) {
//            bindingView.layoutAudio.setVisibility(View.GONE);
//            bindingView.layoutInvitationVoice.setVisibility(View.GONE);
//            bindingView.layoutVideo.layoutVideoRoot.setVisibility(View.VISIBLE);
//        } else {
//            bindingView.layoutVoiceIng.setVisibility(View.VISIBLE);
//            bindingView.txtWaitMsg.setVisibility(View.GONE);
//            bindingView.layoutVoiceWait.setVisibility(View.GONE);
//            bindingView.layoutInvitationVoice.setVisibility(View.GONE);
//        }
//    }
//
//    /**
//     * 注册观察者
//     *
//     * @param register
//     */
//    private void registerObserves(boolean register) {
//        AVChatManager.getInstance().observeAVChatState(avchatStateObserver, register);
//        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, register);
//        AVChatManager.getInstance().observeControlNotification(callControlObserver, register);
//        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, register);
//        AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, register, register, this);
//        PhoneCallStateObserver.getInstance().observeAutoHangUpForLocalPhone(autoHangUpForLocalPhoneObserver, register);
//    }
//
//    /**
//     * ****************************** 通话过程状态监听 监听器 **********************************
//     */
//    private SimpleAVChatStateObserver avchatStateObserver = new SimpleAVChatStateObserver() {
//        @Override
//        public void onAVRecordingCompletion(String account, String filePath) {
//            Log.d(TAG, "onAVRecordingCompletion -> " + account);
////            if (account != null && filePath != null && filePath.length() > 0) {
////                String msg = "音视频录制已结束, " + "账号：" + account + " 录制文件已保存至：" + filePath;
////                Toast.makeText(AVChatActivity.this, msg, Toast.LENGTH_SHORT).show();
////            } else {
////                Toast.makeText(AVChatActivity.this, "录制已结束.", Toast.LENGTH_SHORT).show();
////            }
////            if (state == AVChatType.VIDEO.getValue()) {
////                avChatVideoUI.resetRecordTip();
////            } else {
////                avChatAudioUI.resetRecordTip();
////            }
//        }
//
//        @Override
//        public void onAudioRecordingCompletion(String filePath) {
//            Log.d(TAG, "onAudioRecordingCompletion -> " + filePath);
////            if (filePath != null && filePath.length() > 0) {
////                String msg = "音频录制已结束, 录制文件已保存至：" + filePath;
////                Toast.makeText(AVChatActivity.this, msg, Toast.LENGTH_SHORT).show();
////            } else {
////                Toast.makeText(AVChatActivity.this, "录制已结束.", Toast.LENGTH_SHORT).show();
////            }
////            if (state == AVChatType.AUDIO.getValue()) {
////                avChatAudioUI.resetRecordTip();
////            } else {
////                avChatVideoUI.resetRecordTip();
////            }
//        }
//
//        @Override
//        public void onLowStorageSpaceWarning(long availableSize) {
//            Log.d(TAG, "onLowStorageSpaceWarning -> " + availableSize);
////            if (state == AVChatType.VIDEO.getValue()) {
////                avChatVideoUI.showRecordWarning();
////            } else {
////                avChatAudioUI.showRecordWarning();
////            }
//        }
//
//        @Override
//        public void onJoinedChannel(int code, String audioFile, String videoFile, int i) {
//            Log.d(TAG, "audioFile -> " + audioFile + " videoFile -> " + videoFile);
////            handleWithConnectServerResult(code);
//        }
//
//        @Override
//        public void onUserJoined(String account) {
//            Log.d(TAG, "onUserJoin -> " + account);
////            Log.d("1212", "onUserJoin:" + account);
//            mIsInComingCall = true;
//            if (mAVChatType == AVChatType.VIDEO.getValue()) {
//                bindingView.layoutAudio.setVisibility(View.GONE);
//                bindingView.layoutVideo.layoutVideoRoot.setVisibility(View.VISIBLE);
//                bindingView.layoutVoiceIng.setVisibility(View.GONE);
//
//                initLargeSurfaceView(account);
//            }
//        }
//
//        @Override
//        public void onUserLeave(String account, int event) {
//            Log.d(TAG, "onUserLeave -> " + account);
////            manualHangUp(AVChatExitCode.HANGUP);
//            finish();
//        }
//
//        @Override
//        public void onCallEstablished() {
//            Log.d(TAG, "onCallEstablished");
////            //移除超时监听
//            AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, false, mIsInComingCall, VoiceCallActivity.this);
////            if (avChatController.getTimeBase() == 0)
////                avChatController.setTimeBase(SystemClock.elapsedRealtime());
//
//            if (mAVChatType == AVChatType.AUDIO.getValue()) {
////                showAudioInitLayout();
//            } else {
//                // 接通以后，自己是小屏幕显示图像，对方是大屏幕显示图像
//                initSmallSurfaceView();
////                showVideoInitLayout();
//                findSurfaceView();
//            }
//            isCallEstablished = true;
//            if (!isFinishing() && !isForeground) {
//                sendVoiceMinimizeEventBus();
//            }
//        }
//
//        @Override
//        public boolean onVideoFrameFilter(AVChatVideoFrame frame, boolean maybeDualInput) {
////            if (faceU != null) {
////                faceU.effect(frame.data, frame.width, frame.height, FaceU.VIDEO_FRAME_FORMAT.I420);
////            }
//
//            return true;
//        }
//
//        @Override
//        public boolean onAudioFrameFilter(AVChatAudioFrame frame) {
//            return true;
//        }
//
//    };
//
//    /**
//     * 监听音视频模式切换通知, 对方音视频开关通知
//     */
//    Observer<AVChatControlEvent> callControlObserver = new Observer<AVChatControlEvent>() {
//        @Override
//        public void onEvent(AVChatControlEvent netCallControlNotification) {
//            Log.d(TAG, "通话结束");
//            handleCallControl(netCallControlNotification);
//        }
//    };
//
//    /**
//     * 来电超时，未接听
//     */
//    Observer<Integer> timeoutObserver = new Observer<Integer>() {
//        @Override
//        public void onEvent(Integer integer) {
//            manualHangUp(AVChatExitCode.CANCEL);
//            // 来电超时，自己未接听
//            if (mIsInComingCall) {
////                activeMissCallNotifier();
//            }
//            finish();
//        }
//    };
//
//    Observer<Integer> autoHangUpForLocalPhoneObserver = new Observer<Integer>() {
//        @Override
//        public void onEvent(Integer integer) {
//            hangUpByOther(AVChatExitCode.PEER_BUSY);
//        }
//    };
//
//    /**
//     * 注册/注销网络通话被叫方的响应（接听、拒绝、忙）
//     *
//     * @param observer 观察者， 参数为接收到的网络通话的通知消息
//     * @param register {@code true} 注册监听，{@code false} 注销监听
//     */
//    private Observer<AVChatCalleeAckEvent> callAckObserver = new Observer<AVChatCalleeAckEvent>() {
//        @Override
//        public void onEvent(AVChatCalleeAckEvent ackInfo) {
//            AVChatSoundPlayer.instance().stop();
//            if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {
//                // 对方正在忙
//                LogUtil.getLog().i(TAG, "对方正在忙");
//                ToastUtil.show(VoiceCallActivity.this, "对方正在忙");
//                if (mAvChatData != null) {
//                    mAVChatController.hangUp2(mAvChatData.getChatId(), AVChatExitCode.PEER_BUSY, AVChatType.AUDIO.getValue(),null);
//                }
//
//            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
//                // 对方拒绝接听
//                LogUtil.getLog().i(TAG, "对方拒绝接听");
//                ToastUtil.show(VoiceCallActivity.this, "对方拒绝接听");
//                if (mAvChatData != null) {
//                    mAVChatController.hangUp2(mAvChatData.getChatId(), AVChatExitCode.REJECT, AVChatType.AUDIO.getValue(),null);
//                }
//            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
//                // 对方同意接听
//                LogUtil.getLog().i(TAG, "对方同意接听");
//                if (!isFinishing()) {
//                    mHandler.postDelayed(runnable, TIME);
//                }
//                onAudioAgree();
//            } else {
//                LogUtil.getLog().i(TAG, "other");
//            }
//        }
//    };
//
//    /**
//     * 通话过程中，收到对方挂断电话
//     */
//    Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
//        @Override
//        public void onEvent(AVChatCommonEvent avChatHangUpInfo) {
////            avChatData = avChatController.getAvChatData();
//            Log.d(TAG, "对方挂断电话");
//            if (mAvChatData != null && mAvChatData.getChatId() == avChatHangUpInfo.getChatId()) {
//                EventFactory.CloseVoiceMinimizeEvent event = new EventFactory.CloseVoiceMinimizeEvent();
//                event.avChatType = mAVChatType;
//                event.operation = "hangup";
//                event.txt = "通话时长 " + bindingView.txtLifeTime.getText().toString();
//                EventBus.getDefault().post(event);
//                if (!isFinishing()) {
//                    mHandler.removeCallbacks(runnable);
//                }
//                hangUpByOther(AVChatExitCode.HANGUP);
////                cancelCallingNotifier();
////                // 如果是incoming call主叫方挂断，那么通知栏有通知
////                if (mIsInComingCall && !isCallEstablished) {
////                    activeMissCallNotifier();
//            }
//        }
//    };
//
//    /**
//     * 处理音视频切换请求和对方音视频开关通知
//     *
//     * @param notification
//     */
//    private void handleCallControl(AVChatControlEvent notification) {
//        if (AVChatManager.getInstance().getCurrentChatId() != notification.getChatId()) {
//            return;
//        }
//        switch (notification.getControlCommand()) {
//            case AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO:
////                incomingAudioToVideo();
//                break;
//            case AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO_AGREE:
//                // 对方同意切成视频啦
////                state = AVChatType.VIDEO.getValue();
//                onAudioToVideoAgree(notification.getAccount());
//                break;
//            case AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO_REJECT:
////                rejectAudioToVideo();
////                Toast.makeText(AVChatActivity.this, R.string.avchat_switch_video_reject, Toast.LENGTH_SHORT).show();
//                break;
//            case AVChatControlCommand.SWITCH_VIDEO_TO_AUDIO:
////                onVideoToAudio();
//                break;
//            case AVChatControlCommand.NOTIFY_VIDEO_OFF:
//                // 收到对方关闭画面通知
////                if (state == AVChatType.VIDEO.getValue()) {
//////                    avChatVideoUI.peerVideoOff();
////                }
//                break;
//            case AVChatControlCommand.NOTIFY_VIDEO_ON:
//                // 收到对方打开画面通知
////                if (state == AVChatType.VIDEO.getValue()) {
//////                    avChatVideoUI.peerVideoOn();
////                }
//                break;
//            default:
//                Toast.makeText(this, "对方发来指令值：" + notification.getControlCommand(), Toast.LENGTH_SHORT).show();
//                break;
//        }
//    }
//
//    /**
//     * 对方同意切成视频啦 刷新界面
//     *
//     * @param largeAccount
//     */
//    public void onAudioToVideoAgree(String largeAccount) {
//        Log.d(TAG, "对方账号" + largeAccount);
////        showVideoInitLayout();
//        findSurfaceView();
//        mAccount = largeAccount;
//
////        muteToggle.toggle(AVChatManager.getInstance().isLocalAudioMuted() ? ToggleState.ON : ToggleState.OFF);
////        closeCameraToggle.toggle(ToggleState.OFF);
////        switchCameraToggle.off(false);
////        recordToggle.setEnabled(true);
////        recordToggle.setSelected(avChatController.isRecording());
//
//        //打开视频
//        isReleasedVideo = false;
//        smallRender = new AVChatSurfaceViewRenderer(this);
//        largeRender = new AVChatSurfaceViewRenderer(this);
//
//        //打开视频
//        AVChatManager.getInstance().enableVideo();
//        AVChatManager.getInstance().startVideoPreview();
//
//        initSmallSurfaceView();
//        // 是否在发送视频 即摄像头是否开启
//        if (AVChatManager.getInstance().isLocalVideoMuted()) {
//            AVChatManager.getInstance().muteLocalVideo(false);
////            localVideoOn();
//        }
//
////        initLargeSurfaceView(largeAccount);
////        showRecordView(avChatController.isRecording(), isRecordWarning);
//    }
//
//    /**
//     * 被对方挂断
//     *
//     * @param exitCode
//     */
//    private void hangUpByOther(int exitCode) {
//        if (exitCode == AVChatExitCode.PEER_BUSY) {
//            if (mAvChatData != null) {
//                mAVChatController.hangUp2(mAvChatData.getChatId(), AVChatExitCode.HANGUP, AVChatType.AUDIO.getValue(),null);
//            }
//        } else {
//            handleAcceptFailed();
//        }
//    }
//
//    /**
//     * 主动挂断
//     *
//     * @param exitCode
//     */
//    private void manualHangUp(int exitCode) {
//        mAVChatController.handleAcceptFailed(AVChatType.AUDIO);
//        destroyRTC = true;
//    }
//
//    /**
//     * 拨打音视频
//     */
//    private void outGoingCalling(AVChatType avChatType) {
//        mAVChatController.outGoingCalling(mNeteaseaccId + "", avChatType, null , mAvChatConfigs,null,"",
//                new AVChatCallback<AVChatData>() {
//                    @Override
//                    public void onSuccess(AVChatData avChatData) {
//                        mAvChatData = avChatData;
//
//                        List<String> deniedPermissions = BaseMPermission.getDeniedPermissions(VoiceCallActivity.this, BASIC_PERMISSIONS);
//                        if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
////                    showNoneCameraPermissionView(true);
//                            return;
//                        }
//                        canSwitchCamera = true;
//                        if (mAVChatType == AVChatType.VIDEO.getValue()) {
//                            initLargeSurfaceView(avChatData.getAccount());
//                        }
////                        Toast.makeText(VoiceCallActivity.this, "onSuccess", Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onFailed(int i) {
//                        Toast.makeText(VoiceCallActivity.this, "onFailed", Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onException(Throwable throwable) {
//                        Toast.makeText(VoiceCallActivity.this, "onException", Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
//
//    /**
//     * 接听来电 告知服务器，以便通知其他端
//     *
//     * @param chatId 对方来电ID
//     */
//    private void receiveInComingCall(long chatId) {
//        mAVChatController.receiveInComingCall(chatId, AVChatType.AUDIO , mAvChatConfigs,
//                new AVChatCallback<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        if (!isFinishing()) {
//                            mHandler.postDelayed(runnable, TIME);
//                        }
//                        onAudioAgree();
//                    }
//
//                    @Override
//                    public void onFailed(int code) {
//                        if (code == -1) {
//                            Toast.makeText(VoiceCallActivity.this, "本地音视频启动失败", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(VoiceCallActivity.this, "建立连接失败", Toast.LENGTH_SHORT).show();
//                        }
//                        handleAcceptFailed();
//                    }
//
//                    @Override
//                    public void onException(Throwable throwable) {
//                        Toast.makeText(VoiceCallActivity.this, "onException", Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
//
//    /**
//     * 拒绝来电
//     */
//    private void handleAcceptFailed() {
//        if (destroyRTC) {
//            return;
//        }
//        mAVChatController.handleAcceptFailed(AVChatType.AUDIO);
//        destroyRTC = true;
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (ViewUtils.isFastDoubleClick()) {
//            return;
//        }
//        // 取消、拒绝、挂断
//        if (v.getId() == R.id.img_cancle || v.getId() == R.id.img_hand_up
//                || v.getId() == R.id.img_refuse || v.getId() == R.id.img_hand_up2) {
//            if (mAvChatData != null) {
//                mAVChatController.hangUp2(mAvChatData.getChatId(), AVChatExitCode.HANGUP, AVChatType.AUDIO.getValue(),null);
//            }
//        } else if (v.getId() == R.id.img_answer) {// 接听
//            if (mAvChatData != null) {
//                receiveInComingCall(mAvChatData.getChatId());
//            }
//        } else if (v.getId() == R.id.cb_hands_free) {// 免提
//            mAVChatController.toggleSpeaker();
//        } else if (v.getId() == R.id.cb_mute) {// 音频开关
//            mAVChatController.toggleMute();
//        } else if (v.getId() == R.id.img_minimize) {// 最小化
//            // 停止计时器
////            handler.removeCallbacks(runnable);
//            sendVoiceMinimizeEventBus();
//            // 退到后台不显
//            moveTaskToBack(true);
//        }
//    }
//
//    private void sendVoiceMinimizeEventBus() {
//        EventFactory.VoiceMinimizeEvent event = new EventFactory.VoiceMinimizeEvent();
//        event.passedTime = mPassedTime;
//        event.isCallEstablished = isCallEstablished;
//        event.showTime = bindingView.txtLifeTime.getText().toString();
//        EventBus.getDefault().post(event);
//    }
//
//    /**
//     * 大图像surface view 初始化
//     */
//    public void initLargeSurfaceView(String account) {
//        // 设置画布，加入到自己的布局中，用于呈现视频图像
//        // account 要显示视频的用户帐号
//        mLargeAccount = account;
//        if (!mIsInComingCall) {// AVChatKit.getAccount()
////            viewVideo.setVisibility(View.INVISIBLE);
//            Log.d(TAG, "setupLocalVideoRender：" + account);
//            AVChatManager.getInstance().setupLocalVideoRender(largeRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
//        } else {
////            viewVideo.setVisibility(View.VISIBLE);
//            Log.d(TAG, "setupRemoteVideoRender：" + account);
//            AVChatManager.getInstance().setupRemoteVideoRender(account, largeRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
//        }
//        addIntoLargeSizePreviewLayout(largeRender);
//        remoteRender = largeRender;
//    }
//
//    /**
//     * 小图像surface view 初始化
//     */
//    public void initSmallSurfaceView() {
//        mSmallAccount = mAccount;
//        bindingView.layoutVideo.smallSizePreviewLayout.setVisibility(View.VISIBLE);
//
//        // 设置画布，加入到自己的布局中，用于呈现视频图像
//        AVChatManager.getInstance().setupLocalVideoRender(null, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
//        AVChatManager.getInstance().setupLocalVideoRender(smallRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
//        addIntoSmallSizePreviewLayout(smallRender);
//
//        bindingView.layoutVideo.smallSizePreviewLayout.bringToFront();
//        localRender = smallRender;
//        localPreviewInSmallSize = true;
//        Log.d(TAG, "initSmallSurfaceView：");
//    }
//
//    /**
//     * 大图像surface添加到largeSizePreviewLayout
//     *
//     * @param surfaceView
//     */
//    private void addIntoLargeSizePreviewLayout(SurfaceView surfaceView) {
//        if (surfaceView.getParent() != null) {
//            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
//        }
//        bindingView.layoutVideo.largeSizePreview.removeAllViews();
//        bindingView.layoutVideo.largeSizePreview.addView(surfaceView);
//        bindingView.layoutVideo.largeSizePreview.invalidate();
//        surfaceView.setZOrderMediaOverlay(false);
//    }
//
//    /**
//     * 小图像surface添加到smallSizePreviewLayout
//     *
//     * @param surfaceView
//     */
//    private void addIntoSmallSizePreviewLayout(SurfaceView surfaceView) {
//        bindingView.layoutVideo.smallSizePreviewCoverImg.setVisibility(View.GONE);
//        if (surfaceView.getParent() != null) {
//            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
//        }
//
//        bindingView.layoutVideo.smallSizePreview.removeAllViews();
//        bindingView.layoutVideo.smallSizePreview.addView(surfaceView);
//        surfaceView.setZOrderMediaOverlay(true);
//        bindingView.layoutVideo.smallSizePreview.setVisibility(View.VISIBLE);
//        bindingView.layoutVideo.smallSizePreview.invalidate();
//    }
//
//    /**
//     * 大小图像显示切换
//     *
//     * @param user1
//     * @param user2
//     */
//    private void switchRender(String user1, String user2) {
//        String remoteId = TextUtils.equals(user1, mAvChatData.getAccount()) ? user2 : user1;
//
//        if (remoteRender == null && localRender == null) {
//            localRender = smallRender;
//            remoteRender = largeRender;
//        }
//
//        //交换
//        IVideoRender render = localRender;
//        localRender = remoteRender;
//        remoteRender = render;
//
//
//        //断开SDK视频绘制画布
//        AVChatManager.getInstance().setupLocalVideoRender(null, false, 0);
//        AVChatManager.getInstance().setupRemoteVideoRender(remoteId, null, false, 0);
//
//        //重新关联上画布
//        AVChatManager.getInstance().setupLocalVideoRender(localRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
//        AVChatManager.getInstance().setupRemoteVideoRender(remoteId, remoteRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
//
//    }
//
//    /**
//     * 摄像头切换时，布局显隐
//     */
//    private void switchAndSetLayout() {
//        localPreviewInSmallSize = !localPreviewInSmallSize;
////        largeSizePreviewCoverLayout.setVisibility(View.GONE);
////        smallSizePreviewCoverImg.setVisibility(View.GONE);
//        if (isPeerVideoOff) {
////            peerVideoOff();
//        }
//    }
//
//    private View.OnTouchListener smallPreviewTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(final View v, MotionEvent event) {
//            int x = (int) event.getRawX();
//            int y = (int) event.getRawY();
//
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    lastX = x;
//                    lastY = y;
//                    int[] p = new int[2];
//                    bindingView.layoutVideo.smallSizePreviewLayout.getLocationOnScreen(p);
//                    inX = x - p[0];
//                    inY = y - p[1];
//
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    final int diff = Math.max(Math.abs(lastX - x), Math.abs(lastY - y));
//                    if (diff < TOUCH_SLOP)
//                        break;
//
//                    if (paddingRect == null) {
//                        paddingRect = new Rect(com.example.nim_lib.util.ScreenUtil.dip2px(10),
//                                ScreenUtil.dip2px(20),
//                                ScreenUtil.dip2px(10),
//                                ScreenUtil.dip2px(70));
//                    }
//
//                    int destX, destY;
//                    if (x - inX <= paddingRect.left) {
//                        destX = paddingRect.left;
//                    } else if (x - inX + v.getWidth() >= ScreenUtil.screenWidth - paddingRect.right) {
//                        destX = ScreenUtil.screenWidth - v.getWidth() - paddingRect.right;
//                    } else {
//                        destX = x - inX;
//                    }
//
//                    if (y - inY <= paddingRect.top) {
//                        destY = paddingRect.top;
//                    } else if (y - inY + v.getHeight() >= ScreenUtil.screenHeight - paddingRect.bottom) {
//                        destY = ScreenUtil.screenHeight - v.getHeight() - paddingRect.bottom;
//                    } else {
//                        destY = y - inY;
//                    }
//
//                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
//                    params.gravity = Gravity.NO_GRAVITY;
//                    params.leftMargin = destX;
//                    params.topMargin = destY;
//                    v.setLayoutParams(params);
//
//                    break;
//                case MotionEvent.ACTION_UP:
//                    if (Math.max(Math.abs(lastX - x), Math.abs(lastY - y)) <= 5) {
//                        if (largeAccount == null || smallAccount == null) {
//                            return true;
//                        }
//                        String temp;
//                        switchRender(smallAccount, largeAccount);
//                        temp = largeAccount;
//                        largeAccount = smallAccount;
//                        smallAccount = temp;
//                        switchAndSetLayout();
//                    }
//
//                    break;
//            }
//
//            return true;
//        }
//    };
//
//}
