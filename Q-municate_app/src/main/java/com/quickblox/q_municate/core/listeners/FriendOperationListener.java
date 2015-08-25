package com.quickblox.q_municate.core.listeners;

public interface FriendOperationListener {

    void onAcceptUserClicked(int userId);

    void onRejectUserClicked(int userId);
}