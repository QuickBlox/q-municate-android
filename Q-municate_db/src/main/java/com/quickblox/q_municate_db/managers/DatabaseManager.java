package com.quickblox.q_municate_db.managers;

import android.content.Context;

import com.quickblox.q_municate_db.helpers.DatabaseHelper;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.AttachmentType;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.DialogType;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.Notification;
import com.quickblox.q_municate_db.models.Role;
import com.quickblox.q_municate_db.models.Social;
import com.quickblox.q_municate_db.models.SocialType;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.Status;
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

    public UserManager getUserManager() {
        return new UserManager(getDatabaseHelper().getDaoByClass(User.class));
    }

    public FriendManager getFriendManager() {
        return new FriendManager(getDatabaseHelper().getDaoByClass(Friend.class));
    }

    public RoleManager getRoleManager() {
        return new RoleManager(getDatabaseHelper().getDaoByClass(Role.class));
    }

    public SocialTypeManager getSocialTypeManager() {
        return new SocialTypeManager(getDatabaseHelper().getDaoByClass(SocialType.class));
    }

    public SocialManager getSocialManager() {
        return new SocialManager(getDatabaseHelper().getDaoByClass(Social.class));
    }

    public UserRequestManager getUserRequestManager() {
        return new UserRequestManager(getDatabaseHelper().getDaoByClass(UserRequest.class));
    }

    public StatusManager getStatusManager() {
        return new StatusManager(getDatabaseHelper().getDaoByClass(Status.class));
    }

    public DialogTypeManager getDialogTypeManager() {
        return new DialogTypeManager(getDatabaseHelper().getDaoByClass(DialogType.class));
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

    public NotificationManager getNotificationManager() {
        return new NotificationManager(getDatabaseHelper().getDaoByClass(Notification.class));
    }

    public AttachmentTypeManager getAttachmentTypeManager() {
        return new AttachmentTypeManager(getDatabaseHelper().getDaoByClass(AttachmentType.class));
    }

    public AttachmentManager getAttachmentManager() {
        return new AttachmentManager(getDatabaseHelper().getDaoByClass(Attachment.class));
    }

    public StateManager getStateManager() {
        return new StateManager(getDatabaseHelper().getDaoByClass(State.class));
    }

    public MessageManager getMessageManager() {
        return new MessageManager(getDatabaseHelper().getDaoByClass(Message.class));
    }
}