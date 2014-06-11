package com.quickblox.qmunicate.utils;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;

public class OnlineStatusHelper {

    public static String getOnlineStatus(boolean online) {
        if (online) {
            return App.getInstance().getString(R.string.frl_online);
        } else {
            return App.getInstance().getString(R.string.frl_offline);
        }
    }
}
