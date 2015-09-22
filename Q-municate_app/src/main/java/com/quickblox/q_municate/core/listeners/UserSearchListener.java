package com.quickblox.q_municate.core.listeners;

public interface UserSearchListener {

    void prepareSearch();

    void search(String searchQuery);

    void cancelSearch();
}