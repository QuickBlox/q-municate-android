package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.helpers.QBChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 5/31/17.
 */

public class QBLoadDialogByIdsCommand extends ServiceCommand {
    private static String TAG = QBLoadDialogByIdsCommand.class.getSimpleName();

    private final String OPERATOR_ID = "_id";

    private QBChatHelper chatHelper;

    public QBLoadDialogByIdsCommand(Context context, QBChatHelper chatHelper, String successAction,
                                    String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, List<String> dialogsIds) {
        Intent intent = new Intent(QBServiceConsts.LOAD_CHATS_DIALOGS_BY_IDS_ACTION, null, context, QBService.class);
        Bundle result = new Bundle();

        result.putStringArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS_IDS, (ArrayList<String>) dialogsIds);
        intent.putExtras(result);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        List<String> dialogsIds = extras.getStringArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS_IDS);

        Log.d(TAG, "perform dialogsIds= " + dialogsIds);

        final Bundle returnedBundle = new Bundle();
        final ArrayList<ParcelableQBDialog> parcelableQBDialog = new ArrayList<>();

        parcelableQBDialog.addAll(ChatUtils.qBDialogsToParcelableQBDialogs(loadAllDialogsByIds(dialogsIds, returnedBundle)));

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS, parcelableQBDialog);

        return bundle;
    }

    private List<QBChatDialog> loadAllDialogsByIds(List<String> dialogsIds, Bundle returnedBundle) throws QBResponseException {
        Object[] arrDialogsIds = dialogsIds.toArray(new String[dialogsIds.size()]);

        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.sortAsc(QBServiceConsts.EXTRA_LAST_MESSAGE_DATE_SENT);
        requestBuilder.in(OPERATOR_ID, arrDialogsIds);
        List<QBChatDialog> chatDialogs = getDialogs(requestBuilder, returnedBundle);

        chatHelper.saveDialogsToCache(chatDialogs, false);
        return chatDialogs;
    }

    private List<QBChatDialog> getDialogs(QBRequestGetBuilder qbRequestGetBuilder, Bundle returnedBundle) throws QBResponseException {
        return chatHelper.getDialogs(qbRequestGetBuilder, returnedBundle);
    }
}
