package com.hm.cxpay.rx;

import android.text.TextUtils;

import net.cb.cb.library.AppConfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * 证书provider
 * Created by zm on 2018/4/11.
 */

public class CertificateProvider {

    /**
     * 根据URL的地址来判断证书的加载
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static InputStream[] getCertificateStreams(String url) throws IOException {
        String cer_name = null;
        if (url.startsWith("")) {
            cer_name = "";
        }

        if (!TextUtils.isEmpty(cer_name)) {
            return new InputStream[]{AppConfig.getContext().getAssets().open(cer_name)};
        } else {
            return null;
        }
    }
}
