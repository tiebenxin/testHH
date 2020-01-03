package com.example.nim_lib.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import com.example.nim_lib.R;
import com.example.nim_lib.util.ViewUtils;
import com.example.nim_lib.util.flyn.Eyes;

import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-10-11
 * @updateAuthor
 * @updateDate
 * @description ActivityBind基类
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public abstract class BaseBindActivity<SV extends ViewDataBinding> extends AppActivity {

    /**
     * 是否需要基类帮你注册EventBus
     * 请在方法{@link #preCreate} 初始化这个变量
     * EventBus.getDefault().register(this);
     * EventBus.getDefault().unregister(this);
     */
    protected boolean mNeedBaseEventBusRegist = true;

    protected SV bindingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preCreate();
        super.onCreate(savedInstanceState);
        setContentView(setView());
        setStatusBarColor(R.color.blue_title);
        init(savedInstanceState);
        initEvent();
        loadData();

    }

    public void setStatusBarColor(int color) {
        Eyes.setStatusBarColor(this, getResources().getColor(color));
        Eyes.setStatusBarLightMode(this, getResources().getColor(color));
    }

    @Override
    public void setContentView(int layoutResID) {
        bindingView = DataBindingUtil.inflate(getLayoutInflater(), layoutResID, null, false);
        super.setContentView(bindingView.getRoot());
    }

    /**
     * 此方法在Activity中方法onCreate之前调用此方法
     */
    protected void preCreate() {
        if (mNeedBaseEventBusRegist && !EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNeedBaseEventBusRegist) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Object myEvent) {

    }

    /**
     * 设置activity的布局文件
     *
     * @return 布局文件的resId
     */
    protected abstract int setView();

    /**
     * 对象的初始化工作
     *
     * @param savedInstanceState
     */
    protected abstract void init(Bundle savedInstanceState);

    /**
     * 设置事件
     */
    protected abstract void initEvent();

    /**
     * 加载数据
     */
    protected abstract void loadData();

    @Override
    public void onBackPressed() {
        if (ViewUtils.isFastDoubleClick(300)) {
            return;
        }

        if (!isFinishing()) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    /**
     * 设置返回事件
     * @param headView
     */
    public void setActionBarLeft(HeadView headView) {
        headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
    }

    /**
     * 是否关闭键盘
     *
     * @param show
     */
    public void showInput(boolean show) {
        try {
            if (show) {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInputFromInputMethod(this.getCurrentFocus().getApplicationWindowToken(), 0);
            } else {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
            }
        } catch (NullPointerException e1) {

        } catch (Exception e) {
        }
    }
}
