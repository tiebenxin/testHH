package com.yanlong.im.chat.bean;

import com.google.gson.annotations.SerializedName;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/12/14
 * Description
 */
public class LabelItem extends BaseBean {
//    @PrimaryKey
//    long tradeId;//订单id
    @SerializedName("label_")
    String label = "";
    @SerializedName("value_")
    String value = "";
//
//    public long getTradeId() {
//        return tradeId;
//    }
//
//    public void setTradeId(long tradeId) {
//        this.tradeId = tradeId;
//    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
