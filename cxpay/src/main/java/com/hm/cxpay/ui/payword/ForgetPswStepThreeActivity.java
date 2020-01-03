package com.hm.cxpay.ui.payword;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hm.cxpay.R;
import com.hm.cxpay.bean.CommonBean;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.net.Route;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;

import net.cb.cb.library.utils.ClickFilter;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;
import net.cb.cb.library.view.WebPageActivity;

/**
 * @类名：忘记密码->第三步->银行卡绑定手机号
 * @Date：2019/12/12
 * @by zjy
 * @备注：
 */
public class ForgetPswStepThreeActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private TextView tvCardType;
    private EditText etPhone;
    private TextView tvSubmit;
    private ImageView ivCheck;
    private TextView tvViewSupport;

    private Activity activity;
    private String token;//得到认证需要的token
    private String cardNo;//得到银行卡号
    private String bankName;//得到银行名
    private String cardType  = "(借记卡)";//默认固定一种
    private int from;//从哪里跳转过来的 (1 密码校验 2 密码管理)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_psw_step_three);
        activity = this;
        initView();
        initData();
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        tvCardType = findViewById(R.id.tv_card_type);
        etPhone = findViewById(R.id.et_phone);
        tvSubmit = findViewById(R.id.tv_submit);
        ivCheck = findViewById(R.id.iv_check);
        tvViewSupport = findViewById(R.id.tv_view_support);
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
        ivCheck.setSelected(true);//默认选中
        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivCheck.setSelected(ivCheck.isSelected() ? false : true);
            }
        });
        ClickFilter.onClick(tvSubmit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1 手机号不为空
                if(!TextUtils.isEmpty(etPhone.getText().toString())){
                    //2 手机号格式是否正确
                    if(etPhone.getText().toString().length()==11){
                        //3 是否勾选同意协议
                        if(ivCheck.isSelected()){
                            httpForgetPswStepThree();
                        }else {
                            ToastUtil.show(activity,"请先同意《用户协议》");
                        }
                    }else {
                        ToastUtil.show(activity,"请检查手机号格式是否正确");
                    }
                }else {
                    ToastUtil.show(activity,"手机号不能为空");
                }
            }
        });
        tvViewSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, WebPageActivity.class);
                intent.putExtra(WebPageActivity.AGM_URL, Route.SUPPORT_BANK_URL);
                startActivity(intent);
            }
        });
    }

    /**
     * 发请求->找回密码第三步->绑定银行卡
     */
    private void httpForgetPswStepThree() {
        PayHttpUtils.getInstance().bindBankCard(cardNo,bankName,etPhone.getText().toString(),token)
                .compose(RxSchedulers.<BaseResponse<CommonBean>>compose())
                .compose(RxSchedulers.<BaseResponse<CommonBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<CommonBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<CommonBean> baseResponse) {
                        if(baseResponse.getData()!=null){
                            if(!TextUtils.isEmpty(baseResponse.getData().getToken())){
                                String newToken = baseResponse.getData().getToken();

                                Bundle bundle = new Bundle();
                                bundle.putString("old_token",token);
                                bundle.putString("card_no",cardNo);
                                bundle.putString("bank_name",bankName);
                                bundle.putString("phone_num",etPhone.getText().toString());
                                bundle.putString("new_token",newToken);
                                bundle.putInt("from",from);
                                Intent intent = new Intent(activity,ForgetPswStepFourActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                ToastUtil.show(context,"验证码已发送，请注意查收!");
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
                if (getIntent().getExtras().containsKey("token")) {
                    token = getIntent().getExtras().getString("token");
                }
                if (getIntent().getExtras().containsKey("card_no")) {
                    cardNo = getIntent().getExtras().getString("card_no");
                }
                if (getIntent().getExtras().containsKey("bank_name")) {
                    bankName = getIntent().getExtras().getString("bank_name");
                    tvCardType.setText(bankName+cardType);
                }
                if (getIntent().getExtras().containsKey("from")) {
                    from = getIntent().getExtras().getInt("from");
                }
            }
        }
    }

}
