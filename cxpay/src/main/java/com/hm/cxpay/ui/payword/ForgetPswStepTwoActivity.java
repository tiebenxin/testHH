package com.hm.cxpay.ui.payword;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hm.cxpay.R;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.net.Route;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.bean.BankInfo;

import net.cb.cb.library.utils.ClickFilter;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;
import net.cb.cb.library.view.WebPageActivity;

/**
 * @类名：忘记密码->第二步->填写银行卡信息
 * @Date：2019/12/12
 * @by zjy
 * @备注：
 */
public class ForgetPswStepTwoActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private TextView tvName;
    private EditText etBankCard;
    private TextView tvSubmit;
    private TextView tvViewSupport;

    private Activity activity;
    private String token;//得到认证需要的token
    private int from;//从哪里跳转过来的 (1 密码校验 2 密码管理)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_psw_step_two);
        activity = this;
        initView();
        initData();
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        tvName = findViewById(R.id.tv_name);
        etBankCard = findViewById(R.id.et_bankcard);
        tvSubmit = findViewById(R.id.tv_submit);
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
        ClickFilter.onClick(tvSubmit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //银行卡号不为空
                if(!TextUtils.isEmpty(etBankCard.getText().toString())){
                    httpCheckBankCard(etBankCard.getText().toString());
                }else {
                    ToastUtil.show(activity,"银行卡号不能为空");
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
     * 发请求->绑定银行卡检查
     */
    public void httpCheckBankCard(String bankCardNo) {
        PayHttpUtils.getInstance().checkBankCard(bankCardNo)
                .compose(RxSchedulers.<BaseResponse<BankInfo>>compose())
                .compose(RxSchedulers.<BaseResponse<BankInfo>>handleResult())
                .subscribe(new FGObserver<BaseResponse<BankInfo>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<BankInfo> baseResponse) {
                            BankInfo info = baseResponse.getData();
                            if (info != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString("token",token);
                                bundle.putString("card_no",etBankCard.getText().toString());
                                bundle.putString("bank_name",info.getBankName());
                                bundle.putInt("from",from);
                                Intent intent = new Intent(activity,ForgetPswStepThreeActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
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
                if (getIntent().getExtras().containsKey("name")) {
                    tvName.setText(getIntent().getExtras().getString("name"));
                }
                if (getIntent().getExtras().containsKey("from")) {
                    from = getIntent().getExtras().getInt("from");
                }
            }
        }
    }


}
