package com.quickblox.qmunicate.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.quickblox.qmunicate.qb.commands.QBLoginChatCommand;
import com.quickblox.qmunicate.qb.commands.QBLogoutChatCommand;

public class ActivityLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private int numberOfActivitiesInForeground;

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        if (numberOfActivitiesInForeground == 0) {
            QBLoginChatCommand.start(activity);
        }
        ++numberOfActivitiesInForeground;

    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityStopped(Activity activity) {
        --numberOfActivitiesInForeground;
        if (numberOfActivitiesInForeground == 0) {
            QBLogoutChatCommand.start(activity);
        }
    }
}
