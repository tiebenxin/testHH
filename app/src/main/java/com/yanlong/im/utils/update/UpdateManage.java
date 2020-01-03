package com.yanlong.im.utils.update;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.user.bean.NewVersionBean;
import com.yanlong.im.user.bean.VersionBean;

import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.InstallAppUtil;
import net.cb.cb.library.utils.NetUtil;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.VersionUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateManage {
    private static final int START = 100;
    private static final int LOADING = 200;
    private static final int COMPLETE = 300;
    private static final int EROE = 400;
    private static final int OVERTIME = 500;

    private Context context;
    private Activity activity;
    private UpdateAppDialog dialog;
    private InstallAppUtil installAppUtil;

    private long startsPoint = 0;
    private String updateURL = "";
    private boolean canUse4G = false;//是否允许4G环境下使用流量更新

    public UpdateManage(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public boolean isToDayFirst(NewVersionBean newVersionBean) {
        SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.NEW_VESRSION);
        VersionBean bean = preferencesUtil.get4Json(VersionBean.class);
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String newData = month + "-" + day;
        VersionBean versionBean = new VersionBean();
        versionBean.setTime(newData);
        versionBean.setVersion(newVersionBean.getVersion());
        preferencesUtil.save2Json(versionBean);
        if (bean == null) {
            return true;
        } else {
            if (TextUtils.isEmpty(bean.getTime()) || TextUtils.isEmpty(bean.getVersion())) {
                return true;
            } else {
                if (!newData.equals(bean.getTime()) && !VersionUtil.getVerName(context).equals(newVersionBean.getVersion())) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean check(String versions) {
        boolean isUpdate = false;
        if (VersionUtil.isNewVersion(context, versions)) {
            clearApk();
            isUpdate = true;
        }
        return isUpdate;
    }


    public void uploadApp(String versions, final String content, final String url, boolean isEnforcement) {
        if (check(versions)) {
            updateURL = url;
            dialog = new UpdateAppDialog();
            dialog.init(activity, versions, content, new UpdateAppDialog.Event() {
                @Override
                public void onON() {
                    if (call != null) {
                        call.cancel();
                    }

                }


                @Override
                public void onUpdate() {
                    //循环下载文件流，刚开始下载时第一次判断网络环境
                    //如果是wifi环境则直接下载
                    //如果下载过程中切换到数据流量则提示是否允许使用流量更新，允许则后续不再提示，恢复网络则继续下载
                    startsPoint = getFileStart() > 0 ? getFileStart() - 1 : getFileStart();
                    download(url, downloadListener, startsPoint, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            downloadListener.fail(e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) {
                            //wifi正常下载
                            if (NetUtil.getNetworkType(context).equals("WIFI") || canUse4G) {
                                if (response.code() == 404) {
                                    downloadListener.fail("下载失败");
                                    return;
                                }
                                long length = response.body().contentLength();
                                if (length == 0) {
                                    // 说明文件已经下载完，直接跳转安装就好
                                    downloadListener.complete(String.valueOf(getFile().getAbsoluteFile()));
                                    return;
                                }
                                downloadListener.start(length + startsPoint);
                                // 保存文件到本地
                                InputStream is = null;
                                RandomAccessFile randomAccessFile = null;
                                BufferedInputStream bis = null;

                                byte[] buff = new byte[2048];
                                int len = 0;
                                try {
                                    is = response.body().byteStream();
                                    bis = new BufferedInputStream(is);

                                    File file = getFile();
                                    // 随机访问文件，可以指定断点续传的起始位置
                                    randomAccessFile = new RandomAccessFile(file, "rwd");
                                    randomAccessFile.seek(startsPoint);
                                    while ((len = bis.read(buff)) != -1) {
                                        randomAccessFile.write(buff, 0, len);
                                    }

                                    // 下载完成
                                    downloadListener.complete(String.valueOf(file.getAbsoluteFile()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //监听断网导致的超时
                                    if (e.getMessage().contains("Connection timed out")) {
                                        handler.sendEmptyMessage(OVERTIME);
                                    } else {
                                        downloadListener.loadfail(e.getMessage());
                                    }
                                } finally {
                                    try {
                                        if (is != null) {
                                            is.close();
                                        }
                                        if (bis != null) {
                                            bis.close();
                                        }
                                        if (randomAccessFile != null) {
                                            randomAccessFile.close();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                //是否允许使用流量更新，如果允许，后续不再重复判断是否处于4G
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        show4gNoticeDialog(url);
                                    }
                                });
                            }
                        }
                    });
                }

                @Override
                public void onInstall() {
                    installAppUtil.install(activity, installAppUtil.getApkPath());
                }
            });
            dialog.show();
        }
    }


    private File getFile() {
        String root = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "";
        File file = new File(root, "yanlong.apk");
        return file;
    }

    private long getFileStart() {
        String root = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "";
        File file = new File(root, "yanlong.apk");
        return file.length();
    }

    private File clearApk() {
        String root = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "";
        File file = new File(root, "yanlong.apk");
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

    Call call;

    public Call download(String url, final DownloadListener downloadListener, final long startsPoint, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .header("RANGE", "bytes=" + startsPoint + "-")//断点续传
                .build();

        // 重写ResponseBody监听请求
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new DownloadResponseBody(originalResponse, startsPoint, downloadListener))
                        .build();
            }
        };

        OkHttpClient.Builder dlOkhttp = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor);

        // 发起请求
        call = dlOkhttp.build().newCall(request);
        call.enqueue(callback);

        return call;
    }


    DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void start(long max) {
            handler.sendEmptyMessage(START);
        }

        @Override
        public void loading(int progress) {
            Message message = new Message();
            message.what = LOADING;
            message.arg1 = progress;
            handler.sendMessage(message);

        }

        @Override
        public void complete(String path) {
            Message message = new Message();
            message.what = COMPLETE;
            message.obj = path;
            handler.sendMessage(message);
        }

        @Override
        public void fail(String message) {
            handler.sendEmptyMessage(EROE);
        }

        @Override
        public void loadfail(String message) {
//            handler.sendEmptyMessage(OVERTIME);
        }
    };

    Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START:
                    if (dialog != null) {
                        dialog.updateStart();
                    }
                    break;
                case LOADING:
                    if (dialog != null) {
                        dialog.updateProgress(msg.arg1);
                    }
                    break;
                case COMPLETE:
                    String path = (String) msg.obj;
                    installAppUtil = new InstallAppUtil();
                    installAppUtil.install(activity, path);
                    if (dialog != null) {
                        dialog.downloadComplete();
                    }
                    break;
                case EROE:
                    ToastUtil.show(activity, "下载失败,请重试");
                    if (dialog != null) {
                        dialog.updateStop();
                    }
                    break;
                case OVERTIME:
                    //检查到网络断开，下载超时弹框显示，避免内存泄漏，需要判断activity是否销毁
                    if (activity != null && !activity.isFinishing()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage("更新下载超时");
                        builder.setTitle("提示：");
                        builder.setPositiveButton("下次再更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                if (call != null) {
                                    call.cancel();
                                }
                            }
                        });
                        builder.setNegativeButton("继续下载", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (NetUtil.isNetworkConnected()) {
                                    //TODO zjy 断点续传
                                    startsPoint = getFileStart() > 0 ? getFileStart() - 1 : getFileStart();
                                    download(updateURL, downloadListener, startsPoint, new okhttp3.Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            downloadListener.fail(e.getMessage());
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) {
                                            //wifi正常下载
                                            if (NetUtil.getNetworkType(context).equals("WIFI") || canUse4G) {
                                                if (response.code() == 404) {
                                                    downloadListener.fail("下载失败");
                                                    return;
                                                }
                                                long length = response.body().contentLength();
                                                if (length == 0) {
                                                    // 说明文件已经下载完，直接跳转安装就好
                                                    downloadListener.complete(String.valueOf(getFile().getAbsoluteFile()));
                                                    return;
                                                }
                                                downloadListener.start(length + startsPoint);
                                                // 保存文件到本地
                                                InputStream is = null;
                                                RandomAccessFile randomAccessFile = null;
                                                BufferedInputStream bis = null;

                                                byte[] buff = new byte[2048];
                                                int len = 0;
                                                try {
                                                    is = response.body().byteStream();
                                                    bis = new BufferedInputStream(is);

                                                    File file = getFile();
                                                    // 随机访问文件，可以指定断点续传的起始位置
                                                    randomAccessFile = new RandomAccessFile(file, "rwd");
                                                    randomAccessFile.seek(startsPoint);
                                                    while ((len = bis.read(buff)) != -1) {
                                                        randomAccessFile.write(buff, 0, len);
                                                    }

                                                    // 下载完成
                                                    downloadListener.complete(String.valueOf(file.getAbsoluteFile()));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    //监听断网导致的超时
                                                    if (e.getMessage().contains("Connection timed out")) {
                                                        handler.sendEmptyMessage(OVERTIME);
                                                        return;
                                                    } else {
                                                        downloadListener.loadfail(e.getMessage());
                                                    }
                                                } finally {
                                                    try {
                                                        if (is != null) {
                                                            is.close();
                                                        }
                                                        if (bis != null) {
                                                            bis.close();
                                                        }
                                                        if (randomAccessFile != null) {
                                                            randomAccessFile.close();
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }else {
                                                //是否允许使用流量更新，如果允许，后续不再重复判断是否处于4G
                                                activity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        show4gNoticeDialog(updateURL);
                                                    }
                                                });
                                            }

                                        }
                                    });
                                } else {
                                    //仍然无网络，不允许dialog关闭
                                    ToastUtil.show(context, "请确保网络连接正常");
                                    builder.show();
                                }
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    }
                    break;
            }
        }
    };

    /**
     * 4G数据流量情况下是否确认更新
     */
    private void show4gNoticeDialog(String url) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setCancelable(false);//取消点击外部消失弹窗
        final AlertDialog dialog = dialogBuilder.create();
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_4g_update_notice, null);
        TextView tvCancel = dialogView.findViewById(com.hm.cxpay.R.id.tv_cancel);
        TextView tvSure = dialogView.findViewById(com.hm.cxpay.R.id.tv_sure);
        tvCancel.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        //允许更新
        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canUse4G = true;
                dialog.dismiss();
                startsPoint = getFileStart() > 0 ? getFileStart() - 1 : getFileStart();
                download(url, downloadListener, startsPoint, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        downloadListener.fail(e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        if (response.code() == 404) {
                            downloadListener.fail("下载失败");
                            return;
                        }
                        long length = response.body().contentLength();
                        if (length == 0) {
                            // 说明文件已经下载完，直接跳转安装就好
                            downloadListener.complete(String.valueOf(getFile().getAbsoluteFile()));
                            return;
                        }
                        downloadListener.start(length + startsPoint);
                        // 保存文件到本地
                        InputStream is = null;
                        RandomAccessFile randomAccessFile = null;
                        BufferedInputStream bis = null;

                        byte[] buff = new byte[2048];
                        int len = 0;
                        try {
                            is = response.body().byteStream();
                            bis = new BufferedInputStream(is);

                            File file = getFile();
                            // 随机访问文件，可以指定断点续传的起始位置
                            randomAccessFile = new RandomAccessFile(file, "rwd");
                            randomAccessFile.seek(startsPoint);
                            while ((len = bis.read(buff)) != -1) {
                                randomAccessFile.write(buff, 0, len);
                            }

                            // 下载完成
                            downloadListener.complete(String.valueOf(file.getAbsoluteFile()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            //监听断网导致的超时
                            if (e.getMessage().contains("Connection timed out")) {
                                handler.sendEmptyMessage(OVERTIME);
                            } else {
                                downloadListener.loadfail(e.getMessage());
                            }
                        } finally {
                            try {
                                if (is != null) {
                                    is.close();
                                }
                                if (bis != null) {
                                    bis.close();
                                }
                                if (randomAccessFile != null) {
                                    randomAccessFile.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
        //展示界面
        dialog.show();
        //解决圆角shape背景无效问题
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //设置宽高
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = DensityUtil.dip2px(activity, 139);
        lp.width = DensityUtil.dip2px(activity, 277);
        dialog.getWindow().setAttributes(lp);
        dialog.setContentView(dialogView);
    }


}
