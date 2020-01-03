package com.hm.cxpay.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.text.DecimalFormat;
import java.util.UUID;

/**
 * @author Liszt
 * @date 2019/11/29
 * Description
 */
public class UIUtils {

    public static Drawable getDrawable(Context context, int drawableId) {
        return ContextCompat.getDrawable(context, drawableId);
    }

    //分 转为 String（元）
    public static String getYuan(long amt) {
        if (amt == 0) {
            return "0.00";
        } else if (amt > 0 && amt < 100) {
            double money = (amt * 1.00) / 100;
            return money + "";
        } else {
            double money = (amt * 1.00) / 100;
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(money);
        }
    }

    public static String getYuan(String amt) {
        long amo = 0;
        if (!TextUtils.isEmpty(amt)) {
            try {
                amo = Long.parseLong(amt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (amo == 0) {
            return "0.00";
        } else if (amo > 0 && amo < 100) {
            double money = (amo * 1.00) / 100;
            return money + "";
        } else {
            double money = (amo * 1.00) / 100;
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(money);
        }
    }

    //String 转为 分
    public static long getFen(String money) {
        long fen = 0;
        if (TextUtils.isEmpty(money)) {
            return fen;
        }
        try {
            double m = Double.parseDouble(money);
            fen = (long) (m * 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fen;

    }

    //红包文案
    public static String getRedEnvelopeContent(EditText et) {
        String note = et.getText().toString().trim();
        if (TextUtils.isEmpty(note)) {
            note = "恭喜发财，大吉大利";
        }
        return note;
    }

    //获取红包个数
    public static int getRedEnvelopeCount(String count) {
        int c = 0;
        if (TextUtils.isEmpty(count)) {
            return c;
        }
        try {
            if (!TextUtils.isEmpty(count)) {
                c = Integer.parseInt(count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

    //获取红包个数
    public static int getRedEnvelopeCount(Editable et) {
        int c = 0;
        if (et == null) {
            return c;
        }
        String count = et.toString().trim();
        try {
            if (!TextUtils.isEmpty(count)) {
                c = Integer.parseInt(count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString()/*.replace("-", "")*/;
    }

    //加载头像
    public static void loadAvatar(String avatar, ImageView ivAvatar) {
        if (ivAvatar == null) {
            return;
        }
        RoundedCorners roundedCorners = new RoundedCorners(5);
        RequestOptions mRequestOptions = RequestOptions.bitmapTransform(roundedCorners)
                .error(net.cb.cb.library.R.mipmap.ic_info_head)
                .placeholder(net.cb.cb.library.R.mipmap.ic_info_head)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .skipMemoryCache(false);
        Glide.with(ivAvatar.getContext()).load(avatar).apply(mRequestOptions).into(ivAvatar);
    }

    public static long getTradeId(String trade) {
        long tradeId = 0;
        if (!TextUtils.isEmpty(trade)) {
            try {
                tradeId = Long.parseLong(trade);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return tradeId;
    }

}
