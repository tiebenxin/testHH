package com.yanlong.im.user.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.NewVersionBean;
import com.yanlong.im.user.bean.VersionBean;
import com.yanlong.im.utils.update.UpdateManage;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.VersionUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

import retrofit2.Call;
import retrofit2.Response;

public class AboutAsActivity extends AppActivity {

    private HeadView mHeadView;
    private ImageView mIvLogo;
    private TextView mTvVersionNumber;
    private LinearLayout mLlCheckVersions;
    private LinearLayout mLlService;
    private TextView tvNewVersions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_as);
        initView();
        initEvent();
    }

    private void initView() {
        mHeadView = findViewById(R.id.headView);
        mIvLogo = findViewById(R.id.iv_logo);
        mTvVersionNumber = findViewById(R.id.tv_version_number);
        mLlCheckVersions = findViewById(R.id.ll_check_versions);
        mLlService = findViewById(R.id.ll_service);
        mTvVersionNumber.setText("常信     " + VersionUtil.getVerName(this));
        tvNewVersions =  findViewById(R.id.tv_new_versions);

        SharedPreferencesUtil sharedPreferencesUtil = new  SharedPreferencesUtil(SharedPreferencesUtil.SPName.NEW_VESRSION);
        VersionBean bean = sharedPreferencesUtil.get4Json(VersionBean.class);
        if(bean != null && !TextUtils.isEmpty(bean.getVersion())){
            if(new UpdateManage(context,AboutAsActivity.this).check(bean.getVersion())){
                tvNewVersions.setVisibility(View.VISIBLE);
            }else{
                tvNewVersions.setVisibility(View.GONE);
            }
        }
    }


    private void initEvent() {
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });

        mLlCheckVersions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskNewVersion();
            }
        });

        mLlService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(FeedbackActivity.class);
            }
        });
    }


    private void taskNewVersion() {
        new UserAction().getNewVersion(StringUtil.getChannelName(context),new CallBack<ReturnBean<NewVersionBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<NewVersionBean>> call, Response<ReturnBean<NewVersionBean>> response) {
                if (response.body() == null || response.body().getData() == null) {
                    return;
                }
                if (response.body().isOk()) {
                    NewVersionBean bean = response.body().getData();
                    if (!new UpdateManage(context, AboutAsActivity.this).check(bean.getVersion())) {
                        ToastUtil.show(context, "已经是最新版本");
                        return;
                    }

                    UpdateManage updateManage = new UpdateManage(context, AboutAsActivity.this);
                    if (response.body().getData().getForceUpdate() == 0) {
                        //updateManage.uploadApp(bean.getVersion(), bean.getContent(), bean.getUrl(), false);
                        updateManage.uploadApp(bean.getVersion(), bean.getContent(), bean.getUrl(), false);
                    } else {
                        updateManage.uploadApp(bean.getVersion(), bean.getContent(), bean.getUrl(), true);
                    }
                }
            }
        });
    }

}
