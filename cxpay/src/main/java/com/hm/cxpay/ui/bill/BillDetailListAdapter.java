package com.hm.cxpay.ui.bill;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hm.cxpay.R;
import com.hm.cxpay.bean.CommonBean;
import com.hm.cxpay.ui.change.ChangeDetailActivity;
import com.hm.cxpay.utils.UIUtils;
import com.luck.picture.lib.tools.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @类名：账单明细适配器
 * @Date：2019/12/9
 * @by zjy
 * @备注：零钱明细复用
 */

public class BillDetailListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //头部尾部数量
    private final static int HEAD_COUNT = 0;
    private final static int FOOT_COUNT = 1;//直接修改数量控制头部和尾部
    //区分布局类型
    private final static int TYPE_HEAD = 0;
    private final static int TYPE_CONTENT = 1;
    private final static int TYPE_FOOTER = 2;

    // 当前加载状态，默认为隐藏底部
    private int loadState = 4;
    // 正在加载
    public final int LOADING = 1;
    // 加载更多
    public final int LOADING_MORE = 2;
    // 加载到底
    public final int LOADING_END = 3;
    // 隐藏底部
    public final int LOADING_GONE = 4;

    private LayoutInflater inflater;
    private Activity activity;
    private List<CommonBean> dataList;//列表数据
    private int type;//1 账单明细布局  2 零钱明细布局

    public BillDetailListAdapter(Activity activity, List<CommonBean> dataList,int type) {
        inflater = LayoutInflater.from(activity);
        this.dataList = new ArrayList<>();
        this.dataList.addAll(dataList);
        this.activity = activity;
        this.type = type;
    }

    //刷新数据
    public void updateList(List<CommonBean> list) {
        dataList.clear();
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    //加载更多
    public void addMoreList(List list) {
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    //列表内容数量
    public int getContentSize() {
        return dataList.size();
    }


    @Override
    public int getItemViewType(int position) {
        int contentSize = getContentSize();
        if (HEAD_COUNT != 0 && position == 0) { // 头部
            return TYPE_HEAD;
        } else if (FOOT_COUNT != 0 && position == HEAD_COUNT + contentSize) { // 尾部
            return TYPE_FOOTER;
        } else {
            return TYPE_CONTENT; // 内容
        }
    }

    //item总数
    @Override
    public int getItemCount() {
        return dataList.size() + HEAD_COUNT + FOOT_COUNT;
    }


    //具体显示逻辑
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof BillDetailListAdapter.ContentHolder) {
            ContentHolder holder = (ContentHolder) viewHolder;
            if (dataList != null && dataList.size() > 0) {
                if (dataList.get(position) != null) {
                    final CommonBean bean = dataList.get(position);
                    //时间
                    holder.tvTime.setText(DateUtils.timeStamp2Date(bean.getCreateTime(),""));
                    //根据收支类型->显示操作金额
                    if(bean.getIncome()==1){ //1 收入 其他支出
                        holder.tvCost.setText("+"+UIUtils.getYuan(bean.getAmt()));
                    }else {
                        holder.tvCost.setText("-"+UIUtils.getYuan(bean.getAmt()));
                    }
                    //1转账给别人 2发红包给别人 3充值 4提现 5红包退款 7红包收款 8转账收款 9转账退款 10提现退款 11充值退款 12消费退款(忽略)
                    //TODO 注：type=1 账单明细显示图标+状态/不显示余额，type=2 零钱明细不显示图标+状态/显示余额
                    //     账单规则：充值提现显示状态，其他只在失败/退款时显示状态
                    if(type==1){
                        holder.ivImage.setVisibility(View.VISIBLE);
                        holder.ivImage.setImageResource(selectImg(bean.getTradeType()));
                        holder.tvBalance.setVisibility(View.GONE);
                        holder.tvStatus.setVisibility(View.VISIBLE);
                    }else {
                        holder.ivImage.setVisibility(View.GONE);
                        holder.tvBalance.setVisibility(View.VISIBLE);
                        holder.tvBalance.setText("余额: "+UIUtils.getYuan(bean.getBalance()));
                        holder.tvStatus.setVisibility(View.GONE);
                    }
                    if(bean.getTradeType()==1){
                        if(bean.getOtherUser()!=null && bean.getOtherUser().getNickname()!=null){
                            holder.tvContent.setText("转账-转给"+bean.getOtherUser().getNickname());
                        }else {
                            holder.tvContent.setText("转账");
                        }
                        if(bean.getStat()==1){
//                            holder.tvStatus.setText("转账成功");
                            holder.tvStatus.setVisibility(View.GONE);
                        }else if(bean.getStat()==2){
                            holder.tvStatus.setText("转账失败");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==99){
                            holder.tvStatus.setText("处理中");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }
                    }else if(bean.getTradeType()==2){
                        //如果是群红包
                        if(bean.getToGroup()==1){
                            holder.tvContent.setText("零钱红包-发群红包");
                        }else {
                            //如果是普通红包
                            if(bean.getOtherUser()!=null && bean.getOtherUser().getNickname()!=null){
                                holder.tvContent.setText("零钱红包-发给"+bean.getOtherUser().getNickname());
                            }else {
                                holder.tvContent.setText("零钱红包");
                            }
                        }
                        if(bean.getStat()==1){
//                            holder.tvStatus.setText("发送成功");
                            holder.tvStatus.setVisibility(View.GONE);
                        }else if(bean.getStat()==2){
                            holder.tvStatus.setText("发送失败");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==99){
                            holder.tvStatus.setText("处理中");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }
                    }else if(bean.getTradeType()==3){
                        holder.tvBalance.setVisibility(View.GONE);
                        holder.tvStatus.setVisibility(View.VISIBLE);
                        if(bean.getStat()==1){
                            holder.tvStatus.setText("充值成功");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }else if(bean.getStat()==2){
                            holder.tvStatus.setText("充值失败");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==99){
                            holder.tvStatus.setText("处理中");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }
                        if(!TextUtils.isEmpty(bean.getBankCardInfo())){
                            holder.tvContent.setText("充值-"+bean.getBankCardInfo());
                        }else {
                            holder.tvContent.setText("充值");
                        }
                    }else if(bean.getTradeType()==10){
                        holder.tvBalance.setVisibility(View.GONE);
                        holder.tvStatus.setVisibility(View.VISIBLE);
                        if(bean.getStat()==1){
                            if(bean.getRefundType()==1){
                                holder.tvStatus.setText("已部分退款");
                            }else {
                                holder.tvStatus.setText("已全额退款");
                            }
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==2){
                            holder.tvStatus.setText("退款失败");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==99){
                            holder.tvStatus.setText("处理中");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }
                        if(!TextUtils.isEmpty(bean.getBankCardInfo())){
                            holder.tvContent.setText("提现-"+bean.getBankCardInfo());
                        }else {
                            holder.tvContent.setText("提现");
                        }
                    }else if(bean.getTradeType()==11){
                        holder.tvBalance.setVisibility(View.GONE);
                        holder.tvStatus.setVisibility(View.VISIBLE);
                        if(bean.getStat()==1){
                            if(bean.getRefundType()==1){
                                holder.tvStatus.setText("已部分退款");
                            }else {
                                holder.tvStatus.setText("已全额退款");
                            }
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==2){
                            holder.tvStatus.setText("退款失败");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==99){
                            holder.tvStatus.setText("处理中");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }
                        if(!TextUtils.isEmpty(bean.getBankCardInfo())){
                            holder.tvContent.setText("充值-"+bean.getBankCardInfo());
                        }else {
                            holder.tvContent.setText("充值");
                        }
                    }else if(bean.getTradeType()==4){
                        holder.tvBalance.setVisibility(View.GONE);
                        holder.tvStatus.setVisibility(View.VISIBLE);
                        if(bean.getStat()==1){
                            holder.tvStatus.setText("提现成功");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }else if(bean.getStat()==2){
                            holder.tvStatus.setText("提现失败");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==99){
                            holder.tvStatus.setText("处理中");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }
                        if(!TextUtils.isEmpty(bean.getBankCardInfo())){
                            holder.tvContent.setText("提现-"+bean.getBankCardInfo());
                        }else {
                            holder.tvContent.setText("提现");
                        }
                    }else if(bean.getTradeType()==5){
                        holder.tvContent.setText("零钱红包-退款");
                        if(bean.getStat()==1){
                            if(bean.getRefundType()==1){
                                holder.tvStatus.setText("已部分退款");
                            }else {
                                holder.tvStatus.setText("已全额退款");
                            }
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==2){
                            holder.tvStatus.setText("退款失败");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==99){
                            holder.tvStatus.setText("处理中");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }
                    }else if(bean.getTradeType()==7){
                        //如果是群红包
                        if(bean.getToGroup()==1){
                            holder.tvContent.setText("零钱红包-收群红包");
                        }else {
                            //如果是普通红包
                            if(bean.getOtherUser()!=null && bean.getOtherUser().getNickname()!=null){
                                holder.tvContent.setText("零钱红包-来自"+bean.getOtherUser().getNickname());
                            }else {
                                holder.tvContent.setText("零钱红包");
                            }
                        }
                        if(bean.getStat()==1){
//                            holder.tvStatus.setText("领取成功");
                            holder.tvStatus.setVisibility(View.GONE);
                        }else if(bean.getStat()==2){
                            holder.tvStatus.setText("领取失败");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==99){
                            holder.tvStatus.setText("处理中");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }
                    }else if(bean.getTradeType()==8){
                        if(bean.getOtherUser()!=null && bean.getOtherUser().getNickname()!=null){
                            holder.tvContent.setText("转账-来自"+bean.getOtherUser().getNickname());
                        }else {
                            holder.tvContent.setText("转账");
                        }
                        if(bean.getStat()==1){
//                            holder.tvStatus.setText("收款成功");
                            holder.tvStatus.setVisibility(View.GONE);
                        }else if(bean.getStat()==2){
                            holder.tvStatus.setText("收款失败");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==99){
                            holder.tvStatus.setText("处理中");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }
                    }else if(bean.getTradeType()==9){
                        holder.tvContent.setText("转账退款");
                        if(bean.getStat()==1){
                            if(bean.getRefundType()==1){
                                holder.tvStatus.setText("已部分退款");
                            }else {
                                holder.tvStatus.setText("已全额退款");
                            }
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==2){
                            holder.tvStatus.setText("退款失败");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(0)));
                        }else if(bean.getStat()==99){
                            holder.tvStatus.setText("处理中");
                            holder.tvStatus.setTextColor(activity.getResources().getColor(changeGreenOrRed(1)));
                        }
                    }
                    //点击跳转账单详情
                    holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(type==1){
                                activity.startActivity(new Intent(activity,BillDetailActivity.class).putExtra("item_data",bean));
                            }else if(type==2){
                                activity.startActivity(new Intent(activity,ChangeDetailActivity.class).putExtra("item_data",bean));
                            }
                        }
                    });
                }
            }
        } else {
            //加载更多-尾部
            FootHolder holder = (FootHolder) viewHolder;
            switch (loadState) {
                case LOADING:
                    holder.footerLayout.setVisibility(View.VISIBLE);
                    holder.loadingMore.setVisibility(View.VISIBLE);
                    holder.loadingFinished.setVisibility(View.GONE);
                    holder.loadingNoMore.setVisibility(View.GONE);
                    break;
                case LOADING_MORE:
                    holder.footerLayout.setVisibility(View.VISIBLE);
                    holder.loadingMore.setVisibility(View.GONE);
                    holder.loadingFinished.setVisibility(View.VISIBLE);
                    holder.loadingNoMore.setVisibility(View.GONE);
                    break;
                case LOADING_END:
                    holder.footerLayout.setVisibility(View.VISIBLE);
                    holder.loadingMore.setVisibility(View.GONE);
                    holder.loadingFinished.setVisibility(View.GONE);
                    holder.loadingNoMore.setVisibility(View.VISIBLE);
                    break;
                case LOADING_GONE:
                    holder.footerLayout.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        //进行判断显示类型，来创建返回不同的View
        if (position == TYPE_CONTENT) {
            View itemView = inflater.inflate(R.layout.item_bill_list, parent, false);
            return new BillDetailListAdapter.ContentHolder(itemView);
        } else {
            View itemView = inflater.inflate(R.layout.main_footer_layout, parent, false);
            return new BillDetailListAdapter.FootHolder(itemView);
        }
    }

    // 内容
    private class ContentHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;//头像
        private TextView tvTime;//时间
        private TextView tvContent;//内容
        private TextView tvCost;//花费
        private TextView tvBalance;//余额
        private TextView tvStatus;//提现和充值状态
        private RelativeLayout itemLayout;

        public ContentHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvCost = itemView.findViewById(R.id.tv_cost);
            tvBalance = itemView.findViewById(R.id.tv_balance);
            tvStatus = itemView.findViewById(R.id.tv_status);
            itemLayout = itemView.findViewById(R.id.item_layout);
        }
    }

    // 尾部
    class FootHolder extends RecyclerView.ViewHolder {
        private TextView loadingMore;
        private TextView loadingFinished;
        private TextView loadingNoMore;
        private LinearLayout footerLayout;

        public FootHolder(View itemView) {
            super(itemView);
            loadingMore = itemView.findViewById(R.id.loading_more);
            loadingFinished = itemView.findViewById(R.id.loading_finished);
            loadingNoMore = itemView.findViewById(R.id.loading_nomore);
            footerLayout = itemView.findViewById(R.id.footer_layout);
        }
    }

    /**
     * 设置上拉加载状态
     *
     * @param loadState 0.正在加载 1.加载完成 2.加载到底
     */
    public void setLoadState(int loadState) {
        this.loadState = loadState;
        notifyDataSetChanged();
    }

    /**
     * 根据不同交易类型显示不同图标
     * @return
     */
    private int selectImg(int ImageType) {
        switch (ImageType) {
            case 1:
            case 8:
            case 9:
                return R.mipmap.ic_transfer;
            case 2:
            case 5:
            case 7:
                return R.mipmap.ic_redpackage;
            case 3:
            case 11:
                return R.mipmap.ic_recharge_trade;
            case 4:
            case 10:
                return R.mipmap.ic_withdraw_trade;

            default:
                return R.mipmap.ic_transfer;
        }
    }

    /**
     * 成功/失败颜色改变
     *
     * 0 红色
     * 1 绿色
     */
    private int changeGreenOrRed(int type) {
        if(type==0){
            return net.cb.cb.library.R.color.c_e95f52;
        }else {
            return net.cb.cb.library.R.color.c_32b053;
        }
    }
}
