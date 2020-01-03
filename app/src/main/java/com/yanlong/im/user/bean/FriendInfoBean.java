package com.yanlong.im.user.bean;

import android.text.TextUtils;

import net.cb.cb.library.base.BaseBean;
import net.cb.cb.library.utils.PinyinUtil;
import net.cb.cb.library.utils.StringUtil;
import net.sourceforge.pinyin4j.PinyinHelper;

public class FriendInfoBean extends BaseBean implements Comparable<FriendInfoBean> {
    private String nickname;
    private Long uid;
    private String avatar;
    private String tag;
    private String imid;
    private int gender;
    private int switchmask;
    private String alias;
    private String phoneremark;

    public String getPhoneremark() {
        return phoneremark;
    }

    public void setPhoneremark(String phoneremark) {
        this.phoneremark = phoneremark;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        toTag();
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getImid() {
        return imid;
    }

    public void setImid(String imid) {
        this.imid = imid;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getSwitchmask() {
        return switchmask;
    }

    public void setSwitchmask(int switchmask) {
        this.switchmask = switchmask;
    }

    /***
     * 重设tag

     */
    public void toTag() {
        if (TextUtils.isEmpty(nickname)) {
            setTag("#");
        } else if (!("" + nickname.charAt(0)).matches("^[0-9a-zA-Z\\u4e00-\\u9fa5]+$")) {
            setTag("#");
        } else {
            String[] n = PinyinHelper.toHanyuPinyinStringArray(nickname.charAt(0));
            if (n == null) {
                if (StringUtil.ifContainEmoji(nickname)) {
                    setTag("#");
                } else {
                    setTag("" + (nickname.toUpperCase()).charAt(0));
                }
            } else {
                String value = "";
                // 判断是否为多音字
                if (n.length > 1) {
                    value = PinyinUtil.getUserName(nickname.charAt(0) + "");
                    if (TextUtils.isEmpty(value)) {
                        setTag("" + n[0].toUpperCase().charAt(0));
                    } else {
                        setTag(value);
                    }
                } else {
                    setTag("" + n[0].toUpperCase().charAt(0));
                }
            }
        }
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        if (tag.hashCode() < 65 || tag.hashCode() > 91) {
            tag = "#";
        }
        this.tag = tag;
    }

    @Override
    public int compareTo(FriendInfoBean o) {
        int last = getTag().charAt(0);
        if (getTag().equals("#")) {
            return 1;
        }
        if (last > o.getTag().charAt(0)) {
            return 1;
        }
        return -1;

    }
}
