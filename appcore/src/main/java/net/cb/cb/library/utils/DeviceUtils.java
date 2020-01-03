package net.cb.cb.library.utils;

import android.os.Build;

/**
 * @author Liszt
 * @date 2019/11/8
 * Description
 */
public class DeviceUtils {

    public static String HUA_WEI = "Huawei";
    public static String XIAO_MI = "Xiaomi";
    public static String VIVO = "Vivo";
    public static String OPPO = "Oppo";
    public static String SAMSUNG = "Samsung";


    /**
     * 手机型号
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 获取设备厂商
     * <p>如Xiaomi</p>
     *
     * @return 设备厂商
     */

    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 手机品牌
     */
    public static String getBrand() {
        return Build.BRAND;
    }

    public static boolean isViVoAndOppo() {

        LogUtil.getLog().d("a=", DeviceUtils.class.getSimpleName() + "--手机品牌名=" + getBrand());
        if (getBrand().equals(VIVO) || getBrand().equals(OPPO)) {
            return true;
        }
        return false;
    }

}
