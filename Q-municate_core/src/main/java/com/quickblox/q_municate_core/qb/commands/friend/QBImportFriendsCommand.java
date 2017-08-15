package com.quickblox.q_municate_core.qb.commands.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
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

    public static void start(Context context, List<String> friendsPhonesList,
                             List<String> friendsEmailsList,
                             List<String> friendsFacebookList) {
        Intent intent = new Intent(QBServiceConsts.IMPORT_FRIENDS_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS_PHONES, (java.io.Serializable) friendsPhonesList);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS_EMAILS, (java.io.Serializable) friendsEmailsList);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS_FACEBOOK, (java.io.Serializable) friendsFacebookList);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        ArrayList<String> friendsPhonesList = extras.getStringArrayList(
                QBServiceConsts.EXTRA_FRIENDS_PHONES);
        ArrayList<String> friendsEmailsList = extras.getStringArrayList(
                QBServiceConsts.EXTRA_FRIENDS_EMAILS);
        ArrayList<String> friendsFacebookList = extras.getStringArrayList(
                QBServiceConsts.EXTRA_FRIENDS_FACEBOOK);

        QBPagedRequestBuilder requestBuilder = null;

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
//
//
//        List<Integer> realFriendsList = getSelectedUsersList(realFriendsFacebookList,
//                realFriendsEmailsList);
//
//        for (int userId : realFriendsList) {
//            friendListHelper.inviteFriend(userId);
//        }

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIENDS, (java.io.Serializable) realQbFriendsList);

        return result;
    }

    private List<Integer> getSelectedUsersList(List<QBUser> realFriendsFacebookList,
            List<QBUser> realFriendsContactsList) {
        List<Integer> userIdsList = new ArrayList<Integer>();

        if (realFriendsFacebookList != null && !realFriendsFacebookList.isEmpty()) {
            userIdsList.addAll(UserFriendUtils.getFriendIdsList(realFriendsFacebookList));
        }

        if (realFriendsContactsList != null && !realFriendsContactsList.isEmpty()) {
            userIdsList.addAll(UserFriendUtils.getFriendIdsList(realFriendsContactsList));
        }

        return userIdsList;
    }
}