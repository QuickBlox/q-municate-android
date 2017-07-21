package com.quickblox.q_municate.utils.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate_core.qb.commands.friend.QBImportFriendsCommand;
import com.quickblox.q_municate_core.utils.ConstsCore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ImportContactsHelper {

    public Activity activity;
    private List<InviteFriend> friendsFacebookList;
    private List<InviteFriend> friendsContactsList;

    public ImportContactsHelper(Activity activity) {
        this.activity = activity;
        friendsFacebookList = new ArrayList<>();
        friendsContactsList = new ArrayList<>();
    }

    public void startGetFriendsListTask(boolean isGetFacebookFriends) {
        friendsContactsList = EmailHelper.getContactsWithPhone(activity);
        fiendsReceived();
    }

    private List<String> getIdsList(List<InviteFriend> friendsList) {
        if (friendsList.isEmpty()) {
            return new ArrayList<String>();
        }
        List<String> idsList = new ArrayList<String>();
        for (InviteFriend friend : friendsList) {
            idsList.add(friend.getId());
        }
        return idsList;
    }

    public void fiendsReceived() {
        QBImportFriendsCommand.start(activity, getIdsList(friendsFacebookList), getIdsList(
                friendsContactsList));
    }
}