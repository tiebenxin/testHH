package com.hm.cxpay.ui.payword;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hm.cxpay.R;
import com.hm.cxpay.global.PayEnvironment;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.ui.LooseChangeActivity;

import net.cb.cb.library.utils.ClickFilter;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

/**
 * @类名：零钱->设置支付密码
 * @Date：2019/11/30
 * @by zjy
 * @备注：
 */
public class SetPaywordActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private EditText etPassword;//密码输入框
    private EditText etConfirmPassword;//确认密码输入框
    private TextView tvSubmit;//确认提交
    private ImageView ivClearPaywordOne;//清除支付密码
    private ImageView ivClearPaywordTwo;//清除确认支付密码
    private Context activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_paypsw);
        activity = this;
        initView();
        initData();
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        etPassword = findViewById(R.id.et_payword);
        etConfirmPassword = findViewById(R.id.et_confirm_payword);
        tvSubmit = findViewById(R.id.tv_submit);
        ivClearPaywordOne = findViewById(R.id.iv_clear_payword_one);
        ivClearPaywordTwo = findViewById(R.id.iv_clear_payword_two);
        actionbar = headView.getActionbar();

    }

    private void initData() {
        //密码、确认密码默认隐藏明文
        TransformationMethod method =  PasswordTransformationMethod.getInstance();
        etPassword.setTransformationMethod(method);
        etConfirmPassword.setTransformationMethod(method);
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        //确认提交
        ClickFilter.onClick(tvSubmit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1. 必填不为空
                if (!TextUtils.isEmpty(etPassword.getText().toString()) && !TextUtils.isEmpty(etConfirmPassword.getText().toString())) {
                    //2. 密码必须为6位数字
                    if (etPassword.getText().toString().length() == 6) {
                        //3. 密码和确认密码必须一致
                        if (etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
                            httpSetPayword();
                        } else {
                            ToastUtil.show(activity,"两次输入密码必须一致");
                        }
                    } else {
                        ToastUtil.show(activity,"密码必须为6位数字");
                    }
                } else {
                    ToastUtil.show(activity,"必填项不能为空");
                }
            }
        });
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0){
                    ivClearPaywordOne.setVisibility(View.VISIBLE);
                }else {
                    ivClearPaywordOne.setVisibility(View.INVISIBLE);
                }

            }
        });
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0){
                    ivClearPaywordTwo.setVisibility(View.VISIBLE);
                }else {
                    ivClearPaywordTwo.setVisibility(View.INVISIBLE);
                }

            }
        });
        ivClearPaywordOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPassword.setText("");
                ivClearPaywordOne.setVisibility(View.INVISIBLE);
            }
        });
        ivClearPaywordTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etConfirmPassword.setText("");
                ivClearPaywordTwo.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * 发请求->设置支付密码
     */
    private void httpSetPayword(){
        PayHttpUtils.getInstance().setPayword(etPassword.getText().toString())
                .compose(RxSchedulers.<BaseResponse>compose())
                .compose(RxSchedulers.<BaseResponse>handleResult())
                .subscribe(new FGObserver<BaseResponse>() {
                    @Override
                    public void onHandleSuccess(BaseResponse baseResponse) {
                        if(baseResponse.isSuccess()){
                            ToastUtil.show(activity, "设置成功!");
                            PayEnvironment.getInstance().getUser().setPayPwdStat(1);
                            go(LooseChangeActivity.class);
                            finish();
                        }else {
                            ToastUtil.show(context, baseResponse.getMessage());
                        }

                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        ToastUtil.show(context, baseResponse.getMessage());
                    }
                });
    }
}
