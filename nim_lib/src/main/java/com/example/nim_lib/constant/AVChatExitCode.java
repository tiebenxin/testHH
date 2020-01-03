package com.example.nim_lib.constant;

/**
 * Created by hzxuwen on 2015/4/24.
 */
public class AVChatExitCode {
    public static final int PEER_HANGUP = 0;

    public static final int PEER_REJECT = 1;

    public static final int HANGUP = 2;

    public static final int NET_CHANGE = 4;

    public static final int REJECT = 5;

    public static final int PEER_BUSY = 6;

    public static final int NET_ERROR = 8;

    public static final int KICKED_OUT = 9;

    public static final int CONFIG_ERROR = 10;

    public static final int PROTOCOL_INCOMPATIBLE_SELF_LOWER = 12;

    public static final int PROTOCOL_INCOMPATIBLE_PEER_LOWER = 13;

    public static final int INVALIDE_CHANNELID = 14;

    public static final int OPEN_DEVICE_ERROR = 15;

    public static final int SYNC_REJECT = 16;

    public static final int SYNC_ACCEPT = 17;

    public static final int SYNC_HANGUP = 18;

    public static final int PEER_NO_RESPONSE = 19; //超时，无人接听

    public static final int CANCEL = 20; //取消

    public static final int LOCAL_CALL_BUSY = 21; // 正在进行本地通话

    public static final int INTERRUPT = 22; // 通话中断

    public static final int OTHER_CANCEL = 23; // 对方已取消

    public static final int CODE_201 = 201;//	客户端版本不对，需升级sdk
    public static final int CODE_301 = 301;//	被封禁
    public static final int CODE_302 = 302;//	用户名或密码错误
    public static final int CODE_315 = 315;//	IP限制
    public static final int CODE_403 = 403;//	非法操作或没有权限
    public static final int CODE_404 = 404;//	对象不存在
    public static final int CODE_405 = 405;//	参数长度过长
    public static final int CODE_406 = 406;//	对象只读
    public static final int CODE_408 = 408;//	客户端请求超时
    public static final int CODE_413 = 413;//	验证失败(短信服务)
    public static final int CODE_414 = 414;//	参数错误
    public static final int CODE_415 = 415;//	客户端网络问题
    public static final int CODE_416 = 416;//	频率控制
    public static final int CODE_417 = 417;//	重复操作
    public static final int CODE_418 = 418;//	通道不可用(短信服务)
    public static final int CODE_419 = 419;//	数量超过上限
    public static final int CODE_422 = 422;//	账号被禁用
    public static final int CODE_431 = 431;//	HTTP重复请求
    public static final int CODE_500 = 500;//	服务器内部错误
    public static final int CODE_503 = 503;//	服务器繁忙
    public static final int CODE_508 = 508;//	消息撤回时间超限
    public static final int CODE_509 = 509;//	无效协议
    public static final int CODE_514 = 514;//	服务不可用
    public static final int CODE_998 = 998;//	解包错误
    public static final int CODE_999 = 999;//	打包错误

    public static final int CODE_9102 = 9102;//	通道失效
    public static final int CODE_9103 = 9103;//	已经在他端对这个呼叫响应过了
    public static final int CODE_11001 = 11001;// 通话不可达，对方离线状态

    public static String getCodeString(int code) {
        switch (code) {
            case CODE_201:
                return "客户端版本不对，需升级sdk";
            case CODE_301:
                return "被封禁";
            case CODE_302:
                return "用户名或密码错误";
            case CODE_315:
                return "IP限制";
            case CODE_403:
                return "非法操作或没有权限";
            case CODE_404:
                return "对象不存在";
            case CODE_405:
                return "参数长度过长";
            case CODE_406:
                return "对象只读";
            case CODE_408:
                return "客户端请求超时";
            case CODE_413:
                return "验证失败(短信服务)";
            case CODE_414:
                return "参数错误";
            case CODE_415:
                return "客户端网络问题";
            case CODE_416:
                return "频率控制";
            case CODE_417:
                return "重复操作";
            case CODE_418:
                return "通道不可用(短信服务)";
            case CODE_419:
                return "数量超过上限";
            case CODE_422:
                return "账号被禁用";
            case CODE_431:
                return "HTTP重复请求";
            case CODE_500:
                return "服务器内部错误";
            case CODE_503:
                return "服务器繁忙";
            case CODE_508:
                return "消息撤回时间超限";
            case CODE_509:
                return "无效协议";
            case CODE_514:
                return "服务不可用";
            case CODE_998:
                return "解包错误";
            case CODE_999:
                return "打包错误";
            case CODE_9102:
                return "通道失效";
            case CODE_9103:
                return "已经在他端对这个呼叫响应过了";
            case CODE_11001:
                return "通话不可达，对方离线状态";
            default:
                return "服务异常";
        }
    }

    public static String getExitString(int code) {
        switch (code) {
            case PEER_HANGUP:
                return "PEER_HANGUP";
            case PEER_REJECT:
                return "PEER_REJECT";
            case HANGUP:
                return "HANGUP";
            case NET_CHANGE:
                return "NET_CHANGE";
            case REJECT:
                return "REJECT";
            case PEER_BUSY:
                return "PEER_BUSY";
            case NET_ERROR:
                return "NET_ERROR";
            case KICKED_OUT:
                return "KICKED_OUT";
            case CONFIG_ERROR:
                return "CONFIG_ERROR";
            case PROTOCOL_INCOMPATIBLE_SELF_LOWER:
                return "PROTOCOL_INCOMPATIBLE_SELF_LOWER";
            case PROTOCOL_INCOMPATIBLE_PEER_LOWER:
                return "PROTOCOL_INCOMPATIBLE_PEER_LOWER";
            case INVALIDE_CHANNELID:
                return "INVALIDE_CHANNELID";
            case OPEN_DEVICE_ERROR:
                return "OPEN_DEVICE_ERROR";
            case SYNC_REJECT:
                return "SYNC_REJECT";
            case SYNC_ACCEPT:
                return "SYNC_ACCEPT";
            case SYNC_HANGUP:
                return "SYNC_HANGUP";
            case CANCEL:
                return "CANCEL";
            case PEER_NO_RESPONSE:
                return "PEER_NO_RESPONSE";
            case LOCAL_CALL_BUSY:
                return "LOCAL_CALL_BUSY";
            default:
                return "UNKNOWN";
        }
    }

}
