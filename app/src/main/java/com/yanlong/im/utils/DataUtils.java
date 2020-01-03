package com.yanlong.im.utils;

import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;

/**
 * author : zgd
 * date   : 2019/12/211:11
 */
public class DataUtils {

    // *** 隐藏
    public static String getHideData(String data,int notHideNumb){
        if(StringUtil.isNotNull(data)){
//            LogUtil.getLog().e("====length==="+data.length());
            if(data.length()<=notHideNumb||notHideNumb<=0){
                String start="";
                for (int i = 0; i < data.length(); i++) {
                    start=start+"*";
//                    LogUtil.getLog().e("====start==="+start);
                }
                return start;
            }else {
                String dataTemp=data.substring(0,notHideNumb);
                String start="";
                for (int i = notHideNumb; i < data.length(); i++) {
                    start=start+"*";
//                    LogUtil.getLog().e("====start==="+start);
                }
                dataTemp=dataTemp+start;
                return dataTemp;
            }
        }
        return data;
    }
}
