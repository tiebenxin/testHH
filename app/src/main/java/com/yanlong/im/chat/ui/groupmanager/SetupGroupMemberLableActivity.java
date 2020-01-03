package com.yanlong.im.chat.ui.groupmanager;

import android.os.Bundle;

import com.example.nim_lib.ui.BaseBindActivity;
import com.yanlong.im.R;
import com.yanlong.im.databinding.ActivitySetupGroupMemberLableBinding;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-26
 * @updateAuthor
 * @updateDate
 * @description 设置标签
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class SetupGroupMemberLableActivity extends BaseBindActivity<ActivitySetupGroupMemberLableBinding> {

    @Override
    protected int setView() {
        return R.layout.activity_setup_group_member_lable;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void initEvent() {
        setActionBarLeft(bindingView.headView);
        bindingView.btnAdd.setOnClickListener(o->{

        });
    }

    @Override
    protected void loadData() {

    }
}
