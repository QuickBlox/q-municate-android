package com.quickblox.qmunicate.utils;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class OnlineStatusHelper {

    public static boolean isOnline(Date lastRequestAt) {
        if (lastRequestAt != null) {
            long timeout = new Date().getTime() - lastRequestAt.getTime();
            return TimeUnit.MILLISECONDS.toMinutes(timeout) < Consts.FL_ONLINE_STATUS_TIMEOUT;
        }
        return false;
    }

    public static String getOnlineStatus(Date lastRequestAt) {
        if (isOnline(lastRequestAt)) {
            return App.getInstance().getString(R.string.frl_online);
        } else {
            return App.getInstance().getString(R.string.frl_offline);
        }
    }
}
