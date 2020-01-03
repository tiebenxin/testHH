package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.yanlong.im.R;
import com.yanlong.im.chat.ui.NoticeActivity;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.TokenBean;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.CheckUtil;
import net.cb.cb.library.utils.ClickFilter;
import net.cb.cb.library.utils.CountDownUtil;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.RunUtils;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.ClearEditText;
import net.cb.cb.library.view.HeadView;
import net.cb.cb.library.view.WebPageActivity;

import retrofit2.Call;
import retrofit2.Response;

public class RegisterActivity extends AppActivity implements View.OnClickListener {

    private ClearEditText mEtPhoneContent;
    private EditText mEtIdentifyingCodeContent;
    private Button mBtnRegister;
    private TextView mTvMattersNeedAttention;
    private TextView mTvGetVerificationCode;
    private HeadView mHeadView;
    private UserAction userAction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CountDownUtil.cancelTimer();
    }

    private void initView() {
        mHeadView = findViewById(R.id.headView);
        mEtPhoneContent = findViewById(R.id.et_phone_content);
        mEtIdentifyingCodeContent = findViewById(R.id.et_identifying_code_content);
        mBtnRegister = findViewById(R.id.btn_register);
        mTvMattersNeedAttention = findViewById(R.id.tv_matters_need_attention);
        mTvGetVerificationCode = findViewById(R.id.tv_get_verification_code);
        initTvMNA();
    }

    private void initEvent() {
        mTvMattersNeedAttention.setOnClickListener(this);
        mTvGetVerificationCode.setOnClickListener(this);
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        ClickFilter.onClick(mBtnRegister, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void initData() {
        userAction = new UserAction();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_get_verification_code:
                initCountDownUtil();
                break;
            case R.id.tv_matters_need_attention:
                startActivity(new Intent(RegisterActivity.this, NoticeActivity.class));
                break;
        }
    }


    private void initTvMNA() {
        final SpannableStringBuilder style = new SpannableStringBuilder();
        style.append("点击\"注册\"即表示已阅读并同意《用户使用协议》和《隐私权政策》");
        ClickableSpan clickProtocol = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(RegisterActivity.this,WebPageActivity.class);
                intent.putExtra(WebPageActivity.AGM_URL,"https://changxin.zhixun6.com/yhxy.html");
                startActivity(intent);
            }
        };
        style.setSpan(clickProtocol, 15, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue_600));
        style.setSpan(protocolColorSpan, 15, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan clickPolicy = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(RegisterActivity.this,WebPageActivity.class);
                intent.putExtra(WebPageActivity.AGM_URL,"https://changxin.zhixun6.com/yszc.html");
                startActivity(intent);

            }
        };
        style.setSpan(clickPolicy, 24, 31, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ForegroundColorSpan policyColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue_600));
        style.setSpan(policyColorSpan, 24, 31, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTvMattersNeedAttention.setText(style);
        mTvMattersNeedAttention.setMovementMethod(LinkMovementMethod.getInstance());
    }


    private void initCountDownUtil() {
        final String phone = mEtPhoneContent.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            ToastUtil.show(RegisterActivity.this, "请填写手机号码");
            return;
        }
        if(!CheckUtil.isMobileNO(phone)){
            ToastUtil.show(this, "手机号格式不正确");
            return;
        }

        CountDownUtil.getTimer(60, mTvGetVerificationCode, "发送验证码", this, new CountDownUtil.CallTask() {
            @Override
            public void task() {
                taskGetSms(phone);
            }
        });

    }

    private void register() {
        String phone = mEtPhoneContent.getText().toString();
        String code = mEtIdentifyingCodeContent.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            ToastUtil.show(this, "请输入手机号");
            return;
        }
        if (TextUtils.isEmpty(code)) {
            ToastUtil.show(this, "请输入验证码");
            return;
        }
        if(!CheckUtil.isMobileNO(phone)){
            ToastUtil.show(this, "手机号格式不正确");
            return;
        }
        taskRegister(phone, code);
    }


    private void taskGetSms(String phone) {
        userAction.smsCaptchaGet(phone, "register", new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                ToastUtil.show(RegisterActivity.this, response.body().getMsg());
            }
        });
    }

    private void taskRegister(final String phone, final String captcha) {
        LogUtil.getLog().i("youmeng","RegisterActivity------->getDevId");
        new RunUtils(new RunUtils.Enent() {
            String devId;
            @Override
            public void onRun() {
                devId= UserAction.getDevId(getContext());
            }

            @Override
            public void onMain() {
                userAction.register(phone, captcha, devId, new CallBack<ReturnBean<TokenBean>>() {
                    @Override
                    public void onResponse(Call<ReturnBean<TokenBean>> call, Response<ReturnBean<TokenBean>> response) {
                        LogUtil.getLog().i("youmeng","RegisterActivity------->taskRegister----->onResponse");
                        if (response.body() == null) {
                            return;
                        }
                        ToastUtil.show(RegisterActivity.this, response.body().getMsg());
                        if (response.body().isOk()) {
                            SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.FIRST_TIME);
                            preferencesUtil.save2Json(true);

                            Intent intent = new Intent(getContext(), RegisterUserNameActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ReturnBean<TokenBean>> call, Throwable t) {
                        super.onFailure(call, t);
                        LogUtil.getLog().i("youmeng","RegisterActivity------->taskRegister----->onFailure");
                    }
                });

            }
        }).run();

    }


}
