package com.quickblox.q_municate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBRoster;
import com.quickblox.module.chat.listeners.QBRosterListener;
import com.quickblox.module.chat.listeners.QBSubscriptionListener;
import com.quickblox.module.chat.model.QBPresence;
import com.quickblox.module.chat.model.QBRosterEntry;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.FriendUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QBFriendListHelper extends BaseHelper {

    private static final String TAG = QBFriendListHelper.class.getSimpleName();

    public static final String RELATION_STATUS_NONE = "none";
    public static final String RELATION_STATUS_TO = "to";
    public static final String RELATION_STATUS_FROM = "from";
    public static final String RELATION_STATUS_BOTH = "both";
    public static final String RELATION_STATUS_REMOVE = "remove";
    public static final String RELATION_STATUS_ALL_USERS = "all_users";

    public static final int VALUE_RELATION_STATUS_ALL_USERS = 10;

    private static final String PRESENCE_CHANGE_ERROR = "Presence change error: could not find friend in DB by id = ";
    private static final String ENTRIES_UPDATING_ERROR = "Failed to update friends list";
    private static final String ENTRIES_ADDED_ERROR = "Failed to add friends to list";
    private static final String ENTRIES_DELETED_ERROR = "Failed to delete friends";
    private static final String SUBSCRIPTION_ERROR = "Failed to confirm subscription";
    private static final String ROSTER_INIT_ERROR = "ROSTER isn't initialized. Please make relogin";

    private static final int FIRST_PAGE = 1;
    // Default value equals 0, bigger value allows to prevent overwriting of presence that contains status
    // with presence that is sent on login by default
    private static final int STATUS_PRESENCE_PRIORITY = 1;

    private QBRestHelper restHelper;
    private QBRoster roster;

    public QBFriendListHelper(Context context) {
        super(context);
    }

    public void init() {
        restHelper = new QBRestHelper(context);
        roster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual, new SubscriptionListener());
        roster.setSubscriptionMode(QBRoster.SubscriptionMode.mutual);
        roster.addRosterListener(new RosterListener());
    }

    public void inviteFriend(int userId) throws Exception {
        if (isNotInvited(userId)) {
            sendInvitation(userId);
        }
    }

    public void addFriend(int userId) throws Exception {
        createFriend(userId, false);
        sendInvitation(userId);
    }

    public void acceptFriend(int userId) throws Exception {
        roster.confirmSubscription(userId);
    }

    public void rejectFriend(int userId) throws Exception {
        roster.reject(userId);
        clearRosterEntry(userId);
        deleteUser(userId);
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
        deleteUser(userId);
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

    public List<Integer> updateFriendList() throws QBResponseException {
        Collection<QBRosterEntry> rosterEntryCollection;
        List<Integer> userIdsList = new ArrayList<Integer>();

        if (roster != null) {
            rosterEntryCollection = roster.getEntries();
            if (!rosterEntryCollection.isEmpty()) {
                userIdsList = FriendUtils.getUserIdsFromRoster(rosterEntryCollection);
                updateFriends(userIdsList, rosterEntryCollection);
            }
        } else {
            ErrorUtils.logError(TAG, ROSTER_INIT_ERROR);
        }

        return userIdsList;
    }

    private void updateFriends(Collection<Integer> userIdsList,
            Collection<QBRosterEntry> rosterEntryCollection) throws QBResponseException {
        List<QBUser> qbUsersList = loadUsers(userIdsList);
        List<User> usersList = FriendUtils.createUsersList(qbUsersList);
        List<Friend> friendsList = FriendUtils.createFriendsList(rosterEntryCollection);

        fillUsersWithRosterData(usersList);

        savePeople(usersList, friendsList);
    }

    private void updateFriends(Collection<Integer> userIdsList) throws QBResponseException {
        for (Integer userId : userIdsList) {
            updateFriend(userId);
        }
    }

    private void updateFriend(int userId) throws QBResponseException {
        QBRosterEntry rosterEntry = roster.getEntry(userId);

        User user = restHelper.loadUser(userId);

        if (user == null) {
            return;
        }

        Friend friend = FriendUtils.createFriend(rosterEntry);

        fillUserOnlineStatus(user);

        Friend oldFriend = DatabaseManager.getFriendById(context, friend.getUserId());
        if (oldFriend != null) {
            checkAlertShowing(friend, oldFriend);
        }

        saveUser(user);
        saveFriend(friend);
    }

    private void checkAlertShowing(Friend newFriend, Friend oldFriend) {
        String alertMessage = null;

        String friendName = DatabaseManager.getUserById(context, newFriend.getUserId()).getFullName();

        boolean friendRejectedMe = oldFriend.isAskStatus() && !newFriend.isAskStatus() && newFriend.getRelationStatus().equals(RELATION_STATUS_NONE);
        boolean friendAcceptedMe = oldFriend.isAskStatus() && newFriend.getRelationStatus().equals(RELATION_STATUS_TO);
        boolean friendDeletedMe = (oldFriend.getRelationStatus().equals(RELATION_STATUS_TO) || oldFriend.getRelationStatus().equals(RELATION_STATUS_FROM)
                || oldFriend.getRelationStatus().equals(RELATION_STATUS_BOTH)) && newFriend.getRelationStatus().equals(RELATION_STATUS_NONE);

        if (friendRejectedMe) {
            alertMessage = context.getString(R.string.frl_alrt_reject_friend, friendName);
        } else if (friendAcceptedMe) {
            alertMessage = context.getString(R.string.frl_alrt_accepted_friend, friendName);
        } else if (friendDeletedMe) {
            alertMessage = context.getString(R.string.frl_alrt_deleted_friend, friendName);
        }

        if (alertMessage != null) {
            notifyFriendAlert(alertMessage);
        }
    }

    protected void notifyFriendAlert(String message) {
        Intent intent = new Intent(QBServiceConsts.FRIEND_ALERT_SHOW);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND_ALERT_MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void createFriend(int userId, boolean requestedFriend) throws QBResponseException {
        User user = restHelper.loadUser(userId);
        Friend friend = FriendUtils.createFriend(userId, requestedFriend);
        fillUserOnlineStatus(user);

        saveUser(user);
        saveFriend(friend);
    }

    private List<QBUser> loadUsers(Collection<Integer> userIds) throws QBResponseException {
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(FIRST_PAGE);
        requestBuilder.setPerPage(userIds.size());

        Bundle params = new Bundle();
        return QBUsers.getUsersByIDs(userIds, requestBuilder, params);
    }

    private void fillUsersWithRosterData(List<User> usersList) {
        for (User user : usersList) {
            fillUserOnlineStatus(user);
        }
    }

    private void fillUserOnlineStatus(User user) {
        QBPresence presence = roster.getPresence(user.getUserId());
        fillUserOnlineStatus(user, presence);
    }

    private void fillFriendStatus(User friend) {
        QBPresence presence = roster.getPresence(friend.getUserId());
        friend.setStatus(presence.getStatus());
    }

    private void fillUserOnlineStatus(User user, QBPresence presence) {
        if (QBPresence.Type.online.equals(presence.getType())) {
            user.setOnline(true);
        } else {
            user.setOnline(false);
        }
    }

    public void sendStatus(String status) throws SmackException.NotConnectedException {
        QBPresence presence = new QBPresence(QBPresence.Type.online, status, STATUS_PRESENCE_PRIORITY,
                QBPresence.Mode.available);
        roster.sendPresence(presence);
    }

    private void deleteAllFriends() {
        DatabaseManager.deleteAllFriends(context);
    }

    private void saveUser(User user) {
        DatabaseManager.saveUser(context, user);
    }

    private void savePeople(List<User> usersList, List<Friend> friendsList) {
        DatabaseManager.savePeople(context, usersList, friendsList);
    }

    private void saveFriend(Friend friend) {
        DatabaseManager.saveFriend(context, friend);
    }

    private void deleteUser(int userId) {
        DatabaseManager.deleteUserById(context, userId);
    }

    private void deleteUsers(Collection<Integer> userIdsList) throws QBResponseException {
        for (Integer userId : userIdsList) {
            deleteUser(userId);
        }
    }

    private void notifyContactRequest() {
        Intent intent = new Intent(QBServiceConsts.GOT_CONTACT_REQUEST);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private class RosterListener implements QBRosterListener {

        @Override
        public void entriesDeleted(Collection<Integer> userIdsList) {
            try {
                deleteUsers(userIdsList);
            } catch (QBResponseException e) {
                Log.e(TAG, ENTRIES_DELETED_ERROR, e);
            }
        }

        @Override
        public void entriesAdded(Collection<Integer> userIdsList) {
        }

        @Override
        public void entriesUpdated(Collection<Integer> userIdsList) {
            try {
                updateFriends(userIdsList);
            } catch (QBResponseException e) {
                Log.e(TAG, ENTRIES_UPDATING_ERROR, e);
            }
        }

        @Override
        public void presenceChanged(QBPresence presence) {
            User user = DatabaseManager.getUserById(context, presence.getUserId());
            if (user == null) {
                ErrorUtils.logError(TAG, PRESENCE_CHANGE_ERROR + presence.getUserId());
            } else {
                fillUserOnlineStatus(user, presence);
                DatabaseManager.saveUser(context, user);
            }
        }
    }

    private void addedFriends(Collection<Integer> userIdsList) throws QBResponseException {
        for (Integer userId : userIdsList) {
            createFriend(userId, true);
        }
    }

    private class SubscriptionListener implements QBSubscriptionListener {

        @Override
        public void subscriptionRequested(int userId) {
            try {
                createFriend(userId, true);
                notifyContactRequest();
            } catch (QBResponseException e) {
                Log.e(TAG, SUBSCRIPTION_ERROR, e);
            }
        }
    }
}