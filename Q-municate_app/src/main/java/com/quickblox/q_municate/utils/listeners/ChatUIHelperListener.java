package com.quickblox.q_municate.utils.listeners;

public interface ChatUIHelperListener {

    void onScrollMessagesToBottom();

    void onScreenResetPossibilityPerformLogout(boolean canPerformLogout);
}