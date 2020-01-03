package com.yanlong.im.utils.socket;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hm.cxpay.global.PayEnum;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.AssistantMessage;
import com.yanlong.im.chat.bean.AtMessage;
import com.yanlong.im.chat.bean.BusinessCardMessage;
import com.yanlong.im.chat.bean.ChatMessage;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.IMsgContent;
import com.yanlong.im.chat.bean.ImageMessage;
import com.yanlong.im.chat.bean.LocationMessage;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.MsgCancel;
import com.yanlong.im.chat.bean.MsgConversionBean;
import com.yanlong.im.chat.bean.MsgNotice;
import com.yanlong.im.chat.bean.RedEnvelopeMessage;
import com.yanlong.im.chat.bean.StampMessage;
import com.yanlong.im.chat.bean.TransferMessage;
import com.yanlong.im.chat.bean.VideoMessage;
import com.yanlong.im.chat.bean.VoiceMessage;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.chat.server.ChatServer;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.TokenBean;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.utils.DaoUtil;

import net.cb.cb.library.utils.GsonUtils;
import net.cb.cb.library.utils.ImgSizeUtil;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.StringUtil;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.RealmList;

public class SocketData {
    private static final String TAG = "SocketData";

    private static long preServerAckTime;//前一个服务器回执时间
    private static long preSendLocalTime;//前一个本地消息发送的时间


    private static MsgDao msgDao = new MsgDao();
    public static long CLL_ASSITANCE_ID = 1L;//常信小助手id


    /***
     * 处理一些统一的数据,用于发送消息时获取
     * @return
     */
    public static MsgBean.UniversalMessage.Builder getMsgBuild() {
        MsgBean.UniversalMessage.Builder msg = MsgBean.UniversalMessage.newBuilder();
        MsgBean.UniversalMessage.WrapMessage.Builder wp = MsgBean.UniversalMessage.WrapMessage.newBuilder();
        msg.setRequestId("" + getSysTime());
        msg.addWrapMsg(0, wp.build());

        return msg;
    }

    /***
     * 处理一些统一的数据,用于发送消息时获取
     * @return
     */
    public static MsgBean.UniversalMessage.Builder getMsgBuild(String requestId) {
        MsgBean.UniversalMessage.Builder msg = MsgBean.UniversalMessage.newBuilder();
        MsgBean.UniversalMessage.WrapMessage.Builder wp = MsgBean.UniversalMessage.WrapMessage.newBuilder();
        if (TextUtils.isEmpty(requestId)) {
            msg.setRequestId("" + getSysTime());
        } else {
            msg.setRequestId(requestId);
        }
        msg.addWrapMsg(0, wp.build());

        return msg;
    }

    /***
     * 授权
     * @return
     */
    public static byte[] msg4Auth() {

        TokenBean tokenBean = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.TOKEN).get4Json(TokenBean.class);
        //  tokenBean = new TokenBean();
        // tokenBean.setAccessToken("2N0qG3CHBxVNQfPjIbbCA/YUY48erDHVTBXZHK1JQAOfAxi86DKcvYKqLwxLfINN");
        LogUtil.getLog().i("tag", ">>>>发送token" + tokenBean.getAccessToken());

        if (tokenBean == null || !StringUtil.isNotNull(tokenBean.getAccessToken())) {
            return null;
        }


        MsgBean.AuthRequestMessage auth = MsgBean.AuthRequestMessage.newBuilder()
                .setAccessToken(tokenBean.getAccessToken()).build();

        return SocketPact.getPakage(SocketPact.DataType.AUTH, auth.toByteArray());

    }

    /***
     * 回执,可以不发送msgId
     * @return
     */
    public static byte[] msg4ACK(String rid, List<String> msgids) {

        MsgBean.AckMessage ack;
        MsgBean.AckMessage.Builder amsg = MsgBean.AckMessage.newBuilder().setRequestId(rid);
        if (msgids != null) {
            for (int i = 0; i < msgids.size(); i++) {
                amsg.addMsgId(msgids.get(i));
            }
        }
        ack = amsg.build();

        //添加到消息队中监听
        SendList.addSendList(ack.getRequestId(), amsg);

        return SocketPact.getPakage(SocketPact.DataType.ACK, ack.toByteArray());

    }
//------------------------收-----------------------------

    /***
     * 消息转换
     * @param data
     * @return
     */
    public static MsgBean.UniversalMessage msgConversion(byte[] data) {
        try {

            MsgBean.UniversalMessage msg = MsgBean.UniversalMessage.parseFrom(SocketPact.bytesToLists(data, 12).get(1));
            return msg;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * ack转换
     * @param data
     * @return
     */
    public static MsgBean.AckMessage ackConversion(byte[] data) {
        try {

            MsgBean.AckMessage msg = MsgBean.AckMessage.parseFrom(SocketPact.bytesToLists(data, 12).get(1));
            return msg;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 鉴权消息转换
     * @param data
     * @return
     */
    public static MsgBean.AuthResponseMessage authConversion(byte[] data) {
        try {
            MsgBean.AuthResponseMessage ruthmsg = MsgBean.AuthResponseMessage.parseFrom(SocketPact.bytesToLists(data, 12).get(1));


            return ruthmsg;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    //6.6 为后端擦屁股
    public static CopyOnWriteArrayList<String> oldMsgId = new CopyOnWriteArrayList<>();


    /***
     * 在服务器接收到自己发送的消息后,本地保存
     * @param bean
     */
    public static void msgSave4Me(MsgBean.AckMessage bean) {
        //普通消息
        MsgBean.UniversalMessage.Builder msg = SendList.findMsgById(bean.getRequestId());
        //6.25 排除通知存库
        //6.25 移除重发列队
        SendList.removeSendListJust(bean.getRequestId());

        if (msg != null && msgSendSave4filter(msg.getWrapMsg(0).toBuilder())) {
            //存库处理
            MsgBean.UniversalMessage.WrapMessage wmsg = msg.getWrapMsgBuilder(0)
                    .setMsgId(bean.getMsgIdList().get(0))
                    //时间要和ack一起返回
                    .setTimestamp(getSysTime())
                    .build();
            //  LogUtil.getLog().d(TAG, "msgSave4Me2: msg" + msg.toString());

            MsgAllBean msgAllBean = MsgConversionBean.ToBean(wmsg, msg, false);
            if (msgAllBean == null) {
                return;
            }
            msgAllBean.setRead(true);//自己发送的消息是已读
            msgAllBean.setMsg_id(msgAllBean.getMsg_id());
            msgAllBean.setTimestamp(bean.getTimestamp());

            //7.16 如果是收到先自己发图图片的消息

            //移除旧消息
            DaoUtil.deleteOne(MsgAllBean.class, "request_id", msgAllBean.getRequest_id());

            if (msgAllBean.getVideoMessage() != null) {
                msgAllBean.getVideoMessage().setLocalUrl(videoLocalUrl);
            }
            //收到直接存表,创建会话
            DaoUtil.update(msgAllBean);
            MsgDao msgDao = new MsgDao();

            msgDao.sessionCreate(msgAllBean.getGid(), msgAllBean.getTo_uid());
            MessageManager.getInstance().setMessageChange(true);

        }
    }

    //6.26 消息直接存库
    public static void msgSave4Me(MsgBean.UniversalMessage.Builder msg, int state) {
        //普通消息

        if (msg != null) {
            //存库处理
            MsgBean.UniversalMessage.WrapMessage wmsg = msg.getWrapMsgBuilder(0)
                    // .setMsgId(bean.getMsgIdList().get(0))
                    //时间要和ack一起返回
                    // .setTimestamp(System.currentTimeMillis())
                    .build();
            LogUtil.getLog().d(TAG, "msgSave4Me1: msg" + msg.toString());
            MsgAllBean msgAllBean = MsgConversionBean.ToBean(wmsg, msg, false);
            msgAllBean.setMsg_id(msgAllBean.getMsg_id());
            //时间戳
            // msgAllBean.setTimestamp(bean.getTimestamp());
            //是发送给群助手的消息直接发送成功
            if (isNoAssistant(msgAllBean.getTo_uid(), msgAllBean.getGid())) {
                msgAllBean.setSend_state(state);
            }
            msgAllBean.setSend_data(msg.build().toByteArray());

            //移除旧消息// 7.16 通过msgid 判断唯一
            DaoUtil.deleteOne(MsgAllBean.class, "request_id", msgAllBean.getRequest_id());
            // DaoUtil.deleteOne(MsgAllBean.class, "msg_id", msgAllBean.getMsg_id());
            if (msgAllBean.getVideoMessage() != null) {
                msgAllBean.getVideoMessage().setLocalUrl(videoLocalUrl);
            }
            // 撤消内容 与内容类型写入数据库
            if (msgAllBean.getMsgCancel() != null) {
                msgAllBean.getMsgCancel().setCancelContent(mCancelContent);
                msgAllBean.getMsgCancel().setCancelContentType(mCancelContentType);
            }
            //收到直接存表,创建会话
            DaoUtil.update(msgAllBean);
            MsgDao msgDao = new MsgDao();

            msgDao.sessionCreate(msgAllBean.getGid(), msgAllBean.getTo_uid());
            MessageManager.getInstance().setMessageChange(true);
        }
    }

    /***
     * 发送失败
     * @param bean
     * 发送失败的消息不更新时间
     */
    public static void msgSave4MeFail(MsgBean.AckMessage bean) {
        //普通消息
        MsgBean.UniversalMessage.Builder msg = SendList.findMsgById(bean.getRequestId());
        if (msg != null) {
            //存库处理
            MsgBean.UniversalMessage.WrapMessage wmsg = msg.getWrapMsgBuilder(0)
                    .setMsgId(bean.getMsgIdList().get(0))
                    //时间要和ack一起返回
//                    .setTimestamp(getSysTime())
                    .build();
            MsgAllBean msgAllBean = MsgConversionBean.ToBean(wmsg, msg, true);

            msgAllBean.setMsg_id(msgAllBean.getMsg_id());
            //时间戳
//            msgAllBean.setTimestamp(bean.getTimestamp());
            msgAllBean.setTimestamp(msg.getWrapMsg(0).getTimestamp());
            msgAllBean.setSend_state(ChatEnum.ESendStatus.ERROR);
            msgAllBean.setSend_data(msg.build().toByteArray());

            //移除旧消息
            DaoUtil.deleteOne(MsgAllBean.class, "request_id", msgAllBean.getRequest_id());

            //收到直接存表,创建会话
            DaoUtil.update(msgAllBean);
            MsgDao msgDao = new MsgDao();

            msgDao.sessionCreate(msgAllBean.getGid(), msgAllBean.getTo_uid());
            MessageManager.getInstance().setMessageChange(true);

            //移除重发列队
            SendList.removeSendListJust(bean.getRequestId());


        }
    }

    //5.27 发送前保存到库
    public static void msgSave4MeSendFront(MsgBean.UniversalMessage.Builder msg) {
        msgSave4Me(msg, 2);
    }


    /***
     * 保存并发送消息
     * @param toId
     * @param toGid
     * @param type
     * @param value
     * @return
     */
    private static MsgAllBean send4Base(Long toId, String toGid, MsgBean.MessageType type, Object value) {
        return send4Base(true, true, null, toId, toGid, -1, type, value);
    }

    /***
     * 保存并发送消息
     * @param toId
     * @param toGid
     * @param type
     * @param value
     * @return
     */
    private static MsgAllBean send4Base(boolean isSave, Long toId, String toGid, MsgBean.MessageType type, Object value) {
        return send4Base(isSave, true, null, toId, toGid, -1, type, value);
    }

    /***
     * 根据消息id保存发送数据
     * @param msgId
     * @param toId
     * @param toGid
     * @param type
     * @param value
     * @return
     */
    private static MsgAllBean send4BaseById(String msgId, Long toId, String toGid, long time, MsgBean.MessageType type, Object value) {
        return send4Base(true, true, msgId, toId, toGid, time, type, value);
    }

    /***
     * 只保存消息,不缓存
     * @param msgId
     * @param toId
     * @param toGid
     * @param type
     * @param value
     * @return
     */
    private static MsgAllBean send4BaseJustSave(String msgId, Long toId, String toGid, MsgBean.MessageType type, Object value) {
        return send4Base(true, false, msgId, toId, toGid, -1L, type, value);
    }

    /*
     * @time time > 0
     * */
    private static MsgAllBean send4Base(boolean isSave, boolean isSend, String msgId, Long toId, String toGid, long time, MsgBean.MessageType type, Object value) {
        LogUtil.getLog().i(TAG, ">>>---发送到toid" + toId + "--gid" + toGid);
        MsgBean.UniversalMessage.Builder msg = toMsgBuilder("", msgId, toId, toGid, time > 0 ? time : getFixTime(), type, value);

        if (isSave && msgSendSave4filter(msg.getWrapMsg(0).toBuilder())) {
            msgSave4MeSendFront(msg); //5.27 发送前先保存到库,
        }
        //立即发送
        if (isSend && isNoAssistant(toId, toGid)) {
            SocketUtil.getSocketUtil().sendData4Msg(msg);
        }
        MsgAllBean msgAllbean = MsgConversionBean.ToBean(msg.getWrapMsg(0));
        return msgAllbean;
    }

    //不是常信小助手id
    public static boolean isNoAssistant(Long uid, String gid) {
        if (TextUtils.isEmpty(gid) && (uid != null && uid == CLL_ASSITANCE_ID)) {
            return false;
        }
        return true;
    }


    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /***
     * 6.26消息构建
     * @param toId
     * @param toGid
     * @param type
     * @param value
     * @return
     */
    private static MsgBean.UniversalMessage.Builder toMsgBuilder(String requestId, String msgid, Long toId, String toGid, long time, MsgBean.MessageType type, Object value) {
        MsgBean.UniversalMessage.Builder msg = SocketData.getMsgBuild(requestId);
        MsgBean.UniversalMessage.WrapMessage.Builder wmsg = msg.getWrapMsgBuilder(0);
        UserInfo userInfo = UserAction.getMyInfo();
        wmsg.setFromUid(userInfo.getUid());
        wmsg.setAvatar(userInfo.getHead());
        wmsg.setNickname(userInfo.getName());
        //自动生成uuid
        wmsg.setMsgId(msgid == null ? getUUID() : msgid);

        //添加阅后即焚状态
        int survivalTime = new UserDao().getReadDestroy(toId, toGid);
        LogUtil.getLog().i("SurvivalTime", "消息构建: 阅后即焚状态---->" + survivalTime + "------");

        wmsg.setSurvivalTime(survivalTime);

        wmsg.setTimestamp(time);
        if (toId != null && toId > 0) {//给个人发
            msg.setToUid(toId);
        }
        if (toGid != null && toGid.length() > 0) {//给群发
            wmsg.setGid(toGid);
            Group group = msgDao.getGroup4Id(toGid);
            if (group != null) {
                String name = group.getMygroupName();
                if (StringUtil.isNotNull(name)) {
                    wmsg.setMembername(name);
                }
            }

        }

        wmsg.setMsgType(type);
        switch (type) {
            case CHAT:
                wmsg.setChat((MsgBean.ChatMessage) value);
                break;
            case IMAGE:
                wmsg.setImage((MsgBean.ImageMessage) value);
                break;
            case RED_ENVELOPER:
                wmsg.setRedEnvelope((MsgBean.RedEnvelopeMessage) value);
                break;
            case RECEIVE_RED_ENVELOPER:
                wmsg.setReceiveRedEnvelope((MsgBean.ReceiveRedEnvelopeMessage) value);
                break;
            case TRANSFER:
                wmsg.setTransfer((MsgBean.TransferMessage) value);
                break;
            case STAMP:
                wmsg.setStamp((MsgBean.StampMessage) value);
                break;
            case BUSINESS_CARD:
                wmsg.setBusinessCard((MsgBean.BusinessCardMessage) value);
                break;
            case ACCEPT_BE_FRIENDS:
                wmsg.setAcceptBeFriends((MsgBean.AcceptBeFriendsMessage) value);
                break;
            case REQUEST_FRIEND:
                wmsg.setRequestFriend((MsgBean.RequestFriendMessage) value);
                break;
            case VOICE:
                wmsg.setVoice((MsgBean.VoiceMessage) value);
                break;
            case AT:
                wmsg.setAt((MsgBean.AtMessage) value);
                break;
            case CANCEL:
                wmsg.setCancel((MsgBean.CancelMessage) value);
                break;
            case SHORT_VIDEO:
                wmsg.setShortVideo((MsgBean.ShortVideoMessage) value);
                break;
            case P2P_AU_VIDEO:
                wmsg.setP2PAuVideo((MsgBean.P2PAuVideoMessage) value);
                break;
            case P2P_AU_VIDEO_DIAL:
                wmsg.setP2PAuVideoDial((MsgBean.P2PAuVideoDialMessage) value);
                break;
            case READ:
                wmsg.setRead((MsgBean.ReadMessage) value);
                break;
            case SNAPSHOT_LOCATION://位置
                wmsg.setSnapshotLocation((MsgBean.SnapshotLocationMessage) value);
                break;
            case UNRECOGNIZED:
                break;

        }
        MsgBean.UniversalMessage.WrapMessage wm = wmsg.build();
        msg.setWrapMsg(0, wm);
        return msg;
    }

    /***
     * 忽略存库的消息
     * @return false 需要忽略
     */
    private static boolean msgSendSave4filter(MsgBean.UniversalMessage.WrapMessage.Builder wmsg) {
        if (wmsg.getMsgType() == MsgBean.MessageType.RECEIVE_RED_ENVELOPER || wmsg.getMsgType() == MsgBean.MessageType.P2P_AU_VIDEO_DIAL) {
            return false;
        }
        return true;

    }

    /***
     * 普通消息
     * @param toId
     * @param txt
     * @return
     */
    public static MsgAllBean send4Chat(Long toId, String toGid, String txt) {
        MsgBean.ChatMessage chat = MsgBean.ChatMessage.newBuilder()
                .setMsg(txt)
                .build();
        return send4Base(toId, toGid, MsgBean.MessageType.CHAT, chat);

    }

    /**
     * 阅后即焚消息
     */
    public static MsgAllBean send4ChatSurvivalTime(Long toId, String toGid, String txt, int survivalTime) {
        MsgBean.ChatMessage chat = MsgBean.ChatMessage.newBuilder()
                .setMsg(txt)
                .build();
        MsgBean.UniversalMessage.WrapMessage wrapMessage = MsgBean.UniversalMessage.WrapMessage.newBuilder()
                .setSurvivalTime(survivalTime)
                .setChat(chat)
                .build();
        return send4Base(toId, toGid, MsgBean.MessageType.CHAT, wrapMessage);

    }

    /**
     * 发送一条音视频消息
     *
     * @param toId
     * @param toGid
     * @param txt         操作加时长
     * @param auVideoType 语音、视频
     * @param operation   操作
     * @return
     */
    public static MsgAllBean send4VoiceOrVideo(Long toId, String toGid, String txt, MsgBean.AuVideoType auVideoType, String operation) {
        MsgBean.P2PAuVideoMessage chat = MsgBean.P2PAuVideoMessage.newBuilder()
                .setAvType(auVideoType)
                .setOperation(operation)
                .setDesc(txt)
                .build();

        return send4Base(toId, toGid, MsgBean.MessageType.P2P_AU_VIDEO, chat);

    }

    /**
     * 发送一条音视频通知
     *
     * @param toId
     * @param toGid
     * @param auVideoType 语音、视频
     * @return
     */
    public static MsgAllBean send4VoiceOrVideoNotice(Long toId, String toGid, MsgBean.AuVideoType auVideoType) {
        MsgBean.P2PAuVideoDialMessage chat = MsgBean.P2PAuVideoDialMessage.newBuilder()
                .setAvType(auVideoType)
                .build();

        return send4Base(false, toId, toGid, MsgBean.MessageType.P2P_AU_VIDEO_DIAL, chat);

    }


    /**
     * @param toId
     * @param txt
     * @param atType 0. @多个 1. @所有人
     * @param list   @用户集合
     * @return
     * @消息
     */
    public static MsgAllBean send4At(Long toId, String toGid, String txt, int atType, List<Long> list) {
        MsgBean.AtMessage atMessage = MsgBean.AtMessage.newBuilder()
                .setMsg(txt)
                .setAtTypeValue(atType)
                .addAllUid(list)
                .build();
        return send4Base(toId, toGid, MsgBean.MessageType.AT, atMessage);
    }


    /**
     * 戳一戳消息
     *
     * @param toId
     * @param toGid
     * @param txt
     * @return
     */
    public static MsgAllBean send4action(Long toId, String toGid, String txt) {
        MsgBean.StampMessage action = MsgBean.StampMessage.newBuilder()
                .setComment(txt)
                .build();

        return send4Base(toId, toGid, MsgBean.MessageType.STAMP, action);

    }

    /***
     * 发送图片
     * @param toId
     * @param toGid
     * @param url
     * @return
     */
    public static MsgAllBean send4Image(String msgId, Long toId, String toGid, String url, boolean isOriginal, ImgSizeUtil.ImageSize imageSize, long time) {
        MsgBean.ImageMessage.Builder msg;
        String extTh = "/below-20k";
        String extPv = "/below-200k";
        if (url.toLowerCase().contains(".gif")) {
            extTh = "";
            extPv = "";
        }
        if (isOriginal) {
            msg = MsgBean.ImageMessage.newBuilder()
                    .setOrigin(url)
                    .setPreview(url + extPv)
                    .setThumbnail(url + extTh);

        } else {
            msg = MsgBean.ImageMessage.newBuilder()
                    .setPreview(url)
                    .setThumbnail(url + extTh);

        }
        MsgBean.ImageMessage msgb;
        if (imageSize != null) {
            msgb = msg.setWidth(imageSize.getWidth())
                    .setHeight(imageSize.getHeight())
                    .setSize(new Long(imageSize.getSize()).intValue())
                    .build();
        } else {
            msgb = msg.build();
        }


        return send4BaseById(msgId, toId, toGid, time, MsgBean.MessageType.IMAGE, msgb);
    }

//    /***
//     * 发送视频
//     * @param toId
//     * @param toGid
//     * @param videoMessage
//     * @return
//     */
//    public static MsgAllBean 发送视频整体信息(Long toId, String toGid, VideoMessage videoMessage) {
//        String bg_URL = videoMessage.getBg_url();
//        long time = videoMessage.getDuration();
//        String url = videoMessage.getUrl();
//        long width = videoMessage.getWidth();
//        long height = videoMessage.getHeight();
//        String msgId = videoMessage.getMsgId();
//
//
//        MsgBean.ShortVideoMessage msg;
//        msg = MsgBean.ShortVideoMessage.newBuilder().setBgUrl(bg_URL).setDuration((int) time).setUrl(url).setWidth((int) width).setHeight((int) height).build();
//        return send4BaseById(msgId, toId, toGid, time, MsgBean.MessageType.SHORT_VIDEO, msg);
//    }
//
//    public static MsgAllBean 转发送视频整体信息(Long toId, String toGid, VideoMessage videoMessage) {
//        String bg_URL = videoMessage.getBg_url();
//        long time = videoMessage.getDuration();
//        String url = videoMessage.getUrl();
//        long width = videoMessage.getWidth();
//        long height = videoMessage.getHeight();
//        String msgId = videoMessage.getMsgId();
//
//
//        MsgBean.ShortVideoMessage msg;
//        msg = MsgBean.ShortVideoMessage.newBuilder().setBgUrl(bg_URL).setDuration((int) time).setUrl(url).setWidth((int) width).setHeight((int) height).build();
//        return send4Base(toId, toGid, MsgBean.MessageType.SHORT_VIDEO, msg);
//    }

    /***
     * 发送视频
     * @param toId
     * @param toGid
     * @param url
     * @return
     */
    private static String videoLocalUrl = null;

    public static MsgAllBean sendVideo(String msgId, Long toId, String toGid, String url, String bg_URL, boolean isOriginal, long time, int width, int height, String videoLocalPath) {
        MsgBean.ShortVideoMessage msg;
        videoLocalUrl = videoLocalPath;
//        String extTh = "/below-20k";
//        String extPv = "/below-200k";
//        if (url.toLowerCase().contains(".gif")) {
//            extTh = "";
//            extPv = "";
//        }
//        if (isOriginal) {
//            msg = MsgBean.ShortVideoMessage.newBuilder().set
//                    .setOrigin(url)
//                    .setPreview(url + extPv)
//                    .setThumbnail(url + extTh);
//
//        } else {
//            msg = MsgBean.ShortVideoMessage.newBuilder()
//                    .setPreview(url)
//                    .setThumbnail(url + extTh);
//
//        }
//        MsgBean.ImageMessage msgb;
//        if (imageSize != null) {
//            msgb = msg.setWidth((int)imageSize.getWidth())
//                    .setHeight((int)imageSize.getHeight())
//                    .setSize((int)size)
//                    .build();
//        } else {
//            msgb = msg.build();
//        }

        msg = MsgBean.ShortVideoMessage.newBuilder().setBgUrl(bg_URL).setDuration((int) time).setUrl(url).setWidth(width).setHeight(height).build();
        return send4BaseById(msgId, toId, toGid, -1, MsgBean.MessageType.SHORT_VIDEO, msg);
    }

    /**
     * 转发送视频信息
     *
     * @param msgId
     * @param toId
     * @param toGid
     * @param url
     * @param bg_URL
     * @param isOriginal
     * @param time
     * @param width
     * @param height
     * @return
     */
    public static MsgAllBean forwardingVideo(String msgId, Long toId, String toGid, String url, String bg_URL, boolean isOriginal, long time, int width, int height) {
        MsgBean.ShortVideoMessage msg;
        msg = MsgBean.ShortVideoMessage.newBuilder().setBgUrl(bg_URL).setDuration((int) time).setUrl(url).setWidth(width).setHeight(height).build();
        return send4Base(toId, toGid, MsgBean.MessageType.SHORT_VIDEO, msg);
    }

    /***
     * 转发处理
     * @param toId
     * @param toGid
     * @param url
     * @param url1
     * @param url2
     * @return
     */
    public static MsgAllBean send4Image(Long toId, String toGid, String url, String url1, String url2, int w, int h, int size) {
        MsgBean.ImageMessage msg = MsgBean.ImageMessage.newBuilder()
                .setOrigin(url)
                .setPreview(url1)
                .setThumbnail(url2)
                .setWidth(w)
                .setHeight(h)
                .setSize(size)
                .build();


        return send4Base(toId, toGid, MsgBean.MessageType.IMAGE, msg);
    }

    public static MsgAllBean send4Image(Long toId, String toGid, String url, ImgSizeUtil.ImageSize imgSize, long time) {

        return send4Image(getUUID(), toId, toGid, url, false, imgSize, time);
    }


    //预发送需文件（图片，语音）上传消息,保存消息及更新session
    public static <T> MsgAllBean sendFileUploadMessagePre(String msgId, Long toId, String toGid, long time, T t, @ChatEnum.EMessageType int type) {
        //前保存
        MsgAllBean msgAllBean = new MsgAllBean();
        msgAllBean.setMsg_id(msgId);
        UserInfo myinfo = UserAction.getMyInfo();
        msgAllBean.setFrom_uid(myinfo.getUid());
        msgAllBean.setFrom_avatar(myinfo.getHead());
        msgAllBean.setFrom_nickname(myinfo.getName());
        msgAllBean.setRequest_id(getSysTime() + "");
        msgAllBean.setTimestamp(time);
        msgAllBean.setMsg_type(type);

        int survivaltime = new UserDao().getReadDestroy(toId, toGid);
        msgAllBean.setSurvival_time(survivaltime);

        msgAllBean.setRead(true);//自己发送时已读的
        switch (type) {
            case ChatEnum.EMessageType.IMAGE:
                ImageMessage image = (ImageMessage) t;
                msgAllBean.setImage(image);
                break;
            case ChatEnum.EMessageType.VOICE:
                VoiceMessage voice = (VoiceMessage) t;
                msgAllBean.setVoiceMessage(voice);
                break;
            case ChatEnum.EMessageType.MSG_VIDEO:
                VideoMessage video = (VideoMessage) t;
                msgAllBean.setVideoMessage(video);
                break;
        }

        msgAllBean.setTo_uid(toId);
        msgAllBean.setGid(toGid == null ? "" : toGid);
        msgAllBean.setSend_state(ChatEnum.ESendStatus.PRE_SEND);

        LogUtil.getLog().d(TAG, "sendFileUploadMessagePre: msgId" + msgId);

        DaoUtil.update(msgAllBean);
        msgDao.sessionCreate(msgAllBean.getGid(), msgAllBean.getTo_uid());
        MessageManager.getInstance().setMessageChange(true);
        return msgAllBean;
    }

    public static VoiceMessage createVoiceMessage(String msgId, String url, int duration) {
        VoiceMessage message = new VoiceMessage();
        message.setPlayStatus(ChatEnum.EPlayStatus.NO_DOWNLOADED);
        message.setMsgid(msgId);
        message.setTime(duration);
        message.setLocalUrl(url);
        return message;
    }

    @NonNull
    public static ImageMessage createImageMessage(String msgId, String url, boolean isOriginal) {
        ImageMessage image = new ImageMessage();
        image.setLocalimg(url);
        image.setPreview(url);
        image.setThumbnail(url);
        image.setMsgid(msgId);
        ImgSizeUtil.ImageSize img = ImgSizeUtil.getAttribute(url);
        image.setWidth(img.getWidth());
        image.setHeight(img.getHeight());
        image.setSize(img.getSize());
        if (isOriginal) {
            image.setOrigin(url);
        }
        return image;
    }

    @NonNull
    public static ImageMessage createImageMessage(String msgId, String url, String previewUrl, String thumUrl, long width, long height, boolean isOriginal, boolean isOriginRead, long size) {
        ImageMessage image = new ImageMessage();
        image.setLocalimg(url);
        image.setPreview(previewUrl);
        image.setThumbnail(thumUrl);
        image.setMsgid(msgId);
        image.setWidth(width);
        image.setHeight(height);
        image.setSize(size);
        if (isOriginal) {
            image.setOrigin(url);
        }
        image.setReadOrigin(isOriginRead);
        return image;
    }

    @NonNull
    public static VideoMessage createVideoMessage(String msgId, String url, String bgUrl, boolean isOriginal, long duration, long width, long height, String localUrl) {
        VideoMessage videoMessage = new VideoMessage();
        videoMessage.setMsgId(msgId);
        videoMessage.setUrl(url);
        videoMessage.setBg_url(bgUrl);
        videoMessage.setDuration(duration);
        videoMessage.setHeight(height);
        videoMessage.setWidth(width);
        videoMessage.setLocalUrl(localUrl);
        if (isOriginal) {
            videoMessage.setReadOrigin(isOriginal);
        }
        return videoMessage;
    }


    /**
     * 图片发送失败
     *
     * @param msgId
     * @return
     */
    public static MsgAllBean send4ImageFail(String msgId) {
        return msgDao.fixStataMsg(msgId, 1);

    }

    /***
     * 发送语音
     * @param toId
     * @param toGid
     * @param url
     * @param time
     * @return
     */
    public static MsgAllBean send4Voice(Long toId, String toGid, String url, int time) {
        MsgBean.VoiceMessage msg = MsgBean.VoiceMessage.newBuilder()
                .setUrl(url)
                .setDuration(time)
                .build();
        return send4Base(toId, toGid, MsgBean.MessageType.VOICE, msg);
    }

    /****
     * 发送名片
     * @param toId
     * @param toGid
     * @param iconUrl
     * @param nkName
     * @param info
     * @return
     */
    public static MsgAllBean send4card(Long toId, String toGid, Long uid, String iconUrl, String nkName, String info) {
        MsgBean.BusinessCardMessage msg = MsgBean.BusinessCardMessage.newBuilder()
                .setAvatar(iconUrl)
                .setNickname(nkName)
                .setComment(info)
                .setUid(uid)
                .build();
        return send4Base(toId, toGid, MsgBean.MessageType.BUSINESS_CARD, msg);
    }


    /***
     * 发送红包
     * @param toId
     * @param toGid
     * @param rid
     * @param info
     * @return
     */
    public static MsgAllBean send4Rb(Long toId, String toGid, String rid, String info, MsgBean.RedEnvelopeMessage.RedEnvelopeStyle style) {
        MsgBean.RedEnvelopeMessage msg = MsgBean.RedEnvelopeMessage.newBuilder()
                .setId(rid)
                .setComment(info)
                .setReType(MsgBean.RedEnvelopeType.MFPAY)
                .setStyle(style)
                .build();
        return send4Base(toId, toGid, MsgBean.MessageType.RED_ENVELOPER, msg);
    }

    /***
     * 收红包
     * @param toId
     * @param toGid
     * @param rid
     * @return
     */
    public static MsgAllBean send4RbRev(Long toId, String toGid, String rid, int reType) {
        msgDao.redEnvelopeOpen(rid, PayEnum.EEnvelopeStatus.RECEIVED, reType, "");
        MsgBean.ReceiveRedEnvelopeMessage msg = MsgBean.ReceiveRedEnvelopeMessage.newBuilder()
                .setId(rid)
                .build();

        if (toId.longValue() == UserAction.getMyId().longValue()) {//自己的不发红包通知,只保存
            MsgBean.UniversalMessage.Builder umsg = toMsgBuilder("", null, toId, toGid, getFixTime(), MsgBean.MessageType.RECEIVE_RED_ENVELOPER, msg);
            msgSave4Me(umsg, 0);
            return MsgConversionBean.ToBean(umsg.getWrapMsg(0));
        }

        //8.19 收到红包给自己增加一条消息
        String mid = getUUID();
        MsgNotice note = new MsgNotice();
        note.setMsgid(mid);
        note.setMsgType(8);
        String name = msgDao.getUsername4Show(toGid, toId);
        String rname = "<font color='#276baa' id='" + toId + "'>" + name + "</font>";
        if (toId.longValue() == UserAction.getMyId().longValue()) {
            rname = "自己";
        }
        note.setNote("你领取了\"" + rname + "的云红包" + "<div id= '" + toGid + "'></div>");
        msgDao.noteMsgAddRb(mid, toId, toGid, note);
        return send4Base(toId, toGid, MsgBean.MessageType.RECEIVE_RED_ENVELOPER, msg);
    }

    /***
     *发转账
     * @return
     */
    public static MsgAllBean send4Trans(Long toId, String rid, String info, String money) {
        MsgBean.TransferMessage msg = MsgBean.TransferMessage.newBuilder()
                .setId(rid)
                .setComment(info)
                .setTransactionAmount(money)
                .build();
        return send4Base(toId, null, MsgBean.MessageType.TRANSFER, msg);
    }


    /**
     * 已读消息
     */
    public static MsgAllBean send4Read(Long toId, long timestamp) {
        MsgBean.ReadMessage msg = MsgBean.ReadMessage.newBuilder()
                .setTimestamp(timestamp)
                .build();
        LogUtil.writeLog(">>>已读消息 toId:" + toId + " timestamp:" + timestamp);
        return send4Base(false, toId, null, MsgBean.MessageType.READ, msg);
    }

    private static String mCancelContent;// 撤回内容
    private static Integer mCancelContentType;// 撤回内容类型

    /**
     * 撤回消息
     *
     * @param toId
     * @param toGid
     * @param msgId      消息ID
     * @param msgContent 撤回内容
     * @param msgType    撤回的消息类型
     * @return
     */
    public static MsgAllBean send4CancelMsg(Long toId, String toGid, String msgId, String msgContent, Integer msgType) {
        int survivalTime = new UserDao().getReadDestroy(toId, toGid);
        MsgBean.CancelMessage msg = MsgBean.CancelMessage.newBuilder()
                .setMsgId(msgId)
                .build();

        mCancelContent = msgContent;
        mCancelContentType = msgType;

        String id = getUUID();
        MsgAllBean msgAllBean = send4Base(true, true, id, toId, toGid, -1, MsgBean.MessageType.CANCEL, msg);
//        ChatMessage chatMessage = new ChatMessage();
//        chatMessage.setMsg(msgContent);
//        chatMessage.setMsgid(msgType + "");// 暂时用来存放撤回的消息类型
//        msgAllBean.setChat(chatMessage);
        msgAllBean.setSurvival_time(survivalTime);
        ChatServer.addCanceLsit(id, msgAllBean);

        return msgAllBean;
    }
    /*
     * 发送及保存消息
     * */

    public static void sendAndSaveMessage(MsgAllBean bean) {
        LogUtil.getLog().i(TAG, ">>>---发送到toid" + bean.getTo_uid() + "--gid" + bean.getGid());
        sendAndSaveMessage(bean, true);
    }

    /**
     * 常信小助手发的消息不需要上会服务器，待完善
     *
     * @param bean
     * @param isSend 是否需要发送服务器
     */
    public static void sendAndSaveMessage(MsgAllBean bean, boolean isSend) {
        if (TextUtils.isEmpty(bean.getRequest_id())) {
            bean.setRequest_id(getUUID());
        }
        int msgType = bean.getMsg_type();
        MsgBean.MessageType type = null;
        Object value = null;
        boolean needSave = true;//默认是需要保存已经待发送的消息，但指令消息则只需发送，不要保存
        switch (msgType) {
//            case ChatEnum.EMessageType.NOTICE:
//                ChatMessage chat = bean.getChat();
//                MsgBean.ChatMessage.Builder txtBuilder = MsgBean.ChatMessage.newBuilder();
//                txtBuilder.setMsg(chat.getMsg());
//                value = txtBuilder.build();
//                type = MsgBean.MessageType.CHAT;
//                break;
            case ChatEnum.EMessageType.TEXT://文本
                ChatMessage chat = bean.getChat();
                MsgBean.ChatMessage.Builder txtBuilder = MsgBean.ChatMessage.newBuilder();
                txtBuilder.setMsg(chat.getMsg());
                value = txtBuilder.build();
                type = MsgBean.MessageType.CHAT;
                break;
            case ChatEnum.EMessageType.IMAGE://图片
                ImageMessage image = bean.getImage();
                MsgBean.ImageMessage.Builder imgBuilder = MsgBean.ImageMessage.newBuilder();
                imgBuilder.setOrigin(image.getOrigin())
                        .setPreview(image.getPreview())
                        .setThumbnail(image.getThumbnail())
                        .setHeight((int) image.getHeight())
                        .setWidth((int) image.getWidth())
                        .setSize((int) image.getSize());
                value = imgBuilder.build();
                type = MsgBean.MessageType.IMAGE;
                break;
            case ChatEnum.EMessageType.VOICE://语音
                VoiceMessage voice = bean.getVoiceMessage();
                MsgBean.VoiceMessage.Builder voiceBuilder = MsgBean.VoiceMessage.newBuilder();
                voiceBuilder.setDuration(voice.getTime());
                voiceBuilder.setUrl(voice.getUrl());
                value = voiceBuilder.build();
                type = MsgBean.MessageType.VOICE;
                break;
            case ChatEnum.EMessageType.MSG_VIDEO://小视频
                VideoMessage video = bean.getVideoMessage();
                MsgBean.ShortVideoMessage.Builder videoBuilder = MsgBean.ShortVideoMessage.newBuilder();
                videoBuilder.setBgUrl(video.getBg_url()).setDuration((int) video.getDuration()).setUrl(video.getUrl()).setWidth((int) video.getWidth()).setHeight((int) video.getHeight());
                value = videoBuilder.build();
                type = MsgBean.MessageType.SHORT_VIDEO;
                break;
            case ChatEnum.EMessageType.AT://@
                AtMessage at = bean.getAtMessage();
                MsgBean.AtMessage.Builder atBuilder = MsgBean.AtMessage.newBuilder();
                atBuilder.setAtTypeValue(at.getAt_type()).setMsg(at.getMsg()).addAllUid(at.getUid());
                value = atBuilder.build();
                type = MsgBean.MessageType.AT;
                break;
            case ChatEnum.EMessageType.BUSINESS_CARD://名片
                BusinessCardMessage card = bean.getBusiness_card();
                MsgBean.BusinessCardMessage.Builder cardBuilder = MsgBean.BusinessCardMessage.newBuilder();
                cardBuilder.setUid(card.getUid()).setAvatar(card.getAvatar()).setNickname(card.getNickname()).setComment(card.getComment());
                value = cardBuilder.build();
                type = MsgBean.MessageType.BUSINESS_CARD;
                break;
            case ChatEnum.EMessageType.STAMP://戳一戳
                StampMessage stamp = bean.getStamp();
                MsgBean.StampMessage.Builder stampBuilder = MsgBean.StampMessage.newBuilder();
                stampBuilder.setComment(stamp.getComment());
                value = stampBuilder.build();
                type = MsgBean.MessageType.STAMP;
                break;
            case ChatEnum.EMessageType.TRANSFER://转账
                TransferMessage transfer = bean.getTransfer();
                MsgBean.TransferMessage.Builder transferBuild = MsgBean.TransferMessage.newBuilder();

                transferBuild.setTransactionAmount(transfer.getTransaction_amount());
                transferBuild.setComment(transfer.getComment());
                transferBuild.setId(transfer.getId());
                transferBuild.setOpType(MsgBean.TransferMessage.OpType.forNumber(transfer.getOpType()));
                transferBuild.setSign(transfer.getSign());
                value = transferBuild.build();
                type = MsgBean.MessageType.TRANSFER;
                break;
            case ChatEnum.EMessageType.RED_ENVELOPE://红包
                RedEnvelopeMessage red = bean.getRed_envelope();
                int reType = red.getRe_type().intValue();
                MsgBean.RedEnvelopeMessage.Builder redBuild = null;
                if (reType == MsgBean.RedEnvelopeType.MFPAY_VALUE) {
                    redBuild = MsgBean.RedEnvelopeMessage.newBuilder()
                            .setId(red.getId())
                            .setComment(red.getComment())
                            .setReType(MsgBean.RedEnvelopeType.forNumber(red.getRe_type()))
                            .setStyle(MsgBean.RedEnvelopeMessage.RedEnvelopeStyle.forNumber(red.getStyle()));
                } else if (reType == MsgBean.RedEnvelopeType.SYSTEM_VALUE) {
                    redBuild = MsgBean.RedEnvelopeMessage.newBuilder()
                            .setId(red.getTraceId() + "")
                            .setComment(red.getComment())
                            .setReType(MsgBean.RedEnvelopeType.forNumber(red.getRe_type()))
                            .setStyle(MsgBean.RedEnvelopeMessage.RedEnvelopeStyle.forNumber(red.getStyle()))
                            .setSign(red.getSign());
                }
                if (redBuild != null) {
                    value = redBuild.build();
                    type = MsgBean.MessageType.RED_ENVELOPER;
                }
                break;
            case ChatEnum.EMessageType.READ://已读消息，不需要保存

                needSave = false;
                break;
            case ChatEnum.EMessageType.MSG_CANCEL://撤销消息

                break;
            case ChatEnum.EMessageType.LOCATION://位置
                LocationMessage locationMessage = bean.getLocationMessage();
                MsgBean.SnapshotLocationMessage.Builder locationBuilder = MsgBean.SnapshotLocationMessage.newBuilder();
                locationBuilder.setLat(locationMessage.getLatitude());
                locationBuilder.setLon(locationMessage.getLongitude());
                locationBuilder.setImg(locationMessage.getImg());
                locationBuilder.setAddr(locationMessage.getAddress());
                locationBuilder.setDesc(locationMessage.getAddressDescribe());
                value = locationBuilder.build();
                type = MsgBean.MessageType.SNAPSHOT_LOCATION;
                break;
        }

        if (needSave) {
            saveMessage(bean);
        }
        if (type != null && value != null && isSend) {
            SendList.addMsgToSendSequence(bean.getRequest_id(), bean);//添加到发送队列
            MsgBean.UniversalMessage.Builder msg = toMsgBuilder(bean.getRequest_id(), bean.getMsg_id(), bean.getTo_uid(), bean.getGid(), bean.getTimestamp(), type, value);
            //立即发送
            LogUtil.getLog().e("===发送=msg===" + GsonUtils.optObject(msg));
            SocketUtil.getSocketUtil().sendData4Msg(msg);
        }
    }

    //消息被拒
    public static MsgAllBean createMsgBeanOfNotice(MsgBean.AckMessage ack, @ChatEnum.ENoticeType int type) {
        MsgAllBean bean = msgDao.getMsgById(ack.getMsgId(0));
        MsgAllBean msg = null;
        if (bean != null && TextUtils.isEmpty(bean.getGid())) {
            msg = new MsgAllBean();
            String msgId = SocketData.getUUID();
            msg.setMsg_id(msgId);
            msg.setMsg_type(ChatEnum.EMessageType.NOTICE);
            msg.setFrom_uid(bean.getFrom_uid());
            long time = getSysTime();
            if (ack.getTimestamp() >= time) {
                msg.setTimestamp(bean.getTimestamp() + 1);
            } else {
                msg.setTimestamp(time);
            }

            if (type == ChatEnum.ENoticeType.BLACK_ERROR) {
                int survivalTime = new UserDao().getReadDestroy(bean.getTo_uid(), null);
                msg.setSurvival_time(survivalTime);
                msg.setRead(1);
            }
            msg.setTo_uid(bean.getTo_uid());
            msg.setGid(bean.getGid());
            msg.setFrom_nickname(bean.getFrom_nickname());
            msg.setFrom_group_nickname(bean.getFrom_group_nickname());
            msg.setMsgNotice(createMsgNotice(msgId, type, getNoticeString(bean, type)));
        }
        return msg;
    }

    public static MsgNotice createMsgNotice(String msgId, @ChatEnum.ENoticeType int type, String content) {
        MsgNotice note = new MsgNotice();
        note.setMsgid(msgId);
        note.setMsgType(type);
        note.setNote(content);
        return note;
    }

//    public static MsgNotice createMsgNoticeOfRb(String msgId, Long uid, String gid) {
//        MsgNotice note = new MsgNotice();
//        note.setMsgid(msgId);
//        if (uid != null && uid.longValue() == UserAction.getMyId().longValue()) {
//            note.setMsgType(ChatEnum.ENoticeType.SYS_ENVELOPE_RECEIVED_SELF);
//            note.setNote("你领取了自己的<font color='#cc5944'>零钱红包</font>");
//        } else {
//            note.setMsgType(ChatEnum.ENoticeType.RECEIVE_SYS_ENVELOPE);
//            String name = msgDao.getUsername4Show(gid, uid);
//            String rname = "<font color='#276baa' id='" + uid + "'>" + name + "</font>";
//            note.setNote("你领取了\"" + rname + "的零钱红包" + "<div id= '" + gid + "'></div>");
//        }
//
//        return note;
//    }

    public static MsgNotice createMsgNoticeOfRb(String msgId, Long uid, String gid, String rid) {
        MsgNotice note = new MsgNotice();
        note.setMsgid(msgId);
        if (uid != null && uid.longValue() == UserAction.getMyId().longValue()) {
            note.setMsgType(ChatEnum.ENoticeType.SYS_ENVELOPE_RECEIVED_SELF);
            note.setNote("你领取了自己的<envelope id=" + rid + ">零钱红包</envelope>");
        } else {
            note.setMsgType(ChatEnum.ENoticeType.RECEIVE_SYS_ENVELOPE);
            String name = msgDao.getUsername4Show(gid, uid);
            String n = "<user id='" + uid + "'>" + name + "</user>";
            note.setNote("你领取了\"" + n + "\"的" + "<envelope id=\" + rid + \">零钱红包</envelope>");
        }

        return note;
    }

    public static String getNoticeString(MsgAllBean bean, @ChatEnum.ENoticeType int type) {
        String note = "";
        if (bean != null) {
            switch (type) {
                case ChatEnum.ENoticeType.BLACK_ERROR:
                    note = "消息发送成功，但对方已拒收";
                    break;
                case ChatEnum.ENoticeType.NO_FRI_ERROR:
                    String name = "";
                    if (bean.getTo_user() != null) {
                        name = bean.getTo_user().getName4Show();
                    }
                    note = "你已不是" + "\"<font color='#276baa' id='" + bean.getTo_uid() + "'>" + name + "</font>\"" + "的好友, 请先" + "<font color='#276baa' id='" + bean.getTo_uid() + "'>" + "添加对方为好友" + "</font>";
                    break;
                case ChatEnum.ENoticeType.LOCK:
                    note = "聊天中所有信息已进行" + "<font color='#1f5305' tag=" + ChatEnum.ETagType.LOCK + ">" + "端对端加密" + "</font>" + "保护";
                    break;
            }
        }
        return note;
    }

    public static long getPreServerAckTime() {
        return preServerAckTime;
    }

    public static void setPreServerAckTime(long preServerAckTime) {
        SocketData.preServerAckTime = preServerAckTime;
    }

    public static long getPreSendLocalTime() {
        return preSendLocalTime;
    }

    public static void setPreSendLocalTime(long preSendLocalTime) {
        SocketData.preSendLocalTime = preSendLocalTime;
    }

    //获取修正时间
    public static long getFixTime() {
        long currentTime = System.currentTimeMillis();
        if (preServerAckTime > preSendLocalTime && preServerAckTime > currentTime) {//服务器回执时间最新
            currentTime = preServerAckTime + 1;
            preServerAckTime = currentTime;
        } else if (preSendLocalTime > preServerAckTime && preSendLocalTime > currentTime) {//本地发送时间最新
            currentTime = preSendLocalTime + 1;
            preSendLocalTime = currentTime;
        } else {//本地系统时间最新
            preSendLocalTime = currentTime;
        }
        return currentTime;
    }

    public static long getSysTime() {
        return System.currentTimeMillis();
    }

    /*
     * 创建自己发送的消息bean
     * @param uid Long 用户Id,私聊即to_uid,群聊为null
     * @gid 群id，私聊为空，群聊不能为空
     * @msgType int 消息类型
     * @sendStatus int 发送状态
     * @obj IMsgContent MsgAllBean二级关联表bean
     * */
    public static MsgAllBean createMessageBean(Long uid, String gid, @ChatEnum.EMessageType int msgType, @ChatEnum.ESendStatus int sendStatus, long time, IMsgContent obj) {
        if (UserAction.getMyInfo() == null) {
            return null;
        }
        boolean isGroup = false;
        if (uid == null && !TextUtils.isEmpty(gid)) {
            isGroup = true;
        }

        MsgAllBean msg = new MsgAllBean();
        msg.setMsg_id(obj.getMsgId());
        msg.setMsg_type(msgType);
        msg.setTimestamp(time > 0 ? time : getFixTime());
        msg.setTo_uid(uid);
        msg.setGid(gid);
        msg.setSend_state(sendStatus);
        msg.setFrom_uid(UserAction.getMyId());
        msg.setFrom_avatar(UserAction.getMyInfo().getHead());
        msg.setFrom_nickname(UserAction.getMyInfo().getName());
        int survivaltime = new UserDao().getReadDestroy(uid, gid);
        msg.setSurvival_time(survivaltime);

        msg.setRead(true);//已读
        if (isGroup) {
            Group group = msgDao.getGroup4Id(gid);
            if (group != null) {
                String name = group.getMygroupName();
                if (StringUtil.isNotNull(name)) {
                    msg.setFrom_group_nickname(name);
                }
            }
        }
        switch (msgType) {
            case ChatEnum.EMessageType.NOTICE:
                if (obj instanceof MsgNotice) {
                    msg.setMsgNotice((MsgNotice) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.TEXT:
                if (obj instanceof ChatMessage) {
                    msg.setChat((ChatMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.STAMP:
                if (obj instanceof StampMessage) {
                    msg.setStamp((StampMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.RED_ENVELOPE:
                if (obj instanceof RedEnvelopeMessage) {
                    msg.setRed_envelope((RedEnvelopeMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.IMAGE:
                if (obj instanceof ImageMessage) {
                    msg.setImage((ImageMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.BUSINESS_CARD:
                if (obj instanceof BusinessCardMessage) {
                    msg.setBusiness_card((BusinessCardMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.TRANSFER:
                if (obj instanceof TransferMessage) {
                    msg.setTransfer((TransferMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.VOICE:
                if (obj instanceof VoiceMessage) {
                    msg.setVoiceMessage((VoiceMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.AT:
                if (obj instanceof AtMessage) {
                    msg.setAtMessage((AtMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.ASSISTANT:
                if (obj instanceof AssistantMessage) {
                    msg.setAssistantMessage((AssistantMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.MSG_CANCEL:
                if (obj instanceof MsgCancel) {
                    msg.setMsgCancel((MsgCancel) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.MSG_VIDEO:
                if (obj instanceof VideoMessage) {
                    msg.setVideoMessage((VideoMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.LOCATION:
                if (obj instanceof LocationMessage) {
                    msg.setLocationMessage((LocationMessage) obj);
                } else {
                    return null;
                }
//                LogUtil.getLog().e("===location==LocationMessage="+ GsonUtils.optObject(msg));
                break;

        }

        return msg;
    }

    /*
     * 创建接收到的消息bean
     * @param uid Long 用户Id,私聊即to_uid,群聊为null
     * @gid 群id，私聊为空，群聊不能为空
     * @msgType int 消息类型
     * @sendStatus int 发送状态
     * @obj IMsgContent MsgAllBean二级关联表bean
     * */
    public static MsgAllBean createMsgBean(MsgBean.UniversalMessage.WrapMessage wrap, @ChatEnum.EMessageType int msgType, @ChatEnum.ESendStatus int sendStatus, long time, IMsgContent obj) {
        if (wrap == null) {
            return null;
        }
        boolean isGroup = false;
        if (wrap.getFromUid() <= 0 && !TextUtils.isEmpty(wrap.getGid())) {
            isGroup = true;
        }

        MsgAllBean msg = new MsgAllBean();
        msg.setMsg_id(obj.getMsgId());
        msg.setMsg_type(msgType);
        msg.setTimestamp(time > 0 ? time : getFixTime());
        msg.setFrom_uid(wrap.getFromUid());
        msg.setFrom_avatar(wrap.getAvatar());
        msg.setFrom_nickname(wrap.getNickname());
        msg.setFrom_group_nickname(wrap.getMembername());
        msg.setGid(wrap.getGid());
        msg.setSend_state(sendStatus);
        msg.setRead(false);
        int survivaltime = new UserDao().getReadDestroy(wrap.getFromUid(), wrap.getGid());
        msg.setSurvival_time(survivaltime);

        if (isGroup) {
            Group group = msgDao.getGroup4Id(wrap.getGid());
            if (group != null) {
                String name = group.getMygroupName();
                if (StringUtil.isNotNull(name)) {
                    msg.setFrom_group_nickname(name);
                }
            }
        }
        switch (msgType) {
            case ChatEnum.EMessageType.NOTICE:
                if (obj instanceof MsgNotice) {
                    msg.setMsgNotice((MsgNotice) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.TEXT:
                if (obj instanceof ChatMessage) {
                    msg.setChat((ChatMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.STAMP:
                if (obj instanceof StampMessage) {
                    msg.setStamp((StampMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.RED_ENVELOPE:
                if (obj instanceof RedEnvelopeMessage) {
                    msg.setRed_envelope((RedEnvelopeMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.IMAGE:
                if (obj instanceof ImageMessage) {
                    msg.setImage((ImageMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.BUSINESS_CARD:
                if (obj instanceof BusinessCardMessage) {
                    msg.setBusiness_card((BusinessCardMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.TRANSFER:
                if (obj instanceof TransferMessage) {
                    msg.setTransfer((TransferMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.VOICE:
                if (obj instanceof VoiceMessage) {
                    msg.setVoiceMessage((VoiceMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.AT:
                if (obj instanceof AtMessage) {
                    msg.setAtMessage((AtMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.ASSISTANT:
                if (obj instanceof AssistantMessage) {
                    msg.setAssistantMessage((AssistantMessage) obj);
                } else {
                    return null;
                }
                break;
            case ChatEnum.EMessageType.MSG_CANCEL:
                if (obj instanceof MsgCancel) {
                    msg.setMsgCancel((MsgCancel) obj);
                } else {
                    return null;
                }
                break;

        }

        return msg;
    }

    //@消息
    public static AtMessage createAtMessage(String msgId, String content, @ChatEnum.EAtType int atType, List<Long> userIds) {
        AtMessage message = new AtMessage();
        message.setMsgId(msgId);
        message.setAt_type(atType);
        message.setMsg(content);
        if (userIds != null) {
            RealmList<Long> realmList = new RealmList<>();
            realmList.addAll(userIds);
            message.setUid(realmList);
        }
        return message;
    }

    //文本消息
    public static ChatMessage createChatMessage(String msgId, String content) {
        ChatMessage message = new ChatMessage();
        message.setMsgid(msgId);
        message.setMsg(content);
        return message;
    }

    //戳一戳消息
    public static StampMessage createStampMessage(String msgId, String content) {
        StampMessage message = new StampMessage();
        message.setMsgid(msgId);
        message.setComment(content);
        return message;
    }

    //名片消息
    public static BusinessCardMessage createCardMessage(String msgId, String avatar, String nick, String info, long uid) {
        BusinessCardMessage message = new BusinessCardMessage();
        message.setMsgid(msgId);
        message.setUid(uid);
        message.setAvatar(avatar);
        message.setNickname(nick);
        message.setComment(info);
        return message;
    }

    public static void saveMessage(MsgAllBean bean) {
        DaoUtil.update(bean);
        if (msgDao == null) {
            msgDao = new MsgDao();
        }
        msgDao.sessionCreate(bean.getGid(), bean.getTo_uid());
        MessageManager.getInstance().setMessageChange(true);
    }

    //端到端加密消息
    public static MsgAllBean createMessageLock(String gid, Long uid) {
        MsgAllBean bean = new MsgAllBean();
        if (!TextUtils.isEmpty(gid)) {
            bean.setGid(gid);
            bean.setFrom_uid(UserAction.getMyInfo().getUid());
        } else if (uid != null) {
            bean.setFrom_uid(uid);
        } else {
            return null;
        }
        bean.setMsg_type(ChatEnum.EMessageType.LOCK);
        bean.setMsg_id(SocketData.getUUID());
        bean.setTimestamp(0L);
        ChatMessage message = SocketData.createChatMessage(bean.getMsg_id(), getNoticeString(bean, ChatEnum.ENoticeType.LOCK));
        bean.setChat(message);
        return bean;
    }

    //小视频消息
    @NonNull
    public static VideoMessage createVideoMessage(String msgId, String bgUrl, String url, long duration, long width, long height, boolean isReadOrigin) {
        VideoMessage videoMessage = new VideoMessage();
        videoMessage.setMsgId(msgId);
        videoMessage.setBg_url(bgUrl);
        videoMessage.setUrl(url);
        videoMessage.setDuration(duration);
        videoMessage.setWidth(width);
        videoMessage.setHeight(height);
        videoMessage.setReadOrigin(isReadOrigin);
        return videoMessage;
    }

    //转账消息
    @NonNull
    public static TransferMessage createTransferMessage(String msgId, String rowId, String money, String comment) {
        TransferMessage message = new TransferMessage();
        message.setMsgid(msgId);
        message.setComment(comment);
        message.setId(rowId);
        message.setTransaction_amount(money);
        return message;
    }

    //

    /**
     * 创建发送红包消息
     *
     * @param reType, 红包运营商，如支付宝红包，魔方红包
     * @param style   红包玩法风格，0 普通玩法 ； 1 拼手气玩法
     */
    public static RedEnvelopeMessage createRbMessage(String msgId, String rid, String info, int reType, int style) {
        RedEnvelopeMessage message = new RedEnvelopeMessage();
        message.setMsgid(msgId);
        message.setId(rid);
        message.setComment(info);
        message.setRe_type(reType);
        message.setStyle(style);
        return message;
    }

    //创建系统红包消息
    public static RedEnvelopeMessage createSystemRbMessage(String msgId, long traceId, String actionId, String info, int reType, int style, String sign) {
        RedEnvelopeMessage message = new RedEnvelopeMessage();
        message.setMsgid(msgId);
        message.setTraceId(traceId);
        message.setActionId(actionId);
        message.setComment(info);
        message.setRe_type(reType);
        message.setStyle(style);
        message.setSign(sign);
        return message;
    }

    //更新发送状态，根据ack
    public static boolean updateMsgSendStatusByAck(MsgBean.AckMessage ackMessage) {
        MsgAllBean msgAllBean = SendList.getMsgFromSendSequence(ackMessage.getRequestId());
        if (msgAllBean != null) {
            SendList.removeMsgFromSendSequence(ackMessage.getRequestId());
            SendList.removeSendListJust(ackMessage.getRequestId());
            msgAllBean.setSend_state(ChatEnum.ESendStatus.NORMAL);
            if (msgAllBean.getVideoMessage() != null && !TextUtils.isEmpty(videoLocalUrl)) {
                msgAllBean.getVideoMessage().setLocalUrl(videoLocalUrl);
            }
            DaoUtil.update(msgAllBean);
            return true;
        }
        return false;
    }

    /***
     * 发送领取红包消息, 不会加入发送队列，没有重发机制
     * @param toId
     * @param toGid
     * @param rid
     * @return
     */
    public static void sendReceivedEnvelopeMsg(Long toId, String toGid, String rid, int reType) {
        //自己抢自己的红包，不需要发送
        if (toId != null && UserAction.getMyId() != null && toId.longValue() == UserAction.getMyId().longValue()) {
            return;
        }
        MsgBean.RedEnvelopeType type = MsgBean.RedEnvelopeType.forNumber(reType);
        MsgBean.ReceiveRedEnvelopeMessage contentMsg = MsgBean.ReceiveRedEnvelopeMessage.newBuilder()
                .setId(rid)
                .setReType(type)
                .build();
        MsgBean.UniversalMessage.Builder msg = toMsgBuilder("", SocketData.getUUID(), toId, toGid, SocketData.getFixTime(), MsgBean.MessageType.RECEIVE_RED_ENVELOPER, contentMsg);
        //立即发送
        SocketUtil.getSocketUtil().sendData4Msg(msg);
    }

    //位置消息
    public static LocationMessage createLocationMessage(String msgId, LocationMessage messageTemp) {
        LocationMessage message = new LocationMessage();
        message.setMsgId(msgId);
        message.setLatitude(messageTemp.getLatitude());
        message.setLongitude(messageTemp.getLongitude());
        message.setImg(messageTemp.getImg());
        message.setAddress(messageTemp.getAddress());
        message.setAddressDescribe(messageTemp.getAddressDescribe());
        //message.setImg("http://e7-test.oss-cn-beijing.aliyuncs.com/Android/20190730/2dfe5997-68a5-4545-8099-712982b765c9.jpg");
        return message;
    }

    //创建转账消息
    public static TransferMessage createTransferMessage(String msgId, long traceId, long money, String info, String sign, int opType) {
        TransferMessage message = new TransferMessage();
        message.setMsgid(msgId);
        message.setId(traceId + "");
        message.setComment(info);
        message.setTransaction_amount(money + "");
        message.setSign(sign);
        message.setOpType(opType);
        return message;
    }


}
