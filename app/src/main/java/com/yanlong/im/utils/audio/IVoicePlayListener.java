package com.yanlong.im.utils.audio;

import com.yanlong.im.chat.bean.MsgAllBean;

/**
 * @author Liszt
 * @date 2019/8/23
 * Description
 */
public interface IVoicePlayListener {

    void onStart(MsgAllBean bean);

    void onStop(MsgAllBean bean);

    void onComplete(MsgAllBean bean);

//    void onReadyToNext();
}
