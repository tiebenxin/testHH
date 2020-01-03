package com.yanlong.im.chat.ui.groupmanager;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.nim_lib.config.Preferences;
import com.example.nim_lib.ui.BaseBindActivity;
import com.yanlong.im.R;
import com.yanlong.im.adapter.CommonRecyclerViewAdapter;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.ui.GroupSelectUserActivity;
import com.yanlong.im.databinding.ActivityInactiveMemberListBinding;
import com.yanlong.im.databinding.ItemNoredEnvelopesBinding;

import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-12
 * @updateAuthor
 * @updateDate
 * @description 三、七、一个月不活跃
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class InactiveMemberListActivity extends BaseBindActivity<ActivityInactiveMemberListBinding> {

    private CommonRecyclerViewAdapter<MemberUser, ItemNoredEnvelopesBinding> mViewAdapter;
    private List<MemberUser> mList = new ArrayList<>();
    private String mGid;
    private String mTitle;
    private boolean mCheckBoxShow = false;

    @Override
    protected int setView() {
        return R.layout.activity_inactive_member_list;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mViewAdapter = new CommonRecyclerViewAdapter<MemberUser, ItemNoredEnvelopesBinding>(this, R.layout.item_nored_envelopes) {

            @Override
            public void bind(ItemNoredEnvelopesBinding binding, MemberUser memberUser,
                             int position, RecyclerView.ViewHolder viewHolder) {
                if (mList.size() - 1 == position) {
                    binding.viewLine.setVisibility(View.GONE);
                } else {
                    binding.viewLine.setVisibility(View.VISIBLE);
                }
                if (mCheckBoxShow) {
                    binding.ckSelect.setVisibility(View.VISIBLE);
                } else {
                    binding.ckSelect.setVisibility(View.GONE);
                }
                binding.txtName.setText(memberUser.getName());
                binding.ckSelect.setChecked(memberUser.isChecked());
                binding.txtCancleManager.setOnClickListener(o -> {
                    if (ViewUtils.isFastDoubleClick()) {
                        return;
                    }
                    mList.remove(position);
                    requestRemoveUser(memberUser.getGid());
                });
                binding.ckSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        memberUser.setChecked(isChecked);
                    }
                });
            }
        };
        bindingView.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindingView.recyclerView.setAdapter(mViewAdapter);

        for (int i = 0; i < 5; i++) {
            MemberUser memberUser = new MemberUser();
            memberUser.setGid(i + "");
            memberUser.setName("周杰伦"+i);
            mList.add(memberUser);
        }
        mViewAdapter.setData(mList);
    }

    @Override
    protected void initEvent() {
        setActionBarLeft(bindingView.headView);
        bindingView.headView.getActionbar().setTxtRight("多选");
        bindingView.headView.getActionbar().getTxtRight().setOnClickListener(o -> {
            if (bindingView.layoutBottom.getVisibility() == View.VISIBLE) {
                bindingView.headView.getActionbar().setTxtRight("多选");
                bindingView.layoutBottom.setVisibility(View.GONE);
                mCheckBoxShow = false;
            } else {
                bindingView.headView.getActionbar().setTxtRight("取消");
                mCheckBoxShow = true;
                setCheckUser(false);
                bindingView.layoutBottom.setVisibility(View.VISIBLE);
            }
            mViewAdapter.notifyDataSetChanged();
        });
        bindingView.txtAllSelect.setOnClickListener(o -> {
            setCheckUser(true);
            mViewAdapter.notifyDataSetChanged();
        });
        bindingView.txtRemove.setOnClickListener(o -> {
            if (ViewUtils.isFastDoubleClick()) {
                return;
            }
            if (checkSelectUser()) {
                String gids = getCheckUser();
                mViewAdapter.notifyDataSetChanged();
                requestRemoveUser(gids);
            } else {
                ToastUtil.show(this, "请先选择需要移除的群成员");
            }
        });
    }

    @Override
    protected void loadData() {
        mGid = getIntent().getStringExtra(GroupSelectUserActivity.GID);
        mTitle = getIntent().getStringExtra(Preferences.TITLE);

        bindingView.headView.getActionbar().setTitle(mTitle + "不活跃");
    }

    private void requestRemoveUser(String gid) {
        mViewAdapter.notifyDataSetChanged();
    }

    private boolean checkSelectUser() {
        boolean result = false;
        for (int i = 0; i < bindingView.recyclerView.getChildCount(); i++) {
            CheckBox checkBox = bindingView.recyclerView.getChildAt(i).findViewById(R.id.ck_select);
            if (checkBox.isChecked()) {
                result = true;
                break;
            }
        }
        return result;
    }

    private String getCheckUser() {
        StringBuffer stringBuffer = new StringBuffer();
        String result = "";
        for (int i = bindingView.recyclerView.getChildCount() - 1; i >= 0; i--) {
            CheckBox checkBox = bindingView.recyclerView.getChildAt(i).findViewById(R.id.ck_select);
            if (checkBox.isChecked()) {
                stringBuffer.append(mList.get(i).getGid() + ",");
                mList.remove(i);
            }
        }
        if (stringBuffer.length() > 0) {
            result = stringBuffer.substring(0, stringBuffer.length() - 1);
        }
        return result;
    }

    private void setCheckUser(boolean value) {
        for (MemberUser memberUser : mList) {
            memberUser.setChecked(value);
        }
        mViewAdapter.notifyDataSetChanged();
    }
}
