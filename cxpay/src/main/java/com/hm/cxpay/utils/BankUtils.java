package com.hm.cxpay.utils;

import android.graphics.drawable.Drawable;

import com.hm.cxpay.R;
import com.hm.cxpay.bean.UserBean;
import com.hm.cxpay.global.PayEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Liszt
 * @date 2019/11/30
 * Description
 */
public class BankUtils {
    private static Map<String, Drawable> smallBankIconMap = new HashMap<>();//小图
    private static Map<String, Drawable> bigBankIconMap = new HashMap<>();//大图

//    static {
//        smallBankIconMap.put("北京银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_beijing_small));
//        smallBankIconMap.put("渤海银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_bohai_small));
//        smallBankIconMap.put("工商银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_gongshang_small));
//        smallBankIconMap.put("光大银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_guangda_small));
//        smallBankIconMap.put("广发银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_guangfa_small));
//        smallBankIconMap.put("恒丰银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_hengfeng_small));
//        smallBankIconMap.put("华夏银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_huaxia_small));
//        smallBankIconMap.put("江苏银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_jiangsu_small));
//        smallBankIconMap.put("民生银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_minsheng_small));
//        smallBankIconMap.put("南京银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_nanjing_small));
//        smallBankIconMap.put("农业银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_nongye_small));
//        smallBankIconMap.put("平安银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_pingan_small));
//        smallBankIconMap.put("浦发银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_pufa_small));
//        smallBankIconMap.put("上海银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_shanghai_small));
//        smallBankIconMap.put("兴业银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_xingye_small));
//        smallBankIconMap.put("邮政银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_youzheng_small));
//        smallBankIconMap.put("长沙银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_changsha_small));
//        smallBankIconMap.put("招商银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_zhaoshang_small));
//        smallBankIconMap.put("浙商银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_zheshang_small));
//        smallBankIconMap.put("中国人民银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_renming_small));
//        smallBankIconMap.put("中信银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_zhongxin_small));
//
//        //无资源
//        smallBankIconMap.put("中国银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_gongshang_small));
//        smallBankIconMap.put("建设银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_gongshang_small));
//        smallBankIconMap.put("交通银行", UIUtils.getDrawable(PayEnvironment.getInstance().getContext(), R.mipmap.ic_gongshang_small));
//    }

    public static Drawable getBankIcon(String bankName) {
        return smallBankIconMap.get(bankName);
    }


    //是否默认支付 零钱支付余额足够
    public static boolean isLooseEnough(long money) {
        UserBean userBean = PayEnvironment.getInstance().getUser();
        if (userBean != null && userBean.getBalance() > money) {
            return true;
        }
        return false;
    }
}
