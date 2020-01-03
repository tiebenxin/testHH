package com.yanlong.im.location;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;

/**
 * author : zgd
 * date   : 2019/12/14 9:58
 */
public class LocationPersimmions {
    public static int SDK_PERMISSION_REQUEST = 127;//请求权限返回

    //获取权限需要的权限列表
    public static ArrayList<String> getPersimmions() {
        ArrayList<String> permissions = new ArrayList<String>();
        //定位权限为必须权限，用户如果禁止，则每次进入都会申请
        // 定位精确位置
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissions;
    }

    //检测某个权限组
    public static boolean checkPermissions(Activity activity) {
        ArrayList<String> permissions=getPersimmions();
        Boolean hasPermissions = true;
        for (int i = 0; i < permissions.size(); i++) {
            Boolean hasPermission = checkPermission(activity, permissions.get(i));
//            LogUtil.getLog().e("==hasPermission=="+hasPermission);
            if (!hasPermission) {
                hasPermissions = false;
            }
        }
        if (!hasPermissions) {
            LocationPersimmions.requestPermissions(activity, permissions);
        }
//        LogUtil.getLog().e("=location=hasPermission=s="+hasPermissions);
        return hasPermissions;
    }

    //检测某个权限  PERMISSION_GRANTED 有 ,  PERMISSION_DENIED 无
    public static boolean checkPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }


    //申请定位权限
    public static void requestPermissions(Activity activity, ArrayList<String> permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissions.size() > 0) {
                activity.requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }else {
            //23以下 默认给了定位权限
        }
    }

}
