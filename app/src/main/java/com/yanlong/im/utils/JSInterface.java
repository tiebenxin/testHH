package com.yanlong.im.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.yanlong.im.user.ui.FeedbackActivity;

import net.cb.cb.library.utils.LogUtil;

public class JSInterface {
    private Context mContext;
    public JSInterface(Context context){
        this.mContext=context;
    }

    @JavascriptInterface
    public void callAndroidMethod(){
        LogUtil.getLog().e("TAG","callMETHOD"+"");
        mContext.startActivity(new Intent(mContext, FeedbackActivity.class));
    }
}
