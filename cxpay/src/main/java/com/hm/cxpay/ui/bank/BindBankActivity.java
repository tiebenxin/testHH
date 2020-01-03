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
import com.hm.cxpay.bean.UserBean;
import com.hm.cxpay.databinding.ActivityBindBankBinding;
import com.hm.cxpay.global.PayEnvironment;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.net.Route;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;

import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.WebPageActivity;


/**
 * 添加银行卡
 */
public class BindBankActivity extends BasePayActivity {

    private ActivityBindBankBinding ui;
    private UserBean user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_bind_bank);
        initView();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ui.tvNext.setEnabled(true);
    }

    private void initView() {
        user = PayEnvironment.getInstance().getUser();
        if (user != null) {
            ui.tvName.setText(user.getRealName());
        }
        ui.tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bank = ui.etBank.getText().toString().trim();
                if (TextUtils.isEmpty(bank)) {
                    ToastUtil.show(BindBankActivity.this, "银行卡号不能为空");
                    return;
                }
                checkBankCard(bank);
            }
        });

        ui.tvViewSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BindBankActivity.this, WebPageActivity.class);
                intent.putExtra(WebPageActivity.AGM_URL, Route.SUPPORT_BANK_URL);
                startActivity(intent);
                ui.tvViewSupport.setEnabled(false);
            }
        });

    }


    private void initEvent() {
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

    public void checkBankCard(final String bankCardNo) {
        ui.tvNext.setEnabled(false);
        PayHttpUtils.getInstance().checkBankCard(bankCardNo)
                .compose(RxSchedulers.<BaseResponse<BankInfo>>compose())
                .compose(RxSchedulers.<BaseResponse<BankInfo>>handleResult())
                .subscribe(new FGObserver<BaseResponse<BankInfo>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<BankInfo> baseResponse) {
                        if (baseResponse.isSuccess()) {
                            BankInfo info = baseResponse.getData();
                            if (user != null) {
                                info.setOwnerName(user.getRealName());
                                info.setOwnerId(user.getIdentityNo());
                                info.setBankNumber(bankCardNo);
                            }
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("bank", info);
//                            IntentUtil.gotoActivity(BindBankActivity.this, InputPhoneActivity.class, bundle);
                            Intent intent = new Intent(BindBankActivity.this, InputPhoneActivity.class);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, BankSettingActivity.REQUEST_BIND);
                        } else {
                            ui.tvNext.setEnabled(true);
                            ToastUtil.show(BindBankActivity.this, baseResponse.getMessage());
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        ui.tvNext.setEnabled(true);
                        ToastUtil.show(BindBankActivity.this, baseResponse.getMessage());
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
            ui.etBank.setText(info.getBankNumber());
        }
    }
}


























