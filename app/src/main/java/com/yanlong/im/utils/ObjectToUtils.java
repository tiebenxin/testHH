package com.yanlong.im.utils;

import java.lang.reflect.Method;

public class ObjectToUtils {
    public static Object getObjectInstace(Class cls, String methodStr,Object orgs){
        try{
            Method method= cls.getMethod(methodStr);
           return method.invoke(cls,orgs);
        }catch (Exception e){
            return "";
        }
    }
}
