package com.yanlong.im.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;

import com.example.nim_lib.config.Preferences;
import com.example.nim_lib.ui.BaseBindActivity;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.chat.ui.groupmanager.ExitGroupActivity;
import com.yanlong.im.chat.ui.groupmanager.InactiveMemberActivity;
import com.yanlong.im.chat.ui.groupmanager.NoRedBagActivity;
import com.yanlong.im.chat.ui.groupmanager.NoRedEnvelopesActivity;
import com.yanlong.im.chat.ui.groupmanager.SetupSysManagerActivity;
import com.yanlong.im.databinding.ActivityGroupManageBinding;

import net.cb.cb.library.bean.EventGroupChange;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.IntentUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.ViewUtils;
import net.cb.cb.library.view.ActionbarView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @version V1.0
 * @createAuthor （Shenxin）
 * @createDate 2019-7-9
 * @updateAuthor （Geoff）
 * @updateDate 2019-12-9
 * @description 群管理
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class GroupManageActivity extends BaseBindActivity<ActivityGroupManageBinding> implements View.OnClickListener {

    public static final String AGM_GID = "AGM_GID";
    public static final String IS_ADMIN = "is_admin";
    private MsgAction msgAction;

    private String mGid;
    private Group mGinfo;
    private boolean mIsAdmin = false;// 是否是群主
    private boolean mIsSendRequest = false;// 是否发送请求

    @Override
    protected int setView() {
        return R.layout.activity_group_manage;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        msgAction = new MsgAction();
        mGid = getIntent().getStringExtra(AGM_GID);
        mIsAdmin = getIntent().getBooleanExtra(IS_ADMIN, false);
        if (!mIsAdmin) {
            bindingView.layoutSetupManager.setVisibility(View.GONE);
            bindingView.layoutBottom.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initEvent() {
        bindingView.layoutSetupManager.setOnClickListener(this);
        bindingView.layoutBanRedenvelopes.setOnClickListener(this);
        bindingView.layoutNoRedenvelopes.setOnClickListener(this);
        bindingView.layoutInactiveUser.setOnClickListener(this);
        bindingView.layoutExitGroupUser.setOnClickListener(this);
        bindingView.viewGroupTransfer.setOnClickListener(this);
        bindingView.viewGroupRobot.setOnClickListener(this);
        bindingView.headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });

        bindingView.ckGroupVerif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mIsSendRequest){
                    bindingView.ckGroupVerif.setEnabled(false);
                    taskSetState(mGid, null, null, null, isChecked ? 1 : 0);
                }
            }
        });
        //TODO 群成员相互加好友
        bindingView.ckGroupIntimately.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mIsSendRequest) {
                    bindingView.ckGroupIntimately.setEnabled(false);
                    taskSetIntimatelyState(mGid, isChecked ? 1 : 0);
                }
            }
        });
        bindingView.ckForbiddenWords.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mIsSendRequest) {
                    bindingView.ckForbiddenWords.setEnabled(false);
                    setAllForbiddenWords(mGid, isChecked ? 1 : 0);
                }
            }
        });
    }

    @Override
    protected void loadData() {
        taskGetInfoNetwork();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefreshChat(EventGroupChange event) {
        if (event.isNeedLoad()) {
            taskGetInfoNetwork();
        } else {
            taskGetInfo();
        }
    }

    private void taskGetInfoNetwork() {
        msgAction.groupInfo(mGid, new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                if (response.body().isOk()) {
                    mGinfo = response.body().getData();
                    //群机器人
                    String rname = mGinfo.getRobotname();
                    rname = StringUtil.isNotNull(rname) ? rname : "未配置";
                    bindingView.txtGroupRobot.setText(rname);
                    //群验证
                    bindingView.ckGroupVerif.setChecked(mGinfo.getNeedVerification() == 1);
                    bindingView.ckGroupIntimately.setChecked(mGinfo.getContactIntimately() == 1);
                    bindingView.ckForbiddenWords.setChecked(mGinfo.getWordsNotAllowed() == 1);
                }
                if(!isFinishing()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsSendRequest = true;
                        }
                    },300);
                }
            }

            @Override
            public void onFailure(Call<ReturnBean<Group>> call, Throwable t) {
                super.onFailure(call, t);
                if(!isFinishing()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsSendRequest = true;
                        }
                    },300);
                }
            }
        });
    }

    private void taskGetInfo() {
        msgAction.groupInfo4Db(mGid, new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                if (response.body().isOk()) {
                    mGinfo = response.body().getData();
                    //群机器人
                    String rname = mGinfo.getRobotname();
                    rname = StringUtil.isNotNull(rname) ? rname : "未配置";
                    bindingView.txtGroupRobot.setText(rname);
                    bindingView.viewGroupRobot.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), GroupRobotActivity.class)
                                    .putExtra(GroupRobotActivity.AGM_GID, mGinfo.getGid())
                                    .putExtra(GroupRobotActivity.AGM_RID, mGinfo.getRobotid()));
                        }
                    });
                    //群验证
                    bindingView.ckGroupVerif.setChecked(mGinfo.getNeedVerification() == 1);
                    bindingView.ckGroupIntimately.setChecked(mGinfo.getContactIntimately() == 1);
                    bindingView.ckForbiddenWords.setChecked(mGinfo.getWordsNotAllowed() == 1);
                }
            }
        });
    }

    private void taskSetState(String gid, Integer isTop, Integer notNotify, Integer saved, Integer needVerification) {
        msgAction.groupSwitch(gid, isTop, notNotify, saved, needVerification, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                bindingView.ckGroupVerif.setEnabled(true);
                if (response.body() == null)
                    return;
                if (!TextUtils.isEmpty(response.body().getMsg())) {
                    ToastUtil.show(getContext(), response.body().getMsg());
                }
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                super.onFailure(call, t);
                bindingView.ckGroupVerif.setEnabled(true);
            }
        });
    }

    private void taskSetIntimatelyState(String gid, int i) {
        msgAction.groupSwitchIntimately(gid, i, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                bindingView.ckGroupIntimately.setEnabled(true);
                if (response.body() == null)
                    return;
                if (!TextUtils.isEmpty(response.body().getMsg())) {
                    ToastUtil.show(getContext(), response.body().getMsg());
                }
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                super.onFailure(call, t);
                bindingView.ckGroupIntimately.setEnabled(true);
            }
        });
    }

    /**
     * 全员禁言
     * @param gid
     * @param i
     */
    private void setAllForbiddenWords(String gid, int i) {
        msgAction.setAllForbiddenWords(gid, i, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                bindingView.ckForbiddenWords.setEnabled(true);
                if (response.body() == null){
                    ToastUtil.show(getContext(), "全员禁言开启失败");
                }else if (!TextUtils.isEmpty(response.body().getMsg())) {
                    ToastUtil.show(getContext(), response.body().getMsg());
                }
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                super.onFailure(call, t);
                bindingView.ckForbiddenWords.setEnabled(true);
            }
        });
    }

    private void changeMaster(String gid, String uid, String membername) {
        msgAction.changeMaster(gid, uid, membername, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    ToastUtil.show(context, "转让失败");
                    return;
                }
                ToastUtil.show(context, response.body().getMsg());
                if (response.body().isOk()) {
                    MessageManager.getInstance().notifyGroupChange(true);
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GroupSelectUserActivity.RET_CODE_SELECTUSR) {
            if (data == null)
                return;
            String uid = data.getStringExtra(GroupSelectUserActivity.UID);
            if (StringUtil.isNotNull(uid)) {
                String membername = data.getStringExtra(GroupSelectUserActivity.MEMBERNAME);
                changeMaster(mGid, uid, membername);
            }
        }
    }

    /**
     * 获取群主信息
     *
     * @return
     */
    private MemberUser getAdminMemberUser() {
        MemberUser memberUser = null;
        if (mGinfo != null && mGinfo.getUsers() != null && mGinfo.getUsers().size() > 0) {
            for (MemberUser bean : mGinfo.getUsers()) {
                if (mGinfo.getMaster().equals(bean.getUid() + "")) {
                    memberUser = bean;
                    break;
                }
            }
        }
        return memberUser;
    }

    @Override
    public void onClick(View v) {
        if (ViewUtils.isFastDoubleClick()) {
            return;
        }
        Intent intent;
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.layout_setup_manager:// 设置管理员
                bundle.putString(GroupSelectUserActivity.GID, mGid);
                MemberUser memberUser = getAdminMemberUser();
                if (memberUser != null) {
                    bundle.putString(Preferences.HEAD_SCULPTURE, memberUser.getHead());
                    bundle.putString(Preferences.USER_NAME, memberUser.getShowName());
                }
                IntentUtil.gotoActivity(this, SetupSysManagerActivity.class, bundle);
                break;
            case R.id.layout_ban_redenvelopes:// 禁止领取零钱红包
                bundle.putString(GroupSelectUserActivity.GID, mGid);
                IntentUtil.gotoActivity(this, NoRedEnvelopesActivity.class, bundle);
                break;
            case R.id.layout_no_redenvelopes:// 未领取红包列表
                bundle.putString(GroupSelectUserActivity.GID, mGid);
                IntentUtil.gotoActivity(this, NoRedBagActivity.class, bundle);
                break;
            case R.id.layout_inactive_user:// 不活跃群成员
                bundle.putString(GroupSelectUserActivity.GID, mGid);
                IntentUtil.gotoActivity(this, InactiveMemberActivity.class, bundle);
                break;
            case R.id.layout_exit_group_user:// 退群成员列表
                bundle.putString(GroupSelectUserActivity.GID, mGid);
                IntentUtil.gotoActivity(this, ExitGroupActivity.class, bundle);
                break;
            case R.id.view_group_transfer:// 群主管理权转让
                intent = new Intent(this, GroupSelectUserActivity.class);
                intent.putExtra(GroupSelectUserActivity.GID, mGid);
                intent.putExtra(GroupSelectUserActivity.TYPE, 0);
                startActivityForResult(intent, GroupSelectUserActivity.RET_CODE_SELECTUSR);
                break;
            case R.id.view_group_robot:// 群转让
                if (mGinfo != null) {
                    startActivity(new Intent(this, GroupRobotActivity.class)
                            .putExtra(GroupRobotActivity.AGM_GID, mGinfo.getGid())
                            .putExtra(GroupRobotActivity.AGM_RID, mGinfo.getRobotid()));
                }
                break;
        }
    }
}
