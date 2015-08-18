package com.quickblox.q_municate.core.listeners;

public interface UserStatusChangingListener {

    void onChangedUserStatus(int userId, boolean online);
}