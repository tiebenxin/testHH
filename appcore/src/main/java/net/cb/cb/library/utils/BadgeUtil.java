package net.cb.cb.library.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class BadgeUtil {
    public static int num = 0;

    private BadgeUtil() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    public static void addBadgeCount(Context context) {
        num++;
        setBadgeCount(context, num);
    }

    /**
     * 设置Badge 目前支持Launcher
     */
    public static void setBadgeCount(Context context, int count) {
        if (count <= 0) {
            count = 0;
        } else {
            count = Math.max(0, Math.min(count, 99));
        }
        num = count;
        String rom = Build.MANUFACTURER.toLowerCase();

        if (rom.contains("xiaomi")) {       //小米
            //setBadgeOfMIUI(count, notification);
        } else if (rom.contains("sony")) {  //索尼
            setBadgeOfSony(context, count);
        } else if (rom.contains("samsung") || rom.contains("lg")) {
            setBadgeOfSumsung(context, count);
        } else if (rom.contains("htc")) {   //htc
            setBadgeOfHTC(context, count);
        } else if (rom.contains("nova")) {  //nova
            setBadgeOfNova(context, count);
        } else if (rom.contains("oppo")) {   //oppo
            setBadgeOfOPPO(context, count);
        } else if (rom.contains("lemobile")) {//乐视
            Log.e("BadgeUtil", "setBadgeCount: 乐视 暂不支持");
        } else if (rom.contains("vivo")) {
            setBadgeOfVIVO(context, count);
        } else if (rom.contains("huawei") || Build.BRAND.equals("Huawei") || Build.BRAND.equals("HONOR") || rom.equalsIgnoreCase("huawei")) {//华为
            setHuaweiBadge(context, count);
        } else if (rom.contains("meizu")) {//魅族
            Log.e("BadgeUtil", "setBadgeCount: 魅族 暂不支持");
        } else if (rom.contains("jinli")) {//金立
            Log.e("BadgeUtil", "setBadgeCount: 金立 暂不支持");
        } else if (rom.contains("chuizi")) {//锤子
            Log.e("BadgeUtil", "setBadgeCount: 锤子 暂不支持");
        } else {
            Log.e("BadgeUtil", "setBadgeCount: 未匹配机型：" + rom);
        }
    }

    /**
     * 设置MIUI的Badge
     */
    private static void setBadgeOfMIUI(int count, Notification notification) {
        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置索尼的Badge
     * 需添加权限：<uses-permission android:name="com.sonyericsson.home.permission.BROADCAST_BADGE" />
     */
    private static void setBadgeOfSony(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        boolean isShow = true;
        if (count == 0) {
            isShow = false;
        }
        Intent localIntent = new Intent();
        localIntent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", isShow);//是否显示
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", launcherClassName);//启动页
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(count));//数字
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());//包名
        context.sendBroadcast(localIntent);
    }

    /**
     * 设置三星的Badge\设置LG的Badge
     */
    private static void setBadgeOfSumsung(Context context, int count) {
        // 获取你当前的应用
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    /**
     * 设置HTC的Badge
     */
    private static void setBadgeOfHTC(Context context, int count) {
        Intent intentNotification = new Intent("com.htc.launcher.action.SET_NOTIFICATION");
        ComponentName localComponentName = new ComponentName(context.getPackageName(), getLauncherClassName(context));
        intentNotification.putExtra("com.htc.launcher.extra.COMPONENT", localComponentName.flattenToShortString());
        intentNotification.putExtra("com.htc.launcher.extra.COUNT", count);
        context.sendBroadcast(intentNotification);

        Intent intentShortcut = new Intent("com.htc.launcher.action.UPDATE_SHORTCUT");
        intentShortcut.putExtra("packagename", context.getPackageName());
        intentShortcut.putExtra("count", count);
        context.sendBroadcast(intentShortcut);
    }

    /**
     * 设置Nova的Badge
     */
    private static void setBadgeOfNova(Context context, int count) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("tag", context.getPackageName() + "/" + getLauncherClassName(context));
        contentValues.put("count", count);
        context.getContentResolver().insert(Uri.parse("content://com.teslacoilsw.notifier/unread_count"),
                contentValues);
    }

    /**
     * 设置vivo的Badge :vivoXplay5 vivo x7无效果
     */
    private static void setBadgeOfVIVO(Context context, int count) {
        try {
            //老版可用，新版已封闭，需申请
            Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
            intent.putExtra("packageName", context.getPackageName());
            String launchClassName = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()).getComponent().getClassName();
            intent.putExtra("className", launchClassName);
            intent.putExtra("notificationNum", count);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置oppo的Badge :oppo角标提醒目前只针对内部软件还有微信、QQ开放，其他的暂时无法提供
     */
    private static void setBadgeOfOPPO(Context context, int count) {
        try {
            if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
                Bundle extras = new Bundle();
                extras.putInt("app_badge_count", count);
                context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"), "setAppBadgeCount", String.valueOf(count), extras);
            } else {
                Intent intent = new Intent("com.oppo.unsettledevent");
                intent.putExtra("packageName", context.getPackageName());
                intent.putExtra("number", count);
                intent.putExtra("upgradeNumber", count);
                PackageManager packageManager = context.getPackageManager();
                List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(intent, 0);
                if (receivers != null && receivers.size() > 0) {
                    context.sendBroadcast(intent);
                } else {
                    Bundle extras = new Bundle();
                    extras.putInt("app_badge_count", count);
                    context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"),
                            "setAppBadgeCount", null, extras);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置华为的Badge :mate8 和华为 p7,honor畅玩系列可以,honor6plus 无效果
     */
    private static void setHuaweiBadge(Context context, int count) {
        //权限检查
        try {
            Bundle bundle = new Bundle();
            bundle.putString("package", context.getPackageName());
            bundle.putString("class", getLauncherClassName(context));
            bundle.putInt("badgenumber", count);
            context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBadgeOfMadMode(Context context, int count, String packageName, String className) {
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", packageName);
        intent.putExtra("badge_count_class_name", className);
        context.sendBroadcast(intent);
    }

    /**
     * 重置Badge
     */
    public static void resetBadgeCount(Context context) {
        setBadgeCount(context, 0);
    }

    private static String getLauncherClassName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage(context.getPackageName());
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (info == null) {
            info = packageManager.resolveActivity(intent, 0);
        }
        return info.activityInfo.name;
    }

}