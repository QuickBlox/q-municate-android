package com.quickblox.q_municate.qb.helpers;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.module.custom.request.QBCustomObjectUpdateBuilder;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBChatState;
import com.quickblox.module.chat.QBChatStateListener;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBRoomChat;
import com.quickblox.module.chat.QBRoomChatManager;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.model.MessageCache;
import com.quickblox.q_municate.ui.chats.FindUnknownFriendsTask;
import com.quickblox.q_municate.utils.ChatUtils;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.Utils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QBMultiChatHelper extends BaseChatHelper {

    private static final String TAG = QBMultiChatHelper.class.getSimpleName();
    private QBRoomChatManager roomChatManager;
    private QBRoomChat roomChat;
    private RoomChatMessageListener roomChatMessageListener = new RoomChatMessageListener();
    private QBNotificationChatListener notificationChatListener = new RoomNotificationListener();
    private QBDialog currentDialog;

    public QBMultiChatHelper(Context context) {
        super(context);
    }

    @Override
    public QBChat createChatLocally(QBDialog dialog, Bundle additional) throws QBResponseException {
        QBRoomChat roomChat = createChatIfNotExist(dialog);
        currentDialog = dialog;
        return roomChat;
    }

    @Override
    public void closeChat(QBDialog dialogId, Bundle additional) {
        currentDialog = null;
    }

    public void sendGroupMessage(String roomJidId, String message) throws QBResponseException {
        QBChatMessage chatMessage = getQBChatMessage(message, null);
        sendRoomMessage(chatMessage, roomJidId, currentDialog.getDialogId());
    }

    public void init(QBChatService chatService, QBUser user) {
        super.init(chatService, user);
        roomChatManager = chatService.getRoomChatManager();
        chatStateManager.subscribeOnRoomChat(roomChatManager);
        addNotificationChatListener(notificationChatListener);
    }

    private void sendRoomMessage(QBChatMessage chatMessage, String roomJId,
            String dialogId) throws QBResponseException {
        roomChat = roomChatManager.getRoom(roomJId);
        if (roomChat == null) {
            return;
        }
        String error = null;
        if (!TextUtils.isEmpty(dialogId)) {
            chatMessage.setProperty(ChatUtils.PROPERTY_DIALOG_ID, dialogId);
        }
        try {
            roomChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            error = context.getString(R.string.dlg_fail_send_msg);
        } catch (SmackException.NotConnectedException e) {
            error = context.getString(R.string.dlg_fail_connection);
        }
        if (error != null) {
            throw new QBResponseException(error);
        }
    }

    public void sendGroupMessageWithAttachImage(String roomJidId, QBFile file) throws QBResponseException {
        QBChatMessage chatMessage = getQBChatMessage(context.getString(
                R.string.dlg_attached_last_message), file);
        sendRoomMessage(chatMessage, roomJidId, currentDialog.getDialogId());
    }

    private void tryJoinRoomChat(QBDialog dialog) {
        try {
            joinRoomChat(dialog);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    public void tryJoinRoomChats(List<QBDialog> chatDialogsList) {
        if (!chatDialogsList.isEmpty()) {
            for (QBDialog dialog : chatDialogsList) {
                if (!QBDialogType.PRIVATE.equals(dialog.getType())) {
                    tryJoinRoomChat(dialog);
                }
            }
        }
    }

    public QBDialog createRoomChat(String roomName,
            List<Integer> friendIdsList) throws SmackException, XMPPException, QBResponseException {
        ArrayList<Integer> occupantIdsList = ChatUtils.getOccupantIdsWithUser(friendIdsList);
        QBDialog dialog = roomChatManager.createDialog(roomName, QBDialogType.GROUP, occupantIdsList);
        joinRoomChat(dialog);
        inviteFriendsToRoom(dialog, friendIdsList);
        saveDialogToCache(context, dialog);
        return dialog;
    }

    private void inviteFriendsToRoom(QBDialog dialog,
            List<Integer> friendIdsList) throws XMPPException, SmackException {
        for (Integer friendId : friendIdsList) {
            try {
                notifyFriendAboutInvitation(dialog, friendId);
            } catch (QBResponseException responseException) {

            }
        }
    }

    private void notifyFriendsRoomUpdate(QBDialog dialog,
            List<Integer> friendIdsList) {
        for (Integer friendId : friendIdsList) {
            try {
                notifyFriendOnUpdateChat(dialog, friendId);
            } catch (QBResponseException responseException) {
                ErrorUtils.logError(responseException);
            }
        }
    }

    private void notifyFriendAboutInvitation(QBDialog dialog, Integer friendId) throws QBResponseException {
        long time = DateUtils.getCurrentTime();
        QBPrivateChat chat = chatService.getPrivateChatManager().getChat(friendId);
        if (chat == null) {
            chat = chatService.getPrivateChatManager().createChat(friendId, null);
        }
        QBChatMessage chatMessage = ChatUtils.createRoomNotificationMessage(context, dialog);
        chatMessage.setProperty(ChatUtils.PROPERTY_DATE_SENT, time + Consts.EMPTY_STRING);
        try {
            chat.sendMessage(chatMessage);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    private void notifyFriendOnUpdateChat(QBDialog dialog, Integer friendId) throws QBResponseException {
        long time = DateUtils.getCurrentTime();
        QBPrivateChat chat = chatService.getPrivateChatManager().getChat(friendId);
        if (chat == null) {
            chat = chatService.getPrivateChatManager().createChat(friendId, null);
        }
        QBChatMessage chatMessage = ChatUtils.createUpdateChatNotificationMessage(dialog);
        chatMessage.setProperty(ChatUtils.PROPERTY_DATE_SENT, time + Consts.EMPTY_STRING);
        try {
            chat.sendMessage(chatMessage);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    private QBRoomChat createChatIfNotExist(QBDialog dialog) throws QBResponseException {
        boolean notNull = Utils.validateNotNull(roomChatManager);
        if( !notNull){
            ErrorUtils.logError(TAG, " roomChatManager is NULL");
            throw new QBResponseException(context.getString(R.string.dlg_fail_create_chat));
        }
        roomChat = roomChatManager.getRoom(dialog.getRoomJid());
        if (roomChat == null) {
            roomChat = roomChatManager.createRoom(dialog.getRoomJid());
            roomChat.addMessageListener(roomChatMessageListener);
        }
        return roomChat;
    }

    public void joinRoomChat(QBDialog dialog) throws XMPPException, SmackException, QBResponseException {
        QBRoomChat roomChat = createChatIfNotExist(dialog);
        roomChat.join();
    }

    public List<Integer> getRoomOnlineParticipantList(String roomJid) throws XMPPException {
        return new ArrayList<Integer>(roomChatManager.getRoom(roomJid).getOnlineRoomUserIds());
    }

    public void leaveRoomChat(
            String roomJid) throws XMPPException, SmackException.NotConnectedException, QBResponseException {
        roomChatManager.getRoom(roomJid).leave();
        List<Integer> userIdsList = new ArrayList<Integer>();
        userIdsList.add(chatCreator.getId());
        removeUsersFromRoom(roomJid, userIdsList);

        DatabaseManager.deleteDialogByRoomJid(context, roomJid);
    }

    public void addUsersToRoom(String dialogId, List<Integer> userIdsList) throws QBResponseException, XMPPException, SmackException {
        QBDialog dialog = DatabaseManager.getDialogByDialogId(context, dialogId);

        QBCustomObjectUpdateBuilder requestBuilder = new QBCustomObjectUpdateBuilder();
        requestBuilder.push(com.quickblox.internal.module.chat.Consts.DIALOG_OCCUPANTS,
                userIdsList.toArray());
        updateDialog(dialog, requestBuilder);

        inviteFriendsToRoom(dialog, userIdsList);
    }

    public void removeUsersFromRoom(String roomJid, List<Integer> userIdsList) throws QBResponseException {
        QBDialog dialog = DatabaseManager.getDialogByRoomJid(context, roomJid);

        QBCustomObjectUpdateBuilder requestBuilder = new QBCustomObjectUpdateBuilder();
        requestBuilder.pullAll(com.quickblox.internal.module.chat.Consts.DIALOG_OCCUPANTS,
                userIdsList.toArray());
        updateDialog(dialog, requestBuilder);
    }

    public void updateDialog(QBDialog dialog) throws QBResponseException {
        updateDialog(dialog, (QBCustomObjectUpdateBuilder) null);
    }

    public void updateDialog(QBDialog dialog, File inputFile) throws QBResponseException {
        QBFile file = QBContent.uploadFileTask(inputFile, true, (String) null);
        dialog.setPhotoUrl(file.getPublicUrl());
        updateDialog(dialog, (QBCustomObjectUpdateBuilder) null);
    }

    private void updateDialog(QBDialog dialog, QBCustomObjectUpdateBuilder requestBuilder) throws QBResponseException {
        QBDialog updatedDialog = roomChatManager.updateDialog(dialog, requestBuilder);
        ArrayList<Integer> friendsList = new ArrayList<Integer>(updatedDialog.getOccupants());
        friendsList.remove(chatCreator.getId());
        notifyFriendsRoomUpdate(updatedDialog, friendsList);
        DatabaseManager.saveDialog(context, updatedDialog);
    }

    private void createDialogByNotification(QBChatMessage chatMessage) {
        long time;
        String roomJidId;
        time = DateUtils.getCurrentTime();
        QBDialog dialog = ChatUtils.parseDialogFromMessage(chatMessage, chatMessage.getBody(), time);
        roomJidId = dialog.getRoomJid();
        if (roomJidId != null && !QBDialogType.PRIVATE.equals(dialog.getType())) {
            tryJoinRoomChat(dialog);
            new FindUnknownFriendsTask(context).execute(null, dialog);
            saveDialogToCache(context, dialog);
        }
    }

    private class RoomChatMessageListener implements QBChatStateListener<QBRoomChat> {

        @Override
        public void processMessage(QBRoomChat roomChat, QBChatMessage chatMessage) {
            User user = DatabaseManager.getUserById(context, chatMessage.getSenderId());
            String attachUrl = ChatUtils.getAttachUrlIfExists(chatMessage);
            String dialogId = chatMessage.getProperty(ChatUtils.PROPERTY_DIALOG_ID);
            long time = Long.parseLong(chatMessage.getProperty(ChatUtils.PROPERTY_DATE_SENT).toString());
            String messageId = chatMessage.getProperty(ChatUtils.PROPERTY_MESSAGE_ID).toString();
            String packetId = chatMessage.getPacketId();
            boolean isRead = false;
            boolean isDelivered = false;

            Integer userId = AppSession.getSession().getUser().getId();
            if (chatMessage.getSenderId().equals(userId)) {
                isRead = true;
            }

            if(user == null) {
                user = new User();
                user.setUserId(chatMessage.getSenderId());
                user.setFullName(chatMessage.getSenderId() + Consts.EMPTY_STRING);
            }

            saveMessageToCache(new MessageCache(messageId, dialogId, packetId, chatMessage.getSenderId(),
                    chatMessage.getBody(), attachUrl, time, isRead, isDelivered));

            if (!chatMessage.getSenderId().equals(chatCreator.getId())) {
                // TODO IS handle logic when friend is not in the friend list
                notifyMessageReceived(chatMessage, user, dialogId);
            }
        }

        @Override
        public void stateChanged(QBRoomChat roomChat, int participant, QBChatState chatState) {
            //TODO VF add composing state changed
        }
    }

    private class RoomNotificationListener implements QBNotificationChatListener {

        @Override
        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage) {
            if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_CREATE_CHAT.equals(notificationType)) {
                createDialogByNotification(chatMessage);
            } else if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_UPDATE_CHAT.equals(notificationType)) {
                updateDialogByNotification(chatMessage);
            }
        }
    }
}