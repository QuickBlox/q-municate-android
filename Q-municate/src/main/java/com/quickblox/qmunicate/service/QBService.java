package com.quickblox.qmunicate.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.QBAddFriendCommand;
import com.quickblox.qmunicate.qb.QBAddFriendsCommand;
import com.quickblox.qmunicate.qb.QBChangePasswordCommand;
import com.quickblox.qmunicate.qb.QBGetFileCommand;
import com.quickblox.qmunicate.qb.QBLoginCommand;
import com.quickblox.qmunicate.qb.QBLogoutCommand;
import com.quickblox.qmunicate.qb.QBRemoveFriendCommand;
import com.quickblox.qmunicate.qb.QBResetPasswordCommand;
import com.quickblox.qmunicate.qb.QBSignUpCommand;
import com.quickblox.qmunicate.qb.QBSocialLoginCommand;
import com.quickblox.qmunicate.qb.QBUpdateUserCommand;

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

    private IBinder binder = new QBServiceBinder();
    private final BlockingQueue<Runnable> threadQueue;

    private Map<String, ServiceCommand> serviceCommandMap = new HashMap<String, ServiceCommand>();
    private ThreadPoolExecutor threadPool;
    private QBChatHelper qbChatHelper;

    private SmackAndroid smackAndroid;

    public QBService() {
        threadQueue = new LinkedBlockingQueue<Runnable>();
        threadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                threadQueue);

        serviceCommandMap.put(QBServiceConsts.ADD_FRIEND_ACTION, new QBAddFriendCommand(this,
                QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, QBServiceConsts.ADD_FRIEND_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.ADD_FRIENDS_ACTION, new QBAddFriendsCommand(this,
                QBServiceConsts.ADD_FRIENDS_SUCCESS_ACTION, QBServiceConsts.ADD_FRIENDS_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.CHANGE_PASSWORD_ACTION, new QBChangePasswordCommand(this,
                QBServiceConsts.CHANGE_PASSWORD_SUCCESS_ACTION, QBServiceConsts.CHANGE_PASSWORD_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.GET_FILE_ACTION, new QBGetFileCommand(this,
                QBServiceConsts.GET_FILE_SUCCESS_ACTION, QBServiceConsts.GET_FILE_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.LOGIN_ACTION, new QBLoginCommand(this, qbChatHelper,
                QBServiceConsts.LOGIN_SUCESS_ACTION, QBServiceConsts.LOGIN_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.LOGOUT_ACTION, new QBLogoutCommand(this,
                QBServiceConsts.LOGOUT_SUCCESS_ACTION, QBServiceConsts.LOGOUT_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.REMOVE_FRIEND_ACTION, new QBRemoveFriendCommand(this,
                QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION, QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.RESET_PASSWORD_ACTION, new QBResetPasswordCommand(this,
                QBServiceConsts.RESET_PASSWORD_SUCCESS_ACTION, QBServiceConsts.RESET_PASSWORD_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.SIGNUP_ACTION, new QBSignUpCommand(this,
                QBServiceConsts.SIGNUP_SUCCESS_ACTION, QBServiceConsts.SIGNUP_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.SOCIAL_LOGIN_ACTION, new QBSocialLoginCommand(this,
                QBServiceConsts.LOGIN_SUCESS_ACTION, QBServiceConsts.LOGIN_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.UPDATE_USER_ACTION, new QBUpdateUserCommand(this,
                QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, QBServiceConsts.UPDATE_USER_FAIL_ACTION));
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
            ServiceCommand command = serviceCommandMap.get(action);
            if (command != null) {
                startAsync(command, intent);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public QBChatHelper getQbChatHelper() {
        return qbChatHelper;
    }

    public class QBServiceBinder extends Binder {
        public QBService getService() {
            return QBService.this;
        }
    }

    private void startAsync(final ServiceCommand command, final Intent intent) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                command.execute(intent.getExtras());
            }
        });
    }
}
