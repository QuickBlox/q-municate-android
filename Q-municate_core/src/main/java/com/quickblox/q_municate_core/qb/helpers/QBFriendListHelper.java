package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.DateUtilsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.models.UserRequest;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.packet.RosterPacket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QBFriendListHelper extends BaseHelper implements Serializable {

    private static final String TAG = QBFriendListHelper.class.getSimpleName();
    private static final String PRESENCE_CHANGE_ERROR = "Presence change error: could not find friend in DB by id = ";
    private static final String ENTRIES_UPDATING_ERROR = "Failed to update friends list";
    private static final String ENTRIES_DELETED_ERROR = "Failed to delete friends";
    private static final String SUBSCRIPTION_ERROR = "Failed to confirm subscription";
    private static final String ROSTER_INIT_ERROR = "ROSTER isn't initialized. Please make relogin";

    private static final int FIRST_PAGE = 1;

    private QBRestHelper restHelper;
    private QBRoster roster;
    private QBPrivateChatHelper privateChatHelper;
    private DataManager dataManager;

    public QBFriendListHelper(Context context) {
        super(context);
    }

    public void init(QBPrivateChatHelper privateChatHelper) {
        this.privateChatHelper = privateChatHelper;
        restHelper = new QBRestHelper(context);
        dataManager = DataManager.getInstance();
        roster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual,
                new SubscriptionListener());
        roster.setSubscriptionMode(QBRoster.SubscriptionMode.mutual);
        roster.addRosterListener(new RosterListener());
    }

    public void inviteFriend(int userId) throws Exception {
        if (isNotInvited(userId)) {
            invite(userId);
        }
    }

    public void addFriend(int userId) throws Exception {
        Log.d("friends-logs", "addFriend(), userId = " + userId);
        createUserRequest(userId, UserRequest.RequestStatus.OUTGOING);
        invite(userId);
    }

    public void invite(int userId) throws Exception {
        sendInvitation(userId);

        QBChatMessage chatMessage = ChatNotificationUtils.createNotificationMessageForFriendsRequest(context);
        sendNotificationToFriend(chatMessage, userId);
    }

    private void sendNotificationToFriend(QBChatMessage chatMessage, int userId) throws QBResponseException {
        QBDialog existingPrivateDialog = privateChatHelper.createPrivateDialogIfNotExist(userId,
                chatMessage.getBody());
        privateChatHelper.sendPrivateMessage(chatMessage, userId, existingPrivateDialog.getDialogId());
    }

    public void acceptFriend(int userId) throws Exception {
        roster.confirmSubscription(userId);

        QBChatMessage chatMessage = ChatNotificationUtils.createNotificationMessageForAcceptFriendsRequest(
                context);
        sendNotificationToFriend(chatMessage, userId);
    }

    public void rejectFriend(int userId) throws Exception {
        roster.reject(userId);
        clearRosterEntry(userId);
        deleteFriendOrUserRequest(userId);

        QBChatMessage chatMessage = ChatNotificationUtils
                .createNotificationMessageForRejectFriendsRequest(context);
        sendNotificationToFriend(chatMessage, userId);
    }

    private void clearRosterEntry(int userId) throws Exception {
        QBRosterEntry rosterEntry = roster.getEntry(userId);
        if (rosterEntry != null && roster.contains(userId)) {
            roster.removeEntry(rosterEntry);
        }
    }

    public void removeFriend(int userId) throws Exception {
        roster.unsubscribe(userId);
        clearRosterEntry(userId);
        deleteFriendOrUserRequest(userId);

        QBChatMessage qbChatMessage = ChatNotificationUtils.createNotificationMessageForRemoveFriendsRequest(context);
        qbChatMessage.setRecipientId(userId);
        sendNotificationToFriend(qbChatMessage, userId);
    }

    private boolean isNotInvited(int userId) {
        return !isInvited(userId);
    }

    private boolean isInvited(int userId) {
        QBRosterEntry rosterEntry = roster.getEntry(userId);
        if (rosterEntry == null) {
            return false;
        }
        boolean isSubscribedToUser = rosterEntry.getType() == RosterPacket.ItemType.from;
        boolean isBothSubscribed = rosterEntry.getType() == RosterPacket.ItemType.both;
        return isSubscribedToUser || isBothSubscribed;
    }

    private void sendInvitation(int userId) throws Exception {
        if (roster.contains(userId)) {
            roster.subscribe(userId);
        } else {
            roster.createEntry(userId, null);
        }
    }

    public Collection<Integer> updateFriendList() throws QBResponseException {
        Collection<Integer> userIdsList = new ArrayList<>();

        if (roster != null) {
            if (!roster.getEntries().isEmpty()) {
                userIdsList = createFriendList(roster.getEntries());
                updateFriends(userIdsList);
            }
        } else {
            ErrorUtils.logError(TAG, ROSTER_INIT_ERROR);
        }

        return userIdsList;
    }

    private Collection<Integer> createFriendList(
            Collection<QBRosterEntry> rosterEntryCollection) throws QBResponseException {
        Collection<Integer> friendList = new ArrayList<>();
        Collection<Integer> userList = new ArrayList<>();

        for (QBRosterEntry rosterEntry : rosterEntryCollection) {
            if (!UserFriendUtils.isOutgoingFriend(rosterEntry) && !UserFriendUtils.isNoneFriend(rosterEntry)) {
                friendList.add(rosterEntry.getUserId());
            }
            if (UserFriendUtils.isOutgoingFriend(rosterEntry)) {
                userList.add(rosterEntry.getUserId());
            }
        }

        if (!userList.isEmpty()) {
            List<QBUser> loadedUserList = loadUsers(userList);
            for (QBUser user : loadedUserList) {
                createUserRequest(user.getId(), UserRequest.RequestStatus.OUTGOING);
            }
        }

        return friendList;
    }

    private void updateFriends(Collection<Integer> friendIdsList) throws QBResponseException {
        List<QBUser> usersList = loadUsers(friendIdsList);

        saveUsersAndFriends(usersList);
    }

    private void updateUsersAndFriends(Collection<Integer> idsList) throws QBResponseException {
        for (Integer userId : idsList) {
            updateFriend(userId);
        }
    }

    private void updateFriend(int userId) throws QBResponseException {
        QBRosterEntry rosterEntry = roster.getEntry(userId);

        User newUser = restHelper.loadUser(userId);

        Log.d("friends-logs", "updateFriend(), userId = " + userId);

        if (newUser == null) {
            return;
        }

        saveUser(newUser);

        Log.d("friends-logs", "updateFriend. UserFriendUtils.isOutgoingFriend(rosterEntry) = " + UserFriendUtils.isOutgoingFriend(rosterEntry));

        boolean outgoingUserRequest = UserFriendUtils.isOutgoingFriend(rosterEntry);

//        if (dataManager.getUserRequestDataManager().existsByUserId(userId) && outgoingUserRequest) {
//            Log.d("friends-logs", "updateFriend. 0");
//            return;
//        }

        if (outgoingUserRequest) {
            Log.d("friends-logs", "updateFriend. 1");
            createUserRequest(userId, UserRequest.RequestStatus.OUTGOING);
        } else {
            Log.d("friends-logs", "updateFriend. 2");
            saveFriend(newUser);
            deleteUserRequestByUser(newUser.getUserId());
        }
    }

    private void deleteUserRequestByUser(int userId) {
        dataManager.getUserRequestDataManager().deleteByUserId(userId);
    }

    private List<QBUser> loadUsers(Collection<Integer> userIds) throws QBResponseException {
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(FIRST_PAGE);
        requestBuilder.setPerPage(userIds.size());

        Bundle params = new Bundle();
        return QBUsers.getUsersByIDs(userIds, requestBuilder, params);
    }

    private boolean isUserOnline(QBPresence presence) {
        return QBPresence.Type.online.equals(presence.getType());
    }

    public boolean isUserOnline(int userId) {
        return roster != null
                && roster.getPresence(userId) != null
                && isUserOnline(roster.getPresence(userId));
    }

    private void saveUser(User user) {
        dataManager.getUserDataManager().createOrUpdate(user);
    }

    private void saveUsersAndFriends(Collection<QBUser> usersCollection) {
        for (QBUser qbUser : usersCollection) {
            User user = UserFriendUtils.createLocalUser(qbUser, User.Role.SIMPLE_ROLE);
            dataManager.getUserDataManager().createOrUpdate(user);
            dataManager.getFriendDataManager().createOrUpdate(new Friend(user));
        }
    }

    private void saveFriend(User user) {
        dataManager.getFriendDataManager().createOrUpdate(new Friend(user));
    }

    private void deleteFriend(int userId) {
        Log.d("friends-logs", "deleteFriend(), userId = " + userId);
        dataManager.getFriendDataManager().deleteByUserId(userId);
    }

    private void deleteFriendOrUserRequest(int id) {
        boolean isFriend = dataManager.getFriendDataManager().getByUserId(id) != null;
        boolean isPendingFriend = dataManager.getUserRequestDataManager().existsByUserId(id);

        if (isFriend) {
            deleteFriend(id);
        } else if (isPendingFriend) {
            deleteUserRequestByUser(id);
        }
    }

    private void deleteFriends(Collection<Integer> userIdsList) throws QBResponseException {
        for (Integer userId : userIdsList) {
            deleteFriend(userId);
        }
    }

    private void createUserRequest(int userId, UserRequest.RequestStatus requestStatus) {
        User user = restHelper.loadUser(userId);

        if (user == null) {
            return;
        } else {
            saveUser(user);
        }

        Log.d("friends-logs", "createUserRequest(), userId = " + userId + ", fullName = " + user.getFullName());

        long currentTime = DateUtilsCore.getCurrentTime();
        UserRequest userRequest = new UserRequest(currentTime, null, requestStatus, user);
        dataManager.getUserRequestDataManager().createOrUpdate(userRequest);
    }

    private void notifyContactRequest(int userId) {
        Intent intent = new Intent(QBServiceConsts.GOT_CONTACT_REQUEST);

        intent.putExtra(QBServiceConsts.EXTRA_MESSAGE, context.getResources().getString(
                R.string.frl_friends_contact_request));
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void notifyUserStatusChanged(int userId) {
        Intent intent = new Intent(QBServiceConsts.USER_STATUS_CHANGED_ACTION);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);
        intent.putExtra(QBServiceConsts.EXTRA_USER_STATUS, isUserOnline(userId));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private class RosterListener implements QBRosterListener {

        @Override
        public void entriesDeleted(Collection<Integer> userIdsList) {
            try {
                deleteFriends(userIdsList);
            } catch (QBResponseException e) {
                Log.e(TAG, ENTRIES_DELETED_ERROR, e);
            }
        }

        @Override
        public void entriesAdded(Collection<Integer> userIdsList) {
        }

        @Override
        public void entriesUpdated(Collection<Integer> idsList) {
            try {
                updateUsersAndFriends(idsList);
            } catch (QBResponseException e) {
                Log.e(TAG, ENTRIES_UPDATING_ERROR, e);
            }
        }

        @Override
        public void presenceChanged(QBPresence presence) {
            User user = dataManager.getUserDataManager().get(presence.getUserId());
            if (user == null) {
                ErrorUtils.logError(TAG, PRESENCE_CHANGE_ERROR + presence.getUserId());
            } else {
                notifyUserStatusChanged(user.getUserId());
            }
        }
    }

    private class SubscriptionListener implements QBSubscriptionListener {

        @Override
        public void subscriptionRequested(int userId) {
            try {
                createUserRequest(userId, UserRequest.RequestStatus.INCOMING);
                notifyContactRequest(userId);
            } catch (Exception e) {
                Log.e(TAG, SUBSCRIPTION_ERROR, e);
            }
        }
    }
}