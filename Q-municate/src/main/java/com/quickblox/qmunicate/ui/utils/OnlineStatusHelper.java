package com.quickblox.qmunicate.ui.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class OnlineStatusHelper {

    private static final int ONLINE_TIMEOUT = 15;
    private static final String ONLINE_STATUS = "Online";

    public static boolean isOnline(Date lastRequestAt) {
        long timeout = new Date().getTime() - lastRequestAt.getTime();
        return TimeUnit.MILLISECONDS.toMinutes(timeout) < ONLINE_TIMEOUT;
    }
}
