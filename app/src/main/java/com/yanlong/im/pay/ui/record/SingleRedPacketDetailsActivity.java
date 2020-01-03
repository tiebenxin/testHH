package com.yanlong.im.pay.ui.record;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.hm.cxpay.R;
import com.hm.cxpay.base.BasePayActivity;
import com.hm.cxpay.bean.EnvelopeDetailBean;
import com.hm.cxpay.bean.EnvelopeReceiverBean;
import com.hm.cxpay.bean.FromUserBean;
import com.hm.cxpay.bean.UserBean;
import com.hm.cxpay.databinding.ActivityRedPacketDetailsBinding;
import com.hm.cxpay.global.PayEnum;
import com.hm.cxpay.global.PayEnvironment;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.utils.DateUtils;
import com.hm.cxpay.utils.UIUtils;
import com.hm.cxpay.widget.CircleImageView;

import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.PopupSelectView;

import java.util.ArrayList;
import java.util.List;


/**
 * 红包详情页面
 */
@Route(path = "/app/singleRedPacketDetailsActivity")
public class SingleRedPacketDetailsActivity extends BasePayActivity {
    private List<EnvelopeReceiverBean> list = new ArrayList<>();

    private String[] strings = {"红包记录", "取消"};
    private PopupSelectView popupSelectView;
    private EnvelopeDetailBean envelopeDetailBean;
    private ActivityRedPacketDetailsBinding ui;

    public static Intent newIntent(Context context, EnvelopeDetailBean bean) {
        Intent intent = new Intent(context, SingleRedPacketDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", bean);
        intent.putExtras(bundle);
        return intent;
    }

    public static Intent newIntent(Context context, long rid, int fromType) {
        Intent intent = new Intent(context, SingleRedPacketDetailsActivity.class);
        intent.putExtra("rid", rid);
        intent.putExtra("fromType", fromType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_red_packet_details);
        ui.headView.getActionbar().setChangeStyleBg();
        ui.headView.getAppBarLayout().setBackgroundResource(com.hm.cxpay.R.color.c_c85749);
        envelopeDetailBean = getIntent().getParcelableExtra("data");
        initView();
        initData();
        initEvent();
        if (envelopeDetailBean == null) {
            long rid = getIntent().getLongExtra("rid", 0);
            int fromType = getIntent().getIntExtra("fromType", 0);
            if (rid > 0) {
                getEnvelopeDetail(rid, fromType);
            }
        }
    }

    private void initView() {
        ui.headView.getActionbar().getBtnRight().setImageResource(R.mipmap.ic_more);
        ui.headView.getActionbar().getBtnRight().setVisibility(View.VISIBLE);
        ui.mtListView.init(new RedPacketAdapter());
        ui.mtListView.getLoadView().setStateNormal();
    }


    private void initData() {
        if (envelopeDetailBean == null) {
            return;
        }
        FromUserBean userBean = envelopeDetailBean.getImUserInfo();
        UserBean user = PayEnvironment.getInstance().getUser();
        if (userBean != null) {
            UIUtils.loadAvatar(userBean.getAvatar(), ui.ivAvatar);
            ui.tvName.setText(userBean.getNickname() + "的红包");
        }
        ui.tvContent.setText(TextUtils.isEmpty(envelopeDetailBean.getNote()) ? "恭喜发财，大吉大利" : envelopeDetailBean.getNote());
        if (envelopeDetailBean.getType() == PayEnum.ERedEnvelopeType.NORMAL) {
            if (user != null && userBean.getUid() == user.getUid()) {//是自己发的
                if (envelopeDetailBean.getChatType() == 1) {//群聊
                    ui.llSend.setVisibility(View.GONE);
                    ui.llRecord.setVisibility(View.VISIBLE);
                    ui.tvNote.setVisibility(View.GONE);
                } else {//单聊
                    if (envelopeDetailBean.getRemainCnt() == 1) {//未抢
                        ui.llSend.setVisibility(View.VISIBLE);
                        ui.llRecord.setVisibility(View.GONE);
                        ui.tvNote.setVisibility(View.GONE);
                    } else {
                        ui.llSend.setVisibility(View.GONE);
                        ui.llRecord.setVisibility(View.VISIBLE);
                        ui.tvNote.setVisibility(View.GONE);
                    }
                }
            } else {
                if (envelopeDetailBean.getEnvelopeStatus() == PayEnum.EEnvelopeStatus.RECEIVED_FINISHED) {//红包已经被抢完，未领到
                    ui.llSend.setVisibility(View.GONE);
                    ui.llRecord.setVisibility(View.GONE);
                    ui.tvNote.setVisibility(View.VISIBLE);
                    ui.tvNote.setText("红包已经被领完");
                    ui.tvMoney.setVisibility(View.GONE);
                    ui.tvUnit.setVisibility(View.GONE);
                } else if (envelopeDetailBean.getEnvelopeStatus() == PayEnum.EEnvelopeStatus.PAST) {//红包已过期
                    ui.llSend.setVisibility(View.GONE);
                    ui.llRecord.setVisibility(View.GONE);
                    ui.tvNote.setVisibility(View.VISIBLE);
                    ui.tvNote.setText("该红包已过期");
                    ui.tvMoney.setVisibility(View.GONE);
                    ui.tvUnit.setVisibility(View.GONE);
                } else {
                    ui.llSend.setVisibility(View.GONE);
                    ui.llRecord.setVisibility(View.GONE);
                    ui.tvNote.setVisibility(View.VISIBLE);
                    ui.tvNote.setText("已存入零钱");
                }
            }
        } else {
            ui.llSend.setVisibility(View.GONE);
            ui.llRecord.setVisibility(View.VISIBLE);
            ui.tvNote.setVisibility(View.VISIBLE);
            ui.tvNote.setText("已存入零钱");
        }
        //初始化领取记录
        list = envelopeDetailBean.getRecvList();
        if (ui.llRecord.getVisibility() == View.VISIBLE) {
            ui.mtListView.getListView().getAdapter().notifyDataSetChanged();
            int remainCount = envelopeDetailBean.getRemainCnt();
            int totalCount = envelopeDetailBean.getCnt();
            String receivedMoney = UIUtils.getYuan(envelopeDetailBean.getAmt() - envelopeDetailBean.getRemainAmt());//已经抢了的钱
            String totalMoney = UIUtils.getYuan(envelopeDetailBean.getAmt());
            int receivedCount = totalCount - remainCount;
            if (user != null) {
                if (envelopeDetailBean.getEnvelopeStatus() == PayEnum.EEnvelopeStatus.PAST) {
                    if (userBean.getUid() == user.getUid()) {//是自己发的
                        if (envelopeDetailBean.getRemainCnt() != 0) {//未抢完
                            ui.tvHint.setText("该红包已过期。已领取" + receivedCount + "/" + totalCount + "个，共" + receivedMoney + "/" + totalMoney + "元");
                        } else {
                            String time = DateUtils.getGrabFinishedTime(envelopeDetailBean.getFinishTime());
                            ui.tvHint.setText(totalCount + "个红包共" + totalMoney + "元，" + time + "被抢光");
                        }
                    } else {
                        if (envelopeDetailBean.getRemainCnt() != 0) {//未抢完
                            ui.tvHint.setText("该红包已过期。已领取" + receivedCount + "/" + totalCount + "个");
                        } else {
                            String time = DateUtils.getGrabFinishedTime(envelopeDetailBean.getFinishTime());
                            ui.tvHint.setText(totalCount + "个红包，" + time + "被抢光");
                        }
                    }
                } else {
                    if (userBean.getUid() == user.getUid()) {//是自己发的
                        if (envelopeDetailBean.getRemainCnt() != 0) {//未抢完
                            ui.tvHint.setText("已领取" + receivedCount + "/" + totalCount + "个，共" + receivedMoney + "/" + totalMoney + "元");
                        } else {
                            String time = DateUtils.getGrabFinishedTime(envelopeDetailBean.getFinishTime());
                            ui.tvHint.setText(totalCount + "个红包共" + totalMoney + "元，" + time + "被抢光");
                        }
                    } else {
                        if (envelopeDetailBean.getRemainCnt() != 0) {//未抢完
                            ui.tvHint.setText("已领取" + receivedCount + "/" + totalCount + "个");
                        } else {
                            String time = DateUtils.getGrabFinishedTime(envelopeDetailBean.getFinishTime());
                            ui.tvHint.setText(totalCount + "个红包，" + time + "被抢光");
                        }
                    }
                }
            }
        }
        //设置金额
        if (envelopeDetailBean.getType() == PayEnum.ERedEnvelopeType.NORMAL) {
            ui.tvMoney.setText(UIUtils.getYuan(envelopeDetailBean.getAmt()));
        } else {
            findSelf();
        }

    }

    //找到自己抢红包记录
    private void findSelf() {
        if (list != null && list.size() > 0) {
            UserBean userBean = PayEnvironment.getInstance().getUser();
            EnvelopeReceiverBean selfReceiver = null;
            int len = list.size();
            for (int i = 0; i < len; i++) {
                EnvelopeReceiverBean bean = list.get(i);
                if (bean.getImUserInfo() != null && userBean != null) {
                    if (bean.getImUserInfo().getUid() == userBean.getUid()) {
                        selfReceiver = bean;
                    }
                }
            }

            if (selfReceiver != null) {
                ui.tvMoney.setVisibility(View.VISIBLE);
                ui.tvUnit.setVisibility(View.VISIBLE);
                ui.tvMoney.setText(UIUtils.getYuan(selfReceiver.getAmt()));
                ui.tvNote.setVisibility(View.VISIBLE);
            } else {
                ui.tvMoney.setVisibility(View.GONE);
                ui.tvUnit.setVisibility(View.GONE);
                ui.tvNote.setVisibility(View.GONE);
            }
        }
    }


    private void initEvent() {
        ui.headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                initPopup();
            }
        });
    }

    private void initPopup() {
        popupSelectView = new PopupSelectView(this, strings);
        popupSelectView.showAtLocation(ui.headView.getActionbar(), Gravity.BOTTOM, 0, 0);
        popupSelectView.setListener(new PopupSelectView.OnClickItemListener() {
            @Override
            public void onItem(String string, int postsion) {
                switch (postsion) {
                    case 0:
                        ARouter.getInstance().build("/app/redEnvelopeDetailsActivity").navigation();
                        break;
                }
                popupSelectView.dismiss();
            }
        });
    }


    class RedPacketAdapter extends RecyclerView.Adapter<RedPacketAdapter.RbViewHolder> {


        @Override
        public RbViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            RbViewHolder holder = new RbViewHolder(inflater.inflate(R.layout.item_red_packet_details, viewGroup, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RbViewHolder viewHolder, int position) {
            RbViewHolder holder = viewHolder;
            EnvelopeReceiverBean envelopeReceiverBean = list.get(position);
            holder.bindData(envelopeReceiverBean);
        }

        @Override
        public int getItemCount() {
            if (list != null && list.size() > 0) {
                return list.size();
            }
            return 0;
        }


        class RbViewHolder extends RecyclerView.ViewHolder {
            private CircleImageView ivAvatar;
            private TextView tvName;
            private TextView tvTime;
            private TextView tvMoney;
            private TextView tvLuck;
            private final ImageView ivLuck;


            public RbViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.sd_image_head);
                tvName = itemView.findViewById(R.id.tv_user_name);
                tvTime = itemView.findViewById(R.id.tv_date);
                tvMoney = itemView.findViewById(R.id.tv_money);
                tvLuck = itemView.findViewById(R.id.tv_luck);
                ivLuck = itemView.findViewById(R.id.iv_luck);
            }

            public void bindData(EnvelopeReceiverBean bean) {
                if (bean == null) {
                    return;
                }
                FromUserBean userBean = bean.getImUserInfo();
                if (userBean != null) {
                    UIUtils.loadAvatar(userBean.getAvatar(), ivAvatar);
                    tvName.setText(userBean.getNickname());
                }
                tvTime.setText(DateUtils.getGrabTime(bean.getTime()));
                tvMoney.setText(UIUtils.getYuan(bean.getAmt()) + "元");
                //手气最佳
                ivLuck.setVisibility(bean.getBestLuck() == 1 ? View.VISIBLE : View.GONE);
                tvLuck.setVisibility(bean.getBestLuck() == 1 ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void getEnvelopeDetail(long rid, int fromType) {
        PayHttpUtils.getInstance().getEnvelopeDetail(rid, "", fromType)
                .compose(RxSchedulers.<BaseResponse<EnvelopeDetailBean>>compose())
                .compose(RxSchedulers.<BaseResponse<EnvelopeDetailBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<EnvelopeDetailBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<EnvelopeDetailBean> baseResponse) {
                        if (baseResponse.isSuccess()) {
                            EnvelopeDetailBean bean = baseResponse.getData();
                            if (bean != null) {
                                envelopeDetailBean = bean;
                                initData();
                            }
                        } else {
                            ToastUtil.show(getContext(), baseResponse.getMessage());
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        if (baseResponse.getCode() == -21000) {
                        } else {
                            ToastUtil.show(getContext(), baseResponse.getMessage());
                        }
                    }
                });
    }


}
