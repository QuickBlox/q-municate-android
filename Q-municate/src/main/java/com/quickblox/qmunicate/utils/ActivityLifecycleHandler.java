package com.quickblox.qmunicate.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.quickblox.internal.core.helper.Lo;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.qb.commands.QBLoginAndJoinDialogsCommand;
import com.quickblox.qmunicate.qb.commands.QBLogoutAndDestroyChatCommand;
import com.quickblox.qmunicate.ui.base.QBLogeable;

public class ActivityLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private static final boolean SHOULD_START_MULTICHAT = true;
    private int numberOfActivitiesInForeground;
    private boolean chatDestroyed = false;
    Lo lo = new Lo(this);

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        lo.g("onActivityResumed" + numberOfActivitiesInForeground);
        if (numberOfActivitiesInForeground == 0 && chatDestroyed) {
            QBLoginAndJoinDialogsCommand.start(activity);
        }
        ++numberOfActivitiesInForeground;
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivityStopped(Activity activity) {
        --numberOfActivitiesInForeground;
        lo.g("onActivityStopped" + numberOfActivitiesInForeground);
        boolean isLogined = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_IS_LOGINED, false);
        if (numberOfActivitiesInForeground == 0 && isLogined && activity instanceof QBLogeable) {
            chatDestroyed = ((QBLogeable) activity).isCanPerformLogoutInOnStop();
            if (chatDestroyed) {
                QBLogoutAndDestroyChatCommand.start(activity);
            }
            // TODO SF app was killed.
            //android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }
}