package com.example.nim_lib.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;

import com.example.nim_lib.config.Preferences;

import net.cb.cb.library.utils.SpUtil;
import net.cb.cb.library.view.AlertYesNo;

import java.lang.reflect.Method;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-11-21
 * @updateAuthor
 * @updateDate
 * @description 权限设置
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class PermissionsUtil {

    private static AlertYesNo mAlertYesNo;

    /**
     * 检测 meizu 悬浮窗权限
     */
    public static boolean checkMeiZuFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkMeiZuOp(context, 24); //OP_SYSTEM_ALERT_WINDOW = 24;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean checkMeiZuOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class clazz = AppOpsManager.class;
                Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
            }
        } else {
        }
        return false;
    }

    /**
     * 去魅族权限申请页面
     */
    public static void applyMeiZuOpPermission(Context context) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 检测 Huawei 悬浮窗权限
     */
    public static boolean checkHuaWeiFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkHuaWeiOp(context, 24); //OP_SYSTEM_ALERT_WINDOW = 24;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean checkHuaWeiOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class clazz = AppOpsManager.class;
                Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
                gotoAppDetailSetting(context);
            }
        } else {
        }
        return false;
    }

    /**
     * 华为的权限管理页面
     */
    public static void applyHuaWeiPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            gotoAppDetailSetting(context);
        }
    }

    /**
     * 获取应用详情页面intent
     *
     * @return
     */
    private static void gotoAppDetailSetting(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }

    /**
     * 显示权限对话框
     */
    public static void showPermissionDialog(Activity activity) {
        final String title = "权限申请";
        final String content = "在设置-应用-常信-权限中开启悬浮窗权限，以保证音视频功能的正常使用，取消可能会接收不到音视频通话";

        if (mAlertYesNo == null) {
            mAlertYesNo = new AlertYesNo();
            mAlertYesNo.init(activity, title, content, "去设置", "取消", new AlertYesNo.Event() {
                @Override
                public void onON() {
                    SpUtil spUtil = SpUtil.getSpUtil();
                    spUtil.putSPValue(Preferences.IS_FIRST_DIALOG, true);
                }

                @Override
                public void onYes() {
                    SpUtil spUtil = SpUtil.getSpUtil();
                    spUtil.putSPValue(Preferences.IS_FIRST_DIALOG, true);
                    if (Build.VERSION.SDK_INT >= 23) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);
                    } else {
                        String brand = android.os.Build.BRAND;
                        brand = brand.toUpperCase();
                        if (brand.equals("HUAWEI")) {
                            PermissionsUtil.applyHuaWeiPermission(activity);
                        } else if (brand.equals("MEIZU")) {
                            PermissionsUtil.applyMeiZuOpPermission(activity);
                        }
                    }
                }
            });
        }
        if(activity!=null && !activity.isFinishing()){
            if (mAlertYesNo.isShowing()) {
                mAlertYesNo.dismiss();
            }
            mAlertYesNo.show();
        }
    }

}
