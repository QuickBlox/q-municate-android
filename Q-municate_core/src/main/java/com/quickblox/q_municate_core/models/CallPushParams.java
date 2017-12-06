package com.quickblox.q_municate_core.models;

import java.io.Serializable;

public class CallPushParams implements Serializable {
    private boolean isPushCall;
    private boolean isNewTask;

    public boolean isPushCall() {
        return isPushCall;
    }

    public void setPushCall(boolean pushCall) {
        isPushCall = pushCall;
    }

    public boolean isNewTask() {
        return isNewTask;
    }

    public void setIsNewTask(boolean newTask) {
        isNewTask = newTask;
    }

    @Override
    public String toString() {
        return "isPushCall = " + isPushCall + ", isNewTask= " + isNewTask;
    }
}