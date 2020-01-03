package com.hm.cxpay.ui.bill;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.hm.cxpay.utils.DateUtils;
import com.hm.cxpay.widget.refresh.EndlessRecyclerOnScrollListener;

import net.cb.cb.library.utils.DensityUtil;
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
 * @类名：账单明细
 * @Date：2019/12/9
 * @by zjy
 * @备注：
 */

public class BillDetailListActivity extends AppActivity {

    private HeadView headView;
    private ActionbarView actionbar;
    private RecyclerView recyclerView;
    private Activity activity;
    private LinearLayout noDataLayout;
    private LinearLayout billLayout;
    private TextView tvSelectDate;
    private TextView tvSelectType;
    private AlertDialog selectTypeDialog;
    private TextView tvAll;//全部
    private TextView tvTransfer;//转账
    private TextView tvRedpacket;//红包
    private TextView tvRechargeWithdraw;//充值提现
    private TextView tvRefund;//有退款
    private TextView tvChangeSelectDate;//零钱专用选时间布局


    private BillDetailListAdapter adapter;
    private LinearLayoutManager manager;

    private List<CommonBean> list;
    private int page = 1;//默认第一页
    private int selectType = 1;//交易类型 1.全部 2.转账 3.红包 4.充值提现 5.有退款
    private int year;
    private int month;
    private long selectTimeDataValue = 0L;//选择的月份转换后的时间戳


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_bill_list);
        initView();
        isBill(true);
        initData();
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        recyclerView = findViewById(R.id.rc_list);
        noDataLayout = findViewById(R.id.no_data_layout);
        tvSelectDate = findViewById(R.id.tv_select_date);
        tvSelectType = findViewById(R.id.tv_select_type);
        tvChangeSelectDate = findViewById(R.id.tv_change_select_date);
        billLayout = findViewById(R.id.layout_bill);
        actionbar = headView.getActionbar();
        list = new ArrayList<>();
    }

    private void initData() {
        selectTimeDataValue = DateUtils.getMonthBegin(new Date());//拿当前月份第一天的时间戳
        tvSelectDate.setText(TimeToString.getSelectMouth(Calendar.getInstance().getTimeInMillis()));//默认显示当前年月
        getBillDetailsList();//先拿当前的时间戳去请求
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        adapter = new BillDetailListAdapter(activity,list,1);
        manager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        //加载更多
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                getBillDetailsList();
                adapter.setLoadState(adapter.LOADING);
            }
        });
        //选日期
        tvSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTimePicker();
            }
        });
        //选类型
        tvSelectType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectTypeDialog!=null){
                    selectTypeDialog.show();
                }else {
                    showSelectTypeDialog();
                }
            }
        });
    }


    /**
     * 请求->获取账单明细
     */
    private void getBillDetailsList(){
        PayHttpUtils.getInstance().getBillDetailsList(page, selectTimeDataValue,selectType,"")
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
                tvSelectDate.setText(year + "年" + month + "月");
                //选择日期后，页数和日期均重置，并重新发请求
                page = 1;//页数重置为1
                selectTimeDataValue = DateUtils.getMonthBegin(date);
                getBillDetailsList();
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
     * 选择切换交易类型弹框
     */
    private void showSelectTypeDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setCancelable(true);//取消点击外部消失弹窗
        selectTypeDialog = dialogBuilder.create();
        //获取界面
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_select_bill_type, null);
        //初始化控件
        tvAll = dialogView.findViewById(R.id.tv_all);
        tvTransfer = dialogView.findViewById(R.id.tv_transfer);
        tvRedpacket = dialogView.findViewById(R.id.tv_redpacket);
        tvRechargeWithdraw = dialogView.findViewById(R.id.tv_recharge_withdraw);
        tvRefund = dialogView.findViewById(R.id.tv_refund);
        //显示和点击事件
        tvAll.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelectedStatus();
                tvAll.setBackgroundResource(R.drawable.shape_5radius_solid_517da2);
                tvAll.setTextColor(getResources().getColor(R.color.white));
                selectTypeDialog.dismiss();
                //刷新数据
                tvSelectType.setText("全部");
                page=1;
                selectType =1 ;
                getBillDetailsList();
            }
        });
        tvTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelectedStatus();
                tvTransfer.setBackgroundResource(R.drawable.shape_5radius_solid_517da2);
                tvTransfer.setTextColor(getResources().getColor(R.color.white));
                selectTypeDialog.dismiss();
                //刷新数据
                tvSelectType.setText("转账");
                page=1;
                selectType =2 ;
                getBillDetailsList();
            }
        });
        tvRedpacket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelectedStatus();
                tvRedpacket.setBackgroundResource(R.drawable.shape_5radius_solid_517da2);
                tvRedpacket.setTextColor(getResources().getColor(R.color.white));
                selectTypeDialog.dismiss();
                //刷新数据
                tvSelectType.setText("红包");
                page=1;
                selectType =3 ;
                getBillDetailsList();
            }
        });
        tvRechargeWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelectedStatus();
                tvRechargeWithdraw.setBackgroundResource(R.drawable.shape_5radius_solid_517da2);
                tvRechargeWithdraw.setTextColor(getResources().getColor(R.color.white));
                selectTypeDialog.dismiss();
                //刷新数据
                tvSelectType.setText("充值/提现");
                page=1;
                selectType =4 ;
                getBillDetailsList();
            }
        });
        tvRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelectedStatus();
                tvRefund.setBackgroundResource(R.drawable.shape_5radius_solid_517da2);
                tvRefund.setTextColor(getResources().getColor(R.color.white));
                selectTypeDialog.dismiss();
                //刷新数据
                tvSelectType.setText("退款");
                page=1;
                selectType =5 ;
                getBillDetailsList();
            }
        });
        //展示界面
        selectTypeDialog.show();
        //解决圆角shape背景无效问题
        Window window = selectTypeDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setGravity(Gravity.BOTTOM);
        //设置宽高
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = DensityUtil.dip2px(activity, 195);
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        selectTypeDialog.getWindow().setAttributes(lp);
        selectTypeDialog.setContentView(dialogView);
    }

    /**
     * 清除其他选中状态
     */
    public void clearSelectedStatus() {
        tvAll.setBackgroundResource(R.drawable.shape_5radius_stroke_517da2);
        tvTransfer.setBackgroundResource(R.drawable.shape_5radius_stroke_517da2);
        tvRedpacket.setBackgroundResource(R.drawable.shape_5radius_stroke_517da2);
        tvRechargeWithdraw.setBackgroundResource(R.drawable.shape_5radius_stroke_517da2);
        tvRefund.setBackgroundResource(R.drawable.shape_5radius_stroke_517da2);
        tvAll.setTextColor(getResources().getColor(R.color.c_517da2));
        tvTransfer.setTextColor(getResources().getColor(R.color.c_517da2));
        tvRedpacket.setTextColor(getResources().getColor(R.color.c_517da2));
        tvRechargeWithdraw.setTextColor(getResources().getColor(R.color.c_517da2));
        tvRefund.setTextColor(getResources().getColor(R.color.c_517da2));
    }

    /**
     * 账单 零钱 区分显示布局
     */
    private void isBill(boolean isTrue) {
        if(isTrue){
            tvChangeSelectDate.setVisibility(View.GONE);
            billLayout.setVisibility(View.VISIBLE);
        }else {
            tvChangeSelectDate.setVisibility(View.VISIBLE);
            billLayout.setVisibility(View.GONE);
        }
    }



}
