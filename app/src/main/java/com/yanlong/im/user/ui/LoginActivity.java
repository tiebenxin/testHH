package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yanlong.im.MainActivity;
import com.yanlong.im.R;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.TokenBean;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.PasswordTextWather;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack4Btn;
import net.cb.cb.library.utils.CheckUtil;
import net.cb.cb.library.utils.InputUtil;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.RunUtils;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.AlertYesNo;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.PopupSelectView;

import retrofit2.Call;
import retrofit2.Response;

import static com.yanlong.im.user.ui.IdentifyingCodeActivity.PHONE;

public class LoginActivity extends AppActivity implements View.OnClickListener {

    private ImageView mImgHead;
    private TextView mTvPhoneNumber;
    private EditText mEtPasswordContent;
    private TextView mTvIdentifyingCode;
    private Button mBtnLogin;
    private TextView mTvForgetPassword;
    private TextView mTvMore;
    private PopupSelectView popupSelectView;
    private String[] strings = {"切换账号", "注册", "取消"};
    private String phone;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initEvent();
        initData();
    }


    private void initView() {
        mImgHead = findViewById(R.id.img_head);
        mTvPhoneNumber = findViewById(R.id.tv_phone_number);
        mEtPasswordContent = findViewById(R.id.et_password_content);
        mTvIdentifyingCode = findViewById(R.id.tv_identifying_code);
        mBtnLogin = findViewById(R.id.btn_login);
        mTvForgetPassword = findViewById(R.id.tv_forget_password);
        mTvMore = findViewById(R.id.tv_more);

    }


    private void initEvent() {
        mTvIdentifyingCode.setOnClickListener(this);
        mTvForgetPassword.setOnClickListener(this);
        mTvMore.setOnClickListener(this);
        mEtPasswordContent.addTextChangedListener(new PasswordTextWather(mEtPasswordContent, this));
        /*ClickFilter.onClick(mBtnLogin, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });*/
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputUtil.hideKeyboard(mEtPasswordContent);
                login();
            }
        });
    }

    private void initData() {
        phone = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.PHONE).get4Json(String.class);
        String imageHead = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.IMAGE_HEAD).get4Json(String.class);

        String imid = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.IM_ID).get4Json(String.class);
        if(StringUtil.isNotNull(imid)){
            phone=imid;
        }
        mTvPhoneNumber.setText(phone);
        Glide.with(this).load(imageHead).apply(GlideOptionsUtil.headImageOptions()).into(mImgHead);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_identifying_code:
                Intent intent = new Intent(this, IdentifyingCodeActivity.class);
                intent.putExtra(PHONE, phone);
                startActivity(intent);
                break;
            case R.id.tv_forget_password:
                Intent forgotPasswordIntent = new Intent(this, ForgotPasswordActivity.class);
                startActivity(forgotPasswordIntent);
                break;
            case R.id.tv_more:
                initPopup();
                break;
        }
    }

    private void initPopup() {
        popupSelectView = new PopupSelectView(this, strings);
        popupSelectView.showAtLocation(mImgHead, Gravity.BOTTOM, 0, 0);
        popupSelectView.setListener(new PopupSelectView.OnClickItemListener() {
            @Override
            public void onItem(String string, int postsion) {
                switch (postsion) {
                    case 0:
                        go(PasswordLoginActivity.class);
                        break;
                    case 1:
                        go(RegisterActivity.class);
                        break;
                }
                popupSelectView.dismiss();
            }
        });
    }


    private void initDialog() {
        AlertYesNo alertYesNo = new AlertYesNo();
        alertYesNo.init(this, "找回密码", "密码错误,找回或重置密码?", "找回密码", "取消", new AlertYesNo.Event() {
            @Override
            public void onON() {

            }

            @Override
            public void onYes() {
                go(ForgotPasswordActivity.class);
            }
        });
        alertYesNo.show();
    }


    private void login() {
        // mBtnLogin.setEnabled(false);
        final String password = mEtPasswordContent.getText().toString();
        final String phone = mTvPhoneNumber.getText().toString();


        if (TextUtils.isEmpty(phone)) {
            ToastUtil.show(this, "请输入账号");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtil.show(this, "请输入密码");
            return;
        }

//        if (!CheckUtil.isMobileNO(phone)) {
//            ToastUtil.show(this, "手机号格式不正确");
//            return;
//        }

        LogUtil.getLog().i("youmeng", "LoginActivity------->getDevId");
        new RunUtils(new RunUtils.Enent() {
            String devId;

            @Override
            public void onRun() {
                devId = UserAction.getDevId(getContext());
            }

            @Override
            public void onMain() {
                UserAction userAction=new UserAction();
                if (CheckUtil.isMobileNO(phone)) {
                    userAction.login(phone, password, devId, new CallBack4Btn<ReturnBean<TokenBean>>(mBtnLogin) {
                        @Override
                        public void onResp(Call<ReturnBean<TokenBean>> call, Response<ReturnBean<TokenBean>> response) {
                            LogUtil.getLog().i("youmeng", "LoginActivity------->login----phone---->onResp");
                            if (response.body() == null) {
                                ToastUtil.show(context, "登录异常");
                                return;
                            }
                            if (response.body().isOk()) {
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(MainActivity.IS_LOGIN, true);
                                startActivity(intent);
                            }
                            if (response.body().getCode().longValue() == 10002) {
                                if (count == 0) {
                                    ToastUtil.show(context, "密码错误");
                                } else {
                                    initDialog();
                                }
                                count += 1;
                            } else {
                                ToastUtil.show(getContext(), response.body().getMsg());
                            }
                        }

                        @Override
                        public void onFail(Call<ReturnBean<TokenBean>> call, Throwable t) {
                            super.onFail(call, t);
                            LogUtil.getLog().i("youmeng", "LoginActivity------->login-------->onFail");
                        }
                    });
                }else {
                    userAction.login4Imid(phone, password, devId, new CallBack4Btn<ReturnBean<TokenBean>>(mBtnLogin) {

                        @Override
                        public void onResp(Call<ReturnBean<TokenBean>> call, Response<ReturnBean<TokenBean>> response) {
                            LogUtil.getLog().i("youmeng", "PasswordLoginActivity------->login--imid-->onResp");
                            if (response.body() == null) {
                                ToastUtil.show(context, "登录异常");
                                return;
                            }
                            if (response.body().isOk()) {
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(MainActivity.IS_LOGIN, true);
                                startActivity(intent);
                            }
                            if (response.body().getCode().longValue() == 10002) {
                                if (count == 0) {
                                    ToastUtil.show(context, "密码错误");
                                } else {
                                    initDialog();
                                }
                                count += 1;
                            } else {
                                ToastUtil.show(getContext(), response.body().getMsg());
                            }
                        }

                        @Override
                        public void onFail(Call<ReturnBean<TokenBean>> call, Throwable t) {
                            super.onFail(call, t);
                            LogUtil.getLog().i("youmeng", "PasswordLoginActivity------->login---->onFail");
                        }
                    });
                }

            }
        }).run();
    }
}

