package com.hm.cxpay.ui.identification;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.hm.cxpay.R;
import com.hm.cxpay.base.BasePayActivity;
import com.hm.cxpay.bean.UserBean;
import com.hm.cxpay.databinding.ActivityIdentificationInfoBinding;
import com.hm.cxpay.global.PayEnvironment;

import net.cb.cb.library.view.ActionbarView;

/**
 * @author Liszt
 * @date 2019/11/29
 * Description  认证账号信息展示页面
 */
public class IdentificationInfoActivity extends BasePayActivity {

    private ActivityIdentificationInfoBinding ui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_identification_info);
        UserBean userBean = PayEnvironment.getInstance().getUser();
        if (userBean != null) {
            ui.tvName.setText(userBean.getRealName());
            ui.tvId.setText(userBean.getIdentityNo());
        }

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
