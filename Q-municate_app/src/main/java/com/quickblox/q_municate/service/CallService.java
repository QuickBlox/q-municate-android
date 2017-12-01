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
import com.quickblox.q_municate.utils.helpers.PowerManagerHelper;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.chat.QBInitCallChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBInitChatServiceCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCompositeCommand;
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
    private Handler handler;

    public static void start(Context context) {
        Intent intent = new Intent(context, CallService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastCommandMap = new HashMap<>();
        callBroadcastReceiver = new CallBroadcastReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        addActions();
        Log.d(TAG, "Service onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        QBLoginChatCompositeCommand.start(this);
        return START_REDELIVER_INTENT;
    }

    private void addActions() {
        Log.v(TAG, "AMBRA addActions()");
        addAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_SUCCESS_ACTION, new LoginChatCompositeSuccessAction());
        addAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_FAIL_ACTION, new LoginChatCompositeFailAction());

        addAction(QBServiceConsts.INIT_CHAT_SERVICE_ACTION, new InitChatServiceSuccessAction());
        addAction(QBServiceConsts.INIT_CHAT_SERVICE_FAIL_ACTION, new InitChatServiceFailAction());

        addAction(QBServiceConsts.LOGIN_CHAT_ACTION, new LoginChatSuccessAction());
        addAction(QBServiceConsts.LOGIN_CHAT_FAIL_ACTION, new LoginChatFailAction());

        addAction(QBServiceConsts.INIT_CALL_CHAT_ACTION, new InitCallChatSuccessAction());

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

    public class LoginChatCompositeSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d("AMBRA", "LoginChatCompositeSuccessAction");
            QBInitCallChatCommand.start(CallService.this, CallActivity.class);
        }
    }

    public class LoginChatCompositeFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d("AMBRA", "LoginChatCompositeFailAction");
        }
    }

    public class InitChatServiceSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d("AMBRA", "InitChatServiceSuccessAction");

        }
    }

    public class InitChatServiceFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d("AMBRA", "InitChatServiceFailAction");
        }
    }

    public class InitCallChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d("AMBRA", "InitCallChatSuccessAction");
            PowerManagerHelper.wakeUpScreen(CallService.this);
        }
    }

    public class LoginChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d("AMBRA", "LoginChatSuccessAction");
        }
    }

    public class LoginChatFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Log.d("AMBRA", "LoginChatFailAction");
        }
    }

    private class CallBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.d("AMBRA", "GlobalBroadcastReceiver onReceive");
            String action = intent.getAction();
            if (action != null) {
                Log.d("STEPS", "AMBRA executing " + action);
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
