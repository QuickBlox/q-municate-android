package com.quickblox.q_municate_core.models;

public enum FriendsNotificationType {
    REQUEST(4), ACCEPT(5), REJECT(6), REMOVE(7);

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