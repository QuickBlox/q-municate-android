package com.quickblox.q_municate_core.models;

public enum MessagesNotificationType {
    FRIENDS_REQUEST(4), FRIENDS_ACCEPT(5), FRIENDS_REJECT(6), FRIENDS_REMOVE(7),
    CREATE_DIALOG(25), ADDED_DIALOG(21), NAME_DIALOG(22), PHOTO_DIALOG(23), LEAVE_DIALOG(24);

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