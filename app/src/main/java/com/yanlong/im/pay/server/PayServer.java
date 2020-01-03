package com.yanlong.im.pay.server;

import com.yanlong.im.pay.bean.SignatureBean;
import net.cb.cb.library.bean.ReturnBean;
import retrofit2.Call;
import retrofit2.http.POST;


public interface PayServer {


    @POST("/red-envelope/get-signature")
    Call<ReturnBean<SignatureBean>> getSignature();

}
