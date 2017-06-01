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
import com.quickblox.q_municate_core.qb.commands.chat.QBLoadDialogByIdsCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBAcceptFriendCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBAddFriendCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBAddFriendsToGroupCommand;
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
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLogoutAndDestroyChatCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBLogoutCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBRejectFriendCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBRemoveFriendCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.qb.commands.push.QBSendPushCommand;
import com.quickblox.q_municate_core.qb.helpers.BaseHelper;
import com.quickblox.q_municate_core.qb.helpers.QBChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.qb.helpers.QBRestHelper;
import com.quickblox.q_municate_core.qb.helpers.QBCallChatHelper;
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
    public static final int CHAT_HELPER = 2;
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
        helpersMap.put(CHAT_HELPER, new QBChatHelper(this));
        helpersMap.put(FRIEND_LIST_HELPER, new QBFriendListHelper(this));
        helpersMap.put(CALL_CHAT_HELPER, new QBCallChatHelper(this));
        helpersMap.put(REST_HELPER, new QBRestHelper(this));
    }

    private void initCommands() {
        // first call init
        registerInitCallChatCommand();

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
        registerLoadDialogByIdsCommandCommand();
        registerLoadDialogMessagesCommand();
        registerJoinGroupChatsCommand();
        registerLoginChatCommand();

        // users/friends commands
        registerLoadUsersCommand();
        registerLoadFriendsCommand();
        registerAddFriendCommand();
        registerAcceptFriendCommand();
        registerRemoveFriendCommand();
        registerRejectFriendCommand();
        registerImportFriendsCommand();

        // other commands
        registerGetFileCommand();
        registerSendPushCommand();

        // logout commands
        registerLogoutCommand();
    }


    private void registerLoginChatCommand() {
        CompositeServiceCommand loginChatCompositeCommand = new QBLoginChatCompositeCommand(this,
                QBServiceConsts.LOGIN_CHAT_COMPOSITE_SUCCESS_ACTION,
                QBServiceConsts.LOGIN_CHAT_COMPOSITE_FAIL_ACTION);

        QBChatRestHelper chatRestHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        QBLoginChatCommand loginChatCommand = new QBLoginChatCommand(this, chatRestHelper,
                QBServiceConsts.LOGIN_CHAT_SUCCESS_ACTION,
                QBServiceConsts.LOGIN_CHAT_FAIL_ACTION);

        addLoginChatAndInitCommands(loginChatCompositeCommand, loginChatCommand);

        serviceCommandMap.put(QBServiceConsts.LOGIN_CHAT_ACTION, loginChatCommand);
        serviceCommandMap.put(QBServiceConsts.LOGIN_CHAT_COMPOSITE_ACTION, loginChatCompositeCommand);
    }


    // ------------------ chat commands
    private void registerCreatePrivateChatCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        QBCreatePrivateChatCommand createPrivateChatCommand = new QBCreatePrivateChatCommand(this,
                chatHelper,
                QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION,
                QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.CREATE_PRIVATE_CHAT_ACTION, createPrivateChatCommand);
    }

    private void registerCreateGroupChatCommand() {
        QBChatHelper groupChatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        QBCreateGroupDialogCommand createGroupChatCommand = new QBCreateGroupDialogCommand(this,
                groupChatHelper,
                QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION,
                QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.CREATE_GROUP_CHAT_ACTION, createGroupChatCommand);
    }

    private void registerUpdateGroupDialogCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        QBUpdateGroupDialogCommand updateGroupNameCommand = new QBUpdateGroupDialogCommand(this,
                chatHelper,
                QBServiceConsts.UPDATE_GROUP_DIALOG_SUCCESS_ACTION,
                QBServiceConsts.UPDATE_GROUP_DIALOG_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.UPDATE_GROUP_DIALOG_ACTION, updateGroupNameCommand);
    }

    private void registerDeleteChatCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        ServiceCommand deleteChatCommand = new QBDeleteChatCommand(this, chatHelper,
                QBServiceConsts.DELETE_DIALOG_SUCCESS_ACTION,
                QBServiceConsts.DELETE_DIALOG_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.DELETE_DIALOG_ACTION, deleteChatCommand);
    }

    private void registerUpdateStatusMessageCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        QBUpdateStatusMessageCommand updateStatusMessageCommand = new QBUpdateStatusMessageCommand(this,
                chatHelper,
                QBServiceConsts.UPDATE_STATUS_MESSAGE_SUCCESS_ACTION,
                QBServiceConsts.UPDATE_STATUS_MESSAGE_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.UPDATE_STATUS_MESSAGE_ACTION, updateStatusMessageCommand);
    }

    private void registerLogoutAndDestroyChatCommand() {
        QBChatRestHelper chatRestHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        ServiceCommand logoutCommand = new QBLogoutAndDestroyChatCommand(this, chatRestHelper,
                chatHelper,
                QBServiceConsts.LOGOUT_CHAT_SUCCESS_ACTION,
                QBServiceConsts.LOGOUT_CHAT_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOGOUT_AND_DESTROY_CHAT_ACTION, logoutCommand);
    }

    private void registerAddFriendsToGroupCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        QBAddFriendsToGroupCommand addFriendsToGroupCommand = new QBAddFriendsToGroupCommand(this,
                chatHelper,
                QBServiceConsts.ADD_FRIENDS_TO_GROUP_SUCCESS_ACTION,
                QBServiceConsts.ADD_FRIENDS_TO_GROUP_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.ADD_FRIENDS_TO_GROUP_ACTION, addFriendsToGroupCommand);
    }

    private void registerLeaveGroupDialogCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        QBLeaveGroupDialogCommand leaveGroupDialogCommand = new QBLeaveGroupDialogCommand(this,
                chatHelper,
                QBServiceConsts.LEAVE_GROUP_DIALOG_SUCCESS_ACTION,
                QBServiceConsts.LEAVE_GROUP_DIALOG_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LEAVE_GROUP_DIALOG_ACTION, leaveGroupDialogCommand);
    }

    private void registerJoinGroupChatsCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        QBJoinGroupChatsCommand joinGroupChatsCommand = new QBJoinGroupChatsCommand(this, chatHelper,
                QBServiceConsts.JOIN_GROUP_CHAT_SUCCESS_ACTION,
                QBServiceConsts.JOIN_GROUP_CHAT_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, joinGroupChatsCommand);
    }

    private void registerLoadAttachFileCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        ServiceCommand loadAttachFileCommand = new QBLoadAttachFileCommand(this, chatHelper,
                QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION,
                QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOAD_ATTACH_FILE_ACTION, loadAttachFileCommand);
    }

    private void registerLoadChatsDialogsCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        QBLoadDialogsCommand chatsDialogsCommand = new QBLoadDialogsCommand(this, chatHelper,
                QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION,
                QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, chatsDialogsCommand);
    }

    private void registerLoadDialogByIdsCommandCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        QBLoadDialogByIdsCommand chatsDialogsCommand = new QBLoadDialogByIdsCommand(this, chatHelper,
                QBServiceConsts.LOAD_CHATS_DIALOGS_BY_IDS_SUCCESS_ACTION,
                QBServiceConsts.LOAD_CHATS_DIALOGS_BY_IDS_FAIL_ACTION);

        serviceCommandMap.put(QBServiceConsts.LOAD_CHATS_DIALOGS_BY_IDS_ACTION, chatsDialogsCommand);
    }

    private void registerLoadDialogMessagesCommand() {
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);

        QBLoadDialogMessagesCommand loadDialogMessagesCommand = new QBLoadDialogMessagesCommand(this,
                chatHelper,
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
        QBLogoutCompositeCommand logoutCommand = new QBLogoutCompositeCommand(this,
                QBServiceConsts.LOGOUT_SUCCESS_ACTION,
                QBServiceConsts.LOGOUT_FAIL_ACTION);
        QBLogoutAndDestroyChatCommand logoutChatCommand = (QBLogoutAndDestroyChatCommand) serviceCommandMap.get(
                QBServiceConsts.LOGOUT_AND_DESTROY_CHAT_ACTION);

        logoutCommand.addCommand(logoutChatCommand);

        serviceCommandMap.put(QBServiceConsts.LOGOUT_ACTION, logoutCommand);
    }

    private void addLoginChatAndInitCommands(CompositeServiceCommand loginCommand, ServiceCommand loginChatCommand) {
        QBChatRestHelper chatRestHelper = (QBChatRestHelper) getHelper(CHAT_REST_HELPER);
        QBChatHelper chatHelper = (QBChatHelper) getHelper(CHAT_HELPER);
        QBFriendListHelper friendListHelper = (QBFriendListHelper) getHelper(FRIEND_LIST_HELPER);

        QBInitChatServiceCommand initChatServiceCommand = new QBInitChatServiceCommand(this, chatHelper,
                chatRestHelper,
                QBServiceConsts.INIT_CHAT_SERVICE_SUCCESS_ACTION,
                QBServiceConsts.INIT_CHAT_SERVICE_FAIL_ACTION);
        QBInitChatsCommand initChatsCommand = new QBInitChatsCommand(this, chatHelper,
                QBServiceConsts.INIT_CHATS_SUCCESS_ACTION,
                QBServiceConsts.INIT_CHATS_FAIL_ACTION);
        QBInitFriendListCommand initFriendListCommand = new QBInitFriendListCommand(this, friendListHelper, chatHelper,
                QBServiceConsts.INIT_FRIEND_LIST_SUCCESS_ACTION,
                QBServiceConsts.INIT_FRIEND_LIST_FAIL_ACTION);
        QBLoadFriendListCommand loadFriendListCommand = new QBLoadFriendListCommand(this, friendListHelper,
                QBServiceConsts.LOAD_FRIENDS_SUCCESS_ACTION,
                QBServiceConsts.LOAD_FRIENDS_FAIL_ACTION);
        QBInitCallChatCommand initVideoChatCommand = (QBInitCallChatCommand) serviceCommandMap.get(QBServiceConsts.INIT_CALL_CHAT_ACTION);

        ServiceCommand joinCommand = serviceCommandMap.get(QBServiceConsts.JOIN_GROUP_CHAT_ACTION);

        loginCommand.addCommand(initChatServiceCommand);
        loginCommand.addCommand(loginChatCommand);
        loginCommand.addCommand(initChatsCommand);
        loginCommand.addCommand(joinCommand);
        loginCommand.addCommand(initFriendListCommand);
        loginCommand.addCommand(loadFriendListCommand);
        loginCommand.addCommand(initVideoChatCommand);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        IntentFilter filter = new IntentFilter();
        filter.addAction(QBServiceConsts.RE_LOGIN_IN_CHAT_SUCCESS_ACTION);
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
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
        Log.d(TAG, "onDestroy()");
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }
        threadPool.shutdown();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
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
                    if (Utils.isTokenDestroyedError(e)) {
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
                ((QBChatHelper) getHelper(CHAT_HELPER)).init(AppSession.getSession().getUser());
                ((QBCallChatHelper) getHelper(CALL_CHAT_HELPER)).init(QBChatService.getInstance());
            }
        }
    }
}