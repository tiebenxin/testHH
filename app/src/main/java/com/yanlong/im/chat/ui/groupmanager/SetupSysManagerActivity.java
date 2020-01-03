package com.yanlong.im.chat.ui.groupmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.nim_lib.config.Preferences;
import com.example.nim_lib.ui.BaseBindActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yanlong.im.R;
import com.yanlong.im.adapter.CommonRecyclerViewAdapter;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.ui.GroupSelectUserActivity;
import com.yanlong.im.databinding.ActivitySetupSysManagerBinding;
import com.yanlong.im.databinding.ItemSetupManagerBinding;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.bean.EventGroupChange;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.IntentUtil;
import net.cb.cb.library.utils.NetUtil;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.ViewUtils;
import net.cb.cb.library.view.AlertYesNo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-09`
 * @updateAuthor
 * @updateDate
 * @description 设置管理员
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class SetupSysManagerActivity extends BaseBindActivity<ActivitySetupSysManagerBinding> {

    public static final int REUQEST_CODE_100 = 100;

    private CommonRecyclerViewAdapter<UserInfo, ItemSetupManagerBinding> mViewAdapter;
    private List<UserInfo> mList = new ArrayList<>();
    private String mGid = "", mHeadSculpture = "", mUserName = "";
    private List<Long> mAdmins = new ArrayList<>();
    private MsgAction mMsgAction;
    private Gson mGosn = new Gson();
    private Group mGroupInfo;
    private UserInfo mAdminsUser;

    @Override
    protected int setView() {
        return R.layout.activity_setup_sys_manager;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mViewAdapter = new CommonRecyclerViewAdapter<UserInfo, ItemSetupManagerBinding>(this, R.layout.item_setup_manager) {

            @Override
            public void bind(ItemSetupManagerBinding binding, UserInfo userInfo,
                             int position, RecyclerView.ViewHolder viewHolder) {
                Glide.with(SetupSysManagerActivity.this).load(userInfo.getHead())
                        .apply(GlideOptionsUtil.headImageOptions()).into(binding.imgHead);
                binding.txtName.setText(userInfo.getName4Show());
                if (position == 0) {
                    binding.txtType.setVisibility(View.VISIBLE);
                    binding.txtType.setText("群主");
                    binding.txtCancleManager.setVisibility(View.GONE);
                    binding.viewLine.setVisibility(View.GONE);
                } else {
                    binding.txtCancleManager.setVisibility(View.VISIBLE);
                    if (position == 1) {
                        binding.txtType.setVisibility(View.VISIBLE);
                        binding.txtType.setText("管理员");
                    } else {
                        binding.txtType.setVisibility(View.GONE);
                    }
                    if (position == mList.size() - 1) {
                        binding.viewLine.setVisibility(View.GONE);
                    } else {
                        binding.viewLine.setVisibility(View.VISIBLE);
                    }
                }
                binding.txtCancleManager.setOnClickListener(o -> {
                    if (ViewUtils.isFastDoubleClick()) {
                        return;
                    }
                    if (!checkNetConnectStatus()) {
                        return;
                    }
                    showDialog(userInfo.getName4Show(), position, userInfo.getUid());
                });
            }
        };

        bindingView.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindingView.recyclerView.setAdapter(mViewAdapter);
    }

    @Override
    protected void initEvent() {
        setActionBarLeft(bindingView.headView);
        bindingView.txtAddManager.setOnClickListener(o -> {
            if (ViewUtils.isFastDoubleClick()) {
                return;
            }
            if (!checkNetConnectStatus()) {
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putInt(GroupSelectUserActivity.TYPE, 2);
            bundle.putString(GroupSelectUserActivity.GID, mGid);
            mAdmins.clear();
            if (mList != null && mList.size() > 0) {
                for (UserInfo userInfo : mList) {
                    if (userInfo.getuType() != 1) {
                        mAdmins.add(userInfo.getUid());
                    }
                }
                bundle.putString(GroupSelectUserActivity.UIDS, mGosn.toJson(mAdmins));
            }
            IntentUtil.gotoActivityForResult(SetupSysManagerActivity.this, GroupSelectUserActivity.class, bundle, REUQEST_CODE_100);
        });
    }

    @Override
    protected void loadData() {
        mGid = getIntent().getStringExtra(GroupSelectUserActivity.GID);
        // 群主信息
        mHeadSculpture = getIntent().getStringExtra(Preferences.HEAD_SCULPTURE);
        mUserName = getIntent().getStringExtra(Preferences.USER_NAME);
        mAdminsUser = new UserInfo();
        mAdminsUser.setHead(mHeadSculpture);
        mAdminsUser.setName(mUserName);
        mAdminsUser.setuType(1);
        mList.add(0, mAdminsUser);

        mViewAdapter.setData(mList);
        mMsgAction = new MsgAction();

        requestGroupInfo();
    }

    private void showDialog(String name, final int position, long gid) {
        AlertYesNo dialog = new AlertYesNo();
        dialog.init(this, getString(R.string.dialog_message_title), "确定取消\"" + name + "\"管理员", getString(R.string.dialog_btn_confrim),
                getString(R.string.dialog_btn_cancle), new AlertYesNo.Event() {
                    @Override
                    public void onON() {

                    }

                    @Override
                    public void onYes() {
                        mAdmins.clear();
                        mAdmins.add(mList.get(position).getUid());
                        requestAddManagerUser(mGosn.toJson(mAdmins), null, mList.get(position), false);
                    }
                });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REUQEST_CODE_100) {
                String value = data.getStringExtra(Preferences.DATA);
                List<UserInfo> list = new Gson().fromJson(value, new TypeToken<List<UserInfo>>() {
                }.getType());
                if (list != null && list.size() > 0) {
                    mAdmins.clear();
                    for (UserInfo userInfo : list) {
                        mAdmins.add(userInfo.getUid());
                    }
                    requestAddManagerUser(mGosn.toJson(mAdmins), list, null, true);
                }
            }
        }
    }

    /**
     * 增删管理员
     *
     * @param adminsJson 管理员列表
     * @param list       添加管理列表
     * @param userInfo   移除用户
     * @param isAdd      增加还是取消
     */
    private void requestAddManagerUser(String adminsJson, List<UserInfo> list, UserInfo userInfo, boolean isAdd) {
        mMsgAction.groupChangeAdmins(adminsJson, mGid, isAdd ? 1 : -1, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                if (response.body() != null && response.body().isOk()) {
                    if (isAdd) {
                        mList.addAll(list);
                    } else {
                        mList.remove(userInfo);
                    }
                    // 更新群信息
                    EventGroupChange event = new EventGroupChange();
                    event.setNeedLoad(true);
                    EventBus.getDefault().post(event);
                    mViewAdapter.notifyDataSetChanged();
                } else {
                    if (isAdd) {
                        if (!TextUtils.isEmpty(response.body().getMsg())) {
                            ToastUtil.show(SetupSysManagerActivity.this, response.body().getMsg());
                        } else {
                            ToastUtil.show(SetupSysManagerActivity.this, "添加管理员失败");
                        }
                    } else {
                        ToastUtil.show(SetupSysManagerActivity.this, "取消失败");
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
     * 获取群信息
     */
    private void requestGroupInfo() {
        mMsgAction.groupInfo(mGid, new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                super.onResponse(call, response);
                if (response.body() != null && response.body().isOk()) {
                    mGroupInfo = response.body().getData();
                    if (mGroupInfo != null) {
                        parseMemberUser(mGroupInfo.getUsers());
                    }
                } else {
                    ToastUtil.show(SetupSysManagerActivity.this, "获取群信息失败");
                }
            }

            @Override
            public void onFailure(Call<ReturnBean<Group>> call, Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    /**
     * 获取管理员列表
     *
     * @return
     */
    private void parseMemberUser(List<MemberUser> listMember) {
        List<UserInfo> list = new ArrayList<>();
        if (listMember != null && listMember.size() > 0) {
            for (Long uid : mGroupInfo.getViceAdmins()) {
                for (MemberUser user : listMember) {
                    if (uid != null && uid.longValue() == user.getUid()) {// 群管理员
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUid(user.getUid());
                        userInfo.setHead(user.getHead());
                        userInfo.setName(user.getShowName());
                        list.add(userInfo);
                    }
                }
            }
        }
        mList.clear();
        mList.addAll(list);
        mList.add(0, mAdminsUser);
        mViewAdapter.notifyDataSetChanged();
    }

    /**
     * 发送消息前，需要检测网络连接状态，网络不可用，不能发送
     * 每条消息发送前，需要检测，语音和小视频录制之前，仍需要检测
     *
     * @return
     */
    public boolean checkNetConnectStatus() {
        boolean isOk;
        if (!NetUtil.isNetworkConnected()) {
            ToastUtil.show(this, "网络连接不可用，请稍后重试");
            isOk = false;
        } else {
            isOk = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.CONN_STATUS).get4Json(Boolean.class);
            if (!isOk) {
                ToastUtil.show(this, "连接已断开，请稍后再试");
            }
        }
        return isOk;
    }
}
