package com.example.nim_lib.server;

import net.cb.cb.library.bean.ReturnBean;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-11-01
 * @updateAuthor
 * @updateDate
 * @description 音视频接口
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public interface VideoServer {

    @POST("au-video/dial")
    @FormUrlEncoded
    Call<ReturnBean> auVideoDial(@Field("friend") Long friend, @Field("type") int type, @Field("roomId") String roomId);

    @POST("au-video/hang-up")
    @FormUrlEncoded
    Call<ReturnBean> auVideoHangup(@Field("friend") Long friend,@Field("type") int type,@Field("roomId") String roomId);

    @POST("au-video/keep-alive/{roomId}")
    @FormUrlEncoded
    Call<ReturnBean> auVideoKeepAlive(@Field("roomId") String roomId);
}
