package com.quickblox.q_municate_core.models;

public enum ChatNotificationType {

    CHAT_PHOTO(1),
    CHAT_NAME(2),
    CHAT_OCCUPANTS(3);

    private int value;

    ChatNotificationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ChatNotificationType parseByValue(int value) {
        ChatNotificationType[] prioritiesArray = ChatNotificationType.values();
        ChatNotificationType result = null;
        for (ChatNotificationType type : prioritiesArray) {
            if (type.getValue() == value) {
                result = type;
                break;
            }
        }
        return result;
    }
}