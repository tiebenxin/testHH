package net.cb.cb.library.utils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-10-09
 * @updateAuthor
 * @updateDate
 * @description Intent跳转工具类
 *
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class IntentUtil {

    /**
     * 普通的方式打开一个Activiy
     *
     * @param context   上下文
     * @param gotoClass 需要打开的Activity
     * @version 1.0
     * @createTime 2014年12月31日, 上午7:07:13
     * @updateTime 2014年12月31日, 上午7:07:13
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void gotoActivity(Context context, Class<?> gotoClass) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        context.startActivity(intent);
    }

    /**
     * 打开一个Activity并关闭当前页面
     *
     * @param context   上下文
     * @param gotoClass 需要打开的Activity
     * @version 1.0
     * @createTime 2014年12月31日, 上午7:07:43
     * @updateTime 2014年12月31日, 上午7:07:43
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void gotoActivityToTopAndFinish(Context context, Class<?> gotoClass) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    /**
     * 用单例模式打开一个Activity并关闭当前页面，可携带数据
     *
     * @param context   上下文
     * @param gotoClass 需要跳转的页面
     * @param bundle    携带的数据
     * @version 1.0
     * @createTime 2014年12月31日, 上午7:08:40
     * @updateTime 2014年12月31日, 上午7:08:40
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void gotoActivityToTopAndFinish(Context context, Class<?> gotoClass, Bundle bundle) {
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(context, gotoClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    /**
     * 普通的方式打开一个activity，并携带数据
     *
     * @param context   上下文
     * @param gotoClass 需要打开的Activity
     * @param bundle    携带的数据
     * @version 1.0
     * @createTime 2014年12月31日, 上午7:05:33
     * @updateTime 2014年12月31日, 上午7:05:33
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void gotoActivity(Context context, Class<?> gotoClass, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    /**
     * 用Result的方式跳转到指定页面，不携带数据
     *
     * @param context     上下文
     * @param gotoClass   要跳转的Activity
     * @param requestCode 页面跳转请求码
     * @version 1.0
     * @createTime 2014年12月31日, 上午7:03:59
     * @updateTime 2014年12月31日, 上午7:03:59
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void gotoActivityForResult(Context context, Class<?> gotoClass, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    public static void gotoActivityForResult(Fragment fragment, Class<?> gotoClass, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(fragment.getActivity(), gotoClass);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 用Result的形式跳转到指定页面，并携带数据
     *
     * @param context     上下文
     * @param gotoClass   要跳转的页面
     * @param bundle      携带的数据
     * @param requestCode 跳转搞到页面的请求码
     * @version 1.0
     * @createTime 2014年12月31日, 上午7:02:25
     * @updateTime 2014年12月31日, 上午7:02:25
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void gotoActivityForResult(Context context, Class<?> gotoClass, Bundle bundle, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.putExtras(bundle);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 跳转至指定activity,并关闭当前activity.
     *
     * @param context   上下文
     * @param gotoClass 需要跳转的Activity界面
     * @version 1.0
     * @createTime 2014年12月31日, 上午7:01:47
     * @updateTime 2014年12月31日, 上午7:01:47
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void gotoActivityAndFinish(Context context, Class<?> gotoClass) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    /**
     * 携带传递数据跳转至指定activity,并关闭当前activity.
     *
     * @param context   上下文
     * @param gotoClass 需要跳转的页面
     * @param bundle    附带数据
     * @version 1.0
     * @createTime 2014年12月31日, 上午7:00:59
     * @updateTime 2014年12月31日, 上午7:00:59
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void gotoActivityAndFinish(Context context, Class<?> gotoClass, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.putExtras(bundle);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    /**
     * 跳转至指定activity,不关闭当前页面
     *
     * @param context   上下文
     * @param gotoClass 需要跳转的界面Activity
     * @version 1.0
     * @createTime 2014年12月31日, 上午6:59:27
     * @updateTime 2014年12月31日, 上午6:59:27
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void gotoActivityToTop(Context context, Class<?> gotoClass) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 携带传递数据跳转至指定activity,不关闭当前activity.
     *
     * @param context   上下文
     * @param gotoClass 跳转的activity
     * @param bundle    附带的数据
     * @version 1.0
     * @createTime 2014年12月31日, 上午6:58:46
     * @updateTime 2014年12月31日, 上午6:58:46
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void gotoActivityToTop(Context context, Class<?> gotoClass, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    /**
     * 关闭某个activity
     *
     * @param activity 需要关闭的activity对象
     * @version 1.0
     * @createTime 2014年12月31日, 上午6:57:56
     * @updateTime 2014年12月31日, 上午6:57:56
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void finish(Activity activity) {
        activity.finish();
    }
}
