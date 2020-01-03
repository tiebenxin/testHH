package com.yanlong.im.chat.dao;

import android.text.TextUtils;

import com.hm.cxpay.global.PayEnum;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.ApplyBean;
import com.yanlong.im.chat.bean.AssistantMessage;
import com.yanlong.im.chat.bean.AtMessage;
import com.yanlong.im.chat.bean.BusinessCardMessage;
import com.yanlong.im.chat.bean.ChangeSurvivalTimeMessage;
import com.yanlong.im.chat.bean.ChatMessage;
import com.yanlong.im.chat.bean.EnvelopeInfo;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.GroupConfig;
import com.yanlong.im.chat.bean.GroupImageHead;
import com.yanlong.im.chat.bean.ImageMessage;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.MsgCancel;
import com.yanlong.im.chat.bean.MsgNotice;
import com.yanlong.im.chat.bean.ReceiveRedEnvelopeMessage;
import com.yanlong.im.chat.bean.RedEnvelopeMessage;
import com.yanlong.im.chat.bean.Remind;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.bean.StampMessage;
import com.yanlong.im.chat.bean.TransferMessage;
import com.yanlong.im.chat.bean.UserSeting;
import com.yanlong.im.chat.bean.VideoMessage;
import com.yanlong.im.chat.bean.VoiceMessage;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.utils.DaoUtil;
import com.yanlong.im.utils.ReadDestroyUtil;
import com.yanlong.im.utils.socket.MsgBean;
import com.yanlong.im.utils.socket.SocketData;

import net.cb.cb.library.bean.EventRefreshChat;
import net.cb.cb.library.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class MsgDao {
    //分页数量
    private int pSize = 10;

    public Group getGroup4Id(String gid) {
        return DaoUtil.findOne(Group.class, "gid", gid);
    }

    /***
     * 保存群
     * @param group
     */
    public void groupSave(Group group) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Group g = realm.where(Group.class).equalTo("gid", group.getGid()).findFirst();
            if (null != g) {//已经存在
                try {
                    List<MemberUser> objects = g.getUsers();
                    if (null != objects && objects.size() > 0) {
                        g.setName(group.getName());
                        g.setAvatar(group.getAvatar());
                        if (group.getUsers() != null)
                            g.setUsers(group.getUsers());
                        realm.insertOrUpdate(group);
                    }
                } catch (Exception e) {
                    return;
                }
            } else {//不存在
                realm.insertOrUpdate(group);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.reportException(e);
            DaoUtil.close(realm);
        }
    }


    /***
     * 保存群
     * @param groups 群列表
     */
    public void saveGroups(List<Group> groups) {
        if (groups == null || groups.size() <= 0) {
            return;
        }
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            int len = groups.size();
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    Group group = groups.get(i);
                    List<MemberUser> memberUsers = group.getUsers();
                    if (memberUsers != null) {
                        int size = memberUsers.size();
                        for (int j = 0; j < size; j++) {
                            MemberUser memberUser = memberUsers.get(j);
                            memberUser.init(group.getGid());
                        }
                    }
                }
                realm.copyToRealmOrUpdate(groups);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.reportException(e);
            DaoUtil.close(realm);
        }
    }


    /***
     * 保存群
     * @param gid 群id
     * @param imgHead 群头像
     */
    public boolean groupSaveJustImgHead(String gid, String imgHead) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        Group g = realm.where(Group.class).equalTo("gid", gid).findFirst();
        if (g != null) {//已经存在
            g.setAvatar(imgHead);
            realm.insertOrUpdate(g);

        } else {//不存在
//            realm.insertOrUpdate(g);
            return false;
            // sessionCreate(group.getGid(),null);
        }
        realm.commitTransaction();
        realm.close();
        return true;
        //return DaoUtil.findOne(Group.class, "gid", gid);
    }




    /**
     * 查询已读的阅后即焚消息
     */
    public List<MsgAllBean> getMsg4SurvivalTimeAndRead(Long userid) {
        List<MsgAllBean> beans;
        Realm realm = DaoUtil.open();
        RealmResults list = realm.where(MsgAllBean.class)
                .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                .and().beginGroup().equalTo("to_uid", userid).endGroup()
                .and().greaterThan("survival_time", 0)
                .and().greaterThan("read", 0)
                .findAll();
        beans = realm.copyFromRealm(list);
        realm.close();
        return beans;
    }


    /**
     * 查询当前会话退出即焚消息
     */
    public List<MsgAllBean> getMsg4SurvivalTimeAndExit(String gid, Long userid) {
        List<MsgAllBean> beans;
        Realm realm = DaoUtil.open();
        if (!TextUtils.isEmpty(gid)) {
            RealmResults list = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("gid", gid).endGroup()
                    .and()
                    .beginGroup().lessThan("survival_time", 0).endGroup()
                    .findAll();
            beans = realm.copyFromRealm(list);
        } else {
            RealmResults list = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                    .and()
                    .beginGroup().equalTo("to_uid", userid).or().equalTo("from_uid", userid).endGroup()
                    .and()
                    .beginGroup().lessThan("survival_time", 0).endGroup()
                    .findAll();
            beans = realm.copyFromRealm(list);
        }
        realm.close();
        return beans;
    }


    /**
     * 查询所有查看过的阅后即焚消息
     */
    public List<MsgAllBean> getMsg4SurvivalTime() {
        List<MsgAllBean> beans = new ArrayList<>();
        Realm realm = DaoUtil.open();
        RealmResults list = realm.where(MsgAllBean.class)
                .greaterThan("endTime", 0)
                .findAll();
        beans = realm.copyFromRealm(list);
        realm.close();
        return beans;
    }


    /**
     * 设置阅后即焚销毁时间 和开始时间
     */
    public void setMsgEndTime(long time, long startTime, String msgid) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        MsgAllBean msgAllBean = realm.where(MsgAllBean.class)
                .equalTo("msg_id", msgid).findFirst();
        if (msgAllBean != null) {
            msgAllBean.setEndTime(time);
            msgAllBean.setStartTime(startTime);
            realm.insertOrUpdate(msgAllBean);
        }
        realm.commitTransaction();
        realm.close();
    }


    public List<MsgAllBean> getMsg4User(Long userid, Long time) {
        if (time == null) {
            time = 99999999999999l;
        }
        List<MsgAllBean> beans = new ArrayList<>();
        Realm realm = DaoUtil.open();

        RealmResults list = realm.where(MsgAllBean.class).beginGroup().equalTo("gid", "").or().isNull("gid").endGroup().and().beginGroup()
                .equalTo("from_uid", userid).or().equalTo("to_uid", userid).endGroup()
                .lessThan("timestamp", time)
                .sort("timestamp", Sort.DESCENDING)
                .limit(20)
                .findAll();

        beans = realm.copyFromRealm(list);
        //翻转列表
        Collections.reverse(beans);
        realm.close();
        return beans;
    }

    public List<MsgAllBean> getMsg4User(Long userid, Long time, boolean isNew) {
        if (time == null) {
            time = 99999999999999l;
        }
        List<MsgAllBean> beans = null;
        Realm realm = DaoUtil.open();
        try {
            beans = new ArrayList<>();
            RealmResults list;
            if (isNew) {
                list = realm.where(MsgAllBean.class)
                        .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                        .and()
                        .beginGroup().equalTo("from_uid", userid).or().equalTo("to_uid", userid).endGroup()
                        .greaterThan("timestamp", time)
                        .sort("timestamp", Sort.DESCENDING)
//                    .limit(20)
                        .findAll();
            } else {
                list = realm.where(MsgAllBean.class)
                        .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                        .and()
                        .beginGroup().equalTo("from_uid", userid).or().equalTo("to_uid", userid).endGroup()
                        .lessThan("timestamp", time)
                        .sort("timestamp", Sort.DESCENDING)
                        .limit(20)
                        .findAll();
            }
            beans = realm.copyFromRealm(list);
            //翻转列表
            Collections.reverse(beans);
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.reportException(e);
            DaoUtil.close(realm);
        }
        return beans;
    }

    public List<MsgAllBean> getMsg4User(Long userid, Long time, int size) {
        if (time == null) {
            time = 99999999999999l;
        }
        List<MsgAllBean> beans = null;
        Realm realm = DaoUtil.open();
        try {
            beans = new ArrayList<>();
            RealmResults list = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                    .and()
                    .beginGroup().equalTo("from_uid", userid).or().equalTo("to_uid", userid).endGroup()
                    .lessThan("timestamp", time)
                    .sort("timestamp", Sort.DESCENDING)
                    .limit(size)
                    .findAll();

            beans = realm.copyFromRealm(list);
            //翻转列表
            Collections.reverse(beans);
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return beans;
    }

    public List<MsgAllBean> getMsg4UserImg(Long userid) {

        List<MsgAllBean> beans = null;
        Realm realm = DaoUtil.open();
        try {
            beans = new ArrayList<>();

            RealmResults list = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                    .beginGroup().equalTo("from_uid", userid).or().equalTo("to_uid", userid).endGroup()
                    .beginGroup().equalTo("msg_type", 4).endGroup()
                    .sort("timestamp", Sort.DESCENDING)
                    .findAll();
            beans = realm.copyFromRealm(list);
            //翻转列表
            Collections.reverse(beans);
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return beans;
    }

    /*
     * @param isNew true加载最新数据，false加载更多历史数据
     * */
    public List<MsgAllBean> getMsg4Group(String gid, Long time, boolean isNew) {
        if (time == null) {
            time = 99999999999999l;
        }
        List<MsgAllBean> beans = null;
        Realm realm = DaoUtil.open();
        try {
            beans = new ArrayList<>();
            RealmResults list;
            if (isNew) {
                list = realm.where(MsgAllBean.class)
                        .equalTo("gid", gid)
                        .greaterThan("timestamp", time)
                        .sort("timestamp", Sort.DESCENDING)
                        .findAll();
            } else {
                list = realm.where(MsgAllBean.class)
                        .equalTo("gid", gid)
                        .lessThan("timestamp", time)
                        .sort("timestamp", Sort.DESCENDING)
                        .limit(20)
                        .findAll();
            }

            beans = realm.copyFromRealm(list);
            //翻转列表
            Collections.reverse(beans);
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return beans;
    }

    public List<MsgAllBean> getMsg4Group(String gid, Long time, int size) {
        if (time == null) {
            time = 99999999999999l;
        }
        List<MsgAllBean> beans = null;
        Realm realm = DaoUtil.open();
        try {
            beans = new ArrayList<>();
            RealmResults list = realm.where(MsgAllBean.class)
                    .equalTo("gid", gid)
                    .lessThan("timestamp", time)
                    .sort("timestamp", Sort.DESCENDING)
                    .limit(size)
                    .findAll();

            beans = realm.copyFromRealm(list);
            //翻转列表
            Collections.reverse(beans);
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return beans;
    }

    public List<MsgAllBean> getMsg4GroupImg(String gid) {

        List<MsgAllBean> beans = null;
        Realm realm = DaoUtil.open();
        try {
            beans = new ArrayList<>();
            RealmResults list = realm.where(MsgAllBean.class)
                    .equalTo("gid", gid)
                    .equalTo("msg_type", 4)
                    .sort("timestamp", Sort.DESCENDING)
                    .findAll();

            beans = realm.copyFromRealm(list);
            //翻转列表
            Collections.reverse(beans);
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return beans;
    }

    public List<MsgAllBean> getMsg4GroupHistory(String gid, Long stime) {
        List<MsgAllBean> beans = null;
        Realm realm = DaoUtil.open();
        try {
            beans = new ArrayList<>();
            RealmResults list = realm.where(MsgAllBean.class)
                    .equalTo("gid", gid)
                    //  .lessThan("timestamp",time)
                    .greaterThanOrEqualTo("timestamp", stime)
                    .sort("timestamp", Sort.DESCENDING)
                    .findAll();

            beans = realm.copyFromRealm(list);
            //翻转列表
            Collections.reverse(beans);
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return beans;
    }

    public List<MsgAllBean> getMsg4UserHistory(Long userid, Long stime) {

        // Long  time=99999999999999l;
        List<MsgAllBean> beans = new ArrayList<>();
        Realm realm = DaoUtil.open();
        try {
            RealmResults list = realm.where(MsgAllBean.class).beginGroup().equalTo("gid", "").or().isNull("gid").endGroup().and().beginGroup()
                    .equalTo("from_uid", userid).or().equalTo("to_uid", userid).endGroup()
                    //   .lessThan("timestamp",time)
                    .greaterThanOrEqualTo("timestamp", stime)
                    .sort("timestamp", Sort.DESCENDING)
                    .findAll();

            beans = realm.copyFromRealm(list);
            //翻转列表
            Collections.reverse(beans);
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return beans;
    }

    /***
     * 保存群成员到数据库
     * @param
     */
    public void groupNumberSave(Group ginfo) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            //更新信息到群成员列表
//            RealmList<MemberUser> nums = new RealmList<>();
            //更新信息到用户表
            for (MemberUser sv : ginfo.getUsers()) {
                sv.init(ginfo.getGid());

            }
            //更新自己的群昵称
//            ginfo.getMygroupName();
//            ginfo.setUsers(nums);
            realm.insertOrUpdate(ginfo);
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }


    /***
     * 离线获取群信息
     * @param gid
     * @return
     */
    public Group groupNumberGet(String gid) {
        Group groupInfoBean = null;
        Realm realm = DaoUtil.open();
        try {
            groupInfoBean = new Group();
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group != null) {
                groupInfoBean = realm.copyFromRealm(group);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return groupInfoBean;
    }

    /***
     * 删除聊天记录
     * @param toUid
     * @param gid
     */
    public void msgDel(Long toUid, String gid) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            RealmResults<MsgAllBean> list = null;
            if (StringUtil.isNotNull(gid)) {
                list = realm.where(MsgAllBean.class)
                        .beginGroup().equalTo("gid", gid).endGroup()
                        .and()
                        .beginGroup().notEqualTo("msg_type", ChatEnum.EMessageType.LOCK).endGroup()
                        .findAll();
            } else {
                list = realm.where(MsgAllBean.class)
                        .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                        .and()
                        .beginGroup().notEqualTo("msg_type", ChatEnum.EMessageType.LOCK).endGroup()
                        .and()
                        .beginGroup().equalTo("from_uid", toUid).or().equalTo("to_uid", toUid).endGroup()
                        .findAll();

            }

            //删除前先把子表数据干掉!!切记
            if (list != null) {
                for (MsgAllBean msg : list) {
                    if (msg.getReceive_red_envelope() != null)
                        msg.getReceive_red_envelope().deleteFromRealm();
                    if (msg.getMsgNotice() != null)
                        msg.getMsgNotice().deleteFromRealm();
                    if (msg.getBusiness_card() != null)
                        msg.getBusiness_card().deleteFromRealm();
                    if (msg.getStamp() != null)
                        msg.getStamp().deleteFromRealm();
                    if (msg.getChat() != null)
                        msg.getChat().deleteFromRealm();
                    if (msg.getImage() != null)
                        msg.getImage().deleteFromRealm();
                    if (msg.getRed_envelope() != null)
                        msg.getRed_envelope().deleteFromRealm();
                    if (msg.getTransfer() != null)
                        msg.getTransfer().deleteFromRealm();
                    if (msg.getVoiceMessage() != null)
                        msg.getVoiceMessage().deleteFromRealm();
                }
                list.deleteAllFromRealm();
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    /***
     * 单删某条
     * @param msgId
     */
    public void msgDel4MsgId(String msgId) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            RealmResults<MsgAllBean> list = null;

            list = realm.where(MsgAllBean.class).equalTo("msg_id", msgId).findAll();
            //删除前先把子表数据干掉!!切记
            if (list != null) {
                for (MsgAllBean msg : list) {
                    if (msg.getReceive_red_envelope() != null)
                        msg.getReceive_red_envelope().deleteFromRealm();
                    if (msg.getMsgNotice() != null)
                        msg.getMsgNotice().deleteFromRealm();
                    if (msg.getBusiness_card() != null)
                        msg.getBusiness_card().deleteFromRealm();
                    if (msg.getStamp() != null)
                        msg.getStamp().deleteFromRealm();
                    if (msg.getChat() != null)
                        msg.getChat().deleteFromRealm();
                    if (msg.getImage() != null)
                        msg.getImage().deleteFromRealm();
                    if (msg.getRed_envelope() != null)
                        msg.getRed_envelope().deleteFromRealm();
                    if (msg.getTransfer() != null)
                        msg.getTransfer().deleteFromRealm();
                    if (msg.getMsgCancel() != null)
                        msg.getMsgCancel().deleteFromRealm();
                    if (msg.getVoiceMessage() != null)
                        msg.getVoiceMessage().deleteFromRealm();

                }
                list.deleteAllFromRealm();
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }


    /**
     * 撤回消息
     *
     * @param msgid       消息ID
     * @param msgCancelId
     */
    public void msgDel4Cancel(String msgid, String msgCancelId) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            RealmResults<MsgAllBean> list = null;

            list = realm.where(MsgAllBean.class).equalTo("msg_id", msgCancelId).findAll();
            MsgAllBean cancel = realm.where(MsgAllBean.class).equalTo("msg_id", msgid).findFirst();
            if (cancel == null && list != null && list.size() > 0) {
                MsgAllBean bean = list.get(0);
                if (TextUtils.isEmpty(bean.getMsg_id())) {
                    return;
                }

                cancel = new MsgAllBean();
                cancel.setMsg_id(msgid);
                cancel.setRequest_id("" + System.currentTimeMillis());
                cancel.setFrom_uid(bean.getTo_uid());
                cancel.setTo_uid(UserAction.getMyId());
                cancel.setGid(bean.getGid());
                cancel.setMsg_type(ChatEnum.EMessageType.MSG_CANCEL);

                int survivaltime = new UserDao().getReadDestroy(bean.getTo_uid(), bean.getGid());
                MsgCancel msgCel = new MsgCancel();
                msgCel.setMsgid(msgid);
                msgCel.setNote("你撤回了一条消息");
                msgCel.setMsgidCancel(msgCancelId);
                cancel.setSurvival_time(survivaltime);
                cancel.setMsgCancel(msgCel);
            }

            //删除前先把子表数据干掉!!切记
            if (list != null) {
                for (MsgAllBean msg : list) {
                    if (msg.getReceive_red_envelope() != null)
                        msg.getReceive_red_envelope().deleteFromRealm();
                    if (msg.getMsgNotice() != null)
                        msg.getMsgNotice().deleteFromRealm();
                    if (msg.getBusiness_card() != null)
                        msg.getBusiness_card().deleteFromRealm();
                    if (msg.getStamp() != null)
                        msg.getStamp().deleteFromRealm();
                    if (msg.getChat() != null)
                        msg.getChat().deleteFromRealm();
                    if (msg.getImage() != null)
                        msg.getImage().deleteFromRealm();
                    if (msg.getRed_envelope() != null)
                        msg.getRed_envelope().deleteFromRealm();
                    if (msg.getTransfer() != null)
                        msg.getTransfer().deleteFromRealm();
                    if (msg.getMsgCancel() != null)
                        msg.getMsgCancel().deleteFromRealm();

                    if (cancel != null) {
                        cancel.setTimestamp(msg.getTimestamp());
                        realm.insertOrUpdate(cancel);
                    }
                }
                list.deleteAllFromRealm();
            }


            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }


    /**
     * 阅后即焚消息
     */
    public void msgSurvivalTime(String msgid, String gid, long uid, String nickname, String avatar, int survivalTime) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            //MsgAllBean msgAllBean = realm.where(MsgAllBean.class).equalTo("msg_id", msgid).findFirst();

            MsgAllBean msgAllBean = new MsgAllBean();
            msgAllBean.setMsg_type(ChatEnum.EMessageType.CHANGE_SURVIVAL_TIME);
            msgAllBean.setMsg_id(msgid);
            if (!TextUtils.isEmpty(gid)) {

            } else {
                msgAllBean.setFrom_uid(uid);
                msgAllBean.setFrom_avatar(avatar);
                msgAllBean.setFrom_nickname(nickname);

            }
            MsgNotice notice = new MsgNotice();
            if (survivalTime == -1) {
                notice.setNote(nickname + "设置了退出即焚");
            } else if (survivalTime == 0) {
                notice.setNote(nickname + "取消了阅后即焚");
            } else {
                notice.setNote(nickname + "设置了消息10s后消失");
            }
            msgAllBean.setMsgNotice(notice);
            ChangeSurvivalTimeMessage message = new ChangeSurvivalTimeMessage();
            message.setSurvival_time(survivalTime);
            msgAllBean.setChangeSurvivalTimeMessage(message);
            realm.insertOrUpdate(msgAllBean);
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
        }
    }


    /***
     * 清除所有的聊天记录
     */
    public void msgDelAll() {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            realm.where(MsgAllBean.class).findAll().deleteAllFromRealm();
            //这里要清除关联表
            realm.where(ChatMessage.class).findAll().deleteAllFromRealm();
            realm.where(ImageMessage.class).findAll().deleteAllFromRealm();
            realm.where(RedEnvelopeMessage.class).findAll().deleteAllFromRealm();
            realm.where(ReceiveRedEnvelopeMessage.class).findAll().deleteAllFromRealm();
            realm.where(TransferMessage.class).findAll().deleteAllFromRealm();
            realm.where(StampMessage.class).findAll().deleteAllFromRealm();
            realm.where(BusinessCardMessage.class).findAll().deleteAllFromRealm();
            realm.where(MsgNotice.class).findAll().deleteAllFromRealm();
            realm.where(MsgCancel.class).findAll().deleteAllFromRealm();
            realm.where(VoiceMessage.class).findAll().deleteAllFromRealm();
            realm.where(AtMessage.class).findAll().deleteAllFromRealm();
            realm.where(AssistantMessage.class).findAll().deleteAllFromRealm();
            realm.where(VideoMessage.class).findAll().deleteAllFromRealm();

            //清理角标
            RealmResults<Session> sessions = realm.where(Session.class).findAll();
            for (Session session : sessions) {
                session.setUnread_count(0);
                realm.insertOrUpdate(session);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }

    }


    /***
     * 创建群
     * @param id
     * @param avatar
     * @param name
     * @param listDataTop
     */
    public void groupCreate(String id, String avatar, String name, List<MemberUser> listDataTop) {
        if (!TextUtils.isEmpty(id)) {
            Group group = new Group();
            group.setAvatar(avatar == null ? "" : avatar);
            group.setGid(id);
            group.setName(name == null ? "" : name);
            RealmList<MemberUser> users = new RealmList();
            users.addAll(listDataTop);
            group.setUsers(users);
            DaoUtil.update(group);
        }
    }

    /***
     * 创建群头像
     * @param gid
     * @param avatar
     */
    public void groupHeadImgCreate(String gid, String avatar) {
        if (!TextUtils.isEmpty(gid) && avatar != null) {
            GroupImageHead imageHead = new GroupImageHead();
            imageHead.setGid(gid);
            imageHead.setImgHeadUrl(avatar);
            DaoUtil.update(imageHead);
        }
    }

    /***
     * 修改群头像
     * @param gid
     * @param avatar
     */
    public void groupHeadImgUpdate(String gid, String avatar) {
        if (!TextUtils.isEmpty(gid) && avatar != null) {
            GroupImageHead imageHead = new GroupImageHead();
            imageHead.setGid(gid);
            imageHead.setImgHeadUrl(avatar);
            DaoUtil.update(imageHead);
        }
    }

    /***
     * 获取本地群头像
     * @param gid
     *
     */
    public String groupHeadImgGet(String gid) {
        if (StringUtil.isNotNull(gid)) {
            GroupImageHead head = DaoUtil.findOne(GroupImageHead.class, "gid", gid);
            if (head != null) {
                return head.getImgHeadUrl();
            }
        }
        return "";
    }


    /***
     * 根据key查询群
     */
    public List<Group> searchGroup4key(String key) {
        Realm realm = DaoUtil.open();
        List<Group> ret = new ArrayList<>();
        RealmResults<Group> users = realm.where(Group.class)
                .contains("name", key).findAll();
        if (users != null)
            ret = realm.copyFromRealm(users);
        realm.close();
        return ret;
    }

    /***
     * 根据key查询消息
     *
     * 备注：新增不区分大小写模糊查询
     */
    public List<MsgAllBean> searchMsg4key(String key, String gid, Long uid) {


        Realm realm = DaoUtil.open();
        List<MsgAllBean> ret = null;
        try {
            ret = new ArrayList<>();
            RealmResults<MsgAllBean> msg;
            if (StringUtil.isNotNull(gid)) {//群
                msg = realm.where(MsgAllBean.class)
                        .equalTo("gid", gid).and().equalTo("msg_type", 1).and()
                        .contains("chat.msg", key, Case.INSENSITIVE)
                        .sort("timestamp", Sort.DESCENDING)
                        .findAll();
            } else {//单人
                msg = realm.where(MsgAllBean.class).equalTo("gid", "").equalTo("msg_type", 1)
                        .contains("chat.msg", key, Case.INSENSITIVE).beginGroup()
                        .equalTo("from_uid", uid).or().equalTo("to_uid", uid).endGroup()
                        .sort("timestamp", Sort.DESCENDING)
                        .findAll();
            }
            if (msg != null)
                ret = realm.copyFromRealm(msg);
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return ret;
    }

    /***
     * 创建会话数量
     * @param gid
     * @param toUid
     */
    public Session sessionCreate(String gid, Long toUid) {
        Session session;

        if (StringUtil.isNotNull(gid)) {//群消息
            session = DaoUtil.findOne(Session.class, "gid", gid);
            if (session == null) {
                session = new Session();
                session.setSid(UUID.randomUUID().toString());
                session.setGid(gid);
                session.setType(1);
                Group group = DaoUtil.findOne(Group.class, "gid", gid);
                if (group != null) {
                    session.setIsTop(group.getIsTop());
                    session.setIsMute(group.getNotNotify());
                }
            }

        } else {//个人消息
            session = DaoUtil.findOne(Session.class, "from_uid", toUid);
            if (session == null) {
                session = new Session();
                session.setSid(UUID.randomUUID().toString());
                session.setFrom_uid(toUid);
                session.setType(0);
                UserInfo user = DaoUtil.findOne(UserInfo.class, "uid", toUid);
                if (user != null) {
                    session.setIsTop(user.getIstop());
                    session.setIsMute(user.getDisturb());
                }
            }
        }

        session.setUnread_count(0);
        session.setUp_time(System.currentTimeMillis());
        DaoUtil.update(session);
        return session;
    }

    /***
     * 删除单个或者群会话
     * @param from_uid
     * @param gid
     */
    public void sessionDel(Long from_uid, String gid) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        if (StringUtil.isNotNull(gid)) {//群消息
            realm.where(Session.class).equalTo("gid", gid).findAll().deleteAllFromRealm();

        } else {
            realm.where(Session.class).equalTo("from_uid", from_uid).findAll().deleteAllFromRealm();


        }

        realm.commitTransaction();
        realm.close();
    }

    /*
     * 更新或者创建session
     *
     * */
    public void sessionReadUpdate(String gid, Long from_uid, boolean canChangeUnread, MsgAllBean bean, String firstFlag) {
        //是否是 撤回
        String cancelId = null;
        if (bean != null) {
            boolean isCancel = bean.getMsg_type() == ChatEnum.EMessageType.MSG_CANCEL;
            if (isCancel && bean.getMsgCancel() != null) {
                cancelId = bean.getMsgCancel().getMsgidCancel();
            }
        }


        //isCancel 是否是撤回消息  ，  canChangeUnread 不在聊天页面 注意true表示不在聊天页面
        Session session;
        if (StringUtil.isNotNull(gid)) {//群消息
            session = DaoUtil.findOne(Session.class, "gid", gid);
            if (session == null) {
                session = new Session();
                session.setSid(UUID.randomUUID().toString());
                session.setGid(gid);
                session.setType(1);
                Group group = DaoUtil.findOne(Group.class, "gid", gid);
                if (group != null) {
                    session.setIsTop(group.getIsTop());
                    session.setIsMute(group.getNotNotify());
                }
                if (canChangeUnread) {
                    if (session.getIsMute() == 1) {//免打扰
                        session.setUnread_count(0);
                    } else {
                        if (StringUtil.isNotNull(cancelId)) {
                            session.setUnread_count(0);
                        } else {
                            session.setUnread_count(1);
                        }
                    }
                }
            } else {
                if (canChangeUnread) {
                    if (session.getIsMute() != 1) {//非免打扰
                        int num = 0;
                        if (StringUtil.isNotNull(cancelId)) {
                            MsgAllBean cancel = getMsgById(cancelId);
//                            LogUtil.getLog().e("群==isRead===="+cancel.isRead()+"==getRead="+cancel.getRead());
                            if (cancel != null && !cancel.isRead()) {//撤回的是未读消息 红点-1
                                num = session.getUnread_count() - 1;
                            } else {
                                num = session.getUnread_count();
                            }
                        } else {
                            num = session.getUnread_count() + 1;
                        }
                        num = num < 0 ? 0 : num;
                        session.setUnread_count(num);
                    } else {
                        session.setUnread_count(0);
                    }
                }
            }
            session.setUp_time(System.currentTimeMillis());

        } else {//个人消息
            session = DaoUtil.findOne(Session.class, "from_uid", from_uid);
            if (session == null) {
                session = new Session();
                session.setSid(UUID.randomUUID().toString());
                session.setFrom_uid(from_uid);
                session.setType(0);
                UserInfo user = DaoUtil.findOne(UserInfo.class, "uid", from_uid);
                if (user != null) {
                    session.setIsTop(user.getIstop());
                    session.setIsMute(user.getDisturb());
                }
                if (canChangeUnread) {
                    if (session.getIsMute() == 1) {//免打扰
                        session.setUnread_count(0);
                    } else {
                        if (StringUtil.isNotNull(cancelId)) {
                            session.setUnread_count(0);
                        } else {
                            session.setUnread_count(1);
                        }
                    }
                }
            } else {
                if (canChangeUnread) {
                    if (session.getIsMute() != 1) {//非免打扰
                        //没有撤回消息的id，要判断撤回的消息是已读还是未读
                        int num = 0;
                        if (StringUtil.isNotNull(cancelId)) {
                            MsgAllBean cancel = getMsgById(cancelId);
//                            LogUtil.getLog().e("==isRead===="+cancel.isRead()+"==getRead="+cancel.getRead());
                            if (cancel != null && !cancel.isRead()) {//撤回的是未读消息 红点-1
                                num = session.getUnread_count() - 1;
                            } else {
                                num = session.getUnread_count();
                            }
                        } else {
                            num = session.getUnread_count() + 1;
                        }
                        num = num < 0 ? 0 : num;
                        session.setUnread_count(num);
                    } else {
                        session.setUnread_count(0);
                    }
                }
            }
            session.setUp_time(System.currentTimeMillis());
        }

        if (StringUtil.isNotNull(cancelId)) {//如果是撤回at消息,星哥说把类型给成这个,at就会去掉
            session.setMessageType(1000);
        } else if ("first".equals(firstFlag) && bean != null && bean.getAtMessage() != null && bean.getAtMessage().getAt_type() != 1000) {
            //对at消息处理 而且不是撤回消息
//            LogUtil.getLog().e("===bean.getAtMessage().getAt_type()="+bean.getAtMessage().getAt_type()+"===bean.getAtMessage().getMsg()="+bean.getAtMessage().getMsg());
            int messageType = bean.getAtMessage().getAt_type();
            String atMessage = bean.getAtMessage().getMsg();
            session.setMessageType(messageType);
            session.setAtMessage(atMessage);
        }


        DaoUtil.update(session);
    }

    /*
     * 批量更新或者创建session
     *
     * */
    public void sessionReadUpdate(String gid, Long from_uid, int count) {
        Session session;
        if (StringUtil.isNotNull(gid)) {//群消息
            session = DaoUtil.findOne(Session.class, "gid", gid);
            if (session == null) {
                session = new Session();
                session.setSid(UUID.randomUUID().toString());
                session.setGid(gid);
                session.setType(1);
                Group group = DaoUtil.findOne(Group.class, "gid", gid);
                if (group != null) {
                    session.setIsTop(group.getIsTop());
                    session.setIsMute(group.getNotNotify());
                }
                if (session.getIsMute() == 1) {//免打扰
                    session.setUnread_count(0);
                } else {
                    session.setUnread_count(count < 0 ? 0 : count);
                }
            } else {
                if (session.getIsMute() != 1) {//免打扰
                    int num = session.getUnread_count() + count;
                    num = num < 0 ? 0 : num;
                    session.setUnread_count(num);
                } else {
                    session.setUnread_count(0);
                }
            }
            session.setUp_time(System.currentTimeMillis());

        } else {//个人消息
            session = DaoUtil.findOne(Session.class, "from_uid", from_uid);
            if (session == null) {
                session = new Session();
                session.setSid(UUID.randomUUID().toString());
                session.setFrom_uid(from_uid);
                session.setType(0);
                UserInfo user = DaoUtil.findOne(UserInfo.class, "uid", from_uid);
                if (user != null) {
                    session.setIsTop(user.getIstop());
                    session.setIsMute(user.getDisturb());
                }
                if (session.getIsMute() == 1) {//免打扰
                    session.setUnread_count(0);
                } else {
                    session.setUnread_count(count < 0 ? 0 : count);
                }

            } else {
                if (session.getIsMute() != 1) {//非免打扰
                    int num = session.getUnread_count() + count;
                    num = num < 0 ? 0 : num;
                    session.setUnread_count(num);
                } else {
                    session.setUnread_count(0);
                }
            }
            session.setUp_time(System.currentTimeMillis());
        }
//        if (isCancel) {//如果是撤回at消息,星哥说把类型给成这个,at就会去掉
//            session.setMessageType(1000);
//        }

        DaoUtil.update(session);
    }

    /*
     * 跟随群信，或用户信息更新，更新session置顶免打扰字段
     * */
    public void updateSessionTopAndDisturb(String gid, Long from_uid, int top, int disturb) {
        Session session;
        if (StringUtil.isNotNull(gid)) {//群消息
            session = DaoUtil.findOne(Session.class, "gid", gid);
            if (session != null) {
                session.setIsMute(disturb);
                session.setIsTop(top);
                if (disturb == 1) {
                    session.setUnread_count(0);
                }
            }
        } else {//个人消息
            session = DaoUtil.findOne(Session.class, "from_uid", from_uid);
            if (session != null) {
                session.setIsMute(disturb);
                session.setIsTop(top);
                if (disturb == 1) {
                    session.setUnread_count(0);
                }
            }
        }
        if (session != null) {
            DaoUtil.update(session);
        }
    }

    /***
     * 清理单个会话阅读数量
     * @param gid
     * @param from_uid
     */
    public void sessionReadClean(String gid, Long from_uid) {
        Session session = StringUtil.isNotNull(gid) ? DaoUtil.findOne(Session.class, "gid", gid) :
                DaoUtil.findOne(Session.class, "from_uid", from_uid);
        if (session != null) {
            session.setUnread_count(0);
            //  session.setUp_time(System.currentTimeMillis());
            DaoUtil.update(session);
        }

    }

    /***
     * 清理单个会话阅读数量
     * @param session
     */
    public void sessionReadClean(Session session) {
        if (session != null) {
            session.setUnread_count(0);
            DaoUtil.update(session);
        }
    }

    /***
     * 查询会话所有未读消息
     * @return
     */
    public int sessionReadGetAll() {
        int sum = 0;
        Realm realm = DaoUtil.open();
        List<Session> list = realm.where(Session.class).findAll();

        if (list != null) {
            for (Session s : list) {
                sum += s.getUnread_count();
            }
        }

        realm.close();
        return sum;
    }


    /**
     * 是否有草稿
     */
    public boolean isSaveDraft(String gid) {
        boolean isSaveDraft = false;
        Session session = DaoUtil.findOne(Session.class, "gid", gid);
        if (session != null && StringUtil.isNotNull(session.getDraft())) {
            isSaveDraft = true;
        }
        return isSaveDraft;
    }

    /***
     * 获取会话
     * @param gid
     * @param uid
     * @return
     */
    public Session sessionGet(String gid, Long uid) {
        if (StringUtil.isNotNull(gid)) {
            return DaoUtil.findOne(Session.class, "gid", gid);
        } else {
            return DaoUtil.findOne(Session.class, "from_uid", uid);
        }

    }

    /***
     * 存草稿  需要更新时间
     * @param gid
     * @param uid
     * @param draft
     */
    public void sessionDraft(String gid, Long uid, String draft) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Session session = StringUtil.isNotNull(gid) ? realm.where(Session.class).equalTo("gid", gid).findFirst() : realm.where(Session.class).equalTo("from_uid", uid).findFirst();
            if (session != null) {
                session.setDraft(draft);
                session.setMessageType(2);
                session.setUp_time(SocketData.getSysTime());
                realm.insertOrUpdate(session);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    /***
     * 更新@消息
     * @param gid
     * @param uid
     */
    public void updateSessionAtMsg(String gid, Long uid) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Session session = StringUtil.isNotNull(gid) ? realm.where(Session.class).equalTo("gid", gid).findFirst() : realm.where(Session.class).equalTo("from_uid", uid).findFirst();
            if (session != null) {
                session.setAtMessage("");
                session.setMessageType(1000);
                realm.insertOrUpdate(session);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    /**
     * 存at消息
     */
    public void atMessage(String gid, String atMessage, int type) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Session session = realm.where(Session.class).equalTo("gid", gid).findFirst();

            if (session != null) {
                session.setAtMessage(atMessage);
                session.setMessageType(type);
                realm.insertOrUpdate(session);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }


    /***
     * 获取红点的值
     * @param type
     * @return
     */
    public int remidGet(String type) {
        Remind remind = DaoUtil.findOne(Remind.class, "remid_type", type);
        int num = remind == null ? 0 : remind.getNumber();
        return num;
    }

    /***
     * 清理红点
     * @param type
     */
    public void remidClear(String type) {
        Remind remind = DaoUtil.findOne(Remind.class, "remid_type", type);
        if (remind != null) {
            remind.setNumber(0);
            DaoUtil.update(remind);
        }
    }

    /***
     * 红点加一
     * @param type
     */
    public void remidCount(String type) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Remind remind = realm.where(Remind.class).equalTo("remid_type", type).findFirst();
            int readnum = remind == null ? 1 : remind.getNumber() + 1;
            Remind newreamid = new Remind();
            newreamid.setNumber(readnum);
            newreamid.setRemid_type(type);
            realm.insertOrUpdate(newreamid);
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }


    /***
     * 获取单个会话阅读量
     * @param gid
     * @param from_uid
     * @return
     */
    public int sessionReadGet(String gid, Long from_uid) {
        int sum = 0;
        Session session = StringUtil.isNotNull(gid) ? DaoUtil.findOne(Session.class, "gid", gid) :
                DaoUtil.findOne(Session.class, "from_uid", from_uid);
        if (session != null) {
            sum = session.getUnread_count();
        }


        return sum;
    }

    /***
     * 获取所有会话
     * @param isAll 是否剔除小助手，true不剔除，false剔除
     * @return
     */
    public List<Session> sessionGetAll(boolean isAll) {
        List<Session> rts = null;
        Realm realm = DaoUtil.open();
        try {

//            realm.beginTransaction();
            RealmResults<Session> list;
            if (isAll) {
                list = realm.where(Session.class).sort("up_time", Sort.DESCENDING).findAll();
            } else {
                list = realm.where(Session.class).beginGroup().notEqualTo("from_uid", 1L).and().isNotNull("from_uid").endGroup().
                        or().isNotNull("gid").sort("up_time", Sort.DESCENDING).findAll();
            }
            //6.5 优先读取单独表的配置
//            for (Session l : list) {
//                int top = 0;
//                if (l.getType() == 1) {
//                    Group group = realm.where(Group.class).equalTo("gid", l.getGid()).findFirst();
//                    if (group != null) {
//                        top = group.getIsTop();
//                    }
//                } else {
//                    UserInfo info = realm.where(UserInfo.class).equalTo("uid", l.getFrom_uid()).findFirst();
//                    if (info != null) {
//                        top = info.getIstop();
//                    }
//                }
//                l.setIsTop(top);
//            }
//            realm.copyToRealmOrUpdate(list);
            list = list.sort("isTop", Sort.DESCENDING);
            rts = realm.copyFromRealm(list);

//            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return rts;
    }

    /***
     * 获取所有有效会话，去除被踢群聊
     * @return
     */
    public List<Session> sessionGetAllValid() {
        List<Session> rts = null;
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            RealmResults<Session> list = realm.where(Session.class)
                    .beginGroup().notEqualTo("from_uid", 1L).and().isNotNull("from_uid").endGroup().
                            or().isNotNull("gid").sort("up_time", Sort.DESCENDING).findAll();
            //6.5 优先读取单独表的配置
            List<Session> removes = new ArrayList<>();
            if (list != null) {
                int len = list.size();
                for (int i = 0; i < len; i++) {
                    Session l = list.get(i);
                    Session session = null;
                    int top = 0;
                    if (l.getType() == 1) {
                        GroupConfig config = realm.where(GroupConfig.class).equalTo("gid", l.getGid()).findFirst();
                        if (config != null && config.getIsExit() == 1) {
                            session = realm.copyFromRealm(l);
                            removes.add(session);
                        } else {
                            Group group = realm.where(Group.class).equalTo("gid", l.getGid()).findFirst();
                            if (group != null) {
                                top = group.getIsTop();
                                List<MemberUser> users = realm.copyFromRealm(group.getUsers());
                                MemberUser member = MessageManager.getInstance().userToMember(UserAction.getMyInfo(), group.getGid());
                                if (users != null && member != null && !users.contains(member)) {
                                    session = realm.copyFromRealm(l);
                                    removes.add(session);
                                }
                            }
                        }
                    } else {
                        UserInfo info = realm.where(UserInfo.class).equalTo("uid", l.getFrom_uid()).findFirst();
                        if (info != null) {
                            top = info.getIstop();
                        }
                    }
                    l.setIsTop(top);
                }
            }
            realm.copyToRealmOrUpdate(list);
            list = list.sort("isTop", Sort.DESCENDING);
            rts = realm.copyFromRealm(list);
            if (removes.size() > 0) {
                int len = removes.size();
                for (int i = 0; i < len; i++) {
                    rts.remove(removes.get(i));
                }
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }

        return rts;
    }


    /***
     * 获取最后的消息
     * @param uid
     * @return
     */
    public MsgAllBean msgGetLast4FUid(Long uid) {
        MsgAllBean ret = null;
        Realm realm = DaoUtil.open();
        try {
            MsgAllBean bean = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                    .and()
                    .beginGroup().equalTo("from_uid", uid).or().equalTo("to_uid", uid).endGroup()
                    .sort("timestamp", Sort.DESCENDING).findFirst();
            if (bean != null) {
                ret = realm.copyFromRealm(bean);
            }
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return ret;
    }

    /**
     * 获取最后一条收到的消息
     */
    public MsgAllBean msgGetLast4FromUid(Long uid) {
        MsgAllBean ret = null;
        Realm realm = DaoUtil.open();
        try {
            MsgAllBean bean = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                    .and()
                    .beginGroup().equalTo("from_uid", uid).endGroup()
                    .sort("timestamp", Sort.DESCENDING).findFirst();
            if (bean != null) {
                ret = realm.copyFromRealm(bean);
            }
//            LogUtil.getLog().e("==msg=ret=="+ GsonUtils.optObject(ret));
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
        }
        return ret;
    }


    /***
     * 获取群最后的消息
     * @param uid
     * @return
     */
    public MsgAllBean msgGetLastGroup4Uid(String gid, Long uid) {
        MsgAllBean ret = null;
        Realm realm = DaoUtil.open();
        try {
            MsgAllBean bean = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("gid", gid).endGroup()
                    .and()
                    .beginGroup().equalTo("from_uid", uid).or().equalTo("to_uid", uid).endGroup()
                    .sort("timestamp", Sort.DESCENDING).findFirst();
            if (bean != null) {
                ret = realm.copyFromRealm(bean);
            }
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return ret;
    }

    /***
     * 获取最后的群消息
     * @param gid
     * @return
     */
    public MsgAllBean msgGetLast4Gid(String gid) {
        MsgAllBean ret = null;
        Realm realm = DaoUtil.open();
        MsgAllBean bean = realm.where(MsgAllBean.class).equalTo("gid", gid)
                .sort("timestamp", Sort.DESCENDING).findFirst();
        if (bean != null) {
            ret = realm.copyFromRealm(bean);
        }

        realm.close();
        return ret;
    }

    /**
     * 更新已读状态
     */
    public void setUpdateRead(long uid, long timestamp) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        try {
            List<MsgAllBean> list = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                    .and()
                    .beginGroup().equalTo("to_uid", uid).endGroup()
                    .findAll();
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    MsgAllBean msgAllBean = list.get(i);
                    if (msgAllBean.getRead() == 0) {//msgAllBean.getTimestamp() <= timestamp &&
                        msgAllBean.setRead(1);
                        msgAllBean.setReadTime(timestamp);
                    }
                }
                realm.insertOrUpdate(list);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
        }
    }


    /**
     * 设置已读
     */
    public void setRead(String msgid) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        try {
            MsgAllBean msgAllBean = realm.where(MsgAllBean.class)
                    .equalTo("msg_id", msgid).findFirst();
            if (msgAllBean != null) {
                msgAllBean.setRead(1);
                realm.insertOrUpdate(msgAllBean);
            }

            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
        }

    }


    /***
     * 保存群状态
     * @param gid
     * @param notNotify
     * @param saved
     * @param needVerification
     */
    public Session saveSession4Switch(String gid, Integer isTop, Integer notNotify, Integer saved, Integer needVerification) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        Session session = DaoUtil.findOne(Session.class, "gid", gid);
        if (session == null)
            return null;
        if (notNotify != null)
            session.setIsMute(notNotify);

        if (isTop != null)
            session.setIsTop(isTop);
//        Session result = realm.copyFromRealm(session);
        realm.insertOrUpdate(session);

        realm.commitTransaction();
        realm.close();
        return session;
    }

    /*
     * 更新群置顶
     * */
    public Session updateGroupAndSessionTop(String gid, int top) {
        Session session = null;
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group != null) {
                group.setIsTop(top);
                realm.insertOrUpdate(group);
            }

            Session s = realm.where(Session.class).equalTo("gid", gid).findFirst();
            if (s != null) {
                s.setIsTop(top);
                session = realm.copyFromRealm(s);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return session;

    }

    /*
     * 更新群免打扰
     * */
    public void updateGroupAndSessionDisturb(String gid, int disturb) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group != null) {
                group.setNotNotify(disturb);
                realm.insertOrUpdate(group);
            }

            Session session = realm.where(Session.class).equalTo("gid", gid).findFirst();
            if (session != null) {
                session.setIsMute(disturb);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    /***
     * 保存单聊置顶，session 和 user 一起更新
     * @param uid
     * @param isTop
     */
    public Session updateUserSessionTop(Long uid, int isTop) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        Session session = DaoUtil.findOne(Session.class, "from_uid", uid);
        if (session != null) {
            session.setIsTop(isTop);
            realm.insertOrUpdate(session);
        }

        UserInfo user = DaoUtil.findOne(UserInfo.class, "uid", uid);
        if (user != null) {
            user.setIstop(isTop);
            realm.insertOrUpdate(user);
        }
        realm.commitTransaction();
        realm.close();
        return session;
    }


    /***
     * 保存单聊免打扰，session 和user 一起更新
     * @param uid
     * @param disturb
     */
    public Session updateUserSessionDisturb(Long uid, int disturb) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        Session session = DaoUtil.findOne(Session.class, "from_uid", uid);
        if (session != null) {
            session.setIsMute(disturb);
            realm.insertOrUpdate(session);
        }

        UserInfo user = DaoUtil.findOne(UserInfo.class, "uid", uid);
        if (user != null) {
            user.setDisturb(disturb);
            realm.insertOrUpdate(user);
        }
        realm.commitTransaction();
        realm.close();
        return session;
    }


    //申请加好友
    public void applyFriend(ApplyBean bean) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        bean.setTime(System.currentTimeMillis());
        realm.insertOrUpdate(bean);
        realm.commitTransaction();
        realm.close();
    }

    //申请加群
    public void applyGroup(ApplyBean bean) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        bean.setTime(System.currentTimeMillis());
        realm.insertOrUpdate(bean);
        realm.commitTransaction();
        realm.close();
    }


    //查询申请列表
    public List<ApplyBean> getApplyBeanList() {
        Realm realm = DaoUtil.open();
//        realm.beginTransaction();
        List<ApplyBean> beans = new ArrayList<>();
//        RealmResults<ApplyBean> res = realm.where(ApplyBean.class).notEqualTo("stat", 3).sort("time", Sort.DESCENDING).findAll();
        RealmResults<ApplyBean> res = realm.where(ApplyBean.class).sort("stat", Sort.ASCENDING, "time", Sort.DESCENDING).findAll();
        if (res != null) {
            beans = realm.copyFromRealm(res);
        }
//        realm.commitTransaction();
        realm.close();
        return beans;
    }

    //根据aid查询申请人
    public ApplyBean getApplyBean(String aid) {
        Realm realm = DaoUtil.open();
        ApplyBean bean = new ApplyBean();
        ApplyBean applyBean = realm.where(ApplyBean.class).equalTo("aid", aid).findFirst();
        if (applyBean != null) {
            bean = realm.copyFromRealm(applyBean);
        }
        realm.close();
        return bean;
    }

    // 移除这条群申请
    public void applyRemove(String aid) {
        DaoUtil.deleteOne(ApplyBean.class, "aid", aid);

    }


//
//    /**
//     * 通讯录好友申请添加好友
//     */
//    public void userAcceptAdd(Long uid, String contactName) {
//        Realm realm = DaoUtil.open();
//        realm.beginTransaction();
//
//        ContactNameBean contactNameBean = realm.where(ContactNameBean.class).equalTo("uid", uid).findFirst();
//        if (contactNameBean == null) {
//            contactNameBean = new ContactNameBean();
//            contactNameBean.setUid(uid);
//            contactNameBean.setContactName(contactName);
//        } else {
//            contactNameBean.setContactName(contactName);
//        }
//
//        realm.insertOrUpdate(contactNameBean);
//        realm.commitTransaction();
//        realm.close();
//    }
//
//    /**
//     * 获取contactName
//     */
//    public ContactNameBean getContactName(Long uid) {
//        Realm realm = DaoUtil.open();
//        ContactNameBean contactNameBean = realm.where(ContactNameBean.class).equalTo("uid", uid).findFirst();
//
//        return contactNameBean;
//    }
//
//
//    /***
//     * 群申请
//     * @param gid
//     * @param fromUid
//     * @param nickname
//     */
//    public void groupAcceptAdd(int joinType, long inviter, String inviterName, String gid, long fromUid, String nickname, String head) {
//        Realm realm = DaoUtil.open();
//        realm.beginTransaction();
//
//
//        GroupAccept accept = realm.where(GroupAccept.class).equalTo("gid", gid).equalTo("uid", fromUid).findFirst();
//        if (accept == null) {
//            accept = new GroupAccept();
//            accept.setAid(UUID.randomUUID().toString());
//            accept.setGid(gid);
//        }
//        accept.setTime(System.currentTimeMillis());
//
//        Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
//        if (group != null) {
//            accept.setGroupName(group.getName());
//        }
//
//        accept.setUid(fromUid);
//        accept.setUname(nickname);
//        accept.setHead(head);
//        accept.setInviter(inviter);
//        accept.setInviterName(inviterName);
//        accept.setJoinType(joinType);
//
//        realm.insertOrUpdate(accept);
//
//        realm.commitTransaction();
//        realm.close();
//    }
//
//    public List<GroupAccept> groupAccept() {
//        Realm realm = DaoUtil.open();
//        realm.beginTransaction();
//        List<GroupAccept> beans = new ArrayList<>();
//        RealmResults<GroupAccept> res = realm.where(GroupAccept.class).sort("time", Sort.DESCENDING).findAll();
//        if (res != null) {
//            beans = realm.copyFromRealm(res);
//        }
//        realm.commitTransaction();
//        realm.close();
//        return beans;
//    }
//
//
//    /**
//     * 移除这条群申请
//     *
//     * @param aid
//     */
//    public void groupAcceptRemove(String aid) {
//        DaoUtil.deleteOne(GroupAccept.class, "aid", aid);
//
//    }


    /***
     * 修改群名
     * @param gid
     * @param name
     */
    public void groupNameUpadte(final String gid, final String name) {
        DaoUtil.start(new DaoUtil.EventTransaction() {
            @Override
            public void run(Realm realm) {
                Group ginfo = realm.where(Group.class).equalTo("gid", gid).findFirst();
                if (ginfo != null) {
                    ginfo.setName(name);
                    realm.insertOrUpdate(ginfo);
                }

            }
        });
    }


    /***
     * 群解散,退出的配置
     * @param gid
     * @param isExit
     */
    public void groupExit(final String gid, final String gname, final String gicon, final int isExit) {
        DaoUtil.start(new DaoUtil.EventTransaction() {
            @Override
            public void run(Realm realm) {
                GroupConfig groupConfig = realm.where(GroupConfig.class).equalTo("gid", gid).findFirst();
                if (groupConfig == null) {
                    groupConfig = new GroupConfig();
                    groupConfig.setGid(gid);
                }
                groupConfig.setIsExit(isExit);
                realm.insertOrUpdate(groupConfig);

                Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
                if (group == null) {
                    group = new Group();
                    group.setGid(gid);
                }
                if (gname != null)
                    group.setName(gname);
                if (gicon != null)
                    group.setAvatar(gicon);
                realm.insertOrUpdate(group);


            }
        });
    }

    /***
     * 获取群配置
     * @param gid
     * @return
     */
    public GroupConfig groupConfigGet(String gid) {
        return DaoUtil.findOne(GroupConfig.class, "gid", gid);
    }

    /**
     * 红包开
     *
     * @param rid
     * @param
     */
    public void redEnvelopeOpen(String rid, int envelopeStatus, int reType, String token) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            RedEnvelopeMessage envelopeMessage = null;
            if (reType == MsgBean.RedEnvelopeType.MFPAY_VALUE) {
                envelopeMessage = realm.where(RedEnvelopeMessage.class).equalTo("id", rid).findFirst();
            } else if (reType == MsgBean.RedEnvelopeType.SYSTEM_VALUE) {
                long traceId = Long.parseLong(rid);
                envelopeMessage = realm.where(RedEnvelopeMessage.class).equalTo("traceId", traceId).findFirst();
                if (envelopeMessage
                        != null) {
                    if (!TextUtils.isEmpty(token)) {
                        envelopeMessage.setAccessToken(token);
                    }
                    envelopeMessage.setEnvelopStatus(envelopeStatus);
                }
            }
            if (envelopeMessage != null) {
                if (envelopeMessage.getIsInvalid() == 0) {//没拆才更新，已经拆过了不更新
                    envelopeMessage.setIsInvalid(envelopeStatus != PayEnum.EEnvelopeStatus.NORMAL ? 1 : 0);
                }
                realm.insertOrUpdate(envelopeMessage);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }


    //7.8 要写语音已读的处理
    public void msgRead(String msgid, boolean isRead) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            MsgAllBean msgBean = realm.where(MsgAllBean.class).equalTo("msg_id", msgid).findFirst();
            if (msgBean != null) {
                msgBean.setRead(isRead);
                realm.insertOrUpdate(msgBean);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.reportException(e);
            DaoUtil.close(realm);
        }

    }

    /***
     * 个人配置修改,为空不修改
     */
    public void userSetingUpdate(Boolean shake, Boolean voice) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        UserSeting userSeting = realm.where(UserSeting.class).equalTo("uid", UserAction.getMyId()).findFirst();
        if (userSeting == null) {
            userSeting = new UserSeting();
            userSeting.setUid(UserAction.getMyId());

        }
        if (shake != null) {
            userSeting.setShake(shake);
        }

        if (voice != null) {
            userSeting.setVoice(voice);
        }

        realm.insertOrUpdate(userSeting);

        realm.commitTransaction();
        realm.close();

    }


    /**
     * 修改语音播放模式 0.扬声器  1.听筒
     */
    public void userSetingVoicePlayer(int voicePlayer) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        UserSeting userSeting = realm.where(UserSeting.class).equalTo("uid", UserAction.getMyId()).findFirst();
        if (userSeting == null) {
            userSeting = new UserSeting();
            userSeting.setUid(UserAction.getMyId());

        }
        userSeting.setVoicePlayer(voicePlayer);
        realm.insertOrUpdate(userSeting);
        realm.commitTransaction();
        realm.close();

    }


    /**
     * 修改聊天背景图片
     */
    public void userSetingImage(int image) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        UserSeting userSeting = realm.where(UserSeting.class).equalTo("uid", UserAction.getMyId()).findFirst();
        if (userSeting == null) {
            userSeting = new UserSeting();
            userSeting.setUid(UserAction.getMyId());

        }
        userSeting.setImageBackground(image);
        realm.insertOrUpdate(userSeting);
        realm.commitTransaction();
        realm.close();

    }


    /**
     * 获取用户配置
     */
    public UserSeting userSetingGet() {
        UserSeting userSeting = DaoUtil.findOne(UserSeting.class, "uid", UserAction.getMyId());
        if (userSeting == null) {//数据库中无用户配置信息，则为默认
            userSeting = new UserSeting();
            userSeting.setUid(UserAction.getMyId());
        }
        return userSeting;
    }

    /***
     * 获取原始图的已读状态
     * @param originUrl
     * @return
     */
    public boolean ImgReadStatGet(String originUrl) {
        if (!StringUtil.isNotNull(originUrl)) {
            return false;
        }
        if (originUrl.startsWith("file:")) {
            return true;
        }

        ImageMessage img = DaoUtil.findOne(ImageMessage.class, "origin", originUrl);
        if (img != null) {
            return img.isReadOrigin();
        }
        return false;
    }

    /***
     * 图片已读写入
     * @param originUrl
     * @param isread
     */
    public void ImgReadStatSet(String originUrl, boolean isread) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        ImageMessage img = realm.where(ImageMessage.class).equalTo("origin", originUrl).findFirst();
        if (img != null) {
            img.setReadOrigin(isread);
            realm.insertOrUpdate(img);
        }
        realm.commitTransaction();
        realm.close();

    }

    //修改消息状态
    public MsgAllBean fixStataMsg(String msgid, int sendState) {
        MsgAllBean ret = null;
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        MsgAllBean msgAllBean = realm.where(MsgAllBean.class).equalTo("msg_id", msgid).findFirst();
        if (msgAllBean != null) {
            msgAllBean.setSend_state(sendState);
            realm.insertOrUpdate(msgAllBean);
            ret = realm.copyFromRealm(msgAllBean);
        }
        realm.commitTransaction();
        realm.close();

        return ret;

    }


    //修改消息状态
    public VideoMessage fixVideoLocalUrl(String msgid, String localUrl) {
        VideoMessage ret = null;
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        VideoMessage msgAllBean = realm.where(VideoMessage.class).equalTo("msgId", msgid).findFirst();
        if (msgAllBean != null) {
            msgAllBean.setLocalUrl(localUrl);
            realm.insertOrUpdate(msgAllBean);
            ret = realm.copyFromRealm(msgAllBean);
        }
        realm.commitTransaction();
        realm.close();

        return ret;

    }

    /***
     * 获取用户需要展示的群名字
     * @param gid
     * @param uid
     * @return
     */
    public String getUsername4Show(String gid, Long uid) {
        return getUsername4Show(gid, uid, null, null);
    }

    /***
     * 获取用户需要展示的群名字
     * @param gid
     * @param uid
     * @param uname 用户最新的昵称
     * @param groupName 群最新的昵称
     * @return
     */
    public String getUsername4Show(String gid, Long uid, String uname, String groupName) {
        String name = "";
        Realm realm = DaoUtil.open();
//        realm.beginTransaction();

        UserInfo userInfo = realm.where(UserInfo.class).equalTo("uid", uid).findFirst();
        if (userInfo != null) {
            //1.获取本地用户昵称
            name = userInfo.getName();
            //1.5如果有带过来的昵称先显示昵称
            name = StringUtil.isNotNull(uname) ? uname : name;

            //1.8  如果有带过来的群昵称先显示群昵称
            if (StringUtil.isNotNull(groupName)) {
                name = groupName;
            } else {
                MemberUser memberUser = realm.where(MemberUser.class)
                        .beginGroup().equalTo("uid", uid).endGroup()
                        .beginGroup().equalTo("gid", gid).endGroup()
                        .findFirst();
                if (memberUser != null) {
                    name = StringUtil.isNotNull(memberUser.getMembername()) ? memberUser.getMembername() : name;
                }
            }
            //3.获取用户备注名
            name = StringUtil.isNotNull(userInfo.getMkName()) ? userInfo.getMkName() : name;
        } else {
            MemberUser memberUser = realm.where(MemberUser.class)
                    .beginGroup().equalTo("uid", uid).endGroup()
                    .beginGroup().equalTo("gid", gid).endGroup()
                    .findFirst();
            if (memberUser != null) {
                name = StringUtil.isNotNull(memberUser.getMembername()) ? memberUser.getMembername() : memberUser.getName();
            }

        }


//        realm.commitTransaction();
        realm.close();

        return name;
    }


    /***
     *
     * @param msgid
     * @param note
     * @return
     */
    public MsgAllBean noteMsgAddRb(String msgid, Long toUid, String gid, MsgNotice note) {
        MsgAllBean ret = null;
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        MsgAllBean msgAllBean = realm.where(MsgAllBean.class).equalTo("msg_id", msgid).findFirst();
        if (msgAllBean == null) {
            msgAllBean = new MsgAllBean();
            msgAllBean.setMsg_id(msgid);
            gid = gid == null ? "" : gid;
            msgAllBean.setGid(gid);
            UserInfo userinfo = UserAction.getMyInfo();
            msgAllBean.setFrom_uid(toUid);

            msgAllBean.setTo_uid(userinfo.getUid());


        }

        int survivaltime = new UserDao().getReadDestroy(toUid, gid);

        msgAllBean.setSurvival_time(survivaltime);
        msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
        msgAllBean.setMsgNotice(note);
        msgAllBean.setTimestamp(new Date().getTime());

        realm.insertOrUpdate(msgAllBean);

        realm.commitTransaction();
        realm.close();

        return ret;
    }


    /**
     * 自己修改退出即焚系统消息
     */
    public MsgAllBean noteMsgAddSurvivaltime(Long toUid, String gid) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        String msgid = SocketData.getUUID();
        MsgAllBean msgAllBean = new MsgAllBean();
        msgAllBean.setMsg_id(msgid);
        msgAllBean.setGid(gid);
        UserInfo userinfo = UserAction.getMyInfo();
        msgAllBean.setFrom_uid(toUid);
        msgAllBean.setTo_uid(userinfo.getUid());
        int survivaltime = new UserDao().getReadDestroy(toUid, gid);
        msgAllBean.setSurvival_time(survivaltime);
        String survivaNotice = "";
        if (survivaltime == -1) {
            survivaNotice = "您设置了退出即焚.";
        } else if (survivaltime == 0) {
            survivaNotice = "您取消了阅后即焚.";
        } else {
            survivaNotice = "您设置了消息" +
                    new ReadDestroyUtil().getDestroyTimeContent(survivaltime) + "后消失.";
        }
        MsgCancel survivaMsgCel = new MsgCancel();
        survivaMsgCel.setMsgid(msgid);
        survivaMsgCel.setNote(survivaNotice);
        msgAllBean.setMsgCancel(survivaMsgCel);
        ChangeSurvivalTimeMessage changeSurvivalTimeMessage = new ChangeSurvivalTimeMessage();
        changeSurvivalTimeMessage.setSurvival_time(survivaltime);
        changeSurvivalTimeMessage.setMsgid(msgid);
        msgAllBean.setChangeSurvivalTimeMessage(changeSurvivalTimeMessage);
        msgAllBean.setMsg_type(ChatEnum.EMessageType.CHANGE_SURVIVAL_TIME);
        msgAllBean.setMsgCancel(survivaMsgCel);
        msgAllBean.setTimestamp(new Date().getTime());
        realm.insertOrUpdate(msgAllBean);

        realm.commitTransaction();
        realm.close();
        EventBus.getDefault().post(new EventRefreshChat());
        return msgAllBean;
    }


    /***
     * 把发送中的状态修改为发送失败
     */
    public void msgSendStateToFail() {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            RealmResults<MsgAllBean> list = realm.where(MsgAllBean.class).equalTo("send_state", ChatEnum.ESendStatus.SENDING).or().equalTo("send_state", ChatEnum.ESendStatus.PRE_SEND).findAll();
            if (list != null) {
                for (MsgAllBean ls : list) {
                    ls.setSend_state(ChatEnum.ESendStatus.ERROR);
                }
                realm.insertOrUpdate(list);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }

    }

    //是否存在该消息,getChat=null 需要删除旧消息
    public boolean isMsgLockExist(String gid, Long uid) {
        MsgAllBean ret = null;
        MsgAllBean bean = null;
        Realm realm = DaoUtil.open();
//        realm.beginTransaction();
        if (!TextUtils.isEmpty(gid)) {
            ret = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("msg_type", ChatEnum.EMessageType.LOCK).endGroup()
                    .and()
                    .beginGroup().equalTo("gid", gid).endGroup()
                    .findFirst();
        } else if (uid != null) {
            ret = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("msg_type", ChatEnum.EMessageType.LOCK).endGroup()
                    .and()
                    .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                    .and()
                    .beginGroup().equalTo("from_uid", uid).or().equalTo("to_uid", uid).endGroup()
                    .findFirst();
        }
        if (ret != null) {
            bean = realm.copyFromRealm(ret);
        }
//        realm.commitTransaction();
        realm.close();
        if (bean != null && bean.getChat() != null) {
            return true;
        }
        if (bean != null) {
            DaoUtil.deleteOne(MsgAllBean.class, "msg_id", bean.getMsg_id());
        }
        return false;
    }

    public void insertOrUpdateMessage(MsgAllBean bean) {
        if (bean == null) {
            return;
        }
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        realm.insertOrUpdate(bean);
        realm.commitTransaction();
        realm.close();
    }


    /***
     * 模糊搜索群聊
     * @return
     */
    public List<Group> getGroupByKey(String key) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        List<Group> ret = new ArrayList<>();
        RealmResults<Group> groups = realm.where(Group.class).findAll();
        RealmResults<Group> keyGroups = groups.where().contains("name", key).findAll();
        if (keyGroups != null) {
            ret = realm.copyFromRealm(keyGroups);
        }
        for (int i = 0; i < groups.size(); i++) {
            Group g = groups.get(i);
            if (ret.contains(g)) {
                continue;
            } else {
                RealmList<MemberUser> userInfos = g.getUsers();
                MemberUser userInfo = userInfos.where()
                        .beginGroup().contains("name", key).endGroup()
                        .or()
                        .beginGroup().contains("membername", key).endGroup()
                        .findFirst();
                if (userInfo != null) {
                    Group group = realm.copyFromRealm(g);
                    MemberUser info = realm.copyFromRealm(userInfo);
                    group.setKeyUser(info);
                    ret.add(group);
                } /*else {
                    GropLinkInfo gropLinkInfo = realm.where(GropLinkInfo.class)
                            .beginGroup().equalTo("gid", g.getGid()).endGroup()
                            .and()
                            .beginGroup().contains("membername", key).endGroup()
                            .findFirst();
                    if (gropLinkInfo != null) {
                        userInfo = userInfos.where()
                                .beginGroup().equalTo("uid", gropLinkInfo.getUid()).endGroup()
                                .findFirst();
                        if (userInfo != null) {
                            Group group = realm.copyFromRealm(g);
                            UserInfo info = realm.copyFromRealm(userInfo);
                            info.setMembername(gropLinkInfo.getMembername());
                            group.setKeyUser(info);
                            ret.add(group);
                        }
                    }
                }*/
            }
        }
        realm.commitTransaction();
        realm.close();
        return ret;


    }


    /*
     * 获取下一个待播语音
     * */
    public MsgAllBean getNextVoiceMessage(Long uid, String gid, Long time, Long mid) {
        MsgAllBean ret = null;
        MsgAllBean bean = null;
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        if (mid == null) {
            return null;
        }
        // from_uid = 100804 and timestamp >1567493175111 and msg_type = 7 and isRead = false and to_uid = 101303
        if (uid != null) {//私聊
            ret = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("msg_type", ChatEnum.EMessageType.VOICE).endGroup()
                    .and()
                    .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                    .and()
                    .beginGroup().equalTo("to_uid", mid).endGroup()
                    .and()
                    .beginGroup().equalTo("from_uid", uid).endGroup()
                    .and()
                    .beginGroup().equalTo("isRead", false).endGroup()
                    .and()
                    .beginGroup().greaterThan("timestamp", time).endGroup()
                    .sort("timestamp", Sort.DESCENDING)
                    .findFirst();
        } else if (!TextUtils.isEmpty(gid)) {//群聊
            ret = realm.where(MsgAllBean.class)
                    .beginGroup().equalTo("msg_type", ChatEnum.EMessageType.VOICE).endGroup()
                    .and()
                    .beginGroup().equalTo("gid", gid).endGroup()
                    .and()
                    .beginGroup().notEqualTo("from_uid", mid).endGroup()
                    .and()
                    .beginGroup().equalTo("isRead", false).endGroup()
                    .and()
                    .beginGroup().greaterThan("timestamp", time).endGroup()
                    .sort("timestamp", Sort.DESCENDING)
                    .findFirst();
        }
        if (ret != null) {
            bean = realm.copyFromRealm(ret);
        }
        realm.commitTransaction();
        realm.close();
        return bean;
    }

    //修改播放消息状态
    public void updatePlayStatus(String msgId, @ChatEnum.EPlayStatus int playStatus) {
        MsgAllBean ret = null;
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        VoiceMessage message = realm.where(VoiceMessage.class).equalTo("msgid", msgId).findFirst();
        if (message != null) {
            message.setPlayStatus(playStatus);
            realm.insertOrUpdate(message);
        }
        realm.commitTransaction();
        realm.close();
    }

    /***
     * 群成员保护的开关
     * @param intimately
     */
    public void groupContactIntimatelyUpdate(String gid, boolean intimately) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
        if (group != null) {
            group.setContactIntimately(intimately ? 1 : 0);
            realm.insertOrUpdate(group);
        }

        realm.commitTransaction();
        realm.close();
    }


    public MsgAllBean getMsgById(String msgId) {
        MsgAllBean ret = null;
        MsgAllBean bean = null;
        Realm realm = DaoUtil.open();
        realm.beginTransaction();
        ret = realm.where(MsgAllBean.class).equalTo("msg_id", msgId).findFirst();
        if (ret != null) {
            bean = realm.copyFromRealm(ret);
        }
        realm.commitTransaction();
        realm.close();
        return bean;
    }

    /*
     * 动态获取群名
     * */
    public String getGroupName(String gid) {
        Group group = getGroup4Id(gid);
        if (group == null) {
            return "";
        }
        String result = group.getName();
        if (TextUtils.isEmpty(result)) {
            List<MemberUser> users = group.getUsers();
            if (users != null && users.size() > 0) {
                int len = users.size();
                for (int i = 0; i < len; i++) {
                    MemberUser info = users.get(i);
                    if (i == len - 1) {
                        result += StringUtil.getUserName("", info.getMembername(), info.getName(), info.getUid());
                    } else {
                        result += StringUtil.getUserName(/*info.getMkName()*/"", info.getMembername(), info.getName(), info.getUid()) + "、";
                    }
                }
                result = result.length() > 14 ? StringUtil.splitEmojiString(result, 0, 14) : result;
                result += "的群";
            }
        }
        return result;
    }

    /*
     * 动态获取群名
     * */
    public String getGroupName(Group group) {
        if (group == null) {
            return "";
        }
        String result = group.getName();
//        String result = "";
        if (TextUtils.isEmpty(result)) {
            List<MemberUser> users = group.getUsers();
            if (users != null && users.size() > 0) {
                int len = users.size();
                for (int i = 0; i < len; i++) {
                    MemberUser info = users.get(i);
//                    GropLinkInfo linkInfo = getGropLinkInfo(group.getGid(), info.getUid());
//                    String memberName = "";
//                    if (linkInfo != null && !TextUtils.isEmpty(linkInfo.getMembername())) {
//                        memberName = linkInfo.getMembername();
//                    }
                    if (i == len - 1) {
                        result += StringUtil.getUserName("", info.getMembername(), info.getName(), info.getUid());
                    } else {
                        result += StringUtil.getUserName("", info.getMembername(), info.getName(), info.getUid()) + "、";
                    }

                }
                result = result.length() > 14 ? StringUtil.splitEmojiString(result, 0, 14) : result;
                result += "的群";
            }
        }
        return result;
    }



    /***
     * 获取除当前会话的未读消息数量
     * @param gid
     * @param uid
     */
    public int getUnreadCount(String gid, Long uid) {
        Realm realm = DaoUtil.open();
        int sum = 0;
        try {
            realm.beginTransaction();
            RealmResults<Session> list;
            if (!TextUtils.isEmpty(gid)) {
                list = realm.where(Session.class)
                        .notEqualTo("gid", gid)
                        .findAll();
            } else {
                list = realm.where(Session.class)
                        .notEqualTo("from_uid", uid)
                        .findAll();

            }
            List<Session> sessions = realm.copyFromRealm(list);
            int len = sessions.size();
            for (int i = 0; i < len; i++) {
                sum += sessions.get(i).getUnread_count();
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return sum;
    }

    //判断群是否已存在
    public boolean isGroupExist(String groupId) {
        boolean exist = false;
        if (!TextUtils.isEmpty(groupId)) {
            Realm realm = DaoUtil.open();
            try {
                Group g = realm.where(Group.class).equalTo("gid", groupId).findFirst();
                if (g != null) {
                    exist = true;
                }
                realm.close();
            } catch (Exception e) {
                e.printStackTrace();
                DaoUtil.close(realm);
                DaoUtil.reportException(e);
            }
        }
        return exist;

    }

    /*
     * 更新消息已读状态
     * @param isRead true 更新为已读，false 更新为未读
     * 排除语音，等消息
     *
     * */
    public boolean updateMsgRead(Long uid, String gid, boolean isRead) {
        Realm realm = DaoUtil.open();
        boolean hasChange = false;
        try {
            realm.beginTransaction();
            RealmResults<MsgAllBean> realmResults = null;
            if (isRead) {//将未读改为已读
                if (!TextUtils.isEmpty(gid)) {
                    realmResults = realm.where(MsgAllBean.class)
                            .beginGroup().equalTo("gid", gid).endGroup()
                            .and()
                            .beginGroup().equalTo("isRead", false).endGroup()
                            .findAll();
                } else {
                    realmResults = realm.where(MsgAllBean.class)
                            .beginGroup().equalTo("gid", "").or().isNull("gid").endGroup()
                            .and()
                            .beginGroup().equalTo("from_uid", uid).or().equalTo("to_uid", uid).endGroup()
                            .and()
                            .beginGroup().equalTo("isRead", false).endGroup()
                            .findAll();
                }
            } else {

            }
            List<MsgAllBean> list = null;
            if (realmResults != null) {
                list = realm.copyFromRealm(realmResults);
            }
            if (list != null) {
                int len = list.size();
                if (len > 0) {
                    hasChange = true;
                    for (int i = 0; i < len; i++) {
                        MsgAllBean bean = list.get(i);
//                        if (bean.getMsg_type() == ChatEnum.EMessageType.VOICE) {//  || bean.getMsg_type() == ChatEnum.EMessageType.MSG_VIDEO
//                            continue;
//                        }
                        bean.setRead(isRead);
                        realm.insertOrUpdate(bean);
                    }
                }
            }

            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return hasChange;
    }

    /*
     * 删除所有session
     * */
    public void clearSessions() {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            RealmResults<Session> list;
            list = realm.where(Session.class).sort("up_time", Sort.DESCENDING).findAll();
            list.deleteAllFromRealm();
            realm.beginTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    /***
     * 修改群名
     * @param gid 群id
     * @param name 群名
     */
    public boolean updateGroupName(String gid, String name) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        Group g = realm.where(Group.class).equalTo("gid", gid).findFirst();
        if (g != null) {//已经存在
            g.setName(name);
            realm.insertOrUpdate(g);
        } else {//不存在
            return false;
        }
        realm.commitTransaction();
        realm.close();
        return true;
    }

    /***
     * 修改群头像
     * @param gid 群id
     * @param head 群名
     */
    public boolean updateGroupHead(String gid, String head) {
        Realm realm = DaoUtil.open();
        realm.beginTransaction();

        Group g = realm.where(Group.class).equalTo("gid", gid).findFirst();
        if (g != null) {//已经存在
            g.setAvatar(head);
            realm.insertOrUpdate(g);
        } else {//不存在
            return false;
        }
        realm.commitTransaction();
        realm.close();
        return true;
    }

    /***
     * 修改我在本群昵称
     * @param gid 群id
     * @param name 群名
     */
    public boolean updateMyGroupName(String gid, String name) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();

            Group g = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (g != null) {//已经存在
                g.setMygroupName(name);
                List<MemberUser> users = g.getUsers();
                if (users != null) {
                    int len = users.size();
                    for (int i = 0; i < len; i++) {
                        MemberUser memberUser = users.get(i);
                        if (UserAction.getMyId() != null && memberUser.getUid() == UserAction.getMyId().longValue()) {
                            memberUser.setMembername(name);
                        } else {
                            continue;
                        }
                    }
                }
                realm.insertOrUpdate(g);
            } else {//不存在
                return false;
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.reportException(e);
            DaoUtil.close(realm);
        }

        return true;
    }

    public boolean insertMessages(List<MsgAllBean> list) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            realm.insert(list);
            realm.commitTransaction();
            realm.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return false;
    }

    /***
     * 更新非保存群
     * @param groupList 群列表
     */
    public void updateNoSaveGroup(List<Group> groupList) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            if (groupList == null || groupList.size() <= 0) {
                List<Group> groups = realm.where(Group.class).equalTo("saved", 1).findAll();
                int len = groups.size();
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        Group group = groups.get(i);
                        group.setSaved(0);
                    }
                }
                realm.insertOrUpdate(groups);
            } else {
                List<Group> groups = realm.where(Group.class).equalTo("saved", 1).findAll();
                int len = groups.size();
                if (len > 0) {
                    List<Group> temp = new ArrayList<>();
                    for (Iterator<Group> it = groups.iterator(); it.hasNext(); ) {
                        Group group = it.next();
                        if (!groupList.contains(group)) {
                            group.setSaved(0);
                            temp.add(group);
                        }
                    }
                    realm.insertOrUpdate(temp);
                }
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }

    }


    /***
     * 获取保存群
     */
    public List<Group> getMySavedGroup() {
        List<Group> results = null;
        Realm realm = DaoUtil.open();
        try {
            List<Group> groups = realm.where(Group.class).equalTo("saved", 1).findAll();
            int len = groups.size();
            if (len > 0) {
                results = realm.copyFromRealm(groups);
            }
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return results;
    }

    /**
     * 保存群聊
     */
    public void setSavedGroup(String gid, int saved) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group != null) {
                group.setSaved(saved);
                realm.insertOrUpdate(group);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }


    /***
     * 保存批量消息
     */
    public boolean insertOrUpdateMsgList(List<MsgAllBean> list) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            realm.insertOrUpdate(list);
            realm.commitTransaction();
            realm.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return false;
    }

    //移出群成员
    public void removeGroupMember(String gid, List<Long> uids) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group != null) {
                RealmList<MemberUser> list = group.getUsers();
                if (list != null) {
                    List<MemberUser> removeMembers = new ArrayList<>();
                    for (MemberUser user : list) {
                        if (uids.contains(user.getUid())) {
                            removeMembers.add(user);
                        }
                        if (removeMembers.size() == uids.size()) {
                            break;
                        }
                    }
                    if (removeMembers.size() > 0) {
                        list.removeAll(removeMembers);
                    }
                }
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    //适用：自己被移出群成员
    public void removeGroupMember(String gid, MemberUser user) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group != null) {
                RealmList<MemberUser> list = group.getUsers();
                if (list != null) {
                    list.remove(user);
                }
                if (group.getSaved() != null && group.getSaved().intValue() == 1) {//已保存设置为非保存群
                    group.setSaved(0);
                }
            }
            // TODO　被移出群时要先清除草稿
            Session session = realm.where(Session.class).equalTo("gid", gid).findFirst();
            if (session != null) {
                session.setDraft("");
                session.setMessageType(2);
                session.setUp_time(SocketData.getSysTime());
                realm.insertOrUpdate(session);
            }

            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    //移出群成员
    public void removeGroupMember(String gid, long uid) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group != null) {
                RealmList<MemberUser> list = group.getUsers();
                MemberUser memberUser = list.where().equalTo("uid", uid).findFirst();
                if (memberUser != null) {
                    list.remove(memberUser);
                }
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    //添加群成员
    public void addGroupMember(String gid, List<MemberUser> memberUsers) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group != null) {
                RealmList<MemberUser> list = group.getUsers();
                if (list != null) {
                    list.addAll(memberUsers);
                }
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    //添加群成员
    public void addGroupMember(String gid, MemberUser user) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group != null) {
                RealmList<MemberUser> list = group.getUsers();
                if (list != null) {
                    list.add(user);
                }
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    /**
     * 动态获取用户群昵称
     *
     * @param gid
     * @param uid
     * @param uname
     * @param groupName
     * @return
     */
    public String getGroupMemberName(String gid, long uid, String uname, String groupName) {
        Realm realm = DaoUtil.open();
        String name = "";
        try {
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group == null) {
                return "";
            }
            RealmList<MemberUser> users = group.getUsers();
            if (users != null) {
                MemberUser memberUser = users.where().equalTo("uid", uid).findFirst();
                if (memberUser != null) {
                    name = !TextUtils.isEmpty(memberUser.getMembername()) ? memberUser.getMembername() : memberUser.getName();
                }
            }

            if (TextUtils.isEmpty(name)) {
                UserInfo userInfo = realm.where(UserInfo.class).equalTo("uid", uid).findFirst();
                if (userInfo != null) {
                    //1.获取本地用户昵称
                    name = userInfo.getName();
                    //1.5如果有带过来的昵称先显示昵称
                    name = StringUtil.isNotNull(uname) ? uname : name;

                    //1.8  如果有带过来的群昵称先显示群昵称
                    if (StringUtil.isNotNull(groupName)) {
                        name = groupName;
                    } else {
                        MemberUser memberUser = realm.where(MemberUser.class)
                                .beginGroup().equalTo("uid", uid).endGroup()
                                .beginGroup().equalTo("gid", gid).endGroup()
                                .findFirst();
                        if (memberUser != null) {
                            name = StringUtil.isNotNull(memberUser.getMembername()) ? memberUser.getMembername() : name;
                        }
                    }
                    //3.获取用户备注名
                    name = StringUtil.isNotNull(userInfo.getMkName()) ? userInfo.getMkName() : name;
                } else {
                    MemberUser memberUser = realm.where(MemberUser.class)
                            .beginGroup().equalTo("uid", uid).endGroup()
                            .beginGroup().equalTo("gid", gid).endGroup()
                            .findFirst();
                    if (memberUser != null) {
                        name = StringUtil.isNotNull(memberUser.getMembername()) ? memberUser.getMembername() : memberUser.getName();
                    }
                }
            }
            realm.commitTransaction();
        } catch (Exception e) {
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }

        return name;
    }

    /**
     * 动态获取用户群昵称
     *
     * @param gid
     * @param uid
     * @return
     */
    public String getGroupMemberName2(String gid, long uid) {
        Realm realm = DaoUtil.open();
        String result = "";
        try {
            realm.beginTransaction();
            Group group = realm.where(Group.class).equalTo("gid", gid).findFirst();
            if (group == null) {
                return "";
            }
            RealmList<MemberUser> users = group.getUsers();
            if (users != null) {
                MemberUser memberUser = users.where().equalTo("uid", uid).findFirst();
                if (memberUser != null) {
                    result = memberUser.getMembername();
                }
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return result;
    }

    //更新发送失败红包信息
    public void updateEnvelopeInfo(EnvelopeInfo info) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            realm.insertOrUpdate(info);
            Session session;
            if (!TextUtils.isEmpty(info.getGid())) {
                session = realm.where(Session.class).equalTo("gid", info.getGid()).findFirst();
            } else {
                session = realm.where(Session.class).equalTo("from_uid", info.getUid()).findFirst();
            }
            if (session != null) {
                session.setMessageType(ChatEnum.ESessionType.ENVELOPE_FAIL);
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    public EnvelopeInfo queryEnvelopeInfo(String gid, long uid) {
        EnvelopeInfo envelopeInfo = null;
        Realm realm = DaoUtil.open();
        try {
            EnvelopeInfo info;
            if (!TextUtils.isEmpty(gid)) {
                info = realm.where(EnvelopeInfo.class).equalTo("gid", gid).findFirst();
            } else {
                info = realm.where(EnvelopeInfo.class).equalTo("uid", uid).findFirst();
            }
            if (info != null) {
                envelopeInfo = realm.copyFromRealm(info);
            }
            realm.close();
        } catch (Exception e) {
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return envelopeInfo;
    }

    public List<EnvelopeInfo> queryEnvelopeInfoList() {
        List<EnvelopeInfo> list = null;
        Realm realm = DaoUtil.open();
        try {
            RealmResults<EnvelopeInfo> realmList = realm.where(EnvelopeInfo.class).equalTo("sendStatus", 0).findAll();
            if (realmList != null) {
                list = realm.copyFromRealm(realmList);
            }

            realm.close();
        } catch (Exception e) {
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
        return list;
    }

    //删除发送失败红包信息
    public void deleteEnvelopeInfo(String rid, String gid, long uid, boolean deleInfo) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            EnvelopeInfo info = realm.where(EnvelopeInfo.class).equalTo("rid", rid).findFirst();
            if (info != null) {
                if (deleInfo) {
                    info.deleteFromRealm();
                }
                Session session;
                if (!TextUtils.isEmpty(gid)) {
                    session = realm.where(Session.class).equalTo("gid", gid).findFirst();
                } else {
                    session = realm.where(Session.class).equalTo("from_uid", uid).findFirst();
                }
                if (session != null) {
                    session.setMessageType(ChatEnum.ESessionType.DEFAULT);
                }
            }
            realm.commitTransaction();
            realm.close();
        } catch (Exception e) {
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }

    //更新转账状态
    public void updateTransferStatus(String tradeId, int opType) {
        Realm realm = DaoUtil.open();
        try {
            realm.beginTransaction();
            TransferMessage transfer = realm.where(TransferMessage.class)
                    .beginGroup().equalTo("id", tradeId).endGroup()
                    .and()
                    .beginGroup().equalTo("opType", PayEnum.ETransferOpType.TRANS_SEND).endGroup()
                    .findFirst();
            if (transfer == null) {
                return;
            }
            transfer.setOpType(opType);
            realm.commitTransaction();
        } catch (Exception e) {
            DaoUtil.close(realm);
            DaoUtil.reportException(e);
        }
    }


}
