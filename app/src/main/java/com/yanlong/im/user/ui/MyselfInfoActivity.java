package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yanlong.im.R;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.EventMyUserInfo;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Response;

public class MyselfInfoActivity extends AppActivity implements View.OnClickListener {
    private static final int NICENAME = 1000;
    private static final int PRODUCT = 2000;
    private static final int SEX = 3000;
    private static final int IMAGE_HEAD = 4000;
    private static final int IDENTITY = 5000;

    private ImageView mImgHead;
    private LinearLayout mViewBlacklist;
    private TextView mTvPhone;
    private LinearLayout mViewNickname;
    private TextView mTvNickname;
    private LinearLayout mViewProductNumber;
    private TextView mTvProductNumber;
    private LinearLayout mViewSex;
    private TextView mTvSex;
    private LinearLayout mViewIdentity;
    private TextView mTvIdentity;
    private LinearLayout mViewHead;
    private HeadView mHeadView;
    private UserInfo userInfo;
    private UserAction userAction;
    private ImageView mIvProductNumber;
    private int sex;
    private String imageHead;
    private String imid;
    private String nickName;
    private String oldImid;
    private int authStat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myself_info);
        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        mImgHead = findViewById(R.id.img_head);
        mViewBlacklist = findViewById(R.id.view_blacklist);
        mViewHead = findViewById(R.id.view_head);
        mTvPhone = findViewById(R.id.tv_phone);
        mViewNickname = findViewById(R.id.view_nickname);
        mTvNickname = findViewById(R.id.tv_nickname);
        mViewProductNumber = findViewById(R.id.view_product_number);
        mTvProductNumber = findViewById(R.id.tv_product_number);
        mViewSex = findViewById(R.id.view_sex);
        mTvSex = findViewById(R.id.tv_sex);
        mViewIdentity = findViewById(R.id.view_identity);
        mTvIdentity = findViewById(R.id.tv_identity);
        mHeadView = findViewById(R.id.headView);
        mIvProductNumber = findViewById(R.id.iv_product_number);

        EventBus.getDefault().register(this);
    }


    private void initEvent() {
        mViewNickname.setOnClickListener(this);
        mViewProductNumber.setOnClickListener(this);
        mViewSex.setOnClickListener(this);
        mViewIdentity.setOnClickListener(this);
        mViewHead.setOnClickListener(this);
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
    }

    private void initData() {
        userAction = new UserAction();
        userInfo = UserAction.getMyInfo();
        imageHead = userInfo.getHead();
        mTvPhone.setText(userInfo.getPhone() + "");
        oldImid = userInfo.getOldimid();
        imid = userInfo.getImid();
        nickName = userInfo.getName();
        sex = userInfo.getSex();
        authStat = userInfo.getAuthStat();
        Glide.with(this).load(imageHead)
                .apply(GlideOptionsUtil.headImageOptions()).into(mImgHead);
        mTvNickname.setText(nickName);
        if (!oldImid.equals(imid)) {
            mTvProductNumber.setText(imid);
            mIvProductNumber.setVisibility(View.GONE);
            mViewProductNumber.setClickable(false);

        } else {
            mTvProductNumber.setText("未设置");
            mIvProductNumber.setVisibility(View.VISIBLE);
            mViewProductNumber.setClickable(true);
        }
        switch (sex) {
            case 1:
                mTvSex.setText("男");
                break;
            case 2:
                mTvSex.setText("女");
                break;
            default:
                mTvSex.setText("未知");
                break;
        }
        switch (authStat) {
            case 0:
                mTvIdentity.setText("未认证");
                break;
            case 1:
                mTvIdentity.setText("已认证");
                break;
            case 2:
                mTvIdentity.setText("已认证");
                break;
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_nickname:
                Intent nicknameIntent = new Intent(MyselfInfoActivity.this, CommonSetingActivity.class);
                nicknameIntent.putExtra(CommonSetingActivity.TITLE, "昵称");
                nicknameIntent.putExtra(CommonSetingActivity.REMMARK, "设置昵称");
                nicknameIntent.putExtra(CommonSetingActivity.HINT, "昵称");
                nicknameIntent.putExtra(CommonSetingActivity.SIZE,16);
                nicknameIntent.putExtra(CommonSetingActivity.SETING,nickName);
                startActivityForResult(nicknameIntent, NICENAME);
                break;
            case R.id.view_product_number:
                Intent productIntent = new Intent(MyselfInfoActivity.this, CommonSetingActivity.class);
                productIntent.putExtra(CommonSetingActivity.TITLE, "常信号");
                productIntent.putExtra(CommonSetingActivity.REMMARK, "常信号");
                productIntent.putExtra(CommonSetingActivity.HINT, "可以使用6~16个字符 数字(必须以字母开头)");
                productIntent.putExtra(CommonSetingActivity.REMMARK1, "常信号只能设置一次");
                productIntent.putExtra(CommonSetingActivity.SIZE,16);
                productIntent.putExtra(CommonSetingActivity.SPECIAL,1);
                startActivityForResult(productIntent, PRODUCT);
                break;
            case R.id.view_sex:
                Intent sexIntent = new Intent(MyselfInfoActivity.this, SelectSexActivity.class);
                sexIntent.putExtra(SelectSexActivity.SEX, sex);
                startActivityForResult(sexIntent, SEX);
                break;
            case R.id.view_identity:
//                if (authStat == 0) {
//                    Intent identityIntent = new Intent(MyselfInfoActivity.this, IdentityAttestationActitiy.class);
//                    startActivityForResult(identityIntent, IDENTITY);
//
//                } else {
//                    Intent identityIntent = new Intent(MyselfInfoActivity.this, IdentificationUserActivity.class);
//                    startActivity(identityIntent);
//                }
                break;
            case R.id.view_head:
                Intent headIntent = new Intent(MyselfInfoActivity.this, ImageHeadActivity.class);
                headIntent.putExtra(ImageHeadActivity.IMAGE_HEAD, imageHead);
                startActivityForResult(headIntent, IMAGE_HEAD);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String content = data.getStringExtra(CommonSetingActivity.CONTENT);
            switch (requestCode) {
                case NICENAME:
                    taskUserInfoSet(null, null, content, null);
                    break;
                case SEX:
                    int contentSex;
                    if (content.equals("男")) {
                        contentSex = 1;
                    } else {
                        contentSex = 2;
                    }
                    taskUserInfoSet(null, null, null, contentSex);
                    break;
                case PRODUCT:
                    taskUserInfoSet(content, null, null, null);
                    break;
                case IMAGE_HEAD:
                    if (!TextUtils.isEmpty(content)) {
                        imageHead = content;
                    }
                    break;
                case IDENTITY:
                    mTvIdentity.setText("已认证");
                    break;
            }

        }
    }


    private void taskUserInfoSet(final String imid, final String avatar, final String nickname, final Integer gender) {

        userAction.myInfoSet(imid, avatar, nickname, gender, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                if (!TextUtils.isEmpty(imid)) {
                    MyselfInfoActivity.this.imid = imid;
                    mTvProductNumber.setText(imid);
                    mIvProductNumber.setVisibility(View.GONE);
                    mViewProductNumber.setClickable(false);
                }

                if (!TextUtils.isEmpty(nickname)) {
                    MyselfInfoActivity.this.nickName = nickname;
                    mTvNickname.setText(nickname);
                }
                if (gender != null) {
                    MyselfInfoActivity.this.sex = gender;
                    if (gender == 1) {
                        sex = 1;
                        mTvSex.setText("男");
                    } else if (gender == 2) {
                        sex = 2;
                        mTvSex.setText("女");
                    } else {
                        mTvSex.setText("未知");
                    }
                }
                ToastUtil.show(MyselfInfoActivity.this, response.body().getMsg());
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMyUserInfo event) {
        if (event.type == 1) {
            UserInfo userInfo = event.getUserInfo();
            imageHead = userInfo.getHead();
            Glide.with(this).load(userInfo.getHead())
                    .apply(GlideOptionsUtil.headImageOptions()).into(mImgHead);
        }
    }


}






















