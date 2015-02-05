package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.listeners.QBParticipantListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.MessagesNotificationType;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.FinderUnknownFriends;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QBMultiChatHelper extends QBBaseChatHelper {

    private static final String TAG = QBMultiChatHelper.class.getSimpleName();

    private QBNotificationChatListener notificationChatListener;
    private QBParticipantListener participantListener;
    private List<QBDialog> groupDialogsList;

    public QBMultiChatHelper(Context context) {
        super(context);
        notificationChatListener = new GroupChatNotificationListener();
        participantListener = new ParticipantListener();
        addNotificationChatListener(notificationChatListener);
    }

    @Override
    public QBChat createChatLocally(QBDialog dialog, Bundle additional) throws QBResponseException {
        QBGroupChat roomChat = createGroupChatIfNotExist(dialog);
        roomChat.addParticipantListener(participantListener);
        currentDialog = dialog;
        return roomChat;
    }

    @Override
    public void closeChat(QBDialog dialogId, Bundle additional) {
        currentDialog = null;
    }

    public void init(QBUser user) {
        super.init(user);
    }

    public void onGroupMessageReceived(QBChat chat, QBChatMessage chatMessage) {
        if (!Utils.validateNotNull(chatMessage.getDialogId())) {
            return;
        }

        User user = UsersDatabaseManager.getUserById(context, chatMessage.getSenderId());

        MessageCache messageCache;

        if (user == null) {
            user = ChatUtils.getTempUserFromChatMessage(chatMessage);
        }

        messageCache = parseReceivedMessage(chatMessage);

        boolean ownMessage = chatMessage.getSenderId().equals(chatCreator.getId());

        if (ChatNotificationUtils.isNotificationMessage(chatMessage)) {
            messageCache.setMessagesNotificationType(
                    ChatNotificationUtils.getUpdateChatNotificationMessageType(chatMessage));
            messageCache.setMessage(ChatNotificationUtils.getBodyForUpdateChatNotificationMessage(context,
                    chatMessage));
            if (!ownMessage) {
                updateDialogByNotification(chatMessage);
            }
        }

        saveMessageToCache(messageCache);

        if (!ownMessage) {
            // TODO IS handle logic when friend is not in the friend list
            notifyMessageReceived(chatMessage, user, messageCache.getDialogId(), false);
        }
    }

    public void sendGroupMessage(String roomJidId,
            String message) throws QBResponseException, IllegalStateException {
        QBChatMessage chatMessage = getQBChatMessage(message, null);
        sendGroupMessage(chatMessage, roomJidId, currentDialog.getDialogId());
    }

    private void sendGroupMessage(QBChatMessage chatMessage, String roomJId,
            String dialogId) throws QBResponseException {
        QBGroupChat groupChat = groupChatManager.getGroupChat(roomJId);
        QBDialog existingDialog = null;
        if (groupChat == null) {
            existingDialog = ChatDatabaseManager.getDialogByDialogId(context, dialogId);
            groupChat = (QBGroupChat) createChatLocally(existingDialog, null);
        }
        String error = null;

        addNecessaryPropertyForQBChatMessage(chatMessage, dialogId);

        try {
            groupChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            error = context.getString(R.string.dlg_fail_send_msg);
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
                file);
        sendGroupMessage(chatMessage, roomJidId, currentDialog.getDialogId());
    }

    public void tryJoinRoomChats(List<QBDialog> chatDialogsList) {
        if (!chatDialogsList.isEmpty()) {
            initGroupDialogsList();
            for (QBDialog dialog : chatDialogsList) {
                if (!QBDialogType.PRIVATE.equals(dialog.getType())) {
                    groupDialogsList.add(dialog);
                    tryJoinRoomChat(dialog);
                }
            }
        }
    }

    private void initGroupDialogsList() {
        if (groupDialogsList == null) {
            groupDialogsList = new ArrayList<QBDialog>();
        } else {
            groupDialogsList.clear();
        }
    }

    public QBDialog createGroupChat(String name, List<Integer> friendIdsList) throws Exception {
        ArrayList<Integer> occupantIdsList = ChatUtils.getOccupantIdsWithUser(friendIdsList);

        QBDialog dialogToCreate = new QBDialog();
        dialogToCreate.setName(name);
        dialogToCreate.setType(QBDialogType.GROUP);
        dialogToCreate.setOccupantsIds(occupantIdsList);

        QBDialog dialog = groupChatManager.createDialog(dialogToCreate);

        joinRoomChat(dialog);

        saveDialogToCache(dialog);

        sendNotificationToPrivateChatAboutCreatingGroupChat(dialog, friendIdsList);

        QBChatMessage chatMessage = ChatNotificationUtils.createNotificationMessageForCreateGroupChat(context,
                dialog.getOccupants());
        sendGroupMessage(chatMessage, dialog.getRoomJid(), dialog.getDialogId());

        return dialog;
    }

    public void sendNotificationToPrivateChatAboutCreatingGroupChat(QBDialog dialog,
            List<Integer> friendIdsList) throws Exception {
        for (Integer friendId : friendIdsList) {
            try {
                sendNotificationToPrivateChatAboutCreatingGroupChat(dialog, friendId);
            } catch (QBResponseException e) {
                ErrorUtils.logError(e);
            }
        }
    }

    private void sendNotificationToPrivateChatAboutCreatingGroupChat(QBDialog dialog,
            Integer friendId) throws Exception {
        QBChatMessage chatMessageForSending = ChatNotificationUtils
                .createMessageToPrivateChatAboutCreatingGroupChat(dialog, context.getResources().getString(
                        R.string.cht_notification_message));

        addNecessaryPropertyForQBChatMessage(chatMessageForSending, dialog.getDialogId());
        sendPrivateMessage(chatMessageForSending, friendId);
    }

    public List<Integer> getRoomOnlineParticipantList(String roomJid) throws XMPPException {
        return new ArrayList<Integer>(groupChatManager.getGroupChat(roomJid).getOnlineUsers());
    }

    public void leaveDialogs() throws XMPPException, SmackException.NotConnectedException {
        if (groupDialogsList != null) {
            for (QBDialog dialog : groupDialogsList) {
                QBGroupChat roomChat = groupChatManager.getGroupChat(dialog.getRoomJid());
                if (roomChat != null && roomChat.isJoined()) {
                    roomChat.leave();
                }
            }
        }
    }

    public void leaveRoomChat(
            String roomJid) throws XMPPException, SmackException.NotConnectedException, QBResponseException {
        groupChatManager.getGroupChat(roomJid).leave();
        List<Integer> userIdsList = new ArrayList<Integer>();
        userIdsList.add(chatCreator.getId());
        removeUsersFromDialog(roomJid, userIdsList);

        ChatDatabaseManager.deleteDialogByRoomJid(context, roomJid);
    }

    protected QBGroupChat createGroupChatIfNotExist(QBDialog dialog) throws QBResponseException {
        boolean notNull = Utils.validateNotNull(groupChatManager);
        if (!notNull) {
            ErrorUtils.logError(TAG, " groupChatManager is NULL");
            throw new QBResponseException(context.getString(R.string.dlg_fail_create_chat));
        }
        QBGroupChat groupChat = groupChatManager.getGroupChat(dialog.getRoomJid());
        if (groupChat == null && dialog.getRoomJid() != null) {
            groupChat = groupChatManager.createGroupChat(dialog.getRoomJid());
            groupChat.addMessageListener(groupChatMessageListener);
        }
        return groupChat;
    }

    public void joinRoomChat(QBDialog dialog) throws XMPPException, SmackException, QBResponseException {
        QBGroupChat roomChat = createGroupChatIfNotExist(dialog);
        if (!roomChat.isJoined()) {
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);
            roomChat.join(history);
        }
    }

    protected void tryJoinRoomChat(QBDialog dialog) {
        try {
            joinRoomChat(dialog);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    public QBDialog addUsersToDialog(String dialogId, List<Integer> userIdsList) throws Exception {
        QBDialog dialog = ChatDatabaseManager.getDialogByDialogId(context, dialogId);

        QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
        requestBuilder.push(com.quickblox.chat.Consts.DIALOG_OCCUPANTS, userIdsList.toArray());
        return updateDialog(dialog, requestBuilder);
    }

    public void removeUsersFromDialog(String roomJid, List<Integer> userIdsList) throws QBResponseException {
        QBDialog dialog = ChatDatabaseManager.getDialogByRoomJid(context, roomJid);

        QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
        requestBuilder.pullAll(com.quickblox.chat.Consts.DIALOG_OCCUPANTS, userIdsList.toArray());
        updateDialog(dialog, requestBuilder);
    }

    public QBDialog updateDialog(QBDialog dialog) throws QBResponseException {
        return updateDialog(dialog, (QBRequestUpdateBuilder) null);
    }

    public QBDialog updateDialog(QBDialog dialog, File inputFile) throws QBResponseException {
        QBFile file = QBContent.uploadFileTask(inputFile, true, (String) null);
        dialog.setPhoto(file.getPublicUrl());
        return updateDialog(dialog, (QBRequestUpdateBuilder) null);
    }

    private QBDialog updateDialog(QBDialog dialog,
            QBRequestUpdateBuilder requestBuilder) throws QBResponseException {
        QBDialog updatedDialog = groupChatManager.updateDialog(dialog, requestBuilder);
        return updatedDialog;
    }

    public void sendNotificationToFriends(QBDialog dialog, MessagesNotificationType messagesNotificationType,
            Collection<Integer> addedFriendIdsList) throws QBResponseException {
        QBChatMessage chatMessage = ChatNotificationUtils.createNotificationMessageForUpdateChat(context,
                dialog, messagesNotificationType, addedFriendIdsList);
        sendGroupMessage(chatMessage, dialog.getRoomJid(), dialog.getDialogId());
    }

    private void updateDialogByNotification(QBChatMessage chatMessage) {
        String dialogId = chatMessage.getProperty(ChatNotificationUtils.PROPERTY_DIALOG_ID);
        QBDialog dialog = ChatDatabaseManager.getDialogByDialogId(context, dialogId);

        ChatNotificationUtils.updateDialogFromQBMessage(context, chatMessage, dialog);

        saveDialogToCache(dialog);

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

    private void createDialogByNotification(QBChatMessage chatMessage,
            MessagesNotificationType messagesNotificationType) {
        MessageCache messageCache = parseReceivedMessage(chatMessage);
        messageCache.setMessagesNotificationType(messagesNotificationType);

        String roomJidId;

        QBDialog dialog = ChatNotificationUtils.parseDialogFromQBMessage(context, chatMessage,
                messageCache.getMessage(), QBDialogType.GROUP);
        dialog.setUnreadMessageCount(1);
        saveDialogToCache(dialog);

        roomJidId = dialog.getRoomJid();
        if (roomJidId != null) {
            tryJoinRoomChat(dialog);
            new FinderUnknownFriends(context, chatCreator, dialog).find();
        }

        User senderUser = UsersDatabaseManager.getUserById(context, messageCache.getSenderId());
        notifyMessageReceived(chatMessage, senderUser, messageCache.getDialogId(), false);
    }

    private class GroupChatNotificationListener implements QBNotificationChatListener {

        @Override
        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage) {
            if (ChatNotificationUtils.PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_CREATE.equals(
                    notificationType)) {
                createDialogByNotification(chatMessage, MessagesNotificationType.CREATE_DIALOG);
            } else if (ChatNotificationUtils.PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE.equals(
                    notificationType)) {
                updateDialogByNotification(chatMessage);
            }
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