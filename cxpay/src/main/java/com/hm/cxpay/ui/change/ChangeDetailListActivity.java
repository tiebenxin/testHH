package com.hm.cxpay.ui.change;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.hm.cxpay.R;
import com.hm.cxpay.bean.BillBean;
import com.hm.cxpay.bean.CommonBean;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.ui.bill.BillDetailListAdapter;
import com.hm.cxpay.utils.DateUtils;
import com.hm.cxpay.widget.refresh.EndlessRecyclerOnScrollListener;

import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @类名：零钱明细
 * @Date：2019/12/9
 * @by zjy
 * @备注：
 */

public class ChangeDetailListActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private RecyclerView recyclerView;
    private Activity activity;
    private LinearLayout noDataLayout;
    private LinearLayout billLayout;
    private TextView tvChangeSelectDate;//零钱专用选时间布局


    private BillDetailListAdapter adapter;
    private LinearLayoutManager manager;

    private List<CommonBean> list;
    private int page = 1;//默认第一页
    private int year;
    private int month;
    private long selectTimeDataValue = 0L;//选择的月份转换后的时间戳


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_bill_list);
        initView();
        isBill(false);
        initData();
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        recyclerView = findViewById(R.id.rc_list);
        noDataLayout = findViewById(R.id.no_data_layout);
        tvChangeSelectDate = findViewById(R.id.tv_change_select_date);
        billLayout = findViewById(R.id.layout_bill);
        actionbar = headView.getActionbar();
        list = new ArrayList<>();
    }

    private void initData() {
        selectTimeDataValue = DateUtils.getMonthBegin(new Date());
        tvChangeSelectDate.setText(TimeToString.getSelectMouth(Calendar.getInstance().getTimeInMillis()));//默认显示当前年月
        getChangeDetailsList();//先拿当前的时间戳去请求
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        adapter = new BillDetailListAdapter(activity,list,2);
        manager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        //加载更多
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                getChangeDetailsList();
                adapter.setLoadState(adapter.LOADING);
            }
        });
        //选日期
        tvChangeSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTimePicker();
            }
        });
    }


    /**
     * 请求->获取零钱明细
     */
    private void getChangeDetailsList(){
        PayHttpUtils.getInstance().getChangeDetailsList(page, selectTimeDataValue)
                .compose(RxSchedulers.<BaseResponse<BillBean>>compose())
                .compose(RxSchedulers.<BaseResponse<BillBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<BillBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<BillBean> baseResponse) {
                            if (baseResponse.getData() != null) {
                                //1 如果当前页有数据
                                if(baseResponse.getData().getItems()!=null && baseResponse.getData().getItems().size()>0){
                                    //1-1 如果是加载更多，则分页数据填充到尾部
                                    if (page > 1) {
                                        adapter.addMoreList(baseResponse.getData().getItems());
                                    } else {
                                        //1-2 如果是第一次加载，则只拿第一页数据
                                        adapter.updateList(baseResponse.getData().getItems());
                                    }
                                    page++;
                                    showNoData(false);
                                }else {
                                    //2 如果当前页没数据
                                    //2-1 如果是加载更多，当没有数据的时候，提示已经到底了
                                    if (page > 1) {
                                        adapter.setLoadState(adapter.LOADING_END);
                                        showNoData(false);
                                    } else {
                                        //2-2 如果是第一次加载就没有数据则不显示底部
                                        adapter.setLoadState(adapter.LOADING_GONE);
                                        showNoData(true);
                                    }
                                }
                            }
                    }

                    @Override
                    public void onHandleError(BaseResponse<BillBean> baseResponse) {
                        super.onHandleError(baseResponse);
                        ToastUtil.show(context, baseResponse.getMessage());
                    }
                });

    }

    /**
     * 是否显示无数据默认图
     * @param ifShow
     */
    private void showNoData(boolean ifShow){
        if(ifShow){
            noDataLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            noDataLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void initTimePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, Calendar.DAY_OF_MONTH);

        Calendar start = Calendar.getInstance();
        start.set(2019, 0, 1);//2019-1-1
        Calendar end = Calendar.getInstance();
        end.set(2100, 11, 31);//2100-12-31

        //时间选择器
        TimePickerView pvTime = new TimePickerBuilder(activity, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH) + 1;
                tvChangeSelectDate.setText(year + "年" + month + "月");
                //选择日期后，页数和日期均重置，并重新发请求
                page = 1;//页数重置为1
                selectTimeDataValue = DateUtils.getMonthBegin(date);
                getChangeDetailsList();
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

    /**
     * 账单 零钱 区分显示布局
     */
    private void isBill(boolean isTrue) {
        if(isTrue){
            tvChangeSelectDate.setVisibility(View.GONE);
            billLayout.setVisibility(View.VISIBLE);
            actionbar.setTitle("账单明细");
        }else {
            tvChangeSelectDate.setVisibility(View.VISIBLE);
            billLayout.setVisibility(View.GONE);
            actionbar.setTitle("零钱明细");
        }
    }

}
