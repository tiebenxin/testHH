package com.hm.cxpay.ui.transfer;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.hm.cxpay.R;
import com.hm.cxpay.base.BasePayActivity;
import com.hm.cxpay.bean.CxTransferBean;
import com.hm.cxpay.bean.TransferDetailBean;
import com.hm.cxpay.bean.TransferResultBean;
import com.hm.cxpay.dailog.DialogDefault;
import com.hm.cxpay.databinding.ActivityTransferDetailBinding;
import com.hm.cxpay.eventbus.TransferSuccessEvent;
import com.hm.cxpay.global.PayEnum;
import com.hm.cxpay.global.PayEnvironment;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.utils.DateUtils;
import com.hm.cxpay.utils.UIUtils;
import com.jrmf360.tools.utils.ThreadUtil;

import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;

import org.greenrobot.eventbus.EventBus;

/**
 * @author Liszt
 * @date 2019/12/19
 * Description 转账详情
 */
public class TransferDetailActivity extends BasePayActivity {

    private ActivityTransferDetailBinding ui;
    private boolean isFromMe;
    private String tradeId;
    private TransferDetailBean detailBean;
    private String actionId;
    private String msgJson = "";

    public static Intent newIntent(Context context, TransferDetailBean bean, String tradeId, boolean isFromMe, String msgJson) {
        Intent intent = new Intent(context, TransferDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", bean);
        intent.putExtras(bundle);
        intent.putExtra("isFromMe", isFromMe);
        intent.putExtra("tradeId", tradeId);
        intent.putExtra("msg", msgJson);
        return intent;
    }

    public static Intent newIntent(Context context, TransferDetailBean bean, String tradeId, boolean isFromMe) {
        Intent intent = new Intent(context, TransferDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", bean);
        intent.putExtras(bundle);
        intent.putExtra("isFromMe", isFromMe);
        intent.putExtra("tradeId", tradeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_transfer_detail);
        Intent intent = getIntent();
        detailBean = intent.getParcelableExtra("data");
        isFromMe = intent.getBooleanExtra("isFromMe", false);
        tradeId = intent.getStringExtra("tradeId");
        msgJson = intent.getStringExtra("msg");
        initView();
        if (detailBean != null) {
            initData(detailBean);
        } else {
            httpGetDetail();
        }
    }

    private void initView() {
        ui.headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
            }
        });

        //提醒对方收款
        ui.tvNoticeReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoticeReceiveDialog();
            }
        });
        //立即退还
        ui.tvReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReturnTransferDialog();
            }
        });

        //收款
        ui.tvReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiveTransfer();
            }
        });


    }

    private void initData(TransferDetailBean detailBean) {
        if (detailBean != null) {
            //领取转账 stat: 1未领取 2已领取 3已拒收 4已过期
            int status = detailBean.getStat();
            updateUI(detailBean.getIncome(), status);
            ui.ivIcon.setImageResource(getDrawableId(status));
            ui.tvNote.setText(getNote(detailBean.getIncome(), status, detailBean.getRecvUser().getNickname()));
            ui.tvMoney.setText(UIUtils.getYuan(detailBean.getAmt()));
            updateTimeUI(detailBean, status);
        }

    }

    private void updateTimeUI(TransferDetailBean detailBean, int status) {
        ui.tvTimeTransfer.setText("转账时间：" + DateUtils.getTransferTime(detailBean.getTransTime()));
        if (status == 1) {
            ui.tvTimeReturn.setVisibility(View.GONE);
        } else if (status == 2) {
            ui.tvTimeReturn.setVisibility(View.VISIBLE);
            ui.tvTimeTransfer.setText("收款时间：" + DateUtils.getTransferTime(detailBean.getRecvTime()));
        } else if (status == 3) {
            ui.tvTimeReturn.setVisibility(View.VISIBLE);
            ui.tvTimeTransfer.setText("退还时间：" + DateUtils.getTransferTime(detailBean.getRejectTime()));
        } else if (status == 4) {
            ui.tvTimeReturn.setVisibility(View.GONE);
        }
    }

    private int getDrawableId(int status) {
        if (status == 1) {
            return R.mipmap.ic_wait_collection;
        } else if (status == 2) {
            return R.mipmap.ic_transfer_success;
        } else if (status == 3) {
            return R.mipmap.ic_return_transfer;
        } else if (status == 4) {//过期
            return R.mipmap.ic_return_transfer;
        } else {
            return R.mipmap.ic_wait_collection;
        }
    }

    private String getNote(int income, int status, String nick) {
        String note = "";
        if (status == 1) {
            if (income == 1) {
                note = "等待确认收款";
            } else {
                note = "等待" + nick + "确认收款";
            }
        } else if (status == 2) {
            if (income == 1) {
                note = "已收款";
            } else {
                note = nick + "已收款";
            }
        } else if (status == 3) {
            note = "已退还";
        } else if (status == 4) {//过期
            return "过期已退还";
        }
        return note;
    }

    private void updateUI(int income, int status) {
        if (income == 1) {
            if (status == 1) {
                ui.llReceive.setVisibility(View.VISIBLE);
                ui.llReturn.setVisibility(View.GONE);
                ui.llWaitReceive.setVisibility(View.GONE);
            } else if (status == 2) {
                ui.llReceive.setVisibility(View.GONE);
                ui.llReturn.setVisibility(View.GONE);
                ui.llWaitReceive.setVisibility(View.GONE);
            } else if (status == 3) {
                ui.llReceive.setVisibility(View.GONE);
                ui.llReturn.setVisibility(View.GONE);
                ui.llWaitReceive.setVisibility(View.GONE);
            } else if (status == 4) {
                ui.llReceive.setVisibility(View.GONE);
                ui.llReturn.setVisibility(View.GONE);
                ui.llWaitReceive.setVisibility(View.GONE);
            }
        } else {
            if (status == 1) {
                ui.llReceive.setVisibility(View.GONE);
                ui.llReturn.setVisibility(View.GONE);
                ui.llWaitReceive.setVisibility(View.VISIBLE);
            } else if (status == 2) {
                ui.llReceive.setVisibility(View.GONE);
                ui.llReturn.setVisibility(View.GONE);
                ui.llWaitReceive.setVisibility(View.GONE);
            } else if (status == 3) {
                ui.llReceive.setVisibility(View.GONE);
                ui.llReturn.setVisibility(View.VISIBLE);
                ui.llWaitReceive.setVisibility(View.GONE);
            } else if (status == 4) {
                ui.llReceive.setVisibility(View.GONE);
                ui.llReturn.setVisibility(View.GONE);
                ui.llWaitReceive.setVisibility(View.GONE);
            }
        }
    }


    /**
     * 获取账单详情
     */
    private void httpGetDetail() {
        PayHttpUtils.getInstance().getTransferDetail(tradeId)
                .compose(RxSchedulers.<BaseResponse<TransferDetailBean>>compose())
                .compose(RxSchedulers.<BaseResponse<TransferDetailBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<TransferDetailBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<TransferDetailBean> baseResponse) {
                        if (baseResponse.getData() != null) {
                            //如果当前页有数据
                            detailBean = baseResponse.getData();
                            initData(detailBean);
                        } else {

                        }

                    }

                    @Override
                    public void onHandleError(BaseResponse<TransferDetailBean> baseResponse) {
                        super.onHandleError(baseResponse);
                        ToastUtil.show(context, baseResponse.getMessage());
                    }
                });

    }

    /**
     * 领取转账
     */
    private void receiveTransfer() {
        if (detailBean == null) {
            return;
        }
        String actionId = UIUtils.getUUID();

        PayHttpUtils.getInstance().receiveTransfer(actionId, tradeId, detailBean.getPayUser().getUid())
                .compose(RxSchedulers.<BaseResponse<TransferResultBean>>compose())
                .compose(RxSchedulers.<BaseResponse<TransferResultBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<TransferResultBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<TransferResultBean> baseResponse) {
                        if (baseResponse.getData() != null) {
                            //如果当前页有数据
                            TransferResultBean resultBean = baseResponse.getData();
                            notifyTransfer(createTransferBean(resultBean, PayEnum.ETransferOpType.TRANS_RECEIVE));
                            ThreadUtil.getInstance().runMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    TransferDetailActivity.this.finish();
                                }
                            });
                        } else {
                            ToastUtil.show(context, baseResponse.getMessage());
                        }

                    }

                    @Override
                    public void onHandleError(BaseResponse<TransferResultBean> baseResponse) {
                        super.onHandleError(baseResponse);
                        ToastUtil.show(context, baseResponse.getMessage());
                    }
                });

    }

    /**
     * 拒收转账
     */
    private void returnTransfer() {
        if (detailBean == null) {
            return;
        }
        if (TextUtils.isEmpty(actionId)) {
            actionId = UIUtils.getUUID();
        } else {
            return;
        }

        PayHttpUtils.getInstance().returnTransfer(actionId, tradeId, detailBean.getPayUser().getUid())
                .compose(RxSchedulers.<BaseResponse<TransferResultBean>>compose())
                .compose(RxSchedulers.<BaseResponse<TransferResultBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<TransferResultBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<TransferResultBean> baseResponse) {
                        if (baseResponse.getData() != null) {
                            //如果当前页有数据
                            TransferResultBean resultBean = baseResponse.getData();
                            notifyTransfer(createTransferBean(resultBean, PayEnum.ETransferOpType.TRANS_REJECT));
                            ThreadUtil.getInstance().runMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    TransferDetailActivity.this.finish();
                                }
                            });
                        } else {
                            ToastUtil.show(context, baseResponse.getMessage());
                        }

                    }

                    @Override
                    public void onHandleError(BaseResponse<TransferResultBean> baseResponse) {
                        super.onHandleError(baseResponse);
                        ToastUtil.show(context, baseResponse.getMessage());
                    }
                });

    }

    public CxTransferBean createTransferBean(TransferResultBean bean, @PayEnum.ETransferOpType int type) {
        long id = UIUtils.getTradeId(tradeId);
        if (detailBean == null || id <= 0 || detailBean.getPayUser() == null || detailBean.getRecvUser() == null) {
            return null;
        }
        CxTransferBean transferBean = new CxTransferBean();
        transferBean.setUid(detailBean.getPayUser().getUid());
        transferBean.setAmount(detailBean.getAmt());
        transferBean.setOpType(type);
        transferBean.setInfo(detailBean.getNote());
        transferBean.setSign(bean.getSign());
        transferBean.setTradeId(id);
        transferBean.setMsgJson(msgJson);
        return transferBean;
    }

    public void notifyTransfer(CxTransferBean bean) {
        EventBus.getDefault().post(new TransferSuccessEvent(bean));
    }

    private void showReturnTransferDialog() {
        DialogDefault dialogReturn = new DialogDefault(this);
        dialogReturn.setTitleAndSure(false, true);
        dialogReturn.setContent("是否退还" + detailBean.getPayUser().getNickname() + "的转账", true)
                .setRight("退还")
                .setLeft("取消")
                .setListener(new DialogDefault.IDialogListener() {
                    @Override
                    public void onSure() {
                        returnTransfer();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
        dialogReturn.show();
    }

    private void showNoticeReceiveDialog() {
        DialogDefault dialogNotice = new DialogDefault(this);
        dialogNotice.setTitleAndSure(false, true);
        dialogNotice.setContent("再发一条提醒消息提示朋友收款", true)
                .setRight("确定")
                .setLeft("取消")
                .setListener(new DialogDefault.IDialogListener() {
                    @Override
                    public void onSure() {
                        if (!TextUtils.isEmpty(tradeId)) {
                            PayEnvironment.getInstance().notifyReceive(tradeId);
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
        dialogNotice.show();
    }


}
