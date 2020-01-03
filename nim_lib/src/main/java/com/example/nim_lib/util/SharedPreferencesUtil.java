package com.example.nim_lib.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import net.cb.cb.library.AppConfig;

import java.lang.reflect.Type;

/**
 * 配置缓存
 * Created by Administrator on 2016/11/27.
 */

public class SharedPreferencesUtil {
    private static final String TAG="SharedPreferencesUtil";
    //有时间抽个接口出来吧,利于模块管理
    public enum SPName {
        //这里定义xml的名字,统一管理
        CACHE_DEF("chahe_default"),
        USER_SETTING("user_setting"),
        TOKEN("token"), //token
        FONT_CHAT("font_chat"),//
        USER_INFO("user_info"),
        PUSH("push"),
        FIRST_TIME("first_time"),
        PHONE("phone"),
        IMAGE_HEAD("image_head"),
        DEV_ID("uid"),
        SCROLL("scroll"),
        NOTIFICATION("notification"),
        NEW_VESRSION("new_vesrsion");

        private String name;


        SPName(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }


        @Override
        public String toString() {
            return this.name;
        }
    }

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEdit;
    private Gson gson = new Gson();
    private String spKeyDeg = "json";

    /***
     * @param name 文件名
     */
    public SharedPreferencesUtil(SPName name) {
        sharedPreferences = AppConfig.APP_CONTEXT.getSharedPreferences(name.toString(), Context.MODE_PRIVATE);
        spEdit = sharedPreferences.edit();
    }

    /***
     * 把对象json化后存配置
     *
     * @param obj
     */
    public void save2Json(Object obj) {
        save2Json(obj, spKeyDeg);

    }

    /***
     * 把对象json化后存配置
     *
     * @param obj
     * @param name 配置名
     */
    public void save2Json(Object obj, String name) {
        Log.i(TAG,"save2Json>>"+name);
        String json = gson.toJson(obj);
        spEdit.putString(name, json);
        spEdit.commit();

    }

    /***
     * 从配置中获取对象,注意为空处理
     *
     * @param classOfT
     * @param <T>
     * @return
     */
    public <T> T get4Json(Class<T> classOfT) {


        return get4Json(classOfT, spKeyDeg);
    }


    public <T> T get4Json(Class<T> classOfT, String name) {
        String json = sharedPreferences.getString(name, "");

        return gson.fromJson(json, classOfT);
    }

    public <T> T get4Json(Type typeOfT, String name) {
        String json = sharedPreferences.getString(name, "");

        return gson.fromJson(json,typeOfT);
    }

    public void clear(){
        spEdit.clear().commit();
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

}
