package com.quickblox.q_municate.ui.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.Session;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.splash.SplashActivity;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.QBLoginRestCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoginRestWithSocialCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ErrorUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.keyboardsurfer.android.widget.crouton.Crouton;

//This class uses to delegate common functionality from different types of activity(Activity, FragmentActivity)
public class ActivityHelper extends BaseActivityHelper {

    protected QBService service;
    private Activity activity;
    private BaseBroadcastReceiver broadcastReceiver;
    private GlobalBroadcastReceiver globalBroadcastReceiver;
    private Map<String, Set<Command>> broadcastCommandMap = new HashMap<String, Set<Command>>();
    private GlobalActionsListener actionsListener;
    private Handler handler;
    private ActivityUIHelper activityUIHelper;

    private boolean bounded;
    private ServiceConnection serviceConnection;
    private ServiceConnectionListener serviceConnectionListener;

    public ActivityHelper(Context context, GlobalActionsListener actionsListener,
            ServiceConnectionListener serviceConnectionListener) {
        super(context);
        this.actionsListener = actionsListener;
        this.serviceConnectionListener = serviceConnectionListener;
        activity = (Activity) context;
        activityUIHelper = new ActivityUIHelper(activity);
        serviceConnection = new QBChatServiceConnection();
    }

    protected void onReceivedChatMessageNotification(Bundle extras) {
        activityUIHelper.showChatMessageNotification(extras);
    }

    protected void onReceivedContactRequestNotification(Bundle extras) {
        activityUIHelper.showContactRequestNotification(extras);
    }

    public void forceRelogin() {
        ErrorUtils.showError(activity, activity.getString(R.string.dlg_force_relogin_on_token_required));
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

    protected void onStart() {
        connectToService();
    }

    public void onStop() {
        unbindService();
    }

    private void registerGlobalReceiver() {
        IntentFilter globalActionsIntentFilter = new IntentFilter();
        globalActionsIntentFilter.addAction(QBServiceConsts.GOT_CHAT_MESSAGE);
        globalActionsIntentFilter.addAction(QBServiceConsts.GOT_CONTACT_REQUEST);
        globalActionsIntentFilter.addAction(QBServiceConsts.FORCE_RELOGIN);
        globalActionsIntentFilter.addAction(QBServiceConsts.REFRESH_SESSION);
        globalActionsIntentFilter.addAction(QBServiceConsts.TYPING_MESSAGE);
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

    private void unbindService() {
        if (bounded) {
            activity.unbindService(serviceConnection);
        }
    }

    private void connectToService() {
        Intent intent = new Intent(activity, QBService.class);
        activity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public interface GlobalActionsListener {

        public void onReceiveChatMessageAction(Bundle extras);

        public void onReceiveForceReloginAction(Bundle extras);

        public void onReceiveRefreshSessionAction(Bundle extras);

        public void onReceiveContactRequestAction(Bundle extras);
    }

    public interface ServiceConnectionListener {

        public void onConnectedToService(QBService service);
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
                    if (actionsListener != null) {
                        if (extras != null && QBServiceConsts.GOT_CHAT_MESSAGE.equals(intent.getAction())) {
                            actionsListener.onReceiveChatMessageAction(intent.getExtras());
                        } else if (QBServiceConsts.GOT_CONTACT_REQUEST.equals(intent.getAction())) {
                            actionsListener.onReceiveContactRequestAction(intent.getExtras());
                        } else if (QBServiceConsts.FORCE_RELOGIN.equals(intent.getAction())) {
                            actionsListener.onReceiveForceReloginAction(intent.getExtras());
                        } else if (QBServiceConsts.REFRESH_SESSION.equals(intent.getAction())) {
                            actionsListener.onReceiveRefreshSessionAction(intent.getExtras());
                        }
                    }
                }
            });
        }
    }

    private class QBChatServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            bounded = true;
            service = ((QBService.QBServiceBinder) binder).getService();
            serviceConnectionListener.onConnectedToService(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}