package com.yanlong.im.chat.server;

import com.yanlong.im.chat.bean.ExitGroupUser;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.GroupJoinBean;
import com.yanlong.im.chat.bean.NoRedEnvelopesBean;
import com.yanlong.im.chat.bean.RobotInfoBean;

import net.cb.cb.library.bean.ReturnBean;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface MsgServer {
    @POST("/group/create")
    @FormUrlEncoded
    Call<ReturnBean<Group>> groupCreate(@Field("nickname") String nickname, @Field("groupName") String groupName,
                                        @Field("avatar") String avatar, @Field("@members") String membersJson);

    @POST("/group/quit")
    @FormUrlEncoded
    Call<ReturnBean> groupQuit(@Field("gid") String gid, @Field("nickname") String nickname);

    @POST("/group/remove-members")
    @FormUrlEncoded
    Call<ReturnBean> groupRemove(@Field("gid") String gid, @Field("@members") String membersJson);

    @POST("/group/append-members")
    @FormUrlEncoded
    Call<ReturnBean> groupAdd(@Field("gid") String gid, @Field("@members") String membersJson, @Field("nickname") String nickname);

    @POST("/group/quit")
    @FormUrlEncoded
    Call<ReturnBean> groupDestroy(@Field("gid") String gid);

    @POST("/group/get-group-data")
    @FormUrlEncoded
    Call<ReturnBean<Group>> groupInfo(@Field("gid") String gid);

    @POST("/group/change-member-switch")
    @FormUrlEncoded
    Call<ReturnBean> groupSwitch(@Field("gid") String gid, @Field("toTop") Integer isTop, @Field("notNotify") Integer notNotify, @Field("saved") Integer saved);

    @POST("/group/change-group-switch")
    @FormUrlEncoded
    Call<ReturnBean> groupSwitch(@Field("gid") String gid, @Field("needVerification") Integer needVerification);


    @POST("/group/change-group-switch")
    @FormUrlEncoded
    Call<ReturnBean> groupSwitchIntimately(@Field("gid") String gid, @Field("contactIntimately") Integer contactIntimately);

    @POST("/group/change-group-switch")
    @FormUrlEncoded
    Call<ReturnBean> setAllForbiddenWords(@Field("gid") String gid, @Field("wordsNotAllowed") Integer wordsNotAllowed);

    @POST("group/get-my-saved")
    Call<ReturnBean<List<Group>>> getMySaved();

    @POST("/friends/set-friend-disturb")
    @FormUrlEncoded
    Call<ReturnBean> friendMute(@Field("friend") Long uid, @Field("disturb") Integer isMute);

    @POST("/friends/set-friend-top")
    @FormUrlEncoded
    Call<ReturnBean> friendTop(@Field("friend") Long uid, @Field("istop") Integer istop);

    @POST("/group/request-join")
    @FormUrlEncoded
    Call<ReturnBean<GroupJoinBean>> joinGroup(@Field("gid") String gid, @Field("uid") Long uid,
                                              @Field("nickname") String nickname, @Field("avatar") String avatar,
                                              @Field("inviter") String inviter, @Field("inviterName") String inviterName);

    @POST("/group/change-group-name")
    @FormUrlEncoded
    Call<ReturnBean> changeGroupName(@Field("gid") String gid, @Field("name") String name);

    @POST("/group/change-member-name")
    @FormUrlEncoded
    Call<ReturnBean> changeMemberName(@Field("gid") String gid, @Field("name") String name);

    @POST("/group/change-group-avatar")
    @FormUrlEncoded
    Call<ReturnBean> groupHeadSet(@Field("gid") String gid, @Field("avatar") String avatar);

    @POST("/group-ceiling/add")
    @FormUrlEncoded
    Call<ReturnBean> groupHeadLimit(@Field("gid") String gid, @Field("description") String description, @Field("masterPhone") String masterPhone);

    @POST("/group/accept-request")
    @FormUrlEncoded
    Call<ReturnBean> groupRequest(@Field("gid") String gid, @Field("newMember") String newMember,
                                  @Field("newMemberName") String newMemberName, @Field("newMemberAvatar") String newMemberAvatar,
                                  @Field("joinType") Integer joinType, @Field("inviter") String inviter,
                                  @Field("inviterName") String inviterName);

    @POST("/group/edit-announcement")
    @FormUrlEncoded
    Call<ReturnBean> changeGroupAnnouncement(@Field("gid") String gid, @Field("announcement") String announcement, @Field("masterName") String masterName);

    @POST("/group/search-for-robots")
    @FormUrlEncoded
    Call<ReturnBean<List<RobotInfoBean>>> robotSearch(@Field("keyword") String keyword);

    @POST("/group/change-robot")
    @FormUrlEncoded
    Call<ReturnBean> robotChange(@Field("gid") String gid, @Field("robotid") String robotid);

    @POST("/group/get-robot-detail")
    @FormUrlEncoded
    Call<ReturnBean<RobotInfoBean>> robotInfo(@Field("robotid") String robotid, @Field("gid") String gid);

    @POST("/group/change-master")
    @FormUrlEncoded
    Call<ReturnBean> changeMaster(@Field("gid") String gid, @Field("uid") String uid, @Field("membername") String membername);

    @POST("/friends/set-survival-time")
    @FormUrlEncoded
    Call<ReturnBean> setSurvivalTime(@Field("friend") long friend, @Field("survivalTime") int survivalTime);

    @POST("/group/change-survival-time")
    @FormUrlEncoded
    Call<ReturnBean> changeSurvivalTime(@Field("gid") String gid, @Field("survivalTime") int survivalTime);

    @POST("/group/get-batch-group")
    @FormUrlEncoded
    Call<ReturnBean<List<Group>>> getGroupsByIds(@Field("@gids") String json);

    @POST("/group/change-admins")
    @FormUrlEncoded
    Call<ReturnBean> groupChangeAdmins(@Field("@admins") String adminsJson,@Field("gid") String gid,@Field("opt") int opt);

    @POST("/group/get-gone-members")
    @FormUrlEncoded
    Call<ReturnBean<List<ExitGroupUser>>> exitGroupList(@Field("gid") String gid);

    @POST("/group/toggle-open-up-red-envelope")
    @FormUrlEncoded
    Call<ReturnBean> toggleOpenUpRedEnvelope(@Field("@uidList") String uidJson,@Field("gid") String gid,@Field("ops") int ops);

    @POST("/group/get-cant-open-up-red-envelope-members")
    @FormUrlEncoded
    Call<ReturnBean<List<NoRedEnvelopesBean>>> getCantOpenUpRedMembers(@Field("gid") String gid);

    @POST("/group/toggle-words-not-allowed")
    @FormUrlEncoded
    Call<ReturnBean> toggleWordsNotAllowed(@Field("@uidList") String uidJson,@Field("gid") String gid,@Field("duration") int duration);

}
