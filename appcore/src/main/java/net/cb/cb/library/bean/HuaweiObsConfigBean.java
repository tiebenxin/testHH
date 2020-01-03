package net.cb.cb.library.bean;

import net.cb.cb.library.base.BaseBean;

public class HuaweiObsConfigBean extends BaseBean {
    private String bucket;
    private String endpoint;
    private String sk;
    private String ak;
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
    public String getBucket() {
        return bucket;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    public String getEndpoint() {
        return endpoint;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }
    public String getSk() {
        return sk;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }
    public String getAk() {
        return ak;
    }
}
