package net.cb.cb.library.bean;

import net.cb.cb.library.base.BaseBean;

public class AliObsConfigBean extends BaseBean {
    private String accessKeyId;
    private String securityToken;
    // private Date expiration;
    private String accessKeySecret;
    private String cdnEndpoint;
    private String bucket;

    private String endpoint;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getCdnEndpoint() {
        return cdnEndpoint;
    }

    public void setCdnEndpoint(String cdnEndpoint) {
        this.cdnEndpoint = cdnEndpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public String getSecurityToken() {
        return securityToken;
    }

/*    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }
    public Date getExpiration() {
        return expiration;
    }*/

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }
}
