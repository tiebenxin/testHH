/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * <p>
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * <p>
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package net.cb.cb.library.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.EventNetStatus;
import net.cb.cb.library.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;


/**
 * 网络状态变化广播接受者.
 */
public class NetworkReceiver extends BroadcastReceiver {

    private EventNetStatus netStatusEvent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            return;
        }
        NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        if (networkInfo != null && NetWorkUtils.getNetworkType() != 0) {
            updateNetStatus(CoreEnum.ENetStatus.SUCCESS_ON_NET);
            LogUtil.getLog().i("NetworkReceiver", "有网络了");

        } else {
            updateNetStatus(CoreEnum.ENetStatus.ERROR_ON_NET);
            LogUtil.getLog().i("NetworkReceiver", "无网络了");
        }
    }

    private void updateNetStatus(@CoreEnum.ENetStatus int status) {
        if (netStatusEvent == null) {
            netStatusEvent = new EventNetStatus(status);
        } else {
            netStatusEvent.setStatus(status);
        }
        EventBus.getDefault().post(netStatusEvent);
    }


}