package net.cb.cb.library.utils;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.bean.AliObsConfigBean;
import net.cb.cb.library.bean.ReturnBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Response;

/***
 * @author jyj
 * @date 2016/12/20
 */
public class UpFileAction {
    public static enum PATH {
        HEAD, HEAD_GROUP, COMPLAINT, FEEDBACK, IMG, VOICE, HEAD_GROUP_CHANGE, VIDEO
    }

    private UpFileServer server;

    public UpFileAction() {
        server = NetUtil.getNet().create(UpFileServer.class);
    }


    /*    public void haweiObs(CallBack<ReturnBean<HuaweiObsConfigBean>> callback) {
            NetUtil.getNet().exec(
                    server.haweiObs()
                    , callback);
        }*/

    /***
     * 文件上传
     * @param context
     * @param callback
     * @param filePath
     */
    public void upFile(PATH type, Context context, UpFileUtil.OssUpCallback callback, String filePath) {
        upFile(type, context, callback, filePath, null);
    }

    public void upFile(String id, PATH type, Context context, UpFileUtil.OssUpCallback callback, String filePath) {
        upFile(type, context, callback, filePath, null, id);
    }

    public void upFile(PATH type, Context context, UpFileUtil.OssUpCallback callback, byte[] fileByte) {
        upFile(type, context, callback, null, fileByte);
    }

    public String getPath(PATH type, String id) {
        Date data = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String pt = "";
        switch (type) {
            case IMG:
                data.setTime(System.currentTimeMillis());
//                pt = AppConfig.UP_PATH + "/image/android/" + simpleDateFormat.format(data) + "/";
                pt = AppConfig.UP_PATH + "/image/android/" + simpleDateFormat.format(data) + "/";
                break;
            case HEAD:
                pt = AppConfig.UP_PATH + "/avatar/android/" + id;
                break;
            case HEAD_GROUP:
                pt = AppConfig.UP_PATH + "/avatar/android/" + id;
                break;
            case COMPLAINT:
                pt = AppConfig.UP_PATH + "/misc/complaint/";
                break;
            case FEEDBACK:
                pt = AppConfig.UP_PATH + "/misc/feedback/";
                break;
            case VOICE:
                pt = AppConfig.UP_PATH + "/voice/android/" + simpleDateFormat.format(data) + "/";
                break;
            case HEAD_GROUP_CHANGE:
                pt = AppConfig.UP_PATH + "/avatar/android/" + id;
                break;
            case VIDEO:
                pt = AppConfig.UP_PATH + "/video/android/" + simpleDateFormat.format(data) + "/";
                break;
            default:
                data.setTime(System.currentTimeMillis());
                pt = "/" + AppConfig.UP_PATH + "/android/";
                break;

        }
        return pt;
    }

    private Long startTime = 0L;

    private void upFile(final PATH type, final Context context, final UpFileUtil.OssUpCallback callback, final String filePath, final byte[] fileByte) {
        startTime = SystemClock.currentThreadTimeMillis();
        NetUtil.getNet().exec(
                server.aliObs()
                , new CallBack<ReturnBean<AliObsConfigBean>>() {
                    @Override
                    public void onResponse(Call<ReturnBean<AliObsConfigBean>> call, Response<ReturnBean<AliObsConfigBean>> response) {
                        if (response.body() == null) {
                            callback.fail();
                            return;
                        }
                        if (response.body().isOk()) {
                            final AliObsConfigBean configBean = response.body().getData();
                            if (!StringUtil.isNotNull(configBean.getSecurityToken())) {

                                callback.fail();
                                return;
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    long timeCost = SystemClock.currentThreadTimeMillis() - startTime;
                                    String endpoint;

//                                    String textToken="CAISmQJ1q6Ft5B2yfSjIr4nRCOvagOxqwLPZMGfB3TIGb9trm5TGuzz2IHtLfXhvAu8Zs/oyn29Z5/sflqZiQplBQkrLKMp1q4ha6h/51G8UT3bwv9I+k5SANTW5OXyShb3vAYjQSNfaZY3aCTTtnTNyxr3XbCirW0ffX7SClZ9gaKZ4PGS/diEURq0VRG1YpdQdKGHaONu0LxfumRCwNkdzvRdmgm4Njsbay8aHuB3Flw+4mK1H5aaJe8j7NZcyZcgvC4rsg7UrL5CsinAAt0J4k45tl7FB9Dv9udWQPkJc+R3uMZCPqYI2fVAiOfdnRfMf86mtyKBiyeXXlpXqzRFWJv1SUCnZS42mzdHNBOSzLNE9eKYM8cVEal1OXRqAAakBtm9ZuHW+cnfVxK4PJgmkPwBpMXLZ99oYyk+5E8jbZ4ArgAtdawN2i/syq8GrlHbVwOkvgeeF+nesQdgbKb86a7ZTHsawGZjzvi+xN6FlQwXMw2EA1tb/Wokcz3+EUxE3RLt6CuQ7PNxk65mvIgWqiyFLUozRV3sAUbElSds+";
//                                    UpFileUtil.getInstance().upFile(getPath(type), context, configBean.getAccessKeyId(),
//                                            configBean.getAccessKeySecret(), "", configBean.getEndpoint(),
//                                            configBean.getBucket(), callback, filePath, fileByte);
                                    if (PATH.VIDEO == type) {
                                        endpoint = configBean.getCdnEndpoint();
                                    } else {
                                        endpoint = configBean.getEndpoint();
                                    }
                                    UpFileUtil.getInstance().upFile(getPath(type, ""), context, configBean.getAccessKeyId(),
                                            configBean.getAccessKeySecret(), configBean.getSecurityToken(), endpoint,
                                            configBean.getBucket(), callback, filePath, fileByte);

                                    UpLoadUtils.getInstance().upLoadLog(timeCost + "--------" + configBean.toString());
                                }
                            }).start();
                        } else {
                            ToastUtil.show(context, "上传失败");
                        }


                    }

                    @Override
                    public void onFailure(Call<ReturnBean<AliObsConfigBean>> call, Throwable t) {
                        super.onFailure(call, t);
                        callback.fail();
                        long timeCost = SystemClock.currentThreadTimeMillis() - startTime;
                        UpLoadUtils.getInstance().upLoadLog(timeCost + "--------失败" + call.request().body().toString());
                    }
                });


    }


    private void upFile(final PATH type, final Context context, final UpFileUtil.OssUpCallback callback, final String filePath, final byte[] fileByte, final String id) {
        startTime = SystemClock.currentThreadTimeMillis();
        NetUtil.getNet().exec(
                server.aliObs()
                , new CallBack<ReturnBean<AliObsConfigBean>>() {
                    @Override
                    public void onResponse(Call<ReturnBean<AliObsConfigBean>> call, Response<ReturnBean<AliObsConfigBean>> response) {
                        if (response.body() == null) {
                            callback.fail();
                            return;
                        }
                        if (response.body().isOk()) {
                            final AliObsConfigBean configBean = response.body().getData();
                            if (!StringUtil.isNotNull(configBean.getSecurityToken())) {

                                callback.fail();
                                return;
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    long timeCost = SystemClock.currentThreadTimeMillis() - startTime;
                                    String endpoint;
//                                    String textToken="CAISmQJ1q6Ft5B2yfSjIr4nRCOvagOxqwLPZMGfB3TIGb9trm5TGuzz2IHtLfXhvAu8Zs/oyn29Z5/sflqZiQplBQkrLKMp1q4ha6h/51G8UT3bwv9I+k5SANTW5OXyShb3vAYjQSNfaZY3aCTTtnTNyxr3XbCirW0ffX7SClZ9gaKZ4PGS/diEURq0VRG1YpdQdKGHaONu0LxfumRCwNkdzvRdmgm4Njsbay8aHuB3Flw+4mK1H5aaJe8j7NZcyZcgvC4rsg7UrL5CsinAAt0J4k45tl7FB9Dv9udWQPkJc+R3uMZCPqYI2fVAiOfdnRfMf86mtyKBiyeXXlpXqzRFWJv1SUCnZS42mzdHNBOSzLNE9eKYM8cVEal1OXRqAAakBtm9ZuHW+cnfVxK4PJgmkPwBpMXLZ99oYyk+5E8jbZ4ArgAtdawN2i/syq8GrlHbVwOkvgeeF+nesQdgbKb86a7ZTHsawGZjzvi+xN6FlQwXMw2EA1tb/Wokcz3+EUxE3RLt6CuQ7PNxk65mvIgWqiyFLUozRV3sAUbElSds+";
//                                    UpFileUtil.getInstance().upFile(getPath(type), context, configBean.getAccessKeyId(),
//                                            configBean.getAccessKeySecret(), "", configBean.getEndpoint(),
//                                            configBean.getBucket(), callback, filePath, fileByte);

                                    if (PATH.VIDEO == type) {
                                        endpoint = configBean.getCdnEndpoint();
                                    } else {
                                        endpoint = configBean.getEndpoint();
                                    }
                                    UpFileUtil.getInstance().upFile(getPath(type, id), context, configBean.getAccessKeyId(),
                                            configBean.getAccessKeySecret(), configBean.getSecurityToken(), endpoint,
                                            configBean.getBucket(), callback, filePath, fileByte);

                                    UpLoadUtils.getInstance().upLoadLog(timeCost + "--------" + configBean.toString());
                                }
                            }).start();
                        } else {
                            ToastUtil.show(context, "上传失败");
                        }


                    }

                    @Override
                    public void onFailure(Call<ReturnBean<AliObsConfigBean>> call, Throwable t) {
                        super.onFailure(call, t);
                        callback.fail();
                        long timeCost = SystemClock.currentThreadTimeMillis() - startTime;
                        UpLoadUtils.getInstance().upLoadLog(timeCost + "--------失败" + call.request().body().toString());
                    }
                });


    }


    CountDownLatch signal;

    public void upFileSyn(final PATH type, final Context context, final UpFileUtil.OssUpCallback callback, String filePath) {

        if (filePath.startsWith("file://")) {
            filePath = filePath.replace("file://", "");
        }
        final String filep = filePath;
        signal = new CountDownLatch(1);


        NetUtil.getNet().exec(
                server.aliObs()
                , new CallBack<ReturnBean<AliObsConfigBean>>() {
                    @Override
                    public void onResponse(Call<ReturnBean<AliObsConfigBean>> call, final Response<ReturnBean<AliObsConfigBean>> response) {
                        Log.d("cc", "upFileSyn: onResponse");
                        if (response.body() == null) {
                            callback.fail();
                            signal.countDown();
                            return;
                        }
                        if (response.body().isOk()) {
                            final AliObsConfigBean configBean = response.body().getData();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String endpoint;
                                    if (PATH.VIDEO == type) {
                                        endpoint = configBean.getCdnEndpoint();
                                    } else {
                                        endpoint = configBean.getEndpoint();
                                    }
                                    UpFileUtil.getInstance().upFile(getPath(type, ""), context, configBean.getAccessKeyId(),
                                            configBean.getAccessKeySecret(), configBean.getSecurityToken(), endpoint,
                                            configBean.getBucket(), new UpFileUtil.OssUpCallback() {

                                                @Override
                                                public void success(String url) {
                                                    Log.d("cc", "upFileSyn: success");
                                                    signal.countDown();
                                                    callback.success(url);
                                                }

                                                @Override
                                                public void fail() {
                                                    signal.countDown();
                                                    callback.fail();
                                                }

                                                @Override
                                                public void inProgress(long progress, long zong) {
                                                    callback.inProgress(progress, zong);
                                                }
                                            }, filep, null);
                                }
                            }).start();


                        } else {
                            signal.countDown();
                            callback.fail();
                            ToastUtil.show(context, "上传失败");
                        }


                    }

                    @Override
                    public void onFailure(Call<ReturnBean<AliObsConfigBean>> call, Throwable t) {
                        signal.countDown();
                        callback.fail();
                        super.onFailure(call, t);
                    }
                });


        try {
            signal.await();
            Log.d("", "upFileSyn: await");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

