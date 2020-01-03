package com.yanlong.im.user.bean;


import net.cb.cb.library.base.BaseBean;

public class TokenBean extends BaseBean {
    private Long uid;
    private String accessToken;
    private String neteaseAccid;// 网易id
    private String neteaseToken;// 网易token
    public long validTime;//有效时间，有效时间= token获取时间+ 有效期7天的毫秒值

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getUid() {
        return uid;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getNeteaseAccid() {
        return neteaseAccid;
    }

    public void setNeteaseAccid(String neteaseAccid) {
        this.neteaseAccid = neteaseAccid;
    }

    public String getNeteaseToken() {
        return neteaseToken;
    }

    public void setNeteaseToken(String neteaseToken) {
        this.neteaseToken = neteaseToken;
    }

    public long getValidTime() {
        return validTime;
    }

    public void setValidTime(long validTime) {
        this.validTime = validTime;
    }

    /**
     * token是否有效,备注：imId登录问题，未兼容
     */
    public boolean isTokenValid(Long uid) {
        boolean isValid = false;
        if (uid != null && uid.equals(this.uid)) {
            if (System.currentTimeMillis() < this.validTime) {
                isValid = true;
            }
        }

        return isValid;
    }
}
