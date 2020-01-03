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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hm.cxpay.R;
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
 * @类名：支付密码管理->修改支付密码
 * @Date：2019/11/30
 * @by zjy
 * @备注：
 */
public class ModifyPaywordActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private EditText etOldPassword;//旧密码输入框
    private EditText etPassword;//新密码输入框
    private EditText etConfirmPassword;//确认密码输入框
    private TextView tvSubmit;//确认提交
    private TextView tvSubmitFromForget;//确认提交-来自忘记密码
    private ImageView ivClearPaywordOne;//清除新支付密码
    private ImageView ivClearPaywordTwo;//清除确认支付密码
    private ImageView ivClearPaywordThree;//清除旧支付密码
    private RelativeLayout layoutOldPayWord;//如果是忘记密码，则不显示这一布局
    private Context activity;
    private String finalToken ="";//如果有值，则是忘记密码最后一步携带过来的
    private int from;//从哪里跳转过来的 (1 密码校验 2 密码管理)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_paypsw);
        activity = this;
        initView();
        initData();
    }

    private void getBundle() {
        if(getIntent()!=null){
            if(!TextUtils.isEmpty(getIntent().getStringExtra("final_token"))){
                finalToken = getIntent().getStringExtra("final_token");
                layoutOldPayWord.setVisibility(View.GONE);
                tvSubmit.setVisibility(View.GONE);
                tvSubmitFromForget.setVisibility(View.VISIBLE);//从忘记密码过来的显示另一个确认按钮
            }
        }
        if (getIntent() != null) {
            if (getIntent().getExtras() != null) {
                if (getIntent().getExtras().containsKey("final_token")) {
                        finalToken = getIntent().getExtras().getString("final_token");
                        layoutOldPayWord.setVisibility(View.GONE);
                        tvSubmit.setVisibility(View.GONE);
                        tvSubmitFromForget.setVisibility(View.VISIBLE);//从忘记密码过来的显示另一个确认按钮
                }
                if (getIntent().getExtras().containsKey("from")) {
                    from = getIntent().getExtras().getInt("from");
                }
            }
        }
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        etPassword = findViewById(R.id.et_payword);
        etConfirmPassword = findViewById(R.id.et_confirm_payword);
        etOldPassword = findViewById(R.id.et_old_payword);
        tvSubmit = findViewById(R.id.tv_submit);
        tvSubmitFromForget = findViewById(R.id.tv_submit_from_forget);
        ivClearPaywordOne = findViewById(R.id.iv_clear_payword_one);
        ivClearPaywordTwo = findViewById(R.id.iv_clear_payword_two);
        ivClearPaywordThree = findViewById(R.id.iv_clear_payword_three);
        layoutOldPayWord = findViewById(R.id.layout_old_payword);
        actionbar = headView.getActionbar();

    }

    private void initData() {
        getBundle();
        //密码、确认密码默认隐藏明文
        TransformationMethod method = PasswordTransformationMethod.getInstance();
        etPassword.setTransformationMethod(method);
        etConfirmPassword.setTransformationMethod(method);
        etOldPassword.setTransformationMethod(method);
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
                if (!TextUtils.isEmpty(etPassword.getText().toString()) && !TextUtils.isEmpty(etConfirmPassword.getText().toString())
                        && !TextUtils.isEmpty(etOldPassword.getText().toString())) {
                    //2. 密码必须为6位数字
                    if (etPassword.getText().toString().length() == 6 && etOldPassword.getText().toString().length() == 6) {
                        //3. 密码和确认密码必须一致
                        if (etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
                            httpModifyPayword(etOldPassword.getText().toString(),etPassword.getText().toString(),"");
                        } else {
                            ToastUtil.show(activity, "两次输入的新密码必须一致");
                        }
                    } else {
                        ToastUtil.show(activity, "密码必须为6位数字");
                    }
                } else {
                    ToastUtil.show(activity, "必填项不能为空");
                }
            }
        });
        //确认提交-来自忘记密码
        ClickFilter.onClick(tvSubmitFromForget, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1. 必填不为空
                if (!TextUtils.isEmpty(etPassword.getText().toString()) && !TextUtils.isEmpty(etConfirmPassword.getText().toString())) {
                    //2. 密码必须为6位数字
                    if (etPassword.getText().toString().length() == 6 && etConfirmPassword.getText().toString().length() == 6) {
                        //3. 密码和确认密码必须一致
                        if (etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
                            httpModifyPayword("",etPassword.getText().toString(),finalToken);
                        } else {
                            ToastUtil.show(activity, "两次输入的新密码必须一致");
                        }
                    } else {
                        ToastUtil.show(activity, "密码必须为6位数字");
                    }
                } else {
                    ToastUtil.show(activity, "必填项不能为空");
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
                if (s.toString().length() > 0) {
                    ivClearPaywordOne.setVisibility(View.VISIBLE);
                } else {
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
                if (s.toString().length() > 0) {
                    ivClearPaywordTwo.setVisibility(View.VISIBLE);
                } else {
                    ivClearPaywordTwo.setVisibility(View.INVISIBLE);
                }

            }
        });
        etOldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    ivClearPaywordThree.setVisibility(View.VISIBLE);
                } else {
                    ivClearPaywordThree.setVisibility(View.INVISIBLE);
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
        ivClearPaywordThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etOldPassword.setText("");
                ivClearPaywordThree.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * 发请求->修改支付密码
     */
    private void httpModifyPayword(String oldPayWord,String newPayWord,String token) {
        PayHttpUtils.getInstance().modifyPayword(oldPayWord, newPayWord,token)
                .compose(RxSchedulers.<BaseResponse>compose())
                .compose(RxSchedulers.<BaseResponse>handleResult())
                .subscribe(new FGObserver<BaseResponse>() {
                    @Override
                    public void onHandleSuccess(BaseResponse baseResponse) {
                        ToastUtil.show(context, "新支付密码设置成功!");
                        if(from==CheckPaywordActivity.FROM_CHECK_PAY_WORD){
                            go(CheckPaywordActivity.class);//返回密码校验
                        }else if(from==ManagePaywordActivity.FROM_MANAGE_PAY_WORD){
                            go(ManagePaywordActivity.class);//返回密码管理
                        }else {
                            finish();
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
