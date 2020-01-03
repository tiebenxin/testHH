package com.yanlong.im.chat.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hm.cxpay.utils.UIUtils;
import com.yanlong.im.R;
import com.yanlong.im.adapter.AdapterBalanceLabel;
import com.yanlong.im.chat.bean.BalanceAssistantMessage;
import com.yanlong.im.utils.socket.MsgBean;

import net.cb.cb.library.utils.TimeToString;

/**
 * @author Liszt
 * @date 2019/12/16
 * Description 零钱助手标签
 */
public class LabelItemView extends LinearLayout {
    private TextView tvTitle;
    private TextView tvTime;
    private TextView tvMoneyTitle;
    private TextView tvMoney;
    private LinearLayout llLabelParent;
    private LinearLayout llDetail;
    private View viewLine;
    private TextView tvDetail;

    public LabelItemView(Context context) {
        this(context, null);
    }

    public LabelItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.layout_balance, this);
        tvTitle = root.findViewById(R.id.tv_title);
        tvTime = root.findViewById(R.id.tv_time);
        tvMoneyTitle = root.findViewById(R.id.tv_money_title);
        tvMoney = root.findViewById(R.id.tv_money);
        llLabelParent = root.findViewById(R.id.ll_parent);
        llDetail = root.findViewById(R.id.ll_detail);
        tvDetail = root.findViewById(R.id.tv_view);
        viewLine = root.findViewById(R.id.view_line);
    }

    public void bindData(BalanceAssistantMessage message) {
        if (message == null) {
            return;
        }
        tvTitle.setText(message.getTitle());
        tvTime.setText(TimeToString.YYYY_MM_DD_HH_MM_SS(message.getTime()));
        tvMoneyTitle.setText(message.getAmountTitle());
        tvMoney.setText("¥ " + UIUtils.getYuan(message.getAmount()));
        if (message.getDetailType() == MsgBean.BalanceAssistantMessage.DetailType.RED_ENVELOPE_VALUE) {//红包详情
            llDetail.setVisibility(VISIBLE);
            viewLine.setVisibility(VISIBLE);
//            tvDetail.setText("红包详情");
            tvDetail.setText("查看详情");
        } else if (message.getDetailType() == MsgBean.BalanceAssistantMessage.DetailType.TRANS_VALUE) {//交易详情
            llDetail.setVisibility(VISIBLE);
            viewLine.setVisibility(VISIBLE);
//            tvDetail.setText("交易详情");
            tvDetail.setText("查看详情");
        } else {
            llDetail.setVisibility(GONE);
            viewLine.setVisibility(GONE);
        }
        ControllerLinearList controller = new ControllerLinearList(llLabelParent);
        AdapterBalanceLabel adapterLabel = new AdapterBalanceLabel(message.getLabelItems(), getContext());
        controller.setAdapter(adapterLabel);
        invalidate();
    }
}
