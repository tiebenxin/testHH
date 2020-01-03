package com.yanlong.im;


import android.app.ActivityManager;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;


import com.example.nim_lib.controll.AVChatProfile;
import com.example.nim_lib.controll.AVChatSoundPlayer;
import com.example.nim_lib.ui.VideoActivity;
import com.jrmf360.tools.JrmfClient;
import com.lansosdk.box.LSLog;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.yanlong.im.controll.AVChatKit;
import com.yanlong.im.location.LocationService;
import com.yanlong.im.utils.LogcatHelper;
import com.yanlong.im.utils.MyDiskCacheController;
import com.yanlong.im.utils.MyDiskCacheUtils;
import com.yanlong.im.utils.MyException;
import com.yanlong.im.view.face.FaceView;
import net.cb.cb.library.AppConfig;
import net.cb.cb.library.MainApplication;
import net.cb.cb.library.bean.EventRunState;
import net.cb.cb.library.event.EventFactory;
import net.cb.cb.library.utils.AppFrontBackHelper;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.SpUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.UpLoadUtils;
import net.cb.cb.library.utils.VersionUtil;
import org.greenrobot.eventbus.EventBus;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import cn.jpush.android.api.JPushInterface;
import io.realm.Realm;
public class MyAppLication extends MainApplication {

    private static final String TAG = "MyAppLication";
    private final String U_APP_KEY = "5d53659c570df3d281000225";
    public LocationService locationService;
//    public Vibrator mVibrator;


    @Override
    public void onCreate() {
        super.onCreate();
        initNim();
        AppConfig.setContext(getApplicationContext());
        ///推送处理
        initUPushPre();

        if (!getApplicationContext().getPackageName().equals(getCurrentProcessName())) {
            return;
        }

        //如果需要调试切换版本,请直接修改debug中的ip等信息
        switch (BuildConfig.BUILD_TYPE) {
            case "debug"://测试服
                AppConfig.DEBUG = true;
                //---------------------------
                AppConfig.SOCKET_IP = "yanlong.1616d.top";
                AppConfig.URL_HOST = "https://" + AppConfig.SOCKET_IP + ":8080";
                AppConfig.SOCKET_PORT = 19991;
                AppConfig.UP_PATH = "test-environment";

//                AppConfig.SOCKET_IP = "im-app.zhixun6.com";
//                AppConfig.URL_HOST = "https://" + AppConfig.SOCKET_IP + ":8080";
//                AppConfig.SOCKET_PORT = 19991;
//                AppConfig.UP_PATH = "product-environment";
                break;
            case "pre": //预发布服  美国 usa-test.1616d.top    香港 hk-test.1616d.top
                AppConfig.DEBUG = false;
                //---------------------------
                AppConfig.SOCKET_IP = "hk-test.1616d.top";
                AppConfig.URL_HOST = "https://" + AppConfig.SOCKET_IP + ":8080";
                AppConfig.SOCKET_PORT = 19991;
                AppConfig.UP_PATH = "development";
                break;
            case "release"://正式服
                AppConfig.DEBUG = false; // false true
                //---------------------------
                AppConfig.SOCKET_IP = "im-app.zhixun6.com";
//                AppConfig.SOCKET_IP = "transfer.zhixun6.com";
                AppConfig.URL_HOST = "https://" + AppConfig.SOCKET_IP + ":8080";
                AppConfig.SOCKET_PORT = 19991;
                AppConfig.UP_PATH = "product-environment";
                break;
        }
        //初始化日志
        LogUtil.getLog().init(AppConfig.DEBUG);
        LSLog.TAG = "a===LanSongSDK=";
//        建议统一使用LogUtil.getLog().d标注频繁日志(如 socket，视频播放，通话，每秒都会有日志)
//        建议统一使用LogUtil.getLog().e标注一般日志(如 打印个人信息，打印某个参数等)
//        现在各种格式都有
//        LogUtil
//        Log.e()
//        LSLog
//        system.out等
//        app日志结构LogUtil.getLog
//        appcore日志结构LogUtil.getLog
//        nim_lib日志结构LogUtil.getLog
//        picture_library日志结构LogManager.getLogger和Log.
//        ucrop日志结构Log.
//        weiXinRecorded日志结构LSLog

        //初始化数据库
        Realm.init(getApplicationContext());

        // initUPush();

        //--------------------------
        initWeixinConfig();
        initRunstate();
        initRedPacket();
        LogcatHelper.getInstance(this).start();
//        initException();
        initUploadUtils();
        initBugly();
        initCache();
        // 初始化表情
        FaceView.initFaceMap();
        initLocation();//初始化定位
        initARouter();//初始化路由
    }

    /**
     * 初始化网易云信
     */
    private void initNim() {
        // SDK初始化（启动后台服务，若已经存在用户登录信息， SDK 将完成自动登录） 必须放到主Application中
        SDKOptions options = new SDKOptions();
        // TODO 初始化SDK时配置SDKOptions - disableAwake为true来禁止后台进程唤醒UI进程, 设置了以后，程序最小化后通知栏将不会显示语音的通知
        // 避免Fatal Exception: android.app.RemoteServiceException: Context.startForegroundService() did not then call Service.startForeground()
        options.disableAwake= true;
        NIMClient.init(this, getLoginInfo(), options);
        LogUtil.getLog().d(TAG, "NIMClient.init()");
        // 以下逻辑只在主进程初始化时执行
        if (NIMUtil.isMainProcess(this)) {
            AVChatSoundPlayer.setContext(this);
            AVChatKit.getInstance().init(this);
        }
    }

    private void initCache() {
        MyDiskCacheUtils.getInstance().setDiskController(new MyDiskCacheController()).setContext(this);
    }

    private void initBugly() {
        String packageName = this.getPackageName();
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setAppChannel(StringUtil.getChannelName(this));
        strategy.setAppVersion(VersionUtil.getVerName(this));
        strategy.setAppPackageName(this.getPackageName());
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        CrashReport.initCrashReport(this, "7780d7e928", false, strategy);

    }

    /**
     * 获取网易云账号跟Toekn
     *
     * @return
     */
    private LoginInfo getLoginInfo() {
        SpUtil spUtil = SpUtil.getSpUtil();
        String account = spUtil.getSPValue("account", "");
        String token = spUtil.getSPValue("token", "");
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    private void initUploadUtils() {
        UpLoadUtils.getInstance().init(this);
    }


    /**
     * 获取当前进程名
     */
    private String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService
                (Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }

    /*
     异常捕获
      */
    private void initException() {
        MyException myException = MyException.getInstance();
        myException.init(getApplicationContext());
    }

    /***
     * 初始化红包
     */
    private void initRedPacket() {
        //改为正式环境
        JrmfClient.isDebug(false);
        /*** 需要在Manifest.xml文件*（JRMF_PARTNER_ID）和* 红包名称（JRMF_PARTNER*/
        JrmfClient.init(this);
        com.jrmf360.tools.utils.LogUtil.init(AppConfig.DEBUG);

    }

    private void initUPushPre() {
        UMConfigure.init(this, "5d53659c570df3d281000225",
                "Umeng", UMConfigure.DEVICE_TYPE_PHONE,
                "f045bf243689c2363d5714b781ce556e");
        UMConfigure.setLogEnabled(AppConfig.DEBUG);

        //极光推送初始化
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        MiPushClient.getRegId(getApplicationContext());

    }

    private void initWeixinConfig() {
        PlatformConfig.setWeixin("wxdfd2898507cc4f94", "f5de400d1457c4e75f8719867e2e4810");
    }

    /**
     * 初始化运行状态
     */
    private void initRunstate() {
        AppFrontBackHelper helper = new AppFrontBackHelper();
        helper.register(this, new AppFrontBackHelper.OnAppStatusListener() {
            @Override
            public void onFront() {
                //应用切到前台处理
                LogUtil.getLog().d(TAG, "--->应用切到前台处理");
                EventRunState enent = new EventRunState();
                enent.setRun(true);
                EventBus.getDefault().post(enent);
                // 打开浮动窗口权限时，重新显示音视频浮动按钮
                if (AVChatProfile.getInstance().isAVMinimize()) {
                    EventFactory.ShowVoiceMinimizeEvent event = new EventFactory.ShowVoiceMinimizeEvent();
                    event.isStartRunThread = false;
                    EventBus.getDefault().post(event);
                } else {
                    // 音视频从后台切回前台时判断是否需要打开音视频界面
                    if (VideoActivity.returnVideoActivity) {
                        VideoActivity.returnVideoActivity = false;
                        EventBus.getDefault().post(new EventFactory.VideoActivityEvent());
                    }
                }
            }

            @Override
            public void onBack() {
                //应用切到后台处理
                LogUtil.getLog().d(TAG, "--->应用切到后台处理");
                EventRunState enent = new EventRunState();
                enent.setRun(false);
                EventBus.getDefault().post(enent);
                // 隐藏音视频浮动按钮
                if (AVChatProfile.getInstance().isAVMinimize()) {
                    EventFactory.CloseMinimizeEvent event = new EventFactory.CloseMinimizeEvent();
                    event.isClose = false;
                    EventBus.getDefault().post(event);
                }
            }
        });
    }

    public void initARouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(this);

    }

    //初始化定位sdk，建议在Application中创建
    private void initLocation(){
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        locationService = new LocationService(getApplicationContext());
//        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
    }

}
