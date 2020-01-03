package com.example.nim_lib.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-10-17
 * @updateAuthor
 * @updateDate
 * @description 来电状态监听
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            PhoneCallStateObserver.getInstance().onCallStateChanged(state);
        }
    }
}
