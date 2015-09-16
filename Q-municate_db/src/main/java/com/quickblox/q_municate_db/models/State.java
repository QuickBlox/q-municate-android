package com.quickblox.q_municate_db.models;

public enum State {

    DELIVERED(0),
    READ(1),
    SYNC(2),
    TEMP_LOCAL(3),
    TEMP_LOCAL_UNREAD(4);

    private int code;

    State(int code) {
        this.code = code;
    }

    public static State parseByCode(int code) {
        State[] valuesArray = State.values();
        State result = null;
        for (State value : valuesArray) {
            if (value.getCode() == code) {
                result = value;
                break;
            }
        }
        return result;
    }

    public int getCode() {
        return code;
    }
}