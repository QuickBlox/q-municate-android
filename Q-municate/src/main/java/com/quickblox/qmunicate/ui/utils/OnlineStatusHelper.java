package com.quickblox.qmunicate.ui.utils;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class OnlineStatusHelper {

    private static final int ONLINE_TIMEOUT = 15;

    public static boolean isOnline(Date lastRequestAt) {
        long timeout = new Date().getTime() - lastRequestAt.getTime();
        return TimeUnit.MILLISECONDS.toMinutes(timeout) < ONLINE_TIMEOUT;
    }

    public static String getOnlineStatus(Date lastRequestAt) {
        if (isOnline(lastRequestAt)) {
            return App.getInstance().getString(R.string.frl_online);
        } else {
            return App.getInstance().getString(R.string.frl_offline);
        }
    }
}
