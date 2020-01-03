package com.yanlong.im.pay.ui.record;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.hm.cxpay.bean.RedDetailsBean;
import com.hm.cxpay.bean.UserBean;
import com.hm.cxpay.databinding.ActivityRedEnvelopeDetailBinding;
import com.hm.cxpay.global.PayEnvironment;
import com.hm.cxpay.utils.DateUtils;
import com.hm.cxpay.utils.UIUtils;

import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Liszt
 * @date 2019/12/2
 * Description 红包明细界面
 */
@Route(path = "/app/redEnvelopeDetailsActivity")
public class RedEnvelopeRecordActivity extends AppActivity {

    private ActivityRedEnvelopeDetailBinding ui;
    private Calendar currentCalendar;
    private int year;
    private int month;
    private List<Fragment> fragments;
    String[] tabTiles = new String[]{"收到的红包", "发出的红包"};
    private int currentTab = 0;//默认收到红包界面


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, com.hm.cxpay.R.layout.activity_red_envelope_detail);
        getSystemDate();
        initViewpager();
        ui.headView.getActionbar().setChangeStyleBg();
        ui.headView.getAppBarLayout().setBackgroundResource(com.hm.cxpay.R.color.c_c85749);
        //标题栏事件
        ui.headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                ToastUtil.show(context, "账单");

            }
        });

        ui.tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DateTimeWheelDialog timeWheelDialog = createDialog(2);
//                timeWheelDialog.show();
                initTimePicker();

            }
        });
        ui.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                ui.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    private void initViewpager() {
        fragments = new ArrayList<>();
        fragments.add(FragmentRedEnvelopeReceived.newInstance());
        fragments.add(FragmentRedEnvelopeSend.newInstance());
        ui.viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
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
        ui.tabLayout.setupWithViewPager(ui.viewPager);
    }


    private void getSystemDate() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        ui.tvTime.setText(year + "年" + month + "月");
    }

    private void initTimePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, Calendar.DAY_OF_MONTH);

        Calendar start = Calendar.getInstance();
        start.set(2019, 11, 1);//2019-12-1
        Calendar end = Calendar.getInstance();
        end.set(2100, 11, 31);//2100-12-31

        //时间选择器
        TimePickerView pvTime = new TimePickerBuilder(RedEnvelopeRecordActivity.this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                currentCalendar = calendar;
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH) + 1;
                ui.tvTime.setText(year + "年" + month + "月");
                notifyTimeUpdate();
            }
        })
                .setType(new boolean[]{true, true, false, false, false, false})
                .setDate(calendar)
                .setRangDate(start, end)
                .setCancelText("取消")
                .setCancelColor(Color.parseColor("#878787"))
                .setSubmitText("确定")
                .setSubmitColor(Color.parseColor("#32b152"))
                .build();

        pvTime.show();
    }

    private void notifyTimeUpdate() {
        if (fragments != null && fragments.size() > 0) {
            for (int i = 0; i < fragments.size(); i++) {
                Fragment fragment = fragments.get(i);
                if (fragment instanceof FragmentRedEnvelopeReceived) {
                    FragmentRedEnvelopeReceived received = (FragmentRedEnvelopeReceived) fragment;
                    received.updateDetails();
                } else if (fragment instanceof FragmentRedEnvelopeSend) {
                    FragmentRedEnvelopeSend send = (FragmentRedEnvelopeSend) fragment;
                    send.updateDetails();
                }
            }
        }
    }

    public long getCurrentCalendar() {
        if (currentCalendar == null) {
            currentCalendar = Calendar.getInstance();
        }
        return DateUtils.getStartTimeOfMonth(currentCalendar);
    }

    public int getCurrentTab() {
        return currentTab;
    }

    @SuppressLint("SetTextI18n")
    public void initDetails(RedDetailsBean bean, boolean isReceive) {
        if (bean != null) {
            UserBean user = PayEnvironment.getInstance().getUser();
            String name = "";
            if (user != null) {
                name = !TextUtils.isEmpty(user.getRealName()) ? user.getRealName() : "";
            }
            if (!TextUtils.isEmpty(PayEnvironment.getInstance().getNick())) {
                name = PayEnvironment.getInstance().getNick();
            }
            if (isReceive && currentTab == 0) {//收到红包
                ui.tvName.setText(name + "共收到红包");
                ui.tvMoney.setText("¥" + UIUtils.getYuan(bean.getSumAmt()) + "元");
                ui.tvTotal.setText("共收到红包" + bean.getTotal() + "个");
            } else if (!isReceive && currentTab == 1) {//发出红包
                ui.tvName.setText(name + "共发出红包");
                ui.tvMoney.setText("¥" + UIUtils.getYuan(bean.getSumAmt()) + "元");
                ui.tvTotal.setText("共发出红包" + bean.getTotal() + "个");
            }
        }
    }
}
