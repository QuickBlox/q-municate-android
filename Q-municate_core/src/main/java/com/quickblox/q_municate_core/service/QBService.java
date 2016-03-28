package com.quickblox.q_municate_core.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.CompositeServiceCommand;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.friend.QBAcceptFriendCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBAddFriendCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBAddFriendsToGroupCommand;
import com.quickblox.q_municate_core.qb.commands.QBChangePasswordCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBCreateGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBCreatePrivateChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.commands.QBFindUsersCommand;
import com.quickblox.q_municate_core.qb.commands.QBGetFileCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBImportFriendsCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBInitChatServiceCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBInitChatsCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBInitFriendListCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBInitCallChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBJoinGroupChatsCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLeaveGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoadDialogMessagesCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoadDialogsCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBLoadFriendListCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadUserCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBLoginCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBLoginRestCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLogoutAndDestroyChatCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBLogoutCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBLogoutRestCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBRejectFriendCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBRemoveFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBResetPasswordCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBSignUpCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBSignUpRestCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBSocialLoginCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate_core.qb.commands.push.QBSendPushCommand;
import com.quickblox.q_municate_core.qb.helpers.BaseHelper;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBRestHelper;
import com.quickblox.q_municate_core.qb.helpers.QBCallChatHelper;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QBService extends Service {

    public static final int AUTH_HELPER = 1;
    public static final int PRIVATE_CHAT_HELPER = 2;
    public static final int GROUP_CHAT_HELPER = 3;
    public static final int FRIEND_LIST_HELPER = 4;
    public static final int CALL_CHAT_HELPER = 5;
    public static final int CHAT_REST_HELPER = 6;
    public static final int REST_HELPER = 7;

    private static final String TAG = QBService.class.getSimpleName();

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final BlockingQueue<Runnable> threadQueue;
    private IBinder binder = new QBServiceBinder();

    private Map<String, ServiceCommand> serviceCommandMap = new HashMap<>();
    private ThreadPoolExecutor threadPool;

    private Map<Integer, BaseHelper> helpersMap;
    private BroadcastReceiver broadcastReceiver;

    public QBService() {
        threadQueue = new LinkedBlockingQueue<>();
        helpersMap = new HashMap<>();
        broadcastReceiver = new LoginBroadcastReceiver();

        initThreads();

        initHelpers();
        initCommands();
    }

    private void initThreads() {
        threadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, threadQueue);
        threadPool.allowCoreThreadTimeOut(true);
    }

    private void initHelpers() {
        helpersMap.put(CHAT_REST_HELPER, new QBChatRestHelper(this));
        helpersMap.put(AUTH_HELPER, new QBAuthHelper(this));
        helpersMap.put(PRIVATE_CHAT_HELPER, new QBPrivateChatHelper(this));
        helpersMap.put(GROUP_CHAT_HELPER, new QBGroupChatHelper(this));
        helpersMap.put(FRIEND_LIST_HELPER, new QBFriendListHelper(this));
        helpersMap.put(CALL_CHAT_HELPER, new QBCallChatHelper(this));
        helpersMap.put(REST_HELPER, new QBRestHelper(this));
    }

    private void initCommands() {
        // first call init
        registerInitCallChatCommand();

        // login/signUp commands
        registerLoginRestCommand();
        registerLoginRestSocialCommand();
        registerSignUpCommand();
        registerLoginCommand();

        // chat commands
        registerCreatePrivateChatCommand();
        registerCreateGroupChatCommand();
        registerUpdateGroupDialogCommand();
        registerDeleteChatCommand();
        registerUpdateStatusMessageCommand();
        registerLogoutAndDestroyChatCommand();
        registerAddFriendsToGroupCommand();
        registerLeaveGroupDialogCommand();
        registerLoadAttachFileCommand();
        registerLoadChatsDialogsCommand();
        registerLoadDialogMessagesCommand();
        registerJoinGroupChatsCommand();
        registerLoginChatCommand();

        // users/friends commands
        registerLoadUsersCommand();
        registerLoadFriendsCommand();
        registerUpdateUserCommand();
        registerLoadUserCommand();
        registerAddFriendCommand();
        registerAcceptFriendCommand();
        registerRemoveFriendCommand();
        registerRejectFriendCommand();
        registerImportFriendsCommand();

        // other commands
        registerGetFileCommand();
        registerChangePasswordCommand();
        registerResetPasswordCommand();
        registerSendPushCommand();

        // logout commands
        registerLogoutCommand();
    }

    // ------------------ login commands
    private void registerLoginRestCommand() {
        QBAuthHelper authHelper = (QBAuthHelper) getHelper(AUTH_HELPER);

        QBLoginRestCommand loginRestCommand = new QBLoginRestCommand(this, authHelper,
                QBServiceConsts.LOGIN_REST_SUCCESS_ACTION,
                QBServiceConsts.LOGIN_REST_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOGIN_REST_ACTION, loginRestCommand);
    }

    private void registerLoginRestSocialCommand() {
        QBAuthHelper authHelper = (QBAuthHelper) getHelper(AUTH_HELPER);

        QBSocialLoginCommand loginRestCommand = new QBSocialLoginCommand(this, authHelper,
                QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION,
                QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.SOCIAL_LOGIN_ACTION, loginRestCommand);
    }

    private void registerLoginChatCommand() {
        CompositeServiceCommand loginChatCommand = new QBLoginChatCompositeCommand(this,
                QBServiceConsts.LOGIN_CHAT_COMPOSITE_SUCCESS_ACTION,
                QBServiceConsts.LOGIN_CHAT_COMPOSITE_FAIL_ACTION);

        addLoginChatAndInitCommands(loginChatCommand);

        serviceCommandMap.put(QBServiceConsts.LOGIN_CHAT_COMPOSITE_ACTION, loginChatCommand);
    }

    private void registerSignUpCommand() {
        QBAuthHelper authHelper = (QBAuthHelper) getHelper(AUTH_HELPER);

        QBSignUpCommand signUpCommand = new QBSignUpCommand(this,
                QBServiceConsts.SIGNUP_SUCCESS_ACTION,
                QBServiceConsts.SIGNUP_FAIL_ACTION);
        QBSignUpRestCommand signUpRestCommand = new QBSignUpRestCommand(this, authHelper,
                QBServiceConsts.SIGNUP_REST_SUCCESS_ACTION,
                QBServiceConsts.SIGNUP_REST_FAIL_ACTION);

        signUpCommand.addCommand(signUpRestCommand);

        addLoginChatAndInitCommands(signUpCommand);

        serviceCommandMap.put(QBServiceConsts.SIGNUP_ACTION, signUpCommand);
    }

    // ------------------ chat commands
    private void registerCreatePrivateChatCommand() {
        QBPrivateChatHelper privateChatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);

        QBCreatePrivateChatCommand createPrivateChatCommand = new QBCreatePrivateChatCommand(this,
                privateChatHelper,
                QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION,
                QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.CREATE_PRIVATE_CHAT_ACTION, createPrivateChatCommand);
    }

    private void registerCreateGroupChatCommand() {
        QBGroupChatHelper groupChatHelper = (QBGroupChatHelper) getHelper(GROUP_CHAT_HELPER);

        QBCreateGroupDialogCommand createGroupChatCommand = new QBCreateGroupDialogCommand(this,
                groupChatHelper,
                QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION,
                QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.CREATE_GROUP_CHAT_ACTION, createGroupChatCommand);
    }

    private void registerUpdateGroupDialogCommand() {
        QBGroupChatHelper groupChatHelper = (QBGroupChatHelper) getHelper(GROUP_CHAT_HELPER);

        QBUpdateGroupDialogCommand updateGroupNameCommand = new QBUpdateGroupDialogCommand(this,
                groupChatHelper,
                QBServiceConsts.UPDATE_GROUP_DIALOG_SUCCESS_ACTION,
                QBServiceConsts.UPDATE_GROUP_DIALOG_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.UPDATE_GROUP_DIALOG_ACTION, updateGroupNameCommand);
    }

    private void registerDeleteChatCommand() {
        QBGroupChatHelper groupChatHelper = (QBGroupChatHelper) getHelper(GROUP_CHAT_HELPER);

        ServiceCommand deleteChatCommand = new QBDeleteChatCommand(this, groupChatHelper,
                QBServiceConsts.DELETE_DIALOG_SUCCESS_ACTION,
                QBServiceConsts.DELETE_DIALOG_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.DELETE_DIALOG_ACTION, deleteChatCommand);
    }

    private void registerUpdateStatusMessageCommand() {
        QBPrivateChatHelper privateChatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);

        QBUpdateStatusMessageCommand updateStatusMessageCommand = new QBUpdateStatusMessageCommand(this,
                privateChatHelper,
                QBServiceConsts.UPDATE_STATUS_MESSAGE_SUCCESS_ACTION,
                QBServiceConsts.UPDATE_STATUS_MESSAGE_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.UPDATE_STATUS_MESSAGE_ACTION, updateStatusMessageCommand);
    }

    private void registerLogoutAndDestroyChatCommand() {
        QBChatRestHelper chatRestHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        QBGroupChatHelper groupChatHelper = (QBGroupChatHelper) getHelper(GROUP_CHAT_HELPER);

        ServiceCommand logoutCommand = new QBLogoutAndDestroyChatCommand(this, chatRestHelper,
                groupChatHelper,
                QBServiceConsts.LOGOUT_CHAT_SUCCESS_ACTION,
                QBServiceConsts.LOGOUT_CHAT_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOGOUT_AND_DESTROY_CHAT_ACTION, logoutCommand);
    }

    private void registerAddFriendsToGroupCommand() {
        QBGroupChatHelper groupChatHelper = (QBGroupChatHelper) getHelper(GROUP_CHAT_HELPER);

        QBAddFriendsToGroupCommand addFriendsToGroupCommand = new QBAddFriendsToGroupCommand(this,
                groupChatHelper,
                QBServiceConsts.ADD_FRIENDS_TO_GROUP_SUCCESS_ACTION,
                QBServiceConsts.ADD_FRIENDS_TO_GROUP_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.ADD_FRIENDS_TO_GROUP_ACTION, addFriendsToGroupCommand);
    }

    private void registerLeaveGroupDialogCommand() {
        QBGroupChatHelper groupChatHelper = (QBGroupChatHelper) getHelper(GROUP_CHAT_HELPER);

        QBLeaveGroupDialogCommand leaveGroupDialogCommand = new QBLeaveGroupDialogCommand(this,
                groupChatHelper,
                QBServiceConsts.LEAVE_GROUP_DIALOG_SUCCESS_ACTION,
                QBServiceConsts.LEAVE_GROUP_DIALOG_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LEAVE_GROUP_DIALOG_ACTION, leaveGroupDialogCommand);
    }

    private void registerJoinGroupChatsCommand() {
        QBGroupChatHelper groupChatHelper = (QBGroupChatHelper) getHelper(GROUP_CHAT_HELPER);

        QBJoinGroupChatsCommand joinGroupChatsCommand = new QBJoinGroupChatsCommand(this, groupChatHelper,
                QBServiceConsts.JOIN_GROUP_CHAT_SUCCESS_ACTION,
                QBServiceConsts.JOIN_GROUP_CHAT_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, joinGroupChatsCommand);
    }

    private void registerLoadAttachFileCommand() {
        QBPrivateChatHelper privateChatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);

        ServiceCommand loadAttachFileCommand = new QBLoadAttachFileCommand(this, privateChatHelper,
                QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION,
                QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOAD_ATTACH_FILE_ACTION, loadAttachFileCommand);
    }

    private void registerLoadChatsDialogsCommand() {
        QBGroupChatHelper groupChatHelper = (QBGroupChatHelper) getHelper(GROUP_CHAT_HELPER);
        QBPrivateChatHelper privateChatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);

        QBLoadDialogsCommand chatsDialogsCommand = new QBLoadDialogsCommand(this, privateChatHelper, groupChatHelper,
                QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION,
                QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, chatsDialogsCommand);
    }

    private void registerLoadDialogMessagesCommand() {
        QBGroupChatHelper groupChatHelper = (QBGroupChatHelper) getHelper(GROUP_CHAT_HELPER);

        QBLoadDialogMessagesCommand loadDialogMessagesCommand = new QBLoadDialogMessagesCommand(this,
                groupChatHelper,
                QBServiceConsts.LOAD_DIALOG_MESSAGES_SUCCESS_ACTION,
                QBServiceConsts.LOAD_DIALOG_MESSAGES_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOAD_DIALOG_MESSAGES_ACTION, loadDialogMessagesCommand);
    }

    // ------------------ users/friends commands
    private void registerLoadUsersCommand() {
        QBFindUsersCommand userSearchCommand = new QBFindUsersCommand(this,
                QBServiceConsts.FIND_USERS_SUCCESS_ACTION,
                QBServiceConsts.FIND_USERS_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.FIND_USERS_ACTION, userSearchCommand);
    }

    private void registerLoadFriendsCommand() {
        QBFriendListHelper friendListHelper = (QBFriendListHelper) getHelper(FRIEND_LIST_HELPER);

        QBLoadFriendListCommand loadFriendListCommand = new QBLoadFriendListCommand(this, friendListHelper,
                QBServiceConsts.LOAD_FRIENDS_SUCCESS_ACTION,
                QBServiceConsts.LOAD_FRIENDS_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOAD_FRIENDS_ACTION, loadFriendListCommand);
    }

    private void registerUpdateUserCommand() {
        QBAuthHelper authHelper = (QBAuthHelper) getHelper(AUTH_HELPER);
        QBFriendListHelper friendListHelper = (QBFriendListHelper) getHelper(FRIEND_LIST_HELPER);

        QBUpdateUserCommand updateUserCommand = new QBUpdateUserCommand(this, authHelper, friendListHelper,
                QBServiceConsts.UPDATE_USER_SUCCESS_ACTION,
                QBServiceConsts.UPDATE_USER_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.UPDATE_USER_ACTION, updateUserCommand);
    }

    private void registerLoadUserCommand() {
        QBRestHelper restHelper = (QBRestHelper) getHelper(REST_HELPER);

        QBLoadUserCommand loadUserCommand = new QBLoadUserCommand(this, restHelper,
                QBServiceConsts.LOAD_USER_SUCCESS_ACTION,
                QBServiceConsts.LOAD_USER_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOAD_USER_ACTION, loadUserCommand);
    }

    private void registerAddFriendCommand() {
        QBFriendListHelper friendListHelper = (QBFriendListHelper) getHelper(FRIEND_LIST_HELPER);

        QBAddFriendCommand addFriendCommand = new QBAddFriendCommand(this, friendListHelper,
                QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION,
                QBServiceConsts.ADD_FRIEND_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.ADD_FRIEND_ACTION, addFriendCommand);
    }

    private void registerAcceptFriendCommand() {
        QBFriendListHelper friendListHelper = (QBFriendListHelper) getHelper(FRIEND_LIST_HELPER);

        QBAcceptFriendCommand acceptFriendCommand = new QBAcceptFriendCommand(this, friendListHelper,
                QBServiceConsts.ACCEPT_FRIEND_SUCCESS_ACTION,
                QBServiceConsts.ACCEPT_FRIEND_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.ACCEPT_FRIEND_ACTION, acceptFriendCommand);
    }

    private void registerRemoveFriendCommand() {
        QBFriendListHelper friendListHelper = (QBFriendListHelper) getHelper(FRIEND_LIST_HELPER);

        QBRemoveFriendCommand removeFriendCommand = new QBRemoveFriendCommand(this, friendListHelper,
                QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION,
                QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.REMOVE_FRIEND_ACTION, removeFriendCommand);
    }

    private void registerRejectFriendCommand() {
        QBFriendListHelper friendListHelper = (QBFriendListHelper) getHelper(FRIEND_LIST_HELPER);

        QBRejectFriendCommand rejectFriendCommand = new QBRejectFriendCommand(this, friendListHelper,
                QBServiceConsts.REJECT_FRIEND_SUCCESS_ACTION,
                QBServiceConsts.REJECT_FRIEND_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.REJECT_FRIEND_ACTION, rejectFriendCommand);
    }

    private void registerImportFriendsCommand() {
        QBFriendListHelper friendListHelper = (QBFriendListHelper) getHelper(FRIEND_LIST_HELPER);

        QBImportFriendsCommand importFriendsCommand = new QBImportFriendsCommand(this, friendListHelper,
                QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION,
                QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.IMPORT_FRIENDS_ACTION, importFriendsCommand);
    }


    // ------------------ other commands
    private void registerGetFileCommand() {
        ServiceCommand getFileCommand = new QBGetFileCommand(this,
                QBServiceConsts.GET_FILE_SUCCESS_ACTION,
                QBServiceConsts.GET_FILE_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.GET_FILE_ACTION, getFileCommand);
    }

    private void registerResetPasswordCommand() {
        QBAuthHelper authHelper = (QBAuthHelper) getHelper(AUTH_HELPER);

        QBResetPasswordCommand resetPasswordCommand = new QBResetPasswordCommand(this, authHelper,
                QBServiceConsts.RESET_PASSWORD_SUCCESS_ACTION,
                QBServiceConsts.RESET_PASSWORD_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.RESET_PASSWORD_ACTION, resetPasswordCommand);
    }

    private void registerChangePasswordCommand() {
        QBAuthHelper authHelper = (QBAuthHelper) getHelper(AUTH_HELPER);

        QBChangePasswordCommand changePasswordCommand = new QBChangePasswordCommand(this, authHelper,
                QBServiceConsts.CHANGE_PASSWORD_SUCCESS_ACTION,
                QBServiceConsts.CHANGE_PASSWORD_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.CHANGE_PASSWORD_ACTION, changePasswordCommand);
    }

    private void registerSendPushCommand() {
        QBSendPushCommand sendPushCommand = new QBSendPushCommand(this,
                QBServiceConsts.SEND_PUSH_MESSAGES_SUCCESS_ACTION,
                QBServiceConsts.SEND_PUSH_MESSAGES_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.SEND_PUSH_ACTION, sendPushCommand);
    }

    private void registerInitCallChatCommand() {
        QBCallChatHelper qbCallChatHelper = (QBCallChatHelper) getHelper(CALL_CHAT_HELPER);

        QBInitCallChatCommand qbInitCallChatCommand = new QBInitCallChatCommand(this, qbCallChatHelper,
                QBServiceConsts.INIT_VIDEO_CHAT_SUCCESS_ACTION,
                QBServiceConsts.INIT_VIDEO_CHAT_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.INIT_CALL_CHAT_ACTION, qbInitCallChatCommand);
    }

    // ------------------ logout commands
    private void registerLogoutCommand() {
        QBAuthHelper authHelper = (QBAuthHelper) getHelper(AUTH_HELPER);

        QBLogoutCompositeCommand logoutCommand = new QBLogoutCompositeCommand(this,
                QBServiceConsts.LOGOUT_SUCCESS_ACTION,
                QBServiceConsts.LOGOUT_FAIL_ACTION);
        QBLogoutAndDestroyChatCommand logoutChatCommand = (QBLogoutAndDestroyChatCommand) serviceCommandMap.get(
                QBServiceConsts.LOGOUT_AND_DESTROY_CHAT_ACTION);
        QBLogoutRestCommand logoutRestCommand = new QBLogoutRestCommand(this, authHelper,
                QBServiceConsts.LOGOUT_REST_SUCCESS_ACTION,
                QBServiceConsts.LOGOUT_REST_FAIL_ACTION);

        logoutCommand.addCommand(logoutChatCommand);
        logoutCommand.addCommand(logoutRestCommand);

        serviceCommandMap.put(QBServiceConsts.LOGOUT_ACTION, logoutCommand);
    }

    private void registerLoginCommand() {
        QBLoginCompositeCommand loginCommand = new QBLoginCompositeCommand(this,
                QBServiceConsts.LOGIN_SUCCESS_ACTION, QBServiceConsts.LOGIN_FAIL_ACTION);
        QBLoginRestCommand loginRestCommand = (QBLoginRestCommand) serviceCommandMap.get(
                QBServiceConsts.LOGIN_REST_ACTION);

        loginCommand.addCommand(loginRestCommand);

        serviceCommandMap.put(QBServiceConsts.LOGIN_ACTION, loginCommand);
    }

    private void addLoginChatAndInitCommands(CompositeServiceCommand loginCommand) {
        QBChatRestHelper chatRestHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        QBPrivateChatHelper privateChatHelper = (QBPrivateChatHelper) getHelper(PRIVATE_CHAT_HELPER);
        QBGroupChatHelper groupChatHelper = (QBGroupChatHelper) getHelper(GROUP_CHAT_HELPER);
        QBFriendListHelper friendListHelper = (QBFriendListHelper) getHelper(FRIEND_LIST_HELPER);

        QBInitChatServiceCommand initChatServiceCommand = new QBInitChatServiceCommand(this, chatRestHelper,
                QBServiceConsts.INIT_CHAT_SERVICE_SUCCESS_ACTION,
                QBServiceConsts.INIT_CHAT_SERVICE_FAIL_ACTION);
        QBLoginChatCommand loginChatCommand = new QBLoginChatCommand(this, chatRestHelper,
                QBServiceConsts.LOGIN_CHAT_SUCCESS_ACTION,
                QBServiceConsts.LOGIN_CHAT_FAIL_ACTION);
        QBInitChatsCommand initChatsCommand = new QBInitChatsCommand(this, privateChatHelper, groupChatHelper,
                QBServiceConsts.INIT_CHATS_SUCCESS_ACTION,
                QBServiceConsts.INIT_CHATS_FAIL_ACTION);
        QBInitFriendListCommand initFriendListCommand = new QBInitFriendListCommand(this, friendListHelper, privateChatHelper,
                QBServiceConsts.INIT_FRIEND_LIST_SUCCESS_ACTION,
                QBServiceConsts.INIT_FRIEND_LIST_FAIL_ACTION);
        QBLoadFriendListCommand loadFriendListCommand = new QBLoadFriendListCommand(this, friendListHelper,
                QBServiceConsts.LOAD_FRIENDS_SUCCESS_ACTION,
                QBServiceConsts.LOAD_FRIENDS_FAIL_ACTION);
        QBInitCallChatCommand initVideoChatCommand = (QBInitCallChatCommand) serviceCommandMap.get(QBServiceConsts.INIT_CALL_CHAT_ACTION);

        loginCommand.addCommand(initChatServiceCommand);
        loginCommand.addCommand(loginChatCommand);
        loginCommand.addCommand(initChatsCommand);
        loginCommand.addCommand(initFriendListCommand);
        loginCommand.addCommand(loadFriendListCommand);
        loginCommand.addCommand(initVideoChatCommand);
    }

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(QBServiceConsts.RE_LOGIN_IN_CHAT_SUCCESS_ACTION);
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
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
    public void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public BaseHelper getHelper(int helperId) {
        return helpersMap.get(helperId);
    }

    private void startAsync(final ServiceCommand command, final Intent intent) {
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    command.execute(intent.getExtras());
                } catch (QBResponseException e) {
                    ErrorUtils.logError(e);
                    if (Utils.isExactError(e, ConstsCore.SESSION_DOES_NOT_EXIST)) {
                        refreshSession();
                    } else if (Utils.isTokenDestroyedError(e)) {
                        forceReLogin();
                    }
                } catch (Exception e) {
                    ErrorUtils.logError(e);
                }
            }
        });
    }

    private void forceReLogin() {
        Intent intent = new Intent(QBServiceConsts.FORCE_RELOGIN);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void refreshSession() {
        Intent intent = new Intent(QBServiceConsts.REFRESH_SESSION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class QBServiceBinder extends Binder {

        public QBService getService() {
            return QBService.this;
        }
    }

    private class LoginBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive " + intent.getAction());
            String action = intent.getAction();
            if (action != null && QBServiceConsts.RE_LOGIN_IN_CHAT_SUCCESS_ACTION.equals(action)) {
                ((QBBaseChatHelper) getHelper(PRIVATE_CHAT_HELPER)).init(AppSession.getSession().getUser());
                ((QBBaseChatHelper) getHelper(GROUP_CHAT_HELPER)).init(AppSession.getSession().getUser());
                ((QBCallChatHelper) getHelper(CALL_CHAT_HELPER)).init(QBChatService.getInstance());
            }
        }
    }
}