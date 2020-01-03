package com.yanlong.im.user.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yanlong.im.R;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.utils.PasswordTextWather;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.ClickFilter;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

import retrofit2.Call;
import retrofit2.Response;

public class ChangePasswordActivity extends AppActivity {

    private HeadView mHeadView;
    private EditText mEdOldPassword;
    private EditText mEdNewPassword;
    private EditText mEdNextPassword;
    private Button mBtnCommit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initView();
        initEvent();
    }

    private void initView() {
        mHeadView = findViewById(R.id.headView);
        mEdOldPassword = findViewById(R.id.ed_old_password);
        mEdNewPassword = findViewById(R.id.ed_new_password);
        mEdNextPassword = findViewById(R.id.ed_next_password);
        mBtnCommit = findViewById(R.id.btn_commit);

    }

    private void initEvent() {
        mEdOldPassword.addTextChangedListener(new PasswordTextWather(mEdOldPassword,this));
        mEdNewPassword.addTextChangedListener(new PasswordTextWather(mEdNewPassword,this));
        mEdNextPassword.addTextChangedListener(new PasswordTextWather(mEdNextPassword,this));
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        ClickFilter.onClick(mBtnCommit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commit();
            }
        });
    }


    private void commit(){
        String oldPassword = mEdOldPassword.getText().toString();
        String newPassword = mEdNewPassword.getText().toString();
        String nextPassword = mEdNextPassword.getText().toString();
        if(TextUtils.isEmpty(oldPassword)){
            ToastUtil.show(this,"请填写旧密码");
            return;
        }
        if(TextUtils.isEmpty(newPassword)){
            ToastUtil.show(this,"请填写新密码");
            return;
        }
        if(newPassword.length() < 6){
            ToastUtil.show(context,"密码不能少于六位");
            return;
        }
        if(TextUtils.isEmpty(nextPassword)){
            ToastUtil.show(this,"请再次填写新密码");
            return;
        }
        if(!newPassword.equals(nextPassword)){
            ToastUtil.show(this,"两次填写密码不一致");
            return;
        }
        taskSetUserPassword(newPassword,oldPassword);
    }


    private void taskSetUserPassword(String newPassword,String oldPassword){
        new UserAction().setUserPassword(newPassword, oldPassword, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if(response.body() == null){
                    return;
                }
                if(response.body().isOk()){
                    finish();
                }
                ToastUtil.show(ChangePasswordActivity.this,response.body().getMsg());
            }
        });
    }


}
