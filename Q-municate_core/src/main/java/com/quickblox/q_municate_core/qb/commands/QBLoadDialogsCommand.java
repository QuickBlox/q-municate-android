package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatDialogUtils;
import com.quickblox.q_municate_core.utils.FinderUnknownFriends;
import com.quickblox.q_municate_core.utils.PrefsHelper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;

public class QBLoadDialogsCommand extends ServiceCommand {

    private QBMultiChatHelper multiChatHelper;

    public QBLoadDialogsCommand(Context context, QBMultiChatHelper multiChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws QBResponseException {
        List<QBDialog> dialogsList;
        ArrayList<ParcelableQBDialog> parcelableQBDialog = null;
        try {
            dialogsList = multiChatHelper.getDialogs();
            if (dialogsList != null && !dialogsList.isEmpty()) {
                new FindUnknownFriendsTask().execute(dialogsList);
                parcelableQBDialog = ChatDialogUtils.dialogsToParcelableDialogs(dialogsList);
                multiChatHelper.tryJoinRoomChats(dialogsList);
                // save flag for join to dialogs
                PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_JOINED_TO_ALL_DIALOGS, true);
            }
        } catch (XMPPException | SmackException e) {
            throw new QBResponseException(e.getLocalizedMessage());
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS, parcelableQBDialog);

        return bundle;
    }

    private class FindUnknownFriendsTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            List<QBDialog> dialogsList = (List<QBDialog>) params[0];
            new FinderUnknownFriends(context, AppSession.getSession().getUser(), dialogsList).find();
            return null;
        }
    }
}