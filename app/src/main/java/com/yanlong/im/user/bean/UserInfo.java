package com.yanlong.im.user.bean;


import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import net.cb.cb.library.manager.Constants;
import net.cb.cb.library.utils.PinyinUtil;
import net.cb.cb.library.utils.StringUtil;
import net.sourceforge.pinyin4j.PinyinHelper;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class UserInfo extends RealmObject implements Comparable<UserInfo> {
    @PrimaryKey
    private Long uid;
    @SerializedName("nickname")
    private String name;//昵称
    @SerializedName("alias")
    private String mkName;//备注名
    @SerializedName("gender")
    private int sex;
    private String imid;
    private String tag;
    @SerializedName("avatar")
    private String head;
    //用户类型 0:陌生人或者群友,1:自己,2:通讯录,3黑名单,4小助手
    private Integer uType;
    private String phone;
    private String oldimid;
    private String neteaseAccid;// 网易id
    private String vip;// (0:普通|1:vip)

    private Integer disturb;///消息免打扰(0:关闭|1:打开)
    private Integer istop;//聊天置顶(0:关闭|1:打开)
    private Integer phonefind;//通过手机号找到自己(0:关闭|1:打开)
    private Integer imidfind;//通过常信号找到自己(0:关闭|1:打开)
    private Integer friendvalid;//加我为朋友时需要验证(0:关闭|1:打开)
    private Integer groupvalid; //允许被直接添加至群聊(0:关闭|1:打开)
    private Integer messagenotice;//新消息通知(0:关闭|1:打开)
    private Integer displaydetail;//显示详情(0:关闭|1:打开)
    private Integer stat; //好友状态(0:正常|1:待同意|2:黑名单|9:系统用户，如小助手)
    private Integer authStat; //身份验证状态(0:未认证|1:已认证未上传证件照|2:已认证已上传证件照)
    private int masterRead;//已读总开关(0:关闭|1:打开)
    private int myRead;//我对100101是否开了已读(0:否|1:是)
    private int friendRead;//100101对我是否开了已读(0:否|1:是)

    private boolean emptyPassword = false;// 是否未设置密码

    //阅后即焚
//    private Integer destroy = 1;
//    private Long destroyTime = 30L;

    @Ignore
    private String membername;//群的昵称
    private String sayHi;//待同意好友招呼语

    private Long lastonline;
    private int activeType; //是否在线（0：离线|1：在线）
    private String describe; //用户描述
    private int lockCloudRedEnvelope; //1锁定红包，0不锁定
    @SerializedName("survivaltime")
    private int destroy = 0; //销毁开关
    private long destroyTime; //销毁时间
    private int joinType;
    private String joinTime;
    private String inviter;
    private String inviterName;
    @Ignore
    private boolean isChecked = false;

    private String bankReqSignKey;//支付签名


    public int getMasterRead() {
        return masterRead;
    }

    public void setMasterRead(int masterRead) {
        this.masterRead = masterRead;
    }

    public int getMyRead() {
        return myRead;
    }

    public void setMyRead(int myRead) {
        this.myRead = myRead;
    }

    public int getFriendRead() {
        return friendRead;
    }

    public void setFriendRead(int friendRead) {
        this.friendRead = friendRead;
    }

    public Integer getDestroy() {
        return destroy;
    }

    public void setDestroy(Integer destroy) {
        this.destroy = destroy;
    }

    public Long getDestroyTime() {
        return destroyTime;
    }

    public void setDestroyTime(Long destroyTime) {
        this.destroyTime = destroyTime;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
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

    public boolean isEmptyPassword() {
        return emptyPassword;
    }

    public void setEmptyPassword(boolean emptyPassword) {
        this.emptyPassword = emptyPassword;
    }

    public int getActiveType() {
        return activeType;
    }

    public void setActiveType(int activeType) {
        this.activeType = activeType;
    }

    public Long getLastonline() {
        if (lastonline == null) {
            lastonline = 0L;
        }
        return lastonline;
    }

    public String getNeteaseAccid() {
        return neteaseAccid;
    }

    public void setNeteaseAccid(String neteaseAccid) {
        this.neteaseAccid = neteaseAccid;
    }

    public void setLastonline(Long lastonline) {
        this.lastonline = lastonline;
    }

    public String getSayHi() {
        return sayHi;
    }

    public void setSayHi(String sayHi) {
        this.sayHi = sayHi;
    }

    public Integer getAuthStat() {
        if (authStat == null) {
            authStat = 0;
        }
        return authStat;
    }

    public void setAuthStat(Integer authStat) {
        this.authStat = authStat;
    }

    public String getMembername() {
        return membername;
    }

    public void setMembername(String membername) {
        this.membername = membername;
    }

    public Integer getMessagenotice() {
        if (messagenotice == null) {
            messagenotice = 0;
        }
        return messagenotice;
    }

    public void setMessagenotice(Integer messagenotice) {
        this.messagenotice = messagenotice;
    }

    public Integer getDisplaydetail() {
        if (displaydetail == null) {
            displaydetail = 0;
        }
        return displaydetail;
    }

    public void setDisplaydetail(Integer displaydetail) {
        this.displaydetail = displaydetail;
    }

    public Integer getStat() {
        if (stat == null) {//stat== null 一定是非好友
            stat = 1;
        }
        return stat;
    }

    public void setStat(Integer stat) {
        this.stat = stat;
    }

    public String getOldimid() {
        return oldimid;
    }

    public void setOldimid(String oldimid) {
        this.oldimid = oldimid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getPhonefind() {
        if (phonefind == null) {
            phonefind = 0;
        }
        return phonefind;
    }

    public void setPhonefind(Integer phonefind) {
        this.phonefind = phonefind;
    }

    public Integer getImidfind() {
        if (imidfind == null) {
            imidfind = 0;
        }
        return imidfind;
    }

    public void setImidfind(Integer imidfind) {
        this.imidfind = imidfind;
    }

    public Integer getFriendvalid() {
        if (friendvalid == null) {
            friendvalid = 0;
        }
        return friendvalid;
    }

    public void setFriendvalid(Integer friendvalid) {
        this.friendvalid = friendvalid;
    }

    public Integer getGroupvalid() {
        if (groupvalid == null) {
            groupvalid = 0;
        }
        return groupvalid;
    }

    public void setGroupvalid(Integer groupvalid) {
        this.groupvalid = groupvalid;
    }

    public Integer getDisturb() {
        if (disturb == null) {
            disturb = 0;
        }
        return disturb;
    }

    public void setDisturb(Integer disturb) {
        this.disturb = disturb;
    }

    public Integer getIstop() {
        if (istop == null) {
            istop = 0;
        }
        return istop;
    }

    public void setIstop(Integer istop) {
        this.istop = istop;
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

    //用户类型 0:陌生人或者群友,1:自己,2:通讯录,3黑名单(不区分和陌生人)
    public Integer getuType() {
        if (uType == null) {
            uType = 0;
        }
        return uType;
    }

    public void setuType(Integer uType) {
        this.uType = uType;
    }


    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    /***
     * 显示的名称
     * @return
     */
    public String getName4Show() {
        return StringUtil.isNotNull(mkName) ? mkName : name;
    }


    public void setName(String name) {

        this.name = name;

        toTag();

    }

    public String getHead() {
        return head == null ? "" : head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getMkName() {
        return mkName;
    }

    public void setMkName(String mkName) {
        this.mkName = mkName;
        toTag();
    }

    /***
     * 重设tag

     */
    public void toTag() {
        String name = StringUtil.isNotNull(this.mkName) ? this.mkName : this.name;
        if (TextUtils.isEmpty(name)) {
            setTag("#");
        } else if (!("" + name.charAt(0)).matches("^[0-9a-zA-Z\\u4e00-\\u9fa5]+$")) {
            setTag("#");
        } else {
            String[] n = PinyinHelper.toHanyuPinyinStringArray(name.charAt(0));
            if (n == null) {
                if (StringUtil.ifContainEmoji(name)) {
                    setTag("#");
                } else {
                    setTag("" + (name.toUpperCase()).charAt(0));
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

    public void setTag(String tag) {
        if ("↑".equals(tag)) {
            tag = "↑";
        } else if (tag.hashCode() < 65 || tag.hashCode() > 91) {
            tag = "#";
        }
        this.tag = tag;
    }

    @Override
    public int compareTo(UserInfo o) {

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

    public String getTag() {
        if (TextUtils.isEmpty(tag)) {
            toTag();
        }
        return TextUtils.isEmpty(tag) ? "#" : tag;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public int getLockCloudRedEnvelope() {
        return lockCloudRedEnvelope;
    }

    public void setLockCloudRedEnvelope(int lockCloudRedEnvelope) {
        this.lockCloudRedEnvelope = lockCloudRedEnvelope;
    }

    public String getBankReqSignKey() {
        return bankReqSignKey;
    }

    public void setBankReqSignKey(String bankReqSignKey) {
        this.bankReqSignKey = bankReqSignKey;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || this.uid == null || ((UserInfo) obj).uid == null) {
            return false;
        }
        if (obj instanceof UserInfo) {
            if (((UserInfo) obj).uid.equals(this.uid)) {
                return true;
            }
        }
        return false;
    }

    //判断该用户是否官方系统用户
    public boolean isSystemUser() {
        if (uid == null) {
            return false;
        }
        if (uid.equals(Constants.CX888_UID) || uid.equals(Constants.CX999_UID) || uid.equals(Constants.CX_HELPER_UID)) {
            return true;
        }
        return false;
    }
}
