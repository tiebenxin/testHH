package com.yanlong.im.chat.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.jrmf360.rplib.JrmfRpClient;
import com.jrmf360.rplib.bean.TransAccountBean;
import com.jrmf360.rplib.utils.callback.TransAccountCallBack;
import com.jrmf360.tools.utils.ThreadUtil;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.GroupConfig;
import com.yanlong.im.chat.bean.ImageMessage;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.MsgConversionBean;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.bean.VoiceMessage;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.server.ChatServer;
import com.yanlong.im.chat.server.UpLoadService;
import com.yanlong.im.pay.action.PayAction;
import com.yanlong.im.pay.bean.SignatureBean;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.utils.DaoUtil;
import com.yanlong.im.utils.GroupHeadImageUtil;
import com.yanlong.im.utils.audio.AudioPlayManager;
import com.yanlong.im.utils.audio.IVoicePlayListener;
import com.yanlong.im.utils.socket.MsgBean;
import com.yanlong.im.utils.socket.SocketData;
import com.yanlong.im.utils.socket.SocketEvent;
import com.yanlong.im.utils.socket.SocketUtil;

import net.cb.cb.library.base.BasePresenter;
import net.cb.cb.library.base.DBOptionObserver;
import net.cb.cb.library.bean.EventExitChat;
import net.cb.cb.library.bean.EventFindHistory;
import net.cb.cb.library.bean.EventRefreshChat;
import net.cb.cb.library.bean.EventUserOnlineChange;
import net.cb.cb.library.bean.EventVoicePlay;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.DownloadUtil;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.RunUtils;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.UpFileAction;
import net.cb.cb.library.utils.UpFileUtil;
import net.cb.cb.library.view.AlertTouch;
import net.cb.cb.library.view.MsgEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Liszt
 * @date 2019/9/19
 * Description
 */
public class ChatPresenter extends BasePresenter<ChatModel, ChatView> implements SocketEvent {
    private final String TAG = "ChatActivity";
    public final static int MIN_TEXT = 1000;//
    //红包和转账
    public static final int REQ_RP = 9653;
    public static final int REQ_TRANS = 9653;

    private List<String> sendTexts;//文本分段发送
    private List<MsgAllBean> downloadList = new ArrayList<>();//下载列表
    private Map<String, MsgAllBean> uploadMap = new HashMap<>();//上传列表
    private List<MsgAllBean> uploadList = new ArrayList<>();//上传列表
    private Context context;
    private boolean isSendingHypertext;
    private int textPosition;
    private final PayAction payAction = new PayAction();
    private boolean needRefresh;

    public void init(Context con) {
        context = con;
    }

    /*
     * showSendObj
     * */
    public void loadAndSetData() {
        if (needRefresh) {
            needRefresh = false;
        }
        Observable<List<MsgAllBean>> observable = model.loadMessages();
        observable.subscribe(new DBOptionObserver<List<MsgAllBean>>() {
            @Override
            public void onOptionSuccess(List<MsgAllBean> list) {
                getView().setAndRefreshData(list);
            }
        });
    }

    public void checkLockMessage() {
        model.checkLockMessage();
    }

    public void registerIMListener() {
        SocketUtil.getSocketUtil().addEvent(this);
    }

    public void unregisterIMListener() {
        SocketUtil.getSocketUtil().removeEvent(this);
    }

    @Override
    protected void onViewDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onViewStart() {
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void taskRefreshMessageEvent(EventRefreshChat event) {
        loadAndSetData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefresh(EventExitChat event) {
        ((Activity) context).onBackPressed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefresh(EventUserOnlineChange event) {
        getView().updateOnlineStatus();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventCheckVoice(EventVoicePlay event) {
        checkMoreVoice(event.getPosition(), (MsgAllBean) event.getBean());
    }


    /***
     * 查询历史
     * @param history
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void taskFinadHistoryMessage(EventFindHistory history) {
        Observable<List<MsgAllBean>> observable = model.loadHistoryMessages(history.getStime());
        observable.subscribe(new DBOptionObserver<List<MsgAllBean>>() {
            @Override
            public void onOptionSuccess(List<MsgAllBean> list) {
                getView().bindData(list);
                getView().scrollToPositionWithOff(0, 0);
            }
        });
    }

    @Override
    public void onHeartbeat() {

    }

    @Override
    public void onACK(MsgBean.AckMessage bean) {
        fixSendTime(bean.getMsgId(0));
        if (bean.getRejectType() == MsgBean.RejectType.NOT_FRIENDS_OR_GROUP_MEMBER || bean.getRejectType() == MsgBean.RejectType.IN_BLACKLIST) {
            loadAndSetData();
        } else {
            if (UpLoadService.getProgress(bean.getMsgId(0)) == null /*|| UpLoadService.getProgress(bean.getMsgId(0)) == 100*/) {//忽略图片上传的刷新,图片上传成功后
                for (String msgId : bean.getMsgIdList()) {
                    //撤回消息不做刷新
                    if (ChatServer.getCancelList().containsKey(msgId)) {
                        LogUtil.getLog().i(TAG, "onACK: 收到取消回执,等待刷新列表2");
                        return;
                    }
                }
            }
            loadAndSetData();
        }
        if (isSendingHypertext) {
            if (sendTexts != null && sendTexts.size() > 0 && textPosition != sendTexts.size() - 1) {
                sendHypertext(sendTexts, textPosition + 1);
            }
        }
    }

    @Override
    public void onMsg(MsgBean.UniversalMessage bean) {
        needRefresh = false;
        for (MsgBean.UniversalMessage.WrapMessage msg : bean.getWrapMsgList()) {
            //8.7 是属于这个会话就刷新
            if (!needRefresh) {
                if (model.isGroup()) {
                    needRefresh = msg.getGid().equals(model.getGid());
                } else {
                    needRefresh = msg.getFromUid() == model.getUid();
                }

                if (msg.getMsgType() == MsgBean.MessageType.OUT_GROUP) {//提出群的消息是以个人形式发的
                    needRefresh = msg.getOutGroup().getGid().equals(model.getGid());
                }
                if (msg.getMsgType() == MsgBean.MessageType.REMOVE_GROUP_MEMBER) {//提出群的消息是以个人形式发的
                    needRefresh = msg.getRemoveGroupMember().getGid().equals(model.getGid());
                }
            }
            onMsgBranch(msg);
        }
        //从数据库读取消息
        if (needRefresh) {
            loadAndSetData();
        }
        ThreadUtil.getInstance().runMainThread(new Runnable() {
            @Override
            public void run() {
                initUnreadCount();
            }
        });
    }

    @Override
    public void onSendMsgFailure(MsgBean.UniversalMessage.Builder bean) {
        //撤回处理
        if (bean.getWrapMsg(0).getMsgType() == MsgBean.MessageType.CANCEL) {
            ToastUtil.show(context, "撤回失败");
            return;
        }
        MsgAllBean msgAllBean = MsgConversionBean.ToBean(bean.getWrapMsg(0), bean, true);
        if (msgAllBean.getMsg_type().intValue() == ChatEnum.EMessageType.MSG_CANCEL) {//取消的指令不保存到数据库
            return;
        }
        msgAllBean.setSend_state(ChatEnum.ESendStatus.ERROR);
        ///这里写库
        msgAllBean.setSend_data(bean.build().toByteArray());
        DaoUtil.update(msgAllBean);
        loadAndSetData();
    }

    @Override
    public void onLine(boolean state) {

    }

    public void initUnreadCount() {
        getView().initUnreadCount(model.getUnreadCount());
    }

    //重新发送消息
    private void resendMessage(MsgAllBean msgBean) {
        //从数据拉出来,然后再发送
        MsgAllBean reMsg = DaoUtil.findOne(MsgAllBean.class, "msg_id", msgBean.getMsg_id());

        try {
            LogUtil.getLog().d(TAG, "点击重复发送" + reMsg.getMsg_id() + "--" + reMsg.getTimestamp());
            if (reMsg.getMsg_type() == ChatEnum.EMessageType.IMAGE) {//图片重发处理7.31
                String file = reMsg.getImage().getLocalimg();
                if (!TextUtils.isEmpty(file)) {
                    boolean isArtworkMaster = StringUtil.isNotNull(reMsg.getImage().getOrigin()) ? true : false;
                    ImageMessage image = SocketData.createImageMessage(reMsg.getMsg_id(), file, isArtworkMaster);
                    MsgAllBean imgMsgBean = SocketData.sendFileUploadMessagePre(reMsg.getMsg_id(), model.getUid(), model.getGid(), reMsg.getTimestamp(), image, ChatEnum.EMessageType.IMAGE);
                    getView().replaceListDataAndNotify(imgMsgBean);
                    getView().startUploadServer(reMsg, file, isArtworkMaster);
                } else {
                    //点击发送的时候如果要改变成发送中的状态
                    reMsg.setSend_state(ChatEnum.ESendStatus.SENDING);
                    DaoUtil.update(reMsg);
                    MsgBean.UniversalMessage.Builder bean = MsgBean.UniversalMessage.parseFrom(reMsg.getSend_data()).toBuilder();
                    SocketUtil.getSocketUtil().sendData4Msg(bean);
                    loadAndSetData();
                }
            } else if (reMsg.getMsg_type() == ChatEnum.EMessageType.VOICE) {
                String url = reMsg.getVoiceMessage().getLocalUrl();
                if (!TextUtils.isEmpty(url)) {
                    reMsg.setSend_state(ChatEnum.ESendStatus.PRE_SEND);
                    getView().replaceListDataAndNotify(reMsg);
                    uploadVoice(url, reMsg);
                } else {
                    //点击发送的时候如果要改变成发送中的状态
                    reMsg.setSend_state(ChatEnum.ESendStatus.SENDING);
                    DaoUtil.update(reMsg);
                    MsgBean.UniversalMessage.Builder bean = MsgBean.UniversalMessage.parseFrom(reMsg.getSend_data()).toBuilder();
                    SocketUtil.getSocketUtil().sendData4Msg(bean);
                    getView().replaceListDataAndNotify(reMsg);
                }
            } else {
                //点击发送的时候如果要改变成发送中的状态
                reMsg.setSend_state(ChatEnum.ESendStatus.SENDING);
                DaoUtil.update(reMsg);
                MsgBean.UniversalMessage.Builder bean = MsgBean.UniversalMessage.parseFrom(reMsg.getSend_data()).toBuilder();
                SocketUtil.getSocketUtil().sendData4Msg(bean);
                loadAndSetData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void uploadVoice(String file, final MsgAllBean bean) {
        uploadMap.put(bean.getMsg_id(), bean);
        uploadList.add(bean);
        updateSendStatus(ChatEnum.ESendStatus.SENDING, bean);
        new UpFileAction().upFile(UpFileAction.PATH.VOICE, context, new UpFileUtil.OssUpCallback() {
            @Override
            public void success(String url) {
                LogUtil.getLog().e(ChatActivity3.class.getSimpleName(), "上传语音成功--" + url);
                VoiceMessage voice = bean.getVoiceMessage();
                voice.setUrl(url);
                SocketData.sendAndSaveMessage(bean);
            }

            @Override
            public void fail() {
                updateSendStatus(ChatEnum.ESendStatus.ERROR, bean);
            }

            @Override
            public void inProgress(long progress, long zong) {
            }
        }, file);
    }

    private void updateSendStatus(@ChatEnum.ESendStatus int status, MsgAllBean bean) {
        bean.setSend_state(status);
        model.updateSendStatus(bean.getMsg_id(), status);
        getView().replaceListDataAndNotify(bean);
    }

    public void doSendText(MsgEditText edtChat, boolean isGroup,int survivalTime) {
        String txt = edtChat.getText().toString();
        if (txt.startsWith("@000")) {
            int count = Integer.parseInt(txt.split("_")[1]);
            taskTestSend(count);
            return;
        }
        //  }

        if (isGroup && edtChat.getUserIdList() != null && edtChat.getUserIdList().size() > 0) {
            String text = edtChat.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                int totalSize = text.length();
                if (totalSize > MIN_TEXT) {
                    ToastUtil.show(context, "@消息长度不能超过" + MIN_TEXT);
                    edtChat.getText().clear();
                    return;
                }
            }
            if (edtChat.isAtAll()) {
                MsgAllBean msgAllbean = SocketData.send4At(model.getUid(), model.getGid(), text, 1, edtChat.getUserIdList());
//                showSendObj(msgAllbean);
                loadAndSetData();
                edtChat.getText().clear();
            } else {
                MsgAllBean msgAllbean = SocketData.send4At(model.getUid(), model.getGid(), text, 0, edtChat.getUserIdList());
//                showSendObj(msgAllbean);
                loadAndSetData();
                edtChat.getText().clear();
            }
        } else {
            //发送普通消息
            String text = edtChat.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                int totalSize = text.length();
                int per = totalSize / MIN_TEXT;
                if (per > 10) {
                    ToastUtil.show(context, "文本长度不能超过" + 10 * MIN_TEXT);
                    edtChat.getText().clear();
                    return;
                }
                if (totalSize <= MIN_TEXT) {//非长文本
                    isSendingHypertext = false;
                    MsgAllBean msgAllbean = SocketData.send4Chat(model.getUid(), model.getGid(), text);
//                    showSendObj(msgAllbean);
                    loadAndSetData();
                    edtChat.getText().clear();
                } else {
                    isSendingHypertext = true;//正在分段发送长文本
                    if (totalSize > per * MIN_TEXT) {
                        per = per + 1;
                    }
                    sendTexts = new ArrayList<>();
                    for (int i = 0; i < per; i++) {
                        if (i < per - 1) {
                            sendTexts.add(StringUtil.splitEmojiString(text, i * MIN_TEXT, (i + 1) * MIN_TEXT));
                        } else {
                            sendTexts.add(StringUtil.splitEmojiString(text, i * MIN_TEXT, totalSize));
                        }
                    }
                    sendHypertext(sendTexts, 0);
                    edtChat.getText().clear();
                }
            }
        }
    }

    private void sendHypertext(List<String> list, int position) {
        if (position == list.size() - 1) {
            isSendingHypertext = false;
        }
        textPosition = position;
        SocketData.send4Chat(model.getUid(), model.getGid(), list.get(position));
        loadAndSetData();
//        MsgAllBean msgAllbean = SocketData.send4Chat(model.getUid(), model.getGid(), list.get(position));
//        showSendObj(msgAllbean);
    }

    private void taskTestSend(final int count) {
        ToastUtil.show(context, "连续发送" + count + "测试开始");
        new RunUtils(new RunUtils.Enent() {
            @Override
            public void onRun() {

                try {
                    for (int i = 1; i <= count; i++) {
                        if (i % 10 == 0)
                            SocketData.send4Chat(model.getUid(), model.getGid(), "连续测试发送" + i + "-------");
                        else
                            SocketData.send4Chat(model.getUid(), model.getGid(), "连续测试发送" + i);

                        if (i % 100 == 0)
                            Thread.sleep(2 * 1000);

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMain() {
                getView().notifyDataAndScrollBottom(false);
            }
        }).run();
    }

    /*
     * 发送红包
     * */
    void sendRb() {
        payAction.SignatureBean(new CallBack<ReturnBean<SignatureBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<SignatureBean>> call, Response<ReturnBean<SignatureBean>> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    SignatureBean sign = response.body().getData();
                    String token = sign.getSign();
                    UserInfo info = UserAction.getMyInfo();
                    if (model.isGroup()) {
                        Group group = model.getGroup();
                        JrmfRpClient.sendGroupEnvelopeForResult((Activity) context, "" + model.getGid(), "" + UserAction.getMyId(), token,
                                group.getUsers().size(), info.getName(), info.getHead(), REQ_RP);
                    } else {
                        JrmfRpClient.sendSingleEnvelopeForResult((Activity) context, "" + model.getUid(), "" + info.getUid(), token,
                                info.getName(), info.getHead(), REQ_RP);
                    }
                }
            }
        });
    }

    /***
     * 转账
     */
    public void doTrans() {
        payAction.SignatureBean(new CallBack<ReturnBean<SignatureBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<SignatureBean>> call, Response<ReturnBean<SignatureBean>> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    SignatureBean sign = response.body().getData();
                    String token = sign.getSign();
                    UserInfo otherInfo = model.getUserInfo();
                    UserInfo mInfo = UserAction.getMyInfo();
                    JrmfRpClient.transAccount((Activity) context, "" + otherInfo.getUid(), "" + mInfo.getUid(), token,
                            mInfo.getName(), mInfo.getHead(), otherInfo.getName4Show(), otherInfo.getHead(), new TransAccountCallBack() {
                                @Override
                                public void transResult(TransAccountBean transAccountBean) {
                                    String rid = transAccountBean.getTransferOrder();
                                    String info = transAccountBean.getTransferDesc();
                                    String money = transAccountBean.getTransferAmount();
                                    //设置转账消息
                                    MsgAllBean msgAllbean = SocketData.send4Trans(model.getUid(), rid, info, money);
//                                    showSendObj(msgAllbean);
                                    loadAndSetData();

                                }
                            });
                }
            }
        });
    }

    //戳一下
    public void doStamp(int survivalTime) {
        AlertTouch alertTouch = new AlertTouch();
        alertTouch.init((Activity) context, "请输入戳一下消息", "确定", R.mipmap.ic_chat_actionme, new AlertTouch.Event() {
            @Override
            public void onON() {

            }

            @Override
            public void onYes(String content) {
                if (!TextUtils.isEmpty(content)) {
                    //发送普通消息
                    MsgAllBean msgAllbean = SocketData.send4action(model.getUid(), model.getGid(), content);
//                    showSendObj(msgAllbean);
                    loadAndSetData();
                } else {
                    ToastUtil.show(context, "留言不能为空");
                }
            }
        });
        alertTouch.show();
        alertTouch.setEdHintOrSize(null, 15);
    }


    public void loadAndSetMoreData() {
        final int position = model.getTotalSize();
        Observable<List<MsgAllBean>> observable = model.loadMoreMessages();
        observable.subscribe(new DBOptionObserver<List<MsgAllBean>>() {
            @Override
            public void onOptionSuccess(List<MsgAllBean> list) {
                getView().bindData(list);
                getView().scrollToPositionWithOff(list.size() - position, DensityUtil.dip2px(context, 20f));
            }
        });
    }

    public void setAndClearDraft() {
        Session session = model.getSession();
        if (session == null) {
            return;
        }
        if (!TextUtils.isEmpty(session.getDraft())) {
            getView().setDraft(session.getDraft());
            model.updateDraft("");
        }

    }

    private void fixSendTime(String msgId) {
        MsgAllBean bean = uploadMap.get(msgId);
        boolean needRefresh = false;
        if (bean != null) {
            if (uploadList.indexOf(bean) == 0) {
                needRefresh = true;
            }
            uploadMap.remove(msgId);
        }
        if (needRefresh && uploadMap.size() > 0) {
            for (Map.Entry<String, MsgAllBean> entry : uploadMap.entrySet()) {
                MsgAllBean msg = entry.getValue();
                msg.setTimestamp(SocketData.getFixTime());
                DaoUtil.update(msg);
            }
        }
    }

    //消息的分发
    public void onMsgBranch(MsgBean.UniversalMessage.WrapMessage msg) {
        switch (msg.getMsgType()) {

            case DESTROY_GROUP:
                // ToastUtil.show(getApplicationContext(), "销毁群");
                taskGroupConf();
            case REMOVE_GROUP_MEMBER://退出群
                taskGroupConf();
                break;
            case ACCEPT_BE_GROUP://邀请进群刷新
                if (model.getGroup() == null) {
                    return;
                }
                if (StringUtil.isNotNull(model.getGroup().getAvatar())) {
                    taskGroupConf();
                } else {
                    if (model.getGroup().getUsers().size() >= 9) {
                        taskGroupConf();
                    } else {
                        taskGroupConf();
                        createAndSaveImg(model.getGid());
                    }
                }
                break;
//            case OTHER_REMOVE_GROUP:
//                createAndSaveImg(model.getGid());
//                break;
            case CHANGE_GROUP_META:
                getView().initTitle();
                break;
        }

    }

    private void createAndSaveImg(String gid) {
        Group group = model.getGroup();
        int i = group.getUsers().size();
        i = i > 9 ? 9 : i;
        //头像地址
        String url[] = new String[i];
        for (int j = 0; j < i; j++) {
            MemberUser userInfo = group.getUsers().get(j);
//            if (j == i - 1) {
//                name += userInfo.getName();
//            } else {
//                name += userInfo.getName() + "、";
//            }
            url[j] = userInfo.getHead();
        }
        File file = GroupHeadImageUtil.synthesis(context, url);
        MsgDao msgDao = new MsgDao();
        msgDao.groupHeadImgCreate(group.getGid(), file.getAbsolutePath());
    }

    /***
     * 获取群配置,并显示更多按钮
     */
    void taskGroupConf() {
        if (!model.isGroup()) {
            return;
        }
        GroupConfig config = model.getGroupConfig();
        if (config != null) {
            boolean isExited;
            if (config.getIsExit() == 1) {
                isExited = true;
            } else {
                isExited = false;
            }
            getView().setBanView(isExited);
        }
        taskGroupInfo();
    }

    public void taskGroupInfo() {
        new MsgAction().groupInfo(model.getGid(), new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                if (response.body() == null)
                    return;

                Group groupInfo = response.body().getData();

                if (groupInfo == null) {//取不到群信息了
                    groupInfo = new Group();
                    groupInfo.setMaster("");
                    groupInfo.setUsers(new RealmList<MemberUser>());
                }
                model.setGroup(groupInfo);

                if (groupInfo.getMaster().equals(UserAction.getMyId().toString())) {//本人群主
                    getView().setRobotView(true);
                } else {
                    getView().setRobotView(false);
                }

                //如果自己不在群里面
                boolean isExit = false;
                for (MemberUser uifo : groupInfo.getUsers()) {
                    if (uifo.getUid() == UserAction.getMyId().longValue()) {
                        isExit = true;
                    }
                }
                getView().setBanView(!isExit);
            }
        });
    }

    private void checkMoreVoice(int start, MsgAllBean b) {
//        LogUtil.getLog().i("AudioPlayManager", "checkMoreVoice--onCreate=" + onCreate);
        int length = model.getTotalSize();
        int index = model.getListData().indexOf(b);
        if (index < 0) {
            return;
        }
        if (index != start) {//修正一下起始位置
            start = index;
        }
        MsgAllBean message = null;
        int position = -1;
        if (start < length - 1) {
            for (int i = start + 1; i < length; i++) {
                MsgAllBean bean = model.getListData().get(i);
                if (bean.getMsg_type() == ChatEnum.EMessageType.VOICE && !bean.isMe() && !bean.isRead()) {
                    message = bean;
                    position = i;
                    break;
                }
            }
        }
//        MsgAllBean bean = msgDao.getNextVoiceMessage(toUId,toGid,b.getTimestamp(),UserAction.getMyInfo().getUid());
        if (message != null) {
            playVoice(message, true, position);
        }

    }

    private void playVoice(final MsgAllBean bean, final boolean canAutoPlay, final int position) {
//        LogUtil.getLog().i(TAG, "playVoice--" + position);
        VoiceMessage vm = bean.getVoiceMessage();
        if (vm == null || TextUtils.isEmpty(vm.getUrl())) {
            return;
        }
        String url = "";
        if (bean.isMe()) {
            url = vm.getLocalUrl();
        } else {
            url = vm.getUrl();
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (AudioPlayManager.getInstance().isPlay(Uri.parse(url))) {
            AudioPlayManager.getInstance().stopPlay();
        } else {
            if (!bean.isRead() && !bean.isMe()) {
                int len = downloadList.size();
                if (len > 0) {//有下载
                    MsgAllBean msg = downloadList.get(len - 1);
                    updatePlayStatus(msg, 0, ChatEnum.EPlayStatus.NO_PLAY);
                }
                downloadList.add(bean);

                updatePlayStatus(bean, position, ChatEnum.EPlayStatus.DOWNLOADING);
                AudioPlayManager.getInstance().downloadAudio(context, bean, new DownloadUtil.IDownloadVoiceListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        updatePlayStatus(bean, position, ChatEnum.EPlayStatus.NO_PLAY);
                        startPlayVoice(bean, canAutoPlay, position);

                    }

                    @Override
                    public void onDownloading(int progress) {

                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        updatePlayStatus(bean, position, ChatEnum.EPlayStatus.NO_DOWNLOADED);
                    }
                });
            } else {
                int len = downloadList.size();
                if (len > 0) {//有下载
                    MsgAllBean msg = downloadList.get(len - 1);
                    updatePlayStatus(msg, 0, ChatEnum.EPlayStatus.NO_PLAY);
                }
                startPlayVoice(bean, canAutoPlay, position);
            }
        }
    }

    private void updatePlayStatus(MsgAllBean bean, int position, @ChatEnum.EPlayStatus int status) {
//        LogUtil.getLog().i(TAG, "updatePlayStatus--" + status + "--position=" + position);
        bean = model.amendMsgALlBean(position, bean);
        VoiceMessage voiceMessage = bean.getVoiceMessage();
        if (status == ChatEnum.EPlayStatus.NO_PLAY || status == ChatEnum.EPlayStatus.PLAYING) {//已点击下载，或者正在播
            if (bean.isRead() == false) {
//                msgAction.msgRead(bean.getMsg_id(), true);
                model.updateReadStatus(bean.getMsg_id(), true);
                bean.setRead(true);
            }
        }
        model.updatePlayStatus(voiceMessage.getMsgId(), status);
        model.updateReadStatus(bean.getMsg_id(), true);

        voiceMessage.setPlayStatus(status);
        final MsgAllBean finalBean = bean;
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getView().replaceListDataAndNotify(finalBean);
            }
        });
    }

    private void startPlayVoice(MsgAllBean bean, boolean canAutoPlay, final int position) {
//        LogUtil.getLog().i(TAG, "startPlayVoice--" + "downSize =" + downloadList.size());

        if (downloadList.size() > 1) {
            int size = downloadList.size();
            int p = downloadList.indexOf(bean);
            if (p != size - 1) {
//                LogUtil.getLog().i(TAG, "startPlayVoice--终止下载位置=" + p);
                downloadList.remove(bean);
                return;
            }
        }
        downloadList.remove(bean);

        AudioPlayManager.getInstance().startPlay(context, bean, position, canAutoPlay, new IVoicePlayListener() {
            @Override
            public void onStart(MsgAllBean bean) {
                updatePlayStatus(bean, position, ChatEnum.EPlayStatus.PLAYING);
//                LogUtil.getLog().i("AudioPlayManager", "onStart--" + bean.getVoiceMessage().getUrl());
            }

            @Override
            public void onStop(MsgAllBean bean) {
                updatePlayStatus(bean, position, ChatEnum.EPlayStatus.STOP_PLAY);
//                LogUtil.getLog().i("AudioPlayManager", "onStop--" + bean.getVoiceMessage().getUrl());
            }

            @Override
            public void onComplete(MsgAllBean bean) {
                updatePlayStatus(bean, position, ChatEnum.EPlayStatus.PLAYED);
//                LogUtil.getLog().i("AudioPlayManager", "onComplete--" + bean.getVoiceMessage().getUrl());
            }
        });
    }
}
