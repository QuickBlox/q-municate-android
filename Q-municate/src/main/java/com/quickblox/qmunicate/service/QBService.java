package com.quickblox.qmunicate.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.quickblox.module.chat.QBChatService;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.commands.QBAddFriendCommand;
import com.quickblox.qmunicate.qb.commands.QBAddFriendsCommand;
import com.quickblox.qmunicate.qb.commands.QBChangePasswordCommand;
import com.quickblox.qmunicate.qb.commands.QBFriendsLoadCommand;
import com.quickblox.qmunicate.qb.commands.QBGetFileCommand;
import com.quickblox.qmunicate.qb.commands.QBLoginCommand;
import com.quickblox.qmunicate.qb.commands.QBLogoutCommand;
import com.quickblox.qmunicate.qb.commands.QBRemoveFriendCommand;
import com.quickblox.qmunicate.qb.commands.QBResetPasswordCommand;
import com.quickblox.qmunicate.qb.commands.QBSendPrivateChatMessageCommand;
import com.quickblox.qmunicate.qb.commands.QBSignUpCommand;
import com.quickblox.qmunicate.qb.commands.QBSocialLoginCommand;
import com.quickblox.qmunicate.qb.commands.QBUpdateUserCommand;
import com.quickblox.qmunicate.qb.commands.QBUserSearchCommand;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;

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
    private IBinder binder = new QBServiceBinder();

    private Map<String, ServiceCommand> serviceCommandMap = new HashMap<String, ServiceCommand>();
    private ThreadPoolExecutor threadPool;

    private QBChatHelper qbChatHelper;
    private QBAuthHelper qbAuthHelper;

    public QBService() {
        threadQueue = new LinkedBlockingQueue<Runnable>();
        threadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT, threadQueue);

        qbChatHelper = QBChatHelper.getInstance();
        qbAuthHelper = new QBAuthHelper();

        serviceCommandMap.put(QBServiceConsts.ADD_FRIEND_ACTION, new QBAddFriendCommand(this,
                QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, QBServiceConsts.ADD_FRIEND_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.ADD_FRIENDS_ACTION, new QBAddFriendsCommand(this,
                QBServiceConsts.ADD_FRIENDS_SUCCESS_ACTION, QBServiceConsts.ADD_FRIENDS_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.CHANGE_PASSWORD_ACTION, new QBChangePasswordCommand(this,
                qbAuthHelper, QBServiceConsts.CHANGE_PASSWORD_SUCCESS_ACTION,
                QBServiceConsts.CHANGE_PASSWORD_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.GET_FILE_ACTION, new QBGetFileCommand(this,
                QBServiceConsts.GET_FILE_SUCCESS_ACTION, QBServiceConsts.GET_FILE_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.LOGIN_ACTION, new QBLoginCommand(this, qbAuthHelper,
                qbChatHelper, QBServiceConsts.LOGIN_SUCCESS_ACTION, QBServiceConsts.LOGIN_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.LOGOUT_ACTION, new QBLogoutCommand(this, qbAuthHelper,
                qbChatHelper, QBServiceConsts.LOGOUT_SUCCESS_ACTION, QBServiceConsts.LOGOUT_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.REMOVE_FRIEND_ACTION, new QBRemoveFriendCommand(this,
                QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION, QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.RESET_PASSWORD_ACTION, new QBResetPasswordCommand(this,
                qbAuthHelper, QBServiceConsts.RESET_PASSWORD_SUCCESS_ACTION,
                QBServiceConsts.RESET_PASSWORD_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.SIGNUP_ACTION, new QBSignUpCommand(this, qbAuthHelper,
                qbChatHelper, QBServiceConsts.SIGNUP_SUCCESS_ACTION, QBServiceConsts.SIGNUP_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.SOCIAL_LOGIN_ACTION, new QBSocialLoginCommand(this,
                qbAuthHelper, qbChatHelper, QBServiceConsts.LOGIN_SUCCESS_ACTION,
                QBServiceConsts.LOGIN_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.UPDATE_USER_ACTION, new QBUpdateUserCommand(this, qbAuthHelper,
                QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, QBServiceConsts.UPDATE_USER_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.FRIENDS_LOAD_ACTION, new QBFriendsLoadCommand(this,
                QBServiceConsts.FRIENDS_LOAD_SUCCESS_ACTION, QBServiceConsts.FRIENDS_LOAD_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.USER_SEARCH_ACTION, new QBUserSearchCommand(this,
                QBServiceConsts.USER_SEARCH_SUCCESS_ACTION, QBServiceConsts.USER_SEARCH_FAIL_ACTION));
        serviceCommandMap.put(QBServiceConsts.SEND_MESSAGE_ACTION, new QBSendPrivateChatMessageCommand(this, qbChatHelper,
                QBServiceConsts.SEND_MESSAGE_SUCCESS_ACTION, QBServiceConsts.SEND_MESSAGE_FAIL_ACTION));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        QBChatService.init(this);
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
        QBChatService.getInstance().destroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public QBChatHelper getQbChatHelper() {
        return qbChatHelper;
    }

    public QBAuthHelper getQbAuthHelper() {
        return qbAuthHelper;
    }

    private void startAsync(final ServiceCommand command, final Intent intent) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                command.execute(intent.getExtras());
            }
        });
    }

    public class QBServiceBinder extends Binder {

        public QBService getService() {
            return QBService.this;
        }
    }
}
