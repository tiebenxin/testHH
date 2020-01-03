package com.yanlong.im.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.ui.forward.MsgForwardActivity;
import net.cb.cb.library.utils.GsonUtils;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;

/**
 * author : zgd
 * date   : 2019/11/2610:27
 */
public class ApkUtils {
    public static String qxhnmj="com.qyjh.mj";//亲友湖南麻将 包名
    public static String qxhnmj_splash="com.qyjh.mj.wxapi.WXEntryActivity";//常信 启动页

    public static String cx="com.yanlong.im";//常信 包名
    public static String cx_splash="com.yanlong.im.user.ui.SplashActivity";//常信 启动页
    public static String cx_url="https://www.baidu.com";//常信下载地址

    //判断要启动的app是否已安装
    public static boolean isApkInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }



    //外部app打开本app 分享到本app
    public static void startThisApp(Activity activity){
        if(activity!=null&&activity.getIntent()!=null&&activity.getIntent().getData()!=null){
            String id=activity.getIntent().getData().getQueryParameter("id");
            String name=activity.getIntent().getData().getQueryParameter("name");
            String json=activity.getIntent().getData().getQueryParameter("json");
            LogUtil.getLog().e("外部引用启动本app并携带参数======id="+id+"===name="+name+"===json="+json);


            if(StringUtil.isNotNull(json)){
                MsgAllBean msgbean = GsonUtils.getObject(json, MsgAllBean.class);
                if(msgbean!=null){
                    activity.startActivity(new Intent(activity, MsgForwardActivity.class).putExtra(MsgForwardActivity.AGM_JSON, new Gson().toJson(msgbean)));
                }
            }
        }
    }

    //启动外部app
    public static void startOhterApp(Context context, String packageName,String startActivity,String apkDownloadUrl,String schemeUrl){
        if(isApkInstalled(context,packageName)){
            try {
                if(StringUtil.isNotNull(schemeUrl)){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                schemeUrl="changxin://changxin.zhixun6.com/cx?id=id123&name=name456&json="+"{\"chat\": {\"msg\": \"其他应用分享到常信的信息\",\"msgid\": \"2b6228c8473d47bba3550e37e0752d34\"},\"msg_type\": 1}";
                    intent.setData(Uri.parse(schemeUrl));
                    context.startActivity(intent);
                }else {
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if(StringUtil.isNotNull(startActivity)){//可以指定页面
                        intent.setClassName(packageName, startActivity);
                    }
//            Bundle bundle=new Bundle();
//            bundle.putString("id","123456");
//            intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            //去浏览器下载
            try {
                ToastUtil.show(context,"未安装应用，去下载");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(apkDownloadUrl);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(uri);
                context.startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
                ToastUtil.show(context,"你丫没装浏览器，下载个锤子");
            }

        }
    }
}
