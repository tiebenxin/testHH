package com.yanlong.im.chat.server;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.utils.socket.MsgBean;
import com.yanlong.im.utils.socket.SocketEvent;
import com.yanlong.im.utils.socket.SocketUtil;

import net.cb.cb.library.bean.EventRefreshChat;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.NetUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 * 聊天服务
 */
public class ChatServer extends Service {
    private static final String TAG = "ChatServer";
    private MsgDao msgDao = new MsgDao();

    //撤回消息
    private static Map<String, MsgAllBean> cancelList = new ConcurrentHashMap<>();

    public static Map<String, MsgAllBean> getCancelList() {
        return cancelList;
    }

    /***
     * 添加测试消息
     * @param msg_id 返回的消息id
     * @param msgBean 要撤回的消息
     */
    public static void addCanceLsit(String msg_id, MsgAllBean msgBean) {
        cancelList.put(msg_id, msgBean);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private SocketEvent msgEvent = new SocketEvent() {
        @Override
        public void onHeartbeat() {

        }

        @Override
        public void onACK(MsgBean.AckMessage bean) {

            for (String msgid : bean.getMsgIdList()) {
                //处理撤回消息
                if (cancelList.containsKey(msgid)) {
                    MsgAllBean msgAllBean = cancelList.get(msgid);
                    msgDao.msgDel4Cancel(msgid, msgAllBean.getMsgCancel().getMsgidCancel());

                    LogUtil.getLog().i(TAG, "onACK: 收到取消回执,手动刷新列表");
                    EventBus.getDefault().post(new EventRefreshChat());
                    cancelList.remove(msgid);
                }
            }
        }

        @Override
        public void onMsg(MsgBean.UniversalMessage bean) {

        }

        @Override
        public void onSendMsgFailure(MsgBean.UniversalMessage.Builder bean) {
        }

        @Override
        public void onLine(boolean state) {

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        LogUtil.getLog().e(TAG, ">>>启动socket,服务已经开启-----------------------------------");

        taskFixSendstate();

        SocketUtil.getSocketUtil().startSocket();


        return super.onStartCommand(intent, flags, startId);
    }

    /***
     * 修改发送状态
     */
    private void taskFixSendstate() {
        msgDao.msgSendStateToFail();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SocketUtil.getSocketUtil().removeEvent(msgEvent);
        SocketUtil.getSocketUtil().endSocket();
        unregisterReceiver(mNetworkChangeReceiver);
        LogUtil.getLog().e(TAG, ">>>>>网路状态取消,服务已经关闭-----------------------------------");
    }

    protected BroadcastReceiver mNetworkChangeReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.getLog().d(TAG, ">>>>>网路状态监听");
        SocketUtil.getSocketUtil().addEvent(msgEvent, 0);
        //注册广播用于监听网络状态改变
        mNetworkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.getLog().d(TAG, ">>>>>网路状态改变" + NetUtil.isNetworkConnected());
                if (NetUtil.isNetworkConnected()) {//链接成功
                    onStartCommand(null, 0, 0);
                } else {//链接失败
                    SocketUtil.getSocketUtil().stop(true);

                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mNetworkChangeReceiver, intentFilter);

    }
}
