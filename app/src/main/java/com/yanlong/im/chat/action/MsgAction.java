package com.yanlong.im.chat.action;

import com.google.gson.Gson;
import com.yanlong.im.chat.bean.ExitGroupUser;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.GroupJoinBean;
import com.yanlong.im.chat.bean.GroupUserInfo;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.MsgNotice;
import com.yanlong.im.chat.bean.NoRedEnvelopesBean;
import com.yanlong.im.chat.bean.RobotInfoBean;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.chat.server.MsgServer;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.utils.DaoUtil;
import com.yanlong.im.utils.socket.SocketData;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.NetUtil;
import net.cb.cb.library.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MsgAction {
    private MsgServer server;
    private MsgDao dao;
    private Gson gson = new Gson();

    public MsgAction() {
        server = NetUtil.getNet().create(MsgServer.class);
        dao = new MsgDao();
    }


    public void groupCreate(final String nickname, final String name, final String avatar, final List<UserInfo> listDataTop, final CallBack<ReturnBean<Group>> callback) {
        List<GroupUserInfo> listDataTop2 = new ArrayList<>();
//        List<MemberUser> memberUsers = new ArrayList<>();
        for (int i = 0; i < listDataTop.size(); i++) {
            UserInfo info = listDataTop.get(i);
            GroupUserInfo userInfo = new GroupUserInfo();
            userInfo.setUid(info.getUid() + "");
            userInfo.setNickname(info.getName());
            userInfo.setAvatar(info.getHead());
            listDataTop2.add(userInfo);
//            MemberUser memberUser = MessageManager.getInstance().memberToUser(info);
//            memberUsers.add(memberUser);


        }
        NetUtil.getNet().exec(server.groupCreate(nickname, name, avatar, gson.toJson(listDataTop2)), new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {//存库
                    String id = response.body().getData().getGid();
                    dao.groupCreate(id, avatar, name, MessageManager.getInstance().getMemberList(listDataTop, id));
                    dao.sessionCreate(id, null);
                    MessageManager.getInstance().setMessageChange(true);
                }
                callback.onResponse(call, response);
            }
        });

    }

    public void groupQuit(final String id, String nickname, final CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.groupQuit(id, nickname), new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    dao.sessionDel(null, id);
                    dao.msgDel(null, id);
                    //删除群成员及秀阿贵群保存逻辑
                    MemberUser memberUser = MessageManager.getInstance().userToMember(UserAction.getMyInfo(), id);
                    dao.removeGroupMember(id, memberUser);
                    MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, id, CoreEnum.ESessionRefreshTag.DELETE, null);
                }
                callback.onResponse(call, response);
            }
        });
    }

    public void groupRemove(final String id, List<UserInfo> members, final CallBack<ReturnBean> callback) {
        List<Long> ulist = new ArrayList<>();
        String rname = "";
        for (UserInfo userInfo : members) {
            ulist.add(userInfo.getUid());
            rname += "<font id='" + userInfo.getUid() + "'>" + userInfo.getName() + "</font>";
        }
        final String finalRname = rname;
        NetUtil.getNet().exec(server.groupRemove(id, gson.toJson(ulist)), new Callback<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                callback.onResponse(call, response);
                if (response.body() == null) {
                    return;
                }
                if (response.body().isOk()) {
                    String mid = SocketData.getUUID();
                    MsgNotice note = new MsgNotice();
                    note.setMsgid(mid);
                    note.setMsgType(3);
                    note.setNote("你将\"" + finalRname + "\"移出群聊");
                    dao.noteMsgAddRb(mid, UserAction.getMyId(), id, note);
                }
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void groupDestroy(final String id, final CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.groupDestroy(id), new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    dao.sessionDel(null, id);
                }
                callback.onResponse(call, response);
            }
        });

    }

    public void groupAdd(String id, List<UserInfo> members, String nickname, CallBack<ReturnBean> callback) {
        List<GroupUserInfo> groupUserInfos = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            GroupUserInfo groupUserInfo = new GroupUserInfo();
            groupUserInfo.setUid(members.get(i).getUid() + "");
            groupUserInfo.setAvatar(members.get(i).getHead());
            groupUserInfo.setNickname(members.get(i).getName());
            groupUserInfos.add(groupUserInfo);
        }
        NetUtil.getNet().exec(server.groupAdd(id, gson.toJson(groupUserInfos), nickname), callback);
    }


    /***
     * @param isNew boolean ,true表示是加载最新数据，false表示加载更多历史数据
     * 获取某个用户的数据
     * @return
     */

    public List<MsgAllBean> getMsg4User(String gid, Long uid, Long time, boolean isNew) {
        if (StringUtil.isNotNull(gid)) {
            return dao.getMsg4Group(gid, time, isNew);
        }
        return dao.getMsg4User(uid, time, isNew);
    }


    /*
     * @param gid 群id
     * @param uid 私聊用户id
     * @param time 截止时间
     * @param size 需要数据size
     * */
    public List<MsgAllBean> getMsg4User(String gid, Long uid, Long time, int size) {
        if (StringUtil.isNotNull(gid)) {
            return dao.getMsg4Group(gid, time, size);
        }
        return dao.getMsg4User(uid, time, size);
    }

    /***
     * 获取全部图片
     * @param gid
     * @param uid

     * @return
     */
    public List<MsgAllBean> getMsg4UserImg(String gid, Long uid) {
        if (StringUtil.isNotNull(gid)) {
            return dao.getMsg4GroupImg(gid);
        }
        return dao.getMsg4UserImg(uid);
    }

    public List<MsgAllBean> getMsg4UserHistory(String gid, Long uid, Long stime) {
        if (StringUtil.isNotNull(gid)) {
            return dao.getMsg4GroupHistory(gid, stime);
        }
        return dao.getMsg4UserHistory(uid, stime);
    }

    /***
     * 获取群信息,并缓存
     * @param gid
     * @param callback
     */
    public void groupInfo(final String gid, final Callback<ReturnBean<Group>> callback) {
        if (NetUtil.isNetworkConnected()) {
            NetUtil.getNet().exec(server.groupInfo(gid), new CallBack<ReturnBean<Group>>() {
                @Override
                public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                    if (response.body() == null) {
                        callback.onFailure(call, new Throwable());
                        LogUtil.getLog().d("a=", "MessageManager--加载群信息后的失败 response=null--gid=" + gid);
                        return;
                    }
                    if (response.body().isOk() && response.body().getData() != null) {//保存群友信息到数据库
                        Group newGroup = response.body().getData();
                        newGroup.getMygroupName();
                        Group group = DaoUtil.findOne(Group.class, "gid", gid);
                        if (group != null && group.getUsers() != null) {
                            if (MessageManager.getInstance().isGroupValid(group)) {//在群中，才更新
                                if (MessageManager.getInstance().isGroupValid(newGroup)) {
                                    dao.groupNumberSave(newGroup);
                                    MessageManager.getInstance().updateCacheGroup(group);
                                } else {
                                    dao.removeGroupMember(group.getGid(), UserAction.getMyId());
                                }
                            } else {
                                if (MessageManager.getInstance().isGroupValid(newGroup)) {//重新被拉进群，更新
                                    dao.groupNumberSave(newGroup);
                                    MessageManager.getInstance().updateCacheGroup(group);
                                }
                            }
                            MessageManager.getInstance().updateSessionTopAndDisturb(gid, null, group.getIsTop(), group.getNotNotify());
                        } else {
                            dao.groupNumberSave(newGroup);
                            MessageManager.getInstance().updateCacheGroup(group);
                        }
                        //8.8 取消从数据库里读取群成员信息
                        callback.onResponse(call, response);
                    } else {
                        LogUtil.getLog().d("a=", "MessageManager--加载群信息后的失败--gid=" + gid);
                        MessageManager.getInstance().removeLoadGids(gid);
                        callback.onFailure(call, new Throwable());
                    }
                }

                @Override
                public void onFailure(Call<ReturnBean<Group>> call, Throwable t) {
                    super.onFailure(call, t);
                    LogUtil.getLog().d("a=", "MessageManager--加载群信息后的失败--gid=" + gid + t.getMessage());
//                    t.printStackTrace();
                    MessageManager.getInstance().removeLoadGids(gid);
                    callback.onFailure(call, new Throwable());
                }
            });
        } else {//从缓存中读
            groupInfo4Db(gid, callback);
        }

    }

    /***
     * 获取群成员有变化，更新群成员
     * @param gid
     * @param callback
     */
    public void loadGroupMember(final String gid, final Callback<ReturnBean<Group>> callback) {
        if (NetUtil.isNetworkConnected()) {
            NetUtil.getNet().exec(server.groupInfo(gid), new CallBack<ReturnBean<Group>>() {
                @Override
                public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                    if (response.body() == null) {
                        LogUtil.getLog().d("a=", "MessageManager--加载群信息后的失败 response=null--gid=" + gid);
                        return;
                    }
                    if (response.body().isOk() && response.body().getData() != null) {//保存群友信息到数据库
                        Group newGroup = response.body().getData();
                        newGroup.getMygroupName();
                        dao.groupNumberSave(newGroup);
                        MessageManager.getInstance().updateCacheGroup(newGroup);
                        //8.8 取消从数据库里读取群成员信息
                        MessageManager.getInstance().doImgHeadChange(gid, newGroup);
                        callback.onResponse(call, response);
                    } else {
                        LogUtil.getLog().d("a=", "MessageManager--加载群信息后的失败--gid=" + gid);
                        MessageManager.getInstance().removeLoadGids(gid);
                        callback.onFailure(call, new Throwable());
                    }
                }

                @Override
                public void onFailure(Call<ReturnBean<Group>> call, Throwable t) {
                    super.onFailure(call, t);
                    LogUtil.getLog().d("a=", "MessageManager--加载群信息后的失败--gid=" + gid + t.getMessage());
//                    t.printStackTrace();
//                    MessageManager.getInstance().removeLoadGids(gid);
                    callback.onFailure(call, new Throwable());
                }
            });
        } else {//从缓存中读
            groupInfo4Db(gid, callback);
        }

    }

    /***
     * 从缓存里面读取
     * @param gid
     * @param callback
     */
    public void groupInfo4Db(String gid, Callback<ReturnBean<Group>> callback) {
        Group rdata = dao.groupNumberGet(gid);

        ReturnBean<Group> body = new ReturnBean<>();
        body.setCode(0l);
        body.setData(rdata);
        Response<ReturnBean<Group>> response = Response.success(body);
        //8.8 取消从数据库里读取群成员信息
//        for (MemberUser userInfo : response.body().getData().getUsers()) {
//            GropLinkInfo link = dao.getGropLinkInfo(gid, userInfo.getUid());
//            if (link != null && !TextUtils.isEmpty(link.getMembername())) {
//                userInfo.setMembername(link.getMembername());
//                if (userInfo.getUid().equals(UserAction.getMyId())) {
//                    response.body().getData().setMygroupName(link.getMembername());
//                }
//            }
//        }
        callback.onResponse(null, response);
    }


    /**
     * 获取群信息
     */
    public void groupInfo4UserInfo(final String gid, final Callback<ReturnBean<Group>> callback) {
        NetUtil.getNet().exec(server.groupInfo(gid), callback);
    }

    /***
     * 根据key查询群
     */
    public List<Group> searchGroup4key(String key) {
        return dao.getGroupByKey(key);
//        return dao.searchGroup4key(key);
    }

    /***
     * 根据key查询消息
     */
    public List<MsgAllBean> searchMsg4key(String key, String gid, Long uid) {

        return dao.searchMsg4key(key, gid, uid);
    }

    /***
     * 群详情开关
     * @param gid
     * @param notNotify
     * @param saved
     * @param needVerification
     */
    public void groupSwitch(final String gid, final Integer istop, final Integer notNotify, final Integer saved, final Integer needVerification, final Callback<ReturnBean> cb) {

        //存服务器
    /*    if (istop != null) {
            dao.saveSession4Switch(gid, istop, notNotify, saved, needVerification);
            return;
        }*/

        Callback<ReturnBean> callback = new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {//存库
                    Session session = null;
                    if (istop != null || notNotify != null || needVerification != null) {
                        boolean isTop = false;
                        if (istop != null) {
                            session = dao.updateGroupAndSessionTop(gid, istop.intValue());
                            isTop = true;
                        } else if (notNotify != null) {
                            dao.updateGroupAndSessionDisturb(gid, notNotify.intValue());
                            MessageManager.getInstance().updateCacheTopOrDisturb(gid, 0, notNotify.intValue());
                            isTop = false;
                        }
                        //置顶通知刷新，面打扰在activity onStop时再刷新，避免快速点击开关的时候，造成刷新异常
                        if (isTop) {
                            MessageManager.getInstance().setMessageChange(true);
                            MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, gid, CoreEnum.ESessionRefreshTag.SINGLE, session, isTop);
                        }
                    }
                }
                cb.onResponse(call, response);

            }
        };

        if (needVerification != null) {
            NetUtil.getNet().exec(server.groupSwitch(gid, needVerification), callback);
        } else {
            NetUtil.getNet().exec(server.groupSwitch(gid, istop, notNotify, saved), callback);
        }


    }

    public void groupSwitchIntimately(final String gid, int intimately, Callback<ReturnBean> callback) {

        NetUtil.getNet().exec(server.groupSwitchIntimately(gid, intimately), callback);
    }

    /**
     * 全员禁言
     * @param gid
     * @param intimately
     * @param callback
     */
    public void setAllForbiddenWords(final String gid, int intimately, Callback<ReturnBean> callback) {

        NetUtil.getNet().exec(server.setAllForbiddenWords(gid, intimately), callback);
    }

    /***
     * 单人详情的开关
     * @param isMute
     * @param istop
     */
    public void sessionSwitch(Long uid, Integer isMute, Integer istop, Callback<ReturnBean> callback) {
        if (isMute != null)
            NetUtil.getNet().exec(server.friendMute(uid, isMute), callback);
        if (istop != null)
            NetUtil.getNet().exec(server.friendTop(uid, istop), callback);
    }

    /***
     * 清理所有的消息
     */
    public void msgDelAll() {
        dao.msgDelAll();
    }


    /**
     * 查询已保存的群聊
     */
    List<Group> groupList = new ArrayList<>();
    private int i = 0;
    private int j = 0;

    public void getMySaved(final Callback<ReturnBean<List<Group>>> callback) {

        NetUtil.getNet().exec(server.getMySaved(), new CallBack<ReturnBean<List<Group>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<Group>>> call, Response<ReturnBean<List<Group>>> response) {
                if (response.body() == null)
                    return;
//                callback.onResponse(call, response);
//                List<Group> groupList=new ArrayList<>();
                i = response.body().getData().size();
                for (Group ginfo : response.body().getData()) {
                    //保存群信息到本地
                    final Group group = new Group();
                    group.setGid(ginfo.getGid());
                    group.setAvatar(ginfo.getAvatar());
                    group.setName(ginfo.getName());
                    if (null != dao.getGroup4Id(ginfo.getGid())) {
                        if (null != dao.getGroup4Id(ginfo.getGid()).getUsers() && dao.getGroup4Id(ginfo.getGid()).getUsers().size() > 0) {
                            group.setUsers(dao.getGroup4Id(ginfo.getGid()).getUsers());

                        } else {
                            groupInfo(ginfo.getGid(), new CallBack<ReturnBean<Group>>() {
                                @Override
                                public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                                    if (response.body().isOk()) {
                                        Group bean = response.body().getData();
                                        group.setUsers(bean.getUsers());
                                    }
                                }
                            });
                        }
                        dao.groupSave(group);
                        groupList.add(group);
                    } else {
                        groupInfo(ginfo.getGid(), new CallBack<ReturnBean<Group>>() {
                            @Override
                            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> responseInner) {
                                if (responseInner.body().isOk()) {
                                    Group bean = responseInner.body().getData();
                                    group.setUsers(bean.getUsers());
                                    group.setGid(bean.getGid());
                                    group.setAvatar(bean.getAvatar());
                                    group.setName(bean.getName());
                                    dao.groupSave(group);
                                    groupList.add(group);

                                }
                            }
                        });
                    }

                }
                response.body().setData(groupList);
                callback.onResponse(call, response);
            }
        });
    }

    public void getMySavedGroup(final Callback<ReturnBean<List<Group>>> callback) {

        NetUtil.getNet().exec(server.getMySaved(), new CallBack<ReturnBean<List<Group>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<Group>>> call, Response<ReturnBean<List<Group>>> response) {
                if (response.body() == null || response.body().getData() == null)
                    return;
                if (response.body().isOk()) {
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new Throwable());
                }
            }
        });
    }


    /**
     * 加入群聊
     */
    public void joinGroup(String gid, Long uid, String nickname, String avatar, String inviter, String inviterName, Callback<ReturnBean<GroupJoinBean>> callback) {
        NetUtil.getNet().exec(server.joinGroup(gid, uid, nickname, avatar, inviter, inviterName), callback);
    }


    /**
     * 修改群名称
     */
    public void changeGroupName(String gid, String name, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.changeGroupName(gid, name), callback);
    }

    /**
     * 修改群头像
     */
    public void changeGroupHead(String gid, String avatar, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.groupHeadSet(gid, avatar), callback);
    }

    /**
     * 修改群人数上限
     */
    public void changeGroupLimit(String gid, String description, String masterPhone, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.groupHeadLimit(gid, description, masterPhone), callback);
    }

    /**
     * 修改群成员昵称
     */
    public void changeMemberName(String gid, String name, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.changeMemberName(gid, name), callback);
    }

    /**
     * 同意进群
     */
    public void groupRequest(final String aid, String gid, String newMember, String newMemberName,
                             String newMemberAvatar, int joinType, String inviter, String inviterName,
                             final Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.groupRequest(gid, newMember, newMemberName, newMemberAvatar, joinType, inviter, inviterName), new Callback<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                callback.onResponse(call, response);
//                if (response.body().isOk()) {
//                    dao.groupAcceptRemove(aid);
//                }
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }


//    /**
//     * 删除群申请
//     */
//    public void groupRequestDelect(String aid) {
//        dao.groupAcceptRemove(aid);
//    }


    /**
     * 修改群公告
     */
    public void changeGroupAnnouncement(String gid, String announcement, String masterName, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.changeGroupAnnouncement(gid, announcement, masterName), callback);
    }


    /***
     * 机器人搜索列表
     * @param key
     * @param callback
     */
    public void robotSearch(String key, Callback<ReturnBean<List<RobotInfoBean>>> callback) {
        NetUtil.getNet().exec(server.robotSearch(key), callback);
    }

    /***
     * 修改机器人
     * @param gid
     * @param callback
     */
    public void robotChange(String gid, String rid, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.robotChange(gid, rid), callback);
    }

    /***
     * 删除
     * @param gid
     * @param callback
     */
    public void robotDel(String gid, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.robotChange(gid, "-1"), callback);
    }

    /**
     * 查看详情
     *
     * @param gid
     * @param callback
     */
    public void robotInfo(String robotid, String gid, Callback<ReturnBean<RobotInfoBean>> callback) {
        NetUtil.getNet().exec(server.robotInfo(robotid, gid), callback);
    }


    public void msgRead(String msgId, boolean isRead) {
        dao.msgRead(msgId, isRead);
    }


    /**
     * 转让群
     */
    public void changeMaster(String gid, String uid, String membername, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.changeMaster(gid, uid, membername), callback);
    }

    /**
     * 通过群id批量获取群信息
     */
    public void getGroupsByIds(String gids, Callback<ReturnBean<List<Group>>> callback) {
        NetUtil.getNet().exec(server.getGroupsByIds(gids), callback);
    }

    /**
     * 设置单聊阅后即焚
     */
    public void setSurvivalTime(long friend, int survivalTime, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.setSurvivalTime(friend, survivalTime), callback);
    }

    /**
     * 设置群聊阅后即焚
     */
    public void changeSurvivalTime(String gid, int survivalTime, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.changeSurvivalTime(gid, survivalTime), callback);
    }

    /**
     * 增删管理员
     * @param adminsJson 管理员列表
     * @param gid 群id
     * @param opt -1 取消 0 不做操作 1 新增
     * @param callback
     */
    public void groupChangeAdmins(String adminsJson,String gid,int opt, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.groupChangeAdmins(adminsJson,gid,opt), callback);
    }

    /**
     * 获取退群成员列表
     * @param gid 群id
     * @param callback
     */
    public void exitGroupList(String gid,Callback<ReturnBean<List<ExitGroupUser>>> callback) {
        NetUtil.getNet().exec(server.exitGroupList(gid), callback);
    }

    /**
     * 开关群成员禁领红包
     * @param uidJson 成员列表
     * @param gid 群id
     * @param ops -1 取消 0 不做操作 1 新增
     * @param callback
     */
    public void toggleOpenUpRedEnvelope(String uidJson,String gid,int ops, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.toggleOpenUpRedEnvelope(uidJson,gid,ops), callback);
    }

    /**
     * 获取禁领红包群成员列表
     * @param gid 群id
     * @param callback
     */
    public void getCantOpenUpRedMembers(String gid,Callback<ReturnBean<List<NoRedEnvelopesBean>>> callback) {
        NetUtil.getNet().exec(server.getCantOpenUpRedMembers(gid), callback);
    }

    /**
     * 开关群成员禁言
     * @param uidJson 成员列表
     * @param gid 群id
     * @param duration 禁言时间以秒为单位
     * @param callback
     */
    public void toggleWordsNotAllowed(String uidJson,String gid,int duration, Callback<ReturnBean> callback) {
        NetUtil.getNet().exec(server.toggleWordsNotAllowed(uidJson,gid,duration), callback);
    }
}
