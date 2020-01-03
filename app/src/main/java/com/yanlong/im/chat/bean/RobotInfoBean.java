package com.yanlong.im.chat.bean;

import com.google.gson.annotations.SerializedName;

import net.cb.cb.library.base.BaseBean;

public class RobotInfoBean extends BaseBean {
    @SerializedName("update_time")
    private Long updateTime;
    @SerializedName("robot_id")
    private String robotId;
    private String rid;
    private String rname;
    private String avatar;
    @SerializedName("merchant_name")
    private String merchantName;
    @SerializedName("robot_description")
    private String robotDescription;
    private String disclaimer;
    private String url;
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
    public Long getUpdateTime() {
        return updateTime;
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public void setRname(String rname) {
        this.rname = rname;
    }
    public String getRname() {
        return rname;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getAvatar() {
        return avatar;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    public String getMerchantName() {
        return merchantName;
    }

    public void setRobotDescription(String robotDescription) {
        this.robotDescription = robotDescription;
    }
    public String getRobotDescription() {
        return robotDescription;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
    public String getDisclaimer() {
        return disclaimer;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
}
