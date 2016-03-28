package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
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

    private final String FIELD_DIALOG_TYPE = "type";
    private final String OPERATOR_EQ = "eq";

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

        Bundle returnedBundle = new Bundle();
        QBRequestGetBuilder qbRequestGetBuilder = new QBRequestGetBuilder();

        qbRequestGetBuilder.setPagesLimit(ConstsCore.CHATS_DIALOGS_PER_PAGE);
        qbRequestGetBuilder.sortDesc(QBServiceConsts.EXTRA_LAST_MESSAGE_DATE_SENT);

        parcelableQBDialog.addAll(ChatUtils.qbDialogsToParcelableQBDialogs(
                loadAllDialogsByType(QBDialogType.PRIVATE, returnedBundle, qbRequestGetBuilder)));

        parcelableQBDialog.addAll(ChatUtils.qbDialogsToParcelableQBDialogs(
                loadAllDialogsByType(QBDialogType.GROUP, returnedBundle, qbRequestGetBuilder)));

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS, parcelableQBDialog);

        return bundle;
    }

    private List<QBDialog> loadAllDialogsByType(QBDialogType dialogsType,
                             Bundle returnedBundle, QBRequestGetBuilder qbRequestGetBuilder) throws QBResponseException {
        List<QBDialog> allDialogsList = new ArrayList<>();
        boolean needToLoadMore;

        do {
            qbRequestGetBuilder.setPagesSkip(allDialogsList.size());
            qbRequestGetBuilder.addRule(FIELD_DIALOG_TYPE, OPERATOR_EQ, dialogsType.getCode());
            List<QBDialog> newDialogsList = dialogsType == QBDialogType.PRIVATE
                    ? getPrivateDialogs(qbRequestGetBuilder, returnedBundle)
                    : getGroupDialogs(qbRequestGetBuilder, returnedBundle);
            allDialogsList.addAll(newDialogsList);
            needToLoadMore = newDialogsList.size() == ConstsCore.CHATS_DIALOGS_PER_PAGE;
            Log.d("QBLoadDialogsCommand", "needToLoadMore = " + needToLoadMore  + "newDialogsList.size() = " + newDialogsList.size());
        } while (needToLoadMore);

        if (dialogsType == QBDialogType.GROUP) {
            tryJoinRoomChats(allDialogsList);
        }

        return allDialogsList;
    }

    private List<QBDialog> getPrivateDialogs(QBRequestGetBuilder qbRequestGetBuilder, Bundle returnedBundle) throws QBResponseException {
        return privateChatHelper.getDialogs(qbRequestGetBuilder, returnedBundle);
    }

    private List<QBDialog> getGroupDialogs(QBRequestGetBuilder qbRequestGetBuilder, Bundle returnedBundle) throws QBResponseException {
        return multiChatHelper.getDialogs(qbRequestGetBuilder, returnedBundle);
    }

    private void tryJoinRoomChats(List<QBDialog> allDialogsList){
        multiChatHelper.tryJoinRoomChats(allDialogsList);
    }
}