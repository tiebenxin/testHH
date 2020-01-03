package com.hm.cxpay.global;

import android.content.Context;

import com.hm.cxpay.bean.UserBean;
import com.hm.cxpay.bean.BankBean;
import com.hm.cxpay.eventbus.NoticeReceiveEvent;
import com.hm.cxpay.eventbus.RefreshBalanceEvent;

import net.cb.cb.library.bean.CanStampEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author Liszt
 * @date 2019/11/29
 * Description 支付环境：token,context, 用户信息
 */
public class PayEnvironment {
    private static PayEnvironment INSTANCE;
    private UserBean user;
    private String token;
    private Context context;
    private List<BankBean> banks;//绑定银行卡
    private String phone;//用户手机号
    private String nick;//用户昵称

    public static PayEnvironment getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PayEnvironment();
        }
        return INSTANCE;
    }


    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<BankBean> getBanks() {
        return banks;
    }

    public void setBanks(List<BankBean> banks) {
        this.banks = banks;
    }

    //获取默认第一顺位支付银行卡
    public BankBean getFirstBank() {
        if (banks != null && banks.size() > 0) {
            return banks.get(0);
        }
        return null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    //账号退出的时候，需要清除缓存
    public void clear() {
        user = null;
        banks = null;
        token = null;
        phone = null;
        nick = null;
    }

    //通知刷新余额，发出红包，拆红包成功，转账成功，都需要及时刷新
    public void notifyRefreshBalance() {
        EventBus.getDefault().post(new RefreshBalanceEvent());
    }

    //通知更改是否能显示戳一戳，发红包，充值，提现 均不能显示戳一戳
    public void notifyStampUpdate(boolean canStamp) {
        EventBus.getDefault().post(new CanStampEvent(canStamp));
    }

    //提醒对方收款
    public void notifyReceive(String rid) {
        EventBus.getDefault().post(new NoticeReceiveEvent(rid));
    }

}
