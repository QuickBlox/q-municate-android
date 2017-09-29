package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.chat.JIDHelper;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBChatDialogTypingListener;
import com.quickblox.chat.listeners.QBMessageStatusListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.models.NotificationType;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DateUtilsCore;
import com.quickblox.q_municate_core.utils.DbUtils;
import com.quickblox.q_municate_core.utils.FinderUnknownUsers;
import com.quickblox.q_municate_core.utils.MediaUtils;
import com.quickblox.q_municate_core.utils.MimeTypeAttach;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.utils.DialogTransformUtils;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class QBChatHelper extends BaseThreadPoolHelper{

    private static final String TAG = QBChatHelper.class.getSimpleName();


    private QBChatService chatService;
    private QBUser chatCreator;
    private QBChatDialog currentDialog;
    protected DataManager dataManager;
    private TypingListener typingListener;
    private PrivateChatMessagesStatusListener privateChatMessagesStatusListener;
    private List<QBNotificationChatListener> notificationChatListeners = new CopyOnWriteArrayList<>();
    private final AllChatMessagesListener allChatMessagesListener;
    private final SystemMessageListener systemMessagesListener;
    private QBSystemMessagesManager systemMessagesManager;
    private List<QBChatDialog> groupDialogsList;
    private QBChatDialogParticipantListener participantListener;
    private final ConnectionListener chatConnectionListener;


    public QBChatHelper(Context context) {
        super(context);
        participantListener = new ParticipantListener();
        allChatMessagesListener = new AllChatMessagesListener();
        systemMessagesListener = new SystemMessageListener();
        typingListener = new TypingListener();
        privateChatMessagesStatusListener = new PrivateChatMessagesStatusListener();

        chatConnectionListener = new ChatConnectionListener();
        QBNotificationChatListener notificationChatListener = new PrivateChatNotificationListener();
        addNotificationChatListener(notificationChatListener);

        dataManager = DataManager.getInstance();
    }

    public synchronized void initCurrentChatDialog(QBChatDialog dialog, Bundle additional) throws QBResponseException {
        currentDialog = dialog;
        initCurrentDialogForChatIfPossible();
    }

    private void initCurrentDialogForChatIfPossible() {
        if (currentDialog != null && chatService != null && chatService.isLoggedIn()) {
            currentDialog.initForChat(chatService);
            if (QBDialogType.GROUP.equals(currentDialog.getType())) {
                tryJoinRoomChat(currentDialog);
                currentDialog.addParticipantListener(participantListener);
            } else {
                currentDialog.addIsTypingListener(typingListener);
            }
        }
    }

    public synchronized void closeChat(QBChatDialog chatDialog, Bundle additional) {
        if (currentDialog != null
                && currentDialog.getDialogId().equals(chatDialog.getDialogId())
                && chatService != null
                && chatService.isLoggedIn()) {
            chatDialog.initForChat(chatService);
            if (QBDialogType.GROUP.equals(currentDialog.getType())) {
                if (currentDialog.getParticipantListeners().contains(participantListener)) {
                    currentDialog.removeParticipantListener(participantListener);
                }
            } else {
                if (currentDialog.getIsTypingListeners().contains(typingListener)) {
                    currentDialog.removeIsTypingListener(typingListener);
                }
            }

            currentDialog = null;
        }
    }

    public void initChatService() {
        Log.v(TAG, "initChatService()");
        this.chatService = QBChatService.getInstance();
        chatService.addConnectionListener(chatConnectionListener);
    }

    public void init(QBUser chatCreator) {
        Log.v(TAG, "init()");
        this.chatCreator = chatCreator;

        initCurrentDialogForChatIfPossible();

        initMainChatListeners();
    }

    private void initMainChatListeners() {
        chatService.getMessageStatusesManager().addMessageStatusListener(privateChatMessagesStatusListener);
        chatService.getIncomingMessagesManager().addDialogMessageListener(allChatMessagesListener);

        systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
        systemMessagesManager.addSystemMessageListener(systemMessagesListener);
    }

    public boolean isLoggedInToChat(){
        return chatService != null && chatService.isLoggedIn();
    }

    protected void addNotificationChatListener(QBNotificationChatListener notificationChatListener) {
        notificationChatListeners.add(notificationChatListener);
    }

    public void sendChatMessage(String message) throws QBResponseException {
        QBChatMessage qbChatMessage = getQBChatMessage(message);
        sendAndSaveChatMessage(qbChatMessage, currentDialog);
    }

    public void sendMessageWithAttachment(Attachment.Type attachmentType, Object attachmentObject, String localPath) throws QBResponseException {
        String messageBody = "";
        QBAttachment attachment = null;
        switch (attachmentType) {
            case IMAGE:
                messageBody = context.getString(R.string.dlg_attached_last_message);
                attachment = getAttachmentImage((QBFile) attachmentObject, localPath);
                break;
            case LOCATION:
                messageBody = context.getString(R.string.dlg_location_last_message);
                attachment = getAttachmentLocation((String) attachmentObject);
                break;
            case VIDEO:
                messageBody = context.getString(R.string.dlg_attached_video_last_message);
                attachment = getAttachmentVideo((QBFile) attachmentObject, localPath);
                break;
            case AUDIO:
                messageBody = context.getString(R.string.dlg_attached_audio_last_message);
                attachment = getAttachmentAudio((QBFile) attachmentObject, localPath);
                break;
            case DOC:
                break;
            case OTHER:
                break;
        }

        QBChatMessage message = getQBChatMessage(messageBody);
        message.addAttachment(attachment);

        sendAndSaveChatMessage(message, currentDialog);
    }

    public void sendAndSaveChatMessage(QBChatMessage qbChatMessage, QBChatDialog chatDialog) throws QBResponseException {
        addNecessaryPropertyForQBChatMessage(qbChatMessage, chatDialog.getDialogId());

        sendChatMessage(qbChatMessage, chatDialog);
        if (QBDialogType.PRIVATE.equals(chatDialog.getType())) {
            DbUtils.updateDialogModifiedDate(dataManager, chatDialog, ChatUtils.getMessageDateSent(qbChatMessage), false);
            DbUtils.saveMessageOrNotificationToCache(context, dataManager, chatDialog.getDialogId(), qbChatMessage, State.SYNC, true);
        }
    }

    public void sendChatMessage(QBChatMessage message, QBChatDialog chatDialog) throws QBResponseException {
        message.setMarkable(true);
        chatDialog.initForChat(chatService);

        if (QBDialogType.GROUP.equals(chatDialog.getType())) {
            if (!chatDialog.isJoined()) {
                tryJoinRoomChat(chatDialog);
            }
        }

        String error = null;
        try {
            chatDialog.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            error = context.getString(R.string.dlg_fail_connection);
        }
        if (error != null) {
            throw new QBResponseException(error);
        }
    }


    protected void addNecessaryPropertyForQBChatMessage(QBChatMessage qbChatMessage, String dialogId) {
        long time = DateUtilsCore.getCurrentTime();
        qbChatMessage.setDialogId(dialogId);
        qbChatMessage.setProperty(ChatNotificationUtils.PROPERTY_DATE_SENT, time + ConstsCore.EMPTY_STRING);
    }

    public List<QBChatDialog> getDialogs(QBRequestGetBuilder qbRequestGetBuilder, Bundle returnedBundle) throws QBResponseException {
        List<QBChatDialog> qbDialogsList = QBRestChatService.getChatDialogs(null, qbRequestGetBuilder).perform();
        return qbDialogsList;
    }


    public List<QBChatMessage> getDialogMessages(QBRequestGetBuilder customObjectRequestBuilder,
                                                 Bundle returnedBundle, QBChatDialog qbDialog,
                                                 long lastDateLoad) throws QBResponseException {
        List<QBChatMessage> qbMessagesList = QBRestChatService.getDialogMessages(qbDialog,
                customObjectRequestBuilder).perform();

        if (qbMessagesList != null && !qbMessagesList.isEmpty()) {
            DbUtils.saveMessagesToCache(context, dataManager, qbMessagesList, qbDialog.getDialogId());
        }

        return qbMessagesList;
    }

    public void deleteDialog(String dialogId, QBDialogType dialogType) {
        try {
            if (QBDialogType.GROUP.equals(dialogType)){
                prepareAndSendNotificationsToOpponents(dialogId);
            }

            QBRestChatService.deleteDialog(dialogId, false).perform();
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }

        DbUtils.deleteDialogLocal(dataManager, dialogId);
    }

    public void saveDialogsToCache(List<QBChatDialog> qbDialogsList, boolean allDialogs){
        if (qbDialogsList != null && !qbDialogsList.isEmpty()) {
            FinderUnknownUsers finderUnknownUsers = new FinderUnknownUsers(context, AppSession.getSession().getUser(), qbDialogsList);
            finderUnknownUsers.find();
            if(allDialogs) {
                DbUtils.saveDialogsToCacheAll(DataManager.getInstance(), qbDialogsList, currentDialog);
            } else {
                DbUtils.saveDialogsToCacheByIds(DataManager.getInstance(), qbDialogsList, currentDialog);
            }
            DbUtils.updateDialogsOccupantsStatusesIfNeeded(DataManager.getInstance(), qbDialogsList);
        }
    }

    private void prepareAndSendNotificationsToOpponents(String dialogId) throws QBResponseException {
        QBChatDialog storeChatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(dialogId);
        if (storeChatDialog == null || storeChatDialog.getDialogId() == null) {
            return;
        }

        ArrayList<Integer> actualOpponentsIds =
                ChatUtils.createOccupantsIdsFromDialogOccupantsList(dataManager.getDialogOccupantDataManager()
                        .getActualDialogOccupantsByDialog(storeChatDialog.getDialogId()));
        storeChatDialog.setOccupantsIds(actualOpponentsIds);
        storeChatDialog.initForChat(QBChatService.getInstance());

        List<Integer> occupantsIdsList = new ArrayList<>();
        occupantsIdsList.add(AppSession.getSession().getUser().getId());
        sendGroupMessageToFriends(
                storeChatDialog,
                DialogNotification.Type.OCCUPANTS_DIALOG, occupantsIdsList, true);
    }

    private QBChatMessage getQBChatMessage(String body) {
        long time = DateUtilsCore.getCurrentTime();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        chatMessage.setProperty(ChatNotificationUtils.PROPERTY_DATE_SENT, String.valueOf(time));
        chatMessage.setSaveToHistory(ChatNotificationUtils.VALUE_SAVE_TO_HISTORY);

        return chatMessage;
    }

    private QBAttachment getAttachment(QBFile file, String attachType, String contentType) {
        QBAttachment attachment = new QBAttachment(attachType);
        attachment.setId(file.getUid());
        attachment.setName(file.getName());
        attachment.setContentType(contentType);
        attachment.setSize(file.getSize());
        return attachment;
    }

    private QBAttachment getAttachmentImage(QBFile file, String localPath) {
        QBAttachment attachment = getAttachment(file, QBAttachment.IMAGE_TYPE, MimeTypeAttach.IMAGE_MIME);

        if (!TextUtils.isEmpty(localPath)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(localPath, options);
            attachment.setWidth(options.outWidth);
            attachment.setHeight(options.outHeight);
        }

        return attachment;
    }

    private QBAttachment getAttachmentLocation(String location) {
        QBAttachment attachment = new QBAttachment(Attachment.Type.LOCATION.toString().toLowerCase());
        attachment.setData(location);
        attachment.setId(String.valueOf(location.hashCode()));

        return attachment;
    }

    private QBAttachment getAttachmentAudio(QBFile file, String localPath) {
        QBAttachment attachment = getAttachment(file, QBAttachment.AUDIO_TYPE, MimeTypeAttach.AUDIO_MIME);

        if (!TextUtils.isEmpty(localPath)) {
            int durationSec = MediaUtils.getMetaData(localPath).durationSec();
            attachment.setDuration(durationSec);
        }
        return attachment;
    }

    private QBAttachment getAttachmentVideo(QBFile file, String localPath) {
        QBAttachment attachment = getAttachment(file, QBAttachment.VIDEO_TYPE, MimeTypeAttach.VIDEO_MIME);

        if (!TextUtils.isEmpty(localPath)) {
            MediaUtils.MetaData metaData = MediaUtils.getMetaData(localPath);
            attachment.setHeight(metaData.videoHeight());
            attachment.setWidth(metaData.videoWidth());
            attachment.setDuration(metaData.durationSec());
        }
        return attachment;
    }

    public void sendTypingStatusToServer(boolean startTyping) {
        if (currentDialog != null && chatService.isLoggedIn()) {
            try {
                if (startTyping) {
                    currentDialog.sendIsTypingNotification();
                } else {
                    currentDialog.sendStopTypingNotification();
                }
            } catch (XMPPException | SmackException.NotConnectedException /*| QBResponseException*/ e) {
                ErrorUtils.logError(e);
            }
        }
    }

    public QBChatDialog createPrivateChatOnRest(int opponentId) throws QBResponseException {
        QBChatDialog dialog = QBRestChatService.createChatDialog(DialogUtils.buildPrivateDialog(opponentId)).perform();
        return dialog;
    }

    public QBChatDialog createPrivateDialogIfNotExist(int userId) throws QBResponseException {
        QBChatDialog existingPrivateDialog = ChatUtils.getExistPrivateDialog(dataManager, userId);
        if (existingPrivateDialog == null) {
            existingPrivateDialog = createPrivateChatOnRest(userId);
            DbUtils.saveDialogToCache(dataManager, existingPrivateDialog, false);
        }
        return existingPrivateDialog;
    }

    protected void checkForSendingNotification(boolean ownMessage, QBChatMessage qbChatMessage, QMUser user,
                                               boolean isPrivateChat) {
        String dialogId = qbChatMessage.getDialogId();
        if (qbChatMessage.getId() == null || dialogId == null) {
            return;
        }

        if (!ownMessage) {
            sendNotificationBroadcast(QBServiceConsts.GOT_CHAT_MESSAGE, qbChatMessage, user, dialogId, isPrivateChat);
        }

        if (currentDialog != null) {
            if (!ownMessage && !currentDialog.getDialogId().equals(dialogId)) {
                sendNotificationBroadcast(QBServiceConsts.GOT_CHAT_MESSAGE_LOCAL, qbChatMessage, user, dialogId, isPrivateChat);
            }
        } else if (!ownMessage) {
            sendNotificationBroadcast(QBServiceConsts.GOT_CHAT_MESSAGE_LOCAL, qbChatMessage, user, dialogId,
                    isPrivateChat);
        }
    }

    private void sendNotificationBroadcast(String action, QBChatMessage chatMessage, QMUser user, String dialogId,
                                           boolean isPrivateMessage) {
        Intent intent = new Intent(action);
        String messageBody = chatMessage.getBody();
        String extraChatMessage;

        if (!CollectionsUtil.isEmpty(chatMessage.getAttachments())) {
            //in current version we can send only one attachment
            QBAttachment attachment = chatMessage.getAttachments().iterator().next();
            extraChatMessage = context.getResources().getString(R.string.was_attached, attachment.getType());
        } else {
            extraChatMessage = messageBody;
        }

        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, extraChatMessage);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(QBServiceConsts.EXTRA_IS_PRIVATE_MESSAGE, isPrivateMessage);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        context.sendBroadcast(intent);
    }

    protected void notifyMessageTyping(int userId, boolean isTyping) {
        Intent intent = new Intent(QBServiceConsts.TYPING_MESSAGE);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);
        intent.putExtra(QBServiceConsts.EXTRA_IS_TYPING, isTyping);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    protected Message parseReceivedMessage(QBChatMessage qbChatMessage) {
        long dateSent = ChatUtils.getMessageDateSent(qbChatMessage);
        String attachUrl = ChatUtils.getAttachUrlIfExists(qbChatMessage);
        String dialogId = qbChatMessage.getDialogId();

        Message message = new Message();
        message.setMessageId(qbChatMessage.getId());
        message.setBody(qbChatMessage.getBody());
        message.setCreatedDate(dateSent);
        message.setState(State.DELIVERED);

        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(dialogId, qbChatMessage.getSenderId());
        if (dialogOccupant == null) {
            dialogOccupant = new DialogOccupant();
            QBChatDialog chatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(dialogId);
            if (chatDialog != null) {
                dialogOccupant.setDialog(DialogTransformUtils.createLocalDialog(chatDialog));
            }
            QMUser user = QMUserService.getInstance().getUserCache().get((long) qbChatMessage.getSenderId());
            if (user != null) {
                dialogOccupant.setUser(user);
            }
        }

        message.setDialogOccupant(dialogOccupant);

        if (qbChatMessage.getAttachments() != null && !qbChatMessage.getAttachments().isEmpty()) {
            Attachment attachment = new Attachment();
//            if (getAttachmentType(qbChatMessage.getAttachments()).equalsIgnoreCase(Attachment.Type.LOCATION.toString())) {
//                attachment.setType(Attachment.Type.LOCATION);
//            } else {
//                attachment.setType(Attachment.Type.IMAGE);
//            }
            if (getAttachmentType(qbChatMessage.getAttachments()).equalsIgnoreCase(QBAttachment.PHOTO_TYPE)) {
                attachment.setType(Attachment.Type.IMAGE);
            } else {
                attachment.setType(Attachment.Type.valueOf(getAttachmentType(qbChatMessage.getAttachments()).toUpperCase()));
            }
            attachment.setRemoteUrl(attachUrl);
            message.setAttachment(attachment);
        }

        return message;
    }

    private String getAttachmentType(Collection<QBAttachment> attachments) {
        QBAttachment attachment = attachments.iterator().next();
        return attachment.getType();
    }

    //Update message status

    public void updateStatusNotificationMessageRead(QBChatDialog chatDialog, CombinationMessage combinationMessage) throws Exception {
        updateStatusMessageReadServer(chatDialog, combinationMessage, true);
        DbUtils.updateStatusNotificationMessageLocal(dataManager, combinationMessage.toDialogNotification());
    }

    public void updateStatusMessageRead(QBChatDialog chatDialog, CombinationMessage combinationMessage,
                                        boolean isOnline) throws Exception {
        updateStatusMessageReadServer(chatDialog, combinationMessage, isOnline);
        DbUtils.updateStatusMessageLocal(dataManager, combinationMessage.toMessage());
    }

    public void updateStatusMessageReadServer(QBChatDialog chatDialog, CombinationMessage combinationMessage,
                                              boolean isOnline) throws Exception {
        if (isOnline) {
            chatDialog.initForChat(chatService);

            QBChatMessage qbChatMessage = new QBChatMessage();
            qbChatMessage.setId(combinationMessage.getMessageId());
            qbChatMessage.setDialogId(chatDialog.getDialogId());
            qbChatMessage.setSenderId(combinationMessage.getDialogOccupant().getUser().getId());

            chatDialog.readMessage(qbChatMessage);
        } else {
            StringifyArrayList<String> messagesIdsList = new StringifyArrayList<String>();
            messagesIdsList.add(combinationMessage.getMessageId());
            QBRestChatService.markMessagesAsRead(chatDialog.getDialogId(), messagesIdsList).perform();
        }
    }

    //Group dialogs

    private void createGroupDialogByNotification(QBChatMessage qbChatMessage, DialogNotification.Type notificationType) {
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
        QMUser user = QMUserService.getInstance().getUserCache().get((long) qbChatMessage.getSenderId());
        checkForSendingNotification(ownMessage, qbChatMessage, user, false);
    }

    public synchronized void tryJoinRoomChatsPage(List<QBChatDialog> qbDialogsList, boolean needClean) {
        if (!qbDialogsList.isEmpty()) {
            initGroupDialogsList(needClean);
            for (QBChatDialog dialog : qbDialogsList) {
                if (!QBDialogType.PRIVATE.equals(dialog.getType())) {
                    groupDialogsList.add(dialog);
                    tryJoinRoomChat(dialog, null);
                }
            }
        }
    }

    public void tryJoinRoomChats() {
        List<QBChatDialog> qbDialogsList = dataManager.getQBChatDialogDataManager().getAll();
        if (CollectionsUtil.isEmpty(qbDialogsList)) {
            return;
        }
        for (QBChatDialog dialog : qbDialogsList) {
            if (QBDialogType.PRIVATE != dialog.getType()) {
                tryJoinRoomChat(dialog, null);
            }
        }
    }

    private void initGroupDialogsList(boolean needClean) {
        if (groupDialogsList == null) {
            groupDialogsList = new ArrayList<>();
        } else {
            if (needClean) {
                groupDialogsList.clear();
            }
        }
    }

    public void tryJoinRoomChat(QBChatDialog dialog) {
        try {
            joinRoomChat(dialog);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    public void tryJoinRoomChat(QBChatDialog dialog, QBEntityCallback<Void> callback) {
        joinRoomChat(dialog, callback);
    }

    public void joinRoomChat(QBChatDialog dialog, QBEntityCallback<Void> callback) {
        dialog.initForChat(chatService);
        if (!dialog.isJoined()) { //join only to unjoined dialogs
            dialog.join(history(), callback); //join asynchronously, this doesn't block current thread to enqueue join for next dialog
        }
    }

    public void joinRoomChat(QBChatDialog dialog) throws XMPPException, SmackException {
        dialog.initForChat(chatService);
        if (!dialog.isJoined()) { //join only to unjoined dialogs
            dialog.join(history());
        }
    }

    private DiscussionHistory history() {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0); // without getting messages
        return history;
    }

    public void leaveDialogs() throws XMPPException, SmackException.NotConnectedException {
        if (groupDialogsList != null) {
            for (QBChatDialog dialog : groupDialogsList) {
                if (dialog.isJoined()) {
                    dialog.leave();
                }
            }
        }
    }

    public void leaveRoomChat(QBChatDialog chatDialog) throws Exception {
        chatDialog.initForChat(chatService);
        chatDialog.leave();
        List<Integer> userIdsList = new ArrayList<Integer>();
        userIdsList.add(chatCreator.getId());
        removeUsersFromDialog(chatDialog, userIdsList);
    }

    public boolean isDialogJoined(QBChatDialog dialog) {
        return dialog.isJoined();
    }

    public void sendGroupMessageToFriends(QBChatDialog qbDialog, DialogNotification.Type notificationType,
                                          Collection<Integer> occupantsIdsList, boolean leavedFromDialog) throws QBResponseException {
        QBChatMessage chatMessage = ChatNotificationUtils.createGroupMessageAboutUpdateChat(context, qbDialog,
                notificationType, occupantsIdsList, leavedFromDialog);
        sendChatMessage(chatMessage, qbDialog);
    }

    public QBChatDialog addUsersToDialog(String dialogId, List<Integer> userIdsList) throws Exception {
        StringifyArrayList<Integer> occupantsIdsList = new StringifyArrayList<>(userIdsList);
        QBChatDialog chatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(dialogId);

        QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
        requestBuilder.push(com.quickblox.chat.Consts.DIALOG_OCCUPANTS, occupantsIdsList.getItemsAsString());
        return updateDialog(chatDialog, requestBuilder);
    }

    public void removeUsersFromDialog(QBChatDialog chatDialog, List<Integer> userIdsList) throws QBResponseException {
        QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
        requestBuilder.pullAll(com.quickblox.chat.Consts.DIALOG_OCCUPANTS, userIdsList.toArray());
        updateDialog(chatDialog, requestBuilder);
        DataManager.getInstance().getQBChatDialogDataManager().deleteById(chatDialog.getDialogId());
    }

    public QBChatDialog updateDialog(QBChatDialog dialog) throws QBResponseException {
        return updateDialog(dialog, (QBRequestUpdateBuilder) null);
    }

    public QBChatDialog updateDialog(QBChatDialog dialog, File inputFile) throws QBResponseException {
        QBFile file = QBContent.uploadFileTask(inputFile, true, (String) null).perform();
        dialog.setPhoto(file.getPublicUrl());
        return updateDialog(dialog, (QBRequestUpdateBuilder) null);
    }

    private QBChatDialog updateDialog(QBChatDialog chatDialog, QBRequestUpdateBuilder requestBuilder) throws QBResponseException {
        QBChatDialog updatedDialog = QBRestChatService.updateGroupChatDialog(chatDialog, requestBuilder).perform();
        return updatedDialog;
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
        sendChatMessage(chatMessage, qbDialog);

        return qbDialog;
    }

    //System messages
    //
    public void sendSystemMessage(QBChatMessage chatMessage, int opponentId, String dialogId) {
        addNecessaryPropertyForQBChatMessage(chatMessage, dialogId);
        chatMessage.setRecipientId(opponentId);
        try {
            systemMessagesManager.sendSystemMessage(chatMessage);
        } catch (SmackException.NotConnectedException e) {
            ErrorUtils.logError(e);
        }
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

    public QBFile loadAttachFile(File inputFile) throws Exception {
        QBFile file;

        try {
            file = QBContent.uploadFileTask(inputFile, true, null).perform();
        } catch (QBResponseException exc) {
            throw new Exception(context.getString(R.string.dlg_fail_upload_attach));
        }

        return file;
    }

    private void updateGroupDialogByNotification(QBChatMessage qbChatMessage) {
        String dialogId = qbChatMessage.getDialogId();
        QBChatDialog qbDialog = dataManager.getQBChatDialogDataManager().getByDialogId(dialogId);
        if (qbDialog == null) {
            qbDialog = ChatNotificationUtils.parseDialogFromQBMessage(context, qbChatMessage, QBDialogType.GROUP);
        }

        ChatNotificationUtils.updateDialogFromQBMessage(context, dataManager, qbChatMessage, qbDialog);
        DbUtils.saveDialogToCache(dataManager, qbDialog);

        notifyUpdatingDialog();
    }

    protected void notifyUpdatingDialog() {
        Intent intent = new Intent(QBServiceConsts.UPDATE_DIALOG);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void onChatMessageReceived(String dialogId, QBChatMessage chatMessage, Integer senderId) {
        boolean ownMessage = chatMessage.getSenderId().equals(chatCreator.getId());

        if (ChatNotificationUtils.isNotificationMessage(chatMessage)) {
            if (isNotificationToGroupChat(chatMessage)) {
                if (!ownMessage) {
                    updateGroupDialogByNotification(chatMessage);
                } else if(isNotificationDeletedGroupChat(chatMessage)) {
//                    not notify and return if currentUser deleted groupDialog
                    return;
                }
            } else {
                for (QBNotificationChatListener notificationChatListener : notificationChatListeners) {
                    notificationChatListener.onReceivedNotification((String) chatMessage.getProperty(
                            ChatNotificationUtils.PROPERTY_NOTIFICATION_TYPE), chatMessage);
                }
            }
        }

        QMUser user = QMUserService.getInstance().getUserCache().get((long) senderId);
        QBChatDialog chatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(dialogId);

        //Can receive message from unknown dialog only for QBDialogType.PRIVATE
        if (chatDialog == null) {
            chatDialog = ChatNotificationUtils.parseDialogFromQBMessage(context, chatMessage, QBDialogType.PRIVATE);
            ChatUtils.addOccupantsToQBDialog(chatDialog, chatMessage);
            DbUtils.saveDialogToCache(dataManager, chatDialog, false);
        }

        boolean isPrivateChatMessage = QBDialogType.PRIVATE.equals(chatDialog.getType());
        boolean needNotifyObserver = true;

        DbUtils.updateDialogModifiedDate(dataManager, chatDialog, ChatUtils.getMessageDateSent(chatMessage), false);
        DbUtils.saveMessageOrNotificationToCache(context, dataManager, dialogId, chatMessage,
                !ownMessage
                        ? State.DELIVERED
                        : State.SYNC,
                needNotifyObserver);

        checkForSendingNotification(ownMessage, chatMessage, user, isPrivateChatMessage);
    }

    private boolean isNotificationToGroupChat(QBChatMessage chatMessage) {
        String updatedInfo = (String) chatMessage.getProperty(ChatNotificationUtils.PROPERTY_ROOM_UPDATE_INFO);
        return updatedInfo != null;
    }

    private boolean isNotificationDeletedGroupChat(QBChatMessage chatMessage) {
        String deletedOccupantsIdsString = (String) chatMessage.getProperty(ChatNotificationUtils.PROPERTY_ROOM_DELETED_OCCUPANTS_IDS);
        return deletedOccupantsIdsString != null;
    }

    protected void notifyUpdatingDialogDetails(int userId, boolean online) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_DIALOG_DETAILS);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);
        intent.putExtra(QBServiceConsts.EXTRA_STATUS, online);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void friendRequestMessageReceived(QBChatMessage qbChatMessage, DialogNotification.Type notificationType) {
        String dialogId = qbChatMessage.getDialogId();
        Message message = parseReceivedMessage(qbChatMessage);

        if (!QMUserService.getInstance().getUserCache().exists((long) qbChatMessage.getSenderId())) {
            QBRestHelper.loadAndSaveUser(qbChatMessage.getSenderId());
        }

        DialogNotification dialogNotification = ChatUtils.convertMessageToDialogNotification(message);
        dialogNotification.setType(notificationType);

        QBChatDialog chatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(dialogId);
        if (chatDialog == null) {
            QBChatDialog newChatDialog = ChatNotificationUtils.parseDialogFromQBMessage(context, qbChatMessage, QBDialogType.PRIVATE);
            ArrayList<Integer> occupantsIdsList = ChatUtils.createOccupantsIdsFromPrivateMessage(chatCreator.getId(), qbChatMessage.getSenderId());
            newChatDialog.setOccupantsIds(occupantsIdsList);
            DbUtils.saveDialogToCache(dataManager, newChatDialog, false);
        }

        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(dialogId, qbChatMessage.getSenderId());
        DbUtils.saveDialogNotificationToCache(context, dataManager, dialogOccupant, qbChatMessage, false);

        if (dialogOccupant != null) {
            checkForSendingNotification(false, qbChatMessage, dialogOccupant.getUser(), true);
        }
    }

    interface QBNotificationChatListener {

        void onReceivedNotification(String notificationType, QBChatMessage chatMessage);
    }

    private class ChatConnectionListener extends AbstractConnectionListener{
        @Override
        public void reconnectionSuccessful() {
            tryJoinRoomChats();
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            chatCreator = AppSession.getSession().getUser();
            initMainChatListeners();
        }
    }

    private class AllChatMessagesListener implements QBChatDialogMessageListener {

        @Override
        public void processMessage(final String dialogId, final QBChatMessage qbChatMessage, final Integer senderId) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    onChatMessageReceived(dialogId, qbChatMessage, senderId);
                }
            });
        }

        @Override
        public void processError(String dialogId, QBChatException exception, QBChatMessage qbChatMessage, Integer integer) {
            //TODO VT need implement for process error message
        }
    }

    private class PrivateChatMessagesStatusListener implements QBMessageStatusListener {

        @Override
        public void processMessageDelivered(String messageId, String dialogId, Integer userId) {
            DbUtils.updateStatusMessageLocal(dataManager, messageId, State.DELIVERED);
        }

        @Override
        public void processMessageRead(String messageId, String dialogId, Integer userId) {
            DbUtils.updateStatusMessageLocal(dataManager, messageId, State.READ);
        }
    }

    private class TypingListener implements QBChatDialogTypingListener {

        @Override
        public void processUserIsTyping(String dialogId, Integer userId) {
            notifyMessageTyping(userId, true);
        }

        @Override
        public void processUserStopTyping(String dialogId, Integer userId) {
            notifyMessageTyping(userId, false);
        }
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
                        createGroupDialogByNotification(qbChatMessage, DialogNotification.Type.CREATE_DIALOG);
                    }
                }
            });

        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {
            ErrorUtils.logError(e);
        }
    }

    private class PrivateChatNotificationListener implements QBNotificationChatListener {

        @Override
        public void onReceivedNotification(String notificationTypeString, QBChatMessage chatMessage) {
            NotificationType notificationType = NotificationType.parseByValue(
                    Integer.parseInt(notificationTypeString));
            switch (notificationType) {
                case FRIENDS_REQUEST:
                    friendRequestMessageReceived(chatMessage, DialogNotification.Type.FRIENDS_REQUEST);
                    break;
                case FRIENDS_ACCEPT:
                    friendRequestMessageReceived(chatMessage, DialogNotification.Type.FRIENDS_ACCEPT);
                    break;
                case FRIENDS_REJECT:
                    friendRequestMessageReceived(chatMessage, DialogNotification.Type.FRIENDS_REJECT);
                    break;
                case FRIENDS_REMOVE:
                    friendRequestMessageReceived(chatMessage, DialogNotification.Type.FRIENDS_REMOVE);
                    clearFriendOrUserRequestLocal(chatMessage.getSenderId());
                    break;
            }
        }

        private void clearFriendOrUserRequestLocal(int userId) {
            boolean friend = dataManager.getFriendDataManager().existsByUserId(userId);
            boolean outgoingUserRequest = dataManager.getUserRequestDataManager().existsByUserId(userId);
            if (friend) {
                dataManager.getFriendDataManager().deleteByUserId(userId);
            } else if (outgoingUserRequest) {
                dataManager.getUserRequestDataManager().deleteByUserId(userId);
            }
        }
    }

    private class ParticipantListener implements QBChatDialogParticipantListener {

        @Override
        public void processPresence(String dialogId, QBPresence presence) {
            boolean validData = currentDialog != null && presence.getUserId() != null && currentDialog.getRoomJid() != null;
            if (validData && currentDialog.getRoomJid().equals(JIDHelper.INSTANCE.getRoomJidByDialogId(dialogId))) {
                notifyUpdatingDialogDetails(presence.getUserId(), QBPresence.Type.online.equals(presence.getType()));
            }
        }
    }
}