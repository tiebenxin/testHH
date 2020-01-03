package net.cb.cb.library.utils;

/***
 * 按钮频率点击检测
 * @author jyj
 * @date 2017/6/9
 */
public class BtnClickUtil {
    private static BtnClickUtil clickUtil;
    private long time=0;
    private long time_interval=1000;
    private BtnClickUtil(){}

    public static BtnClickUtil getClickUtil() {
        clickUtil=clickUtil==null?new BtnClickUtil():clickUtil;
        return clickUtil;
    }

    public boolean canClick(){
        long t=System.currentTimeMillis();
        if(t-time>time_interval){
            time=t;
            return true;
        }else{
            return false;
        }


    }

}
