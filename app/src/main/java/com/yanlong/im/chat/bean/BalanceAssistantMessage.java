package com.yanlong.im.chat.bean;

import android.text.TextUtils;

import com.google.gson.JsonArray;

import net.cb.cb.library.utils.GsonUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * @author Liszt
 * @date 2019/12/14
 * Description
 */
public class BalanceAssistantMessage extends RealmObject implements IMsgContent {
    @PrimaryKey
    String msgId;
    long tradeId;
    int detailType;//详情类型：0-无详情；1-红包详情；2-交易详情
    long time;//
    String title;//标题
    String amountTitle;//金额标题
    long amount;//金额,单位：分
    String items;
    @Ignore
    List<LabelItem> labelItems;

    @Override
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public int getDetailType() {
        return detailType;
    }

    public void setDetailType(int detailType) {
        this.detailType = detailType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public List<LabelItem> getLabelItems() {
        if (labelItems == null && !TextUtils.isEmpty(items)) {
            try {
                labelItems = new ArrayList<>();
                JSONArray array = new JSONArray(items);
                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        LabelItem item = GsonUtils.getObject(array.getString(i), LabelItem.class);
                        if (item != null) {
                            labelItems.add(item);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return labelItems;
    }

    public void setLabelItems(List<LabelItem> labelItems) {
        this.labelItems = labelItems;
    }

    public String getAmountTitle() {
        return amountTitle;
    }

    public void setAmountTitle(String amountTitle) {
        this.amountTitle = amountTitle;
    }

    public String getItems() {
        if (TextUtils.isEmpty(items) && labelItems != null) {
            JsonArray jsonArray = new JsonArray();
            for (int i = 0; i < labelItems.size(); i++) {
                LabelItem item = labelItems.get(i);
                String s = GsonUtils.optObject(item);
                if (!TextUtils.isEmpty(s)) {
                    jsonArray.add(s);
                }
            }
            items = jsonArray.toString();
        }
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }
}
