package com.quickblox.q_municate.qb.helpers;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBPresence;
import com.quickblox.module.chat.QBRoster;
import com.quickblox.module.chat.QBRosterEntry;
import com.quickblox.module.chat.listeners.QBRosterListener;
import com.quickblox.module.chat.listeners.QBSubscriptionListener;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.FriendUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
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

    private static final String PRESENCE_CHANGE_ERROR = "Presence change error: could not find friend in DB by id = ";
    private static final String ENTRIES_UPDATING_ERROR = "Failed to update friends list";
    private static final String ENTRIES_ADDED_ERROR = "Failed to add friends to list";
    private static final String SUBSCRIPTION_ERROR = "Failed to confirm subscription";
    private static final String ROSTER_INIT_ERROR = "ROSTER isn't initialized. Please make relogin";

    private static final int FIRST_PAGE = 1;
    // Default value equals 0, bigger value allows to prevent overwriting of presence that contains status
    // with presence that is sent on login by default
    private static final int STATUS_PRESENCE_PRIORITY = 1;

    private QBRoster roster;

    public QBFriendListHelper(Context context) {
        super(context);
    }

    public void init() {
        roster = QBChatService.getInstance().getRoster();
        roster.setSubscriptionMode(QBRoster.SubscriptionMode.mutual);
        roster.addRosterListener(new RosterListener());
        roster.addSubscriptionListener(new SubscriptionListener());
    }

    public void inviteFriend(int userId) throws Exception {
        if (isNotInvited(userId)) {
            sendInvitation(userId);
            addUserToFriendList(userId);
        }
    }

    public void acceptFriend(int userId) throws Exception {
        roster.confirmSubscription(userId);
        updateFriend(userId);
    }

    private boolean isNotInvited(int userId) {
        return !isInvited(userId);
    }

    private boolean isInvited(int userId) {
        QBRosterEntry entry = roster.getEntry(userId);
        if (entry == null) {
            return false;
        }
        boolean isSubscribedToUser = entry.getType() == RosterPacket.ItemType.from;
        boolean isBothSubscribed = entry.getType() == RosterPacket.ItemType.both;
        return isSubscribedToUser || isBothSubscribed;
    }

    private void sendInvitation(int userId) throws Exception {
        if (roster.contains(userId)) {
            roster.subscribe(userId);
        } else {
            roster.createEntry(userId, null);
        }
    }

    private void addUserToFriendList(int userId) throws Exception {
        User user = loadUser(userId);
        Friend friend = FriendUtils.getFriendWithStatus(user.getUserId(), RosterPacket.ItemType.to.name());

        fillUserOnlineStatus(user);

        DatabaseManager.saveUser(context, user);
        DatabaseManager.saveFriend(context, friend);
    }

    private User loadUser(int userId) throws QBResponseException {
        QBUser user = QBUsers.getUser(new QBUser(userId));
        return FriendUtils.createUser(user);
    }

    public void confirmInvitation(
            QBUser user) throws SmackException.NotLoggedInException, SmackException.NoResponseException, SmackException.NotConnectedException, XMPPException {
        roster.confirmSubscription(user.getId());
    }

    public void removeFriend(User friend) throws SmackException.NotConnectedException {
        roster.unsubscribe(friend.getUserId());
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

    private void makeAutoSubscription(Collection<QBRosterEntry> entriesList) throws QBResponseException {
        for (QBRosterEntry rosterEntry : entriesList) {
            if (RosterPacket.ItemType.from.equals(rosterEntry.getType())) {
                boolean errorOccurred = false;
                try {
                    roster.confirmSubscription(rosterEntry.getUserId());
                } catch (SmackException.NotConnectedException e) {
                    errorOccurred = true;
                } catch (SmackException.NotLoggedInException e) {
                    errorOccurred = true;
                } catch (XMPPException e) {
                    errorOccurred = true;
                } catch (SmackException.NoResponseException e) {
                    errorOccurred = true;
                }
                if (errorOccurred) {
                    throw new QBResponseException("Errors occurred while confirm friend subscriptions");
                }
            }
        }
    }

    private void updateFriends(Collection<Integer> userIdsList,
            Collection<QBRosterEntry> rosterEntryCollection) throws QBResponseException {
        List<QBUser> qbUsersList = loadUsers(userIdsList);
        List<User> usersList = FriendUtils.createUsersList(qbUsersList);
        List<Friend> friendsList = FriendUtils.createFriendsList(rosterEntryCollection);

        fillUsersWithRosterData(usersList);

        DatabaseManager.savePeople(context, usersList, friendsList);
    }

    private void updateFriends(Collection<Integer> userIdsList) throws QBResponseException {
        for (Integer userId : userIdsList) {
            updateFriend(userId);
        }
    }

    private void updateFriend(int userId) throws QBResponseException {
        QBRosterEntry rosterEntry = roster.getEntry(userId);
        User user = loadUser(userId);
        Friend friend = FriendUtils.createFriend(rosterEntry);

        fillUserOnlineStatus(user);

        DatabaseManager.saveUser(context, user);
        DatabaseManager.saveFriend(context, friend);
    }

    private void createFriendWithStatusFrom(int userId) throws QBResponseException {
        User user = loadUser(userId);
        Friend friend = FriendUtils.getFriendWithStatus(userId, RosterPacket.ItemType.from.name());

        fillUserOnlineStatus(user);

        DatabaseManager.saveUser(context, user);
        DatabaseManager.saveFriend(context, friend);
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

    private class RosterListener implements QBRosterListener {

        @Override
        public void entriesDeleted(Collection<Integer> userIds) {
        }

        @Override
        public void entriesAdded(Collection<Integer> userIdsList) {
            try {
                updateFriends(userIdsList);
            } catch (QBResponseException e) {
                Log.e(TAG, ENTRIES_ADDED_ERROR, e);
            }
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

    private class SubscriptionListener implements QBSubscriptionListener {

        @Override
        public void subscriptionRequested(int userId) {
            try {
                createFriendWithStatusFrom(userId);
            } catch (QBResponseException e) {
                Log.e(TAG, SUBSCRIPTION_ERROR, e);
            }
        }
    }
}