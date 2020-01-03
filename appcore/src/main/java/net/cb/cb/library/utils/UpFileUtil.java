package net.cb.cb.library.utils;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.callback.OSSRetryCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/***
 * 文件上传备用
 * @author jyj
 * @date 2016/12/20
 */
public class UpFileUtil {

    private final String TAG = "UpFileUtil";


    private static UpFileUtil instance;


    //  private final String P_STSSERVER = "http://sts.aliyuncs.com";

    //   private final String P_BUCKETNAME = "e7-test";
    //  private final String P_ENDPOINT = "http://oss-cn-beijing.aliyuncs.com";
    //  private static final String OSS_ACCESS_KEY_ID = "STS.NK9H2WVQ7m1omvBj6rvW7pBJd";
    //  private static final String OSS_ACCESS_KEY_SECRET = "C69qyuu4y9YNtsEkNTXFo9yvrGdySJxnpfxPzhAEakQx";
    //  private static final String OSS_ACCESS_KEY_TOKEN = "CAIShgJ1q6Ft5B2yfSjIr4iMA4jju44W2vOEb1DzjjYnetgbn4fhhjz2IH1IeHVgB+wcsP4xn2BV7PgflqZiQplBQkrLKMp1q4ha6h/5v0UfTwrwv9I+k5SANTW5OXyShb3vAYjQSNfaZY3aCTTtnTNyxr3XbCirW0ffX7SClZ9gaKZ4PGS/diEURq0VRG1YpdQdKGHaONu0LxfumRCwNkdzvRdmgm4Njsbay8aHuB3Flw+4mK1H5aaJe8D9NJk9Yc8lCobph7YvJpCsinAAt0J4k45tl7FB9Dv9udWQPkJc+R3uMZCPr4EzcF8nOvJkQfAf/KWmy6Bi2uvIjML51hJJLTuOxugq6A7JGoABIuPFVFt2gARCZTZlkzkCz8h9bcYCqeqK7wWNrFz7Ur25D7hjk33tLJ6LvZNIkRZWr60NdzneF8pfiT1bj9vvSVlz483HjRVIbkccVGqacxoSWc7+T0GR8vtC6GrBbN8UFq3IjZcvknoIJBvUsUlaWxQP8BCuxFxU7HelQ1wyCv4=";

    private OSS oss;

    private SimpleDateFormat simpleDateFormat;

    public UpFileUtil() {

    }

    public static UpFileUtil getInstance() {

        if (instance == null) {

            if (instance == null) {

                return new UpFileUtil();

            }

        }

        return instance;

    }

    private void getOSs(Context context, String keyid, String secret, String token, String endpoint) {


//该配置类如果不设置，会有默认配置，具体可看该类

        ClientConfiguration conf = new ClientConfiguration();

        conf.setConnectionTimeout(15 * 1000);// 连接超时，默认15秒

        conf.setSocketTimeout(15 * 1000);// socket超时，默认15秒

        conf.setMaxConcurrentRequest(5);// 最大并发请求数，默认5个

        conf.setMaxErrorRetry(2);// 失败后最大重试次数，默认2次

        //推荐使用OSSAuthCredentialsProvider。token过期可以及时更新

    /*    OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider(P_STSSERVER);

        oss = new OSSClient(context, P_ENDPOINT, credentialProvider);*/


        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(keyid, secret, token);

        oss = new OSSClient(context, endpoint, credentialProvider);

        if (simpleDateFormat == null) {

            simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        }

    }

    /**
     * 上传图片 上传文件
     *
     * @param context       application上下文对象
     * @param ossUpCallback 成功的回调
     * @param imgPath       图片的本地路径
     */

    public void upFile(String path, final Context context, String keyid, String secret, String token, String endpoint, final String btName, final UpFileUtil.OssUpCallback ossUpCallback, String imgPath, byte[] imgbyte) {

        getOSs(context, keyid, secret, token, endpoint);

        final Date data = new Date();
        String endEx = "";
        if (imgPath != null) {
            int sEx = imgPath.lastIndexOf(".");

            if (sEx > 0) {
                endEx = imgPath.substring(sEx);
            }
        }
        final String img_name = UUID.randomUUID().toString() + endEx;

        //data.setTime(System.currentTimeMillis());
        //"Android/" + simpleDateFormat.format(data) + "/"
        final String objkey =path  + img_name;

        PutObjectRequest putObjectRequest;

        if (StringUtil.isNotNull(imgPath)) {
            putObjectRequest = new PutObjectRequest(btName, objkey, imgPath);
        } else {
            putObjectRequest = new PutObjectRequest(btName, objkey, imgbyte);
        }

        putObjectRequest.setRetryCallback(new OSSRetryCallback() {
            @Override
            public void onRetryCallback() {
                Log.v(TAG,"重试回调------------------>");
            }
        });


        putObjectRequest.setProgressCallback(new OSSProgressCallback() {

            @Override
            public void onProgress(Object request, long currentSize, long totalSize) {
                ossUpCallback.inProgress(currentSize, totalSize);
            }


        });

        //6.11 图片上传引起界面刷新
        oss.asyncPutObject(putObjectRequest, new OSSCompletedCallback() {

            @Override
            public void onSuccess(OSSRequest request, OSSResult result) {
                ossUpCallback.success(oss.presignPublicObjectURL(btName, objkey));
            }

            @Override
            public void onFailure(OSSRequest request, ClientException clientException, ServiceException serviceException) {
                ossUpCallback.fail();
                LogUtil.getLog().e("uplog", "---->上传异常:"+clientException.getMessage()+"\n" + serviceException.getRawMessage());
                try{
                    ToastUtil.show(context, "上传失败");
                }catch (Exception e){

                }


            }


        });

    }


    public interface OssUpCallback {

        void success(String url);

        void fail();

        void inProgress(long progress, long zong);

    }

}

