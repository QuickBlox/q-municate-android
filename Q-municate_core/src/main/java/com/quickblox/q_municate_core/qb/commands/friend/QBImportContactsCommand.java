package com.quickblox.q_municate_core.qb.commands.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.InviteContact;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class QBImportContactsCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBImportContactsCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
                                   String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, List<InviteContact> friendsList) {
        Intent intent = new Intent(QBServiceConsts.IMPORT_FRIENDS_ACTION, null, context, QBService.class);
        intent.putParcelableArrayListExtra(QBServiceConsts.EXTRA_FRIENDS, (ArrayList<? extends Parcelable>) friendsList);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        ArrayList<String> friendsPhonesList = new ArrayList<>();
        ArrayList<String> friendsEmailsList = new ArrayList<>();
        ArrayList<String> friendsFacebookList = new ArrayList<>();

        ArrayList<InviteContact> friendsList = extras.getParcelableArrayList(QBServiceConsts.EXTRA_FRIENDS);


        for (InviteContact inviteContact : friendsList){
            switch (inviteContact.getViaLabelType()){
                case InviteContact.VIA_PHONE_TYPE:
                    friendsPhonesList.add(inviteContact.getId());
                    break;
                case InviteContact.VIA_EMAIL_TYPE:
                    friendsEmailsList.add(inviteContact.getId());
                    break;
                case InviteContact.VIA_FACEBOOK_TYPE:
                    friendsFacebookList.add(inviteContact.getId());
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