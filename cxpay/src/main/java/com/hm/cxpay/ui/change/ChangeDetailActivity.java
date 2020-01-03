package com.hm.cxpay.ui.change;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hm.cxpay.R;
import com.hm.cxpay.bean.CommonBean;
import com.hm.cxpay.utils.UIUtils;
import com.luck.picture.lib.tools.DateUtils;

import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

/**
 * @类名：零钱详情
 * @Date：2019/12/11
 * @by zjy
 * @备注：
 */

public class ChangeDetailActivity extends AppActivity {
    private HeadView headView;
    private ActionbarView actionbar;
    private Activity activity;
    private TextView tvTitle;//顶部标题
    private TextView tvContent;//顶部内容
    private TextView tvType;//类型
    private TextView tvTime;//时间
    private TextView tvOrderId;//交易单号
    private TextView tvBalance;//零钱余额
    private TextView tvRemark;//备注
    private TextView titleTvTime;//时间类型标题

    private RelativeLayout layoutQuestion;//常见问题

    private CommonBean data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_change_detail);
        initView();
        initData();
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        layoutQuestion = findViewById(R.id.layout_question);
        tvContent = findViewById(R.id.tv_content);
        tvTitle = findViewById(R.id.tv_title);
        tvType = findViewById(R.id.tv_type);
        tvTime = findViewById(R.id.tv_time);
        tvOrderId = findViewById(R.id.tv_order_id);
        tvBalance = findViewById(R.id.tv_balance);
        tvRemark = findViewById(R.id.tv_remark);
        titleTvTime = findViewById(R.id.title_tv_time);
        actionbar = headView.getActionbar();
    }

    private void initData() {
        if (getIntent().getParcelableExtra("item_data") != null) {
            data = getIntent().getParcelableExtra("item_data");
        } else {
            data = new CommonBean();
        }
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        layoutQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build("/app/HelpActivity").navigation();
            }
        });
        //收支类型
        if (data.getIncome() == 1) { //1 收入 其他支出
            tvType.setText("收入");
            tvContent.setText("+" + UIUtils.getYuan(data.getAmt()));
        } else {
            tvType.setText("支出");
            tvContent.setText("-" + UIUtils.getYuan(data.getAmt()));
        }
        //类型：1转账给 2发红包给 3充值 4提现 5红包退款 6消费(忽略) 7红包收款 8转账收款 9转账退款
        int type = data.getTradeType();
        if(type == 1 || type == 8){
            tvTitle.setText("转账");
            titleTvTime.setText("转账时间：");
        }else if(type == 2 || type == 7){
            tvTitle.setText("零钱红包");
            titleTvTime.setText("创建时间：");
        }else if(type == 3){
            tvTitle.setText("充值");
            titleTvTime.setText("充值时间：");
        }else if(type == 4){
            tvTitle.setText("提现");
            titleTvTime.setText("提现时间：");
        }else if(type == 5 || type == 9 || type == 10|| type == 11){
            tvTitle.setText("退款");
            titleTvTime.setText("退款时间：");
        }
        tvTime.setText(DateUtils.timeStamp2Date(data.getCreateTime(), ""));
        tvOrderId.setText(data.getTradeId() + "");
        tvBalance.setText("¥"+UIUtils.getYuan(data.getBalance()));
        if(!TextUtils.isEmpty(data.getNote())){
            tvRemark.setText(data.getNote());
        }
    }
}
