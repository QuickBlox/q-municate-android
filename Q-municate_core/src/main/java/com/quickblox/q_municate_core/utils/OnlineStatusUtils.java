package com.quickblox.q_municate_core.utils;

import android.content.Context;

import com.quickblox.q_municate_core.R;

public class OnlineStatusUtils {

    public static int getOnlineStatus(boolean online) {
        if (online) {
            return R.string.frl_online;
        } else {
            return R.string.frl_offline;
        }
    }

    public static String getOnlineStatus(Context context, boolean online, String offlineStatus) {
        if (online) {
            return context.getString(R.string.frl_online);
        } else {
            return offlineStatus;
        }
    }
}