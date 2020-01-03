package net.cb.cb.library.utils;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.widget.Toast;

import net.cb.cb.library.AppConfig;

/***
 * @author jyj
 * @date 2016/11/29
 */
public class ToastUtil {
    private static Toast toast;

    public static void show(Context context, String txt) {
        if (txt != null && txt.length() > 0) {
            if (toast != null)
                toast.cancel();
            try{
              //  Looper.prepare();
                toast = Toast.makeText(context, txt, Toast.LENGTH_SHORT);
            //    Looper.loop();
            }catch (Exception e){
                e.printStackTrace();
            }

        //    toast.setGravity(Gravity.CENTER, 0, 0);

            toast.show();
        }

    }

    public static void showCenter(Context context, String txt) {
        if (txt != null && txt.length() > 0) {
            if (toast != null)
                toast.cancel();
            try{
                //  Looper.prepare();
                toast = Toast.makeText(context, txt, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                //    Looper.loop();
            }catch (Exception e){
                e.printStackTrace();
            }

            //    toast.setGravity(Gravity.CENTER, 0, 0);

            toast.show();
        }

    }

    public static void show(Context context, int txt) {
        if (toast != null)
            toast.cancel();
        try {
           // Looper.prepare();
            toast = Toast.makeText(context, txt, Toast.LENGTH_SHORT);
           // Looper.loop();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
          toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 新增->长提示
     * @param context
     * @param txt
     */
    public static void showLong(Context context, String txt) {
        if (txt != null && txt.length() > 0) {
            if (toast != null)
                toast.cancel();
            try{
                //  Looper.prepare();
                toast = Toast.makeText(context, txt, Toast.LENGTH_LONG);
                //    Looper.loop();
            }catch (Exception e){
                e.printStackTrace();
            }

            //    toast.setGravity(Gravity.CENTER, 0, 0);

            toast.show();
        }

    }


    public static void show(String txt) {
        if (txt != null && txt.length() > 0&& AppConfig.APP_CONTEXT!=null) {
            if (toast != null){
                toast.cancel();
            }
            try{
                toast = Toast.makeText(AppConfig.APP_CONTEXT, txt, Toast.LENGTH_SHORT);
                toast.show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
