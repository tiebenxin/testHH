package net.cb.cb.library.bean;

import com.google.gson.annotations.SerializedName;

import net.cb.cb.library.base.BaseBean;

/***
 * @author jyj
 * @date 2016/12/20
 */
public class ReturnBean<T> extends BaseBean {
    @SerializedName("errCode")
    Long code;
    @SerializedName("errMsg")
    String msg;
    T data;

    public Boolean isOk() {
        return code.longValue()==0l;
    }
    public Long getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }
}
