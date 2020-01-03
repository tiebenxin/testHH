package com.yanlong.im.user.bean;

import android.net.Uri;

import net.cb.cb.library.base.BaseBean;

/**
 * @创建人 shenxin
 * @创建时间 2019/8/24 0024 14:58
 */
public class ImageBean extends BaseBean {

    private String url;

    private int type; // 0.默认图

    private Uri path;

    public Uri getPath() {
        return path;
    }

    public void setPath(Uri path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
