package com.yanlong.im.location;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.yanlong.im.MyAppLication;

/**
 * author : zgd
 * date   : 2019/12/1410:06
 */
public class LocationUtils {
    public LocationService locationService;
    public static double beishu=1000000d;
    public static String baiduImageUrl = "http://api.map.baidu.com/staticimage/v2";//静态图地址
    public static String ak = "ak=L7VrjgIV1dMONUenMO8XmIwOPKGLSDE5";//秘钥
    public static String mcode = "mcode=62:84:03:64:9A:E7:CD:28:13:24:91:1A:16:60:8F:47:83:8D:98:B3;com.yanlong.im";//安全码  sha1+包名
    public static String widthAndHeight = "width=300&height=200&zoom=18";//尺寸和缩放比例

    //根据 坐标 获取静态图url
    public static String getLocationUrl(int latitude, int longitude) {
        //http://api.map.baidu.com/staticimage/v2?
        // ak=L7VrjgIV1dMONUenMO8XmIwOPKGLSDE5&
        // mcode=62:84:03:64:9A:E7:CD:28:13:24:91:1A:16:60:8F:47:83:8D:98:B3;com.yanlong.im&
        // center=112.953042,28.136296
        // &width=300&height=200&zoom=18

        double latitudeDouble = latitude / beishu;
        double longitudeDouble = longitude / beishu;
        String center = "center=" + longitudeDouble + "," + latitudeDouble;//中心点坐标
        String locationUrl = baiduImageUrl + "?" + ak + "&" + mcode + "&" + center + "&" + widthAndHeight;
        return locationUrl;
    }

    //根据 坐标 获取静态图url
    public static String getLocationUrl2(double latitude, double longitude) {
        String center = "center=" + longitude + "," + latitude;//中心点坐标
        String locationUrl = baiduImageUrl + "?" + ak + "&" + mcode + "&" + center + "&" + widthAndHeight;
        return locationUrl;
    }


    //判断定位服务是否打开
    public static boolean isLocationEnabled(Context content) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(content.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(content.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    

    //初始化
    public void initLocation(Application application) {
        // -----------location config ------------
//        locationService = ((MyAppLication) application).locationService;
//        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
//        locationService.registerListener(new BDAbstractLocationListener() {
//            @Override
//            public void onReceiveLocation(BDLocation bdLocation) {
////                LogUtil.getLog().e("====location="+ GsonUtils.optObject(bdLocation));
//            }
//
//            @Override
//            public void onLocDiagnosticMessage(int i, int i1, String s) {
//                super.onLocDiagnosticMessage(i, i1, s);
//
//            }
//        });
//        if (type == 0) {
//            //注册监听
//        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
//        } else if (type == 1) {  //  有权限 后开启定位
//            locationService.start();
//        }

//        locationService.stop();

//        locationService.start();// 定位SDK
//        // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
//        locationService.stop();



    }


}
