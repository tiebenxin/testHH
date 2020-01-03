package net.cb.cb.library.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态权限申请
 */
public class CheckPermission2Util {

    private static final String TAG = "CheckPermission2Util";

    private Event onEvent;
    private Activity activity;
    private String[] prm;

    /***
     * 1.申请权限
     * @param act
     * @param event
     * @param prm
     */
    public void requestPermissions(Activity act, Event event, String[] prm) {
        onEvent = event;
        activity = act;
        this.prm = prm;
        String[] prming = getNeededPermission(act, prm);
        if (prming != null && prming.length > 0) {
            ActivityCompat.requestPermissions(act, prming, 1);

        } else {
            onEvent.onSuccess();
        }


    }

    /***
     * 2.放在PonRequestermissionsResult下面
     */
    public void onRequestPermissionsResult() {
        if(onEvent==null){
            return;
        }
        String[] prming = getNeededPermission(activity, prm);
        if (prming != null && prming.length > 0) {
            onEvent.onFail();
        } else {
            onEvent.onSuccess();
        }

    }

    private String[] getNeededPermission(Context context, String[] permissionArray) {
        if (context == null || permissionArray == null || permissionArray.length == 0) {
            return new String[]{};
        }

        List<String> permissionList = new ArrayList<>();
        for (int i = 0; i < permissionArray.length; i++) {
            if (isNeedAddPermission(context, permissionArray[i])) {
                permissionList.add(permissionArray[i]);
            }
        }
        return permissionList.toArray(new String[permissionList.size()]);
    }

    private boolean isNeedAddPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED;
    }


    public interface Event {
        void onSuccess();

        void onFail();
    }
}