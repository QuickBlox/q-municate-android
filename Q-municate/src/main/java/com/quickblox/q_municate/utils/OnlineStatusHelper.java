package com.quickblox.q_municate.utils;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;

public class OnlineStatusHelper {

    public static String getOnlineStatus(boolean online) {
        if (online) {
            return App.getInstance().getString(R.string.frl_online);
        } else {
            return App.getInstance().getString(R.string.frl_offline);
        }
    }
}
