package com.hm.cxpay.ui.bank;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.hm.cxpay.R;
import com.hm.cxpay.base.BasePayActivity;
import com.hm.cxpay.bean.BankInfo;
import com.hm.cxpay.bean.BindBankInfo;
import com.hm.cxpay.databinding.ActivityFinishBindBinding;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;

import net.cb.cb.library.utils.CheckUtil;
import net.cb.cb.library.utils.ClickFilter;
import net.cb.cb.library.utils.CountDownUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;

/**
 * @author Liszt
 * @date 2019/11/30
 * Description
 */
public class BindBankFinishActivity extends BasePayActivity {

    private ActivityFinishBindBinding ui;
    private BankInfo bankInfo;
    private BindBankInfo bindInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_finish_bind);
        bankInfo = getIntent().getParcelableExtra("bank");
        bindInfo = getIntent().getParcelableExtra("bindInfo");

        if (bankInfo != null) {
            ui.tvPhone.setText("接收验证码手机号:" + bankInfo.getPhone());
        }

        if (bindInfo != null) {
            initCountDownUtil(false);
        }

        ui.tvGetVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initCountDownUtil(true);
            }
        });
        ClickFilter.onClick(ui.tvNext, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindBank();
            }
        });
        ui.headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
//                onBackPressed();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putParcelable("bank", bankInfo);
                intent.putExtras(bundle);
                setResult(RESULT_CANCELED, intent);
                finish();
            }

            @Override
            public void onRight() {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        ui.tvNext.setEnabled(true);
    }

    private void initCountDownUtil(final boolean isNeedApply) {
        final String phone = bankInfo.getPhone();
        if (TextUtils.isEmpty(phone)) {
            ToastUtil.show(BindBankFinishActivity.this, "请填写手机号码");
            return;
        }
        if (!CheckUtil.isMobileNO(phone)) {
            ToastUtil.show(this, "手机号格式不正确");
            return;
        }

        CountDownUtil.getTimer(60, ui.tvGetVerificationCode, "发送验证码", this, new CountDownUtil.CallTask() {
            @Override
            public void task() {
                if (isNeedApply) {
                    applyBindBank(bankInfo.getBankNumber(), bankInfo.getPhone());
                }

            }
        });

    }

    //获取绑定银行卡签名及验证码
    public void applyBindBank(String bankCardNo, String phone) {
        PayHttpUtils.getInstance().applyBindBank(bankCardNo, phone)
                .compose(RxSchedulers.<BaseResponse<BindBankInfo>>compose())
                .compose(RxSchedulers.<BaseResponse<BindBankInfo>>handleResult())
                .subscribe(new FGObserver<BaseResponse<BindBankInfo>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<BindBankInfo> baseResponse) {
                        if (baseResponse.isSuccess()) {
                            bindInfo = baseResponse.getData();
                            if (bindInfo != null) {

                            }
                        } else {
                            ToastUtil.show(BindBankFinishActivity.this, baseResponse.getMessage());
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        ToastUtil.show(BindBankFinishActivity.this, baseResponse.getMessage());
                    }
                });
    }

    public void bindBank() {
        String verifyCode = ui.etCode.getText().toString().trim();
        if (TextUtils.isEmpty(verifyCode)) {
            ToastUtil.show(this, "验证码不能为空");
            return;
        }
        if (bindInfo == null || bankInfo == null) {
            ToastUtil.show(this, "银行或订单信息不能为空");
            return;
        }
        ui.tvNext.setEnabled(false);
        PayHttpUtils.getInstance().bindBank(bindInfo.getSign(), bankInfo.getBankNumber(), bankInfo.getBankName(), bankInfo.getPhone(), bindInfo.getTranceNum(), bindInfo.getTransDate(), verifyCode)
                .compose(RxSchedulers.<BaseResponse>compose())
                .compose(RxSchedulers.<BaseResponse>handleResult())
                .subscribe(new FGObserver<BaseResponse>() {
                    @Override
                    public void onHandleSuccess(BaseResponse baseResponse) {
                        if (baseResponse.isSuccess()) {
//                            IntentUtil.gotoActivity(BindBankFinishActivity.this, BankSettingActivity.class);
                            ToastUtil.show(BindBankFinishActivity.this, "银行卡绑定成功! ");
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            ui.tvNext.setEnabled(true);
                            ToastUtil.show(BindBankFinishActivity.this, baseResponse.getMessage());
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        ui.tvNext.setEnabled(true);
                        ToastUtil.show(BindBankFinishActivity.this, baseResponse.getMessage());
                    }
                });

    }

}
