package com.quickblox.q_municate_db.managers;

import android.content.Context;

import com.quickblox.q_municate_db.helpers.DatabaseHelper;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.Social;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.models.UserRequest;

public class DatabaseManager {

    private static DatabaseManager instance;
    private DatabaseHelper databaseHelper;

    private UserManager userManager;
    private FriendManager friendManager;
    private SocialManager socialManager;
    private UserRequestManager userRequestManager;
    private DialogManager dialogManager;
    private DialogOccupantManager dialogOccupantManager;
    private DialogNotificationManager dialogNotificationManager;
    private AttachmentManager attachmentManager;
    private MessageManager messageManager;

    private DatabaseManager(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public static void init(Context context) {
        if (null == instance) {
            instance = new DatabaseManager(context);
        }
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public void clearAllTables() {
        databaseHelper.clearTables();
    }

    public UserManager getUserManager() {
        if (userManager == null) {
            userManager = new UserManager(getDatabaseHelper().getDaoByClass(User.class));
        }
        return userManager;
    }

    public FriendManager getFriendManager() {
        if (friendManager == null) {
            friendManager = new FriendManager(getDatabaseHelper().getDaoByClass(Friend.class));
        }
        return friendManager;
    }

    public SocialManager getSocialManager() {
        if (socialManager == null) {
            socialManager = new SocialManager(getDatabaseHelper().getDaoByClass(Social.class));
        }
        return socialManager;
    }

    public UserRequestManager getUserRequestManager() {
        if (userRequestManager == null) {
            userRequestManager = new UserRequestManager(getDatabaseHelper().getDaoByClass(UserRequest.class));
        }
        return userRequestManager;
    }

    public DialogManager getDialogManager() {
        if (dialogManager == null) {
            dialogManager = new DialogManager(getDatabaseHelper().getDaoByClass(Dialog.class));
        }
        return dialogManager;
    }

    public DialogOccupantManager getDialogOccupantManager() {
        if (dialogOccupantManager == null) {
            dialogOccupantManager = new DialogOccupantManager(getDatabaseHelper().getDaoByClass(
                    DialogOccupant.class), getDatabaseHelper().getDaoByClass(Dialog.class));
        }
        return dialogOccupantManager;
    }

    public DialogNotificationManager getDialogNotificationManager() {
        if (dialogNotificationManager == null) {
            dialogNotificationManager = new DialogNotificationManager(getDatabaseHelper().getDaoByClass(
                    DialogNotification.class), getDatabaseHelper().getDaoByClass(Dialog.class),
                    getDatabaseHelper().getDaoByClass(DialogOccupant.class));
        }
        return dialogNotificationManager;
    }

    public AttachmentManager getAttachmentManager() {
        if (attachmentManager == null) {
            attachmentManager = new AttachmentManager(getDatabaseHelper().getDaoByClass(Attachment.class));
        }
        return attachmentManager;
    }

    public MessageManager getMessageManager() {
        if (messageManager == null) {
            messageManager = new MessageManager(getDatabaseHelper().getDaoByClass(Message.class),
                    getDatabaseHelper().getDaoByClass(Dialog.class), getDatabaseHelper().getDaoByClass(
                    DialogOccupant.class));
        }
        return messageManager;
    }
}