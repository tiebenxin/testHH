package com.yanlong.im.chat.bean;

import com.yanlong.im.chat.ChatEnum;

import net.cb.cb.library.base.BaseBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @创建人 shenxin
 * @创建时间 2019/8/22 0022 16:09
 */
public class HtmlBean extends BaseBean {

    private String gid;
    private List<HtmlBeanList> list = new ArrayList<>();

    @ChatEnum.ETagType
    private int tagType;

    public List<HtmlBeanList> getList() {
        return list;
    }

    public void setList(List<HtmlBeanList> list) {
        this.list = list;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public int getTagType() {
        return tagType;
    }

    public void setTagType(int tagType) {
        this.tagType = tagType;
    }
}
