package com.quickblox.q_municate_core.qb.commands.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class QBImportFriendsCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBImportFriendsCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, List<InviteFriend> friendsList) {
        Intent intent = new Intent(QBServiceConsts.IMPORT_FRIENDS_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, (java.io.Serializable) friendsList);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        ArrayList<String> friendsPhonesList = null;
        ArrayList<String> friendsEmailsList = null;
        ArrayList<String> friendsFacebookList = null;

        ArrayList<InviteFriend> friendsList = (ArrayList<InviteFriend>) extras.getSerializable(QBServiceConsts.EXTRA_FRIENDS);


        for (InviteFriend inviteFriend : friendsList){
            switch (inviteFriend.getViaLabelType()){
                case InviteFriend.VIA_PHONE_TYPE:
                    if (friendsPhonesList == null){
                        friendsPhonesList = new ArrayList<>();
                    }
                    friendsPhonesList.add(inviteFriend.getId());
                    break;
                case InviteFriend.VIA_EMAIL_TYPE:
                    if (friendsEmailsList == null){
                        friendsEmailsList = new ArrayList<>();
                    }
                    friendsEmailsList.add(inviteFriend.getId());
                    break;
                case InviteFriend.VIA_FACEBOOK_TYPE:
                    if (friendsFacebookList == null){
                        friendsFacebookList = new ArrayList<>();
                    }
                    friendsFacebookList.add(inviteFriend.getId());
            }
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPerPage(ConstsCore.USERS_PER_PAGE);

        List<QBUser> realFriendsPhonesList = null;
        List<QBUser> realFriendsEmailsList = null;
        List<QBUser> realFriendsFacebookList = null;

        if (!friendsPhonesList.isEmpty()) {
            List<QMUser> realQMFriendsPhonesList = QMUserService.getInstance().getUsersByPhoneNumbersSync(friendsPhonesList, requestBuilder, true);
            realFriendsPhonesList = new ArrayList<>(realQMFriendsPhonesList.size());
            for (QMUser user : realQMFriendsPhonesList){
                realFriendsPhonesList.add(user);
            }
        }

        if (!friendsEmailsList.isEmpty()) {
            List<QMUser> realQMFriendsContactsList = QMUserService.getInstance().getUsersByEmailsSync(friendsEmailsList, requestBuilder, true);
            realFriendsEmailsList = new ArrayList<>(realQMFriendsContactsList.size());
            for (QMUser user : realQMFriendsContactsList){
                realFriendsEmailsList.add(user);
            }
        }

        if (!friendsFacebookList.isEmpty()) {
            List<QMUser> realQMFriendsFacebookList=  QMUserService.getInstance().getUsersByFacebookIdSync(friendsFacebookList, requestBuilder, true);
            realFriendsFacebookList = new ArrayList<>(realQMFriendsFacebookList.size());
            for (QMUser user : realQMFriendsFacebookList){
                realFriendsFacebookList.add(user);
            }
        }

        List<QBUser> realQbFriendsList = new ArrayList<>();
        if (realFriendsFacebookList != null) {
            realQbFriendsList.addAll(realFriendsFacebookList);
        }

        if (realFriendsEmailsList != null) {
            realQbFriendsList.addAll(realFriendsEmailsList);
        }

        if (realFriendsPhonesList != null) {
            realQbFriendsList.addAll(realFriendsPhonesList);
        }

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIENDS, (java.io.Serializable) getNotInvitedUsersFromAll(realQbFriendsList));
        return result;
    }

    private List<QBUser> getNotInvitedUsersFromAll(List<QBUser> allUsers){
        return friendListHelper.getNotFriendUsers(allUsers);
    }
}