package net.cb.cb.library.utils;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;

public class UpLoadUtils {
    private Context mContext;
    private static UpLoadUtils mInstance=null;
    public UpLoadUtils(){

    }
    public static UpLoadUtils getInstance(){
        if (null==mInstance){
            mInstance=new UpLoadUtils();
        }
        return mInstance;
    }
    public void init(Context context){
        this.mContext=context;
    }
    public void upLoadLog(String text){
        MobclickAgent.reportError(mContext, text);//errorContent是String格式
    }
}
