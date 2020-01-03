package com.yanlong.im.chat.ui.groupmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.nim_lib.config.Preferences;
import com.example.nim_lib.ui.BaseBindActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yanlong.im.R;
import com.yanlong.im.adapter.CommonRecyclerViewAdapter;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.NoRedEnvelopesBean;
import com.yanlong.im.chat.ui.GroupSelectUserActivity;
import com.yanlong.im.databinding.ActivityNoredEnvelopesBinding;
import com.yanlong.im.databinding.ItemNoredEnvelopesBinding;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.IntentUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.ViewUtils;
import net.cb.cb.library.view.AlertYesNo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-10
 * @updateAuthor
 * @updateDate
 * @description 禁止领取零钱红包
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class NoRedEnvelopesActivity extends BaseBindActivity<ActivityNoredEnvelopesBinding> {

    public static final int REUQEST_CODE_200 = 200;

    private CommonRecyclerViewAdapter<UserInfo, ItemNoredEnvelopesBinding> mViewAdapter;
    private List<UserInfo> mList = new ArrayList<>();
    private List<Long> mUsers = new ArrayList<>();
    private String mGid;
    private MsgAction mMsgAction;
    private Gson mGosn = new Gson();

    @Override
    protected int setView() {
        return R.layout.activity_nored_envelopes;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mViewAdapter = new CommonRecyclerViewAdapter<UserInfo, ItemNoredEnvelopesBinding>(this, R.layout.item_nored_envelopes) {

            @Override
            public void bind(ItemNoredEnvelopesBinding binding, UserInfo userInfo,
                             int position, RecyclerView.ViewHolder viewHolder) {
                if (position == mList.size() - 1) {
                    binding.viewLine.setVisibility(View.GONE);
                } else {
                    binding.viewLine.setVisibility(View.VISIBLE);
                }
                Glide.with(NoRedEnvelopesActivity.this).load(userInfo.getHead())
                        .apply(GlideOptionsUtil.headImageOptions()).into(binding.imgHead);
                binding.txtName.setText(userInfo.getName4Show());
                binding.txtCancleManager.setOnClickListener(o -> {
                    if (ViewUtils.isFastDoubleClick()) {
                        return;
                    }
                    showDialog(userInfo.getName4Show(), position);
                });
            }
        };

        bindingView.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindingView.recyclerView.setAdapter(mViewAdapter);

        mViewAdapter.setData(mList);
    }

    @Override
    protected void initEvent() {
        setActionBarLeft(bindingView.headView);
        bindingView.txtSetupMember.setOnClickListener(o -> {
            if (ViewUtils.isFastDoubleClick()) {
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putInt(GroupSelectUserActivity.TYPE, 3);
            bundle.putString(GroupSelectUserActivity.GID, mGid);
            mUsers.clear();
            if (mList != null && mList.size() > 0) {
                for (UserInfo userInfo : mList) {
                    if (userInfo.getuType() != 1) {
                        mUsers.add(userInfo.getUid());
                    }
                }
                bundle.putString(GroupSelectUserActivity.UIDS, mGosn.toJson(mUsers));
            }
            IntentUtil.gotoActivityForResult(NoRedEnvelopesActivity.this, GroupSelectUserActivity.class, bundle, REUQEST_CODE_200);
        });
    }

    @Override
    protected void loadData() {
        mGid = getIntent().getStringExtra(GroupSelectUserActivity.GID);
        mMsgAction = new MsgAction();
        getCantOpenUpRedMembers();
    }

    private void showDialog(String name, final int position) {
        AlertYesNo dialog = new AlertYesNo();
        dialog.init(this, getString(R.string.dialog_message_title), "确定要移除\"" + name + "\"领取零钱红包", getString(R.string.dialog_btn_confrim),
                getString(R.string.dialog_btn_cancle), new AlertYesNo.Event() {
                    @Override
                    public void onON() {

                    }

                    @Override
                    public void onYes() {
                        mUsers.clear();
                        mUsers.add(mList.get(position).getUid());
                        toggleOpenUpRedEnvelope(mGosn.toJson(mUsers), null, mList.get(position), false);
                    }
                });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REUQEST_CODE_200) {
                String value = data.getStringExtra(Preferences.DATA);
                List<UserInfo> list = new Gson().fromJson(value, new TypeToken<List<UserInfo>>() {
                }.getType());

                if (list != null && list.size() > 0) {
                    mUsers.clear();
                    for (UserInfo userInfo : list) {
                        mUsers.add(userInfo.getUid());
                    }
                    toggleOpenUpRedEnvelope(mGosn.toJson(mUsers), list, null, true);
                }
            }
        }
    }

    /**
     * 开关群成员禁领红包
     *
     * @param uidJson  用户uidjson
     * @param list     添加用户列表
     * @param userInfo 移除用户
     * @param isAdd    增加还是移除
     */
    private void toggleOpenUpRedEnvelope(String uidJson, List<UserInfo> list, UserInfo userInfo, boolean isAdd) {
        mMsgAction.toggleOpenUpRedEnvelope(uidJson, mGid, isAdd ? 1 : -1, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                if (response.body() != null && response.body().isOk()) {
                    if (isAdd) {
                        mList.addAll(list);
                    } else {
                        mList.remove(userInfo);
                    }
                    mViewAdapter.notifyDataSetChanged();
                } else {
                    if (isAdd) {
                        ToastUtil.show(NoRedEnvelopesActivity.this, "禁止领取零钱红包失败");
                    } else {
                        ToastUtil.show(NoRedEnvelopesActivity.this, "移除失败");
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
     * 获取禁领红包群成员列表
     */
    private void getCantOpenUpRedMembers() {
        mMsgAction.getCantOpenUpRedMembers(mGid, new CallBack<ReturnBean<List<NoRedEnvelopesBean>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<NoRedEnvelopesBean>>> call, Response<ReturnBean<List<NoRedEnvelopesBean>>> response) {
                super.onResponse(call, response);
                if (response.body() != null && response.body().isOk()) {
                    List<NoRedEnvelopesBean> list = response.body().getData();
                    if (list != null) {
                        parseMemberUser(list);
                    }
                } else {
                    ToastUtil.show(NoRedEnvelopesActivity.this, "获取禁领红包群成员列表失败");
                }
            }

            @Override
            public void onFailure(Call<ReturnBean<List<NoRedEnvelopesBean>>> call, Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    /**
     * 实体类转换
     *
     * @return
     */
    private void parseMemberUser(List<NoRedEnvelopesBean> listMember) {
        mList.clear();
        for (NoRedEnvelopesBean bean : listMember) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUid(bean.getUid());
            userInfo.setHead(bean.getAvatar());
            userInfo.setName(bean.getNickname());
            mList.add(userInfo);
        }
        mViewAdapter.notifyDataSetChanged();
    }
}
