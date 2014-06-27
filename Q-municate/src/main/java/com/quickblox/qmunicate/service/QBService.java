package com.quickblox.qmunicate.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.facebook.Session;
import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.qmunicate.core.command.CompositeServiceCommand;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.model.LoginType;
import com.quickblox.qmunicate.qb.commands.QBAddFriendCommand;
import com.quickblox.qmunicate.qb.commands.QBAddFriendsToGroupCommand;
import com.quickblox.qmunicate.qb.commands.QBChangePasswordCommand;
import com.quickblox.qmunicate.qb.commands.QBCreateGroupDialogCommand;
import com.quickblox.qmunicate.qb.commands.QBCreatePrivateChatCommand;
import com.quickblox.qmunicate.qb.commands.QBGetFileCommand;
import com.quickblox.qmunicate.qb.commands.QBImportFriendsCommand;
import com.quickblox.qmunicate.qb.commands.QBInitChatCommand;
import com.quickblox.qmunicate.qb.commands.QBInitFriendListCommand;
import com.quickblox.qmunicate.qb.commands.QBInitVideoChatCommand;
import com.quickblox.qmunicate.qb.commands.QBJoinGroupDialogCommand;
import com.quickblox.qmunicate.qb.commands.QBLeaveGroupDialogCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadDialogMessagesCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadDialogsCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadFriendListCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadGroupDialogCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadUsersCommand;
import com.quickblox.qmunicate.qb.commands.QBLoginAndJoinDialogsCommand;
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
import com.quickblox.qmunicate.qb.commands.QBUpdateGroupNameCommand;
import com.quickblox.qmunicate.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.qmunicate.qb.commands.QBUpdateUserCommand;
import com.quickblox.qmunicate.qb.commands.push.QBSendPushCommand;
import com.quickblox.qmunicate.qb.helpers.BaseHelper;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.qb.helpers.QBChatRestHelper;
import com.quickblox.qmunicate.qb.helpers.QBFriendListHelper;
import com.quickblox.qmunicate.qb.helpers.QBMultiChatHelper;
import com.quickblox.qmunicate.qb.helpers.QBPrivateChatHelper;
import com.quickblox.qmunicate.qb.helpers.QBVideoChatHelper;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QBService extends Service {

    public static final int AUTH_HELPER = 1;
    public static final int PRIVATE_CHAT_HELPER = 2;
    public static final int MULTI_CHAT_HELPER = 3;
    public static final int FRIEND_LIST_HELPER = 4;
    public static final int VIDEO_CHAT_HELPER = 5;
    public static final int CHAT_REST_HELPER = 6;

    private static final String TAG = QBService.class.getSimpleName();

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final BlockingQueue<Runnable> threadQueue;
    private IBinder binder = new QBServiceBinder();

    private Map<String, ServiceCommand> serviceCommandMap = new HashMap<String, ServiceCommand>();
    private ThreadPoolExecutor threadPool;

    private QBAuthHelper authHelper;
    private QBVideoChatHelper videoChatHelper;
    private QBFriendListHelper friendListHelper;

    private Map<Integer, BaseHelper> helpers = new HashMap<Integer, BaseHelper>();
    private BroadcastReceiver broadcastReceiver = new LoginBroadcastReceiver();

    public QBService() {
        threadQueue = new LinkedBlockingQueue<Runnable>();
        initThreads();

        initHelpers();
        initCommands();
    }

    private void initThreads() {
        threadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT, threadQueue);
        threadPool.allowCoreThreadTimeOut(true);
    }

    private void initHelpers() {
        QBChatRestHelper chatRestHelper = new QBChatRestHelper(this);
        helpers.put(CHAT_REST_HELPER, chatRestHelper);
        authHelper = new QBAuthHelper(this);
        helpers.put(AUTH_HELPER, authHelper);
        QBPrivateChatHelper privateChatHelper = new QBPrivateChatHelper(this);
        helpers.put(PRIVATE_CHAT_HELPER, privateChatHelper);
        QBMultiChatHelper multiChatHelper = new QBMultiChatHelper(this);
        helpers.put(MULTI_CHAT_HELPER, multiChatHelper);
        friendListHelper = new QBFriendListHelper(this);
        helpers.put(FRIEND_LIST_HELPER, friendListHelper);
        videoChatHelper = new QBVideoChatHelper(this);
        helpers.put(VIDEO_CHAT_HELPER, videoChatHelper);
    }

    private void initChatHelpers() {
        QBPrivateChatHelper privateChatHelper = new QBPrivateChatHelper(this);
        helpers.put(PRIVATE_CHAT_HELPER, privateChatHelper);
        QBMultiChatHelper multiChatHelper = new QBMultiChatHelper(this);
        helpers.put(MULTI_CHAT_HELPER, multiChatHelper);
        friendListHelper = new QBFriendListHelper(this);
        helpers.put(FRIEND_LIST_HELPER, friendListHelper);
        videoChatHelper = new QBVideoChatHelper(this);
        helpers.put(VIDEO_CHAT_HELPER, videoChatHelper);
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
        registerImportFriendsCommand();
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
        registerLoadGroupDialogCommand();
        registerLeaveGroupDialogCommand();
        registerAddFriendsToGroupCommand();
        registerUpdateGroupNameCommand();

        registerGetFileCommand();
        registerLoadAttachFileCommand();

        registerLoadChatsDialogsCommand();
        registerUpdateChatDialogCommand();
        registerLoadDialogMessagesCommand();
        registerUpdateStatusMessageCommand();
        registerSendPUshCommand();
        registerLoginAndJoinGroupChat();
    }

    private void registerUpdateGroupNameCommand() {
        QBMultiChatHelper multiChatHelper = (QBMultiChatHelper) getHelper(MULTI_CHAT_HELPER);
        QBUpdateGroupNameCommand updateGroupNameCommand = new QBUpdateGroupNameCommand(this, multiChatHelper,
                QBServiceConsts.UPDATE_GROUP_NAME_SUCCESS_ACTION,
                QBServiceConsts.UPDATE_GROUP_NAME_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.UPDATE_GROUP_NAME_ACTION, updateGroupNameCommand);
    }

    private void registerAddFriendsToGroupCommand() {
        QBMultiChatHelper multiChatHelper = (QBMultiChatHelper) getHelper(MULTI_CHAT_HELPER);
        QBAddFriendsToGroupCommand addFriendsToGroupCommand = new QBAddFriendsToGroupCommand(this,
                multiChatHelper, QBServiceConsts.ADD_FRIENDS_TO_GROUP_SUCCESS_ACTION,
                QBServiceConsts.ADD_FRIENDS_TO_GROUP_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.ADD_FRIENDS_TO_GROUP_ACTION, addFriendsToGroupCommand);
    }

    private void registerLeaveGroupDialogCommand() {
        QBMultiChatHelper multiChatHelper = (QBMultiChatHelper) getHelper(MULTI_CHAT_HELPER);
        QBLeaveGroupDialogCommand leaveGroupDialogCommand = new QBLeaveGroupDialogCommand(this,
                multiChatHelper, QBServiceConsts.LEAVE_GROUP_DIALOG_SUCCESS_ACTION,
                QBServiceConsts.LEAVE_GROUP_DIALOG_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LEAVE_GROUP_DIALOG_ACTION, leaveGroupDialogCommand);
    }

    private void registerLoadGroupDialogCommand() {
        QBMultiChatHelper multiChatHelper = (QBMultiChatHelper) getHelper(MULTI_CHAT_HELPER);
        QBLoadGroupDialogCommand loadGroupDialogCommand = new QBLoadGroupDialogCommand(this, multiChatHelper,
                QBServiceConsts.LOAD_GROUP_DIALOG_SUCCESS_ACTION,
                QBServiceConsts.LOAD_GROUP_DIALOG_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOAD_GROUP_DIALOG_ACTION, loadGroupDialogCommand);
    }

    private void registerJoinGroupChat() {
        QBMultiChatHelper multiChatHelper = (QBMultiChatHelper) getHelper(MULTI_CHAT_HELPER);
        QBJoinGroupDialogCommand joinGroupChatCommand = new QBJoinGroupDialogCommand(this, multiChatHelper,
                QBServiceConsts.JOIN_GROUP_CHAT_SUCCESS_ACTION, QBServiceConsts.JOIN_GROUP_CHAT_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, joinGroupChatCommand);
    }

    private void registerLoginAndJoinGroupChat() {
        QBLoginAndJoinDialogsCommand joinGroupChatsCommand = new QBLoginAndJoinDialogsCommand(this,
                QBServiceConsts.LOGIN_AND_JOIN_CHATS_SUCCESS_ACTION,
                QBServiceConsts.LOGIN_AND_JOIN_CHATS_FAIL_ACTION);
        ServiceCommand loginChatCommand = serviceCommandMap.get(QBServiceConsts.LOGIN_CHAT_ACTION);
        joinGroupChatsCommand.addCommand(loginChatCommand);

        ServiceCommand joinChatCommand = serviceCommandMap.get(QBServiceConsts.JOIN_GROUP_CHAT_ACTION);
        joinGroupChatsCommand.addCommand(joinChatCommand);
        serviceCommandMap.put(QBServiceConsts.LOGIN_AND_JOIN_CHAT_ACTION, joinGroupChatsCommand);
    }

    private void registerCreateGroupChatCommand() {
        QBMultiChatHelper multiChatHelper = (QBMultiChatHelper) getHelper(MULTI_CHAT_HELPER);
        QBCreateGroupDialogCommand createGroupChatCommand = new QBCreateGroupDialogCommand(this,
                multiChatHelper, QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION,
                QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.CREATE_GROUP_CHAT_ACTION, createGroupChatCommand);
    }

    private void registerCreatePrivateChatCommand() {
        QBPrivateChatHelper privateChatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);
        QBCreatePrivateChatCommand createPrivateChatCommand = new QBCreatePrivateChatCommand(this,
                privateChatHelper, QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION,
                QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.CREATE_PRIVATE_CHAT_ACTION, createPrivateChatCommand);
    }

    private void registerLoginChatCommand() {
        QBChatRestHelper chatRestHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        ServiceCommand loginCommand = new QBLoginChatCommand(this, authHelper, chatRestHelper,
                QBServiceConsts.LOGIN_CHAT_SUCCESS_ACTION, QBServiceConsts.LOGIN_CHAT_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOGIN_CHAT_ACTION, loginCommand);
    }

    private void registerLogoutChatCommand() {
        QBChatRestHelper chatRestHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        ServiceCommand logoutCommand = new QBLogoutChatCommand(this, chatRestHelper,
                QBServiceConsts.LOGOUT_CHAT_SUCCESS_ACTION, QBServiceConsts.LOGOUT_CHAT_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOGOUT_CHAT_ACTION, logoutCommand);
    }

    private void registerLoadAttachFileCommand() {
        QBPrivateChatHelper privateChatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);
        ServiceCommand loadAttachFileCommand = new QBLoadAttachFileCommand(this, privateChatHelper,
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
        QBPrivateChatHelper privateChatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);
        QBSendPrivateChatMessageCommand sendMessageCommand = new QBSendPrivateChatMessageCommand(this,
                privateChatHelper, QBServiceConsts.SEND_MESSAGE_SUCCESS_ACTION,
                QBServiceConsts.SEND_MESSAGE_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.SEND_MESSAGE_ACTION, sendMessageCommand);
    }

    private void registerSendGroupMessageCommand() {
        QBMultiChatHelper multiChatHelper = (QBMultiChatHelper) getHelper(MULTI_CHAT_HELPER);
        QBSendGroupDialogMessageCommand sendMessageCommand = new QBSendGroupDialogMessageCommand(this,
                multiChatHelper, QBServiceConsts.SEND_MESSAGE_SUCCESS_ACTION,
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
        QBUpdateUserCommand updateUserCommand = new QBUpdateUserCommand(this, authHelper, friendListHelper,
                QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, QBServiceConsts.UPDATE_USER_FAIL_ACTION);
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

    private void registerImportFriendsCommand() {
        QBImportFriendsCommand importFriendsCommand = new QBImportFriendsCommand(this, friendListHelper,
                QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION, QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.IMPORT_FRIENDS_ACTION, importFriendsCommand);
    }

    private void registerRemoveFriendCommand() {
        QBRemoveFriendCommand removeFriendCommand = new QBRemoveFriendCommand(this,
                QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION, QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.REMOVE_FRIEND_ACTION, removeFriendCommand);
    }

    private void registerLogoutCommand() {
        QBLogoutCommand logoutCommand = new QBLogoutCommand(this, QBServiceConsts.LOGOUT_SUCCESS_ACTION,
                QBServiceConsts.LOGOUT_FAIL_ACTION);
        QBChatRestHelper chatRestHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        QBLogoutAndDestroyChatCommand logoutChatCommand = new QBLogoutAndDestroyChatCommand(this,
                chatRestHelper, QBServiceConsts.LOGOUT_AND_DESTROY_CHAT_SUCCESS_ACTION,
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
        QBUpdateUserCommand updateUserCommand = new QBUpdateUserCommand(this, authHelper, friendListHelper,
                QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, QBServiceConsts.UPDATE_USER_FAIL_ACTION);

        loginCommand.addCommand(loginRestCommand);
        loginCommand.addCommand(updateUserCommand);

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
        QBPrivateChatHelper chatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);
        QBMultiChatHelper multiChatHelper = (QBMultiChatHelper) getHelper(MULTI_CHAT_HELPER);
        QBInitChatCommand initChatCommand = new QBInitChatCommand(this, chatHelper, multiChatHelper,
                QBServiceConsts.INIT_CHAT_SUCCESS_ACTION, QBServiceConsts.INIT_CHAT_FAIL_ACTION);
        QBChatRestHelper chatRestHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        QBLoginChatCommand loginChatCommand = new QBLoginChatCommand(this, authHelper, chatRestHelper,
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
        QBChatRestHelper chatRestHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        QBLoadDialogsCommand chatsDialogsCommand = new QBLoadDialogsCommand(this, chatRestHelper,
                QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION,
                QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, chatsDialogsCommand);
    }

    private void registerUpdateChatDialogCommand() {
        QBPrivateChatHelper privateChatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);
        QBUpdateDialogCommand updateChatDialogCommand = new QBUpdateDialogCommand(this, privateChatHelper,
                QBServiceConsts.UPDATE_CHAT_DIALOG_SUCCESS_ACTION,
                QBServiceConsts.UPDATE_CHAT_DIALOG_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.UPDATE_CHAT_DIALOG_ACTION, updateChatDialogCommand);
    }

    private void registerLoadDialogMessagesCommand() {
        QBChatRestHelper chatHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        QBLoadDialogMessagesCommand loadDialogMessagesCommand = new QBLoadDialogMessagesCommand(this,
                chatHelper, QBServiceConsts.LOAD_DIALOG_MESSAGES_SUCCESS_ACTION,
                QBServiceConsts.LOAD_DIALOG_MESSAGES_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.LOAD_DIALOG_MESSAGES_ACTION, loadDialogMessagesCommand);
    }

    private void registerUpdateStatusMessageCommand() {
        QBPrivateChatHelper privateChatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);
        QBUpdateStatusMessageCommand updateStatusMessageCommand = new QBUpdateStatusMessageCommand(this,
                privateChatHelper, QBServiceConsts.UPDATE_STATUS_MESSAGE_SUCCESS_ACTION,
                QBServiceConsts.UPDATE_STATUS_MESSAGE_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.UPDATE_STATUS_MESSAGE_ACTION, updateStatusMessageCommand);
    }

    private void registerSendPUshCommand() {
        QBSendPushCommand sendPushCommand = new QBSendPushCommand(this,
                QBServiceConsts.SEND_PUSH_MESSAGES_SUCCESS_ACTION,
                QBServiceConsts.SEND_PUSH_MESSAGES_FAIL_ACTION);
        serviceCommandMap.put(QBServiceConsts.SEND_PUSH_ACTION, sendPushCommand);
    }

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(QBServiceConsts.LOGIN_ACTION);
        if (broadcastReceiver != null) {
            registerReceiver(broadcastReceiver, filter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
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

    public BaseHelper getHelper(int helperId) {
        return helpers.get(helperId);
    }

    private void startAsync(final ServiceCommand command, final Intent intent) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "executing with resultAction=" + intent.getAction());
                try {
                    command.execute(intent.getExtras());
                } catch (QBResponseException e) {
                    ErrorUtils.logError(e);
                    if (Utils.isTokenDestroyedError(e) && AppSession.getSession().isSessionExist()) {
                        refreshSession();
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    public void refreshSession() {
        if (LoginType.EMAIL.equals(AppSession.getSession().getLoginType())) {
            QBLoginRestCommand.start(this, AppSession.getSession().getUser());
        } else {
            QBLoginRestWithSocialCommand.start(this, QBProvider.FACEBOOK,
                    Session.getActiveSession().getAccessToken(), null);
        }
    }

    public QBVideoChatHelper getVideoChatHelper() {
        return videoChatHelper;
    }

    public class QBServiceBinder extends Binder {

        public QBService getService() {
            return QBService.this;
        }
    }

    private class LoginBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO will be defined in next refactor
        }
    }
}
