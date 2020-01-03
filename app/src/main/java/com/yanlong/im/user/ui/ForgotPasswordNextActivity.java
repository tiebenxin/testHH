package com.yanlong.im.user.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yanlong.im.R;

import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

public class ForgotPasswordNextActivity extends AppActivity {
    public final static String PHONE = "phone";
    private TextView mTvTitle;
    private EditText mEtNewPasswordContent;
    private EditText mEtRepetitionPasswordContent;
    private Button mBtnConfirm;
    private HeadView mHeadView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_next);
        initView();
        initEvent();
    }


    private void initView() {
        mTvTitle = findViewById(R.id.tv_title);
        mHeadView = findViewById(R.id.headView);
        mEtNewPasswordContent = findViewById(R.id.et_new_password_content);
        mEtRepetitionPasswordContent = findViewById(R.id.et_repetition_password_content);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mTvTitle.setText("手机号:" + getIntent().getStringExtra(PHONE));
    }

    private void initEvent() {
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
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

    private void confirm() {
        String password = mEtNewPasswordContent.getText().toString();
        String nextPassword = mEtRepetitionPasswordContent.getText().toString();
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

    }


}
