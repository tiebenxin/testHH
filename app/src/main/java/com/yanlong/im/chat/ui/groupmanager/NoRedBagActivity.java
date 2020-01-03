package com.yanlong.im.chat.ui.groupmanager;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.nim_lib.ui.BaseBindActivity;
import com.yanlong.im.R;
import com.yanlong.im.adapter.CommonRecyclerViewAdapter;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.ui.GroupSelectUserActivity;
import com.yanlong.im.databinding.ActivityNoRedBagBinding;
import com.yanlong.im.databinding.ItemNoRedbagBinding;

import net.cb.cb.library.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-10
 * @updateAuthor
 * @updateDate
 * @description 未领取的零钱红包
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class NoRedBagActivity extends BaseBindActivity<ActivityNoRedBagBinding> {

    private CommonRecyclerViewAdapter<MemberUser, ItemNoRedbagBinding> mViewAdapter;
    private List<MemberUser> list = new ArrayList<>();
    private String mGid;

    @Override
    protected int setView() {
        return R.layout.activity_no_red_bag;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mViewAdapter = new CommonRecyclerViewAdapter<MemberUser, ItemNoRedbagBinding>(this, R.layout.item_no_redbag) {

            @Override
            public void bind(ItemNoRedbagBinding binding, MemberUser memberUser,
                             int position, RecyclerView.ViewHolder viewHolder) {
                binding.layoutRedBag.setOnClickListener(o ->{
                    if(ViewUtils.isFastDoubleClick()){
                        return;
                    }

                });
            }
        };

        bindingView.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindingView.recyclerView.setAdapter(mViewAdapter);

        for (int i = 0; i < 3; i++) {
            MemberUser memberUser = new MemberUser();
            list.add(memberUser);
        }
        mViewAdapter.setData(list);
    }

    @Override
    protected void initEvent() {
        setActionBarLeft(bindingView.headView);
    }

    @Override
    protected void loadData() {
        mGid = getIntent().getStringExtra(GroupSelectUserActivity.GID);
    }
}
