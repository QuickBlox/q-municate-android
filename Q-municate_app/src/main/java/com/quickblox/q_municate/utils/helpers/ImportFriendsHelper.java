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

public class ImportFriendsHelper {

    public Activity activity;
    private List<InviteFriend> friendsFacebookList;
    private List<InviteFriend> friendsContactsList;
    private int expectedFriendsCallbacks;
    private int realFriendsCallbacks;

    public ImportFriendsHelper(Activity activity) {
        this.activity = activity;
        friendsFacebookList = new ArrayList<>();
        friendsContactsList = new ArrayList<>();
    }

    public void startGetFriendsListTask(boolean isGetFacebookFriends) {
        expectedFriendsCallbacks++;
        friendsContactsList = EmailHelper.getContactsWithEmail(activity);
        if (isGetFacebookFriends) {
            expectedFriendsCallbacks++;
            getFacebookFriendsList();
        }
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
        realFriendsCallbacks++;
        if (realFriendsCallbacks == expectedFriendsCallbacks) {
            realFriendsCallbacks = ConstsCore.ZERO_INT_VALUE;
            QBImportFriendsCommand.start(activity, getIdsList(friendsFacebookList), getIdsList(
                    friendsContactsList));
        }
    }

    private void getFacebookFriendsList() {
        GraphRequest requestFriends = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(JSONArray objects, GraphResponse response) {
                Log.d("ImportFriendsHelper", objects.toString());
                if (objects.length() > 0) {
                    for (int i = 0; i < objects.length(); i ++) {
                        try {
                            JSONObject elementFriend = (JSONObject) objects.get(i);
                            String userId = elementFriend.getString("id");
                            String userName = elementFriend.getString("name");
                            String userLink = elementFriend.getString("link");
                            friendsFacebookList.add(new InviteFriend(userId, userName, userLink,
                                    InviteFriend.VIA_FACEBOOK_TYPE, null, false));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    fiendsReceived();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        requestFriends.setParameters(parameters);
        requestFriends.executeAsync();
    }
}