package com.quickblox.q_municate.utils.helpers;

import android.app.Activity;
import android.util.Log;

import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate_core.qb.commands.friend.QBImportFriendsCommand;

import java.util.ArrayList;
import java.util.List;

public class ImportContactsHelper {

    public Activity activity;
    private List<InviteFriend> friendsPhonesList;
    private List<InviteFriend> friendsEmailsList;
    private List<InviteFriend> friendsFacebookList;

    public ImportContactsHelper(Activity activity) {
        this.activity = activity;
        friendsPhonesList = new ArrayList<>();
        friendsEmailsList = new ArrayList<>();
        friendsFacebookList = new ArrayList<>();
    }

    public void startGetFriendsListTask(boolean isGetFacebookFriends) {
        friendsPhonesList.addAll(EmailHelper.getContactsWithPhone(activity));
        friendsEmailsList.addAll(EmailHelper.getContactsWithEmail(activity));
        fiendsReceived();
    }

    private List<String> getIdsList(List<InviteFriend> friendsList) {
        if (friendsList.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> idsList = new ArrayList<>();
        for (InviteFriend friend : friendsList) {
            idsList.add(friend.getId());
        }
        return idsList;
    }

    public void fiendsReceived() {
        QBImportFriendsCommand.start(activity,
                getIdsList(friendsPhonesList),
                getIdsList(friendsEmailsList),
                getIdsList(friendsFacebookList));
    }
}