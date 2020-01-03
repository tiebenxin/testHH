package com.hm.cxpay.ui.transfer;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.hm.cxpay.R;
import com.hm.cxpay.base.BasePayActivity;
import com.hm.cxpay.databinding.ActivityTransferDetailBinding;
import com.hm.cxpay.databinding.ActivityTransferResultBinding;
import com.hm.cxpay.utils.UIUtils;

import net.cb.cb.library.view.ActionbarView;

/**
 * @author Liszt
 * @date 2019/12/19
 * Description 转账结果页面
 */
public class TransferResultActivity extends BasePayActivity {

    private ActivityTransferResultBinding ui;
    private long money;
    private int resultType;
    private String nick;

    public static Intent newIntent(Context context, long money, int result, String nick) {
        Intent intent = new Intent(context, TransferResultActivity.class);
        intent.putExtra("result", result);
        intent.putExtra("money", money);
        intent.putExtra("nick", nick);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_transfer_result);
        Intent intent = getIntent();
        money = intent.getLongExtra("money", 0);
        resultType = intent.getIntExtra("result", 0);
        nick = intent.getStringExtra("nick");
        initView();
    }

    private void initView() {
        if (resultType == 1) {
            ui.tvStatus.setText("支付成功");
        } else {
            ui.tvStatus.setText("支付申请已成功");
        }
        ui.tvMoney.setText(UIUtils.getYuan(money));
        ui.tvInfo.setText("等待" + nick + "确认收款");

        ui.tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ui.headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
    }

}
