package com.quickblox.qmunicate.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.quickblox.qmunicate.core.command.CompositeServiceCommand;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.filetransfer.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.qmunicate.qb.commands.QBAddFriendCommand;
import com.quickblox.qmunicate.qb.commands.QBAddFriendsCommand;
import com.quickblox.qmunicate.qb.commands.QBChangePasswordCommand;
import com.quickblox.qmunicate.qb.commands.QBCreateGroupDialogCommand;
import com.quickblox.qmunicate.qb.commands.QBCreatePrivateChatCommand;
import com.quickblox.qmunicate.qb.commands.QBGetFileCommand;
import com.quickblox.qmunicate.qb.commands.QBInitChatCommand;
import com.quickblox.qmunicate.qb.commands.QBInitFriendListCommand;
import com.quickblox.qmunicate.qb.commands.QBInitVideoChatCommand;
import com.quickblox.qmunicate.qb.commands.QBJoinGroupChatCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadDialogsCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadDialogMessagesCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadFriendListCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadUsersCommand;
import com.quickblox.qmunicate.qb.commands.QBLoginChatCommand;
import com.quickblox.qmunicate.qb.commands.QBLoginCommand;
import com.quickblox.qmunicate.qb.commands.QBLoginRestCommand;
import com.quickblox.qmunicate.qb.commands.QBLoginRestWithSocialCommand;
import com.quickblox.qmunicate.qb.commands.QBLoginWithSocialCommand;
import com.quickblox.qmunicate.qb.commands.QBLogoutAndDestroyChatCommand;
import com.quickblox.qmunicate.qb.commands.QBLogoutChatCommand;
import com.quickblox.qmunicate.qb.commands.QBLogoutCommand;
import com.quickblox.qmunicate.qb.commands.QBLogoutRestCommand;
import com.quickblox.qmunicate.qb.commands.QBRemoveFriendCommand;
import com.quickblox.qmunicate.qb.commands.QBResetPasswordCommand;
import com.quickblox.qmunicate.qb.commands.QBSendGroupDialogMessageCommand;
import com.quickblox.qmunicate.qb.commands.QBSendPrivateChatMessageCommand;
import com.quickblox.qmunicate.qb.commands.QBSignUpCommand;
import com.quickblox.qmunicate.qb.commands.QBSignUpRestCommand;
import com.quickblox.qmunicate.qb.commands.QBUpdateDialogCommand;
import com.quickblox.qmunicate.qb.commands.QBUpdateUserCommand;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.qb.helpers.QBFriendListHelper;
import com.quickblox.qmunicate.qb.helpers.QBVideoChatHelper;

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

    private QBChatHelper chatHelper;
    private QBAuthHelper authHelper;
    private QBVideoChatHelper videoChatHelper;
    private QBFriendListHelper friendListHelper;

    public QBService() {
        threadQueue = new LinkedBlockingQueue<Runnable>();
        threadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT, threadQueue);

        initHelpers();
        initCommands();
    }

    private void initHelpers() {
        authHelper = new QBAuthHelper(this);
        chatHelper = new QBChatHelper(this);
        friendListHelper = new QBFriendListHelper(this);
        videoChatHelper = new QBVideoChatHelper(this);
    }

    private void initCommands() {
        registerLoginCommand();
        registerLoginWithSocialCommand();
        registerSignUpCommand();
        registerLogoutCommand();
        registerChangePasswordCommand();
        registerResetPasswordCommand();
        registerUpdateUserCommand();

        registerAddFriendCommand();
        registerAddFriendsCommand();
        registerRemoveFriendCommand();
        registerLoadFriendsCommand();
        registerLoadUsersCommand();

        registerLoginChatCommand();
        registerLogoutChatCommand();
        registerCreatePrivateChatCommand();
        registerCreateGroupChatCommand();
        registerJoinGroupChat();
        registerSendMessageCommand();
        registerSendGroupMessageCommand();

        registerGetFileCommand();
        registerLoadAttachFileCommand();

        registerLoadChatsDialogsCommand();
        registerUpdateChatDialogCommand();
        registerLoadDialogMessagesCommand();
    }

    private void registerJoinGroupChat() {
        QBJoinGroupChatCommand joinGroupChatCommand = new QBJoinGroupChatCommand(this, chatHelper,
                QBServiceConsts.JOIN_GROUP_CHAT_SUCCESS_ACTION,
                QBServiceConsts.JOIN_GROUP_CHAT_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, joinGroupChatCommand);
    }

    private void registerCreateGroupChatCommand() {
        QBCreateGroupDialogCommand createGroupChatCommand = new QBCreateGroupDialogCommand(this, chatHelper,
                QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION,
                QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.CREATE_GROUP_CHAT_ACTION, createGroupChatCommand);
    }

    private void registerCreatePrivateChatCommand() {
        QBCreatePrivateChatCommand createPrivateChatCommand = new QBCreatePrivateChatCommand(this,
                chatHelper, QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION,
                QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.CREATE_PRIVATE_CHAT_ACTION, createPrivateChatCommand);
    }

    private void registerLoginChatCommand() {
        ServiceCommand loginCommand = new QBLoginChatCommand(this, authHelper, chatHelper,
                QBServiceConsts.LOGIN_CHAT_SUCCESS_ACTION, QBServiceConsts.LOGIN_CHAT_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOGIN_CHAT_ACTION, loginCommand);
    }

    private void registerLogoutChatCommand() {
        ServiceCommand logoutCommand = new QBLogoutChatCommand(this, chatHelper,
                QBServiceConsts.LOGOUT_CHAT_SUCCESS_ACTION, QBServiceConsts.LOGOUT_CHAT_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOGOUT_CHAT_ACTION, logoutCommand);
    }

    private void registerLoadAttachFileCommand() {
        ServiceCommand loadAttachFileCommand = new QBLoadAttachFileCommand(this, chatHelper,
                QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION,
                QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOAD_ATTACH_FILE_ACTION, loadAttachFileCommand);
    }

    private void registerGetFileCommand() {
        ServiceCommand getFileCommand = new QBGetFileCommand(this, QBServiceConsts.GET_FILE_SUCCESS_ACTION,
                QBServiceConsts.GET_FILE_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.GET_FILE_ACTION, getFileCommand);
    }

    private void registerSendMessageCommand() {
        QBSendPrivateChatMessageCommand sendMessageCommand = new QBSendPrivateChatMessageCommand(this,
                chatHelper, QBServiceConsts.SEND_MESSAGE_SUCCESS_ACTION,
                QBServiceConsts.SEND_MESSAGE_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.SEND_MESSAGE_ACTION, sendMessageCommand);
    }

    private void registerSendGroupMessageCommand() {
        QBSendGroupDialogMessageCommand sendMessageCommand = new QBSendGroupDialogMessageCommand(this, chatHelper, QBServiceConsts.SEND_MESSAGE_SUCCESS_ACTION,
                QBServiceConsts.SEND_MESSAGE_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.SEND_GROUP_MESSAGE_ACTION, sendMessageCommand);
    }

    private void registerLoadFriendsCommand() {
        QBLoadFriendListCommand loadFriendListCommand = new QBLoadFriendListCommand(this, friendListHelper,
                QBServiceConsts.LOAD_FRIENDS_SUCCESS_ACTION, QBServiceConsts.LOAD_FRIENDS_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOAD_FRIENDS_ACTION, loadFriendListCommand);
    }

    private void registerLoadUsersCommand() {
        QBLoadUsersCommand userSearchCommand = new QBLoadUsersCommand(this,
                QBServiceConsts.LOAD_USERS_SUCCESS_ACTION, QBServiceConsts.LOAD_USERS_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOAD_USERS_ACTION, userSearchCommand);
    }

    private void registerUpdateUserCommand() {
        QBUpdateUserCommand updateUserCommand = new QBUpdateUserCommand(this, authHelper, friendListHelper, QBServiceConsts.UPDATE_USER_SUCCESS_ACTION,
                QBServiceConsts.UPDATE_USER_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.UPDATE_USER_ACTION, updateUserCommand);
    }

    private void registerResetPasswordCommand() {
        QBResetPasswordCommand resetPasswordCommand = new QBResetPasswordCommand(this, authHelper,
                QBServiceConsts.RESET_PASSWORD_SUCCESS_ACTION, QBServiceConsts.RESET_PASSWORD_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.RESET_PASSWORD_ACTION, resetPasswordCommand);
    }

    private void registerChangePasswordCommand() {
        QBChangePasswordCommand changePasswordCommand = new QBChangePasswordCommand(this, authHelper,
                QBServiceConsts.CHANGE_PASSWORD_SUCCESS_ACTION, QBServiceConsts.CHANGE_PASSWORD_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.CHANGE_PASSWORD_ACTION, changePasswordCommand);
    }

    private void registerAddFriendCommand() {
        QBAddFriendCommand addFriendCommand = new QBAddFriendCommand(this, friendListHelper,
                QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, QBServiceConsts.ADD_FRIEND_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.ADD_FRIEND_ACTION, addFriendCommand);
    }

    private void registerAddFriendsCommand() {
        QBAddFriendsCommand addFriendsCommand = new QBAddFriendsCommand(this, friendListHelper,
                QBServiceConsts.ADD_FRIENDS_SUCCESS_ACTION, QBServiceConsts.ADD_FRIENDS_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.ADD_FRIENDS_ACTION, addFriendsCommand);
    }

    private void registerRemoveFriendCommand() {
        QBRemoveFriendCommand removeFriendCommand = new QBRemoveFriendCommand(this,
                QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION, QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.REMOVE_FRIEND_ACTION, removeFriendCommand);
    }

    private void registerLogoutCommand() {
        QBLogoutCommand logoutCommand = new QBLogoutCommand(this, QBServiceConsts.LOGOUT_SUCCESS_ACTION,
                QBServiceConsts.LOGOUT_FAIL_ACTION);
        QBLogoutAndDestroyChatCommand logoutChatCommand = new QBLogoutAndDestroyChatCommand(this, chatHelper, QBServiceConsts.LOGOUT_AND_DESTROY_CHAT_SUCCESS_ACTION,
                QBServiceConsts.LOGOUT_AND_DESTROY_CHAT_FAIL_ACTION);
        QBLogoutRestCommand logoutRestCommand = new QBLogoutRestCommand(this, authHelper,
                QBServiceConsts.LOGOUT_REST_SUCCESS_ACTION, QBServiceConsts.LOGOUT_REST_FAIL_ACTION);

        logoutCommand.addCommand(logoutChatCommand);
        logoutCommand.addCommand(logoutRestCommand);
        serviceCommandMap.put(QBServiceConsts.LOGOUT_ACTION, logoutCommand);
    }

    private void registerSignUpCommand() {
        QBSignUpCommand signUpCommand = new QBSignUpCommand(this, QBServiceConsts.SIGNUP_SUCCESS_ACTION,
                QBServiceConsts.SIGNUP_FAIL_ACTION);
        QBSignUpRestCommand signUpRestCommand = new QBSignUpRestCommand(this, authHelper,
                QBServiceConsts.SIGNUP_REST_SUCCESS_ACTION, QBServiceConsts.SIGNUP_REST_FAIL_ACTION);

        signUpCommand.addCommand(signUpRestCommand);

        addLoginChatAndInitCommands(signUpCommand);
        serviceCommandMap.put(QBServiceConsts.SIGNUP_ACTION, signUpCommand);
    }

    private void registerLoginWithSocialCommand() {
        QBLoginWithSocialCommand loginCommand = new QBLoginWithSocialCommand(this,
                QBServiceConsts.LOGIN_SUCCESS_ACTION, QBServiceConsts.LOGIN_FAIL_ACTION);
        QBLoginRestWithSocialCommand loginRestCommand = new QBLoginRestWithSocialCommand(this, authHelper,
                QBServiceConsts.LOGIN_REST_SUCCESS_ACTION, QBServiceConsts.LOGIN_REST_FAIL_ACTION);

        loginCommand.addCommand(loginRestCommand);

        addLoginChatAndInitCommands(loginCommand);
        serviceCommandMap.put(QBServiceConsts.SOCIAL_LOGIN_ACTION, loginCommand);
    }

    private void registerLoginCommand() {
        QBLoginCommand loginCommand = new QBLoginCommand(this, QBServiceConsts.LOGIN_SUCCESS_ACTION,
                QBServiceConsts.LOGIN_FAIL_ACTION);
        QBLoginRestCommand loginRestCommand = new QBLoginRestCommand(this, authHelper,
                QBServiceConsts.LOGIN_REST_SUCCESS_ACTION, QBServiceConsts.LOGIN_REST_FAIL_ACTION);

        loginCommand.addCommand(loginRestCommand);

        addLoginChatAndInitCommands(loginCommand);
        serviceCommandMap.put(QBServiceConsts.LOGIN_ACTION, loginCommand);
    }

    private void addLoginChatAndInitCommands(CompositeServiceCommand loginCommand) {
        QBInitChatCommand initChatCommand = new QBInitChatCommand(this, chatHelper,
                QBServiceConsts.INIT_CHAT_SUCCESS_ACTION, QBServiceConsts.INIT_CHAT_FAIL_ACTION);
        QBLoginChatCommand loginChatCommand = new QBLoginChatCommand(this, authHelper, chatHelper,
                QBServiceConsts.LOGIN_CHAT_SUCCESS_ACTION, QBServiceConsts.LOGIN_CHAT_FAIL_ACTION);
        QBInitVideoChatCommand initVideoChatCommand = new QBInitVideoChatCommand(this, videoChatHelper,
                QBServiceConsts.INIT_VIDEO_CHAT_SUCCESS_ACTION, QBServiceConsts.INIT_VIDEO_CHAT_FAIL_ACTION);
        QBInitFriendListCommand initFriendListCommand = new QBInitFriendListCommand(this, friendListHelper,
                QBServiceConsts.INIT_FRIEND_LIST_SUCCESS_ACTION,
                QBServiceConsts.INIT_FRIEND_LIST_FAIL_ACTION);

        loginCommand.addCommand(loginChatCommand);
        loginCommand.addCommand(initChatCommand);
        loginCommand.addCommand(initVideoChatCommand);
        loginCommand.addCommand(initFriendListCommand);
    }

    private void registerLoadChatsDialogsCommand() {
        QBLoadDialogsCommand chatsDialogsCommand = new QBLoadDialogsCommand(this, chatHelper,
                QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION, QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, chatsDialogsCommand);
    }

    private void registerUpdateChatDialogCommand() {
        QBUpdateDialogCommand updateChatDialogCommand = new QBUpdateDialogCommand(this, chatHelper,
                QBServiceConsts.UPDATE_CHAT_DIALOG_SUCCESS_ACTION, QBServiceConsts.UPDATE_CHAT_DIALOG_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.UPDATE_CHAT_DIALOG_ACTION, updateChatDialogCommand);
    }

    private void registerLoadDialogMessagesCommand() {
        QBLoadDialogMessagesCommand loadDialogMessagesCommand = new QBLoadDialogMessagesCommand(this, chatHelper,
                QBServiceConsts.LOAD_DIALOG_MESSAGES_SUCCESS_ACTION, QBServiceConsts.LOAD_DIALOG_MESSAGES_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOAD_DIALOG_MESSAGES_ACTION, loadDialogMessagesCommand);
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
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public QBChatHelper getChatHelper() {
        return chatHelper;
    }

    public QBAuthHelper getAuthHelper() {
        return authHelper;
    }

    public QBFriendListHelper getFriendListHelper() {
        return friendListHelper;
    }

    private void startAsync(final ServiceCommand command, final Intent intent) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                command.execute(intent.getExtras());
            }
        });
    }

    public QBVideoChatHelper getVideoChatHelper() {
        return videoChatHelper;
    }

    public class QBServiceBinder extends Binder {

        public QBService getService() {
            return QBService.this;
        }
    }
}
