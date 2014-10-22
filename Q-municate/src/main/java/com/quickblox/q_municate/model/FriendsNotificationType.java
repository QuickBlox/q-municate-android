package com.quickblox.q_municate.model;

public enum FriendsNotificationType {
    REQUEST(1), REJECT(2), REMOVED(3);

    private int code;

    FriendsNotificationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static FriendsNotificationType parseByCode(int code) {
        FriendsNotificationType[] values = FriendsNotificationType.values();
        FriendsNotificationType result = null;
        for (FriendsNotificationType value : values) {
            if (value.getCode() == code) {
                result = value;
                break;
            }
        }
        return result;
    }
}