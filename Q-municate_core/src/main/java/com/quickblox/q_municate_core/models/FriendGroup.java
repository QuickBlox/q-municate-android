package com.quickblox.q_municate_core.models;

import java.util.ArrayList;
import java.util.List;

public class FriendGroup {

    public static int GROUP_POSITION_MY_CONTACTS = 0;
    public static int GROUP_POSITION_ALL_USERS = 1;

    private int headerId;
    private String headerName;
    private List<User> userList = new ArrayList<User>();

    public FriendGroup(int headerId, String headerName, List<User> userList) {
        this.headerId = headerId;
        this.headerName = headerName;
        this.userList = userList;
    }

    public FriendGroup(int headerId, String headerName) {
        this.headerId = headerId;
        this.headerName = headerName;
    }

    public int getHeaderId() {
        return headerId;
    }

    public void setHeaderId(int headerId) {
        this.headerId = headerId;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public void addUserList(List<User> userList) {
        this.userList.addAll(userList);
    }

    public void removeFriendsFromList(List<User> friendsList) {
        this.userList.removeAll(friendsList);
    }
}