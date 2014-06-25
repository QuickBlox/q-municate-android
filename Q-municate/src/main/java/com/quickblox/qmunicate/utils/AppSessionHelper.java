package com.quickblox.qmunicate.utils;

import com.quickblox.qmunicate.model.AppSession;

public class AppSessionHelper {

    public static AppSession getSession() {
        AppSession activeSession = AppSession.getActiveSession();
        if (activeSession == null) {
            activeSession = AppSession.load();
        }
        return activeSession;
    }
}
