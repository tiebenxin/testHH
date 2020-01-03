package com.hm.cxpay.utils;

import android.util.Base64;

import com.hm.cxpay.global.PayEnvironment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @类名：金融相关支付/签名工具类
 * @Date：2019/12/27
 * @by zjy
 * @备注：
 *          1 生成随机数
 *          2 HmacSHA256签名+ Base64编码+ URL编码
 */
public class PayUtils {

    /**
     * 生成随机数 (100)
     *
     * @return
     */
    public static int getRandomNumber() {
        Random rd = new Random();
        return rd.nextInt(100);
    }

    /**
     * 先使用HmacSHA256签名，再使用Base64编码,最后进行URL编码
     * signatureReqStr : 待加密data
     * secretKey : 密钥
     */
    public static String getSignature(String signatureReqStr, String secretKey) {
        Mac sha256_HMAC;
        String result = "";
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            result = Base64.encodeToString(sha256_HMAC.doFinal(signatureReqStr.getBytes()),Base64.DEFAULT);
            result = URLEncoder.encode(result,"UTF-8");
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
