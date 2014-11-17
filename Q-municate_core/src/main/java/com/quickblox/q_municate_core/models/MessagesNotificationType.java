package com.quickblox.q_municate_core.models;

public enum MessagesNotificationType {
    FRIENDS_REQUEST(4), FRIENDS_ACCEPT(5), FRIENDS_REJECT(6), FRIENDS_REMOVE(7),
    CREATE_DIALOG(8), ADDED_DIALOG(9), NAME_DIALOG(10), PHOTO_DIALOG(11), LEAVE_DIALOG(12);

    private int code;

    MessagesNotificationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MessagesNotificationType parseByCode(int code) {
        MessagesNotificationType[] values = MessagesNotificationType.values();
        MessagesNotificationType result = null;
        for (MessagesNotificationType value : values) {
            if (value.getCode() == code) {
                result = value;
                break;
            }
        }
        return result;
    }
}