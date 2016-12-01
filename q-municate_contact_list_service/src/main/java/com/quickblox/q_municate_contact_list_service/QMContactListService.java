package com.quickblox.q_municate_contact_list_service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBContactList;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBContactEntry;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_base_service.QMBaseService;
import com.quickblox.q_municate_contact_list_service.cache.QMContactListCache;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.roster.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;

public class QMContactListService extends QMBaseService {

    private static final String TAG = QMContactListService.class.getSimpleName();
    private static final String PRESENCE_CHANGE_ERROR = "Presence change error: could not find contact in DB by id = ";
    private static final String ENTRIES_CREATING_ERROR = "Failed to create contact list";
    private static final String ENTRIES_UPDATING_ERROR = "Failed to update contact list";
    private static final String ENTRIES_DELETED_ERROR = "Failed to delete contact";
    private static final String SUBSCRIPTION_ERROR = "Failed to confirm subscription";
    private static final String ROSTER_INIT_ERROR = "ROSTER isn't initialized. Please make relogin";

    private Context context;
    private QMContactListCache contactListCache;
    private QBContactList qbContacntList;

    public QMContactListService(QMContactListCache contactListCache) {
        super();
        init(contactListCache);
    }

    private void init(QMContactListCache contactListCache) {
        this.context = QBSettings.getInstance().getContext();
        this.contactListCache = contactListCache;
        qbContacntList = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual,
                new SubscriptionListener());
        qbContacntList.setSubscriptionMode(QBRoster.SubscriptionMode.mutual);
        qbContacntList.addRosterListener(new ContactListListener());
    }

    @Override
    protected void serviceWillStart() {

    }

    public void addUserToContactListRequest(int userId) throws Exception {
        invite(userId);
    }

    public void acceptContactRequest(int userId) throws Exception {
        qbContacntList.confirmSubscription(userId);
    }

    public void rejectContactRequest(int userId) throws Exception {
        qbContacntList.reject(userId);
        clearRosterEntry(userId);
        deleteContactRequest(userId);
    }

    public void removeUserFromContactList(int userId) throws Exception {
        qbContacntList.unsubscribe(userId);
        clearRosterEntry(userId);
        deleteContactRequest(userId);
    }

    public Observable<List<QBContactEntry>> getAllContacts(boolean forceLoad){
        Observable<List<QBContactEntry>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBContactEntry>>>() {
                @Override
                public Observable<List<QBContactEntry>> call() {
                    List<QBContactEntry> qbContacts = contactListCache.getAll();
                    return  qbContacts.size() == 0 ? getAllContacts(true): Observable.just(qbContacts);
                }
            });
            return result;
        }

        result = Observable.defer(new Func0<Observable<List<QBContactEntry>>>() {
            @Override
            public Observable<List<QBContactEntry>> call() {
                List<QBContactEntry> list = new ArrayList<QBContactEntry>(qbContacntList.getEntries());
                return  Observable.just(list);
            }
        });

        return result;
    }

    private void invite(int userId) throws Exception {
        sendInvitation(userId);
    }


    private void clearRosterEntry(int userId) throws Exception {
        QBRosterEntry rosterEntry = qbContacntList.getEntry(userId);
        if (rosterEntry != null && qbContacntList.contains(userId)) {
            qbContacntList.removeEntry(rosterEntry);
        }
    }

    private boolean isInvited(int userId) {
        QBRosterEntry rosterEntry = qbContacntList.getEntry(userId);
        if (rosterEntry == null) {
            return false;
        }
        boolean isSubscribedToUser = rosterEntry.getType() == RosterPacket.ItemType.from;
        boolean isBothSubscribed = rosterEntry.getType() == RosterPacket.ItemType.both;
        return isSubscribedToUser || isBothSubscribed;
    }

    private boolean isNotInvited(int userId) {
        return !isInvited(userId);
    }

    private void sendInvitation(int userId) throws Exception {
        if (qbContacntList.contains(userId)) {
            qbContacntList.subscribe(userId);
        } else {
            qbContacntList.createEntry(userId, null);
        }
    }

    public Collection<Integer> updateContactList() throws QBResponseException {
        Collection<Integer> userIdsList = new ArrayList<>();

        if (qbContacntList != null) {
            if (!qbContacntList.getEntries().isEmpty()) {
                userIdsList = createContactList(qbContacntList.getEntries());
                updateContacts(userIdsList);
            }
        } else {
            ErrorUtils.logError(TAG, ROSTER_INIT_ERROR);
        }

        return userIdsList;
    }

    private Collection<Integer> createContactList(
            Collection<QBRosterEntry> rosterEntryCollection) throws QBResponseException {
        Collection<Integer> contactList = new ArrayList<>();
        Collection<Integer> userList = new ArrayList<>();

        for (QBRosterEntry rosterEntry : rosterEntryCollection) {
            if (!UserFriendUtils.isOutgoingFriend(rosterEntry) && !UserFriendUtils.isNoneFriend(rosterEntry)) {
                contactList.add(rosterEntry.getUserId());
            }
            if (UserFriendUtils.isOutgoingFriend(rosterEntry)) {
                userList.add(rosterEntry.getUserId());
            }
        }
        return contactList;
    }

    private void updateContacts(Collection<Integer> idsList) throws QBResponseException {
        for (Integer userId : idsList) {
            updateContact(userId);
        }
    }

    private void updateContact(int userId) throws QBResponseException {
        QBRosterEntry rosterEntry = qbContacntList.getEntry(userId);
        contactListCache.update(rosterEntry);
    }

    private void addContact(int userId) throws QBResponseException {
        QBRosterEntry rosterEntry = qbContacntList.getEntry(userId);
        contactListCache.createOrUpdate(rosterEntry);
    }

    private void addContacts(Collection<Integer> userIdsList) throws QBResponseException {
        for (Integer userId : userIdsList) {
            addContact(userId);
        }
    }

    private void deleteContact(int userId) throws QBResponseException {
        contactListCache.deleteById(Long.valueOf(userId));
    }

    private void deleteContactRequest(int userId) {
        contactListCache.deleteById(Long.valueOf(userId));
    }

    private void deleteContacts(Collection<Integer> userIdsList) throws QBResponseException {
        for (Integer userId : userIdsList) {
            deleteContact(userId);
        }
    }

    private boolean isUserOnline(QBPresence presence) {
        return QBPresence.Type.online.equals(presence.getType());
    }

    public boolean isUserOnline(int userId) {
        return qbContacntList != null
                && qbContacntList.getPresence(userId) != null
                && isUserOnline(qbContacntList.getPresence(userId));
    }

    private void notifyContactRequest(int userId) {
        Intent intent = new Intent(QBServiceConsts.GOT_CONTACT_REQUEST);

        intent.putExtra(QBServiceConsts.EXTRA_MESSAGE, context.getResources().getString(com.quickblox.q_municate_core.R.string.cht_notification_message));
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void notifyUserStatusChanged(int userId) {
        Intent intent = new Intent(QBServiceConsts.USER_STATUS_CHANGED_ACTION);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);
        intent.putExtra(QBServiceConsts.EXTRA_USER_STATUS, isUserOnline(userId));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    private class ContactListListener implements QBRosterListener {

        @Override
        public void entriesDeleted(Collection<Integer> userIdsList) {
            try {
                deleteContacts(userIdsList);
            } catch (QBResponseException e) {
                Log.e(TAG, ENTRIES_DELETED_ERROR, e);
            }
        }

        @Override
        public void entriesAdded(Collection<Integer> userIdsList) {
            try {
                addContacts(userIdsList);
            } catch (QBResponseException e) {
                Log.e(TAG, ENTRIES_CREATING_ERROR, e);
            }
        }

        @Override
        public void entriesUpdated(Collection<Integer> idsList) {
            try {
                updateContacts(idsList);
            } catch (QBResponseException e) {
                Log.e(TAG, ENTRIES_UPDATING_ERROR, e);
            }
        }

        @Override
        public void presenceChanged(QBPresence presence) {
            notifyUserStatusChanged(presence.getUserId());
        }
    }

    private class SubscriptionListener implements QBSubscriptionListener {

        @Override
        public void subscriptionRequested(int userId) {
            try {
                addContact(userId);
            } catch (QBResponseException e) {
                Log.e(TAG, ENTRIES_CREATING_ERROR, e);
            }
        }
    }

}
