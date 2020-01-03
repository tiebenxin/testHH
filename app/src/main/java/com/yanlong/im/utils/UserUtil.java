package com.yanlong.im.utils;

import com.yanlong.im.user.bean.FriendInfoBean;
import com.yanlong.im.user.bean.UserInfo;

import net.cb.cb.library.manager.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-10-08
 * @updateAuthor
 * @updateDate
 * @description 用户处理类
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class UserUtil {

    /**
     * 获取用户的首字母列表
     *
     * @param userList 用户列表
     * @return
     */
    public static List<String> userParseString(List<UserInfo> userList) {
        List<String> list = new ArrayList<>();
        try {
            if (userList != null) {
                for (int i = 0; i < userList.size(); i++) {
                    list.add(userList.get(i).getTag());
                }
            }
        } catch (Exception e) {

        }
        return list;

    }

    /**
     * 获取用户的首字母列表
     *
     * @param friendList 用户列表
     * @return
     */
    public static List<String> friendParseString(List<FriendInfoBean> friendList) {
        List<String> list = new ArrayList<>();
        try {
            if (friendList != null) {
                for (int i = 0; i < friendList.size(); i++) {
                    list.add(friendList.get(i).getTag());
                }
            }
        } catch (Exception e) {

        }
        return list;

    }

    /**
     * 是否是常信客服、常信小助手
     *
     * @return
     */
    public static boolean isSystemUser(Long toUId) {
        if (toUId == null) {
            return false;
        }
        if (Constants.CX_HELPER_UID.equals(toUId) || Constants.CX888_UID.equals(toUId) ||  Constants.CX999_UID.equals(toUId)) {
            return true;
        }
        return false;
    }

}
