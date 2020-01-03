package com.hm.cxpay.ui.payword;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hm.cxpay.R;
import com.hm.cxpay.bean.CommonBean;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;

import net.cb.cb.library.utils.ClickFilter;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

/**
 * @类名：忘记密码->第一步->验证身份
 * @Date：2019/12/12
 * @by zjy
 * @备注：
 */
public class ForgetPswStepOneActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private EditText etName;
    private EditText etIdcard;
    private TextView tvSubmit;

    private Activity activity;
    private String token;//得到认证需要的token
    private int from;//从哪里跳转过来的 (1 密码校验 2 密码管理)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_psw_step_one);
        activity = this;
        initView();
        initData();
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        etName = findViewById(R.id.et_name);
        etIdcard = findViewById(R.id.et_idcard);
        tvSubmit = findViewById(R.id.tv_submit);
    }

    private void initData() {
        fromWhere();
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        ClickFilter.onClick(tvSubmit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1 姓名不为空
                if(!TextUtils.isEmpty(etName.getText().toString())){
                    //2 身份证号不为空
                    if(!TextUtils.isEmpty(etIdcard.getText().toString())){
                        httpForgetPswStepOne(etIdcard.getText().toString(),etName.getText().toString());
                    }else {
                        ToastUtil.show(activity,"身份证号不能为空");
                    }
                }else {
                    ToastUtil.show(activity,"姓名不能为空");
                }
            }
        });
    }

    private void fromWhere() {
        if(getIntent()!=null){
            from = getIntent().getIntExtra("from",0);
        }
    }

    /**
     * 发请求->找回密码第一步->验证实名信息
     */
    private void httpForgetPswStepOne(String idNumber,String realName) {
        PayHttpUtils.getInstance().checkRealNameInfo(idNumber,realName)
                .compose(RxSchedulers.<BaseResponse<CommonBean>>compose())
                .compose(RxSchedulers.<BaseResponse<CommonBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<CommonBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<CommonBean> baseResponse) {
                        if(baseResponse.getData()!=null){
                            if(!TextUtils.isEmpty(baseResponse.getData().getToken())){
                                token = baseResponse.getData().getToken();
                                Bundle bundle = new Bundle();
                                bundle.putString("token",token);
                                bundle.putString("name",etName.getText().toString());
                                bundle.putInt("from",from);
                                Intent intent = new Intent(activity,ForgetPswStepTwoActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse<CommonBean> baseResponse) {
                        super.onHandleError(baseResponse);
                        ToastUtil.show(context, baseResponse.getMessage());
                    }
                });
    }

}
