package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nim_lib.controll.AVChatProfile;
import com.hm.cxpay.global.PayEnvironment;
import com.lansosdk.videoeditor.LanSongFileUtil;
import com.luck.picture.lib.tools.DoubleUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.UserSeting;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.chat.ui.ChatFontActivity;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.VersionBean;
import com.yanlong.im.utils.update.UpdateManage;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.bean.EventLoginOut;
import net.cb.cb.library.event.EventFactory;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.VersionUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AlertYesNo;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;
import net.cb.cb.library.view.WebPageActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class CommonActivity extends AppActivity implements View.OnClickListener {

    private LinearLayout mViewAccountBinding;
    private LinearLayout mViewNewMessage;
    private LinearLayout mViewSecurityPrivacy;
    private LinearLayout mViewSetingFont;
    private LinearLayout mViewClear;
    private LinearLayout mviewPrivacy;
    private Button mBtnExit;
    private HeadView mHeadView;
    private LinearLayout mViewAboutAs;
    private TextView mTvVersion;
    private UserAction userAction = new UserAction();
    private CheckBox cbVoice;
    private MsgDao msgDao;
    private LinearLayout viewSelectBackground;
    private TextView tvNewVersions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        initView();
        initEvent();
    }

    private void initView() {
        mHeadView = findViewById(R.id.headView);
        mViewAccountBinding = findViewById(R.id.view_account_binding);
        mViewNewMessage = findViewById(R.id.view_new_message);
        mViewSecurityPrivacy = findViewById(R.id.view_security_privacy);
        mViewSetingFont = findViewById(R.id.view_seting_font);
        mViewClear = findViewById(R.id.view_clear);
        mBtnExit = findViewById(R.id.btn_exit);
        mViewAboutAs = findViewById(R.id.view_about_as);
        mTvVersion = findViewById(R.id.tv_version);
        mTvVersion.setText(VersionUtil.getVerName(this));
        cbVoice = findViewById(R.id.cb_voice);
        viewSelectBackground = findViewById(R.id.view_select_background);
        tvNewVersions = findViewById(R.id.tv_new_versions);
        mviewPrivacy = findViewById(R.id.view_privacy);

        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.NEW_VESRSION);
        VersionBean bean = sharedPreferencesUtil.get4Json(VersionBean.class);
        if (bean != null && !TextUtils.isEmpty(bean.getVersion())) {
            if (new UpdateManage(context, CommonActivity.this).check(bean.getVersion())) {
                tvNewVersions.setVisibility(View.VISIBLE);
            } else {
                tvNewVersions.setVisibility(View.GONE);
            }
        }

    }

    private void initEvent() {
        mViewAccountBinding.setOnClickListener(this);
        mViewNewMessage.setOnClickListener(this);
        mViewSecurityPrivacy.setOnClickListener(this);
        mViewSetingFont.setOnClickListener(this);
        mViewClear.setOnClickListener(this);
        mBtnExit.setOnClickListener(this);
        mViewAboutAs.setOnClickListener(this);
        mviewPrivacy.setOnClickListener(this);
        viewSelectBackground.setOnClickListener(this);
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });

        msgDao = new MsgDao();
        UserSeting userSeting = msgDao.userSetingGet();
        int voice = userSeting.getVoicePlayer();
        if (voice == 0) {
            cbVoice.setChecked(false);
        } else {
            cbVoice.setChecked(true);
        }


        cbVoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    msgDao.userSetingVoicePlayer(1);
                } else {
                    msgDao.userSetingVoicePlayer(0);
                }
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_account_binding:
                Intent bindingIntent = new Intent(this, AccountBindingActvity.class);
                startActivity(bindingIntent);
                break;
            case R.id.view_new_message:
                Intent messageIntent = new Intent(this, NewMessageActivity.class);
                startActivity(messageIntent);
                break;
            case R.id.view_security_privacy:
                Intent securityIntent = new Intent(this, SecurityPrivacyActivity.class);
                startActivity(securityIntent);
                break;
            case R.id.view_seting_font:
                Intent fontInent = new Intent(this, ChatFontActivity.class);
                startActivity(fontInent);
                break;
            case R.id.view_clear:
                taskClearMsg();
                break;
            case R.id.btn_exit:
                MessageManager.getInstance().clearCache();
                PayEnvironment.getInstance().clear();
                loginOut(UserAction.getMyInfo().isEmptyPassword());
                break;
            case R.id.view_about_as:
                Intent aboutIntent = new Intent(this, AboutAsActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.view_select_background:
                go(BackgroundImageActivity.class);
                break;
            case R.id.view_privacy:
                if (DoubleUtils.isFastDoubleClick()) {//防止快速点击弹出两个界面
                    return;
                }
                Intent intent = new Intent(this, WebPageActivity.class);
                intent.putExtra(WebPageActivity.AGM_URL, AppConfig.USER_PRIVACY);
                startActivity(intent);
                break;
        }
    }

    private void loginOut(boolean emptyPassword) {
        if (!emptyPassword) {
            AlertYesNo alertYesNo = new AlertYesNo();
            alertYesNo.init(this, "退出", "确定要退出登录吗?", "确定", "取消", new AlertYesNo.Event() {
                @Override
                public void onON() {

                }

                @Override
                public void onYes() {
                    taskExit();
                }
            });
            alertYesNo.show();
        } else {
            go(SetingPasswordActitity.class);
        }
    }


    /***
     * 退出
     */
    private void taskExit() {
        finish();
        if (AVChatProfile.getInstance().isAVMinimize()) {
            // 关闭不发送消息
            EventBus.getDefault().post(new EventFactory.CloseMinimizeEvent());
        }
        // 关闭音视频界面
        EventBus.getDefault().post(new EventFactory.CloseVideoActivityEvent());
        NIMClient.getService(AuthService.class).logout();// 登出网易登录
        userAction.loginOut();
        EventBus.getDefault().post(new EventLoginOut(1));

    }

    private MsgAction msgAction = new MsgAction();

    /***
     * 清理消息
     */
    private void taskClearMsg() {
        AlertYesNo alertYesNo = new AlertYesNo();
        alertYesNo.init(this, "提示", "确定清空聊天记录？", "确定", "取消", new AlertYesNo.Event() {
            @Override
            public void onON() {

            }

            @Override
            public void onYes() {
                msgAction.msgDelAll();
                MessageManager.getInstance().setMessageChange(true);
                MessageManager.getInstance().notifyRefreshMsg();
                // 清空本地小视频
                File file = new File(LanSongFileUtil.DEFAULT_DIR);
                if (file.exists()) {
                    LanSongFileUtil.deleteDir(file);
                }
            }
        });
        alertYesNo.show();
    }

}
