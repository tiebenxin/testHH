package com.hm.cxpay.ui.recharege;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.hm.cxpay.R;
import com.hm.cxpay.ui.LooseChangeActivity;

import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

/**
 * @类名：充值成功-充值详情
 * @Date：2019/12/25
 * @by zjy
 * @备注：
 */
public class RechargeSuccessActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private TextView tvSubmit;
    private TextView tvMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge_success);
        initView();
        getExtra();
        initData();
    }

    private void getExtra() {
        if(getIntent()!=null){
            if(!TextUtils.isEmpty(getIntent().getStringExtra("money"))){
                tvMoney.setText("充值金额: ￥"+getIntent().getStringExtra("money"));
            }
        }
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        tvSubmit = findViewById(R.id.tv_submit);
        tvMoney = findViewById(R.id.tv_money);

    }

    private void initData() {
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                go(LooseChangeActivity.class);//回零钱首页
            }

            @Override
            public void onRight() {

            }
        });
        tvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(LooseChangeActivity.class);
            }
        });

    }
}
