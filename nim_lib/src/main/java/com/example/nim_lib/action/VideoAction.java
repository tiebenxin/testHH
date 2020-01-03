package com.example.nim_lib.action;


import com.example.nim_lib.server.VideoServer;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.NetIntrtceptor;
import net.cb.cb.library.utils.NetUtil;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-11-01
 * @updateAuthor
 * @updateDate
 * @description
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class VideoAction {

    private VideoServer videoServer;
    private String TAG = VideoAction.class.getName();

    public VideoAction(String token) {
        videoServer = NetUtil.getNet().create(VideoServer.class);
        //设置token
        NetIntrtceptor.headers = Headers.of("X-Access-Token", token);
    }

    /**
     * 点对点语音发起(已完成)
     *
     * @param friend   通话接收人uid
     * @param type     通话类型(1:音频|2:视频)
     * @param roomId   网易房间id
     * @param callBack
     */
    public void auVideoDial(Long friend, int type, String roomId, final CallBack<ReturnBean> callBack) {
        NetUtil.getNet().exec(videoServer.auVideoDial(friend, type, roomId), new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                LogUtil.getLog().i(TAG,"点对点语音发起(已完成)");
                callBack.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                super.onFailure(call, t);
                LogUtil.getLog().i(TAG,"点对点语音发起(失败)");
                callBack.onFailure(call, t);
            }
        });
    }

    /**
     * 点对点语音挂断(已完成)
     *
     * @param friend   通话接收人uid
     * @param type     通话类型(1:音频|2:视频)
     * @param roomId   网易房间id
     * @param callBack
     */
    public void auVideoHandup(Long friend, int type, String roomId, final CallBack<ReturnBean> callBack) {
        NetUtil.getNet().exec(videoServer.auVideoHangup(friend, type, roomId), new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                LogUtil.getLog().i(TAG,"点对点语音挂断(已完成)");
                callBack.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                super.onFailure(call, t);
                LogUtil.getLog().i(TAG,"点对点语音挂断(失败)");
                callBack.onFailure(call, t);
            }
        });
    }

    /**
     * 音视频状态保活(已完成)
     *
     * @param roomId   网易房间id
     * @param callBack
     */
    public void auVideoKeepAlive(String roomId, final CallBack<ReturnBean> callBack) {
        NetUtil.getNet().exec(videoServer.auVideoKeepAlive(roomId), new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                LogUtil.getLog().i(TAG,"音视频状态保活(已完成)");
                callBack.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                super.onFailure(call, t);
                LogUtil.getLog().i(TAG,"音视频状态保活(失败)");
                callBack.onFailure(call, t);
            }
        });
    }
}
