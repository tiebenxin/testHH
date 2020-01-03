package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.utils.PasswordTextWather;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.CheckUtil;
import net.cb.cb.library.utils.CountDownUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

import retrofit2.Call;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppActivity implements View.OnClickListener {
    private EditText mEtPhoneContent;
    private EditText mEtIdentifyingCodeContent;
    private TextView mTvGetVerificationCode;
    private EditText mEtNewPasswordContent;
    private EditText mEtRepetitionPasswordContent;
    private Button mBtnNext;
    private HeadView mHeadView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        initView();
        initEvent();
    }


    private void initView() {
        mHeadView = findViewById(R.id.headView);
        mEtPhoneContent = findViewById(R.id.et_phone_content);
        mEtIdentifyingCodeContent = findViewById(R.id.et_identifying_code_content);
        mBtnNext = findViewById(R.id.btn_next);
        mTvGetVerificationCode = findViewById(R.id.tv_get_verification_code);
        mEtNewPasswordContent = findViewById(R.id.et_new_password_content);
        mEtRepetitionPasswordContent = findViewById(R.id.et_repetition_password_content);

    }

    private void initEvent() {
        mBtnNext.setOnClickListener(this);
        mTvGetVerificationCode.setOnClickListener(this);
        mEtNewPasswordContent.addTextChangedListener(new PasswordTextWather(mEtNewPasswordContent,this));
        mEtRepetitionPasswordContent.addTextChangedListener(new PasswordTextWather(mEtRepetitionPasswordContent,this));
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                register();
                break;
            case R.id.tv_get_verification_code:
                initCountDownUtil();
                break;
        }
    }


    private void initCountDownUtil() {
        final String phone = mEtPhoneContent.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            ToastUtil.show(ForgotPasswordActivity.this, "请填写手机号码");
            return;
        }
        if(!CheckUtil.isMobileNO(phone)){
            ToastUtil.show(this, "手机号格式不正确");
            return;
        }

        CountDownUtil.getTimer(60, mTvGetVerificationCode, "发送验证码", this, new CountDownUtil.CallTask() {
            @Override
            public void task() {
                taskGetSms(phone);
            }
        });
    }

    private void register() {
        String phone = mEtPhoneContent.getText().toString();
        String code = mEtIdentifyingCodeContent.getText().toString();
        String password = mEtNewPasswordContent.getText().toString();
        String nextPassword = mEtRepetitionPasswordContent.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            ToastUtil.show(this, "请输入手机号");
            return;
        }
        if (TextUtils.isEmpty(code)) {
            ToastUtil.show(this, "请输入验证码");
            return;
        }
        if(!CheckUtil.isMobileNO(phone)){
            ToastUtil.show(this, "手机号格式不正确");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtil.show(this, "请输入新密码");
            return;
        }
        if (TextUtils.isEmpty(nextPassword)) {
            ToastUtil.show(this, "请再次输入新密码");
            return;
        }
        if (!password.equals(nextPassword)) {
            ToastUtil.show(this, "两次输入密码不一致");
            return;
        }

        taskChangePasswordBySms(phone, Integer.valueOf(code), password);

    }


    private void taskChangePasswordBySms(String phone, Integer captcha, String password) {
        new UserAction().changePasswordBySms(phone, captcha, password, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                ToastUtil.show(context, response.body().getMsg());
                if (response.body().isOk()) {
                    finish();
                }
            }
        });
    }


    private void taskGetSms(String phone) {
        new UserAction().smsCaptchaGet(phone, "password", new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                ToastUtil.show(ForgotPasswordActivity.this, response.body().getMsg());
            }
        });
    }


}
