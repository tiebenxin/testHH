package com.yanlong.im.chat.ui.groupmanager;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.CompoundButton;

import com.example.nim_lib.config.Preferences;
import com.example.nim_lib.ui.BaseBindActivity;
import com.google.gson.Gson;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.SingleMeberInfoBean;
import com.yanlong.im.databinding.ActivityGroupMemPowerSetBinding;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.utils.DestroyTimeView;
import com.yanlong.im.utils.ForbiddenWordsView;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-25
 * @updateAuthor
 * @updateDate
 * @description 群成员权限设置
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class GroupMemPowerSetActivity extends BaseBindActivity<ActivityGroupMemPowerSetBinding> {

    private int mSelectPostion = 0;
    private String mGid, mUidJson = "";
    private long mUid;
    private MsgAction mMsgAction;
    private UserAction mUserAction;
    private List<Long> mList;
    private boolean mIsFirst = true;

    @Override
    protected int setView() {
        return R.layout.activity_group_mem_power_set;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void initEvent() {
        setActionBarLeft(bindingView.headView);
        bindingView.layoutSetup.setOnClickListener(o -> {
            ForbiddenWordsView destroyTimeView = new ForbiddenWordsView(GroupMemPowerSetActivity.this);
            destroyTimeView.initView();
            destroyTimeView.setPostion(mSelectPostion);
            destroyTimeView.setListener(new DestroyTimeView.OnClickItem() {
                @Override
                public void onClickItem(String content, int survivaltime) {
                    if (mSelectPostion != survivaltime) {
                        toggleWordsNotAllowed(survivaltime, content);
                    }
                }
            });
        });
        bindingView.ckBanMoney.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mIsFirst) {
                    toggleOpenUpRedEnvelope(isChecked);
                }
            }
        });
    }

    @Override
    protected void loadData() {
        mGid = getIntent().getStringExtra(Preferences.TOGID);
        mUid = getIntent().getLongExtra(Preferences.TOUID, 0);
        mList = new ArrayList<>();
        mList.add(mUid);
        mUidJson = new Gson().toJson(mList);
        mMsgAction = new MsgAction();
        mUserAction = new UserAction();
        getSingleMemberInfo();
    }

    /**
     * 开关群成员禁言
     *
     * @param duration 禁言时间以秒为单位
     */
    private void toggleWordsNotAllowed(int duration, String content) {

        mMsgAction.toggleWordsNotAllowed(mUidJson, mGid, duration, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                if (response.body() == null || response.body().isOk()) {
                    mSelectPostion = duration;
                    bindingView.txtTime.setText(content);
                } else if (!TextUtils.isEmpty(response.body().getMsg())) {
                    ToastUtil.show(GroupMemPowerSetActivity.this, response.body().getMsg());
                }
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    /**
     * 开关群成员禁领红包
     *
     * @param isAdd 增加还是移除
     */
    private void toggleOpenUpRedEnvelope(boolean isAdd) {
        mMsgAction.toggleOpenUpRedEnvelope(mUidJson, mGid, isAdd ? 1 : -1, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                if (response.body() == null || !response.body().isOk()) {
                    if (isAdd) {
                        ToastUtil.show(GroupMemPowerSetActivity.this, "禁止领取零钱红包失败");
                    } else {
                        ToastUtil.show(GroupMemPowerSetActivity.this, "移除失败");
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    /**
     * 获取单个群成员信息
     */
    private void getSingleMemberInfo() {
        mUserAction.getSingleMemberInfo(mGid, Integer.parseInt(mUid + ""), new CallBack<ReturnBean<SingleMeberInfoBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<SingleMeberInfoBean>> call, Response<ReturnBean<SingleMeberInfoBean>> response) {
                super.onResponse(call, response);
                if (response != null && response.body().isOk()) {
                    SingleMeberInfoBean singleMeberInfoBean = response.body().getData();
                    mSelectPostion = singleMeberInfoBean.getShutUpDuration();
                    boolean value = singleMeberInfoBean.isCantOpenUpRedEnv();
                    bindingView.ckBanMoney.setChecked(value);
                    bindingView.txtTime.setText(getSurvivaltime(mSelectPostion));

                }
                if (!isFinishing()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsFirst = false;
                        }
                    }, 300);
                }
            }

            @Override
            public void onFailure(Call<ReturnBean<SingleMeberInfoBean>> call, Throwable t) {
                super.onFailure(call, t);
                mIsFirst = false;
            }
        });
    }

    public static String getSurvivaltime(int time) {
        String value = "无";
        switch (time) {
            case 0:
                value = "无";
                break;
            case 300:
                value = "5分钟";
                break;
            case 900:
                value = "15分钟";
                break;
            case 3600:
                value = "1小时";
                break;
            case 43200:
                value = "12小时";
                break;
            case 86400:
                value = "1天";
                break;
            case 259200:
                value = "3天";
                break;
        }
        return value;
    }
}
