package com.hm.cxpay.net;

import com.hm.cxpay.BuildConfig;
import com.hm.cxpay.net.converter.MGsonConverterFactory;
import com.hm.cxpay.rx.PayHostUtils;
import com.hm.cxpay.rx.interceptor.CommonInterceptor;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Liszt
 * @date 2019/11/28
 * Description http
 */
public class HttpChannel {
    private static final long DEFAULT_TIME_OUT = 12;
    OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private static CompositeDisposable compositeDisposable;
    private final PayService payService;
    private static HttpChannel instance;

    public static HttpChannel getInstance() {
        if (instance == null) {
            instance = new HttpChannel();
        }
        return instance;
    }

    private HttpChannel() {
        compositeDisposable = new CompositeDisposable();
        if (okHttpClient == null) {
            okHttpClient = getOkHttpClient();
        }
        // 初始化Retrofit
        retrofit = createRetrofit();
        payService = retrofit.create(PayService.class);
    }

    private Retrofit createRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(PayHostUtils.getHttpsUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // 支持RxJava
                    .client(okHttpClient) // 打印请求参数
                    .build();
        }
        return retrofit;
    }

    private OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//        httpLoggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        // 定制OkHttp
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        //支持HTTPS请求，跳过证书验证
        builder.sslSocketFactory(createSSLSocketFactory());
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        builder.addInterceptor(httpLoggingInterceptor);
        CommonInterceptor commonInterceptor = new CommonInterceptor();
        builder.addInterceptor(commonInterceptor);
        if (BuildConfig.DEBUG) {
//            builder.addNetworkInterceptor(new StethoInterceptor());
        }
        return builder.build();
    }

    public PayService getPayService() {
        return payService;
    }

    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     */
    public SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    /**
     * 用于信任所有证书
     */
    class TrustAllCerts implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }


}
