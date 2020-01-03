package com.hm.cxpay.global;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Liszt
 * @date 2019/12/3
 * Description
 */
public class PayEnum {

    //红包支付方式
    @IntDef({EPayStyle.LOOSE, EPayStyle.BANK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EPayStyle {
        int LOOSE = 0;//零钱支付
        int BANK = 1;//银行卡支付
    }

    //红包类型
    @IntDef({ERedEnvelopeType.NORMAL, ERedEnvelopeType.LUCK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ERedEnvelopeType {
        int NORMAL = 0;//普通红包
        int LUCK = 1;//拼手气
    }

    //红包发送结果
    @IntDef({ESendResult.SUCCESS, ESendResult.FAIL, ESendResult.PENDING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ESendResult {
        int SUCCESS = 0;//成功
        int FAIL = 1;//失败
        int PENDING = 2;//待处理
    }

    //支付结果
    @IntDef({EPayResult.SUCCESS, EPayResult.FAIL, EPayResult.REFUND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EPayResult {
        int SUCCESS = 0;//成功
        int FAIL = 1;//失败
        int REFUND = 2;//成功后退款了
    }

    //抢红包结果
    @IntDef({EPayResult.SUCCESS, EPayResult.FAIL, EPayResult.REFUND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EEnvelopeOpenResult {
        int SUCCESS = 0;//成功
        int FAIL = 1;//失败
        int REFUND = 2;//成功后退款了
    }

    //红包状态
    @IntDef({EEnvelopeStatus.NORMAL, EEnvelopeStatus.RECEIVED, EEnvelopeStatus.PAST, EEnvelopeStatus.RECEIVED_FINISHED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EEnvelopeStatus {
        int NORMAL = 0;//正常
        int RECEIVED = 1;//已经领取
        int PAST = 2;//过期
        int RECEIVED_FINISHED = 3;//已领完,自己未领导
    }

    //转账操作type
    @IntDef({ETransferOpType.TRANS_SEND, ETransferOpType.TRANS_RECEIVE, ETransferOpType.TRANS_REJECT, ETransferOpType.TRANS_PAST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ETransferOpType {
        int TRANS_SEND = 0;//发起转账
        int TRANS_RECEIVE = 1;//接收转账
        int TRANS_REJECT = 2;//退还转账


        int TRANS_PAST = 100;//过期
    }

    //转账状态
    @IntDef({ETransferStatus.NORMAL, ETransferStatus.RECEIVED, ETransferStatus.REJECT, ETransferStatus.PAST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ETransferStatus {
        int NORMAL = 0;//正常
        int RECEIVED = 1;//已经领取
        int REJECT = 2;//拒收
        int PAST = 3;//过期
    }

}
