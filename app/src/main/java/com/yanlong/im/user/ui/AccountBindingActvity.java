package com.yanlong.im.user.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yanlong.im.R;

import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AlertYesNo;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;
import net.cb.cb.library.view.LoadView;

public class AccountBindingActvity extends AppActivity {

    private HeadView mHeadView;
    private LoadView mLoadView;
    private TextView mTvTitle;
    private Button mBtnUnbound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_binding);
        initView();
        initEvent();
    }

    private void initView() {
        mHeadView = findViewById(R.id.headView);
        mLoadView = findViewById(R.id.load_view);
        mTvTitle = findViewById(R.id.tv_title);
        mBtnUnbound = findViewById(R.id.btn_unbound);
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
        mBtnUnbound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertYesNo alertYesNo = new AlertYesNo();
                alertYesNo.init(AccountBindingActvity.this, "提示",
                        "确定要解除与该微信绑定吗?", "确定", "取消", new AlertYesNo.Event() {
                     @Override
                     public void onON() {

                     }

                     @Override
                     public void onYes() {

                     }
                 });
                alertYesNo.show();

            }
        });
    }


}
