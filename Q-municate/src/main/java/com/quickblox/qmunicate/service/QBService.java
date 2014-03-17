package com.quickblox.qmunicate.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.qmunicate.core.command.BaseCommand;
import com.quickblox.qmunicate.qb.command.QBAddFriendCommand;
import com.quickblox.qmunicate.qb.command.QBChangePasswordCommand;
import com.quickblox.qmunicate.qb.command.QBGetFileCommand;
import com.quickblox.qmunicate.qb.command.QBLoginCommand;
import com.quickblox.qmunicate.qb.command.QBLogoutCommand;
import com.quickblox.qmunicate.qb.command.QBRemoveFriendCommand;
import com.quickblox.qmunicate.qb.command.QBResetPasswordCommand;
import com.quickblox.qmunicate.qb.command.QBSignUpCommand;
import com.quickblox.qmunicate.qb.command.QBSocialLoginCommand;
import com.quickblox.qmunicate.qb.command.QBUpdateUserCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QBService extends Service {

    private static final String TAG = QBService.class.getSimpleName();

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final BlockingQueue<Runnable> threadQueue;

    private Map<String, BaseCommand> serviceCommandMap = new HashMap<String, BaseCommand>();
    private ThreadPoolExecutor threadPool;

    private SmackAndroid smackAndroid;

    public QBService() {
        threadQueue = new LinkedBlockingQueue<Runnable>();
        threadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                threadQueue);

        serviceCommandMap.put(QBServiceConsts.ADD_FRIEND_ACTION, new QBAddFriendCommand(this, QBServiceConsts.ADD_FRIEND_RESULT));
        serviceCommandMap.put(QBServiceConsts.CHANGE_PASSWORD_ACTION, new QBChangePasswordCommand(this, QBServiceConsts.CHANGE_PASSWORD_RESULT));
        serviceCommandMap.put(QBServiceConsts.GET_FILE_ACTION, new QBGetFileCommand(this, QBServiceConsts.GET_FILE_RESULT));
        serviceCommandMap.put(QBServiceConsts.LOGIN_ACTION, new QBLoginCommand(this, QBServiceConsts.LOGIN_RESULT));
        serviceCommandMap.put(QBServiceConsts.LOGOUT_ACTION, new QBLogoutCommand(this, QBServiceConsts.LOGOUT_RESULT));
        serviceCommandMap.put(QBServiceConsts.REMOVE_FRIEND_ACTION, new QBRemoveFriendCommand(this, QBServiceConsts.REMOVE_FRIEND_RESULT));
        serviceCommandMap.put(QBServiceConsts.RESET_PASSWORD_ACTION, new QBResetPasswordCommand(this, QBServiceConsts.RESET_PASSWORD_RESULT));
        serviceCommandMap.put(QBServiceConsts.SIGNUP_ACTION, new QBSignUpCommand(this, QBServiceConsts.SIGNUP_RESULT));
        serviceCommandMap.put(QBServiceConsts.SOCIAL_LOGIN_ACTION, new QBSocialLoginCommand(this, QBServiceConsts.LOGIN_RESULT));
        serviceCommandMap.put(QBServiceConsts.UPDATE_USER_ACTION, new QBUpdateUserCommand(this, QBServiceConsts.UPDATE_USER_RESULT));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        smackAndroid = SmackAndroid.init(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action;
        if (intent != null && (action = intent.getAction()) != null) {
            Log.d(TAG, "service started with resultAction=" + action);
            BaseCommand command = serviceCommandMap.get(action);
            if (command != null) {
                startAsync(command, intent);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        smackAndroid.onDestroy();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startAsync(final BaseCommand command, final Intent intent) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                command.execute(intent.getExtras());
            }
        });
    }
}
