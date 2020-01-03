package com.yanlong.im.pay.action;


import com.yanlong.im.pay.bean.SignatureBean;
import com.yanlong.im.pay.server.PayServer;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.NetUtil;

/***
 * @author jyj
 * @date 2016/12/20
 */
public class PayAction {
    private PayServer server;

    public PayAction() {
        server = NetUtil.getNet().create(PayServer.class);
    }
    //以下是演示
    /*public void login( Long phone, String pwd,CallBack<ReturnBean<TokenBean>> callback) {

        LoginBean bean = new LoginBean();
        bean.setPassword(pwd);
        bean.setPhone(phone);
        NetUtil.getNet().exec(server.login(bean), callback);
    }*/

    public void SignatureBean(CallBack<ReturnBean<SignatureBean>> callback){
        NetUtil.getNet().exec(server.getSignature(), callback);
    }

}

