package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate_core.utils.FriendUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;

public class QBImportFriendsCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBImportFriendsCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, List<String> friendsFacebookList,
            List<String> friendsContactsList) {
        Intent intent = new Intent(QBServiceConsts.IMPORT_FRIENDS_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS_FACEBOOK, (java.io.Serializable) friendsFacebookList);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS_CONTACTS, (java.io.Serializable) friendsContactsList);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws QBResponseException {
        ArrayList<String> friendsFacebookList = extras.getStringArrayList(
                QBServiceConsts.EXTRA_FRIENDS_FACEBOOK);
        ArrayList<String> friendsContactsList = extras.getStringArrayList(
                QBServiceConsts.EXTRA_FRIENDS_CONTACTS);

        Bundle params = new Bundle();

        QBPagedRequestBuilder requestBuilder = null;

        List<QBUser> realFriendsFacebookList = null;
        List<QBUser> realFriendsContactsList = null;

        if (!friendsFacebookList.isEmpty()) {
            realFriendsFacebookList = QBUsers.getUsersByFacebookId(friendsFacebookList, requestBuilder,
                    params);
        }

        if (!friendsContactsList.isEmpty()) {
            realFriendsContactsList = QBUsers.getUsersByEmails(friendsContactsList, requestBuilder, params);
        }

        List<Integer> realFriendsList = getSelectedUsersList(realFriendsFacebookList,
                realFriendsContactsList);

        try {
            for (int userId : realFriendsList) {
                friendListHelper.inviteFriend(userId);
            }
        } catch (XMPPException | SmackException e) {
            throw new QBResponseException(e.getLocalizedMessage());
        }

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIENDS, (java.io.Serializable) realFriendsList);

        return result;
    }

    private List<Integer> getSelectedUsersList(List<QBUser> realFriendsFacebookList,
            List<QBUser> realFriendsContactsList) {
        List<Integer> userIdsList = new ArrayList<Integer>();

        if (realFriendsFacebookList != null && !realFriendsFacebookList.isEmpty()) {
            userIdsList.addAll(FriendUtils.getFriendIdsList(realFriendsFacebookList));
        }

        if (realFriendsContactsList != null && !realFriendsContactsList.isEmpty()) {
            userIdsList.addAll(FriendUtils.getFriendIdsList(realFriendsContactsList));
        }

        return userIdsList;
    }
}