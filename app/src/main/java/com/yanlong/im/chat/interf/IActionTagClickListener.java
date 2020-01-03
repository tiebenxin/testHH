package com.yanlong.im.chat.interf;

/**
 * Created by LL130386 on 2018/5/16.
 */

public interface IActionTagClickListener {

    //用户
    void clickUser(String userId);


    //红包或者转账
    void clickEnvelope(String rid);

}
