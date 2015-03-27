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
        return new UserManager(getDatabaseHelper().getDaoByClass(User.class));
    }

    public FriendManager getFriendManager() {
        return new FriendManager(getDatabaseHelper().getDaoByClass(Friend.class));
    }

    public SocialManager getSocialManager() {
        return new SocialManager(getDatabaseHelper().getDaoByClass(Social.class));
    }

    public UserRequestManager getUserRequestManager() {
        return new UserRequestManager(getDatabaseHelper().getDaoByClass(UserRequest.class));
    }

    public DialogManager getDialogManager() {
        return new DialogManager(getDatabaseHelper().getDaoByClass(Dialog.class));
    }

    public DialogOccupantManager getDialogOccupantManager() {
        return new DialogOccupantManager(getDatabaseHelper().getDaoByClass(DialogOccupant.class));
    }

    public DialogNotificationManager getDialogNotificationManager() {
        return new DialogNotificationManager(getDatabaseHelper().getDaoByClass(DialogNotification.class));
    }

    public AttachmentManager getAttachmentManager() {
        return new AttachmentManager(getDatabaseHelper().getDaoByClass(Attachment.class));
    }

    public MessageManager getMessageManager() {
        return new MessageManager(getDatabaseHelper().getDaoByClass(Message.class));
    }
}