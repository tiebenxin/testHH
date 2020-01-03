package com.yanlong.im.chat.bean;

import net.cb.cb.library.base.BaseBean;

/**
 * @创建人 shenxin
 * @创建时间 2019/8/16 0016 14:53
 */
public class HtmlBeanList extends BaseBean {

    private String id;

    private String name;

    private int type;



    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
