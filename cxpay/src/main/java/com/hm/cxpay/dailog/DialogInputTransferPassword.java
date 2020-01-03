package com.hm.cxpay.dailog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hm.cxpay.R;
import com.hm.cxpay.bean.BankBean;
import com.hm.cxpay.bean.UserBean;
import com.hm.cxpay.global.PayEnum;
import com.hm.cxpay.global.PayEnvironment;
import com.hm.cxpay.utils.UIUtils;
import com.hm.cxpay.widget.PswView;

import net.cb.cb.library.base.BaseDialog;

/**
 * @author Liszt
 * @date 2019/12/3
 * Description 输入支付密码dialog
 */
public class DialogInputTransferPassword extends BaseDialog {

    private TextView tvMoney;
    private TextView tvPayer;
    private ImageView ivIcon;
    private PswView pswView;
    private IPswListener listener;
    private int payStyle;
    private BankBean bankBean;
    private ImageView ivClose;
    private LinearLayout llPayStyle;

    public DialogInputTransferPassword(Context context) {
        this(context, R.style.MyDialogTheme);
    }


    public DialogInputTransferPassword(Context context, int theme) {
        super(context, theme);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void initView() {
        setContentView(R.layout.dialog_input_transfer_pwd);
        ivClose = findViewById(R.id.iv_close);
        tvMoney = findViewById(R.id.tv_money);
        tvPayer = findViewById(R.id.tv_payer);
        ivIcon = findViewById(R.id.iv_icon);
        pswView = findViewById(R.id.psw_view);
        llPayStyle = findViewById(R.id.ll_pay_style);
        pswView.setOnPasswordChangedListener(new PswView.onPasswordChangedListener() {
            @Override
            public void setPasswordChanged(String password) {
                if (!TextUtils.isEmpty(password)) {
                    if (listener != null) {
                        if (payStyle == PayEnum.EPayStyle.LOOSE) {
                            listener.onCompleted(password, -1);
                        } else if (payStyle == PayEnum.EPayStyle.BANK && bankBean != null) {
                            listener.onCompleted(password, bankBean.getId());
                        }
                    }
                }
            }
        });

        ivClose.setOnClickListener(this);
        llPayStyle.setOnClickListener(this);
    }

    @Override
    public void processClick(View view) {
        int id = view.getId();
        if (id == ivClose.getId()) {
            dismiss();
        } else if (id == llPayStyle.getId()) {
            dismiss();
            if (listener != null) {
                listener.selectPayStyle();
            }
        }
    }

    public void init(long money, @PayEnum.EPayStyle int payStyle, BankBean info) {
        this.payStyle = payStyle;
        bankBean = info;
        tvMoney.setText(UIUtils.getYuan(money));
        if (payStyle == PayEnum.EPayStyle.LOOSE) {
            String payer = "零钱";
            UserBean userBean = PayEnvironment.getInstance().getUser();
            if (userBean != null && userBean.getBalance() > 0) {
                payer += "(余额¥" + UIUtils.getYuan(userBean.getBalance()) + ")";
            }
            tvPayer.setText(payer);
            ivIcon.setImageDrawable(UIUtils.getDrawable(getContext(), R.mipmap.ic_loose));
        } else {
            if (info != null) {
                tvPayer.setText(info.getBankName());
                Glide.with(getContext()).load(info.getLogo()).into(ivIcon);
            }
        }
    }

    //清空密码，重新输入
    public void clearPsw() {
        if (pswView != null) {
            pswView.clear();
        }
    }

    public View getPswView() {
        return pswView;
    }

    public void setPswListener(IPswListener l) {
        listener = l;
    }

    public interface IPswListener {
        /**
         * 支付密码完成
         * 注意：bankCardId 银行卡id，不是卡号
         */
        void onCompleted(String psw, long bankCardId);

        //选择支付方式
        void selectPayStyle();
    }

    //获取选中银行卡
    public BankBean getSelectedBank() {
        return bankBean;
    }
}
