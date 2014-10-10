package com.quickblox.q_municate.ui.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.Session;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.command.Command;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.model.LoginType;
import com.quickblox.q_municate.qb.commands.QBLoginRestCommand;
import com.quickblox.q_municate.qb.commands.QBLoginRestWithSocialCommand;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.dialogs.AlertDialog;
import com.quickblox.q_municate.ui.splash.SplashActivity;
import com.quickblox.q_municate.utils.ErrorUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.keyboardsurfer.android.widget.crouton.Crouton;

//This class uses to delegate common functionality from different types of activity(Activity, FragmentActivity)
public class ActivityHelper extends BaseActivityDelegator {

    private Activity activity;
    private BaseBroadcastReceiver broadcastReceiver;
    private GlobalBroadcastReceiver globalBroadcastReceiver;
    private Map<String, Set<Command>> broadcastCommandMap = new HashMap<String, Set<Command>>();
    private GlobalActionsListener actionsListener;
    private Handler handler;
    private ActivityUIHelper activityUIHelper;

    public ActivityHelper(Context context, GlobalActionsListener actionsListener) {
        super(context);
        this.actionsListener = actionsListener;
        activity = (Activity) context;
        activityUIHelper = new ActivityUIHelper(activity);
    }

    public void showFriendAlert(String message) {
        AlertDialog alertDialog = AlertDialog.newInstance(message);
        alertDialog.show(activity.getFragmentManager(), null);
    }

    protected void onReceiveMessage(Bundle extras) {
        activityUIHelper.onReceiveMessage(extras);
    }

    public void forceRelogin() {
        ErrorUtils.showError(activity, activity.getString(
                R.string.dlg_force_relogin_on_token_required));
        SplashActivity.start(activity);
        activity.finish();
    }

    public void refreshSession() {
        if (LoginType.EMAIL.equals(AppSession.getSession().getLoginType())) {
            QBLoginRestCommand.start(activity, AppSession.getSession().getUser());
        } else {
            QBLoginRestWithSocialCommand.start(activity, QBProvider.FACEBOOK,
                    Session.getActiveSession().getAccessToken(), null);
        }
    }

    public void onCreate() {
        broadcastReceiver = new BaseBroadcastReceiver();
        globalBroadcastReceiver = new GlobalBroadcastReceiver();
    }

    public void hideActionBarProgress() {
        setVisibilityActionBarProgress(false);
    }

    public void showActionBarProgress() {
        setVisibilityActionBarProgress(true);
    }

    public void setVisibilityActionBarProgress(boolean visibility) {
        activity.setProgressBarIndeterminateVisibility(visibility);
    }

    public void addAction(String action, Command command) {
        Set<Command> commandSet = broadcastCommandMap.get(action);
        if (commandSet == null) {
            commandSet = new HashSet<Command>();
            broadcastCommandMap.put(action, commandSet);
        }
        commandSet.add(command);
    }

    public boolean hasAction(String action) {
        return broadcastCommandMap.containsKey(action);
    }

    public void removeAction(String action) {
        broadcastCommandMap.remove(action);
    }

    public void updateBroadcastActionList() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver);
        IntentFilter intentFilter = new IntentFilter();
        for (String commandName : broadcastCommandMap.keySet()) {
            intentFilter.addAction(commandName);
        }
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, intentFilter);
    }

    public void onPause() {
        unregisterBroadcastReceiver();
        Crouton.cancelAllCroutons();
    }

    public void onResume() {
        registerGlobalReceiver();
        updateBroadcastActionList();
    }

    private void registerGlobalReceiver() {
        IntentFilter globalActionsIntentFilter = new IntentFilter();
        globalActionsIntentFilter.addAction(QBServiceConsts.GOT_CHAT_MESSAGE);
        globalActionsIntentFilter.addAction(QBServiceConsts.FORCE_RELOGIN);
        globalActionsIntentFilter.addAction(QBServiceConsts.REFRESH_SESSION);
        globalActionsIntentFilter.addAction(QBServiceConsts.FRIEND_ALERT_SHOW);
        LocalBroadcastManager.getInstance(activity).registerReceiver(globalBroadcastReceiver,
                globalActionsIntentFilter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(globalBroadcastReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver);
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    public interface GlobalActionsListener {

        public void onReceiveChatMessageAction(Bundle extras);

        public void onReceiveForceReloginAction(Bundle extras);

        public void onReceiveRefreshSessionAction(Bundle extras);

        public void onReceiveFriendActionAction(Bundle extras);
    }

    private class BaseBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (intent != null && (action) != null) {
                Log.d("STEPS", "executing " + action);
                final Set<Command> commandSet = broadcastCommandMap.get(action);

                if (commandSet != null && !commandSet.isEmpty()) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            for (Command command : commandSet) {
                                try {
                                    command.execute(intent.getExtras());
                                } catch (Exception e) {
                                    ErrorUtils.logError(e);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private class GlobalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    Bundle extras = intent.getExtras();
                    if (extras != null && QBServiceConsts.GOT_CHAT_MESSAGE.equals(intent.getAction())) {
                        if (actionsListener != null) {
                            actionsListener.onReceiveChatMessageAction(intent.getExtras());
                        }
                    } else if (QBServiceConsts.FORCE_RELOGIN.equals(intent.getAction())) {
                        if (actionsListener != null) {
                            actionsListener.onReceiveForceReloginAction(intent.getExtras());
                        }
                    } else if (QBServiceConsts.REFRESH_SESSION.equals(intent.getAction())) {
                        if (actionsListener != null) {
                            actionsListener.onReceiveRefreshSessionAction(intent.getExtras());
                        }
                    } else if (QBServiceConsts.FRIEND_ALERT_SHOW.equals(intent.getAction())) {
                        if (actionsListener != null) {
                            actionsListener.onReceiveFriendActionAction(intent.getExtras());
                        }
                    }
                }
            });
        }
    }
}