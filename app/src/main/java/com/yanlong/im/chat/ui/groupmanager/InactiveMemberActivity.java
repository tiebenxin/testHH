package com.yanlong.im.chat.ui.groupmanager;

import android.os.Bundle;
import android.view.View;

import com.example.nim_lib.config.Preferences;
import com.example.nim_lib.ui.BaseBindActivity;
import com.yanlong.im.R;
import com.yanlong.im.chat.ui.GroupSelectUserActivity;
import com.yanlong.im.databinding.ActivityInactiveMemberBinding;

import net.cb.cb.library.utils.IntentUtil;
import net.cb.cb.library.utils.ViewUtils;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-10
 * @updateAuthor
 * @updateDate
 * @description 不活跃群成员
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class InactiveMemberActivity extends BaseBindActivity<ActivityInactiveMemberBinding> implements View.OnClickListener {

    private String mGid;

    @Override
    protected int setView() {
        return R.layout.activity_inactive_member;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void initEvent() {
        setActionBarLeft(bindingView.headView);
        bindingView.layoutThreeDay.setOnClickListener(this);
        bindingView.layoutSevenDay.setOnClickListener(this);
        bindingView.layoutOneMonth.setOnClickListener(this);
    }

    @Override
    protected void loadData() {
        mGid = getIntent().getStringExtra(GroupSelectUserActivity.GID);
    }

    @Override
    public void onClick(View v) {
        if(ViewUtils.isFastDoubleClick()){
            return;
        }
        String title="";
         switch (v.getId()){
             case R.id.layout_three_day:
                 title = "三天";
                 break;
             case R.id.layout_seven_day:
                 title = "一周";
                 break;
             case R.id.layout_one_month:
                 title = "一个月";
                 break;
         }
        Bundle bundle = new Bundle();
        bundle.putString(GroupSelectUserActivity.GID, mGid);
        bundle.putString(Preferences.TITLE, title);
        IntentUtil.gotoActivity(this,InactiveMemberListActivity.class,bundle);
    }
}
