package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class QBLoginChatCommand extends ServiceCommand {

    public static final String SHOULD_START_MULTICHATS = "start_multichats";

    private QBAuthHelper authHelper;
    private QBChatHelper chatHelper;
    private boolean shouldStartMultiChat;

    public QBLoginChatCommand(Context context, QBAuthHelper authHelper, QBChatHelper chatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
        this.chatHelper = chatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_CHAT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    public static void start(Context context, boolean shouldStartMultiChat) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SHOULD_START_MULTICHATS, shouldStartMultiChat);
        Intent intent = new Intent(QBServiceConsts.LOGIN_CHAT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        if (extras != null) {
            shouldStartMultiChat = extras.getBoolean(SHOULD_START_MULTICHATS, false);
        }
        // TODO IS remove when fix ResourceBindingNotOfferedException occurrence
        tryLogin();
        if (!chatHelper.isLoggedIn()) {
            throw new Exception();
        }
        if (shouldStartMultiChat) {
            execJoin(extras);
        }
        return extras;
    }

    private void tryLogin() throws XMPPException, IOException, SmackException {
        long startTime = new Date().getTime();
        long currentTime = startTime;
        while (!chatHelper.isLoggedIn() && (currentTime - startTime) < Consts.LOGIN_TIMEOUT) {
            currentTime = new Date().getTime();
            try {
                chatHelper.login(authHelper.getUser());
            } catch (SmackException.ResourceBindingNotOfferedException ignore) { /* NOP */ }
        }
    }


    private Bundle execJoin(Bundle extras) throws Exception {
        List<QBDialog> dialogs = DatabaseManager.getDialogs(context);
        List<String> roomJidListFromDialogs = ChatUtils.getRoomJidListFromDialogs(dialogs);
        for (String roomJid : roomJidListFromDialogs) {
            chatHelper.joinRoomChat(roomJid);
        }
        return extras;
    }
}