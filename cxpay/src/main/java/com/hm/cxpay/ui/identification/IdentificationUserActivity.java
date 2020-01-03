package com.hm.cxpay.ui.identification;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.hm.cxpay.R;
import com.hm.cxpay.base.BasePayActivity;
import com.hm.cxpay.databinding.ActivityIdentificationCentreBinding;
import com.hm.cxpay.eventbus.IdentifyUserEvent;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.ui.BindPhoneNumActivity;

import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;

import org.greenrobot.eventbus.EventBus;


/**
 * 用户认证界面
 */
public class IdentificationUserActivity extends BasePayActivity {

    private ActivityIdentificationCentreBinding ui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_identification_centre);
        initView();
        initEvent();
    }

    private void initView() {
        ui.headView.setTitle("实名认证");
        ui.tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idCard = ui.etIdcard.getText().toString().trim();
                String name = ui.etName.getText().toString().trim();
                if (TextUtils.isEmpty(idCard)) {
                    ToastUtil.show(IdentificationUserActivity.this, "身份证号码不能为空");
                    return;
                }
                if (TextUtils.isEmpty(name)) {
                    ToastUtil.show(IdentificationUserActivity.this, "真实姓名不能为空");
                    return;
                }
                //TODO:检测身份证号码是否正确
                authUser(idCard, name);
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

    public void authUser(String idNum, String realName) {
        ui.tvNext.setEnabled(false);
        PayHttpUtils.getInstance().authUserInfo(idNum, realName)
                .compose(RxSchedulers.<BaseResponse>compose())
                .compose(RxSchedulers.<BaseResponse>handleResult())
                .subscribe(new FGObserver<BaseResponse>() {
                    @Override
                    public void onHandleSuccess(BaseResponse baseResponse) {
                        if (baseResponse.isSuccess()) {
                            EventBus.getDefault().post(new IdentifyUserEvent());
//                            ToastUtil.show(IdentificationUserActivity.this, "认证成功!");
                            go(BindPhoneNumActivity.class);
                            finish();
                        } else {
                            ui.tvNext.setEnabled(true);
                            ToastUtil.show(IdentificationUserActivity.this, baseResponse.getMessage());
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        ui.tvNext.setEnabled(true);
                        ToastUtil.show(IdentificationUserActivity.this, baseResponse.getMessage());
                    }
                });
    }


}


























