package com.hm.cxpay.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hm.cxpay.R;
import com.hm.cxpay.base.BasePayActivity;
import com.hm.cxpay.bean.BankBean;
import com.hm.cxpay.bean.UserBean;
import com.hm.cxpay.controller.ControllerPaySetting;
import com.hm.cxpay.global.PayEnvironment;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.ui.bank.BankSettingActivity;
import com.hm.cxpay.ui.bank.BindBankActivity;
import com.hm.cxpay.ui.bill.BillDetailListActivity;
import com.hm.cxpay.ui.change.ChangeDetailListActivity;
import com.hm.cxpay.ui.identification.IdentificationInfoActivity;
import com.hm.cxpay.ui.payword.ManagePaywordActivity;
import com.hm.cxpay.ui.payword.SetPaywordActivity;
import com.hm.cxpay.ui.recharege.RechargeActivity;
import com.hm.cxpay.ui.withdraw.WithdrawActivity;
import com.hm.cxpay.utils.UIUtils;

import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.IntentUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.HeadView;

import java.util.List;

/**
 * 零钱首页
 */
public class LooseChangeActivity extends BasePayActivity {

    private ControllerPaySetting viewSettingOfPsw;
    private ControllerPaySetting viewMyCard;
    private ControllerPaySetting viewMyRedEnvelope;
    private ControllerPaySetting layoutChangeDetails;//零钱明细
    private ControllerPaySetting layoutAuthRealName;//实名认证

    private HeadView mHeadView;
    private TextView tvBalance;//余额
    private LinearLayout layoutRecharge;//充值
    private LinearLayout layoutWithdrawDeposit;//提现

    private Activity activity;

    private int myCardListSize = 0;//我的银行卡个数 (判断是否添加过银行卡)
//    private ControllerPaySetting viewAccountInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loose_change);
        activity = this;
        initView();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBankList();
        httpGetUserInfo();
    }

//    private void showAuthView(UserBean user) {
//        if (user == null) {
//            return;
//        }
//        if (user.getIsVerify() == 1) {//已经认证，隐藏
//            viewAccountInfo.setVisible(false);
//        } else {
//            viewAccountInfo.setVisible(true);
//        }
//    }


    @Override
    protected void onPause() {
        super.onPause();
        resumeEnabled();
    }

    private void resumeEnabled() {
        layoutRecharge.setEnabled(true);
        layoutWithdrawDeposit.setEnabled(true);
        layoutChangeDetails.setEnabled(true);
        layoutAuthRealName.setEnabled(true);
        viewMyRedEnvelope.setEnabled(true);
//        viewAccountInfo.setEnabled(true);
        viewMyCard.setEnabled(true);
        viewSettingOfPsw.setEnabled(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void initView() {
        mHeadView = findViewById(R.id.headView);
        tvBalance = findViewById(R.id.tv_money);
        layoutRecharge = findViewById(R.id.layout_recharge);
        layoutWithdrawDeposit = findViewById(R.id.layout_withdraw_deposit);
    }


    private void initEvent() {
        mHeadView.getActionbar().setChangeStyleBg();
        mHeadView.getAppBarLayout().setBackgroundResource(R.color.c_c85749);
        mHeadView.getActionbar().setTxtRight("账单");
        //标题栏事件
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                startActivity(new Intent(activity, BillDetailListActivity.class));
            }
        });
        //显示余额
        tvBalance.setText("¥ " + UIUtils.getYuan(Long.valueOf(PayEnvironment.getInstance().getUser().getBalance())));
        //充值
        layoutRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutRecharge.setEnabled(false);
                // 1 已设置支付密码 -> 允许跳转
                if (PayEnvironment.getInstance().getUser().getPayPwdStat() == 1) {
                    startActivity(new Intent(activity, RechargeActivity.class));
                } else {
                    //2 未设置支付密码 -> 需要先设置
                    showSetPaywordDialog();
                }
            }
        });
        //提现
        layoutWithdrawDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //1 已设置支付密码 -> 允许跳转
                if (PayEnvironment.getInstance().getUser().getPayPwdStat() == 1) {
                    //2 是否添加过银行卡
                    if (myCardListSize > 0) {
                        startActivity(new Intent(activity, WithdrawActivity.class));
                    } else {
                        showAddBankCardDialog();
                    }
                } else {
                    //未设置支付密码 -> 需要先设置
                    showSetPaywordDialog();
                }
            }
        });
        //零钱明细
        layoutChangeDetails = new ControllerPaySetting(findViewById(R.id.layout_change_details));
        layoutChangeDetails.init(R.mipmap.ic_change_details, R.string.change_details, "");
        layoutChangeDetails.setOnClickListener(new ControllerPaySetting.OnControllerClickListener() {
            @Override
            public void onClick() {
                layoutChangeDetails.setEnabled(false);
                startActivity(new Intent(activity, ChangeDetailListActivity.class));
            }
        });
        //红包明细
        viewMyRedEnvelope = new ControllerPaySetting(findViewById(R.id.viewMyRedEnvelope));
        viewMyRedEnvelope.init(R.mipmap.ic_red_packet_info, R.string.my_red_envelope, "");
        viewMyRedEnvelope.setOnClickListener(new ControllerPaySetting.OnControllerClickListener() {
            @Override
            public void onClick() {
                viewMyRedEnvelope.setEnabled(false);
                ARouter.getInstance().build("/app/redEnvelopeDetailsActivity").navigation();
//                ARouter.getInstance().build("/app/redpacketRecordActivity").navigation();
            }
        });
        //账户信息
//        viewAccountInfo = new ControllerPaySetting(findViewById(R.id.viewAccountInfo));
//        viewAccountInfo.init(R.mipmap.ic_account_info, R.string.account_info, "");
//        viewAccountInfo.setOnClickListener(new ControllerPaySetting.OnControllerClickListener() {
//            @Override
//            public void onClick() {
//                viewAccountInfo.setEnabled(false);
//                IntentUtil.gotoActivity(activity, IdentificationInfoActivity.class);
//            }
//        });
        //实名认证
        layoutAuthRealName = new ControllerPaySetting(findViewById(R.id.layout_auth_realname));
        layoutAuthRealName.init(R.mipmap.ic_auth_realname, R.string.auth_realname, "");
        layoutAuthRealName.setOnClickListener(new ControllerPaySetting.OnControllerClickListener() {
            @Override
            public void onClick() {
                layoutAuthRealName.setEnabled(false);
                //1 已经绑定手机
                if (PayEnvironment.getInstance().getUser().getPhoneBindStat() == 1) {
                    //TODO 还有一个认证信息展示界面未出
//                    ToastUtil.show(activity,"您已绑定手机号(暂时允许进入)");
//                    IntentUtil.gotoActivity(activity, BindPhoneNumActivity.class);
                    IntentUtil.gotoActivity(activity, IdentificationInfoActivity.class);

                } else {
                    //2 没有绑定手机
                    showBindPhoneNumDialog();
                }
            }
        });
        //我的银行卡
        viewMyCard = new ControllerPaySetting(findViewById(R.id.viewBankSetting));
        int count = 0;
        viewMyCard.init(R.mipmap.ic_bank_card, R.string.settings_of_bank, count + "张");
        viewMyCard.setOnClickListener(new ControllerPaySetting.OnControllerClickListener() {
            @Override
            public void onClick() {
                viewMyCard.setEnabled(false);
                //已设置支付密码 -> 允许跳转
                if (PayEnvironment.getInstance().getUser().getPayPwdStat() == 1) {
                    startActivity(new Intent(activity, BankSettingActivity.class));
                } else {
                    //未设置支付密码 -> 需要先设置
                    showSetPaywordDialog();
                }
            }
        });
        //支付密码管理
        viewSettingOfPsw = new ControllerPaySetting(findViewById(R.id.viewSettingOfPsw));
        viewSettingOfPsw.init(R.mipmap.ic_paypsw_manage, R.string.settings_of_psw, "");
        viewSettingOfPsw.setOnClickListener(new ControllerPaySetting.OnControllerClickListener() {
            @Override
            public void onClick() {
                // 1 已设置支付密码 -> 允许跳转
                viewSettingOfPsw.setEnabled(false);
                if (PayEnvironment.getInstance().getUser().getPayPwdStat() == 1) {
                    IntentUtil.gotoActivity(activity, ManagePaywordActivity.class);
                } else {
                    //2 未设置支付密码 -> 需要先设置
                    showSetPaywordDialog();
                }
            }
        });
    }

    /**
     * 请求->获取用户信息
     */
    private void httpGetUserInfo() {
        PayHttpUtils.getInstance().getUserInfo()
                .compose(RxSchedulers.<BaseResponse<UserBean>>compose())
                .compose(RxSchedulers.<BaseResponse<UserBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<UserBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<UserBean> baseResponse) {
                        if (baseResponse.isSuccess()) {
                            UserBean userBean = null;
                            if (baseResponse.getData() != null) {
                                userBean = baseResponse.getData();
                            } else {
                                userBean = new UserBean();
                            }
                            PayEnvironment.getInstance().setUser(userBean);
                            //刷新最新余额
//                            PayEnvironment.getInstance().getUser().setBalance(userBean.getBalance());
                            tvBalance.setText("¥ " + UIUtils.getYuan(Long.valueOf(userBean.getBalance())));
                        } else {
                            ToastUtil.show(context, baseResponse.getMessage());
                        }

                    }

                    @Override
                    public void onHandleError(BaseResponse<UserBean> baseResponse) {
                        super.onHandleError(baseResponse);
                        ToastUtil.show(context, baseResponse.getMessage());
                    }
                });
    }

    /**
     * 请求->绑定的银行卡列表
     * <p>
     * 备注：主要用于零钱首页更新"我的银行卡" 张数，暂时仅"充值、提现、我的银行卡"返回此界面后需要刷新
     */
    private void getBankList() {
        PayHttpUtils.getInstance().getBankList()
                .compose(RxSchedulers.<BaseResponse<List<BankBean>>>compose())
                .compose(RxSchedulers.<BaseResponse<List<BankBean>>>handleResult())
                .subscribe(new FGObserver<BaseResponse<List<BankBean>>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<List<BankBean>> baseResponse) {
                        List<BankBean> info = baseResponse.getData();
                        if (info != null) {
                            myCardListSize = info.size();
                        } else {
                            myCardListSize = 0;
                        }
                        viewMyCard.getRightTitle().setText(myCardListSize + "张");
                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        ToastUtil.show(activity, baseResponse.getMessage());
                    }
                });
    }


    /**
     * 检测到未设置支付密码弹框
     */
    private void showSetPaywordDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setCancelable(false);
        final AlertDialog dialog = dialogBuilder.create();
        //获取界面
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_set_payword, null);
        //初始化控件
        TextView tvSet = dialogView.findViewById(R.id.tv_set);
        TextView tvExit = dialogView.findViewById(R.id.tv_exit);
        //去设置
        tvSet.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(activity, SetPaywordActivity.class));

            }
        });
        //取消
        tvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                resumeEnabled();
            }
        });
        //展示界面
        dialog.show();
        //解决圆角shape背景无效问题
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //设置宽高
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = DensityUtil.dip2px(activity, 136);
        lp.width = DensityUtil.dip2px(activity, 277);
        dialog.getWindow().setAttributes(lp);
        dialog.setContentView(dialogView);
    }

    /**
     * 是否绑定手机号弹框
     */
    private void showBindPhoneNumDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setCancelable(false);
        final AlertDialog dialog = dialogBuilder.create();
        //获取界面
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_bind_phonenum, null);
        //初始化控件
        TextView tvBind = dialogView.findViewById(R.id.tv_bind);
        TextView tvExit = dialogView.findViewById(R.id.tv_exit);
        //去绑定
        tvBind.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(activity, BindPhoneNumActivity.class));

            }
        });
        //取消
        tvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                resumeEnabled();
            }
        });
        //展示界面
        dialog.show();
        //解决圆角shape背景无效问题
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //设置宽高
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = DensityUtil.dip2px(activity, 155);
        lp.width = DensityUtil.dip2px(activity, 277);
        dialog.getWindow().setAttributes(lp);
        dialog.setContentView(dialogView);
    }

    /**
     * 没有添加过银行卡弹框
     */
    private void showAddBankCardDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setCancelable(false);
        final AlertDialog dialog = dialogBuilder.create();
        //获取界面
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_bind_bankcard, null);
        //初始化控件
        TextView tvAdd = dialogView.findViewById(R.id.tv_add);
        TextView tvExit = dialogView.findViewById(R.id.tv_exit);
        //去添加银行卡
        tvAdd.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(activity, BindBankActivity.class));
            }
        });
        //取消
        tvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //展示界面
        dialog.show();
        //解决圆角shape背景无效问题
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //设置宽高
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = DensityUtil.dip2px(activity, 136);
        lp.width = DensityUtil.dip2px(activity, 277);
        dialog.getWindow().setAttributes(lp);
        dialog.setContentView(dialogView);
    }


}
