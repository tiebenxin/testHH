package com.yanlong.im.chat.ui.forward;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description
 */
public interface IForwardListener {
    /*
     * @param uid:对方用户id
     * @param gid:若是群聊，群id
     * @param avatar:对方头像
     * @param nick:对方昵称
     * */
    void onForward(long uid, String gid, String avatar, String nick);
}
