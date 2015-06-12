package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate_core.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.PrefsHelper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

public class QBLogoutAndDestroyChatCommand extends ServiceCommand {

    private QBChatRestHelper chatRestHelper;
    private QBMultiChatHelper multiChatHelper;

    public QBLogoutAndDestroyChatCommand(Context context, QBChatRestHelper chatRestHelper, QBMultiChatHelper multiChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatRestHelper = chatRestHelper;
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context, boolean destroyChat) {
        Intent intent = new Intent(QBServiceConsts.LOGOUT_AND_DESTROY_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.DESTROY_CHAT, destroyChat);
        context.startService(intent);
    }

    public static void start(Context context) {
       start(context, false);
    }

    private void clearDialogsDataFromPreferences() {
        PrefsHelper.getPrefsHelper().delete(PrefsHelper.PREF_JOINED_TO_ALL_DIALOGS);
    }

    @Override
    public Bundle perform(Bundle extras) throws QBResponseException {
        boolean destroy = true;
        if (extras != null) {
            destroy = extras.getBoolean(QBServiceConsts.DESTROY_CHAT, true);
        }
        try {
            if (chatRestHelper != null && chatRestHelper.isLoggedIn()) {
                multiChatHelper.leaveDialogs();
                chatRestHelper.logout();
                if (destroy) {
                    chatRestHelper.destroy();
                }
                clearDialogsDataFromPreferences();
            }
        } catch (XMPPException | SmackException e){
            throw new QBResponseException(e.getLocalizedMessage());
        }
        return extras;
    }
}