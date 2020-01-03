package com.yanlong.im.chat.manager;

import android.content.Intent;
import android.text.TextUtils;

import com.hm.cxpay.eventbus.PayResultEvent;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.ApplyBean;
import com.yanlong.im.chat.bean.ChatMessage;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.MsgConversionBean;
import com.yanlong.im.chat.bean.ReadDestroyBean;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.eventbus.EventRefreshMainMsg;
import com.yanlong.im.chat.eventbus.EventRefreshUser;
import com.yanlong.im.chat.task.TaskDealWithMsgList;
import com.yanlong.im.chat.ui.ChatActionActivity;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.utils.DaoUtil;
import com.yanlong.im.utils.GroupHeadImageUtil;
import com.yanlong.im.utils.MediaBackUtil;
import com.yanlong.im.utils.socket.MsgBean;
import com.yanlong.im.utils.socket.SocketData;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.EventGroupChange;
import net.cb.cb.library.bean.EventIsShowRead;
import net.cb.cb.library.bean.EventLoginOut4Conflict;
import net.cb.cb.library.bean.EventRefreshChat;
import net.cb.cb.library.bean.EventRefreshFriend;
import net.cb.cb.library.bean.EventSwitchDisturb;
import net.cb.cb.library.bean.EventUserOnlineChange;
import net.cb.cb.library.bean.RefreshApplyEvent;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.event.EventFactory;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.GsonUtils;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.TimeToString;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;

import static com.yanlong.im.utils.socket.MsgBean.MessageType.ACCEPT_BE_FRIENDS;
import static com.yanlong.im.utils.socket.MsgBean.MessageType.ACTIVE_STAT_CHANGE;
import static com.yanlong.im.utils.socket.MsgBean.MessageType.CANCEL;
import static com.yanlong.im.utils.socket.MsgBean.MessageType.P2P_AU_VIDEO_DIAL;
import static com.yanlong.im.utils.socket.MsgBean.MessageType.REMOVE_FRIEND;
import static com.yanlong.im.utils.socket.MsgBean.MessageType.REQUEST_FRIEND;
import static com.yanlong.im.utils.socket.MsgBean.MessageType.REQUEST_GROUP;
import static com.yanlong.im.utils.socket.SocketData.createMsgBean;
import static com.yanlong.im.utils.socket.SocketData.oldMsgId;

/**
 * @author Liszt
 * @date 2019/9/24
 * Description 消息管理类
 */
public class MessageManager {
    private final String TAG = MessageManager.class.getSimpleName();

    private static int SESSION_TYPE = 0;//无会话,1:单人;2群,3静音模式
    public static Long SESSION_FUID;//单人会话id
    public static String SESSION_GID;//群会话id

    private static MessageManager INSTANCE;
    private MsgDao msgDao = new MsgDao();
    private UserDao userDao = new UserDao();
    private static boolean isMessageChange;//是否有聊天消息变化

    private static List<String> loadGids = new ArrayList<>();//需要异步加载群数据的群id
    private static List<Long> loadUids = new ArrayList<>();//需要异步记载用户数据的用户id

//    private static Map<String, MsgAllBean> sequenceMap = new HashMap<>();//消息发送队列


    //缓存
    private static Map<Long, UserInfo> cacheUsers = new HashMap<>();//用户信息缓存
    private static Map<String, Group> cacheGroups = new HashMap<>();//群信息缓存
    private static List<Session> cacheSessions = new ArrayList<>();//Session缓存，
    private static Map<Long, List<MsgAllBean>> cacheMessagePrivate = new HashMap();//私聊消息缓存，以用户id为key
    private static Map<String, List<MsgAllBean>> cacheMessageGroup = new HashMap();//群聊消息缓存，以群id为key
    private static List<Group> saveGroups = new ArrayList<>();//已保存群信息缓存


    //批量消息待处理
//    private static Map<String, MsgAllBean> pendingMessages = new HashMap<>();//批量接收到的消息，待保存到数据库
//    private static Map<String, MsgAllBean> pendingCancelMessages = new HashMap<>();//批量接收到的撤销消息，待保存到数据库
//    private static Map<Long, UserInfo> pendingUsers = new HashMap<>();//批量用户信息（头像和昵称），待保存到数据库
//    private static Map<String, Integer> pendingGroupUnread = new HashMap<>();//批量群session未读数，待保存到数据库
//    private static Map<Long, Integer> pendingUserUnread = new HashMap<>();//批量私聊session未读数，待保存到数据库

    private static Map<String, TaskDealWithMsgList> taskMaps = new HashMap<>();//批量消息的处理


    private long playTimeOld = 0;//当前声音播放时间
    private long playVBTimeOld = 0; //当前震动时间

    private static Boolean CAN_STAMP = true;//true 允许戳一戳弹窗 ,false 不允许


    public static MessageManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MessageManager();
        }
        return INSTANCE;
    }

    /*
     * 消息接收流程
     * */
    public synchronized void onReceive(MsgBean.UniversalMessage bean) {
        List<MsgBean.UniversalMessage.WrapMessage> msgList = bean.getWrapMsgList();
        if (msgList != null) {
            int length = msgList.size();
            if (length > 0) {
                if (length == 1) {//收到单条消息
                    MsgBean.UniversalMessage.WrapMessage wrapMessage = msgList.get(0);
                    dealWithMsg(wrapMessage, false, true, bean.getRequestId());

                } else {//收到多条消息（如离线）
//                    LogUtil.getLog().d("a=", "--总任务数="  + "--当前时间-3=" + System.currentTimeMillis());
                    TaskDealWithMsgList taskMsgList = getMsgTask(bean.getRequestId());
                    if (taskMsgList == null) {
                        taskMsgList = new TaskDealWithMsgList(msgList, bean.getRequestId());
                        System.out.println(TAG + "--MsgTask--add--requestId=" + bean.getRequestId());
                        taskMaps.put(bean.getRequestId(), taskMsgList);
                    } else {
                        taskMsgList.clearPendingList();
                    }
                    taskMsgList.execute();
//                    LogUtil.getLog().d("a=", TaskDealWithMsgList.class.getSimpleName() + "--总任务数="  + "--当前时间-4=" + System.currentTimeMillis());
                }
            }
        }
    }

    /*
     * 处理接收到的消息
     * 分两类处理，一类是需要产生本地消息记录的，一类是相关指令，无需产生消息记录
     * @param wrapMessage 接收到的消息
     * @param isList 是否是批量消息
     * @return 返回结果，不需要处理逻辑的消息，默认处理成功
     * */
    public boolean dealWithMsg(MsgBean.UniversalMessage.WrapMessage wrapMessage, boolean isList, boolean canNotify, String requestId) {

        if (wrapMessage != null && wrapMessage.getMsgType() != null && wrapMessage.getMsgType() != MsgBean.MessageType.ACTIVE_STAT_CHANGE) {
            LogUtil.getLog().e("===收到=msg=" + GsonUtils.optObject(wrapMessage));
        }

        if (wrapMessage.getMsgType() == MsgBean.MessageType.UNRECOGNIZED) {
            return true;
        }
        boolean result = true;
        boolean hasNotified = false;//已经通知刷新了
        boolean isCancelValid = false;//是否是有效撤销信息
        boolean isFromSelf = false;
        if (UserAction.getMyId() != null) {
            isFromSelf = wrapMessage.getFromUid() == UserAction.getMyId().intValue();
        }
        if (!TextUtils.isEmpty(wrapMessage.getMsgId())) {
            if (oldMsgId.contains(wrapMessage.getMsgId())) {
                LogUtil.getLog().e(TAG, ">>>>>重复消息: " + wrapMessage.getMsgId());
                System.out.println(TAG + ">>>>>重复消息: " + wrapMessage.getMsgId());
                return true;
            } else {
                if (oldMsgId.size() >= 500) {
                    oldMsgId.remove(0);
                }
                oldMsgId.add(wrapMessage.getMsgId());
            }
        }
        updateUserAvatarAndNick(wrapMessage, isList, requestId);
        MsgAllBean bean = MsgConversionBean.ToBean(wrapMessage);
        if (bean != null && !TextUtils.isEmpty(requestId)) {
            bean.setRequest_id(requestId);
        }
        switch (wrapMessage.getMsgType()) {
            case CHAT://文本
            case IMAGE://图片
            case STAMP://戳一戳
            case VOICE://语音
            case SHORT_VIDEO://短视频
//            case TRANSFER://转账
            case BUSINESS_CARD://名片
            case RED_ENVELOPER://红包
            case RECEIVE_RED_ENVELOPER://领取红包
            case SNAPSHOT_LOCATION://位置
            case ASSISTANT://小助手消息
            case BALANCE_ASSISTANT://零钱助手消息
            case CHANGE_VICE_ADMINS:// 管理员变更通知
                if (bean != null) {
                    result = saveMessageNew(bean, isList);
                }
                break;
            case P2P_AU_VIDEO:// 音视频消息
                if (bean != null) {
                    if (bean.getP2PAuVideoMessage() != null && "cancel".equals(bean.getP2PAuVideoMessage().getOperation())) {
                        bean.getP2PAuVideoMessage().setDesc("对方" + bean.getP2PAuVideoMessage().getDesc());
                    } else if (bean.getP2PAuVideoMessage() != null && "reject".equals(bean.getP2PAuVideoMessage().getOperation())) {
                        bean.getP2PAuVideoMessage().setDesc(bean.getP2PAuVideoMessage().getDesc().replace("对方", ""));
                    } else if (bean.getP2PAuVideoMessage() != null && "notaccpet".equals(bean.getP2PAuVideoMessage().getOperation())) {
                        bean.getP2PAuVideoMessage().setDesc("对方已取消");
                    } else if (bean.getP2PAuVideoMessage() != null && "interrupt".equals(bean.getP2PAuVideoMessage().getOperation())) {
                        bean.getP2PAuVideoMessage().setDesc("通话中断");
                    }
                    result = saveMessageNew(bean, isList);
                }
                break;
            case ACCEPT_BE_FRIENDS://接受成为好友,需要产生消息后面在处理
                checkDoubleMessage(wrapMessage);//检测双重消息
                if (bean != null) {
                    result = saveMessageNew(bean, isList);
                }
                notifyRefreshFriend(false, isFromSelf ? wrapMessage.getToUid() : wrapMessage.getFromUid(), CoreEnum.ERosterAction.ACCEPT_BE_FRIENDS);
                // TODO 双方互添加好友的情况
                EventBus.getDefault().post(new RefreshApplyEvent(wrapMessage.getFromUid(), CoreEnum.EChatType.PRIVATE, 1));
                ApplyBean applyBean1 = msgDao.getApplyBean(wrapMessage.getFromUid() + "");
                if (applyBean1 != null) {
                    applyBean1.setStat(2);
                    msgDao.applyFriend(applyBean1);
                }
                break;
            case REQUEST_FRIEND://请求添加为好友
//                if (!TextUtils.isEmpty(wrapMessage.getRequestFriend().getContactName())) {
                UserAction userAction = new UserAction();
                userAction.friendGet4Apply(new CallBack<ReturnBean<List<ApplyBean>>>() {
                    @Override
                    public void onResponse(Call<ReturnBean<List<ApplyBean>>> call, Response<ReturnBean<List<ApplyBean>>> response) {
                        if (response.body() == null || !response.body().isOk()) {
                            return;
                        }
                        List<ApplyBean> applyBeanList = response.body().getData();
                        for (int i = 0; i < applyBeanList.size(); i++) {
                            ApplyBean applyBean = applyBeanList.get(i);
                            applyBeanList.get(i).setAid(applyBean.getUid() + "");
                            applyBeanList.get(i).setChatType(CoreEnum.EChatType.PRIVATE);
                            if (!TextUtils.isEmpty(wrapMessage.getRequestFriend().getContactName())) {
                                applyBeanList.get(i).setAlias(wrapMessage.getRequestFriend().getContactName());
                            }
                            applyBeanList.get(i).setStat(1);

                            msgDao.applyFriend(applyBean);
                        }
                    }
                });
//                }
                msgDao.remidCount("friend_apply");
                notifyRefreshFriend(true, isFromSelf ? wrapMessage.getToUid() : wrapMessage.getFromUid(), CoreEnum.ERosterAction.REQUEST_FRIEND);
                break;
            case REMOVE_FRIEND:
                notifyRefreshFriend(true, isFromSelf ? wrapMessage.getToUid() : wrapMessage.getFromUid(), CoreEnum.ERosterAction.REMOVE_FRIEND);
                break;
            case CHANGE_GROUP_MASTER://转让群主
                if (bean != null) {
                    result = saveMessageNew(bean, isList);
                    refreshGroupInfo(bean.getGid());
                    hasNotified = true;
                }
                break;
            case OUT_GROUP://退出群聊，如果该群是已保存群聊，需要改为未保存
                if (wrapMessage.getFromUid() != UserAction.getMyId()) {//不是自己退群，才更新（自己退群，session信息已经被删除）
                    if (bean != null) {
                        result = saveMessageNew(bean, isList);
                        refreshGroupInfo(bean.getGid());
                        hasNotified = true;
                    }
                } else {
                    MemberUser memberUser = userToMember(UserAction.getMyInfo(), bean.getGid());
                    msgDao.removeGroupMember(bean.getGid(), memberUser);
                    notifyGroupChange(false);
                    hasNotified = true;
                }
                break;
            case REMOVE_GROUP_MEMBER://自己被移除群聊，如果该群是已保存群聊，需要改为未保存
                if (bean != null) {
                    result = saveMessageNew(bean, isList);
                    MemberUser memberUser = userToMember(UserAction.getMyInfo(), bean.getGid());
                    msgDao.removeGroupMember(bean.getGid(), memberUser);
                    changeGroupAvatar(bean.getGid());
                    notifyGroupChange(false);
                    hasNotified = true;
                }
                break;
            case REMOVE_GROUP_MEMBER2://其他群成员被移除群聊，可能会有群主退群，涉及群主迭代,所以需要从服务器重新拉取数据
                removeGroupMember(wrapMessage);
                refreshGroupInfo(wrapMessage.getGid());
                notifyGroupChange(false);
                hasNotified = true;
                break;
            case ACCEPT_BE_GROUP://接受入群，
                if (bean != null) {
                    result = saveMessageNew(bean, isList);
                    refreshGroupInfo(bean.getGid());
                    hasNotified = true;
                }
                notifyGroupChange(true);
                break;
            case REQUEST_GROUP://群主会收到成员进群的请求的通知
//                LogUtil.getLog().e("==wrapMessage=json="+GsonUtils.optObject(wrapMessage));
                for (MsgBean.GroupNoticeMessage ntm : wrapMessage.getRequestGroup().getNoticeMessageList()) {

                    ApplyBean applyBean = new ApplyBean();
                    applyBean.setAid(wrapMessage.getGid() + ntm.getUid());
                    applyBean.setChatType(CoreEnum.EChatType.GROUP);

                    applyBean.setGid(wrapMessage.getGid());

                    Realm realm = DaoUtil.open();
                    realm.beginTransaction();
                    Group group = realm.where(Group.class).equalTo("gid", wrapMessage.getGid()).findFirst();
                    if (group != null) {
                        if (StringUtil.isNotNull(group.getName())) {
                            applyBean.setGroupName(group.getName());
                        } else {
                            applyBean.setGroupName(msgDao.getGroupName(group));
                        }
                    }
                    realm.close();

                    applyBean.setJoinType(wrapMessage.getRequestGroup().getJoinType().getNumber());
                    applyBean.setInviter(wrapMessage.getRequestGroup().getInviter());
                    applyBean.setInviterName(wrapMessage.getRequestGroup().getInviterName());
                    applyBean.setUid(ntm.getUid());
                    applyBean.setNickname(ntm.getNickname());
                    applyBean.setAvatar(ntm.getAvatar());
                    applyBean.setStat(1);

                    msgDao.applyGroup(applyBean);
                }
                msgDao.remidCount("friend_apply");
                notifyRefreshFriend(true, -1L, CoreEnum.ERosterAction.DEFAULT);//刷新首页 通讯录底部小红点
//                notifyRefreshFriend(true, -1L, CoreEnum.ERosterAction.REQUEST_FRIEND);//假动作
                break;
            case CHANGE_GROUP_META://修改群属性
                MsgBean.ChangeGroupMetaMessage.RealMsgCase realMsgCase = wrapMessage.getChangeGroupMeta().getRealMsgCase();
                switch (realMsgCase) {
                    case NAME://群名
                        if (bean != null) {
                            result = saveMessageNew(bean, isList);
                        }
                        msgDao.groupNameUpadte(wrapMessage.getGid(), wrapMessage.getChangeGroupMeta().getName());
                        break;
                    case PROTECT_MEMBER://群成员保护
                        msgDao.groupContactIntimatelyUpdate(wrapMessage.getGid(), wrapMessage.getChangeGroupMeta().getProtectMember());
                        break;
                    case AVATAR://群头像
                        break;
                    case SHUT_UP:// 是否开启全群禁言
                        // 更新群禁言状态
                        if (bean != null) {
                            result = saveMessageNew(bean, isList);
                        }
                        break;
                }
                break;
            case DESTROY_GROUP://销毁群
                String groupName = wrapMessage.getDestroyGroup().getName();
                String icon = wrapMessage.getDestroyGroup().getAvatar();
                msgDao.groupExit(wrapMessage.getGid(), groupName, icon, 1);
                break;
            case FORCE_OFFLINE://强制退出，登录冲突
                EventLoginOut4Conflict eventLoginOut4Conflict = new EventLoginOut4Conflict();
                if (wrapMessage.getForceOffline().getForceOfflineReason() == MsgBean.ForceOfflineReason.CONFLICT) {// 登录冲突
                    String phone = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.PHONE).get4Json(String.class);
                    eventLoginOut4Conflict.setMsg("您的账号" + phone + "已经在另一台设备上登录。如果不是您本人操作,请尽快修改密码");
                } else if (wrapMessage.getForceOffline().getForceOfflineReason() == MsgBean.ForceOfflineReason.LOCKED) {//被冻结
                    eventLoginOut4Conflict.setMsg("你已被限制登录");
                }
                EventBus.getDefault().post(eventLoginOut4Conflict);
                break;
            case AT://@消息
                if (bean != null) {
                    result = saveMessageNew(bean, isList);
                }
                updateAtMessage(wrapMessage);
                break;
            case ACTIVE_STAT_CHANGE://在线状态改变
                updateUserOnlineStatus(wrapMessage);
                notifyRefreshFriend(true, isFromSelf ? wrapMessage.getToUid() : wrapMessage.getFromUid(), CoreEnum.ERosterAction.UPDATE_INFO);
                notifyOnlineChange(wrapMessage.getFromUid());
                break;
            case CANCEL://撤销消息
                if (bean != null) {
                    String cancelMsgId = wrapMessage.getCancel().getMsgId();
                    if (isList) {
                        TaskDealWithMsgList task = getMsgTask(requestId);
                        if (task != null) {
                            Map<String, MsgAllBean> pendingMessages = task.getPendingMessagesMap();
                            Map<String, MsgAllBean> pendingCancelMessages = task.getPendingCancelMap();
                            if (pendingMessages != null && pendingCancelMessages != null) {
                                if (pendingMessages.containsKey(cancelMsgId)) {
                                    result = saveMessageNew(bean, isList);
                                    pendingCancelMessages.put(bean.getMsg_id(), bean);
                                    isCancelValid = true;
                                } else {
                                    MsgAllBean msgAllBean = msgDao.getMsgById(cancelMsgId);
                                    if (msgAllBean != null) {
                                        result = saveMessageNew(bean, isList);
                                        pendingCancelMessages.put(bean.getMsg_id(), bean);
                                        isCancelValid = true;
                                    }
                                }
                            }
                        }
                    } else {
                        //TODO:saveMessageNew的有更新未读数
                        // 判断消息是否存在，不存在则不保存
                        MsgAllBean msgAllBean = msgDao.getMsgById(cancelMsgId);
                        if (msgAllBean != null) {
                            result = saveMessageNew(bean, isList);
                            msgDao.msgDel4Cancel(wrapMessage.getMsgId(), cancelMsgId);
                            isCancelValid = true;
                        }
                    }
                    EventBus.getDefault().post(new EventRefreshChat());
                    // 处理图片撤回，在预览弹出提示
                    EventFactory.ClosePictureEvent event = new EventFactory.ClosePictureEvent();
                    event.msg_id = bean.getMsgCancel().getMsgidCancel();
                    event.name = bean.getFrom_nickname();
                    EventBus.getDefault().post(event);
                    // 处理语音撤回，对方在播放时停止播放
                    EventFactory.StopVoiceeEvent eventVoice = new EventFactory.StopVoiceeEvent();
                    eventVoice.msg_id = bean.getMsgCancel().getMsgidCancel();
                    EventBus.getDefault().post(eventVoice);
                    // 处理视频撤回，对方在播放时停止播放
                    EventFactory.StopVideoEvent eventVideo = new EventFactory.StopVideoEvent();
                    eventVideo.msg_id = bean.getMsgCancel().getMsgidCancel();
                    eventVideo.name = bean.getFrom_nickname();
                    EventBus.getDefault().post(eventVideo);
                    MessageManager.getInstance().setMessageChange(true);
                }
                break;
            case RESOURCE_LOCK://资源锁定
                updateUserLockCloudRedEnvelope(wrapMessage);
                break;
            case CHANGE_SURVIVAL_TIME: //阅后即焚
                if (bean != null) {
                    result = saveMessageNew(bean, isList);
                }
                int survivalTime = wrapMessage.getChangeSurvivalTime().getSurvivalTime();
                if (!TextUtils.isEmpty(wrapMessage.getGid())) {
                    userDao.updateGroupReadDestroy(wrapMessage.getGid(), survivalTime);
                } else {
                    userDao.updateReadDestroy(isFromSelf ? wrapMessage.getToUid() : wrapMessage.getFromUid(), survivalTime);
                }
                EventBus.getDefault().post(new ReadDestroyBean(survivalTime, wrapMessage.getGid(), wrapMessage.getFromUid()));
                break;
            case READ://已读消息
                msgDao.setUpdateRead(isFromSelf ? wrapMessage.getToUid() : wrapMessage.getFromUid(), wrapMessage.getRead().getTimestamp());
                LogUtil.getLog().d(TAG, "已读消息:" + wrapMessage.getRead().getTimestamp());
                break;
            case SWITCH_CHANGE: //开关变更

                if (wrapMessage.getSwitchChange().getSwitchType() == MsgBean.SwitchChangeMessage.SwitchType.UNRECOGNIZED) {
                    return true;
                }
                LogUtil.getLog().d(TAG, "开关变更:" + wrapMessage.getSwitchChange().getSwitchType());

                // TODO　处理老版本不兼容问题
                if(wrapMessage.getSwitchChange().getSwitchType()== MsgBean.SwitchChangeMessage.SwitchType.UNRECOGNIZED){
                    return true;
                }
                int switchType = wrapMessage.getSwitchChange().getSwitchType().getNumber();
                int switchValue = wrapMessage.getSwitchChange().getSwitchValue();
                long uid = isFromSelf ? wrapMessage.getToUid() : wrapMessage.getFromUid();
                UserInfo userInfo = userDao.findUserInfo(uid);
                if (userInfo == null) {
                    break;
                }
                switch (switchType) {
                    case 0: // 单聊已读
                        userInfo.setFriendRead(switchValue);
                        userDao.updateUserinfo(userInfo);
                        EventBus.getDefault().post(new EventIsShowRead());
                        break;
                    case 1: //vip
                        userInfo.setVip(wrapMessage.getSwitchChange().getSwitchValue() + "");
                        userDao.updateUserinfo(userInfo);
                        // 刷新用户信息
                        EventFactory.FreshUserStateEvent event = new EventFactory.FreshUserStateEvent();
                        event.vip = wrapMessage.getSwitchChange().getSwitchValue() + "";
                        EventBus.getDefault().post(event);
                        break;
                    case 2:  //已读总开关
                        userInfo.setMasterRead(switchValue);
                        userDao.updateUserinfo(userInfo);
                        EventBus.getDefault().post(new EventIsShowRead());
                        break;
                    case 3: // 单人禁言
                    case 4: // 领取群红包
                        if (bean != null) {
                            result = saveMessageNew(bean, isList);
                        }
                        break;
                }
                break;

            case P2P_AU_VIDEO_DIAL:// 音视频通知
                break;
            case PAY_RESULT://支付结果
                MsgBean.PayResultMessage payResult = wrapMessage.getPayResult();
                System.out.println(TAG + "--支付结果=" + payResult.getResult());
                notifyPayResult(payResult);
                break;
            case TRANSFER://转账消息
                if (bean != null) {
                    MsgBean.TransferMessage transferMessage = wrapMessage.getTransfer();
                    if (transferMessage != null) {
                        //领取或退还转账,先更新历史转账消息状态，后存消息
                        if (transferMessage.getOpType() == MsgBean.TransferMessage.OpType.RECEIVE || transferMessage.getOpType() == MsgBean.TransferMessage.OpType.REJECT) {
                            msgDao.updateTransferStatus(transferMessage.getId(), transferMessage.getOpTypeValue());
                        }
                    }
                    result = saveMessageNew(bean, isList);
                }
                break;
        }
        //刷新单个,接收到音视频通话消息不需要刷新
        if (result && !hasNotified && !isList && bean != null && wrapMessage.getMsgType() != P2P_AU_VIDEO_DIAL) {
            setMessageChange(true);
            boolean isGroup = isGroup(wrapMessage.getFromUid(), bean.getGid());
            long chatterId = wrapMessage.getFromUid();
            if (!isGroup && isFromSelf) {
                chatterId = wrapMessage.getToUid();
            }
            if (wrapMessage.getMsgType() == CANCEL) {//撤销消息不能传值，因为撤销消息不一定是当前会话最后一条消息
                if (isCancelValid) {
                    notifyRefreshMsg(isGroup ? CoreEnum.EChatType.GROUP : CoreEnum.EChatType.PRIVATE, chatterId, bean.getGid(), CoreEnum.ESessionRefreshTag.SINGLE, null);
                }
            } else {
                notifyRefreshMsg(isGroup ? CoreEnum.EChatType.GROUP : CoreEnum.EChatType.PRIVATE, chatterId, bean.getGid(), CoreEnum.ESessionRefreshTag.SINGLE, bean);
            }
        }

        //记录批量信息来源
        TaskDealWithMsgList taskMsgList = null;
        if (bean != null) {
            taskMsgList = getMsgTask(bean.getRequest_id());
        }
        if (isList && taskMsgList != null) {
            String gid = wrapMessage.getGid();
            if (TextUtils.isEmpty(gid) && bean != null) {
                gid = bean.getGid();
            }
            if (isGroup(wrapMessage.getFromUid(), gid)) {
                taskMsgList.addGid(gid);
            } else {
                taskMsgList.addUid(wrapMessage.getFromUid());

            }
        }
        checkNotifyVoice(wrapMessage, isList, canNotify);
        return result;
    }

    private boolean isCancelValid(MsgBean.MessageType type, boolean isValid) {
        return type == MsgBean.MessageType.CANCEL && isValid;
    }

    private void notifyOnlineChange(long uid) {
        EventUserOnlineChange event = new EventUserOnlineChange();
        event.setUid(uid);
        EventBus.getDefault().post(event);
    }

    //重新生成群头像
    public void changeGroupAvatar(String gid) {
        Group group = msgDao.getGroup4Id(gid);
        if (group != null) {
            doImgHeadChange(gid, group);
            MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, gid, CoreEnum.ESessionRefreshTag.SINGLE, null);
        }
    }

    private void removeGroupMember(MsgBean.UniversalMessage.WrapMessage wrapMessage) {
        MsgBean.RemoveGroupMember2Message removeGroupMember2 = wrapMessage.getRemoveGroupMember2();
        msgDao.removeGroupMember(wrapMessage.getGid(), removeGroupMember2.getUidList());
    }

    private boolean isGroup(Long uid, String gid) {
        if (!TextUtils.isEmpty(gid)) {
            return true;
        }
        return false;
    }

    /*
     * 更新用户红包锁定功能
     * */
    private void updateUserLockCloudRedEnvelope(MsgBean.UniversalMessage.WrapMessage wrapMessage) {
        MsgBean.ResourceLockMessage lock = wrapMessage.getResourceLock();
        if (lock != null) {
            MsgBean.ResourceLockMessage.ResourceLockType type = lock.getResourceLockType();
            switch (type) {
                case CLOUDREDENVELOPE:
                    UserDao userDao = new UserDao();
                    userDao.updateUserLockRedEnvelope(UserAction.getMyId(), lock.getLock());
                    UserInfo info = UserAction.getMyInfo();
                    if (info != null) {
                        info.setLockCloudRedEnvelope(lock.getLock());
                    }
                    break;
            }
        }
    }

//    /*
//     * 保存消息
//     * @param msgAllBean 消息
//     * @isList 是否是批量消息
//     * */
//    private boolean saveMessage(MsgAllBean msgAllBean, boolean isList) {
//        msgAllBean.setRead(false);//设置未读
//        msgAllBean.setTo_uid(msgAllBean.getTo_uid());
//        boolean result = false;
//        //收到直接存表
//        DaoUtil.update(msgAllBean);
//        if (!TextUtils.isEmpty(msgAllBean.getGid()) && !msgDao.isGroupExist(msgAllBean.getGid())) {
//            if (!loadGids.contains(msgAllBean.getGid())) {
//                loadGids.add(msgAllBean.getGid());
//                loadGroupInfo(msgAllBean.getGid(), msgAllBean.getFrom_uid(), isList, msgAllBean);
//                LogUtil.getLog().d("a=", TAG + "--需要加载群信息");
//            } else {
//                updateSessionUnread(msgAllBean.getGid(), msgAllBean.getFrom_uid(), false);
//                if (isList) {
//                    setMessageChange(true);
//                }
//                result = true;
//            }
//        } else if (TextUtils.isEmpty(msgAllBean.getGid()) && msgAllBean.getFrom_uid() != null && msgAllBean.getFrom_uid() > 0 && !userDao.isUserExist(msgAllBean.getFrom_uid())) {
//            if (!loadUids.contains(msgAllBean.getFrom_uid())) {
//                loadUids.add(msgAllBean.getFrom_uid());
//                loadUserInfo(msgAllBean.getGid(), msgAllBean.getFrom_uid(), isList, msgAllBean);
//                LogUtil.getLog().d("a=", TAG + "--需要加载用户信息");
//
//            } else {
//                LogUtil.getLog().d("a=", TAG + "--异步加载用户信息更新未读数");
//                updateSessionUnread(msgAllBean.getGid(), msgAllBean.getFrom_uid(), false);
//                if (isList) {
//                    setMessageChange(true);
//                }
//                result = true;
//            }
//        } else {
//            updateSessionUnread(msgAllBean.getGid(), msgAllBean.getFrom_uid(), false);
//            if (isList) {
//                setMessageChange(true);
//            }
//            result = true;
//        }
//        return result;
//    }

    /*
     * 保存消息
     * @param msgAllBean 消息
     * @isList 是否是批量消息
     * */
    private boolean saveMessageNew(MsgAllBean msgAllBean, boolean isList) {
        boolean result = false;
        boolean isFromSelf = false;
        if (UserAction.getMyId() != null) {
            isFromSelf = msgAllBean.getFrom_uid() == UserAction.getMyId().intValue();
        }
        try {
            msgAllBean.setTo_uid(msgAllBean.getTo_uid());
            //收到直接存表
            if (isList) {
                TaskDealWithMsgList task = getMsgTask(msgAllBean.getRequest_id());
                if (task != null) {
                    task.getPendingMessagesMap().put(msgAllBean.getMsg_id(), msgAllBean);
                }
//                pendingMessages.put(msgAllBean.getMsg_id(), msgAllBean);//批量消息先保存到map中，后面再批量存到数据库

            } else {
                DaoUtil.update(msgAllBean);
            }
            boolean isCancel = msgAllBean.getMsg_type() == ChatEnum.EMessageType.MSG_CANCEL;
            if (!TextUtils.isEmpty(msgAllBean.getGid()) && !msgDao.isGroupExist(msgAllBean.getGid())) {
                if (!loadGids.contains(msgAllBean.getGid())) {
                    loadGids.add(msgAllBean.getGid());
                    loadGroupInfo(msgAllBean.getGid(), msgAllBean.getFrom_uid(), isList, msgAllBean);
                } else {
                    if (!isList) {
                        updateSessionUnread(msgAllBean.getGid(), msgAllBean.getFrom_uid(), msgAllBean,null);
                        setMessageChange(true);
                    } else {
                        updatePendingSessionUnreadCount(msgAllBean.getGid(), msgAllBean.getFrom_uid(), false, isCancel, msgAllBean.getRequest_id());
                    }
                    result = true;
                }
            } else if (TextUtils.isEmpty(msgAllBean.getGid()) && msgAllBean.getFrom_uid() != null && msgAllBean.getFrom_uid() > 0 && !userDao.isUserExist(msgAllBean.getFrom_uid())) {
                long chatterId = -1;//对方的Id
                if (isFromSelf) {
                    chatterId = msgAllBean.getTo_uid();
                } else {
                    chatterId = msgAllBean.getFrom_uid();
                }
                if (!loadUids.contains(chatterId)) {
                    loadUids.add(chatterId);
                    loadUserInfo(msgAllBean.getGid(), chatterId, isList, msgAllBean);
                    LogUtil.getLog().d("a=", TAG + "--需要加载用户信息");
                } else {
                    LogUtil.getLog().d("a=", TAG + "--异步加载用户信息更新未读数");
                    if (!isList) {
                        updateSessionUnread(msgAllBean.getGid(), chatterId ,msgAllBean,null);
                        setMessageChange(true);
                    } else {
                        updatePendingSessionUnreadCount(msgAllBean.getGid(), chatterId, false, isCancel, msgAllBean.getRequest_id());
                    }
                    result = true;
                }
            } else {
                if (!TextUtils.isEmpty(msgAllBean.getGid())) {
                    if (!isList) {
                        updateSessionUnread(msgAllBean.getGid(), msgAllBean.getFrom_uid(),msgAllBean,null);
                        setMessageChange(true);
                    } else {
                        updatePendingSessionUnreadCount(msgAllBean.getGid(), msgAllBean.getFrom_uid(), false, isCancel, msgAllBean.getRequest_id());
                    }
                } else {
                    long chatterId = isFromSelf ? msgAllBean.getTo_uid() : msgAllBean.getFrom_uid();
                    if (!isList) {
                        updateSessionUnread(msgAllBean.getGid(), chatterId, msgAllBean,null);
                        setMessageChange(true);
                    } else {
                        updatePendingSessionUnreadCount(msgAllBean.getGid(), chatterId, false, isCancel, msgAllBean.getRequest_id());
                    }
                }
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.getLog().d("a=", TAG + "--消息存储失败--msgId=" + msgAllBean.getMsg_id() + "--msgType=" + msgAllBean.getMsg_type());
        }
//        LogUtil.getLog().d("a=", TAG + "--消息存储成功--msgId=" + msgAllBean.getMsg_id() + "--msgType=" + msgAllBean.getMsg_type());
        return result;
    }

    /*
     * 网络加载用户信息,只能接受来自好友的信息
     * */
    private synchronized void loadUserInfo(final String gid, final Long uid, boolean isList, MsgAllBean bean) {
        if (UserAction.getMyId() != null && uid.equals(UserAction.getMyId())) {
            return;
        }
        new UserAction().getUserInfoAndSave(uid, ChatEnum.EUserType.FRIEND, new CallBack<ReturnBean<UserInfo>>() {
            @Override
            public void onResponse(Call<ReturnBean<UserInfo>> call, Response<ReturnBean<UserInfo>> response) {
//                updateSessionUnread(gid, uid, false);
                if (isList) {
                    UserInfo user = response.body().getData();
                    boolean isDisturb = false;
                    if (user != null) {
                        isDisturb = user.getDisturb() == 1;
                    }
                    updatePendingSessionUnreadCount(gid, uid, isDisturb, false, bean.getRequest_id());
                    TaskDealWithMsgList taskMsgList = getMsgTask(bean.getRequest_id());
                    if (taskMsgList != null) {
                        taskMsgList.updateTaskCount();
                    }
                } else {
                    updateSessionUnread(gid, uid, bean,null);
                    setMessageChange(true);
                    notifyRefreshMsg(CoreEnum.EChatType.PRIVATE, uid, gid, CoreEnum.ESessionRefreshTag.SINGLE, bean);
                }
            }

            @Override
            public void onFailure(Call<ReturnBean<UserInfo>> call, Throwable t) {
                super.onFailure(call, t);
                if (isList) {
                    updatePendingSessionUnreadCount(gid, uid, false, false, bean.getRequest_id());
                    TaskDealWithMsgList taskMsgList = getMsgTask(bean.getRequest_id());
                    if (taskMsgList != null) {
                        taskMsgList.updateTaskCount();
                    }
                } else {
                    updateSessionUnread(gid, uid, bean,null);
                    notifyRefreshMsg(CoreEnum.EChatType.PRIVATE, uid, gid, CoreEnum.ESessionRefreshTag.SINGLE, bean);
                }
            }
        });
    }

    /*
     * 网络加载群信息
     * */
    private void loadGroupInfo(final String gid, final long uid, boolean isList, MsgAllBean bean) {
        new MsgAction().groupInfo(gid, new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                super.onResponse(call, response);
                if (isList) {
                    Group group = response.body().getData();
                    boolean isDisturb = false;
                    if (group != null) {
                        isDisturb = group.getNotNotify() == 1;
                    }
                    updatePendingSessionUnreadCount(gid, uid, isDisturb, false, bean.getRequest_id());
                    TaskDealWithMsgList taskMsgList = getMsgTask(bean.getRequest_id());
                    if (taskMsgList != null) {
                        taskMsgList.updateTaskCount();
                    }
                } else {
                    updateSessionUnread(gid, uid, bean,"first");
                    setMessageChange(true);
                    notifyRefreshMsg(CoreEnum.EChatType.GROUP, uid, gid, CoreEnum.ESessionRefreshTag.SINGLE, bean);
                }
            }

            @Override
            public void onFailure(Call<ReturnBean<Group>> call, Throwable t) {
                super.onFailure(call, t);
                if (isList) {
                    updatePendingSessionUnreadCount(gid, uid, false, false, bean.getRequest_id());
                    TaskDealWithMsgList taskMsgList = getMsgTask(bean.getRequest_id());
                    if (taskMsgList != null) {
                        taskMsgList.updateTaskCount();
                    }
                } else {
                    updateSessionUnread(gid, uid, bean,null);
                    notifyRefreshMsg(CoreEnum.EChatType.GROUP, uid, gid, CoreEnum.ESessionRefreshTag.SINGLE, bean);
                }
            }
        });
    }

    public boolean isMessageChange() {
        return isMessageChange;
    }

    public void setMessageChange(boolean isChange) {
        this.isMessageChange = isChange;
    }

    public void createSession(String gid, Long uid) {
        msgDao.sessionCreate(gid, uid);
    }

    /*
     * 更新session未读数
     * */
    public synchronized void updateSessionUnread(String gid, Long from_uid ,MsgAllBean bean,String firstFlag) {
//        LogUtil.getLog().d("a=", TAG + "--更新Session--updateSessionUnread" + "--isCancel=" + isCancel);
        boolean canChangeUnread = true;
        if (!TextUtils.isEmpty(gid)) {
            if (!TextUtils.isEmpty(SESSION_GID) && SESSION_GID.equals(gid)) {
                canChangeUnread = false;
            }
        } else {
            if (SESSION_FUID != null && from_uid != null && SESSION_FUID.equals(from_uid)) {
                canChangeUnread = false;
            }

        }
        msgDao.sessionReadUpdate(gid, from_uid,canChangeUnread, bean,firstFlag);
    }

    /*
     * 更新session未读数
     * */
    public synchronized void updateSessionUnread(String gid, Long from_uid, int count) {
//        LogUtil.getLog().d("a=", TAG + "--更新Session--updateSessionUnread--gid=" + gid + "--uid=" + from_uid + "--count=" + count);
        msgDao.sessionReadUpdate(gid, from_uid, count);
    }

    /*
     * @param isDisturb 是否免打扰
     * @param isCancel 是否是撤销消息
     * */
    public synchronized void updatePendingSessionUnreadCount(String gid, Long uid, boolean isDisturb, boolean isCancel, String requestId) {
//        LogUtil.getLog().d("a=", TAG + "--更新Session--updatePendingSessionUnreadCount--gid=" + gid + "--uid=" + uid + "--isCancel=" + isCancel);
        if (TextUtils.isEmpty(requestId)) {
            return;
        }
        TaskDealWithMsgList task = getMsgTask(requestId);
        if (task == null) {
            return;
        }
        Map<String, Integer> pendingGroupUnread = task.getPendingGroupUnreadMap();
        Map<Long, Integer> pendingUserUnread = task.getPendingUserUnreadMap();
        if (isCancel) {
            if (!TextUtils.isEmpty(gid)) {
                if (pendingGroupUnread.containsKey(gid)) {
                    int count = pendingGroupUnread.get(gid);
                    if (isDisturb) {
                        count = 0;
                        pendingGroupUnread.put(gid, count);
                    } else {
                        count--;
                        pendingGroupUnread.put(gid, count);
                    }
                } else {
                    pendingGroupUnread.put(gid, -1);
                }
            } else {
                if (pendingUserUnread.containsKey(uid)) {
                    int count = pendingUserUnread.get(uid);
                    if (isDisturb) {
                        count = 0;
                        pendingUserUnread.put(uid, count);
                    } else {
                        count--;
                        pendingUserUnread.put(uid, count);
                    }
                } else {
                    pendingUserUnread.put(uid, -1);
                }
            }
        } else {
            if (!TextUtils.isEmpty(gid)) {
                if (pendingGroupUnread.containsKey(gid)) {
                    int count = pendingGroupUnread.get(gid);
                    if (isDisturb) {
                        count = 0;
                        pendingGroupUnread.put(gid, count);
                    } else {
                        if (TextUtils.isEmpty(SESSION_GID) || !SESSION_GID.equals(gid)) {//不是当前会话
                            count++;
                        }
                        pendingGroupUnread.put(gid, count);
                    }
                } else {
                    int count = 0;
                    if (TextUtils.isEmpty(SESSION_GID) || !SESSION_GID.equals(gid)) {//不是当前会话
                        count = isDisturb ? 0 : 1;
                    }
                    pendingGroupUnread.put(gid, count);

                }
            } else {
                if (pendingUserUnread.containsKey(uid)) {
                    int count = pendingUserUnread.get(uid);
                    if (isDisturb) {
                        count = 0;
                        pendingUserUnread.put(uid, count);
                    } else {
                        if (SESSION_FUID == null || !SESSION_FUID.equals(uid)) {//不是当前会话
                            count++;
                        }
                        pendingUserUnread.put(uid, count);
                    }
                } else {
                    int count = 0;
                    if (SESSION_FUID == null || !SESSION_FUID.equals(uid)) {//不是当前会话
                        count = isDisturb ? 0 : 1;
                    }
                    pendingUserUnread.put(uid, count);
                }
            }

        }
    }

    /*
     * 通知刷新消息列表，及未读数，未设置及整体刷新
     * */
    public void notifyRefreshMsg() {
        EventBus.getDefault().post(new EventRefreshMainMsg());
    }


    /*
     * 通知刷新消息列表，及未读数
     * @param chatType 单聊群聊
     * @param uid 单聊即用户id，群聊为null
     * @param gid 群聊即群id，单聊为""
     * @param msg,最后一条消息，也要刷新时间
     * */
    public void notifyRefreshMsg(@CoreEnum.EChatType int chatType, Long uid, String gid, @CoreEnum.ESessionRefreshTag int refreshTag, Object object) {
        setMessageChange(true);
        EventRefreshMainMsg eventRefreshMainMsg = new EventRefreshMainMsg();
        eventRefreshMainMsg.setType(chatType);
        eventRefreshMainMsg.setUid(uid);
        eventRefreshMainMsg.setGid(gid);
        eventRefreshMainMsg.setRefreshTag(refreshTag);
        if (object != null) {
            if (object instanceof MsgAllBean) {
                eventRefreshMainMsg.setMsgAllBean((MsgAllBean) object);
            } else if (object instanceof Session) {
                eventRefreshMainMsg.setSession((Session) object);

            }
        }
        EventBus.getDefault().post(eventRefreshMainMsg);
    }

    /*
     * 通知刷新消息列表，及未读数
     * @param chatType 单聊群聊
     * @param uid 单聊即用户id，群聊为null
     * @param gid 群聊即群id，单聊为""
     * @param msg,最后一条消息，也要刷新时间
     * */
    public void notifyRefreshMsg(@CoreEnum.EChatType int chatType, Long uid, String gid, @CoreEnum.ESessionRefreshTag int refreshTag, Object object, boolean isRefreshTop) {
        EventRefreshMainMsg eventRefreshMainMsg = new EventRefreshMainMsg();
        eventRefreshMainMsg.setType(chatType);
        eventRefreshMainMsg.setUid(uid);
        eventRefreshMainMsg.setGid(gid);
        eventRefreshMainMsg.setRefreshTag(refreshTag);
        eventRefreshMainMsg.setRefreshTop(isRefreshTop);
        if (object != null) {
            if (object instanceof MsgAllBean) {
                eventRefreshMainMsg.setMsgAllBean((MsgAllBean) object);
            } else if (object instanceof Session) {
                eventRefreshMainMsg.setSession((Session) object);

            }
        }
        EventBus.getDefault().post(eventRefreshMainMsg);
    }

    public void notifySwitchDisturb() {
        EventBus.getDefault().post(new EventSwitchDisturb());

    }

    /*
     * 通知刷新聊天界面
     * */
    public void notifyRefreshChat() {
        EventRefreshChat event = new EventRefreshChat();
        EventBus.getDefault().post(event);
    }


    public void deleteSessionAndMsg(Long uid, String gid) {
        msgDao.sessionDel(uid, gid);
        msgDao.msgDel(uid, gid);

    }

    /*
     * 刷新通讯录
     * @param isLocal 是否是本地刷新
     * @param uid 需要刷新的用户id
     * @param action 花名册操作类型
     * */
    public void notifyRefreshFriend(boolean isLocal, long uid, @CoreEnum.ERosterAction int action) {
        EventRefreshFriend event = new EventRefreshFriend();
        event.setLocal(isLocal);
        if (action != CoreEnum.ERosterAction.DEFAULT) {
            event.setUid(uid);
            event.setRosterAction(action);
        }
        EventBus.getDefault().post(event);
    }

    /*
     * 获取缓存信息中用户信息
     * */
    public UserInfo getCacheUserInfo(Long uid) {
        UserInfo info = null;
        if (uid != null && uid > 0) {
            info = cacheUsers.get(uid);
            if (info == null) {
                info = userDao.findUserInfo(uid);
                if (info != null) {
                    cacheUsers.put(uid, info);
                }
            }
        }
        return info;
    }

    /*
     * 获取缓存数据中群信息
     * */
    public Group getCacheGroup(String gid) {
        Group group = null;
        if (!TextUtils.isEmpty(gid)) {
            group = cacheGroups.get(gid);
            if (group == null) {
                group = msgDao.getGroup4Id(gid);
                if (group != null) {
                    cacheGroups.put(gid, group);
                }
            }
        }
        return group;
    }

    /*
     * 更新用户头像和昵称
     * */
    public boolean updateUserAvatarAndNick(long uid, String avatar, String nickName) {
        boolean hasChange = userDao.userHeadNameUpdate(uid, avatar, nickName);
        if (hasChange) {
            updateCacheUserAvatarAndName(uid, avatar, nickName);
        }
        return hasChange;
    }

    /*
     * 更新缓存用户头像及昵称
     * */
    private void updateCacheUserAvatarAndName(long uid, String avatar, String nickName) {
        if (cacheUsers != null) {
            UserInfo info = getCacheUserInfo(uid);
            if (info != null) {
                info.setHead(avatar);
                info.setName(nickName);
                cacheUsers.remove(info);
                cacheUsers.put(uid, info);
            }
        }
    }

    /*
     * 更新缓存用户在线状态及最后在线时间
     * */
    public void updateCacheUserOnlineStatus(long uid, int onlineType, long time) {
        if (cacheUsers != null) {
            UserInfo info = getCacheUserInfo(uid);
            if (info != null) {
                info.setLastonline(time);
                info.setActiveType(onlineType);
                cacheUsers.remove(info);
                cacheUsers.put(uid, info);
            }
        }
    }

    /*
     * 获取内存缓存中session数据
     * */
    public List<Session> getCacheSession() {
        return cacheSessions;
    }

    //检测是否是双重消息，及一条消息需要产生两条本地消息记录,回执在通知消息中发送
    private static void checkDoubleMessage(MsgBean.UniversalMessage.WrapMessage wmsg) {
        if (wmsg.getMsgType() == ACCEPT_BE_FRIENDS) {
            MsgBean.AcceptBeFriendsMessage receiveMessage = wmsg.getAcceptBeFriends();
            if (receiveMessage != null && !TextUtils.isEmpty(receiveMessage.getSayHi())) {
                ChatMessage chatMessage = SocketData.createChatMessage(SocketData.getUUID(), receiveMessage.getSayHi());
                MsgAllBean message = createMsgBean(wmsg, ChatEnum.EMessageType.TEXT, ChatEnum.ESendStatus.NORMAL, SocketData.getFixTime(), chatMessage);
                DaoUtil.save(message);
//                MessageManager.getInstance().updateSessionUnread(message.getGid(), message.getFrom_uid(),false);//不更新未读，只需要一条即可
//                MessageManager.getInstance().setMessageChange(true);
            }
        }
    }

    private boolean updateAtMessage(MsgBean.UniversalMessage.WrapMessage msg) {
        boolean isAt = false;
        String gid = msg.getGid();
        MsgBean.AtMessage atMessage = msg.getAt();
        String message = atMessage.getMsg();
        int atType = msg.getAt().getAtType().getNumber();
        if (atType == 0) {
            List<Long> list = msg.getAt().getUidList();
            if (list == null)
                isAt = false;

            Long uid = UserAction.getMyId();
            for (int i = 0; i < list.size(); i++) {
                if (uid.equals(list.get(i))) {
                    LogUtil.getLog().e(TAG, "有人@我" + uid);
                    if (!gid.equals(SESSION_GID)) {
                        msgDao.atMessage(gid, message, atType);
                        playDingDong();
                    }

                    isAt = true;
                }
            }
        } else {
            if (atMessage.getUidList() == null || atMessage.getUidList().size() == 0) {//是群公告
                refreshGroupInfo(msg.getGid());
            }
            LogUtil.getLog().e(TAG, "@所有人");
            if (!gid.equals(SESSION_GID)) {
                msgDao.atMessage(gid, message, atType);
                playDingDong();
            }
            isAt = true;
        }
        return isAt;
    }

    private void playDingDong() {
        if (System.currentTimeMillis() - playTimeOld < 500) {
            return;
        }
        playTimeOld = System.currentTimeMillis();
        MediaBackUtil.palydingdong(AppConfig.getContext());
    }

    private void updateUserOnlineStatus(MsgBean.UniversalMessage.WrapMessage msg) {
        long fromUid = msg.getFromUid();
        MsgBean.ActiveStatChangeMessage message = msg.getActiveStatChange();
        if (message == null) {
            return;
        }
        fetchTimeDiff(message.getTimestamp());
        userDao.updateUserOnlineStatus(fromUid, message.getActiveTypeValue(), message.getTimestamp());
        MessageManager.getInstance().updateCacheUserOnlineStatus(fromUid, message.getActiveTypeValue(), message.getTimestamp());
    }

    /*
     * 修正本地时间与服务器时间差值，暂时没考虑时区问题
     * */
    private void fetchTimeDiff(long timestamp) {
        long current = System.currentTimeMillis();//本地系统当前时间
        TimeToString.DIFF_TIME = timestamp - current;
    }

    /***
     * 群
     * @param sid 群id
     */
    public void setSessionGroup(String sid) {
        if (SESSION_TYPE == 3)
            return;
        SESSION_TYPE = 2;
        SESSION_FUID = null;
        SESSION_GID = sid;
    }

    /***
     * 单人
     * @param fuid
     */
    public void setSessionSolo(Long fuid) {
        if (SESSION_TYPE == 3)
            return;
        SESSION_TYPE = 1;
        SESSION_FUID = fuid;
        SESSION_GID = null;
    }

    /***
     * 无会话
     */
    public static void setSessionNull() {
        if (SESSION_TYPE == 3)
            return;
        SESSION_TYPE = 0;
        SESSION_FUID = null;
        SESSION_GID = null;
    }

    //允许戳一戳弹窗
    public static void setCanStamp(Boolean canStamp) {
        CAN_STAMP = canStamp;
        LogUtil.getLog().e("==CAN_STAMP==" + CAN_STAMP);
    }

    /***
     * 根据接收到的消息内容，更新用户头像昵称等资料
     * @param msg
     */
    private void updateUserAvatarAndNick(MsgBean.UniversalMessage.WrapMessage msg, boolean isList, String requestId) {
        if (msg.getMsgType() == MsgBean.MessageType.UNRECOGNIZED || msg.getMsgType().getNumber() > 100) {//通知类消息
            return;
        }
        if (isList) {
            if (TextUtils.isEmpty(requestId)) {
                return;
            }
            TaskDealWithMsgList task = getMsgTask(requestId);
            if (task == null) {
                return;
            }
            Map<Long, UserInfo> pendingUsers = task.getUserMap();
            if (pendingUsers == null) {
                return;
            }
            UserInfo info = new UserInfo();
            info.setUid(msg.getFromUid());
            info.setHead(msg.getAvatar());
            info.setName(msg.getNickname());
            pendingUsers.put(msg.getFromUid(), info);
        } else {
            boolean hasChange = updateUserAvatarAndNick(msg.getFromUid(), msg.getAvatar(), msg.getNickname());
            //避免重复刷新通讯录
            if (msg.getMsgType() == REQUEST_FRIEND || msg.getMsgType() == ACCEPT_BE_FRIENDS
                    || msg.getMsgType() == REMOVE_FRIEND || msg.getMsgType() == REQUEST_GROUP
                    || msg.getMsgType() == ACTIVE_STAT_CHANGE) {
                return;
            }
            if (hasChange) {
                notifyRefreshFriend(true, msg.getFromUid(), CoreEnum.ERosterAction.UPDATE_INFO);
            }
        }
    }

    /*
     * 检测接收消息是否发出通知或者震动
     * @param isList 是否是批量消息
     * @param canNotify 是否能发出通知声音后震动，批量消息只要通知一声
     * */
    private void checkNotifyVoice(MsgBean.UniversalMessage.WrapMessage msg, boolean isList, boolean canNotify) {
        if (msg.getMsgType() != null && msg.getMsgType().getNumber() > 100) {
            return;
        }
        if (!isList) {
            doNotify(msg);
        } else {
            if (canNotify) {
                doNotify(msg);
            }
        }
    }

    /*
     * 发出通知声音或者震动
     * */
    private void doNotify(MsgBean.UniversalMessage.WrapMessage msg) {
        //        LogUtil.getLog().e("===msg.getMsgType()=="+msg.getMsgType()+"======SESSION_TYPE=="+SESSION_TYPE
        //                +"======SESSION_FUID=="+SESSION_FUID+"======SESSION_GID=="+SESSION_GID);
        boolean isGroup = StringUtil.isNotNull(msg.getGid());
        //会话已经静音
        Session session = isGroup ? DaoUtil.findOne(Session.class, "gid", msg.getGid()) : DaoUtil.findOne(Session.class, "from_uid", msg.getFromUid());
        if (session != null && session.getIsMute() == 1) {

            return;
        }
        if (isGroup && SESSION_TYPE == 2 && SESSION_GID.equals(msg.getGid())) { //群
            //当前会话是本群不提示

        } else if (SESSION_TYPE == 1 && SESSION_FUID != null && SESSION_FUID.longValue() == msg.getFromUid()) {//单人
            //当前会话就是这个人
            if (msg.getMsgType() == MsgBean.MessageType.STAMP) {
                playVibration();
            }
        } else if (SESSION_TYPE == 3) {//静音模式

        } else if (msg.getMsgType() == MsgBean.MessageType.STAMP && CAN_STAMP) {//戳一戳
            //不在聊天页 或 在聊天页，当前聊天人不是这个人
            AppConfig.getContext().startActivity(new Intent(AppConfig.getContext(), ChatActionActivity.class)
                    .putExtra(ChatActionActivity.AGM_DATA, msg.toByteArray())
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );
        } else {
            playDingDong();
        }
    }

    //振动
    private void playVibration() {
        if (System.currentTimeMillis() - playVBTimeOld < 500) {
            return;
        }
        playVBTimeOld = System.currentTimeMillis();
        MediaBackUtil.playVibration(AppConfig.getContext(), 200);
    }

    /*
     * 更新缓存群头像
     * */
    public void updateCacheGroupAvatar(String gid, String url) {
        if (!TextUtils.isEmpty(gid) && !TextUtils.isEmpty(url)) {
            Group group = getCacheGroup(gid);
            if (group != null) {
                group.setAvatar(url);
                cacheGroups.remove(gid);
                cacheGroups.put(gid, group);
            }
        }
    }

    /*
     * 更新缓存置顶或者免打扰
     * */
    public void updateCacheTopOrDisturb(String gid, int top, int disturb) {
        if (!TextUtils.isEmpty(gid)) {
            Group group = getCacheGroup(gid);
            if (group != null) {
//                group.setAvatar(url);
                cacheGroups.remove(gid);
                cacheGroups.put(gid, group);
            }
        }
    }

    public void updateCacheGroup(Group group) {
        if (cacheGroups != null && group != null) {
            if (cacheGroups.containsValue(group)) {
                cacheGroups.remove(group.getGid());
            }
            cacheGroups.put(group.getGid(), group);
        }
    }

    public void updateCacheUser(UserInfo user) {
        if (cacheUsers != null && user != null) {
            if (cacheUsers.containsValue(user)) {
                cacheUsers.remove(user.getUid());
            }
            cacheUsers.put(user.getUid(), user);

        }
    }

    /*
     * 更新session 置顶及免打扰字段
     * */
    public void updateSessionTopAndDisturb(String gid, Long uid, int top, int disturb) {
        msgDao.updateSessionTopAndDisturb(gid, uid, top, disturb);
    }

    public void removeLoadGids(String gid) {
        if (loadGids != null && !TextUtils.isEmpty(gid)) {
            loadGids.remove(gid);
        }
    }

    public void removeLoadUids(Long uid) {
        if (loadUids != null && uid != null) {
            loadUids.remove(uid);
        }
    }

    public void addSavedGroup(List<Group> list) {
        if (list != null && list.size() > 0) {
            saveGroups.addAll(list);
        }
    }

    public List<Group> getSavedGroups() {
        return saveGroups;
    }

    /*
     * 群成员数据转变为UserInfo
     * */
    public UserInfo memberToUser(MemberUser user) {
        UserInfo info = null;
        if (user != null) {
            info = new UserInfo();
            info.setUid(user.getUid());
            info.setName(user.getName());
            info.setHead(user.getHead());
            info.setMembername(user.getMembername());
            info.setInviter(user.getInviter());
            info.setInviterName(user.getInviterName());
            info.setJoinTime(user.getJoinTime());
            info.setJoinType(user.getJoinType());
            info.setImid(user.getImid());
            info.setSex(user.getSex());
            info.setTag(user.getTag());
        }
        return info;
    }

    /*
     * UserInfo 转变为 MemberUser
     * */
    public MemberUser userToMember(UserInfo user, String gid) {
        MemberUser info = null;
        if (user != null) {
            info = new MemberUser();
            info.setUid(user.getUid());
            info.setName(user.getName());
            info.setHead(user.getHead());
            info.setMembername(user.getMembername());
            info.setInviter(user.getInviter());
            info.setInviterName(user.getInviterName());
            info.setJoinTime(user.getJoinTime());
            info.setJoinType(user.getJoinType());
            info.setImid(user.getImid());
            info.setSex(user.getSex());
//            info.setTag(user.getTag());//tag不能直接用userInfo的
            info.init(gid);
        }
        return info;
    }

    /*
     * UserInfo 转变为 MemberUser
     * */
    public List<MemberUser> getMemberList(List<UserInfo> list, String gid) {
        List<MemberUser> memberUsers = null;
        if (list == null) {
            return memberUsers;
        }
        int len = list.size();
        if (len > 0) {
            memberUsers = new ArrayList<>();
        }
        for (int i = 0; i < len; i++) {
            UserInfo user = list.get(i);
            if (user != null) {
                memberUsers.add(userToMember(user, gid));
            }
        }
        return memberUsers;
    }


    /*
     * 检测该群是否还有效，即自己是否还在该群中,有效为true，无效为false
     * */
    public boolean isGroupValid(Group group) {
        if (group != null) {
            List<MemberUser> users = group.getUsers();
            if (users != null) {
                MemberUser member = MessageManager.getInstance().userToMember(UserAction.getMyInfo(), group.getGid());
                if (member != null && !users.contains(member)) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * 检测该群是否还有效，即自己是否还在该群中
     * */
    public boolean isGroupValid(String gid) {
        Group group = msgDao.groupNumberGet(gid);
//        Group group = msgDao.getGroup4Id(gid);
        if (group != null) {
            List<MemberUser> users = group.getUsers();
            if (users != null) {
                MemberUser member = MessageManager.getInstance().userToMember(UserAction.getMyInfo(), group.getGid());
                if (member != null && !users.contains(member)) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * 通知群变化
     * */
    public void notifyGroupChange(boolean isNeedLoad) {
        EventGroupChange event = new EventGroupChange();
        event.setNeedLoad(isNeedLoad);
        EventBus.getDefault().post(event);
    }

    //登出时需要清除缓存数据
    public void clearCache() {
        if (loadUids != null) {
            loadUids.clear();
        }
        if (loadGids != null) {
            loadGids.clear();
        }
        if (oldMsgId != null) {
            oldMsgId.clear();
        }
        if (taskMaps != null) {
            taskMaps.clear();
        }
    }

    public void doImgHeadChange(String gid, Group group) {
        int i = group.getUsers().size();
        i = i > 9 ? 9 : i;
        //头像地址
        String url[] = new String[i];
        for (int j = 0; j < i; j++) {
            MemberUser userInfo = group.getUsers().get(j);
            url[j] = userInfo.getHead();
        }
        File file = GroupHeadImageUtil.synthesis(AppConfig.getContext(), url);
        MsgDao msgDao = new MsgDao();
        msgDao.groupHeadImgUpdate(gid, file.getAbsolutePath());
    }

    /*
     * 群成员数据变化时，更新群信息
     * */
    public synchronized void refreshGroupInfo(final String gid) {
        new MsgAction().loadGroupMember(gid, new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                super.onResponse(call, response);
                notifyGroupChange(false);
                MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, gid, CoreEnum.ESessionRefreshTag.SINGLE, null);
            }

            @Override
            public void onFailure(Call<ReturnBean<Group>> call, Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    public void notifyRefreshUser(UserInfo info) {
        EventRefreshUser eventRefreshUser = new EventRefreshUser();
        eventRefreshUser.setInfo(info);
        EventBus.getDefault().post(eventRefreshUser);
    }

    public void removeMsgTask(String requestId) {
        System.out.println(TAG + "--MsgTask--remove--requestId=" + requestId);
        taskMaps.remove(requestId);
    }

    public TaskDealWithMsgList getMsgTask(String requestId) {
        return taskMaps.get(requestId);
    }

    //通知支付结果
    public void notifyPayResult(MsgBean.PayResultMessage resultMessage) {
        if (resultMessage == null) {
            return;
        }
        PayResultEvent event = new PayResultEvent();
        MsgBean.PayResultMessage.PayResult result = resultMessage.getResult();
        event.setActionId(resultMessage.getActionId());
        event.setTradeId(resultMessage.getTradeId());
        event.setErrMsg(resultMessage.getErrorMsg());
        event.setResult(result.getNumber());
        EventBus.getDefault().post(event);
    }


}
