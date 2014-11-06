package com.quickblox.q_municate.ui.importfriends;

import android.app.Activity;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate_core.qb.commands.QBImportFriendsCommand;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate.utils.EmailUtils;
import com.quickblox.q_municate.utils.FacebookHelper;

import java.util.ArrayList;
import java.util.List;

public class ImportFriends {

    public Activity activity;
    private FacebookHelper facebookHelper;
    private List<InviteFriend> friendsFacebookList;
    private List<InviteFriend> friendsContactsList;
    private int expectedFriendsCallbacks;
    private int realFriendsCallbacks;

    public ImportFriends(Activity activity, FacebookHelper facebookHelper) {
        this.activity = activity;
        this.facebookHelper = facebookHelper;
        this.facebookHelper.loginWithFacebook();
        friendsFacebookList = new ArrayList<InviteFriend>();
        friendsContactsList = new ArrayList<InviteFriend>();
    }

    public void startGetFriendsListTask(boolean isGetFacebookFriends) {
        expectedFriendsCallbacks++;
        friendsContactsList = EmailUtils.getContactsWithEmail(activity);
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
        Request.newMyFriendsRequest(Session.getActiveSession(), new Request.GraphUserListCallback() {

            @Override
            public void onCompleted(List<GraphUser> users, Response response) {
                for (com.facebook.model.GraphUser user : users) {
                    friendsFacebookList.add(new InviteFriend(user.getId(), user.getName(), user.getLink(),
                            InviteFriend.VIA_FACEBOOK_TYPE, null, false));
                }
                fiendsReceived();
            }
        }).executeAsync();
    }
}