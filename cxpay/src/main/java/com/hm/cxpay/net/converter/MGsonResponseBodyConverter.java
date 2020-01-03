package com.hm.cxpay.net.converter;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import okhttp3.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Converter;

public class MGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final Gson gson;
    private final TypeAdapter<T> adapter;

    MGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        try {
            if (TextUtils.isEmpty(value.string())) {
                return null;
            }
            JSONObject object = new JSONObject(value.string());
            ByteArrayInputStream inputStream;
            //后端没传data字段
            if (!object.toString().contains("\"{\\\"data")) {
                object.put("data", null);
            }
//            if (object.toString().contains("\"{\\\"errCode")) {
//                String[] split = object.toString().split("\"");
//                String string = split[1];
//                Log.e("string", "" + string);
//                String results = object.getString(string);
//                Log.e("results", "" + results);
//                inputStream = new ByteArrayInputStream(results.getBytes());
//            } else {
//                inputStream = new ByteArrayInputStream(object.toString().getBytes());
//            }
            inputStream = new ByteArrayInputStream(object.toString().getBytes());
            //字节流转换成字符流
            Reader reader = new InputStreamReader(inputStream);

            JsonReader jsonReader = gson.newJsonReader(reader);
            //如果你不能轻易找出json格式错误的位置，你也可以设置GSON解析模式为lenient模式:
            jsonReader.setLenient(true);
            return adapter.read(jsonReader);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            value.close();
        }
        return null;

    }
}