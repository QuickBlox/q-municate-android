package com.quickblox.qmunicate.ui.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class OnlineStatusHelper {

    private static final int ONLINE_TIMEOUT = 15;
    private static final String ONLINE_STATUS = "Online";
    private static final String OFFLINE_STATUS = "Online";

    public static boolean isOnline(Date lastRequestAt) {
        long timeout = new Date().getTime() - lastRequestAt.getTime();
        return TimeUnit.MILLISECONDS.toMinutes(timeout) < ONLINE_TIMEOUT;
    }

    public static String getOnlineStatus(Date lastRequestAt) {
        if (isOnline(lastRequestAt)) {
            return ONLINE_STATUS;
        } else {
            return OFFLINE_STATUS;
        }
    }
}
