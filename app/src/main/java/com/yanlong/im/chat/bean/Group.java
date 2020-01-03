package com.yanlong.im.chat.bean;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.yanlong.im.user.action.UserAction;

import net.cb.cb.library.utils.StringUtil;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/***
 * 群
 */
public class Group extends RealmObject {
    @PrimaryKey
    private String gid;
    private String name;
    private String avatar;

    private String master;
//    @SerializedName("members")
//    private RealmList<UserInfo> users;

    private RealmList<MemberUser> members;

    private Integer ceiling;
    private Integer saved;// 是否已保存
    private Integer notNotify;// 消息免打扰
    private Integer needVerification;//是否需要群验证
    private Integer contactIntimately;//是否需要群保护
    private Integer wordsNotAllowed;// 全员禁言
    @SerializedName("toTop")
    private Integer isTop;

    private String mygroupName;//我的群昵称
    private String announcement;//群公告
    //机器人id
    private String robotid;
    //名称
    private String robotname;

    private RealmList<Long> viceAdmins;// 管理员

    @Ignore
    private boolean isSelect = false;//本地字段
    @Ignore
    private MemberUser keyUser;//符合搜索条件key的群成员

    private int survivaltime;  // 阅后即焚时间
    private String merchantEntry;// 群机器人网址

    public int getSurvivaltime() {
        return survivaltime;
    }

    public void setSurvivaltime(int survivaltime) {
        this.survivaltime = survivaltime;
    }

    public String getRobotname() {
        return robotname == null ? "" : robotname;
    }

    public void setRobotname(String robotname) {
        this.robotname = robotname;
    }

    public String getRobotid() {
        return robotid == null ? "" : robotid;
    }

    public void setRobotid(String robotid) {
        this.robotid = robotid;
    }

    public String getMygroupName() {
        if (!StringUtil.isNotNull(mygroupName) && members != null) {
            for (MemberUser user : members) {
                if (UserAction.getMyId().longValue() == user.getUid()) {
                    mygroupName = user.getMembername();
                    break;
                }
            }
        }
        return mygroupName;
    }

    public Integer getContactIntimately() {
        if (contactIntimately == null) {
            contactIntimately = 0;
        }
        return contactIntimately;
    }

    public Integer getWordsNotAllowed() {
        if (wordsNotAllowed == null) {
            wordsNotAllowed = 0;
        }
        return wordsNotAllowed;
    }

    public void setWordsNotAllowed(Integer wordsNotAllowed) {
        this.wordsNotAllowed = wordsNotAllowed;
    }

    public RealmList<Long> getViceAdmins() {
        return viceAdmins == null ? new RealmList<Long>() : viceAdmins;
    }

    public void setViceAdmins(RealmList<Long> viceAdmins) {
        this.viceAdmins = viceAdmins;
    }

    public void setContactIntimately(Integer contactIntimately) {
        this.contactIntimately = contactIntimately;
    }

    public String getAnnouncement() {
        return announcement == null ? "" : announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public void setMygroupName(String mygroupName) {
        this.mygroupName = mygroupName;
    }

    public String getGid() {
        return gid == null ? "" : gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

//    public RealmList<UserInfo> getUsers() {
//        users = users == null ? new RealmList<UserInfo>() : users;
//        return users;
//    }
//
//    public void setUsers(RealmList<UserInfo> users) {
//        this.users = users;
//    }

    public RealmList<MemberUser> getUsers() {
        members = members == null ? new RealmList<MemberUser>() : members;
        return members;
    }

    public void setUsers(RealmList<MemberUser> users) {
        this.members = users;
    }

    public String getName() {
        name = name == null ? "" : name;
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar == null ? "" : avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getMaster() {
        return master == null ? "" : master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public Integer getCeiling() {
        if (ceiling == null) {
            ceiling = 0;
        }
        return ceiling;
    }

    public void setCeiling(Integer ceiling) {
        this.ceiling = ceiling;
    }

    public Integer getSaved() {
        return saved == null ? 0 : saved;
    }

    public void setSaved(Integer saved) {
        this.saved = saved;
    }

    public Integer getNotNotify() {
        return notNotify == null ? 0 : notNotify;
    }

    public void setNotNotify(Integer notNotify) {
        this.notNotify = notNotify;
    }

    public Integer getNeedVerification() {
        return needVerification == null ? 0 : needVerification;
    }

    public void setNeedVerification(Integer needVerification) {
        this.needVerification = needVerification;
    }

    public Integer getIsTop() {
        return isTop == null ? 0 : isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public MemberUser getKeyUser() {
        return keyUser;
    }

    public void setKeyUser(MemberUser keyUser) {
        this.keyUser = keyUser;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Group) {
            if (((Group) obj).gid.equals(this.gid)) {
                return true;
            }
        }
        return false;
    }

    public String getMerchantEntry() {
        return merchantEntry;
    }

    public void setMerchantEntry(String merchantEntry) {
        this.merchantEntry = merchantEntry;
    }
}
