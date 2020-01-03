package com.yanlong.im.user.bean;

import net.cb.cb.library.base.BaseBean;

/**
 * @创建人 shenxin
 * @创建时间 2019/8/28 0028 14:13
 */
public class VersionBean extends BaseBean {

    private String time;

    private String version;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
