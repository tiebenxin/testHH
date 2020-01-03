package com.yanlong.im.user.bean;

import net.cb.cb.library.base.BaseBean;

/**
 * @创建人 shenxin
 * @创建时间 2019/7/26 0026 15:16
 */
public class BackgroundImageBean extends BaseBean {

    private int image;

    private boolean isSelect;

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
