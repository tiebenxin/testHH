package com.hm.cxpay.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Liszt
 * @date 2019/12/4
 * Description
 */
public class DateUtils {
    public final static long MILLISECOND = 1000;
    public final static long MINUTE = MILLISECOND * 60;
    public final static long HOUR = MINUTE * 60;
    public final static long DAY = HOUR * 24;


    //获取每月第一天的最初时间
    public static long getStartTimeOfMonth(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取指定日期所在月份第一天开始的时间戳
     *
     * @param date 指定日期
     * @return
     */
    public static Long getMonthBegin(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        //设置为1号,当前日期既为本月第一天
        c.set(Calendar.DAY_OF_MONTH, 1);
        //将小时至0
        c.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟至0
        c.set(Calendar.MINUTE, 0);
        //将秒至0
        c.set(Calendar.SECOND, 0);
        //将毫秒至0
        c.set(Calendar.MILLISECOND, 0);
        // 获取本月第一天的时间戳
        return c.getTimeInMillis();
    }


    /**
     * 根据某个时间戳获取当月的结束时间（最后一天的最后一毫秒）
     *
     * @return
     */
    public static Date endTimeOfMonth(long time) {
        Calendar first = new GregorianCalendar();
        first.setTimeInMillis(time);
        first.add(Calendar.MONTH, 1); // 加一个月
        first.set(Calendar.DAY_OF_MONTH, 1);
        first.set(Calendar.HOUR_OF_DAY, 0);
        first.set(Calendar.MINUTE, 0);
        first.set(Calendar.SECOND, 0);
        first.set(Calendar.MILLISECOND, 0);
        first.add(Calendar.MILLISECOND, -1);
        return first.getTime();
    }

    //获取红包抢完时间
    public static String getGrabFinishedTime(long finishTime) {
        String result = "0秒";
        long diff = finishTime;
        if (diff <= MILLISECOND) {
            result = "1秒";
        } else if (diff > MILLISECOND && diff < MINUTE) {
            int sec = (int) (diff / MILLISECOND);
            result = sec + "秒";
        } else if (diff > MINUTE && diff < HOUR) {
            int min = (int) (diff / MINUTE);
            result = min + "分钟";
        } else if (diff > HOUR && diff < DAY) {
            int hour = (int) (diff / HOUR);
            result = hour + "小时";
        } else if (diff > DAY) {
            int day = (int) (diff / DAY);
            result = day + "天";
        }
        return result;
    }

    //获取抢红包时间
    public static String getGrabTime(long time) {
        Calendar todayCalendar = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String hourTimeFormat = "HH:mm";
        String dayTimeFormat = "MM-dd HH:mm";
        String yearTimeFormat = "yyyy-MM-dd  HH:mm";
        String result = "";
        if (todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
            if (todayCalendar.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {//当天
                result = getTime(time, hourTimeFormat);
            } else {
                result = getTime(time, dayTimeFormat);
            }
        } else {
            result = getTime(time, yearTimeFormat);
        }
        return result;

    }

    public static String getTime(long time, String timeFormat) {
        SimpleDateFormat format = new SimpleDateFormat(timeFormat);
        return format.format(new Date(time));
    }

    public static String getTransferTime(long time) {
        return getTime(time, "yyyy-MM-dd  HH:mm:ss");
    }
}
