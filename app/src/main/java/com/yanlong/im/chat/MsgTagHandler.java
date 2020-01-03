package com.yanlong.im.chat;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.yanlong.im.R;
import com.yanlong.im.chat.interf.IActionTagClickListener;

import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Stack;

/**
 * Created by xhdl0002 on 2018/3/27.
 * action信息解析
 */
public class MsgTagHandler implements TagHandler {


    private IActionTagClickListener actionListener;


    public static final String ENVELOPE = "envelope";//红包或者转账
    public static final String USER = "user";//用户

    private int sIndex = 0;
    private int eIndex = 0;
    //是否解析
    private boolean isBilder;
    private final Context mContext;
    private String msgId;
    /**
     * html 标签的开始下标
     */
    private Stack<Integer> startIndex;
    /**
     * html的标签的属性值
     */
    private Stack<String> propertyValue;

    public MsgTagHandler(Context context, boolean isBilder, String msgId) {
        mContext = context;
        this.msgId = msgId;
        this.isBilder = isBilder;
    }

    public MsgTagHandler(Context context, boolean isBilder, String msgId,
                         IActionTagClickListener listener) {
        mContext = context;
        this.msgId = msgId;
        this.isBilder = isBilder;
        actionListener = listener;
    }

    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        // TODO Auto-generated method stub
        try {
            if (opening) {
                sIndex = output.length();
                if (isBilder) {
                    handlerStartTAG(tag, output, xmlReader);
                }
            } else {
                eIndex = output.length();
                if (isBilder) {
                    handlerEndTAG(tag, output);
                } else {
                    output.setSpan(
                            new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.gray_99)),
                            sIndex, eIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 处理开始的标签位
     */
    private void handlerStartTAG(String tag, Editable output, XMLReader xmlReader) {
        handlerStartUser(output, xmlReader, tag);
    }

    /**
     * 处理结尾的标签位
     */
    private void handlerEndTAG(String tag, Editable output) {
        handlerEndUser(output, tag);
    }

    private void handlerStartUser(Editable output, XMLReader xmlReader, String tag) {
        if (startIndex == null) {
            startIndex = new Stack<>();
        }
        startIndex.push(output.length());

        if (propertyValue == null) {
            propertyValue = new Stack<>();
        }
        if (tag.equalsIgnoreCase(USER)) {
            propertyValue.push(getProperty(xmlReader, "id"));
        } else if (tag.equalsIgnoreCase(ENVELOPE)) {
            propertyValue.push(getProperty(xmlReader, "id"));
        }
    }

    private void handlerEndUser(Editable output, String tag) {

        if (!isEmpty(propertyValue)) {
            try {
                String id = propertyValue.pop();
                output.setSpan(new MxgsaSpan(id, output.subSequence(sIndex, eIndex).toString(), tag),
                        sIndex, eIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 集合是否为空
     */
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    private class MxgsaSpan extends ClickableSpan {

        private String id;
        private String nick;
        private String tag;

        public MxgsaSpan(String id, String nick, String tag) {
            this.id = id;
            this.nick = nick;
            this.tag = tag;
        }

        @Override
        public void onClick(View widget) {
            // TODO Auto-generated method stub
            if (tag.equalsIgnoreCase(USER)) {
                if (actionListener != null && !TextUtils.isEmpty(id)) {
                    actionListener.clickUser(id);
                }
            } else if (tag.equalsIgnoreCase(ENVELOPE)) {
                if (actionListener != null && !TextUtils.isEmpty(id)) {
                    actionListener.clickUser(id);
                }
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            if (tag.equalsIgnoreCase(USER)) {
                ds.setColor(ContextCompat.getColor(mContext, R.color.msg_tag_color));
            } else if (tag.equalsIgnoreCase(ENVELOPE)) {
                ds.setColor(Color.GREEN);
            }
            ds.setUnderlineText(true);
        }

    }

    /**
     * 利用反射获取html标签的属性值
     */
    private String getProperty(XMLReader xmlReader, String property) {
        try {
            Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            Object element = elementField.get(xmlReader);
            Field attsField = element.getClass().getDeclaredField("theAtts");
            attsField.setAccessible(true);
            Object atts = attsField.get(element);
            Field dataField = atts.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            String[] data = (String[]) dataField.get(atts);
            Field lengthField = atts.getClass().getDeclaredField("length");
            lengthField.setAccessible(true);
            int len = (Integer) lengthField.get(atts);
            for (int i = 0; i < len; i++) {
                // 这边的property换成你自己的属性名就可以了
                if (property.equals(data[i * 5 + 1])) {
                    return data[i * 5 + 4];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
