package net.cb.cb.library.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public final static Pattern URL =
            Pattern.compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]", Pattern.MULTILINE | Pattern.DOTALL);

    public final static Pattern EMOJI = Pattern.compile("(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)");

    /**
     * Emoji表情正则表达式
     */
    public static final String PATTERN_FACE_EMOJI = "\\[emoji_[0-9]{3}\\]";

    public static boolean isNotNull(String str) {
        if (str != null && str.length() > 0) {
            return true;
        }
        return false;
    }

    /***
     * 吧HTML转为text
     * @param htmlStr
     * @return
     */
    public static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); //过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); //过滤html标签

        return htmlStr.trim(); //返回文本字符串
    }


    /*
     * @param userRemarkName 好友备注名
     * @param mucNick 用户群昵称
     * @param userNick 用户昵称
     * @param uid 用户uid
     * 优先级：userRemarkName>mucNick>userNick>uid
     * */
    public static String getUserName(String userRemarkName, String mucNick, String userNick, Long uid) {
        String name = uid + "";
        if (!TextUtils.isEmpty(userRemarkName)) {
            name = userRemarkName;
        } else if (!TextUtils.isEmpty(mucNick)) {
            name = mucNick;
        } else if (!TextUtils.isEmpty(userNick)) {
            name = userNick;
        }
        return name;
    }

    public static int[] getVersionArr(String version) {
        if (TextUtils.isEmpty(version)) {
            return null;
        }
        String[] a = version.split("-");
        String oVersion = a[0];
        String[] oldArr = oVersion.split(".");
        int[] arr = null;
        if (oldArr != null && oldArr.length == 3) {
            arr = new int[3];
            arr[0] = Integer.valueOf(oldArr[0]);
            arr[1] = Integer.valueOf(oldArr[1]);
            arr[2] = Integer.valueOf(oldArr[2]);
        }
        return arr;
    }

    /**
     * 截取之前，检测是否包含emoji
     *
     * @param content
     * @param start
     * @param end
     * @return
     */
    public static String splitEmojiString(String content, int start, int end) {
        Pattern pattern = Pattern.compile(PATTERN_FACE_EMOJI, Pattern.CASE_INSENSITIVE); // 通过传入的正则表达式来生成一个pattern
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            int first = matcher.start();
            int last = matcher.end();
            if (first < start && last > start) {
                start = first;
            } else if (first < end && last > end) {
                end = first;//只能少，不能多，左闭右开
            }
        }
        return content.substring(start, end);
    }

    /**
     * 字符串是否包含emoji
     *
     * @param content
     * @return 备注：一个emoji在String中占2个长度
     */
    public static boolean ifContainEmoji(String content) {
        return EMOJI.matcher(content).find();
    }

    /**
     * 获取APK渠道号
     *
     * @param context
     * @return
     */
    public static String getChannelName(Context context) {
        if (context == null) {
            return "default_android";
        }
        String resultData = "default_android";
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo，因为友盟设置的meta-data是在application标签中
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        //key要与manifest中的配置文件标识一致
                        resultData = applicationInfo.metaData.getString("UMENG_CHANNEL");
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            return "default_android";
        }
        return resultData;
    }

    /**
     * 判断某个Activity 界面是否在前台
     *
     * @param context
     * @param className 某个界面名称
     * @return
     */
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }

        return false;
    }

    /*
     * String to hexString
     * */
    public String stringToHex16String(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder();
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }

    /*
     * hexString to String
     * */
    public String hexStrToString(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    public static long getLong(String s) {
        long result = 0;
        if (!TextUtils.isEmpty(s)) {
            try {
                double money = Double.parseDouble(s);
                result = (long) (money * 100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
