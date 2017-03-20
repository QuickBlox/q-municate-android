package com.quickblox.q_municate_core.models;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;

import java.io.Serializable;
import java.util.Comparator;

// Combination DialogNotification and Message (for chats)
public class CombinationMessage extends QBChatMessage implements Serializable {

    private String messageId;
    private DialogOccupant dialogOccupant;
    private Attachment attachment;
    private State state;
    private String body;
    private long createdDate;
    private QBAttachment qbAttachment;
    private DialogNotification.Type notificationType;

    public CombinationMessage(DialogNotification dialogNotification) {
        this.messageId = dialogNotification.getDialogNotificationId();
        this.dialogOccupant = dialogNotification.getDialogOccupant();
        this.state = dialogNotification.getState();
        this.createdDate = dialogNotification.getCreatedDate();
        this.notificationType = dialogNotification.getType();
        this.body = dialogNotification.getBody();
    }

    public CombinationMessage(Message message) {
        this.messageId = message.getMessageId();
        this.dialogOccupant = message.getDialogOccupant();
        this.attachment = message.getAttachment();
        this.state = message.getState();
        this.body = message.getBody();
        this.createdDate = message.getCreatedDate();
        this.setDateSent(createdDate);
        addQBAttachment(attachment);
    }

    public Message toMessage() {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setDialogOccupant(dialogOccupant);
        message.setAttachment(attachment);
        message.setState(state);
        message.setBody(body);
        message.setCreatedDate(createdDate);
        return message;
    }

    public DialogNotification toDialogNotification() {
        DialogNotification dialogNotification = new DialogNotification();
        dialogNotification.setDialogNotificationId(messageId);
        dialogNotification.setDialogOccupant(dialogOccupant);
        dialogNotification.setState(state);
        dialogNotification.setType(notificationType);
        dialogNotification.setBody(body);
        dialogNotification.setCreatedDate(createdDate);
        return dialogNotification;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public DialogNotification.Type getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(DialogNotification.Type notificationType) {
        this.notificationType = notificationType;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public DialogOccupant getDialogOccupant() {
        return dialogOccupant;
    }

    public void setDialogOccupant(DialogOccupant dialogOccupant) {
        this.dialogOccupant = dialogOccupant;
    }

    public boolean isIncoming(int currentUserId) {
        return dialogOccupant != null && dialogOccupant.getUser() != null && currentUserId != dialogOccupant.getUser().getId().intValue();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(CombinationMessage.class.getSimpleName());
        sb.append("[ messageId = " + getMessageId())
                .append(", dialogOccupant = " + getDialogOccupant())
                .append(", attachment = " + getAttachment())
                .append(", state = " + getState())
                .append(", body = " + getBody())
                .append(", createdDate = " + getCreatedDate())
                .append(", notificationType = " + getNotificationType()+ " ]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CombinationMessage) {
            return ((CombinationMessage) object).getMessageId().equals(messageId);
        } else {
            return false;
        }
    }

    private void addQBAttachment(Attachment attachment) {
        String attachType;
        if(attachment == null ){
            return;
        }
        if(attachment.getType() == null){
            attachType = QBAttachment.IMAGE_TYPE;
        } else {
            attachType = attachment.getType().name().toLowerCase();
        }
        qbAttachment = new QBAttachment(attachType);
        qbAttachment.setId(attachment.getAttachmentId());
        qbAttachment.setUrl(attachment.getRemoteUrl());
        qbAttachment.setName(attachment.getName());
        qbAttachment.setSize(attachment.getSize());
        qbAttachment.setHeight(attachment.getHeight());
        qbAttachment.setWidth(attachment.getWidth());
        qbAttachment.setDuration(attachment.getDuration());
        qbAttachment.setData(attachment.getAdditionalInfo());
        this.addAttachment(qbAttachment);
    }

    public static class DateComparator implements Comparator<CombinationMessage> {

        @Override
        public int compare(CombinationMessage combinationMessage1, CombinationMessage combinationMessage2) {
            return ((Long) combinationMessage1.getCreatedDate()).compareTo(
                    ((Long) combinationMessage2.getCreatedDate()));
        }
    }
}