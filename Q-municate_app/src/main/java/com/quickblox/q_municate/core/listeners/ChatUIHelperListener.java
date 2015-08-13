package com.quickblox.q_municate.core.listeners;

public interface ChatUIHelperListener {

    void onScrollMessagesToBottom();

    void onScreenResetPossibilityPerformLogout(boolean canPerformLogout);
}