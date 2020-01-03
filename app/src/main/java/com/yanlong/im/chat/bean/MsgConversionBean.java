package com.yanlong.im.chat.bean;

import android.text.TextUtils;

import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.chat.ui.groupmanager.GroupMemPowerSetActivity;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.utils.DaoUtil;
import com.yanlong.im.utils.ReadDestroyUtil;
import com.yanlong.im.utils.socket.MsgBean;

import net.cb.cb.library.bean.EventGroupChange;
import net.cb.cb.library.utils.GsonUtils;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;

import io.realm.RealmList;

/***
 * 消息转换类
 */
public class MsgConversionBean {


    /***
     * 把pd转成greendao需要的bean
     * @param bean
     * @return
     */
    public static MsgAllBean ToBean(MsgBean.UniversalMessage.WrapMessage bean) {
        return ToBean(bean, null, false);
    }

    /*
     * @param isError是否是发送失败消息
     * */
    public static MsgAllBean ToBean(MsgBean.UniversalMessage.WrapMessage bean, MsgBean.UniversalMessage.Builder msg, boolean isError) {
        if (bean.getMsgType() == MsgBean.MessageType.ACTIVE_STAT_CHANGE) {
            return null;
        }
        MsgDao msgDao = new MsgDao();
        //手动处理转换
        MsgAllBean msgAllBean = new MsgAllBean();
        if (isError) {
            msgAllBean.setTimestamp(bean.getTimestamp());
            UserDao userDao = new UserDao();
            int survivalTime = userDao.getReadDestroy(bean.getFromUid(), bean.getGid());
            if (survivalTime != 0) {
                msgAllBean.setSurvival_time(survivalTime);
            }
        } else {
            if (msg != null) {
                msgAllBean.setTimestamp(msg.getWrapMsg(0).getTimestamp());
                msgAllBean.setSurvival_time(msg.getWrapMsg(0).getSurvivalTime());

            } else {
                msgAllBean.setSurvival_time(bean.getSurvivalTime());
                msgAllBean.setTimestamp(bean.getTimestamp());
            }
        }
        msgAllBean.setFrom_uid(bean.getFromUid());
        msgAllBean.setFrom_avatar(bean.getAvatar());
        msgAllBean.setFrom_nickname(bean.getNickname());
        msgAllBean.setFrom_group_nickname(bean.getMembername());
        msgAllBean.setTo_uid(bean.getToUid());

        msgAllBean.setGid(bean.getGid());
        if (UserAction.getMyId() != null && bean.getFromUid() == UserAction.getMyId().intValue()) {//自己发的
            msgAllBean.setRead(true);
        } else {
            if (!TextUtils.isEmpty(bean.getGid())) {//群聊
                if (!TextUtils.isEmpty(MessageManager.SESSION_GID) && MessageManager.SESSION_GID.equals(bean.getGid())) {
                    msgAllBean.setRead(true);
                } else {
                    if (bean.getMsgTypeValue() == ChatEnum.EMessageType.MSG_CANCEL) {
                        msgAllBean.setRead(true);
                    } else {
                        msgAllBean.setRead(false);
                    }
                }
            } else {//私聊
                if (MessageManager.SESSION_FUID != null && MessageManager.SESSION_FUID.equals(bean.getFromUid())) {
                    msgAllBean.setRead(true);
                } else {
                    if (bean.getMsgTypeValue() == ChatEnum.EMessageType.MSG_CANCEL) {
                        msgAllBean.setRead(true);
                    } else {
                        msgAllBean.setRead(false);
                    }
                }
            }
        }

//        msgAllBean.setSurvival_time(bean.getSurvivalTime());
//        UserDao userDao = new UserDao();
//        int survivalTime = userDao.getReadDestroy(bean.getFromUid(), bean.getGid());
//        if (survivalTime != 0) {
//            msgAllBean.setSurvival_time(survivalTime);
//        }

        if (msg != null) {
            msgAllBean.setRequest_id(msg.getRequestId());
            msgAllBean.setTo_uid(msg.getToUid());
        } else {
            msgAllBean.setTo_uid(bean.getToUid());
        }

        //这里需要处理用户信息
        UserInfo userInfo = DaoUtil.findOne(UserInfo.class, "uid", bean.getFromUid());
        if (userInfo != null) {//更新用户信息
            //msgAllBean.setFrom_user(userInfo);
        } else {
            //从网路缓存
            bean.getAvatar();
            bean.getNickname();
        }
        //---------------------
        msgAllBean.setMsg_id(bean.getMsgId());

        switch (bean.getMsgType()) {
            case CHAT:
                ChatMessage chat = new ChatMessage();
                chat.setMsgid(msgAllBean.getMsg_id());
                chat.setMsg(bean.getChat().getMsg());
                msgAllBean.setChat(chat);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.TEXT);
                break;
            case IMAGE:
                ImageMessage image = new ImageMessage();
                image.setMsgid(msgAllBean.getMsg_id());
                MsgAllBean imgMsg = DaoUtil.findOne(MsgAllBean.class, "msg_id", msgAllBean.getMsg_id());
                if (imgMsg != null) {//7.16 替换成上一次本地的图片路径
                    image.setLocalimg(imgMsg.getImage().getLocalimg());
                    LogUtil.getLog().d("TAG", "查询到本地图" + image.getLocalimg());

                }

                image.setOrigin(bean.getImage().getOrigin());
                image.setPreview(bean.getImage().getPreview());
                image.setThumbnail(bean.getImage().getThumbnail());
                image.setWidth(bean.getImage().getWidth());
                image.setHeight(bean.getImage().getHeight());
                image.setSize(bean.getImage().getSize());
                msgAllBean.setImage(image);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.IMAGE);
                break;

            case STAMP:// 戳一下消息
                StampMessage stamp = new StampMessage();
                stamp.setMsgid(msgAllBean.getMsg_id());
                stamp.setComment(bean.getStamp().getComment());
                msgAllBean.setStamp(stamp);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.STAMP);
                break;

            case VOICE:// 语音消息
                MsgAllBean voiceMsg = DaoUtil.findOne(MsgAllBean.class, "msg_id", msgAllBean.getMsg_id());
                VoiceMessage voiceMessage = new VoiceMessage();

                if (voiceMsg != null && voiceMsg.getVoiceMessage() != null && !TextUtils.isEmpty(voiceMsg.getVoiceMessage().getLocalUrl())) {//保存本地路径到localUrl
                    voiceMessage.setLocalUrl(voiceMsg.getVoiceMessage().getLocalUrl());
                }
                voiceMessage.setMsgid(msgAllBean.getMsg_id());
                voiceMessage.setUrl(bean.getVoice().getUrl());
                voiceMessage.setTime(bean.getVoice().getDuration());
                msgAllBean.setVoiceMessage(voiceMessage);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.VOICE);
                break;
            case SHORT_VIDEO:
                MsgAllBean videoMsg = DaoUtil.findOne(MsgAllBean.class, "msg_id", msgAllBean.getMsg_id());
                VideoMessage videoMessage = new VideoMessage();
                videoMessage.setMsgId(msgAllBean.getMsg_id());
                videoMessage.setUrl(bean.getShortVideo().getUrl());
                videoMessage.setBg_url(bean.getShortVideo().getBgUrl());
                videoMessage.setWidth(bean.getShortVideo().getWidth());
                videoMessage.setHeight(bean.getShortVideo().getHeight());
                videoMessage.setDuration(bean.getShortVideo().getDuration());
                msgAllBean.setMsg_type(ChatEnum.EMessageType.MSG_VIDEO);
                msgAllBean.setVideoMessage(videoMessage);
//                videoMsg.setMsg_type(ChatEnum.EMessageType.MSG_VIDEO);
//                videoMsg.setVideoMessage(videoMessage);
//                videoMessage.setLocalUrl(bean.getShortVideo().getLocalUrl());
                break;
            case TRANSFER:
                TransferMessage transferMessage = new TransferMessage();
                transferMessage.setMsgid(msgAllBean.getMsg_id());
                transferMessage.setId(bean.getTransfer().getId());
                transferMessage.setComment(bean.getTransfer().getComment());
                transferMessage.setTransaction_amount(bean.getTransfer().getTransactionAmount());
                transferMessage.setOpType(bean.getTransfer().getOpTypeValue());
                msgAllBean.setTransfer(transferMessage);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.TRANSFER);
                break;
            case BUSINESS_CARD:
                BusinessCardMessage businessCard = new BusinessCardMessage();
                businessCard.setMsgid(msgAllBean.getMsg_id());
                businessCard.setAvatar(bean.getBusinessCard().getAvatar());
                businessCard.setComment(bean.getBusinessCard().getComment());
                businessCard.setNickname(bean.getBusinessCard().getNickname());
                businessCard.setUid(bean.getBusinessCard().getUid());
                msgAllBean.setBusiness_card(businessCard);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.BUSINESS_CARD);
                break;
            case RED_ENVELOPER: //红包消息
                RedEnvelopeMessage envelopeMessage = new RedEnvelopeMessage();
                envelopeMessage.setMsgid(msgAllBean.getMsg_id());
                envelopeMessage.setComment(bean.getRedEnvelope().getComment());
                if (bean.getRedEnvelope().getReTypeValue() == MsgBean.RedEnvelopeType.MFPAY_VALUE) {
                    envelopeMessage.setId(bean.getRedEnvelope().getId());
                } else if (bean.getRedEnvelope().getReTypeValue() == MsgBean.RedEnvelopeType.SYSTEM_VALUE) {
                    try {
                        long tradeId = Long.parseLong(bean.getRedEnvelope().getId());
                        envelopeMessage.setTraceId(tradeId);
                    } catch (Exception e) {
                        envelopeMessage.setId(bean.getRedEnvelope().getId());
                    }
                }
                envelopeMessage.setRe_type(bean.getRedEnvelope().getReTypeValue());
                envelopeMessage.setStyle(bean.getRedEnvelope().getStyleValue());
                msgAllBean.setRed_envelope(envelopeMessage);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.RED_ENVELOPE);
                break;
            case RECEIVE_RED_ENVELOPER:
                msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                MsgNotice rbNotice = new MsgNotice();
                rbNotice.setMsgid(msgAllBean.getMsg_id());
                //jyj 8.19
                if (bean.getReceiveRedEnvelope().getReType().getNumber() == 0) {
                    if (UserAction.getMyId() != null && bean.getFromUid() == UserAction.getMyId().longValue()) {
                        rbNotice.setNote("你领取了自己的<font color='#cc5944'>云红包</font>");
                        rbNotice.setMsgType(ChatEnum.ENoticeType.RED_ENVELOPE_RECEIVED_SELF);
                    } else {
                        rbNotice.setMsgType(ChatEnum.ENoticeType.RED_ENVELOPE_RECEIVED);
                        rbNotice.setNote("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + bean.getNickname() + "</font>" + "\"领取了你的云红包 <div id='" + bean.getGid() + "'></div>");
                    }
                } else if (bean.getReceiveRedEnvelope().getReType().getNumber() == 1) {
                    if (UserAction.getMyId() != null && bean.getFromUid() == UserAction.getMyId().longValue()) {
                        rbNotice.setNote("你领取了自己的<font color='#cc5944'>零钱红包</font>");
                        rbNotice.setMsgType(ChatEnum.ENoticeType.SYS_ENVELOPE_RECEIVED_SELF);
                    } else {
                        rbNotice.setMsgType(ChatEnum.ENoticeType.SYS_ENVELOPE_RECEIVED);
                        rbNotice.setNote("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + bean.getNickname() + "</font>" + "\"领取了你的零钱红包 <div id='" + bean.getGid() + "'></div>");
                    }
                }

                msgAllBean.setMsgNotice(rbNotice);
                break;

            //需要保存的通知类消息
            case ACCEPT_BE_FRIENDS:// 接收好友请求
                msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                MsgNotice msgNotice = new MsgNotice();
                msgNotice.setMsgid(msgAllBean.getMsg_id());
                msgNotice.setNote(bean.getNickname() + "已加你为好友");
                msgAllBean.setMsgNotice(msgNotice);
                break;
            case ACCEPT_BE_GROUP://接受入群请求
                msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                MsgNotice gNotice = new MsgNotice();
                gNotice.setMsgid(msgAllBean.getMsg_id());
                String names = "";
                for (int i = 0; i < bean.getAcceptBeGroup().getNoticeMessageCount(); i++) {
                    //7.13 加入替换自己的昵称
                    if (UserAction.getMyId() != null && bean.getAcceptBeGroup().getNoticeMessage(i).getUid() == UserAction.getMyId().longValue()) {
                        names += "\"<font value ='1'> 你、</font>\"、";

                    } else {
                        String name = bean.getAcceptBeGroup().getNoticeMessage(i).getNickname();
                        Long uid = bean.getAcceptBeGroup().getNoticeMessage(i).getUid();

                        MsgAllBean gmsg = msgDao.msgGetLastGroup4Uid(bean.getGid(), uid);
                        if (gmsg != null) {
                            name = StringUtil.isNotNull(gmsg.getFrom_group_nickname()) ? gmsg.getFrom_group_nickname() : name;
                        }

                        UserInfo userinfo = DaoUtil.findOne(UserInfo.class, "uid", uid);
                        if (userinfo != null) {
                            name = StringUtil.isNotNull(userinfo.getMkName()) ? userinfo.getMkName() : name;
                        }

                        names += "\"<font id='" + uid + "' value ='2'>" + name + "</font>\"、";
                    }

                }
                names = names.length() > 0 ? names.substring(0, names.length() - 1) : names;
                String inviterName = bean.getAcceptBeGroup().getInviterName();//邀请者名字
                if (UserAction.getMyId() != null && bean.getAcceptBeGroup().getInviter() == UserAction.getMyId().longValue()) {
                    inviterName = "\"<font value ='3'> 你</font>\"";
                } else {
                    MsgAllBean gmsg = msgDao.msgGetLastGroup4Uid(bean.getGid(), bean.getAcceptBeGroup().getInviter());
                    if (gmsg != null) {
                        inviterName = StringUtil.isNotNull(gmsg.getFrom_group_nickname()) ? gmsg.getFrom_group_nickname() : inviterName;
                    }

                    UserInfo userinfo = DaoUtil.findOne(UserInfo.class, "uid", bean.getAcceptBeGroup().getInviter());//查询昵称
                    if (userinfo != null) {

                        inviterName = StringUtil.isNotNull(userinfo.getMkName()) ? userinfo.getMkName() : inviterName;
                    }

                    inviterName = "\"<font id='" + bean.getAcceptBeGroup().getInviter() + "'  value ='4'>" + inviterName + "</font>\"";

                }
                //A邀请B加入群聊
                //B通过扫码A分享的二维码加入群聊
                String node = "";
                if (bean.getAcceptBeGroup().getJoinTypeValue() == 0) {//扫码
                    gNotice.setMsgType(1);
                    node = names + "通过扫" + inviterName + "分享的二维码加入了群聊" + "<div id='" + bean.getGid() + "'></div>";
                } else {//被邀请
                    gNotice.setMsgType(2);
                    node = inviterName + "邀请" + names + "加入了群聊" + "<div id='" + bean.getGid() + "'></div>";
                }

                // String way=bean.getAcceptBeGroup().getJoinTypeValue()==0?"通过xxx扫码":"通过xxx";
                gNotice.setNote(node);
                msgAllBean.setMsgNotice(gNotice);
                break;
            case DESTROY_GROUP://群解散
                msgAllBean.setGid(bean.getGid());
                msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                MsgNotice gdelNotice = new MsgNotice();
                gdelNotice.setMsgid(msgAllBean.getMsg_id());
                gdelNotice.setNote("该群已解散");
                msgAllBean.setMsgNotice(gdelNotice);

                break;
            case REMOVE_GROUP_MEMBER:
                //  ToastUtil.show(getApplicationContext(), "删除群成员");
                msgAllBean.setGid(bean.getRemoveGroupMember().getGid());
                msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                MsgNotice grmvNotice = new MsgNotice();

                grmvNotice.setMsgid(msgAllBean.getMsg_id());
                grmvNotice.setNote("你已被移出群");
                msgAllBean.setMsgNotice(grmvNotice);
                break;
            case CHANGE_GROUP_MASTER:
                msgAllBean.setGid(bean.getGid());
                msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                MsgNotice gnewAdminNotice = new MsgNotice();
                gnewAdminNotice.setMsgid(msgAllBean.getMsg_id());

                if (UserAction.getMyId() != null && bean.getChangeGroupMaster().getUid() == UserAction.getMyId().longValue()) {
                    gnewAdminNotice.setNote("你已成为新的群主");
                } else {
                    gnewAdminNotice.setMsgType(5);
                    gnewAdminNotice.setNote("\"<font color='#276baa' id='" + bean.getChangeGroupMaster().getUid() + "'>"
                            + bean.getChangeGroupMaster().getMembername() + "</font>\"" + "已成为新群主" + "<div id='" + bean.getGid() + "'></div>");
                }
                msgAllBean.setMsgNotice(gnewAdminNotice);
                break;
            case OUT_GROUP://退出群
//                String gid = bean.getOutGroup().getGid();
                String gid = bean.getGid();
                msgAllBean.setGid(gid);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                MsgNotice goutNotice = new MsgNotice();
                goutNotice.setMsgid(msgAllBean.getMsg_id());
                goutNotice.setMsgType(6);
                String name = bean.getNickname();
                UserInfo user = new UserDao().findUserInfo(bean.getFromUid());
                if (user != null && !TextUtils.isEmpty(user.getMkName())) {
                    name = user.getMkName();
                }
                if (TextUtils.isEmpty(name)) {
                    name = new MsgDao().getUsername4Show(gid, bean.getFromUid());
                }
                goutNotice.setNote("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name + "</font>\"" + "离开群聊" + "<div id='" + bean.getGid() + "'></div>");
                msgAllBean.setMsgNotice(goutNotice);
                break;
            case CHANGE_GROUP_META://修改群信息
                MsgBean.ChangeGroupMetaMessage.RealMsgCase realMsgCase = bean.getChangeGroupMeta().getRealMsgCase();
                switch (realMsgCase) {
                    case NAME://群名
                        msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                        MsgNotice info = new MsgNotice();
                        info.setMsgid(msgAllBean.getMsg_id());
                        bean.getChangeGroupMeta().getName();
                        info.setNote("新群名称:" + bean.getChangeGroupMeta().getName());
                        msgAllBean.setMsgNotice(info);
                        break;
                    case SHUT_UP:// 是否开启全群禁言
                    {
                        name = new MsgDao().getUsername4Show(bean.getGid(), bean.getFromUid());
                        StringBuffer stringBuffer1 = new StringBuffer();
                        msgAllBean.setGid(bean.getGid());
                        msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                        MsgNotice msgNotice1 = new MsgNotice();
                        msgNotice1.setMsgid(msgAllBean.getMsg_id());

                        EventGroupChange event = new EventGroupChange();
                        event.setNeedLoad(true);
                        if (bean.getChangeGroupMeta().getShutUp()) {
                            msgNotice1.setMsgType(ChatEnum.ENoticeType.FORBIDDEN_WORDS_OPEN);
                            stringBuffer1.append("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name + "</font>\"将全员禁言已打开");
                        } else {
                            msgNotice1.setMsgType(ChatEnum.ENoticeType.FORBIDDEN_WORDS_CLOSE);
                            stringBuffer1.append("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name + "</font>\"将全员禁言已关闭");
                        }
                        EventBus.getDefault().post(event);
                        msgNotice1.setNote(stringBuffer1 + "<div id='" + bean.getGid() + "'></div>");
                        msgAllBean.setMsgNotice(msgNotice1);
                    }
                    break;
                    case PROTECT_MEMBER://群成员保护
                        return null;
                    case AVATAR://群头像
                        //todo 刷新群成员头像
                        return null;
                }

                break;
            case AT:
                RealmList<Long> realmList = new RealmList<>();
                realmList.addAll(bean.getAt().getUidList());
                msgAllBean.setMsg_type(ChatEnum.EMessageType.AT);
                AtMessage atMessage = new AtMessage();
                atMessage.setMsgId(bean.getMsgId());
                atMessage.setMsg(bean.getAt().getMsg());
                atMessage.setAt_type(bean.getAt().getAtType().getNumber());
                atMessage.setUid(realmList);
                msgAllBean.setAtMessage(atMessage);
                break;
            case ASSISTANT:
                AssistantMessage assistant = new AssistantMessage();
                assistant.setMsg(bean.getAssistant().getMsg());
                assistant.setMsgId(bean.getMsgId());
                msgAllBean.setMsg_type(ChatEnum.EMessageType.ASSISTANT);
                msgAllBean.setAssistantMessage(assistant);
                break;
            case CANCEL://撤回消息
                String rname = "";
                MsgCancel msgCel = new MsgCancel();
                if (UserAction.getMyId() != null && bean.getFromUid() == UserAction.getMyId().longValue()) {
                    rname = "你";
                } else {//对方撤回的消息当通知处理
                    msgCel.setMsgType(9);
                    rname = "\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + msgDao.getUsername4Show(bean.getGid(), bean.getFromUid()) + "</font>\"" + "<div id='" + bean.getGid() + "'></div>";
                }
                msgAllBean.setMsg_type(ChatEnum.EMessageType.MSG_CANCEL);
                msgCel.setMsgid(msgAllBean.getMsg_id());
                msgCel.setNote(rname + "撤回了一条消息");
                msgCel.setMsgidCancel(bean.getCancel().getMsgId());
                // 查出本地数据库的消息
                MsgAllBean msgAllBean1 = msgDao.getMsgById(bean.getMsgId());
                if (msgAllBean1 != null) {
                    msgCel.setCancelContent(msgAllBean1.getMsgCancel().getCancelContent());
                    msgCel.setCancelContentType(msgAllBean1.getMsgCancel().getCancelContentType());
                }

                msgAllBean.setMsgCancel(msgCel);
                msgAllBean.setRead(1);
                LogUtil.getLog().i("撤回消息", bean.getMsgId() + "------" + bean.getSurvivalTime() + "-----");

                break;
            case P2P_AU_VIDEO:// 音视频消息
                P2PAuVideoMessage p2PAuVideoMessage = new P2PAuVideoMessage();
                p2PAuVideoMessage.setMsgId(msgAllBean.getMsg_id());
                p2PAuVideoMessage.setAv_type(bean.getP2PAuVideo().getAvTypeValue());
                p2PAuVideoMessage.setOperation(bean.getP2PAuVideo().getOperation());
                p2PAuVideoMessage.setDesc(bean.getP2PAuVideo().getDesc());
                msgAllBean.setP2PAuVideoMessage(p2PAuVideoMessage);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.MSG_VOICE_VIDEO);
                break;
            case CHANGE_SURVIVAL_TIME:
                String survivaNotice = "";
                if (bean.getChangeSurvivalTime().getSurvivalTime() == -1) {
                    if (TextUtils.isEmpty(bean.getGid())) {
                        survivaNotice = "\"" + bean.getNickname() + "\"" + "设置了退出即焚";
                    } else {
                        survivaNotice = "\"群主\"设置了退出即焚";
                    }
                } else if (bean.getChangeSurvivalTime().getSurvivalTime() == 0) {
                    if (TextUtils.isEmpty(bean.getGid())) {
                        survivaNotice = "\"" + bean.getNickname() + "\"" + "取消了阅后即焚";
                    } else {
                        survivaNotice = "\"群主\"取消了阅后即焚";
                    }
                } else {
                    if (TextUtils.isEmpty(bean.getGid())) {
                        survivaNotice = "\"" + bean.getNickname() + "\"" + "设置了消息" +
                                new ReadDestroyUtil().getDestroyTimeContent(bean.getChangeSurvivalTime().getSurvivalTime()) + "后消失";
                    } else {
                        survivaNotice = "\"群主\"设置了消息" +
                                new ReadDestroyUtil().getDestroyTimeContent(bean.getChangeSurvivalTime().getSurvivalTime()) + "后消失";
                    }
                }
                MsgCancel survivaMsgCel = new MsgCancel();
                survivaMsgCel.setMsgid(bean.getMsgId());
                survivaMsgCel.setNote(survivaNotice);
                msgAllBean.setMsgCancel(survivaMsgCel);

                ChangeSurvivalTimeMessage changeSurvivalTimeMessage = new ChangeSurvivalTimeMessage();
                changeSurvivalTimeMessage.setSurvival_time(bean.getChangeSurvivalTime().getSurvivalTime());
                changeSurvivalTimeMessage.setMsgid(bean.getMsgId());
                msgAllBean.setChangeSurvivalTimeMessage(changeSurvivalTimeMessage);

                msgAllBean.setMsg_type(ChatEnum.EMessageType.CHANGE_SURVIVAL_TIME);
                break;
            case P2P_AU_VIDEO_DIAL:// 点对点音视频发起通知
                P2PAuVideoDialMessage p2PAuVideoDialMessage = new P2PAuVideoDialMessage();
                p2PAuVideoDialMessage.setAv_type(bean.getP2PAuVideoDial().getAvTypeValue());
                msgAllBean.setP2PAuVideoDialMessage(p2PAuVideoDialMessage);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.MSG_VOICE_VIDEO_NOTICE);
                break;
            case BALANCE_ASSISTANT://零钱助手消息
                BalanceAssistantMessage balanceMessage = new BalanceAssistantMessage();
                balanceMessage.setMsgId(bean.getMsgId());
                balanceMessage.setTradeId(bean.getBalanceAssistant().getTradeId());
                balanceMessage.setAmount(bean.getBalanceAssistant().getAmt());
                balanceMessage.setDetailType(bean.getBalanceAssistant().getDetailTypeValue());
                balanceMessage.setTitle(bean.getBalanceAssistant().getTitle());
                balanceMessage.setTime(bean.getBalanceAssistant().getTime());
                balanceMessage.setAmountTitle(bean.getBalanceAssistant().getAmtLabel());
                String items = GsonUtils.optObject(bean.getBalanceAssistant().getItemList());
                if (!TextUtils.isEmpty(items)) {
                    balanceMessage.setItems(items);
                }
                msgAllBean.setBalanceAssistantMessage(balanceMessage);
                msgAllBean.setMsg_type(ChatEnum.EMessageType.BALANCE_ASSISTANT);
                break;
            case SNAPSHOT_LOCATION:// 地图位置
                LocationMessage locationMessage = new LocationMessage();
                locationMessage.setMsgId(msgAllBean.getMsg_id());
                locationMessage.setLatitude(bean.getSnapshotLocation().getLat());
                locationMessage.setLongitude(bean.getSnapshotLocation().getLon());
                locationMessage.setImg(bean.getSnapshotLocation().getImg());
                locationMessage.setAddress(bean.getSnapshotLocation().getAddr());
                locationMessage.setAddressDescribe(bean.getSnapshotLocation().getDesc());

                msgAllBean.setMsg_type(ChatEnum.EMessageType.LOCATION);
                msgAllBean.setLocationMessage(locationMessage);

//                LogUtil.getLog().e("====location==bean==="+ GsonUtils.optObject(bean));
//                LogUtil.getLog().e("====location==msgAllBean==="+ GsonUtils.optObject(msgAllBean));
                break;
            case CHANGE_VICE_ADMINS:// 管理员变更通知
                msgAllBean.setGid(bean.getGid());
                msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                MsgNotice changeViceAdminsNotice = new MsgNotice();
                changeViceAdminsNotice.setMsgid(msgAllBean.getMsg_id());

                StringBuffer stringBuffer = new StringBuffer();
                EventGroupChange event = new EventGroupChange();
                event.setNeedLoad(true);
                if (MsgBean.ChangeViceAdminsMessage.Opt.APPEND.getNumber() == bean.getChangeViceAdminsOrBuilder().getOptValue()) {// 新增
                    for (MsgBean.GroupNoticeMessage groupNotice : bean.getChangeViceAdminsOrBuilder().getMembersList()) {
                        if (UserAction.getMyId() != null && groupNotice.getUid() == UserAction.getMyId().longValue()) {
                            stringBuffer.append("\"<font color='#276baa' id='" + groupNotice.getUid() + "'>你</font>\"");
                        } else {
                            stringBuffer.append("\"<font color='#276baa' id='" + groupNotice.getUid() + "'>" + groupNotice.getNickname() + "</font>\"、");
                        }
                    }
                    stringBuffer.append("已成为管理员");
                    changeViceAdminsNotice.setMsgType(ChatEnum.ENoticeType.CHANGE_VICE_ADMINS_ADD);
                } else {
                    name = new MsgDao().getUsername4Show(bean.getGid(), bean.getFromUid());
                    stringBuffer.append("你已被\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name + "</font>\"取消管理员身份");
                    changeViceAdminsNotice.setMsgType(ChatEnum.ENoticeType.CHANGE_VICE_ADMINS_CANCLE);
                }
                EventBus.getDefault().post(event);
                changeViceAdminsNotice.setNote(stringBuffer + "<div id='" + bean.getGid() + "'></div>");
                msgAllBean.setMsgNotice(changeViceAdminsNotice);
                break;

            case SWITCH_CHANGE: //开关变更
                // TODO　处理老版本不兼容问题
                if (bean.getSwitchChange().getSwitchType() != MsgBean.SwitchChangeMessage.SwitchType.UNRECOGNIZED) {
                    int switchType = bean.getSwitchChange().getSwitchType().getNumber();
                    int switchValue = bean.getSwitchChange().getSwitchValue();// 禁言时间/秒
                    if (switchType == MsgBean.SwitchChangeMessage.SwitchType.SHUT_UP.getNumber()) {// 单人禁言
                        msgAllBean.setGid(bean.getGid());
                        msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                        MsgNotice msgNotice1 = new MsgNotice();
                        msgNotice1.setMsgid(msgAllBean.getMsg_id());
                        msgNotice1.setMsgType(ChatEnum.ENoticeType.FORBIDDEN_WORDS_SINGE);
                        StringBuffer sb = new StringBuffer();
                        name = new MsgDao().getUsername4Show(bean.getGid(), bean.getFromUid());

                        if (bean.getSwitchChange().getMembersList() != null && bean.getSwitchChange().getMembersList().size() > 0) {
                            MsgBean.GroupNoticeMessage message = bean.getSwitchChange().getMembers(0);
                            long uid = message.getUid();
                            if (switchValue == 0) {
                                if (UserAction.getMyId() != null && uid == UserAction.getMyId().longValue()) {
                                    sb.append("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name
                                            + "</font>\"" + "解除了\"<font color='#276baa' id='" + message.getUid() + "'>你</font>\"禁言");
                                } else {
                                    sb.append("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name
                                            + "</font>\"" + "解除了\"<font color='#276baa' id='" + message.getUid() + "'>" + message.getNickname() + "</font>\"禁言");
                                }
                            } else {
                                if (UserAction.getMyId() != null && uid == UserAction.getMyId().longValue()) {
                                    sb.append("你被\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name + "</font>\"禁言" + GroupMemPowerSetActivity.getSurvivaltime(switchValue));
                                } else {
                                    sb.append("\"<font color='#276baa' id='" + message.getUid() + "'>" + message.getNickname()
                                            + "</font>\"" + "被\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name + "</font>\"禁言" + GroupMemPowerSetActivity.getSurvivaltime(switchValue));
                                }
                            }
                            msgNotice1.setNote(sb + "<div id='" + bean.getGid() + "'></div>");
                            msgAllBean.setMsgNotice(msgNotice1);
                        }
                    } else if (switchType == MsgBean.SwitchChangeMessage.SwitchType.OPEN_UP_RED_ENVELOPER.getNumber()) {// 领取群红包
                        msgAllBean.setGid(bean.getGid());
                        msgAllBean.setMsg_type(ChatEnum.EMessageType.NOTICE);
                        MsgNotice msgNotice1 = new MsgNotice();
                        msgNotice1.setMsgid(msgAllBean.getMsg_id());
                        msgNotice1.setMsgType(ChatEnum.ENoticeType.OPEN_UP_RED_ENVELOPER);
                        StringBuffer sb = new StringBuffer();
                        name = new MsgDao().getUsername4Show(bean.getGid(), bean.getFromUid());

                        if (bean.getSwitchChange().getMembersList() != null && bean.getSwitchChange().getMembersList().size() > 0) {
                            MsgBean.GroupNoticeMessage message = bean.getSwitchChange().getMembers(0);
                            long uid = message.getUid();
                            if (switchValue == 0) {
                                if (UserAction.getMyId() != null && uid == UserAction.getMyId().longValue()) {
                                    sb.append("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name
                                            + "</font>\"" + "允许\"<font color='#276baa' id='" + message.getUid() + "'>你</font>\"在本群领取零钱红包");
                                } else {
                                    sb.append("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name
                                            + "</font>\"" + "允许\"<font color='#276baa' id='" + message.getUid() + "'>" + message.getNickname() + "</font>\"在本群领取零钱红包");
                                }
                            } else {
                                if (bean.getSwitchChange().getMembersList().size() == 1) {
                                    if (UserAction.getMyId() != null && uid == UserAction.getMyId().longValue()) {
                                        sb.append("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name
                                                + "</font>\"" + "已禁止\"<font color='#276baa' id='" + message.getUid() + "'>你</font>\"在本群领取零钱红包");
                                    } else {
                                        sb.append("\"<font color='#276baa' id='" + bean.getFromUid() + "'>" + name
                                                + "</font>\"" + "已禁止\"<font color='#276baa' id='" + message.getUid() + "'>" + message.getNickname() + "</font>\"在本群领取零钱红包");
                                    }
                                } else {
                                    for (MsgBean.GroupNoticeMessage groupNotice : bean.getSwitchChange().getMembersList()) {
                                        if (UserAction.getMyId() != null && groupNotice.getUid() == UserAction.getMyId().longValue()) {
                                            sb.append("\"<font color='#276baa' id='" + groupNotice.getUid() + "'>你</font>\"");
                                        } else {
                                            sb.append("\"<font color='#276baa' id='" + groupNotice.getUid() + "'>" + groupNotice.getNickname() + "</font>\"、");
                                        }
                                    }
                                    sb.append("已禁止在本群领取零钱红包");
                                }
                            }
                            msgNotice1.setNote(sb + "<div id='" + bean.getGid() + "'></div>");
                            msgAllBean.setMsgNotice(msgNotice1);
                        }


                    }
                }
                break;
            default://普通操作通知，不产生本地消息记录，直接return null
                return null;
        }

        return msgAllBean;
    }


}
