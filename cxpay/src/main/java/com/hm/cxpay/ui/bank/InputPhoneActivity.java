package com.hm.cxpay.ui.bank;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.hm.cxpay.R;
import com.hm.cxpay.base.BasePayActivity;
import com.hm.cxpay.bean.BankInfo;
import com.hm.cxpay.bean.BindBankInfo;
import com.hm.cxpay.databinding.ActivityInputPhoneBinding;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.net.Route;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;

import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.WebPageActivity;


/**
 * 添加需要绑定银行卡的预留手机号
 */
public class InputPhoneActivity extends BasePayActivity {

    private ActivityInputPhoneBinding ui;
    private BankInfo bankInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_input_phone);
        bankInfo = getIntent().getParcelableExtra("bank");
        initView();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ui.tvNext.setEnabled(true);
        ui.ivCheck.setEnabled(true);
    }

    private void initView() {
        if (bankInfo != null) {
            ui.tvBankName.setText(bankInfo.getBankName());
            ui.tvBankNum.setText(bankInfo.getBankNumber());
            ui.tvName.setText(bankInfo.getOwnerName());
            ui.tvCardId.setText(bankInfo.getOwnerId());
        }
        ui.ivCheck.setSelected(true);//默认选中
        ui.tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ui.tvNext.setEnabled(false);
                String phone = ui.etPhoneNum.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    ToastUtil.show(InputPhoneActivity.this, "手机号不能为空");
                    ui.tvNext.setEnabled(true);
                    return;
                }
                if (bankInfo == null || TextUtils.isEmpty(bankInfo.getBankNumber())) {
                    ToastUtil.show(InputPhoneActivity.this, "银行卡号不能为空");
                    ui.tvNext.setEnabled(true);
                    return;
                }
                bankInfo.setPhone(phone);
                applyBindBank(bankInfo.getBankNumber(), bankInfo.getPhone());

            }
        });
        ui.ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ui.ivCheck.setSelected(ui.ivCheck.isSelected() ? false : true);
            }
        });

        ui.tvAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InputPhoneActivity.this, WebPageActivity.class);
                intent.putExtra(WebPageActivity.AGM_URL, Route.URL_USER_AUTH);
                startActivity(intent);
                ui.tvAgreement.setEnabled(false);
            }
        });

    }


    private void initEvent() {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BankSettingActivity.REQUEST_BIND) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            } else if (resultCode == RESULT_CANCELED) {
                if (data != null) {
                    BankInfo info = data.getParcelableExtra("bank");
                    if (info != null) {
                        bankInfo = info;
                        initBankInfo(info);
                    }
                }
            }
        }
    }

    private void initBankInfo(BankInfo info) {
        if (info == null) {
            return;
        }
        if (!TextUtils.isEmpty(info.getBankNumber())) {
            ui.tvBankName.setText(bankInfo.getBankName());
            ui.tvBankNum.setText(bankInfo.getBankNumber());
            ui.tvName.setText(bankInfo.getOwnerName());
            ui.tvCardId.setText(bankInfo.getOwnerId());
            if (!TextUtils.isEmpty(bankInfo.getPhone())) {
                ui.etPhoneNum.setText(bankInfo.getPhone());
            }
        }
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
                            BindBankInfo bindInfo = baseResponse.getData();
                            if (bindInfo != null) {
                                Intent intent = new Intent(InputPhoneActivity.this, BindBankFinishActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("bank", bankInfo);
                                bundle.putParcelable("bindInfo", bindInfo);
                                intent.putExtras(bundle);
                                startActivityForResult(intent, BankSettingActivity.REQUEST_BIND);
                            }
                        } else {
                            ui.tvNext.setEnabled(true);
                            ToastUtil.show(InputPhoneActivity.this, baseResponse.getMessage());
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        ui.tvNext.setEnabled(true);
                        ToastUtil.show(InputPhoneActivity.this, baseResponse.getMessage());
                    }
                });
    }

}


























