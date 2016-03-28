package com.quickblox.q_municate.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.quickblox.core.helper.Lo;
import com.quickblox.chat.QBChatService;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLogoutAndDestroyChatCommand;

public class ActivityLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private int numberOfActivitiesInForeground;
    private boolean chatDestroyed = false;

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        Lo.g("onActivityResumed" + numberOfActivitiesInForeground);
        //Count only our app logeable activity
        boolean activityLogeable = isActivityLogeable(activity);
        chatDestroyed = chatDestroyed && !isLoggedIn();
        if (numberOfActivitiesInForeground == 0 && chatDestroyed && activityLogeable) {
            boolean canLogin = chatDestroyed && AppSession.getSession().isSessionExist();
            if (canLogin && ((BaseActivity) activity).isNetworkAvailable()) {
                QBLoginChatCompositeCommand.start(activity);
            }
        }
        if (activityLogeable) {
            ++numberOfActivitiesInForeground;
        }
    }

    public boolean isActivityLogeable(Activity activity) {
        return (activity instanceof Loggable);
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivityStopped(Activity activity) {
        //Count only our app logeable activity
        if (activity instanceof Loggable) {
            --numberOfActivitiesInForeground;
        }
        Lo.g("onActivityStopped" + numberOfActivitiesInForeground);

        if (numberOfActivitiesInForeground == 0 && activity instanceof Loggable) {
            boolean isLogedIn = isLoggedIn();
            if (!isLogedIn) {
                return;
            }
            chatDestroyed = ((Loggable) activity).isCanPerformLogoutInOnStop();
            if (chatDestroyed) {
                QBLogoutAndDestroyChatCommand.start(activity, true);
            }
        }
    }

    private boolean isLoggedIn() {
        boolean result = false;
        if (QBChatService.isInitialized()) {
            result = QBChatService.getInstance().isLoggedIn();
        }
        return result;
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }
}