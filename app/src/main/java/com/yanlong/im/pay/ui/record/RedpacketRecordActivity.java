package com.yanlong.im.pay.ui.record;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.hm.cxpay.R;
import com.hm.cxpay.base.BasePayActivity;

import net.cb.cb.library.view.ActionbarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Route(path = "/app/redpacketRecordActivity")
public class RedpacketRecordActivity extends BasePayActivity {


    private ActionbarView mActionBar;
    private ImageView mSdImageHead;
    private TextView mTvSelectDate;
    private TextView mTvUserName;
    private TextView mTvMoney;
    private TabLayout mTabLayout;
    private ViewPager mViewPage;
    List<Fragment> fragments;
    String[] tabTiles = new String[]{"我收到的", "我发出的"};
    private int year;
    private int month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_packet_record);
        initView();
        initViewpager();
        initEvent();
    }

    private void initView() {
        mActionBar = findViewById(R.id.action_bar);
        mSdImageHead = findViewById(R.id.sd_image_head);
        mTvSelectDate = findViewById(R.id.tv_select_date);
        mTvUserName = findViewById(R.id.tv_user_name);
        mTvMoney = findViewById(R.id.tv_money);
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPage = findViewById(R.id.viewPage);
        getSystemDate();
    }

    private void initViewpager() {
        fragments = new ArrayList<>();
        fragments.add(FragmentRedEnvelopeReceived.newInstance());
        fragments.add(FragmentRedEnvelopeSend.newInstance());
        mViewPage.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabTiles[position];
            }
        });
        mTabLayout.setupWithViewPager(mViewPage);
    }

    private void initEvent() {
        mActionBar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        mTvSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTimePicker();
            }
        });


    }


    private void getSystemDate() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        mTvSelectDate.setText(year + "年" + month + "月");
    }


    private void initTimePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, Calendar.DAY_OF_MONTH);

        Calendar start = Calendar.getInstance();
        start.set(2019, 11, 1);
        Calendar end = Calendar.getInstance();
        end.set(2099, 11, 31);

        //时间选择器
        TimePickerView pvTime = new TimePickerBuilder(RedpacketRecordActivity.this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH) + 1;
                mTvSelectDate.setText(year + "年" + month + "月");
            }
        })
                .setType(new boolean[]{true, true, false, false, false, false})
                .setDate(calendar)
                .setRangDate(start, end)
                .build();

        pvTime.show();
    }


}
