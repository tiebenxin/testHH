package com.yanlong.im.chat.bean;


import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import net.cb.cb.library.utils.PinyinUtil;
import net.cb.cb.library.utils.StringUtil;
import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/*
 * 群成员，用户表
 * @Required 配置不能为null
 * */
public class MemberUser extends RealmObject implements Comparable<MemberUser> {
    @PrimaryKey
    private String memberId;//群成员id=gid+uid;
    private long uid;
    //    @Required
    private String gid;
    @SerializedName("nickname")
//    @Required
    private String name;//昵称
    @SerializedName("gender")
    private int sex;
    //    @Required
    private String imid;//常信号
    @SerializedName("avatar")
//    @Required
    private String head;//头像
    //    @Required
    private String membername;//群的昵称
    private int joinType;
    //    @Required
    private String joinTime;
    //    @Required
    private String inviter;
    //    @Required
    private String inviterName;
    //    @Required
    private String tag;
    @Ignore
    private boolean isChecked = false;

    /*
     * 初始化gid,memberId,和tag
     * */
    public void init(String gid) {
        this.gid = gid;
        memberId = gid + uid;
        getTag();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getInviterName() {
        return inviterName;
    }

    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }

    public int getJoinType() {
        return joinType;
    }

    public void setJoinType(int joinType) {
        this.joinType = joinType;
    }

    public String getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(String joinTime) {
        this.joinTime = joinTime;
    }

    public String getInviter() {
        return inviter;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    public String getMembername() {
        return membername;
    }

    public void setMembername(String membername) {
        this.membername = membername;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getImid() {
        return imid;
    }

    public void setImid(String imid) {
        this.imid = imid;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getHead() {
        return head == null ? "" : head;
    }

    public void setHead(String head) {
        this.head = head;
    }


    public void setTag(String tag) {
        Pattern pattern = Pattern.compile("[0-9]");
        Matcher isNum = pattern.matcher(tag);
        if (isNum.matches()) {
            tag = "#";
        }

        this.tag = tag;
    }

    public String getTag() {
        if (TextUtils.isEmpty(tag)) {
            String name = StringUtil.isNotNull(this.membername) ? this.membername : this.name;
            if (TextUtils.isEmpty(name)) {
                tag = "#";
            } else if (!("" + name.charAt(0)).matches("^[0-9a-zA-Z\\u4e00-\\u9fa5]+$")) {
                tag = "#";
            } else {
                String[] n = PinyinHelper.toHanyuPinyinStringArray(name.charAt(0));
                if (n == null) {
                    if (StringUtil.ifContainEmoji(name)) {
                        tag = "#";
                    } else {
                        tag = name.toUpperCase().charAt(0) + "";
                    }
                } else {
                    String value = "";
                    // 判断是否为多音字
                    if (n.length > 1) {
                        value = PinyinUtil.getUserName(name.charAt(0) + "");
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
        return tag;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    //获取需要展示的名字
    public String getShowName() {
        return TextUtils.isEmpty(membername) ? name : membername;
    }

    @Override
    public int compareTo(MemberUser o) {

        int last = getTag().charAt(0);
        if (getTag().equals("#")) {
            return 1;
        }
        if (o.getTag().equals("#")) {
            return -1;
        }

        if (getTag().equals("↑")) {
            return -1;
        }
        if (o.getTag().equals("↑")) {
            return 1;
        }

        if (last > o.getTag().charAt(0)) {
            return 1;
        } else if (last < o.getTag().charAt(0)) {
            return -1;
        } else {
            return 0;
        }
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof MemberUser) {
            if (((MemberUser) obj).uid == this.uid) {
                return true;
            }
        }
        return false;
    }
}
