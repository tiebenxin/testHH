package net.cb.cb.library.utils;

import com.google.gson.Gson;

/**
 * @author Liszt
 * @date 2019/8/12
 * Description bean 转 json ，json转bean
 */
public class GsonUtils {

    public static <T extends Object> T getObject(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, clazz);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }


    }


    public static <T extends Object> String optObject(T t) {
        if (t == null) {
            return null;
        }
        try {
            Gson gson = new Gson();
            return gson.toJson(t);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
