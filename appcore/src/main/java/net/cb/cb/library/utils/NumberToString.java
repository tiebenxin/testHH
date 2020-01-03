package net.cb.cb.library.utils;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/***
 * 数字格式化
 * @author jyj
 * @date 2017/2/14
 */
public class NumberToString {
    public static String TO_0_0(double d){
        Log.e("TO_0_0", "TO_0_0: "+d );
        DecimalFormat df=new DecimalFormat("0.00");
      //  df.setRoundingMode(RoundingMode.HALF_DOWN);
        String str=df.format(d);
        str=str.substring(0,str.length()-1);


        str=str.endsWith(".0")?str.replace(".0",""):str;
        Log.e("TO_0_0", "TO_0_0bak: "+str);
        return str;
    }


/*    public static double TO_double_0_0(double d){

        BigDecimal bigDecimal = new BigDecimal(d);
        bigDecimal.setScale(1,BigDecimal.ROUND_FLOOR);
      *//*  DecimalFormat df=new DecimalFormat("0.0");
        String str=df.format(bigDecimal);

        Log.e("TO_double_0_0", ">>>TO_double_0_0: "+str);
        return Double.parseDouble(str) ;*//*
        Log.e("TO_double_0_0", "TO_double_0_0: "+d );


       return bigDecimal.doubleValue();
    }*/


    public static String TO_0_00(double d){
        DecimalFormat df=new DecimalFormat("0.00");
        String str=df.format(d);
        str=str.endsWith(".00")?str.replace(".00",""):str;
        return str;

    }
    public static String TO_Km(int d){

        if(d<1000){
            return d+"米";
        }


        return TO_0_0(d/1000.0)+"公里";
    }

    public static String TO_Time(int time){


        long hour=time/3600;

        long minute=(time-hour*3600)/60;
        String s=hour+"小时"+minute+"分钟";

        if(hour<=0)
            s=minute+"分钟";
        if(minute<1)
            s="1分钟";

        return s;
    }
}
