package net.cb.cb.library.view;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.umeng.analytics.MobclickAgent;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.dialog.DialogLoadingProgress;
import net.cb.cb.library.event.EventFactory;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/***
 * 统一的activity
 * @author jyj
 * @date 2016/12/7
 */
public class AppActivity extends AppCompatActivity {
    public Context context;
    public LayoutInflater inflater;
    public AlertWait alert;
    public Boolean isFirstRequestPermissionsResult=true;//第一次请求权限返回
    DialogLoadingProgress payWaitDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //initFont();
        context = getApplicationContext();
        inflater = getLayoutInflater();
        alert = new AlertWait(this);
        super.onCreate(savedInstanceState);
        //友盟Push后台进行日活统计及多维度推送的必调用方法
        if(savedInstanceState!=null){
            // 处理APP在后台，关闭某个权限后需要重启APP
            EventBus.getDefault().register(this);
            EventBus.getDefault().post(new EventFactory.RestartAppEvent());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        taskClearNotification();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alert.dismiss4distory();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Object myEvent) {

    }

    //字体缩放倍数
    private static float fontScan = 1.0f;

    public void initFont() {
        if (fontScan == AppConfig.FONT)
            return;

        setFontScan(AppConfig.FONT);
    }

    /***
     * 设置app字体缩放倍率
     * @param fontSize
     */
    public void setFontScan(float fontSize) {
        this.fontScan = fontSize;
        AppConfig.setFont(fontSize);
        Resources resources = getResources();

        resources.getConfiguration().fontScale = fontSize;
        resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
        // this.recreate();

        //  SharedPreferencesUtil sharedPreferencesUtil=new SharedPreferencesUtil(SharedPreferencesUtil.SPName.FONT_SCAN);

        //  sharedPreferencesUtil.save2Json(fontSize);
    }

    /***
     * 清理通知栏
     */
    private void taskClearNotification() {
        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }


    public Context getContext() {
        return context;
    }

    /***
     * 直接跳转
     * @param c
     */
    public void go(Class c) {
        startActivity(new Intent(context, c));
    }


    public void hideKeyboard() {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null) {
            im.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    /**
     * 新增->强制弹出软键盘
     * @param view
     * 备注：延迟任务解决之前无法弹出问题
     */
    public void showSoftKeyword(final View view) {
        if (view == null) {
            return;
        }
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(view, 0);
                }
            }
        }, 100);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 127: {//申请定位权限返回
                Boolean hasPermissions = true;
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        hasPermissions = false;
                    }
                }
//                ToastUtil.show(this,"请打开定位权限");
                LogUtil.getLog().e("=申请定位权限返回=location=hasPermission=s="+hasPermissions);

                if(!hasPermissions && !isFirstRequestPermissionsResult){
                    AlertYesNo alertYesNo=new AlertYesNo();
                    alertYesNo.init(this, "提示",  "您拒绝了定位权限，打开定位权限吗？", "确定", "取消", new AlertYesNo.Event() {
                        @Override
                        public void onON() {
                        }

                        @Override
                        public void onYes() {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    });
                    alertYesNo.show();
                }
                isFirstRequestPermissionsResult=false;
                break;
            }
        }
    }

    public void showLoadingDialog() {
        if (payWaitDialog == null) {
            payWaitDialog = new DialogLoadingProgress(this);
        }
        payWaitDialog.show();
    }

    public void dismissLoadingDialog() {
        if (payWaitDialog != null) {
            payWaitDialog.dismiss();
        }
    }
}
