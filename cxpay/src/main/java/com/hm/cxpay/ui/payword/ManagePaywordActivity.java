package com.hm.cxpay.ui.payword;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.hm.cxpay.R;

import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

/**
 * @类名：零钱->支付密码管理
 * @Date：2019/12/2
 * @by zjy
 * @备注：
 */
public class ManagePaywordActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private LinearLayout layoutModifyPayword;//修改支付密码
    private LinearLayout layoutFindbackPayword;//找回支付密码
    private Activity activity;

    public static final int FROM_MANAGE_PAY_WORD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_payword);
        activity = this;
        initView();
        initData();
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        layoutModifyPayword = findViewById(R.id.layout_modify_payword);
        layoutFindbackPayword = findViewById(R.id.layout_findback_payword);
        actionbar = headView.getActionbar();
    }

    private void initData() {
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        layoutModifyPayword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(ModifyPaywordActivity.class);
            }
        });
        layoutFindbackPayword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity,ForgetPswStepOneActivity.class).putExtra("from",FROM_MANAGE_PAY_WORD));
            }
        });

    }


}
