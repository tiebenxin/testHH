package com.hm.cxpay.ui.withdraw;

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
 * @类名：提现成功-提现详情
 * @Date：2019/12/25
 * @by zjy
 * @备注：
 */
public class WithdrawSuccessActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private TextView tvSubmit;
    private TextView tvBankName;//提现银行
    private TextView tvWithdrawMoney;//提现金额
    private TextView tvServiceFee;//手续费
    private TextView tvGetMoney;//到账金额

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_success);
        initView();
        getExtra();
        initData();
    }

    private void getExtra() {
        if(getIntent()!=null){
            if(!TextUtils.isEmpty(getIntent().getStringExtra("bank_name"))){
                tvBankName.setText(getIntent().getStringExtra("bank_name"));
            }
            if(!TextUtils.isEmpty(getIntent().getStringExtra("withdraw_money"))){
                tvWithdrawMoney.setText("￥"+getIntent().getStringExtra("withdraw_money"));
            }
            if(!TextUtils.isEmpty(getIntent().getStringExtra("service_fee"))){
                tvServiceFee.setText("￥"+getIntent().getStringExtra("service_fee"));
            }
            if(!TextUtils.isEmpty(getIntent().getStringExtra("get_money"))){
                tvGetMoney.setText("￥"+getIntent().getStringExtra("get_money"));
            }
        }
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        tvSubmit = findViewById(R.id.tv_submit);
        tvBankName = findViewById(R.id.tv_bank_name);
        tvWithdrawMoney = findViewById(R.id.tv_withdraw_money);
        tvServiceFee = findViewById(R.id.tv_service_fee);
        tvGetMoney = findViewById(R.id.tv_get_money);

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
