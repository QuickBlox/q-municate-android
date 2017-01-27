package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBParticipantListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.models.NotificationType;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.DbUtils;
import com.quickblox.q_municate_core.utils.FinderUnknownUsers;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QBGroupChatHelper extends QBBaseChatHelper {

    private static final String TAG = QBGroupChatHelper.class.getSimpleName();

    private QBParticipantListener participantListener;
    private List<QBChatDialog> groupDialogsList;

    public QBGroupChatHelper(Context context) {
        super(context);
        participantListener = new ParticipantListener();
    }

    public void init(QBUser user) {
        super.init(user);
        addSystemMessageListener(new SystemMessageListener());
    }

    @Override
    public synchronized QBChat createChatLocally(QBChatDialog dialog, Bundle additional) throws QBResponseException {
        Log.d("Fix double message", "createChatLocally from " + QBGroupChatHelper.class.getSimpleName());
        Log.d("Fix double message", "dialog = " + dialog);
        currentDialog = dialog;
        QBGroupChat roomChat = createGroupChatIfNotExist(dialog);
        roomChat.addParticipantListener(participantListener);
        return roomChat;
    }

    @Override
    public synchronized void closeChat(QBChatDialog qbDialog, Bundle additional) {
        Log.d("Fix double message", "closeChat from " + QBGroupChatHelper.class.getSimpleName());
        if (currentDialog != null && currentDialog.getDialogId().equals(qbDialog.getDialogId())) {
            currentDialog = null;
        }
    }

    public void onGroupMessageReceived(QBChat chat, QBChatMessage qbChatMessage) {
        String dialogId = (String) qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_DIALOG_ID);
        User user = DataManager.getInstance().getUserDataManager().get(qbChatMessage.getSenderId());
        Message message = parseReceivedMessage(qbChatMessage);

        boolean ownMessage = !message.isIncoming(chatCreator.getId());

        if (ChatNotificationUtils.isNotificationMessage(qbChatMessage)) {
            DialogNotification dialogNotification = ChatUtils.convertMessageToDialogNotification(message);
            dialogNotification.setType(ChatNotificationUtils.getUpdateChatLocalNotificationType(qbChatMessage));
            dialogNotification.setBody(ChatNotificationUtils.getBodyForUpdateChatNotificationMessage(context, dataManager, qbChatMessage));

            if (!ownMessage) {
                updateDialogByNotification(qbChatMessage);
            }
        }

        DbUtils.saveMessageOrNotificationToCache(context, dataManager, dialogId, qbChatMessage, State.DELIVERED, true);
        DbUtils.updateDialogModifiedDate(dataManager, dialogId, ChatUtils.getMessageDateSent(qbChatMessage), true);

        checkForSendingNotification(ownMessage, qbChatMessage, user, false);
    }

    public void sendGroupMessage(String roomJidId, String message) throws Exception {
        QBChatMessage chatMessage = getQBChatMessage(message, null, null);
        sendGroupMessage(chatMessage, roomJidId, currentDialog.getDialogId());
    }

    private void sendGroupMessage(QBChatMessage chatMessage, String roomJId, String dialogId) throws QBResponseException {
        QBGroupChat groupChat = groupChatManager.getGroupChat(roomJId);
        QBChatDialog existingDialog = null;
        if (groupChat == null) {
            existingDialog = ChatUtils.createQBDialogFromLocalDialog(dataManager, dataManager.getDialogDataManager().getByDialogId(dialogId));
            groupChat = (QBGroupChat) createChatLocally(existingDialog, null);
        }
        String error = null;

        addNecessaryPropertyForQBChatMessage(chatMessage, dialogId);

        try {
            groupChat.sendMessage(chatMessage);
        } catch (SmackException.NotConnectedException e) {
            error = context.getString(R.string.dlg_fail_connection);
        } catch (IllegalStateException e) {
            tryJoinRoomChat(existingDialog);
            throw new IllegalStateException(e);
        }
        if (error != null) {
            throw new QBResponseException(error);
        }
    }

    public void sendGroupMessageWithAttachImage(String roomJidId, QBFile file) throws QBResponseException {
        QBChatMessage chatMessage = getQBChatMessage(context.getString(R.string.dlg_attached_last_message),
                file, null);
        sendGroupMessage(chatMessage, roomJidId, currentDialog.getDialogId());
    }

    public void sendGroupMessageWithAttachLocation(String roomJidId, String location) throws QBResponseException {
        QBChatMessage chatMessage = getQBChatMessage(context.getString(R.string.dlg_attached_last_message),
                null, location);
        sendGroupMessage(chatMessage, roomJidId, currentDialog.getDialogId());
    }

    public void tryJoinRoomChats(List<QBChatDialog> qbDialogsList) {
        tryJoinRoomChatsPage(qbDialogsList, true);
    }

    public synchronized void tryJoinRoomChatsPage(List<QBChatDialog> qbDialogsList, boolean needClean) {
        if (!qbDialogsList.isEmpty()) {
            initGroupDialogsList(needClean);
            for (QBChatDialog dialog : qbDialogsList) {
                if (!QBDialogType.PRIVATE.equals(dialog.getType())) {
                    groupDialogsList.add(dialog);
                    tryJoinRoomChat(dialog);
                }
            }
        }
    }

    public void tryJoinRoomChats() {
        List<Dialog> dialogsList = dataManager.getDialogDataManager().getAll();

        if (dialogsList != null) {
            List<QBChatDialog> qbDialogsList = ChatUtils.createQBDialogsListFromDialogsList(dataManager, dialogsList);
            tryJoinRoomChats(qbDialogsList);
        }
    }

    private void initGroupDialogsList(boolean needClean) {
        if (groupDialogsList == null) {
            groupDialogsList = new ArrayList<QBChatDialog>();
        } else {
            if (needClean) {
                groupDialogsList.clear();
            }
        }
    }

    public QBChatDialog createGroupChat(String name, List<Integer> friendIdsList, String photoUrl) throws Exception {
        ArrayList<Integer> occupantIdsList = (ArrayList<Integer>) ChatUtils.getOccupantIdsWithUser(friendIdsList);

        QBChatDialog dialogToCreate = new QBChatDialog();
        dialogToCreate.setName(name);
        dialogToCreate.setType(QBDialogType.GROUP);
        dialogToCreate.setOccupantsIds(occupantIdsList);
        dialogToCreate.setPhoto(photoUrl);

        QBChatDialog qbDialog = QBRestChatService.createChatDialog(dialogToCreate).perform();
        DbUtils.saveDialogToCache(dataManager, qbDialog);

        joinRoomChat(qbDialog);

        sendSystemMessageAboutCreatingGroupChat(qbDialog, friendIdsList);

        QBChatMessage chatMessage = ChatNotificationUtils.createGroupMessageAboutCreateGroupChat(context, qbDialog, photoUrl);
        sendGroupMessage(chatMessage, qbDialog.getRoomJid(), qbDialog.getDialogId());

        return qbDialog;
    }

    public void sendSystemMessageAboutCreatingGroupChat(QBChatDialog dialog, List<Integer> friendIdsList) throws Exception {
        for (Integer friendId : friendIdsList) {
            try {
                sendSystemMessageAboutCreatingGroupChat(dialog, friendId);
            } catch (QBResponseException e) {
                ErrorUtils.logError(e);
            }
        }
    }

    private void sendSystemMessageAboutCreatingGroupChat(QBChatDialog dialog, Integer friendId) throws Exception {
        QBChatMessage chatMessageForSending = ChatNotificationUtils
                .createSystemMessageAboutCreatingGroupChat(context, dialog);

        addNecessaryPropertyForQBChatMessage(chatMessageForSending, dialog.getDialogId());
        sendSystemMessage(chatMessageForSending, friendId, dialog.getDialogId());
    }

    public void leaveDialogs() throws XMPPException, SmackException.NotConnectedException {
        if (groupDialogsList != null) {
            for (QBChatDialog dialog : groupDialogsList) {
                QBGroupChat roomChat = groupChatManager.getGroupChat(dialog.getRoomJid());
                if (roomChat != null && roomChat.isJoined()) {
                    roomChat.leave();
                }
            }
        }
    }

    public void leaveRoomChat(Dialog dialog) throws Exception {
        groupChatManager.getGroupChat(dialog.getRoomJid()).leave();
        List<Integer> userIdsList = new ArrayList<Integer>();
        userIdsList.add(chatCreator.getId());
        removeUsersFromDialog(dialog, userIdsList);
    }

    protected QBGroupChat createGroupChatIfNotExist(QBChatDialog dialog) throws QBResponseException {
        boolean notNull = Utils.validateNotNull(groupChatManager);
        if (!notNull) {
            ErrorUtils.logError(TAG, " groupChatManager is NULL");
            throw new QBResponseException(context.getString(R.string.dlg_fail_create_chat));
        }
        QBGroupChat groupChat = groupChatManager.getGroupChat(dialog.getRoomJid());
        if (groupChat == null && dialog.getRoomJid() != null) {
            groupChat = groupChatManager.createGroupChat(dialog.getRoomJid());
        }

        if (groupChat != null) {
            groupChat.addMessageListener(groupChatMessageListener);
        }

        return groupChat;
    }

    public boolean isDialogJoined(QBChatDialog dialog) {
        QBGroupChat roomChat;
        boolean joined = false;
        try {
            roomChat = createGroupChatIfNotExist(dialog);
            joined = roomChat.isJoined();
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
        return joined;
    }

    public void joinRoomChat(QBChatDialog dialog) throws Exception {
        QBGroupChat roomChat = createGroupChatIfNotExist(dialog);
        if (roomChat != null && !roomChat.isJoined()) {
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0); // without getting messages
            roomChat.join(history);
        }
    }

    public void tryJoinRoomChat(QBChatDialog dialog) {
        try {
            joinRoomChat(dialog);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    public QBChatDialog addUsersToDialog(String dialogId, List<Integer> userIdsList) throws Exception {
        StringifyArrayList<Integer> occupantsIdsList = new StringifyArrayList<>(userIdsList);
        QBChatDialog dialog = ChatUtils.createQBDialogFromLocalDialog(dataManager,
                dataManager.getDialogDataManager().getByDialogId(dialogId));

        QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
        requestBuilder.push(com.quickblox.chat.Consts.DIALOG_OCCUPANTS, occupantsIdsList.getItemsAsString());
        return updateDialog(dialog, requestBuilder);
    }

    public void removeUsersFromDialog(Dialog dialog, List<Integer> userIdsList) throws QBResponseException {
        QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
        requestBuilder.pullAll(com.quickblox.chat.Consts.DIALOG_OCCUPANTS, userIdsList.toArray());
        updateDialog(ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog), requestBuilder);
        DataManager.getInstance().getDialogDataManager().delete(dialog);
    }

    public QBChatDialog updateDialog(QBChatDialog dialog) throws QBResponseException {
        return updateDialog(dialog, (QBRequestUpdateBuilder) null);
    }

    public QBChatDialog updateDialog(QBChatDialog dialog, File inputFile) throws QBResponseException {
        QBFile file = QBContent.uploadFileTask(inputFile, true, null).perform();
        dialog.setPhoto(file.getPublicUrl());
        return updateDialog(dialog, (QBRequestUpdateBuilder) null);
    }

    private QBChatDialog updateDialog(QBChatDialog dialog, QBRequestUpdateBuilder requestBuilder) throws QBResponseException {
        QBChatDialog updatedDialog = QBRestChatService.updateGroupChatDialog(dialog, requestBuilder).perform();
        return updatedDialog;
    }

    public void sendGroupMessageToFriends(QBChatDialog qbDialog, DialogNotification.Type notificationType,
            Collection<Integer> occupantsIdsList, boolean leavedFromDialog) throws QBResponseException {
        QBChatMessage chatMessage = ChatNotificationUtils.createGroupMessageAboutUpdateChat(context, qbDialog,
                notificationType, occupantsIdsList, leavedFromDialog);
        sendGroupMessage(chatMessage, qbDialog.getRoomJid(), qbDialog.getDialogId());
    }

    private void updateDialogByNotification(QBChatMessage qbChatMessage) {
        String dialogId = (String) qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_DIALOG_ID);
        Dialog dialog = dataManager.getDialogDataManager().getByDialogId(dialogId);
        QBChatDialog qbDialog;
        if (dialog == null) {
            qbDialog = ChatNotificationUtils.parseDialogFromQBMessage(context, qbChatMessage, QBDialogType.GROUP);
        } else {
            qbDialog = ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog);
        }

        ChatNotificationUtils.updateDialogFromQBMessage(context, dataManager, qbChatMessage, qbDialog);
        DbUtils.saveDialogToCache(dataManager, qbDialog);

        notifyUpdatingDialog();
    }

    protected void notifyUpdatingDialog() {
        Intent intent = new Intent(QBServiceConsts.UPDATE_DIALOG);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    protected void notifyUpdatingDialogDetails(int userId, boolean online) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_DIALOG_DETAILS);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);
        intent.putExtra(QBServiceConsts.EXTRA_STATUS, online);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void createDialogByNotification(QBChatMessage qbChatMessage, DialogNotification.Type notificationType) {
        qbChatMessage.setBody(context.getString(R.string.cht_notification_message));

        QBChatDialog qbDialog = ChatNotificationUtils.parseDialogFromQBMessage(context, qbChatMessage, qbChatMessage.getBody(), QBDialogType.GROUP);

        qbDialog.getOccupants().add(chatCreator.getId());
        DbUtils.saveDialogToCache(dataManager, qbDialog);

        String roomJidId = qbDialog.getRoomJid();
        if (roomJidId != null) {
            tryJoinRoomChat(qbDialog);
            new FinderUnknownUsers(context, chatCreator, qbDialog).find();
        }

        DialogNotification dialogNotification = ChatUtils.convertMessageToDialogNotification(parseReceivedMessage(qbChatMessage));
        dialogNotification.setType(notificationType);
        Message message = ChatUtils.createTempLocalMessage(dialogNotification);
        DbUtils.saveTempMessage(dataManager, message);

        boolean ownMessage = !message.isIncoming(chatCreator.getId());
        User user = DataManager.getInstance().getUserDataManager().get(qbChatMessage.getSenderId());
        checkForSendingNotification(ownMessage, qbChatMessage, user, false);
    }

    private class SystemMessageListener implements QBSystemMessageListener {

        @Override
        public void processMessage(final QBChatMessage qbChatMessage) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    String notificationTypeString = (String) qbChatMessage
                            .getProperty(ChatNotificationUtils.PROPERTY_NOTIFICATION_TYPE);
                    NotificationType notificationType = NotificationType.parseByValue(
                            Integer.parseInt(notificationTypeString));
                    if (NotificationType.GROUP_CHAT_CREATE.equals(notificationType)) {
                        createDialogByNotification(qbChatMessage, DialogNotification.Type.CREATE_DIALOG);
                    }
                }
            });
        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {
            ErrorUtils.logError(e);
        }
    }

    private class ParticipantListener implements QBParticipantListener {

        @Override
        public void processPresence(QBGroupChat groupChat, QBPresence presence) {
            boolean validData = currentDialog != null && presence.getUserId() != null;
            if (validData && currentDialog.getRoomJid().equals(groupChat.getJid())) {
                notifyUpdatingDialogDetails(presence.getUserId(), QBPresence.Type.online.equals(presence.getType()));
            }
        }
    }
}