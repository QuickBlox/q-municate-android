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
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.ArrayList;
import java.util.List;

public class QBLoadDialogsCommand extends ServiceCommand {

    private QBGroupChatHelper multiChatHelper;

    public QBLoadDialogsCommand(Context context, QBGroupChatHelper multiChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        ArrayList<ParcelableQBDialog> parcelableQBDialog = new ArrayList<>();
        List<QBDialog> dialogsList;

        Bundle returnedBundle = new Bundle();
        QBRequestGetBuilder qbRequestGetBuilder = new QBRequestGetBuilder();

        qbRequestGetBuilder.setPagesLimit(ConstsCore.CHATS_DIALOGS_PER_PAGE);
        qbRequestGetBuilder.sortDesc(QBServiceConsts.EXTRA_LAST_MESSAGE_DATE_SENT);

        dialogsList = multiChatHelper.getDialogs(qbRequestGetBuilder, returnedBundle);
        parseLoadedDialogsAndJoin(dialogsList, parcelableQBDialog);

        int totalEntries = returnedBundle.getInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES);
        boolean needToLoadByPart = dialogsList != null && !dialogsList.isEmpty()
                && totalEntries > ConstsCore.CHATS_DIALOGS_PER_PAGE;
        if (needToLoadByPart) {
            loadByParts(parcelableQBDialog, dialogsList, returnedBundle, qbRequestGetBuilder, totalEntries);
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS, parcelableQBDialog);

        return bundle;
    }

    private void loadByParts(ArrayList<ParcelableQBDialog> parcelableQBDialog, List<QBDialog> dialogsList,
            Bundle returnedBundle, QBRequestGetBuilder qbRequestGetBuilder, float totalEntries) throws QBResponseException {
        int partsCount = (int) Math.ceil((totalEntries / ConstsCore.CHATS_DIALOGS_PER_PAGE));
        for (int i = 0; i < partsCount - 1; i++) {
            qbRequestGetBuilder.setPagesSkip(dialogsList.size());
            dialogsList.addAll(multiChatHelper.getDialogs(qbRequestGetBuilder, returnedBundle));
            parseLoadedDialogsAndJoin(dialogsList, parcelableQBDialog);
        }
    }

    private void parseLoadedDialogsAndJoin(List<QBDialog> dialogsList, ArrayList<ParcelableQBDialog> parcelableQBDialog) {
        if (dialogsList != null && !dialogsList.isEmpty()) {
            parcelableQBDialog.addAll(ChatUtils.qbDialogsToParcelableQBDialogs(dialogsList));
            multiChatHelper.tryJoinRoomChats(dialogsList);
        }
    }
}