package com.hm.cxpay.dailog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hm.cxpay.R;
import com.hm.cxpay.utils.UIUtils;

import net.cb.cb.library.base.BaseDialog;

/**
 * @author Liszt
 * @date 2019/12/24
 * Description 零钱余额不足弹窗
 */
public class DialogBalanceNoEnough extends BaseDialog {

    private ImageView ivClose, ivBack;
    private TextView tvMoney;
    private TextView tvBalance;
    private LinearLayout llRecharge;
    private IRechargeListener listener;

    public DialogBalanceNoEnough(Context context, int theme) {
        super(context, theme);
    }

    public DialogBalanceNoEnough(Context context) {
        this(context, R.style.MyDialogTheme);
    }

    @Override
    public void initView() {
        setContentView(R.layout.dialog_transfer_balance_no_enough);
        ivClose = findViewById(R.id.iv_close);
        ivBack = findViewById(R.id.iv_back);
        tvMoney = findViewById(R.id.tv_money);
        tvBalance = findViewById(R.id.tv_balance);
        llRecharge = findViewById(R.id.ll_recharge);
        ivClose.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        llRecharge.setOnClickListener(this);
    }

    public void initMoney(long money, long balance) {
        tvMoney.setText(UIUtils.getYuan(money));
        String payer = "零钱" + "(余额¥" + UIUtils.getYuan(balance) + ")";
        tvBalance.setText(payer);
    }

    @Override
    public void processClick(View view) {
        int id = view.getId();
        if (id == ivClose.getId()) {
            dismiss();
        } else if (id == llRecharge.getId() || id == ivBack.getId()) {
            dismiss();
            if (listener != null) {
                listener.onRecharge();
            }
        }
    }

    public interface IRechargeListener {
        void onRecharge();
    }

    public void setListener(IRechargeListener l) {
        listener = l;
    }
}
