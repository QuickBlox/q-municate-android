package com.quickblox.q_municate_db.managers;

import android.content.Context;

import com.quickblox.q_municate_db.helpers.DataHelper;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.Social;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.models.UserRequest;

public class DataManager {

    private static DataManager instance;
    private DataHelper dataHelper;

    private UserDataManager userDataManager;
    private FriendDataManager friendDataManager;
    private SocialDataManager socialDataManager;
    private UserRequestDataManager userRequestDataManager;
    private DialogDataManager dialogDataManager;
    private DialogOccupantDataManager dialogOccupantDataManager;
    private DialogNotificationDataManager dialogNotificationDataManager;
    private AttachmentManager attachmentDataManager;
    private MessageDataManager messageDataManager;

    private DataManager(Context context) {
        dataHelper = new DataHelper(context);
    }

    public static void init(Context context) {
        if (null == instance) {
            instance = new DataManager(context);
        }
    }

    public static DataManager getInstance() {
        return instance;
    }

    private DataHelper getDataHelper() {
        return dataHelper;
    }

    public void clearAllTables() {
        dataHelper.clearTables();
    }

    public UserDataManager getUserDataManager() {
        if (userDataManager == null) {
            userDataManager = new UserDataManager(
                    getDataHelper().getDaoByClass(User.class),
                    getDataHelper().getDaoByClass(DialogOccupant.class));
        }
        return userDataManager;
    }

    public FriendDataManager getFriendDataManager() {
        if (friendDataManager == null) {
            friendDataManager = new FriendDataManager(
                    getDataHelper().getDaoByClass(Friend.class),
                    getDataHelper().getDaoByClass(User.class));
        }
        return friendDataManager;
    }

    public SocialDataManager getSocialDataManager() {
        if (socialDataManager == null) {
            socialDataManager = new SocialDataManager(
                    getDataHelper().getDaoByClass(Social.class));
        }
        return socialDataManager;
    }

    public UserRequestDataManager getUserRequestDataManager() {
        if (userRequestDataManager == null) {
            userRequestDataManager = new UserRequestDataManager(
                    getDataHelper().getDaoByClass(UserRequest.class));
        }
        return userRequestDataManager;
    }

    public DialogDataManager getDialogDataManager() {
        if (dialogDataManager == null) {
            dialogDataManager = new DialogDataManager(
                    getDataHelper().getDaoByClass(Dialog.class));
        }
        return dialogDataManager;
    }

    public DialogOccupantDataManager getDialogOccupantDataManager() {
        if (dialogOccupantDataManager == null) {
            dialogOccupantDataManager = new DialogOccupantDataManager(
                    getDataHelper().getDaoByClass(DialogOccupant.class),
                    getDataHelper().getDaoByClass(Dialog.class));
        }
        return dialogOccupantDataManager;
    }

    public DialogNotificationDataManager getDialogNotificationDataManager() {
        if (dialogNotificationDataManager == null) {
            dialogNotificationDataManager = new DialogNotificationDataManager(
                    getDataHelper().getDaoByClass(DialogNotification.class),
                    getDataHelper().getDaoByClass(Dialog.class),
                    getDataHelper().getDaoByClass(DialogOccupant.class));
        }
        return dialogNotificationDataManager;
    }

    public AttachmentManager getAttachmentDataManager() {
        if (attachmentDataManager == null) {
            attachmentDataManager = new AttachmentManager(
                    getDataHelper().getDaoByClass(Attachment.class));
        }
        return attachmentDataManager;
    }

    public MessageDataManager getMessageDataManager() {
        if (messageDataManager == null) {
            messageDataManager = new MessageDataManager(
                    getDataHelper().getDaoByClass(Message.class),
                    getDataHelper().getDaoByClass(Dialog.class),
                    getDataHelper().getDaoByClass(DialogOccupant.class));
        }
        return messageDataManager;
    }
}