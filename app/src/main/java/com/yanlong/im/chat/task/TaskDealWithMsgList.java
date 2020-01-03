package com.yanlong.im.chat.task;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.tencent.bugly.crashreport.CrashReport;
import com.yanlong.im.MyAppLication;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.chat.ui.MsgMainFragment;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.utils.socket.MsgBean;
import com.yanlong.im.utils.socket.SocketData;
import com.yanlong.im.utils.socket.SocketPact;
import com.yanlong.im.utils.socket.SocketUtil;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.constant.BuglyTag;
import net.cb.cb.library.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Liszt
 * @date 2019/10/11
 * Description 批量处理接收到的消息，针对收到单聊，或者群聊消息，而本地无用户或者群数据，需要异步请求用户或者群数据的情况。
 * 批量消息必须所有消息处理完毕，才能通知刷新session和未读数
 * 风险：当请求用户数据和群数据失败的时候，可能导致任务无法正常处理完
 */
public class TaskDealWithMsgList extends AsyncTask<Void, Integer, Boolean> {
    private final String TAG = TaskDealWithMsgList.class.getSimpleName();
    private MsgDao msgDao = new MsgDao();
    List<MsgBean.UniversalMessage.WrapMessage> messages;
    List<String> gids = new ArrayList<>();//批量消息接受到群聊id
    List<Long> uids = new ArrayList<>();//批量消息接收到单聊uid
    private int taskCount = 0;//任务总数

    // Bugly数据保存异常标签
    private final int BUGLY_TAG_SAVE_DATA = 139066;

    private Map<String, MsgAllBean> pendingMessages = new HashMap<>();//批量接收到的消息，待保存到数据库
    private Map<String, MsgAllBean> pendingCancelMessages = new HashMap<>();//批量接收到的撤销消息，待保存到数据库
    private Map<Long, UserInfo> pendingUsers = new HashMap<>();//批量用户信息（头像和昵称），待保存到数据库
    private Map<String, Integer> pendingGroupUnread = new HashMap<>();//批量群session未读数，待保存到数据库
    private Map<Long, Integer> pendingUserUnread = new HashMap<>();//批量私聊session未读数，待保存到数据库
    private final String requestId;

    public TaskDealWithMsgList(List<MsgBean.UniversalMessage.WrapMessage> wrapMessageList, String requestId) {
        this.requestId = requestId;
        messages = wrapMessageList;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (messages != null) {
            int length = messages.size();
            taskCount = length;
            System.out.println(TaskDealWithMsgList.class.getSimpleName() + "总消息数-taskCount=" + taskCount + "--requestId=" + requestId);
            for (int i = 0; i < length; i++) {
                MsgBean.UniversalMessage.WrapMessage wrapMessage = messages.get(i);
                boolean result = MessageManager.getInstance().dealWithMsg(wrapMessage, true, i == length - 1, requestId);//最后一条消息，发出通知声音
                if (result) {
                    cutTaskCount();
                } else {
                    // 上报后的Crash会显示该标签
                    CrashReport.setUserSceneTag(MyAppLication.getInstance().getApplicationContext(), BUGLY_TAG_SAVE_DATA);
                    // 上传异常数据
                    CrashReport.putUserData(MyAppLication.getInstance().getApplicationContext(), BuglyTag.BUGLY_TAG_1,
                            "requestId:" + requestId + ";MsgType:" +wrapMessage.getMsgType());
                }
            }
        }
        if (taskCount == 0) {
            return true;
        } else {
            return false;
        }
    }

    //减一次任务数，同步方法，避免异步操作造成数据异常
    private synchronized void cutTaskCount() {
        taskCount--;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            saveAndRefresh();
        }
    }

    private void notifyUIRefresh() {
        MessageManager.getInstance().setMessageChange(true);
        if (checkIsFromSingle()) {
            if (gids.size() > 0) {
                MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, null, gids.get(0), CoreEnum.ESessionRefreshTag.SINGLE, null);
            } else {
                MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.PRIVATE, uids.get(0), "", CoreEnum.ESessionRefreshTag.SINGLE, null);
            }

        } else {
            MessageManager.getInstance().notifyRefreshMsg();
        }
        MessageManager.getInstance().notifyRefreshChat();

        clearIds();
    }

    /*
     * 更新任务数
     * 应用场景：异步加载用户数据或者群数据成功后
     * */
    public void updateTaskCount() {
        cutTaskCount();
        System.out.println(TaskDealWithMsgList.class.getSimpleName() + "更新异步任务数-taskCount=" + taskCount + "--requestId=" + requestId);
        if (taskCount == 0) {
            saveAndRefresh();
        }
    }

    public void addUid(Long uid) {
        if (uid != null && !uids.contains(uid)) {
            uids.add(uid);
        }
    }


    public void addGid(String gid) {
        if (!TextUtils.isEmpty(gid) && !gids.contains(gid)) {
            gids.add(gid);
        }
    }

    public boolean checkIsFromSingle() {
        int len1 = uids.size();
        int len2 = gids.size();
        if (len1 + len2 == 1) {
            return true;
        } else {
            return false;
        }
    }

    //批量更新后清除数据
    private void clearIds() {
        uids.clear();
        gids.clear();
    }

    private boolean doPendingData() {
        System.out.println(TaskDealWithMsgList.class.getSimpleName() + "--requestId=" + requestId + "--doPendingData--" /*+ Log.getStackTraceString(new Throwable())*/);
        try {
            Map<Long, Integer> mapUSession = /*MessageManager.getInstance().*/getPendingUserUnreadMap();
            if (mapUSession != null && mapUSession.size() > 0) {
                System.out.println(TaskDealWithMsgList.class.getSimpleName() + "--doPendingData--更新单聊session" + mapUSession.size());
                Iterator iterator = mapUSession.keySet().iterator();
                while (iterator.hasNext()) {
                    Long uid = (Long) iterator.next();
                    if (uid != null) {
                        Integer count = mapUSession.get(uid);
                        if (count != null) {
                            MessageManager.getInstance().updateSessionUnread("", uid, count.intValue());
                        }
                    }
                }
//                for (Map.Entry<Long, Integer> entry : mapUSession.entrySet()) {
//                    MessageManager.getInstance().updateSessionUnread("", entry.getKey(), entry.getValue());
//                }
            }

            Map<String, Integer> mapGSession = /*MessageManager.getInstance().*/getPendingGroupUnreadMap();
            if (mapGSession != null && mapGSession.size() > 0) {
                System.out.println(TaskDealWithMsgList.class.getSimpleName() + "--doPendingData--更新群聊session" + mapGSession.size());
                Iterator iterator = mapGSession.keySet().iterator();
                while (iterator.hasNext()) {
                    String gid = iterator.next().toString();
                    if (!TextUtils.isEmpty(gid)) {
                        Integer count = mapGSession.get(gid);
                        if (count != null) {
                            MessageManager.getInstance().updateSessionUnread(gid, -1L, count.intValue());
                        }
                    }
                }
//                for (Map.Entry<String, Integer> entry : mapGSession.entrySet()) {
//                    MessageManager.getInstance().updateSessionUnread(entry.getKey(), -1L, entry.getValue());
//                }
            }

            List<UserInfo> userInfos = /*MessageManager.getInstance().*/getPendingUserList();
            if (userInfos != null) {
                int len = userInfos.size();
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        UserInfo info = userInfos.get(i);
                        MessageManager.getInstance().updateUserAvatarAndNick(info.getUid(), info.getHead(), info.getName());
                    }
                }
            }

            List<MsgAllBean> msgList = /*MessageManager.getInstance().*/getPendingMsgList();
            if (msgList != null) {
                System.out.println(TaskDealWithMsgList.class.getSimpleName() + "--doPendingData--更新消息--" + msgList.size() + "--requestId=" + requestId);
                if (msgList.size() > 0) {
                    boolean isSuccess = msgDao.insertOrUpdateMsgList(msgList);
                    if (isSuccess) {
                        SocketUtil.getSocketUtil().sendData(SocketData.msg4ACK(requestId, null), null,requestId);
                        System.out.println(TAG + "--发送回执2--requestId=" + requestId);
                        LogUtil.writeLog("--发送回执2--requestId=" + requestId);
                    } else {
                        LogUtil.writeLog("--数据更新失败--requestId="+ requestId + ";" + new Gson().toJson(msgList));
                        // 上报后的Crash会显示该标签
                        CrashReport.setUserSceneTag(MyAppLication.getInstance().getApplicationContext(), BUGLY_TAG_SAVE_DATA);
                        // 上传异常数据
                        CrashReport.putUserData(MyAppLication.getInstance().getApplicationContext(), BuglyTag.BUGLY_TAG_1, "Id:" + requestId + ";" + new Gson().toJson(msgList));
                    }
                } else {
                    SocketUtil.getSocketUtil().sendData(SocketData.msg4ACK(requestId, null), null,requestId);
                    System.out.println(TAG + "--发送回执3--requestId=" + requestId);
                    LogUtil.writeLog("--发送回执3--requestId=" + requestId);
                }
            } else {
                SocketUtil.getSocketUtil().sendData(SocketData.msg4ACK(requestId, null), null,requestId);
                System.out.println(TAG + "--发送回执4--requestId=" + requestId);
                LogUtil.writeLog("--发送回执4--requestId=" + requestId);
            }
            Map<String, MsgAllBean> mapCancel = /*MessageManager.getInstance().*/getPendingCancelMap();
            if (mapCancel != null && mapCancel.size() > 0) {
                System.out.println(TaskDealWithMsgList.class.getSimpleName() + "--doPendingData--更新cancel消息" + mapCancel.size());
                Iterator iterator = mapCancel.keySet().iterator();
                while (iterator.hasNext()) {
                    MsgAllBean bean = mapCancel.get(iterator.next().toString());
                    msgDao.msgDel4Cancel(bean.getMsg_id(), bean.getMsgCancel().getMsgidCancel());
                }
            }
            clearPendingList();
            MessageManager.getInstance().removeMsgTask(requestId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //获取私聊未读map
    public Map<Long, Integer> getPendingUserUnreadMap() {
        return pendingUserUnread;
    }

    //获取私聊未读map
    public Map<String, MsgAllBean> getPendingCancelMap() {
        return pendingCancelMessages;
    }

    //获取群聊未读map
    public Map<String, Integer> getPendingGroupUnreadMap() {
        return pendingGroupUnread;
    }

    public Map<String, MsgAllBean> getPendingMessagesMap() {
        return pendingMessages;
    }

    /*
     * 数据更新完毕，清理pending数据
     * */
    public void clearPendingList() {
        if (pendingMessages != null) {
            pendingMessages.clear();
        }

        if (pendingUsers != null) {
            pendingUsers.clear();
        }

        if (pendingUserUnread != null) {
            pendingUserUnread.clear();
        }

        if (pendingGroupUnread != null) {
            pendingGroupUnread.clear();
        }

        if (pendingCancelMessages != null) {
            pendingCancelMessages.clear();
        }
    }

    public List<MsgAllBean> getPendingMsgList() {
        List<MsgAllBean> list = null;
        try {
            if (pendingMessages != null && pendingMessages.size() > 0) {
                list = new ArrayList<>();
                Iterator iterator = pendingMessages.keySet().iterator();
                while (iterator.hasNext()) {
                    list.add(pendingMessages.get(iterator.next().toString()));
                }
            }
        } catch (Exception e) {

        }
        return list;
    }

    //获取需要更新头像和昵称的用户
    public List<UserInfo> getPendingUserList() {
        List<UserInfo> list = null;
        if (pendingUsers != null && pendingUsers.size() > 0) {
            list = new ArrayList<>();
            for (Map.Entry<Long, UserInfo> entry : pendingUsers.entrySet()) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    public Map<Long, UserInfo> getUserMap() {
        return pendingUsers;
    }

    @SuppressLint("CheckResult")
    public void saveAndRefresh() {
        Observable.just(0)
                .map(new Function<Integer, Boolean>() {
                    @Override
                    public Boolean apply(Integer integer) throws Exception {
                        return doPendingData();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(Observable.<Boolean>empty())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean b) throws Exception {
                        if (b) {
                            notifyUIRefresh();
                        }
                    }
                });
    }

}
