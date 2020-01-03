package com.yanlong.im.notify;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.yanlong.im.R;

import net.cb.cb.library.view.AppActivity;

/**
 * @创建人 shenxin
 * @创建时间 2019/10/15 0015 15:45
 */
public class MessageActivity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //锁屏状态下显示
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD //解锁
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //保持屏幕长亮
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); //打开屏幕
        setContentView(R.layout.dialog_lock_detail);
    }
}
