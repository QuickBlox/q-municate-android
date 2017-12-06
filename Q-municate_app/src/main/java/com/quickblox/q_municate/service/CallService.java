package com.quickblox.q_municate.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quickblox.q_municate.ui.activities.call.CallActivity;
import com.quickblox.q_municate.utils.SystemUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.CallPushParams;
import com.quickblox.q_municate_core.qb.commands.chat.QBInitCallChatCommand;
import com.quickblox.q_municate_core.qb.commands.push.QBPushCallCompositeCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CallService extends Service {
    private static final String TAG = CallService.class.getSimpleName();

    private Map<String, Set<Command>> broadcastCommandMap;
    protected LocalBroadcastManager localBroadcastManager;
    private CallBroadcastReceiver callBroadcastReceiver;
    private CallPushParams callPushParams;
    private Handler handler;

    public static void start(Context context) {
        Intent intent = new Intent(context, CallService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate()");
        broadcastCommandMap = new HashMap<>();
        callBroadcastReceiver = new CallBroadcastReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        addActions();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        QBPushCallCompositeCommand.start(this);
        return START_NOT_STICKY;
    }

    private void addActions() {
        Log.v(TAG, "addActions()");
        addAction(QBServiceConsts.PUSH_CALL_COMPOSITE_SUCCESS_ACTION, new PushCallCompositeSuccessAction());
        addAction(QBServiceConsts.PUSH_CALL_COMPOSITE_FAIL_ACTION, new PushCallCompositeFailAction());

        addAction(QBServiceConsts.INIT_VIDEO_CHAT_SUCCESS_ACTION, new InitCallChatSuccessAction());
        addAction(QBServiceConsts.INIT_VIDEO_CHAT_FAIL_ACTION, new InitCallChatFailedAction());

        updateBroadcastActionList();
    }

    public void addAction(String action, Command command) {
        Set<Command> commandSet = broadcastCommandMap.get(action);
        if (commandSet == null) {
            commandSet = new HashSet<>();
            broadcastCommandMap.put(action, commandSet);
        }
        commandSet.add(command);
    }

    public void updateBroadcastActionList() {
        localBroadcastManager.unregisterReceiver(callBroadcastReceiver);
        IntentFilter intentFilter = new IntentFilter();
        for (String commandName : broadcastCommandMap.keySet()) {
            intentFilter.addAction(commandName);
        }
        localBroadcastManager.registerReceiver(callBroadcastReceiver, intentFilter);
    }

    public class PushCallCompositeSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d(TAG, "PushCallCompositeSuccessAction");
            initCallParams();
            QBInitCallChatCommand.start(CallService.this, CallActivity.class, callPushParams);
        }
    }

    private void initCallParams() {
        callPushParams = new CallPushParams();
        callPushParams.setIsNewTask(!SystemUtils.isAppRunning());
        callPushParams.setPushCall(true);
    }

    public class PushCallCompositeFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d(TAG, "PushCallCompositeFailAction");
        }
    }


    public class InitCallChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d(TAG, "InitCallChatSuccessAction");
            stopSelf();
        }
    }

    public class InitCallChatFailedAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d(TAG, "InitCallChatFailedAction");
        }
    }

    private class CallBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.d(TAG, "CallBroadcastReceiver onReceive");
            String action = intent.getAction();
            if (action != null) {
                Log.d(TAG, "executing " + action);
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

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy()");
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(callBroadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service onBind)");
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "Service onTaskRemoved()");
        super.onTaskRemoved(rootIntent);
    }
}
