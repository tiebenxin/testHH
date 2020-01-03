package net.cb.cb.library.event;

import net.cb.cb.library.CoreEnum;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-11-04
 * @updateAuthor
 * @updateDate
 * @description 事件工厂
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class EventFactory extends BaseEvent {
    /**
     * 重启APP
     */
    public static class RestartAppEvent extends BaseEvent {

    }

    /**
     * 撤回消息关闭图片预览
     */
    public static class ClosePictureEvent extends BaseEvent {
        public String msg_id;
        public String name;
    }

    /**
     * 撤回消息关闭语音播放
     */
    public static class StopVoiceeEvent extends BaseEvent {
        public String msg_id;
    }

    /**
     * 撤回视频
     */
    public static class StopVideoEvent extends BaseEvent {
        public String msg_id;
        public String name;
    }

    /**
     * 刷新用户信息
     */
    public static class FreshUserStateEvent extends BaseEvent {
        public String vip;// (0:普通|1:vip)
    }

    /**
     * 语音最小化
     */
    public static class VoiceMinimizeEvent extends BaseEvent {
        public int type;
        public int passedTime;
        public String showTime;
        public boolean isCallEstablished;// 是否接听
    }

    /**
     * 关闭语音最小化并发送一条消息
     */
    public static class CloseVoiceMinimizeEvent extends BaseEvent {
        public String operation;// 操作(cancel|hangup|reject)
        public String txt;// 操作加时长
        public int avChatType;// 语音、视频
        public Long toUId;
        public String toGid;
    }

    /**
     * 关闭语音最小化
     */
    public static class CloseMinimizeEvent extends BaseEvent {

        public boolean isClose=true;
    }

    /**
     * 退出登录时关闭音视频界面
     */
    public static class CloseVideoActivityEvent extends BaseEvent {
    }

    /**
     * 发送一条通知
     */
    public static class SendP2PAuVideoDialMessage extends BaseEvent {
        public int avChatType;// 语音、视频
        public Long toUId;
        public String toGid;
    }

    /**
     * 语音最小化
     */
    public static class ShowVoiceMinimizeEvent extends BaseEvent {
        public boolean isStartRunThread=true;
    }

    /**
     * 开启音视频界面
     */
    public static class VideoActivityEvent extends BaseEvent {
    }

    /**
     * 停止极光推送铃声
     */
    public static class StopJPushResumeEvent extends BaseEvent {
    }

    /**
     * 禁言提示
     */
    public static class ToastEvent extends BaseEvent {
        public String value;
    }

    /**
     * 网络监听
     */
    public static class EventNetStatus {
        @CoreEnum.ENetStatus
        private int status;

        public EventNetStatus(@CoreEnum.ENetStatus int value) {
            status = value;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
