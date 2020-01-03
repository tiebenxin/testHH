package com.hm.cxpay.global;

/**
 * @author Liszt
 * @date 2019/12/4
 * Description 支付模块常量
 */
public class PayConstants {
    public static final long MAX_AMOUNT = 200 * 100;//单个红包最大金额 200元
    public static final long MIN_AMOUNT = 1;//单个红包最大金额 0.01元
    public static final long TOTAL_MAX_AMOUNT = 5000 * 100;//单次红包最大金额
    public static final long TOTAL_TRANSFER_MAX_AMOUNT = 1000 * 100;//单次转账最大金额
    public static final int WAIT_TIME = 30 * 1000;//支付等待时间，30s


}
