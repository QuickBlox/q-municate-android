package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.ArrayList;
import java.util.List;

public class QBLoadDialogsCommand extends ServiceCommand {

    private QBPrivateChatHelper privateChatHelper;
    private QBGroupChatHelper multiChatHelper;

    // TODO: HACK!
    // This is temporary value,
    // by default MAX count of Dialogs should be !> (DIALOGS_PARTS * ConstsCore.CHATS_DIALOGS_PER_PAGE)
    // it is 200 Dialogs
    private final static int DIALOGS_PARTS = 10; // TODO: need to fix in the second release.

    public QBLoadDialogsCommand(Context context, QBPrivateChatHelper privateChatHelper, QBGroupChatHelper multiChatHelper, String successAction,
                                String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
        this.privateChatHelper = privateChatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        ArrayList<ParcelableQBDialog> parcelableQBDialog = new ArrayList<>();
        List<QBDialog> groupDialogsList;
        List<QBDialog> privateDialogsList;
        List<QBDialog> dialogsList = new ArrayList<>();



        Bundle returnedBundle = new Bundle();
        QBRequestGetBuilder qbRequestGetBuilder = new QBRequestGetBuilder();

        qbRequestGetBuilder.setPagesLimit(ConstsCore.CHATS_DIALOGS_PER_PAGE);
        qbRequestGetBuilder.sortDesc(QBServiceConsts.EXTRA_LAST_MESSAGE_DATE_SENT);

        qbRequestGetBuilder.addRule("type", "eq", 3);
        privateDialogsList = privateChatHelper.getDialogs(qbRequestGetBuilder, returnedBundle);
        parseLoadedDialogsAndJoin(privateDialogsList, parcelableQBDialog, false);

        qbRequestGetBuilder.addRule("type", "eq", 2);
        groupDialogsList = multiChatHelper.getDialogs(qbRequestGetBuilder, returnedBundle);
        parseLoadedDialogsAndJoin(groupDialogsList, parcelableQBDialog, true);

//        dialogsList.addAll(privateDialogsList);
//        dialogsList.addAll(groupDialogsList);

//        parseLoadedDialogsAndJoin(dialogsList, parcelableQBDialog);

//        boolean needToLoadByPart = dialogsList != null && !dialogsList.isEmpty();
//        if (needToLoadByPart) {
//            loadByParts(parcelableQBDialog, dialogsList, returnedBundle, qbRequestGetBuilder);
//        }



        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS, parcelableQBDialog);

        return bundle;
    }

//    private void loadByParts(ArrayList<ParcelableQBDialog> parcelableQBDialog, List<QBDialog> dialogsList,
//            Bundle returnedBundle, QBRequestGetBuilder qbRequestGetBuilder) throws QBResponseException {
//        boolean needToLoadMore = true;
//        for (int i = 0; i < DIALOGS_PARTS && needToLoadMore; i++) {
//            qbRequestGetBuilder.setPagesSkip(dialogsList.size());
//            List<QBDialog> newDialogsList = multiChatHelper.getDialogs(qbRequestGetBuilder, returnedBundle);
//            dialogsList.addAll(newDialogsList);
//            parseLoadedDialogsAndJoin(dialogsList, parcelableQBDialog);
//            needToLoadMore = !newDialogsList.isEmpty();
//        }
//    }

    private void parseLoadedDialogsAndJoin(List<QBDialog> dialogsList, ArrayList<ParcelableQBDialog> parcelableQBDialog, boolean isGroupDialogs) {
        if (dialogsList != null && !dialogsList.isEmpty()) {
            parcelableQBDialog.addAll(ChatUtils.qbDialogsToParcelableQBDialogs(dialogsList));
            if (isGroupDialogs) {
                multiChatHelper.tryJoinRoomChats(dialogsList);
            }
        }
    }
}