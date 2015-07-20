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
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.Friend;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.FriendUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QBFriendListHelper extends BaseHelper {

    public static final String RELATION_STATUS_NONE = "none";
    public static final String RELATION_STATUS_TO = "to";
    public static final String RELATION_STATUS_FROM = "from";
    public static final String RELATION_STATUS_BOTH = "both";
    public static final String RELATION_STATUS_REMOVE = "remove";
    public static final String RELATION_STATUS_ALL_USERS = "all_users";
    public static final int VALUE_RELATION_STATUS_ALL_USERS = 10;
    private static final String TAG = QBFriendListHelper.class.getSimpleName();
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
    private QBPrivateChatHelper privateChatHelper;

    public QBFriendListHelper(Context context) {
        super(context);
    }

    public void init(QBPrivateChatHelper privateChatHelper) {
        this.privateChatHelper = privateChatHelper;
        restHelper = new QBRestHelper(context);
        roster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual,
                new SubscriptionListener());
        roster.setSubscriptionMode(QBRoster.SubscriptionMode.mutual);
        roster.addRosterListener(new RosterListener());
    }

    public void inviteFriend(int userId) throws QBResponseException,XMPPException,SmackException {
        if (isNotInvited(userId)) {
            invite(userId);
        }
    }

    public void addFriend(int userId) throws QBResponseException, XMPPException, SmackException {
        if (isNewFriend(userId)) {
            acceptFriend(userId);
        } else {
            createFriend(userId, false);
            invite(userId);
        }
    }

    public void invite(int userId) throws QBResponseException, XMPPException, SmackException {
        sendInvitation(userId);

        QBChatMessage chatMessage = ChatNotificationUtils.createNotificationMessageForFriendsRequest(context);
        sendNotificationToFriend(chatMessage, userId);
    }

    private void sendNotificationToFriend(QBChatMessage chatMessage, int userId) throws QBResponseException {
        QBDialog existingPrivateDialog = privateChatHelper.createPrivateDialogIfNotExist(userId,
                chatMessage.getBody());
        privateChatHelper.sendPrivateMessage(chatMessage, userId, existingPrivateDialog.getDialogId());
    }

    public void acceptFriend(int userId) throws XMPPException, SmackException, QBResponseException {
        roster.confirmSubscription(userId);

        QBChatMessage chatMessage = ChatNotificationUtils.createNotificationMessageForAcceptFriendsRequest(
                context);
        sendNotificationToFriend(chatMessage, userId);
    }

    public void rejectFriend(int userId) throws QBResponseException, SmackException, XMPPException {
        roster.reject(userId);
        clearRosterEntry(userId);
        deleteFriend(userId);

        QBChatMessage chatMessage = ChatNotificationUtils.createNotificationMessageForRejectFriendsRequest(
                context);
        sendNotificationToFriend(chatMessage, userId);
    }

    private void clearRosterEntry(int userId) throws XMPPException, SmackException {
        QBRosterEntry rosterEntry = roster.getEntry(userId);
        if (rosterEntry != null && roster.contains(userId)) {
            roster.removeEntry(rosterEntry);
        }
    }

    public void removeFriend(int userId) throws QBResponseException, SmackException,XMPPException {
        roster.unsubscribe(userId);
        clearRosterEntry(userId);
        deleteFriend(userId);

        QBChatMessage chatMessage = ChatNotificationUtils.createNotificationMessageForRemoveFriendsRequest(
                context);
        sendNotificationToFriend(chatMessage, userId);
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

    private void sendInvitation(int userId) throws XMPPException, SmackException {
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

        User newUser = restHelper.loadUser(userId);

        if (newUser == null) {
            return;
        }

        Friend friend = FriendUtils.createFriend(rosterEntry);

        newUser.setOnline(isFriendOnline(roster.getPresence(userId)));

        saveUser(newUser);
        saveFriend(friend);

        fillUserOnlineStatus(newUser);
    }

    private void createFriend(int userId, boolean isNewFriendStatus) throws QBResponseException {
        User user = restHelper.loadUser(userId);
        Friend friend = FriendUtils.createFriend(userId);
        friend.setNewFriendStatus(isNewFriendStatus);
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
        if (roster != null) {
            QBPresence presence = roster.getPresence(user.getUserId());
            fillUserOnlineStatus(user, presence);
        }
    }

    private void fillUserOnlineStatus(User user, QBPresence presence) {
        if (isFriendOnline(presence)) {
            user.setOnline(true);
        } else {
            user.setOnline(false);
        }
    }

    private boolean isFriendOnline(QBPresence presence) {
        return QBPresence.Type.online.equals(presence.getType());
    }

    //    private void fillFriendStatus(User friend) {
    //        QBPresence presence = roster.getPresence(friend.getUserId());
    //        friend.setStatus(presence.getStatus());
    //    }

    //    public void sendStatus(String status) throws SmackException.NotConnectedException {
    //        QBPresence presence = new QBPresence(QBPresence.Type.online, status, STATUS_PRESENCE_PRIORITY,
    //                QBPresence.Mode.available);
    //        roster.sendPresence(presence);
    //    }

    private void saveUser(User user) {
        UsersDatabaseManager.saveUser(context, user);
    }

    private void savePeople(List<User> usersList, List<Friend> friendsList) {
        UsersDatabaseManager.savePeople(context, usersList, friendsList);
    }

    private void saveFriend(Friend friend) {
        UsersDatabaseManager.saveFriend(context, friend);
    }

    private void deleteFriend(int userId) {
        ChatDatabaseManager.deleteFriendById(context, userId);
    }

    private void deleteFriends(Collection<Integer> userIdsList) throws QBResponseException {
        for (Integer userId : userIdsList) {
            deleteFriend(userId);
        }
    }

    private boolean isNewFriend(int userId) {
        return UsersDatabaseManager.isFriendWithStatusNew(context, userId);
    }

    private void notifyContactRequest(int userId) {
        Intent intent = new Intent(QBServiceConsts.GOT_CONTACT_REQUEST);

        intent.putExtra(QBServiceConsts.EXTRA_MESSAGE, context.getResources().getString(R.string.frl_friends_contact_request));
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);

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
        public void entriesUpdated(Collection<Integer> userIdsList) {
            try {
                updateFriends(userIdsList);
            } catch (QBResponseException e) {
                Log.e(TAG, ENTRIES_UPDATING_ERROR, e);
            }
        }

        @Override
        public void presenceChanged(QBPresence presence) {
            User user = UsersDatabaseManager.getUserById(context, presence.getUserId());
            if (user == null) {
                ErrorUtils.logError(TAG, PRESENCE_CHANGE_ERROR + presence.getUserId());
            } else {
                fillUserOnlineStatus(user, presence);
                UsersDatabaseManager.saveUser(context, user);
            }
        }
    }

    private class SubscriptionListener implements QBSubscriptionListener {

        @Override
        public void subscriptionRequested(int userId) {
            try {
                createFriend(userId, true);
                notifyContactRequest(userId);
            } catch (QBResponseException e) {
                Log.e(TAG, SUBSCRIPTION_ERROR, e);
            } catch (Exception e) {
                Log.e(TAG, SUBSCRIPTION_ERROR, e);
            }
        }
    }
}