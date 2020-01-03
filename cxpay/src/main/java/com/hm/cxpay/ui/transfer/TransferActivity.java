package com.hm.cxpay.ui.transfer;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.hm.cxpay.R;
import com.hm.cxpay.base.BasePayActivity;
import com.hm.cxpay.bean.BankBean;
import com.hm.cxpay.bean.CxTransferBean;
import com.hm.cxpay.bean.SendResultBean;
import com.hm.cxpay.dailog.DialogBalanceNoEnough;
import com.hm.cxpay.dailog.DialogDefault;
import com.hm.cxpay.dailog.DialogErrorPassword;
import com.hm.cxpay.dailog.DialogInputTransferPassword;
import com.hm.cxpay.dailog.DialogSelectPayStyle;
import com.hm.cxpay.databinding.ActivityTransferBinding;
import com.hm.cxpay.eventbus.PayResultEvent;
import com.hm.cxpay.eventbus.TransferSuccessEvent;
import com.hm.cxpay.global.PayEnum;
import com.hm.cxpay.global.PayEnvironment;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.ui.bank.BankSettingActivity;
import com.hm.cxpay.ui.bank.BindBankActivity;
import com.hm.cxpay.ui.redenvelope.AdapterSelectPayStyle;
import com.hm.cxpay.utils.BankUtils;
import com.hm.cxpay.utils.UIUtils;
import com.jrmf360.tools.utils.ThreadUtil;

import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.hm.cxpay.global.PayConstants.MIN_AMOUNT;
import static com.hm.cxpay.global.PayConstants.TOTAL_TRANSFER_MAX_AMOUNT;
import static com.hm.cxpay.global.PayConstants.WAIT_TIME;

/**
 * @author Liszt
 * @date 2019/12/19
 * Description
 */
public class TransferActivity extends BasePayActivity {

    private ActivityTransferBinding ui;
    private DialogInputTransferPassword dialogPassword;
    private long toUid;
    private String name;
    private DialogSelectPayStyle dialogSelectPayStyle;
    private DialogErrorPassword dialogErrorPassword;
    private long money;
    private String avatar;
    boolean isSending = false;
    public final Handler handler = new Handler();
    public final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ToastUtil.show(getContext(), "转账失败");
            dismissLoadingDialog();
            finish();
        }
    };
    private CxTransferBean cxTransferBean;


    public static Intent newIntent(Context context, long uid, String name, String avatar) {
        Intent intent = new Intent(context, TransferActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("name", name);
        intent.putExtra("avatar", avatar);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_transfer);
        Intent intent = getIntent();
        toUid = intent.getLongExtra("uid", 0);
        name = intent.getStringExtra("name");
        avatar = intent.getStringExtra("avatar");
        initView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventPayResult(PayResultEvent event) {
        payFailed();
        if (cxTransferBean != null && event.getTradeId() == cxTransferBean.getTradeId()) {
            if (event.getResult() == PayEnum.EPayResult.SUCCESS) {
                eventTransferSuccess();
                toTransferResult(money);
            } else {
                ToastUtil.show(this, R.string.send_fail_note);
            }
        }
    }

    private void initView() {
        UIUtils.loadAvatar(avatar, ui.ivAvatar);
        ui.tvName.setText(name);

        ui.headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
            }
        });
        ui.tvTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String moneyTxt = ui.edMoney.getText().toString();
                money = UIUtils.getFen(moneyTxt);
                if (money > MIN_AMOUNT) {
                    if (BankUtils.isLooseEnough(money)) {
                        showInputPasswordDialog(money, PayEnum.EPayStyle.LOOSE, null);
                    } else {
                        BankBean bank = PayEnvironment.getInstance().getFirstBank();
                        if (bank != null) {
                            showInputPasswordDialog(money, PayEnum.EPayStyle.BANK, bank);
                        } else {
                            showBalanceNoEnoughDialog(money, PayEnvironment.getInstance().getUser().getBalance());
                        }
                    }
                }
            }
        });

        ui.edMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString().trim();
                long money = UIUtils.getFen(string);
                updateUI(money);
            }
        });
    }

    private void updateUI(long money) {
        if (money < MIN_AMOUNT) {
            ui.tvTransfer.setEnabled(false);
            ui.tvNotice.setVisibility(View.VISIBLE);
            ui.tvNotice.setText(getString(R.string.transfer_min_amount_notice));
        } else if (money > TOTAL_TRANSFER_MAX_AMOUNT) {
            ui.tvTransfer.setEnabled(false);
            ui.tvNotice.setVisibility(View.VISIBLE);
            ui.tvNotice.setText(getString(R.string.total_max_amount_notice));
        } else {
            ui.tvTransfer.setEnabled(true);
            ui.tvNotice.setVisibility(View.GONE);
        }
    }


    public void httpSendTransfer(String actionId, final long money, String psw, final long toUid, final String note, long banCardId) {
        isSending = true;
        showLoadingDialog();
        handler.postDelayed(runnable, WAIT_TIME);
        PayHttpUtils.getInstance().sendTransfer(actionId, money, psw, toUid, note, banCardId)
                .compose(RxSchedulers.<BaseResponse<SendResultBean>>compose())
                .compose(RxSchedulers.<BaseResponse<SendResultBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<SendResultBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<SendResultBean> baseResponse) {
                        if (baseResponse.isSuccess()) {
                            SendResultBean sendBean = baseResponse.getData();
                            if (sendBean != null) {
                                cxTransferBean = createTransferBean(sendBean, money, PayEnum.ETransferOpType.TRANS_SEND, note);
                                if (sendBean.getCode() == 1) {//成功\
                                    dismissLoadingDialog();
                                    isSending = false;
                                    eventTransferSuccess();
                                    ThreadUtil.getInstance().runMainThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            toTransferResult(money);
                                        }
                                    });
                                } else if (sendBean.getCode() == 2) {//失败
                                    payFailed();
                                    ToastUtil.show(getContext(), sendBean.getErrMsg());
                                } else if (sendBean.getCode() == 99) {//待处理

                                } else {
                                    payFailed();
                                    isSending = false;
                                    ToastUtil.show(getContext(), baseResponse.getMessage());
                                }
                            }
                        } else {
                            isSending = false;
                            ToastUtil.show(getContext(), baseResponse.getMessage());
                        }
                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        payFailed();
                        if (baseResponse.getCode() == -21000) {//密码错误
                            showPswErrorDialog();
                        } else if (baseResponse.getCode() == 40014) {//余额不足
                            showBalanceOfBankNoEnough();
                        } else {
                            ToastUtil.show(getContext(), baseResponse.getMessage());
                        }
                    }
                });
    }

    private void toTransferResult(long money) {
        Intent intent = TransferResultActivity.newIntent(TransferActivity.this, money, 1, name);
        startActivity(intent);
        finish();
    }


    //输入密码弹窗
    private void showInputPasswordDialog(final long money, int payStyle, BankBean bankBean) {
        dialogPassword = new DialogInputTransferPassword(this);
        dialogPassword.init(money, payStyle, bankBean);
        dialogPassword.setPswListener(new DialogInputTransferPassword.IPswListener() {
            @Override
            public void onCompleted(String psw, long bankCardId) {
                dialogPassword.dismiss();
                String note = ui.etDescription.getText().toString().trim();
                String actionId = UIUtils.getUUID();
                httpSendTransfer(actionId, money, psw, toUid, note, bankCardId);
            }

            @Override
            public void selectPayStyle() {
                showSelectPayStyleDialog();
            }
        });
        dialogPassword.show();
        showSoftKeyword(dialogPassword.getPswView());
    }

    private void showSelectPayStyleDialog() {
        dialogSelectPayStyle = new DialogSelectPayStyle(this, R.style.MyDialogTheme);
        BankBean selectBank = null;
        if (dialogPassword != null) {
            selectBank = dialogPassword.getSelectedBank();
        }
        dialogSelectPayStyle.bindData(PayEnvironment.getInstance().getBanks(), selectBank);
        dialogSelectPayStyle.setListener(new AdapterSelectPayStyle.ISelectPayStyleListener() {
            @Override
            public void onSelectPay(int style, BankBean bank) {
                dialogSelectPayStyle.dismiss();
                if (dialogPassword != null) {
                    dialogPassword.init(money, style, bank);
                    resetShowDialogPayPassword();
                }
            }

            @Override
            public void onAddBank() {
                dialogSelectPayStyle.dismiss();
                Intent intent = new Intent(TransferActivity.this, BindBankActivity.class);
                startActivity(intent);
            }

            @Override
            public void onBack() {
                resetShowDialogPayPassword();
            }
        });
        dialogSelectPayStyle.show();

    }

    private void showPswErrorDialog() {
        dialogErrorPassword = new DialogErrorPassword(this, R.style.MyDialogTheme);
        dialogErrorPassword.setListener(new DialogErrorPassword.IErrorPasswordListener() {
            @Override
            public void onForget() {

            }

            @Override
            public void onTry() {
                resetShowDialogPayPassword();
            }
        });
        dialogErrorPassword.show();
    }

    //重新显示输入密码弹窗
    private void resetShowDialogPayPassword() {
        if (dialogPassword != null) {
            dialogPassword.clearPsw();
            dialogPassword.show();
            showSoftKeyword(dialogPassword.getPswView());
        }
    }

    //银行卡余额不足弹窗
    public void showBalanceOfBankNoEnough() {
        DialogDefault dialogBankNoEnough = new DialogDefault(this);
        dialogBankNoEnough.setTitleAndSure(false, true)
                .setRight("换卡支付").setLeft("取消")
                .setTitle("转账失败")
                .setContent("银行卡可用余额不足，请核实后再试", true)
                .setListener(new DialogDefault.IDialogListener() {
                    @Override
                    public void onSure() {
                        List<BankBean> banks = PayEnvironment.getInstance().getBanks();
                        if (banks != null) {
                            if (banks.size() > 1) {
                                showSelectPayStyleDialog();
                            } else {
                                toBindBankActivity();
                            }
                        } else {
                            toBindBankActivity();
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
        dialogBankNoEnough.show();
    }

    /**
     * @param money   转账金额
     * @param balance 零钱余额
     * @discribe 零钱余额不足弹窗, 且未绑卡
     */
    public void showBalanceNoEnoughDialog(long money, long balance) {
        DialogBalanceNoEnough dialogBalanceNoEnough = new DialogBalanceNoEnough(this);
        dialogBalanceNoEnough.initMoney(money, balance);
        dialogBalanceNoEnough.setListener(new DialogBalanceNoEnough.IRechargeListener() {
            @Override
            public void onRecharge() {
                toBindBankActivity();
            }
        });
        dialogBalanceNoEnough.show();
    }

    public void toBindBankActivity() {
        startActivity(new Intent(TransferActivity.this, BankSettingActivity.class));
    }

    private void payFailed() {
        dismissLoadingDialog();
        if (isSending) {
            isSending = false;
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
        }
    }

    public CxTransferBean createTransferBean(SendResultBean bean, long money, @PayEnum.ETransferOpType int type, String info) {
        CxTransferBean transferBean = new CxTransferBean();
        transferBean.setUid(toUid);
        transferBean.setAmount(money);
        transferBean.setOpType(type);
        transferBean.setInfo(info);
        transferBean.setSign(bean.getSign());
        transferBean.setTradeId(bean.getTradeId());
        return transferBean;
    }

    public void eventTransferSuccess() {
        if (cxTransferBean != null) {
            EventBus.getDefault().post(new TransferSuccessEvent(cxTransferBean));
        }
        PayEnvironment.getInstance().notifyRefreshBalance();
    }


}
