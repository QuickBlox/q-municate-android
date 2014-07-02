package com.quickblox.qmunicate.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.quickblox.internal.core.helper.Lo;
import com.quickblox.qmunicate.model.AppSession;
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
        //Count only our app logeable activity
        boolean activityLogeable = isActivityLogeable(activity);
        if (activityLogeable){
            ++numberOfActivitiesInForeground;
        }
        if (numberOfActivitiesInForeground == 0 && chatDestroyed && activityLogeable) {
            boolean canLogin = chatDestroyed && AppSession.getSession().isSessionExist();
            if (canLogin) {
                QBLoginAndJoinDialogsCommand.start(activity);
            }
        }
    }

    public boolean isActivityLogeable(Activity activity){
        return (activity instanceof QBLogeable);
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivityStopped(Activity activity) {
        //Count only our app logeable activity
        if (activity instanceof QBLogeable) {
            --numberOfActivitiesInForeground;
        }
        lo.g("onActivityStopped" + numberOfActivitiesInForeground);

        if (numberOfActivitiesInForeground == 0 && activity instanceof QBLogeable) {
            boolean isLogedIn = isLogedIn();
            if (!isLogedIn) {
                return;
            }
            chatDestroyed = ((QBLogeable) activity).isCanPerformLogoutInOnStop();
            if (chatDestroyed) {
                QBLogoutAndDestroyChatCommand.start(activity);
            }
            // TODO SF app was killed.
            //android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    private boolean isLogedIn() {
        AppSession activeSession = AppSession.getSession();
        return activeSession.isSessionExist();
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }
}