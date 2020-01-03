package com.yanlong.im;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nim_lib.config.Preferences;
import com.example.nim_lib.controll.AVChatProfile;
import com.example.nim_lib.ui.VideoActivity;
import com.example.nim_lib.util.PermissionsUtil;
import com.hm.cxpay.bean.BankBean;
import com.hm.cxpay.bean.UserBean;
import com.hm.cxpay.eventbus.IdentifyUserEvent;
import com.hm.cxpay.eventbus.RefreshBalanceEvent;
import com.hm.cxpay.global.PayEnvironment;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.yanlong.im.chat.EventSurvivalTimeAdd;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.EnvelopeInfo;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.NotificationConfig;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.eventbus.EventRefreshMainMsg;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.chat.server.ChatServer;
import com.yanlong.im.chat.task.TaskLoadSavedGroup;
import com.yanlong.im.chat.ui.MsgMainFragment;
import com.yanlong.im.notify.NotifySettingDialog;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.EventCheckVersionBean;
import com.yanlong.im.user.bean.NewVersionBean;
import com.yanlong.im.user.bean.TokenBean;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.bean.VersionBean;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.user.ui.FriendMainFragment;
import com.yanlong.im.user.ui.LoginActivity;
import com.yanlong.im.user.ui.MyFragment;
import com.yanlong.im.user.ui.SplashActivity;
import com.yanlong.im.utils.TimeUtils;
import com.yanlong.im.utils.socket.MsgBean;
import com.yanlong.im.utils.socket.SocketData;
import com.yanlong.im.utils.update.UpdateManage;
import com.zhaoss.weixinrecorded.CanStampEventWX;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.CanStampEvent;
import net.cb.cb.library.bean.EventLoginOut;
import net.cb.cb.library.bean.EventLoginOut4Conflict;
import net.cb.cb.library.bean.EventNetStatus;
import net.cb.cb.library.bean.EventRefreshChat;
import net.cb.cb.library.bean.EventRefreshFriend;
import net.cb.cb.library.bean.EventRunState;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.event.EventFactory;
import net.cb.cb.library.manager.TokenManager;
import net.cb.cb.library.net.NetWorkUtils;
import net.cb.cb.library.net.NetworkReceiver;
import net.cb.cb.library.utils.BadgeUtil;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.IntentUtil;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.NetUtil;
import net.cb.cb.library.utils.NotificationsUtils;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.SpUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.VersionUtil;
import net.cb.cb.library.view.AlertYesNo;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.ImageMoveView;
import net.cb.cb.library.view.StrikeButton;
import net.cb.cb.library.view.ViewPagerSlide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

import cn.jpush.android.api.JPluginPlatformInterface;
import cn.jpush.android.api.JPushInterface;
import retrofit2.Call;
import retrofit2.Response;

import static net.cb.cb.library.utils.SharedPreferencesUtil.SPName.NOTIFICATION;


public class MainActivity extends AppActivity {
    public final static String IS_LOGIN = "is_from_login";
    private ViewPagerSlide viewPage;
    private android.support.design.widget.TabLayout bottomTab;

    private Fragment[] fragments;
    private String[] tabs;
    private int[] iconRes;
    private int[] iconHRes;
    private StrikeButton sbmsg;
    private StrikeButton sbfriend;
    private StrikeButton sbme;
    private NotifySettingDialog notifyDialog;
    private NetworkReceiver mNetworkReceiver;
    private MsgMainFragment mMsgMainFragment;
    private ImageMoveView mBtnMinimizeVoice;
    Handler mHandler = new Handler();
    // 通话时间
    private int mPassedTime = 0;
    private final int TIME = 1000;
    private TimeUtils timeUtils = new TimeUtils();
    private long mExitTime;
    private int mHour, mMin, mSecond;
    private EventFactory.VoiceMinimizeEvent mVoiceMinimizeEvent;
    private boolean isActivityStop;

    private UserAction userAction = new UserAction();
    private boolean testMe = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        findViews();
        initEvent();
        uploadApp();
        getSurvivalTimeData();
        checkRosters();
        doRegisterNetReceiver();
        checkNeteaseLogin();
        SpUtil spUtil = SpUtil.getSpUtil();
        boolean isFist = spUtil.getSPValue(Preferences.IS_FIRST_DIALOG, false);
        if (!isFist) {
            String brand = android.os.Build.BRAND;
            brand = brand.toUpperCase();
            if (brand.contains("OPPO")) {
                permissionCheck();
            }
        }
    }

    /**
     * 部份手机覆盖安装后，网易云在MyApplicon有时会登录失败，导致部分用户使用不了音视频，所以在首页10秒后在检查网易云是否登录，没登录重新登录
     */
    private void checkNeteaseLogin() {
        if (!isFinishing()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!isFinishing()) {
                            if (NIMClient.getStatus() != StatusCode.LOGINED) {
                                LogUtil.getLog().i(MainActivity.class.getName(), "网易云登录失败，重新登录了:" + NIMClient.getStatus());
                                LogUtil.writeLog(">>>>>>>>>网易云登录失败，重新登录了 状态是: "+NIMClient.getStatus());
                                NIMClient.getService(AuthService.class).logout();// 需要先登出网易登录，在重新登录
                                UserAction userAction = new UserAction();
                                SpUtil spUtil = SpUtil.getSpUtil();
                                String account = spUtil.getSPValue("account", "");
                                String token = spUtil.getSPValue("token", "");
                                userAction.doNeteaseLogin(account, token);
                            } else {
                                LogUtil.getLog().i(MainActivity.class.getName(), "网易云登录成功");
                                LogUtil.writeLog(">>>>>>>>>网易云登录成功>>>>>>>>> ");
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }, 10000);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            reTryExit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    //自动寻找控件
    private void findViews() {
        viewPage = findViewById(R.id.viewPage);
        bottomTab = findViewById(R.id.bottom_tab);
        timeUtils.RunTimer();
        mBtnMinimizeVoice = findViewById(R.id.btn_minimize_voice);
    }


    public ViewPagerSlide getViewPage() {
        return viewPage;
    }

    //自动生成的控件事件
    private void initEvent() {
        mMsgMainFragment = MsgMainFragment.newInstance();
        fragments = new Fragment[]{mMsgMainFragment, FriendMainFragment.newInstance(), MyFragment.newInstance()};
        tabs = new String[]{"消息", "通讯录", "我"};
        iconRes = new int[]{R.mipmap.ic_msg, R.mipmap.ic_frend, R.mipmap.ic_me};
        iconHRes = new int[]{R.mipmap.ic_msg_h, R.mipmap.ic_frend_h, R.mipmap.ic_me_h};
        viewPage.setOffscreenPageLimit(2);
        viewPage.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragments[i];
            }

            @Override
            public int getCount() {
                return fragments.length;
            }
        });
        bottomTab.setupWithViewPager(viewPage);
        bottomTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPage.setCurrentItem(tab.getPosition());
                for (int i = 0; i < bottomTab.getTabCount(); i++) {
                    View rootView = bottomTab.getTabAt(i).getCustomView();
                    LinearLayout viewItem = rootView.findViewById(R.id.view_item);
                    StrikeButton sb = rootView.findViewById(R.id.sb);
                    TextView txt = rootView.findViewById(R.id.txt);
                    if (i == tab.getPosition()) { // 选中状态
                        sb.setButtonBackground(iconHRes[i]);
                        txt.setTextColor(getResources().getColor(R.color.green_500));
                    } else {// 未选中状态
                        sb.setButtonBackground(iconRes[i]);
                        txt.setTextColor(getResources().getColor(R.color.gray_400));
                    }
                }

                if (tab.getPosition() == 2) {
                    //每次点击检查新版泵
                    EventBus.getDefault().post(new EventCheckVersionBean());
                }
                // 同时点击导航栏跟气泡时，延迟关闭气泡
                if (tab.getPosition() == 1 || tab.getPosition() == 2) {
                    if (!isFinishing()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mMsgMainFragment != null) {
                                    mMsgMainFragment.hidePopView();
                                }
                            }
                        }, 100);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // 提供自定义的布局添加Tab
        for (int i = 0; i < fragments.length; i++) {
            View rootView = getLayoutInflater().inflate(R.layout.tab_item, null);
            TextView txt = rootView.findViewById(R.id.txt);
            StrikeButton sb = rootView.findViewById(R.id.sb);
            if (i == 2) {
                sb.setSktype(1);
                //设置值
                sb.setNum(0, true);
                sbme = sb;
            }
            if (i == 1) {
                sb.setSktype(1);
                sb.setNum(0, true);
                sbfriend = sb;
            }

            if (i == 0) {//消息数量

                sbmsg = sb;
            }


            txt.setText(tabs[i]);
            bottomTab.getTabAt(i).setCustomView(rootView);
        }
        //切换
        bottomTab.getTabAt(1).select();
        bottomTab.getTabAt(0).select();


        // 启动聊天服务
        startService(new Intent(getContext(), ChatServer.class));

        //监听应用
  /*      AppFrontBackHelper helper = new AppFrontBackHelper();
        helper.register(getApplication(), new AppFrontBackHelper.OnAppStatusListener() {
            @Override
            public void onFront() {
                //应用切到前台处理
                startService(new Intent(getContext(), ChatServer.class));
            }

            @Override
            public void onBack() {
                //应用切到后台处理
                stopService(new Intent(getContext(), ChatServer.class));

            }
        });*/
        mBtnMinimizeVoice.setOnClickListener(new ImageMoveView.OnSingleTapListener() {
            @Override
            public void onClick() {
                mBtnMinimizeVoice.close(MainActivity.this);
                mHandler.removeCallbacks(runnable);
                AVChatProfile.getInstance().setAVMinimize(false);
                IntentUtil.gotoActivity(MainActivity.this, VideoActivity.class);
            }
        });

    }

    /**
     * 在主界面按两次back键退出App
     */
    private void reTryExit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            ToastUtil.show(getApplicationContext(), "再按一次退出程序");
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    private void doRegisterNetReceiver() {
        if (mNetworkReceiver == null) {
            mNetworkReceiver = new NetworkReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mNetworkReceiver, filter);
        }
    }

    //检测通讯录问题
    private void checkRosters() {
        Intent intent = getIntent();
        boolean isFromLogin = intent.getBooleanExtra(IS_LOGIN, false);
        if (isFromLogin) {//从登陆页面过来，从网络获取最新数据
            taskLoadFriends();
//            taskLoadSavedGroups();
        } else {
            UserDao userDao = new UserDao();
            boolean hasInit = userDao.isRosterInit();
            if (!hasInit) {//未初始化，初始化本地通讯录
                taskLoadFriends();
            }
        }
    }

    private void taskLoadSavedGroups() {
        new MsgAction().getMySavedGroup(new CallBack<ReturnBean<List<Group>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<Group>>> call, Response<ReturnBean<List<Group>>> response) {
                if (response.body() != null && response.body().getData() != null) {
                    List<Group> groups = response.body().getData();
                    TaskLoadSavedGroup taskGroup = new TaskLoadSavedGroup(groups);
                    taskGroup.execute();
                }
            }
        });
    }

    private void taskLoadFriends() {
        userAction.friendGet4Me(new CallBack<ReturnBean<List<UserInfo>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<UserInfo>>> call, Response<ReturnBean<List<UserInfo>>> response) {
                MessageManager.getInstance().notifyRefreshFriend(true, -1, CoreEnum.ERosterAction.LOAD_ALL_SUCCESS);
            }

            @Override
            public void onFailure(Call<ReturnBean<List<UserInfo>>> call, Throwable t) {
                super.onFailure(call, t);
                MessageManager.getInstance().notifyRefreshFriend(true, -1, CoreEnum.ERosterAction.LOAD_ALL_SUCCESS);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateNetStatus();
        isActivityStop = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JPluginPlatformInterface.JPLUGIN_REQUEST_CODE) {

        }
    }

    private void updateNetStatus() {
        if (NetUtil.isNetworkConnected()) {
            EventNetStatus netStatusEvent = new EventNetStatus(CoreEnum.ENetStatus.SUCCESS_ON_NET);
            EventBus.getDefault().post(netStatusEvent);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(getContext(), ChatServer.class));
        if (mNetworkReceiver != null) {
            unregisterReceiver(mNetworkReceiver);
        }
        AVChatProfile.getInstance().setAVMinimize(false);
        EventBus.getDefault().unregister(this);
        // 关闭浮动窗口
        mBtnMinimizeVoice.close(this);
        mHandler.removeCallbacks(runnable);
        timeUtils.cancle();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventNetStatus(EventNetStatus event) {
        EventFactory.EventNetStatus eventNetStatus = new EventFactory.EventNetStatus(event.getStatus());
        EventBus.getDefault().post(eventNetStatus);
        System.out.println(MainActivity.class.getSimpleName() + "---" + NetWorkUtils.getLocalIpAddress(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityStop = false;
        taskGetMsgNum();
        //taskClearNotification();
        checkNotificationOK();
//        checkPayEnvironmentInit();
        if (AppConfig.isOnline()) {
            checkHasEnvelopeSendFailed();
        }
    }

    //检测支付环境的初始化
    private void checkPayEnvironmentInit() {
        checkPayToken();
        UserInfo info = UserAction.getMyInfo();
        if (info != null) {
            PayEnvironment.getInstance().setPhone(info.getPhone());
            PayEnvironment.getInstance().setNick(info.getName());
            UserBean bean = PayEnvironment.getInstance().getUser();
            if (bean == null || (bean != null && bean.getUid() != info.getUid().intValue())) {
                httpGetUserInfo();
            }
        }
        PayEnvironment.getInstance().setContext(AppConfig.getContext());
        if (PayEnvironment.getInstance().getBanks() == null) {//初始化银行
            getBankList();
        }
    }

    private void checkPayToken() {
        if (TextUtils.isEmpty(PayEnvironment.getInstance().getToken())) {
            TokenBean token = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.TOKEN).get4Json(TokenBean.class);
            if (token != null) {
                TokenManager.initToken(token.getAccessToken());
                PayEnvironment.getInstance().setToken(token.getAccessToken());
//                CommonInterceptor.headers = Headers.of(TokenManager.TOKEN_KEY, token.getAccessToken());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefresh(EventRefreshMainMsg event) {
        taskGetMsgNum();
        taskGetFriendNum();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefreshBalance(RefreshBalanceEvent event) {
        httpGetUserInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventLoginOut(EventLoginOut event) {
        if (event.loginType != 1) {
            ToastUtil.show(context, "因长期未登录已过有效期,请重新登录");
        }
        loginoutComment();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventIdentifyUser(IdentifyUserEvent event) {
        httpGetUserInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventLoginOutconflict(EventLoginOut4Conflict event) {
        loginoutComment();
        startActivity(new Intent(getContext(), MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        );

        AlertYesNo alertYesNo = new AlertYesNo();
        alertYesNo.init(this, "提示", event.getMsg(), "确定", null, new AlertYesNo.Event() {
            @Override
            public void onON() {
                startActivity(new Intent(getContext(), LoginActivity.class));
                finish();
            }

            @Override
            public void onYes() {
                startActivity(new Intent(getContext(), LoginActivity.class));
                finish();
            }
        });
        alertYesNo.show();

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRunState(EventRunState event) {
        LogUtil.getLog().i("TAG", ">>>>EventRunState:" + event.getRun());
        if (event.getRun()) {
            startService(new Intent(getContext(), ChatServer.class));
        } else {
            stopService(new Intent(getContext(), ChatServer.class));
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefreshFriend(EventRefreshFriend event) {
        if (event.getRosterAction() == CoreEnum.ERosterAction.LOAD_ALL_SUCCESS) {
            taskLoadSavedGroups();
        } else if (event.getRosterAction() == CoreEnum.ERosterAction.REQUEST_FRIEND
                || event.getRosterAction() == CoreEnum.ERosterAction.DEFAULT) {//请求添加为好友 申请进群
            taskGetFriendNum();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void voiceMinimizeEvent(EventFactory.VoiceMinimizeEvent event) {
        mPassedTime = event.passedTime;
        mVoiceMinimizeEvent = event;
        showMinimizeVoiceView(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void stopJPushResumeEvent(EventFactory.StopJPushResumeEvent event) {
        // TODO 处理部分手机收到音视频消息后，多个铃声在播放问题
        JPushInterface.stopPush(this);
        if (!isFinishing()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    JPushInterface.resumePush(MainActivity.this);
                }
            }, 500);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void closevoiceMinimizeEvent(EventFactory.CloseVoiceMinimizeEvent event) {
        if (!isFinishing()) {
            // 判断ChatActivity是否到前端显示，不是则更新并发送音视频消息数据
            mBtnMinimizeVoice.close(this);
            mHandler.removeCallbacks(runnable);
            if (event != null) {
                MsgAllBean msgAllbean = null;
                if (event.avChatType == AVChatType.AUDIO.getValue()) {
                    msgAllbean = SocketData.send4VoiceOrVideo(event.toUId, event.toGid, event.txt, MsgBean.AuVideoType.Audio, event.operation);
                } else if (event.avChatType == AVChatType.VIDEO.getValue()) {
                    msgAllbean = SocketData.send4VoiceOrVideo(event.toUId, event.toGid, event.txt, MsgBean.AuVideoType.Vedio, event.operation);
                }
                EventRefreshChat eventRefreshChat = new EventRefreshChat();
                eventRefreshChat.isScrollBottom = true;
                EventBus.getDefault().post(eventRefreshChat);
                if (msgAllbean != null) {
                    MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.PRIVATE, event.toUId, event.toGid, CoreEnum.ESessionRefreshTag.SINGLE, msgAllbean);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void closeMinimizeEvent(EventFactory.CloseMinimizeEvent event) {
        if (!isFinishing()) {
            if (event.isClose) {
                mHandler.removeCallbacks(runnable);
            }
            mBtnMinimizeVoice.close(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void restartAppEvent(net.cb.cb.library.event.EventFactory.RestartAppEvent event) {
        if (!isFinishing()) {
            // 处理APP在后台，关闭某个权限后需要重启APP
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sendP2PAuVideoDialMessageEvent(EventFactory.SendP2PAuVideoDialMessage event) {

        if (event.avChatType == AVChatType.AUDIO.getValue()) {
            SocketData.send4VoiceOrVideoNotice(event.toUId, event.toGid, MsgBean.AuVideoType.Audio);
        } else if (event.avChatType == AVChatType.VIDEO.getValue()) {
            SocketData.send4VoiceOrVideoNotice(event.toUId, event.toGid, MsgBean.AuVideoType.Vedio);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showVoiceMinimizeEvent(EventFactory.ShowVoiceMinimizeEvent event) {
        LogUtil.getLog().i("VideoActivity", "showVoiceMinimizeEvent：" + event.isStartRunThread);
        showMinimizeVoiceView(event.isStartRunThread);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void videoActivityEvent(EventFactory.VideoActivityEvent event) {
        IntentUtil.gotoActivity(MainActivity.this, VideoActivity.class);
    }

    /**
     * 显示浮动按钮的音视频时长
     *
     * @param isStartRunThread 是否需要开一个线程计时
     */
    private void showMinimizeVoiceView(boolean isStartRunThread) {
        if (mVoiceMinimizeEvent != null && mVoiceMinimizeEvent.isCallEstablished) {// 是否接听
            if (!mBtnMinimizeVoice.isShown()) {
                mBtnMinimizeVoice.show(MyAppLication.getInstance().getApplicationContext(), getWindow());
            }
            if (!isFinishing() && isStartRunThread) {
                mBtnMinimizeVoice.updateCallTime(mVoiceMinimizeEvent.showTime);
                mHandler.postDelayed(runnable, TIME);
            } else {
                if (mHour > 0) {
                    mBtnMinimizeVoice.updateCallTime(String.format(Locale.CHINESE, "%02d:%02d:%02d", mHour, mMin, mSecond));
                } else {
                    mBtnMinimizeVoice.updateCallTime(String.format(Locale.CHINESE, "%02d:%02d", mMin, mSecond));
                }
            }
        } else {
            mBtnMinimizeVoice.show(MyAppLication.getInstance().getApplicationContext(), getWindow());
            mBtnMinimizeVoice.updateCallTime("等待接听");
        }
    }

    /**
     * 通话计时
     */
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            mPassedTime++;
            mHour = mPassedTime / 3600;
            mMin = mPassedTime % 3600 / 60;
            mSecond = mPassedTime % 60;

            if (!isFinishing()) {
                mHandler.postDelayed(this, TIME);
                if (mHour > 0) {
                    mBtnMinimizeVoice.updateCallTime(String.format(Locale.CHINESE, "%02d:%02d:%02d", mHour, mMin, mSecond));
                } else {
                    mBtnMinimizeVoice.updateCallTime(String.format(Locale.CHINESE, "%02d:%02d", mMin, mSecond));
                }
            }
        }
    };

    private void loginoutComment() {
        UserInfo userInfo = UserAction.getMyInfo();
        if (userInfo != null) {
            new SharedPreferencesUtil(SharedPreferencesUtil.SPName.IMAGE_HEAD).save2Json(userInfo.getHead() + "");
            new SharedPreferencesUtil(SharedPreferencesUtil.SPName.PHONE).save2Json(userInfo.getPhone() + "");
        }
        userAction.cleanInfo();
    }

    private void uploadApp() {
        taskNewVersion();
    }


    private MsgDao msgDao = new MsgDao();

    /***
     * 未读消息
     * @return
     */
    private void taskGetMsgNum() {
        if (sbmsg == null)
            return;
        int num = msgDao.sessionReadGetAll();
        sbmsg.setNum(num, true);
        BadgeUtil.setBadgeCount(getApplicationContext(), num);
    }

    /***
     * 好友或者群申请数量
     */
    private void taskGetFriendNum() {
        //  ToastUtil.show(getContext(),"更新好友的提示数量");
        int sum = 0;
        sum += msgDao.remidGet("friend_apply");
        // sum+=msgDao.remidGet("friend_apply");
        //  sum+=msgDao.remidGet("friend_apply");
        sbfriend.setNum(sum, true);

    }

    private void taskNewVersion() {
        userAction.getNewVersion(StringUtil.getChannelName(context), new CallBack<ReturnBean<NewVersionBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<NewVersionBean>> call, Response<ReturnBean<NewVersionBean>> response) {
                if (response.body() == null || response.body().getData() == null) {
                    return;
                }
                if (response.body().isOk()) {
                    NewVersionBean bean = response.body().getData();
                    UpdateManage updateManage = new UpdateManage(context, MainActivity.this);
                    if (response.body().getData().getForceUpdate() != 0) {
                        //updateManage.uploadApp(bean.getVersion(), bean.getContent(), bean.getUrl(), false);
                        updateManage.uploadApp(bean.getVersion(), bean.getContent(), bean.getUrl(), true);
                    } else {
                        SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.NEW_VESRSION);
                        VersionBean versionBean = new VersionBean();
                        versionBean.setVersion(bean.getVersion());
                        preferencesUtil.save2Json(versionBean);
                        updateManage.uploadApp(bean.getVersion(), bean.getContent(), bean.getUrl(), false);
//                        if (updateManage.isToDayFirst(bean)) {
//                        updateManage.uploadApp(bean.getVersion(), bean.getContent(), bean.getUrl(), false);
//                        }

                        if (bean != null && !TextUtils.isEmpty(bean.getVersion())) {
                            if (new UpdateManage(context, MainActivity.this).check(bean.getVersion())) {
                                sbme.setNum(1, true);
                            } else {
                                sbme.setNum(0, true);
                            }
                        }
                    }
                }
            }
        });
    }


    /*
     * 检测通知栏是否开启
     * */
    private void checkNotificationOK() {
        if (canRemindToSetting() && !NotificationsUtils.isNotificationEnabled(MainActivity.this)) {
            LogUtil.getLog().i(MainActivity.class.getSimpleName(), "无推送权限");
            notifyDialog = new NotifySettingDialog(MainActivity.this, R.style.MyDialogTheme);
            notifyDialog.setCancelable(false);
            notifyDialog.create();
            notifyDialog.setTitle("温馨提示");
            notifyDialog.setMessage("由于目前未开通系统通知服务，为不影响使用，将在3秒后前往设置");
            notifyDialog.show();
//            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
//            builder.setTitle("温馨提示");
//            builder.setMessage("由于目前未开通系统通知服务，为不影响使用，将在3秒后前往设置");
//            notifyDialog = builder.create();
//            notifyDialog.show();
            // TODO 解决IllegalArgumentException异常
            if (!isFinishing()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            notifyDialog.dismiss();
                        }
                        NotificationsUtils.toNotificationSetting(MainActivity.this);
                        saveNotifyConfig();
                    }
                }, 3000);
            }
        } else {
            LogUtil.getLog().i(MainActivity.class.getSimpleName(), "有推送权限" + canRemindToSetting());
        }
    }

    /*
     * 已经通知或者不是新版本，允许进入设置
     * */
    private boolean canRemindToSetting() {
        SharedPreferencesUtil sp = new SharedPreferencesUtil(NOTIFICATION);
        if (sp != null) {
            NotificationConfig config = sp.get4Json(NotificationConfig.class, "notify_config");
            if (config != null && userAction.getMyId() != null) {
//                LogUtil.getLog().i(MainActivity.class.getSimpleName(), "oldUid=" + config.getUid() + "--newUid=" + userAction.getMyId());
                if (config.getUid() == userAction.getMyId() && (!isNewVersion(config.getVersion()) || config.isHasNotify())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isNewVersion(String version) {
        if (TextUtils.isEmpty(version)) {
            return false;
        }
        String newVersion = VersionUtil.getVerName(this);
        int[] oldArr = StringUtil.getVersionArr(version);
        int[] newArr = StringUtil.getVersionArr(newVersion);
        LogUtil.getLog().i(MainActivity.class.getSimpleName(), "newVersion=" + newVersion + "--oldVersion=" + version);
        if (oldArr != null && newArr != null && oldArr.length == 3 && newArr.length == 3) {
            if (oldArr[0] < newArr[0]) {
                return true;
            } else {
                if (oldArr[1] < newArr[1]) {
                    return true;
                } else {
                    if (oldArr[2] < newArr[2]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void saveNotifyConfig() {
        NotificationConfig config = new NotificationConfig();
        config.setHasNotify(true);
        config.setUid(userAction.getMyId());
        config.setVersion(VersionUtil.getVerName(this));
        LogUtil.getLog().i(MainActivity.class.getSimpleName(), VersionUtil.getVerName(this));
        SharedPreferencesUtil sp = new SharedPreferencesUtil(NOTIFICATION);
        sp.save2Json(config, "notify_config");
    }

    public boolean isActivityStop() {
        return isActivityStop;
    }


    private void getSurvivalTimeData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //查询所有阅后即焚消息加入定时器
                List<MsgAllBean> list = new MsgDao().getMsg4SurvivalTime();
                if (list != null) {
                    timeUtils.addMsgAllBeans(list);
                }
            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void addSurvivalTimeList(EventSurvivalTimeAdd survivalTimeAdd) {
        if (survivalTimeAdd.msgAllBean != null) {
            timeUtils.addMsgAllBean(survivalTimeAdd.msgAllBean);
        } else if (survivalTimeAdd.list != null) {
            timeUtils.addMsgAllBeans(survivalTimeAdd.list);
        }
    }

    /**
     * 检查是否开启悬浮窗权限
     * OPPO 手机必须开启程序自动启动或开启悬浮窗权限，程序退到后台才能弹出音视频界面
     */
    private void permissionCheck() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                PermissionsUtil.showPermissionDialog(this);
            }
        } else {
            String brand = android.os.Build.BRAND;
            brand = brand.toUpperCase();
            if (brand.equals("HUAWEI")) {
                if (!PermissionsUtil.checkHuaWeiFloatWindowPermission(this)) {
                    PermissionsUtil.showPermissionDialog(this);
                }
            } else if (brand.equals("MEIZU")) {
                if (!PermissionsUtil.checkMeiZuFloatWindowPermission(this)) {
                    PermissionsUtil.showPermissionDialog(this);
                }
            } else {


            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void posting(CanStampEvent event) {
        if (!isFinishing()) {
            //允许
            MessageManager.setCanStamp(event.canStamp);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void posting(CanStampEventWX event) {
        if (!isFinishing()) {
            //允许
            MessageManager.setCanStamp(event.canStamp);
        }
    }

    private void checkHasEnvelopeSendFailed() {
        List<EnvelopeInfo> list = msgDao.queryEnvelopeInfoList();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                EnvelopeInfo info = list.get(i);
                if (info.getCreateTime() - System.currentTimeMillis() >= TimeToString.DAY) {//超过24小时
                    deleteEnvelopInfo(info);
                }
            }
        }
    }

    //删除临时红包信息
    private void deleteEnvelopInfo(EnvelopeInfo envelopeInfo) {
        msgDao.deleteEnvelopeInfo(envelopeInfo.getRid(), envelopeInfo.getGid(), envelopeInfo.getUid(), false);
        MessageManager.getInstance().notifyRefreshMsg(!TextUtils.isEmpty(envelopeInfo.getGid()) ? CoreEnum.EChatType.GROUP : CoreEnum.EChatType.PRIVATE, envelopeInfo.getUid(), envelopeInfo.getGid(), CoreEnum.ESessionRefreshTag.SINGLE, null);
    }

    /**
     * 获取 绑定的银行卡列表
     * <p>
     * 备注：主要用于零钱首页更新"我的银行卡" 张数，暂时仅"充值、提现、我的银行卡"返回此界面后需要刷新
     */
    private void getBankList() {
        PayHttpUtils.getInstance().getBankList()
                .compose(RxSchedulers.<BaseResponse<List<BankBean>>>compose())
                .compose(RxSchedulers.<BaseResponse<List<BankBean>>>handleResult())
                .subscribe(new FGObserver<BaseResponse<List<BankBean>>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<List<BankBean>> baseResponse) {
                        List<BankBean> info = baseResponse.getData();
                        if (info != null) {
                            PayEnvironment.getInstance().setBanks(info);
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                    }
                });
    }

    /**
     * 请求零钱红包用户信息,刷新用户余额
     */
    private void httpGetUserInfo() {
        PayHttpUtils.getInstance().getUserInfo()
                .compose(RxSchedulers.<BaseResponse<UserBean>>compose())
                .compose(RxSchedulers.<BaseResponse<UserBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<UserBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<UserBean> baseResponse) {
                        if (baseResponse.isSuccess()) {
                            if (baseResponse.getData() != null) {
                                UserBean userBean = baseResponse.getData();
                                PayEnvironment.getInstance().setUser(userBean);
                            }
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse<UserBean> baseResponse) {
                        super.onHandleError(baseResponse);
                    }
                });
    }
}
