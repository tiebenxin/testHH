package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.yanlong.im.R;

import net.cb.cb.library.view.AppActivity;

public class SelectLoginActivity extends AppActivity implements View.OnClickListener {

    private Button mBtnLogin;
    private Button mBtnRegister;
    private ImageView mIvWechat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_login);
        initView();
        initEvent();
    }


    private void initView(){
        mBtnLogin =  findViewById(R.id.btn_login);
        mBtnRegister =  findViewById(R.id.btn_register);
        mIvWechat =  findViewById(R.id.iv_wechat);
    }


    private void initEvent(){
        mBtnLogin.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
        mIvWechat.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
                Intent loginIntent = new Intent(this,PasswordLoginActivity.class);
                startActivity(loginIntent);
                break;
            case R.id.btn_register:
                Intent registerIntent = new Intent(this,RegisterActivity.class);
                startActivity(registerIntent);
                break;
            case R.id.iv_wechat:

                break;
        }
    }
}
