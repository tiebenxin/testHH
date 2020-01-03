package com.hm.cxpay.rx.api;

import com.hm.cxpay.rx.FXRequestManager;
import com.hm.cxpay.rx.FXRxSubscriberHelper;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.POST;

/**
 * @author Liszt
 * @date 2019/11/27
 * Description 支付模块api
 */
public class PayApi {
    public interface Api {
        //用户认证接口,idNumber 身份证号码；idType 目前只有身份证，传1即可；realName 真实姓名
        @POST("/user/real_name_auth")
        Observable<BaseResponse> authUserInfo(@Field("idNumber") String idNumber, @Field("idType") int idType, @Field("realName") String name);
    }

    private Api api;

    public PayApi() {
        this.api = FXRequestManager.getRequest(Api.class);
    }


    public void authUserInfo(String idNum, String realName, FXRxSubscriberHelper<BaseResponse> subscriberHelper) {
        api.authUserInfo(idNum,1,realName)
                .compose(RxSchedulers.handleResult())
                .compose(RxSchedulers.rxSchedulerHelper())
                .subscribe();

    }


}
