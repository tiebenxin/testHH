package net.cb.cb.library.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

public class VersionUtil {
    /**
     * 获取当前本地apk的版本
     *
     * @param mContext
     * @return
     */
    public static int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本号名称
     *
     * @param context 上下文
     * @return
     */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            if (context != null) {
                verName = context.getPackageManager().
                        getPackageInfo(context.getPackageName(), 0).versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }


    /**
     * 比较新版本
     */
    public static boolean isNewVersion(Context context, String newVerName) {
        boolean isNeed = false;
        try {
            //获取当前版本
            String OldVerName = getVerName(context);
            if (!TextUtils.isEmpty(newVerName) && !TextUtils.isEmpty(OldVerName)) {
                String[] newVer = newVerName.split("\\.");
                String[] oldVer = OldVerName.split("\\.");
                //这里因为服务器和本地版本号的格式一样，所以随便哪个的长度都可以使用
                for (int i = 0; i < newVer.length; i++) {
                    int newNumber = Integer.parseInt(newVer[i]);
                    int oldNumber = Integer.parseInt(oldVer[i]);
                    if(oldNumber>newNumber){
                        isNeed = false;
                        break;
                    }
                    if (oldNumber < newNumber) {
                        isNeed = true;
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.getLog().e("比较版本号时出错");
        }
        return isNeed;
    }


    public static String getPhoneModel() {
        return android.os.Build.BRAND + " " + android.os.Build.MODEL;
    }

}