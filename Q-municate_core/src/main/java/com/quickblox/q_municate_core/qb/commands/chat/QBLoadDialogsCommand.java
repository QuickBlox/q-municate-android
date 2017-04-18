package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.helpers.QBChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DbUtils;
import com.quickblox.q_municate_core.utils.FinderUnknownUsers;
import com.quickblox.q_municate_db.managers.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QBLoadDialogsCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    private final String FIELD_DIALOG_TYPE = "type";
    private final String OPERATOR_EQ = "eq";

    private final int FIRST_PAGE_NUMBER = 1;

    // TODO: HACK!
    // This is temporary value,
    // by default MAX count of Dialogs should be !> (DIALOGS_PARTS * ConstsCore.CHATS_DIALOGS_PER_PAGE)
    // it is 200 Dialogs
    private final static int DIALOGS_PARTS = 10; // TODO: need to fix in the second release.

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private BlockingQueue<Runnable> threadQueue;
    private ThreadPoolExecutor threadPool;

    private List<QBChatDialog> dialogsListPrivate;
    private List<QBChatDialog> dialogsListGroup;

    public QBLoadDialogsCommand(Context context, QBChatHelper chatHelper, String successAction,
                                String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
        initThreads();
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        final ArrayList<ParcelableQBDialog> parcelableQBDialog = new ArrayList<>();

        final Bundle returnedBundle = new Bundle();
        final QBRequestGetBuilder qbRequestGetBuilder = new QBRequestGetBuilder();

        qbRequestGetBuilder.setLimit(ConstsCore.CHATS_DIALOGS_PER_PAGE);
        qbRequestGetBuilder.sortDesc(QBServiceConsts.EXTRA_LAST_MESSAGE_DATE_SENT);

        parcelableQBDialog.addAll(ChatUtils.qBDialogsToParcelableQBDialogs(
                loadAllDialogs(returnedBundle, qbRequestGetBuilder)));

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS, parcelableQBDialog);

        return bundle;
    }

    private void initThreads() {
        threadQueue = new LinkedBlockingQueue<>();
        threadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, threadQueue);
        threadPool.allowCoreThreadTimeOut(true);
    }

    private int loadAllDialogsByType(QBDialogType dialogsType,  Bundle returnedBundle, QBRequestGetBuilder qbRequestGetBuilder, List<QBChatDialog> allDialogsList, int pageNumber) throws QBResponseException {
        boolean needToLoadMore = false;

        qbRequestGetBuilder.setSkip(allDialogsList.size());
        qbRequestGetBuilder.eq(FIELD_DIALOG_TYPE, dialogsType.getCode());
        List<QBChatDialog> newDialogsList = getDialogs(qbRequestGetBuilder, returnedBundle);
        if (dialogsType == QBDialogType.GROUP) {
            dialogsListGroup = newDialogsList;
        } else{
            dialogsListPrivate = newDialogsList;
        }
        allDialogsList.addAll(newDialogsList);
        Log.d("QBLoadDialogsCommand", "needToLoadMore = " + needToLoadMore + "newDialogsList.size() = " + newDialogsList.size());

        if (dialogsType == QBDialogType.GROUP) {
            boolean needClean = (pageNumber == 0);
            tryJoinRoomChatsPage(newDialogsList, needClean);
        }

        return newDialogsList.size();
    }

    private List<QBChatDialog> loadAllDialogs(Bundle returnedBundle, QBRequestGetBuilder qbRequestGetBuilder) throws QBResponseException {
        List<QBChatDialog> allDialogsList = null;
        List<QBChatDialog> allDialogsListPrivate = new ArrayList<>();
        List<QBChatDialog> allDialogsListGroup = new ArrayList<>();
        boolean needToLoadMorePrivate = true;
        boolean needToLoadMoreGroup = true;
        int pageNumber = 0;

        final QBRequestGetBuilder qbRequestGetBuilderPrivate = new QBRequestGetBuilder();

        qbRequestGetBuilderPrivate.setLimit(ConstsCore.CHATS_DIALOGS_PER_PAGE);
        qbRequestGetBuilderPrivate.sortDesc(QBServiceConsts.EXTRA_LAST_MESSAGE_DATE_SENT);
        qbRequestGetBuilderPrivate.addRule(FIELD_DIALOG_TYPE, OPERATOR_EQ, QBDialogType.PRIVATE.getCode());

        final QBRequestGetBuilder qbRequestGetBuilderGroup = new QBRequestGetBuilder();

        qbRequestGetBuilderGroup.setLimit(ConstsCore.CHATS_DIALOGS_PER_PAGE);
        qbRequestGetBuilderGroup.sortDesc(QBServiceConsts.EXTRA_LAST_MESSAGE_DATE_SENT);
        qbRequestGetBuilderGroup.addRule(FIELD_DIALOG_TYPE, OPERATOR_EQ, QBDialogType.GROUP.getCode());

        do {
            int privateDialogsSize = 0;
            int groupDialogsSize = 0;
            if(needToLoadMorePrivate) {
                privateDialogsSize = loadAllDialogsByType(QBDialogType.PRIVATE, returnedBundle, qbRequestGetBuilderPrivate, allDialogsListPrivate, pageNumber);
                needToLoadMorePrivate = privateDialogsSize == ConstsCore.CHATS_DIALOGS_PER_PAGE;
            }

            if(needToLoadMoreGroup) {
                groupDialogsSize = loadAllDialogsByType(QBDialogType.GROUP, returnedBundle, qbRequestGetBuilderGroup, allDialogsListGroup, pageNumber);
                needToLoadMoreGroup = groupDialogsSize == ConstsCore.CHATS_DIALOGS_PER_PAGE;
            }

            chatHelper.saveDialogsToCache(dialogsListPrivate);
            chatHelper.saveDialogsToCache(dialogsListGroup);
            dialogsListPrivate = null;
            dialogsListGroup = null;

            pageNumber++;

            int perPage = privateDialogsSize + groupDialogsSize;
            Log.d("QBLoadDialogsCommand", "sendLoadPageSuccess perPage= " + perPage);
            Bundle bundle = new Bundle();
            bundle.putInt(ConstsCore.PAGE_NUMBER, pageNumber);
            bundle.putInt(ConstsCore.DIALOGS_PER_PAGE, perPage);
            sendLoadPageSuccess(bundle);

        } while (needToLoadMorePrivate || needToLoadMoreGroup);

        allDialogsList = new ArrayList<>(allDialogsListPrivate.size() + allDialogsListGroup.size());
        allDialogsList.addAll(allDialogsListPrivate);
        allDialogsList.addAll(allDialogsListGroup);

        return allDialogsList;
    }

    private List<QBChatDialog> getDialogs(QBRequestGetBuilder qbRequestGetBuilder, Bundle returnedBundle) throws QBResponseException {
        return chatHelper.getDialogs(qbRequestGetBuilder, returnedBundle);
    }

    private void tryJoinRoomChatsPage(final List<QBChatDialog> dialogsList, final boolean needClean) {
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                chatHelper.tryJoinRoomChatsPage(dialogsList, needClean);
            }
        });
    }

    private void sendLoadPageSuccess(Bundle result){
        sendResult(result, successAction);
    }

    private void sendLoadPageFail(Bundle result){
        sendResult(result, failAction);
    }
}