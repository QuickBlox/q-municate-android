package com.quickblox.q_municate.utils.helpers.notification;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.service.CallService;
import com.quickblox.q_municate.utils.SystemUtils;
import com.quickblox.q_municate.utils.helpers.SharedHelper;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.NotificationEvent;

import static com.quickblox.q_municate_core.utils.ConstsCore.*;

public class ChatNotificationHelper {

    private Context context;
    private SharedHelper appSharedHelper;
    private String dialogId;
    private int userId;

    private String message;
    private String messageType;
    private String messageTypeVOIP;

    public ChatNotificationHelper(Context context) {
        this.context = context;
        appSharedHelper = App.getInstance().getAppSharedHelper();
    }

    public void parseChatMessage(Bundle extras) {
        message = extras.getString(MESSAGE);
        userId = extras.getString(MESSAGE_USER_ID) == null ? 0 : Integer.parseInt(extras.getString(MESSAGE_USER_ID));
        dialogId = extras.getString(MESSAGE_DIALOG_ID);
        messageType = extras.getString(MESSAGE_TYPE);
        messageTypeVOIP = extras.getString(MESSAGE_VOIP_TYPE);

        boolean callPush = TextUtils.equals(messageType, PUSH_MESSAGE_TYPE_CALL) || TextUtils.equals(messageTypeVOIP, PUSH_MESSAGE_TYPE_VOIP);

        if (callPush && shouldProceedCall()) {
            CallService.start(context);
            return;
        }

        if (SystemUtils.isAppRunningNow()) {
            return;
        }

        if (isOwnMessage(userId)) {
            return;
        }

        boolean chatPush = userId != 0 && !TextUtils.isEmpty(dialogId);

        if (chatPush) {
            saveOpeningDialogData(userId, dialogId);
            saveOpeningDialog(true);
            sendChatNotification(message, userId, dialogId);
        } else {
            sendCommonNotification(message);
        }

    }

    private boolean shouldProceedCall() {
        return !SystemUtils.isAppRunningNow() || AppSession.ChatState.BACKGROUND == AppSession.getSession().getChatState();
    }

    public void sendChatNotification(String message, int userId, String dialogId) {
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setTitle(context.getString(R.string.app_name));
        notificationEvent.setSubject(message);
        notificationEvent.setBody(message);

        NotificationManagerHelper.sendChatNotificationEvent(context, userId, dialogId, notificationEvent);
    }

    private void sendCommonNotification(String message) {
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setTitle(context.getString(R.string.app_name));
        notificationEvent.setSubject(message);
        notificationEvent.setBody(message);

        NotificationManagerHelper.sendCommonNotificationEvent(context, notificationEvent);
    }

    public void saveOpeningDialogData(int userId, String dialogId) {
        appSharedHelper.savePushUserId(userId);
        appSharedHelper.savePushDialogId(dialogId);
    }

    public void saveOpeningDialog(boolean open) {
        appSharedHelper.saveNeedToOpenDialog(open);
    }

    private boolean isOwnMessage(int senderUserId) {
        return appSharedHelper.getUserId() == senderUserId;
    }
}