package com.yanlong.im.chat.ui.groupmanager;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.nim_lib.ui.BaseBindActivity;
import com.yanlong.im.R;
import com.yanlong.im.adapter.CommonRecyclerViewAdapter;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.ExitGroupUser;
import com.yanlong.im.chat.ui.GroupSelectUserActivity;
import com.yanlong.im.databinding.ActivityExitGroupBinding;
import com.yanlong.im.databinding.ItemNoredEnvelopesBinding;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-13
 * @updateAuthor
 * @updateDate
 * @description 退群成员列表
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class ExitGroupActivity extends BaseBindActivity<ActivityExitGroupBinding> {

    private CommonRecyclerViewAdapter<ExitGroupUser, ItemNoredEnvelopesBinding> mViewAdapter;
    private List<ExitGroupUser> mList = new ArrayList<>();
    private String mGid;
    private MsgAction mMsgAction;

    @Override
    protected int setView() {
        return R.layout.activity_exit_group;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

        mViewAdapter = new CommonRecyclerViewAdapter<ExitGroupUser, ItemNoredEnvelopesBinding>(this, R.layout.item_nored_envelopes) {

            @Override
            public void bind(ItemNoredEnvelopesBinding binding, ExitGroupUser memberUser,
                             int position, RecyclerView.ViewHolder viewHolder) {
                if (mList.size() - 1 == position) {
                    binding.viewLine.setVisibility(View.GONE);
                } else {
                    binding.viewLine.setVisibility(View.VISIBLE);
                }
                Glide.with(ExitGroupActivity.this).load(memberUser.getAvatar())
                        .apply(GlideOptionsUtil.headImageOptions()).into(binding.imgHead);
                binding.txtExitGroupTime.setText(TimeToString.getTimeWx(memberUser.getLeaveTime() * 1000l) + "退群");
                binding.txtName.setText(memberUser.getNickname());

                binding.txtCancleManager.setVisibility(View.GONE);
                binding.txtExitGroupTime.setVisibility(View.VISIBLE);
            }
        };
        bindingView.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindingView.recyclerView.setAdapter(mViewAdapter);
        mViewAdapter.setData(mList);
    }

    @Override
    protected void initEvent() {
        setActionBarLeft(bindingView.headView);
    }

    @Override
    protected void loadData() {
        mGid = getIntent().getStringExtra(GroupSelectUserActivity.GID);
        mMsgAction = new MsgAction();
        exitGroupList();
    }

    /**
     * 获取退群成员列表
     */
    private void exitGroupList() {
        mMsgAction.exitGroupList(mGid, new CallBack<ReturnBean<List<ExitGroupUser>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<ExitGroupUser>>> call, Response<ReturnBean<List<ExitGroupUser>>> response) {
                super.onResponse(call, response);
                if (response.body() != null && response.body().isOk()) {
                    mList.clear();
                    List<ExitGroupUser> list = response.body().getData();
                    mList.addAll(list);
                    if(list.size()>0){
                        setVisibleData(true);
                    }else{
                        setVisibleData(false);
                    }
                    mViewAdapter.notifyDataSetChanged();
                } else {
                    setVisibleData(false);
                    ToastUtil.show(ExitGroupActivity.this, "获取群信息失败");
                }
            }

            @Override
            public void onFailure(Call<ReturnBean<List<ExitGroupUser>>> call, Throwable t) {
                super.onFailure(call, t);
                setVisibleData(false);
            }
        });
    }

    private void setVisibleData(boolean value){
        if(value){
            bindingView.recyclerView.setVisibility(View.VISIBLE);
            bindingView.viewNoData.setVisibility(View.GONE);
        }else{
            bindingView.recyclerView.setVisibility(View.GONE);
            bindingView.viewNoData.setVisibility(View.VISIBLE);
        }
    }
}
