package com.quickblox.q_municate.core.listeners;

public interface FriendOperationListener {

    void onAcceptUserClicked(int position, int userId);

    void onRejectUserClicked(int position, int userId);
}