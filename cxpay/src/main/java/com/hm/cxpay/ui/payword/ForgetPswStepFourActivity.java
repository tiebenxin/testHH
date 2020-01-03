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
import net.cb.cb.library.utils.CountDownUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

/**
 * @类名：忘记密码->第四步->验证短信验证码
 * @Date：2019/12/12
 * @by zjy
 * @备注：
 */
public class ForgetPswStepFourActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private TextView tvGetCode;
    private EditText etCode;
    private TextView tvSubmit;

    private Activity activity;
    private String oldToken;//旧token，若验证码迟迟收不到，需要再重发绑卡请求来获取验证码
    private String cardNo;//得到银行卡号
    private String bankName;//得到银行名
    private String phoneNum;//手机号
    private String newToken;//若验证码获取成功则直接用新token验证
    private int from;//从哪里跳转过来的 (1 密码校验 2 密码管理)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_psw_step_four);
        activity = this;
        initView();
        initData();
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        tvGetCode = findViewById(R.id.tv_get_code);
        etCode = findViewById(R.id.et_code);
        tvSubmit = findViewById(R.id.tv_submit);
    }

    private void initData() {
        getBundle();
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        tvGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initCountDownUtil();
            }
        });
        ClickFilter.onClick(tvSubmit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1 验证码不为空
                if(!TextUtils.isEmpty(etCode.getText().toString())){
                    httpForgetPswStepFour();
                }else {
                    ToastUtil.show(activity,"验证码不能为空");
                }
            }
        });
    }

    /**
     * 发请求->找回密码第四步->检测短信验证码
     */
    private void httpForgetPswStepFour() {
        PayHttpUtils.getInstance().checkCode(newToken,etCode.getText().toString())
                .compose(RxSchedulers.<BaseResponse<CommonBean>>compose())
                .compose(RxSchedulers.<BaseResponse<CommonBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<CommonBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<CommonBean> baseResponse) {
                        if(baseResponse.getData()!=null){
                            if(!TextUtils.isEmpty(baseResponse.getData().getToken())){
                                //最后一个token，再跳修改密码
                                String finalToken = baseResponse.getData().getToken();
                                Bundle bundle = new Bundle();
                                bundle.putString("final_token",finalToken);
                                bundle.putInt("from",from);
                                Intent intent = new Intent(activity,ModifyPaywordActivity.class);
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

    //获取传过来的值
    private void getBundle() {
        if (getIntent() != null) {
            if (getIntent().getExtras() != null) {
                if (getIntent().getExtras().containsKey("old_token")) {
                    oldToken = getIntent().getExtras().getString("old_token");
                }
                if (getIntent().getExtras().containsKey("card_no")) {
                    cardNo = getIntent().getExtras().getString("card_no");
                }
                if (getIntent().getExtras().containsKey("bank_name")) {
                    bankName = getIntent().getExtras().getString("bank_name");
                }
                if (getIntent().getExtras().containsKey("phone_num")) {
                    phoneNum = getIntent().getExtras().getString("phone_num");
                }
                if (getIntent().getExtras().containsKey("new_token")) {
                    newToken = getIntent().getExtras().getString("new_token");
                }
                if (getIntent().getExtras().containsKey("from")) {
                    from = getIntent().getExtras().getInt("from");
                }
            }
        }
    }

    private void initCountDownUtil() {
        CountDownUtil.getTimer(60, tvGetCode, "重新获取验证码", this, new CountDownUtil.CallTask() {
            @Override
            public void task() {
                httpForgetPswStepThree();
            }
        });

    }

    /**
     * 发请求->绑定银行卡->适用于迟迟收不到验证的情况
     */
    private void httpForgetPswStepThree() {
        PayHttpUtils.getInstance().bindBankCard(cardNo,bankName,phoneNum,oldToken)
                .compose(RxSchedulers.<BaseResponse<CommonBean>>compose())
                .compose(RxSchedulers.<BaseResponse<CommonBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<CommonBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<CommonBean> baseResponse) {
                        if(baseResponse.getData()!=null){
                            if(!TextUtils.isEmpty(baseResponse.getData().getToken())){
                                newToken = baseResponse.getData().getToken();
                            }
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse<CommonBean> baseResponse) {
                        super.onHandleError(baseResponse);
                    }
                });
    }


}
