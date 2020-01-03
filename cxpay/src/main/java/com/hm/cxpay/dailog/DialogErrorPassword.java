package com.hm.cxpay.dailog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.hm.cxpay.R;

import net.cb.cb.library.base.BaseDialog;

/**
 * @author Liszt
 * @date 2019/12/4
 * Description 发送红包时，支付密码错误弹窗
 */
public class DialogErrorPassword extends BaseDialog {

    private TextView tvTryAgain;
    private TextView tvForget;
    private IErrorPasswordListener listener;

    public DialogErrorPassword(Context context, int theme) {
        super(context, theme);
        setCanceledOnTouchOutside(false);

    }

    @Override
    public void initView() {
        setContentView(R.layout.dialog_payword_error);
        tvTryAgain = findViewById(R.id.tv_try_again);
        tvForget = findViewById(R.id.tv_forget_psw);
        tvForget.setOnClickListener(this);
        tvTryAgain.setOnClickListener(this);

    }

    @Override
    public void processClick(View view) {
        int id = view.getId();
        if (id == tvForget.getId()) {
            if (listener != null) {
                listener.onForget();
                dismiss();
            }
        } else if (id == tvTryAgain.getId()) {
            if (listener != null) {
                listener.onTry();
                dismiss();
            }
        }
    }

    public void setListener(IErrorPasswordListener l) {
        listener = l;
    }

    public interface IErrorPasswordListener {
        //忘记密码
        void onForget();

        //重试一次
        void onTry();
    }
}
