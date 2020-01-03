package com.yanlong.im.chat.task;

import android.os.AsyncTask;

import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;

import java.util.List;

/**
 * @author Liszt
 * @date 2019/10/11
 * Description 初始化session数据
 */
public class TaskSession extends AsyncTask<Void, Integer, Boolean> {
    private MsgDao msgDao = new MsgDao();
    private UserDao userDao = new UserDao();

    @Override
    protected Boolean doInBackground(Void... voids) {
        List<Session> sessions = msgDao.sessionGetAll(true);
        doListDataSort(sessions);
        return true;
    }


    private void doListDataSort(List<Session> listData) {
        if (listData != null) {
            int len = listData.size();
            for (int i = 0; i < len; i++) {
                Session session = listData.get(i);
                prepareSession(session);
            }
        }
    }

    private void prepareSession(Session session) {
        if (session == null) {
            return;
        }
        if (session.getType() == 1) {
            Group group = MessageManager.getInstance().getCacheGroup(session.getGid());
            if (group != null) {
                session.setName(msgDao.getGroupName(group));
                session.setIsMute(group.getNotNotify());
                session.setAvatar(group.getAvatar());
            } else {
                session.setName(msgDao.getGroupName(session.getGid()));
            }
            MsgAllBean msg = msgDao.msgGetLast4Gid(session.getGid());
            if (msg != null) {
                session.setMessage(msg);
                if (msg.getMsg_type() == ChatEnum.EMessageType.NOTICE || msg.getMsg_type() == ChatEnum.EMessageType.MSG_CANCEL) {//通知不要加谁发的消息
                    session.setSenderName("");
                } else {
                    if (msg.getFrom_uid().longValue() != UserAction.getMyId().longValue()) {//自己的不加昵称
                        //8.9 处理群昵称
                        String name = msgDao.getUsername4Show(msg.getGid(), msg.getFrom_uid(), msg.getFrom_nickname(), msg.getFrom_group_nickname()) + " : ";
                        session.setSenderName(name);
                    }
                }
            }
        } else {
            UserInfo info = userDao.findUserInfo(session.getFrom_uid());
            if (info != null) {
                session.setName(info.getName4Show());
                session.setIsMute(info.getDisturb());
                session.setAvatar(info.getHead());
            }
            MsgAllBean msg = msgDao.msgGetLast4FUid(session.getFrom_uid());
            if (msg != null) {
                session.setMessage(msg);
            }
        }
    }
}
