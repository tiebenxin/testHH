package net.cb.cb.library.utils;

import android.util.Log;

import net.cb.cb.library.AppConfig;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 网络自动缓存工具
 * Created by jyj on 2016/11/28.
 */

public class NetCacheUtil {
    private final static String TAG = "NetCacheUtil";

    /***
     * 存到默认配置
     * @param cacheName 缓存名
     * @param server
     * @param call
     * @param <T>
     */
    public <T> void doCache(final String cacheName, Call<T> server, final Callback<T> call) {
        doCache(SharedPreferencesUtil.SPName.CACHE_DEF, cacheName, server, call);
    }

    /***
     * 存缓存,一般用于列表,首页等需要后置缓存的地方
     * @param spName 请先在SPName配置
     * @param cacheName
     * @param server
     * @param call
     * @param <T>
     */
    public <T> void doCache(SharedPreferencesUtil.SPName spName, final String cacheName, Call<T> server, final Callback<T> call) {
        final SharedPreferencesUtil sp = new SharedPreferencesUtil(spName);

        if (NetUtil.isNetworkConnected()) {

            server.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> callc, Response<T> response) {
                    sp.save2Json(response.body(), cacheName);
                    if (AppConfig.DEBUG)
                        Log.i(TAG, "网络读取数据<<<");
                    call.onResponse(callc, response);
                }

                @Override
                public void onFailure(Call<T> callc, Throwable t) {
                    call.onFailure(callc, t);
                }
            });

        } else {
            try {
                Type genericSuperclass = call.getClass().getGenericSuperclass();
                Type agms = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
               // Type type = call.getClass().getGenericInterfaces()[0];
              //  Type agms = ((ParameterizedType) type).getActualTypeArguments()[0];
                T date = sp.get4Json(agms, cacheName);
                if (AppConfig.DEBUG)
                    Log.i(TAG, "缓存读取数据<<<");

                call.onResponse(null, Response.success(date));

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "缓存读取数据<<<" + e.getMessage());
            }
        }
    }


}
