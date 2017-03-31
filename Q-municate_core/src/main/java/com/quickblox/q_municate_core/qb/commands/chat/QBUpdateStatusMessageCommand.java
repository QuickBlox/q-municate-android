package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.helpers.QBChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QBUpdateStatusMessageCommand extends ServiceCommand {

    private static String TAG = QBUpdateStatusMessageCommand.class.getName();

    private QBChatHelper chatHelper;

    public QBUpdateStatusMessageCommand(Context context, QBChatHelper chatHelper, String successAction,
                                        String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, QBChatDialog dialog, CombinationMessage combinationMessage, boolean forPrivate) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_STATUS_MESSAGE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_MESSAGE, combinationMessage);
        intent.putExtra(QBServiceConsts.EXTRA_IS_FOR_PRIVATE, forPrivate);
        context.startService(intent);
    }

    public static void start(Context context, QBChatDialog dialog, List<CombinationMessage> combinationMessagesList) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_STATUS_MESSAGE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_MESSAGES, (Serializable) combinationMessagesList);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBChatDialog chatDialog = (QBChatDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        CombinationMessage combinationMessage = (CombinationMessage) extras.getSerializable(QBServiceConsts.EXTRA_MESSAGE);
        boolean forPrivate = extras.getBoolean(QBServiceConsts.EXTRA_IS_FOR_PRIVATE);

        List<CombinationMessage> combinationMessagesList = (List<CombinationMessage>) extras.getSerializable(QBServiceConsts.EXTRA_MESSAGES);

        if (combinationMessagesList != null && !combinationMessagesList.isEmpty()) {
            List<CombinationMessage> forLocalReadList = new ArrayList<>();
            List<CombinationMessage> forServerReadList = new ArrayList<>();

            QBUser currentUser = AppSession.getSession().getUser();

            for (CombinationMessage cm : combinationMessagesList) {
                boolean ownMessage = !cm.isIncoming(currentUser.getId());
                if (!State.READ.equals(cm.getState()) && !ownMessage) {
                    forServerReadList.add(cm);
                } else if (!State.READ.equals(cm.getState()) && ownMessage) {
                    cm.setState(State.READ);
                    forLocalReadList.add(cm);
                }
            }

            chatHelper.updateStatusMessagesReadServer(chatDialog, forServerReadList);
            chatHelper.updateStatusMessagesReadLocal(forLocalReadList);
        } else if (combinationMessage != null && chatDialog != null) {
            try {
                if (combinationMessage.getNotificationType() != null) {
                    chatHelper.updateStatusNotificationMessageRead(chatDialog, combinationMessage);
                } else {
                    chatHelper.updateStatusMessageRead(chatDialog, combinationMessage, forPrivate);
                }
            } catch (Exception e) {
                String errorText = " --- dialogId = " + chatDialog == null
                        ? "null"
                        : chatDialog.getDialogId()
                        + ", messageId = "
                        + combinationMessage == null
                        ? "null"
                        : combinationMessage.getMessageId();
                ErrorUtils.logError(TAG, e + errorText);
            }
        }

        return null;
    }
}