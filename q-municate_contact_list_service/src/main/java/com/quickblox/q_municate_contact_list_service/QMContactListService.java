package com.quickblox.q_municate_contact_list_service;

import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBContactList;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_base_service.QMBaseService;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.XMPPConnection;

import java.util.Collection;

public class QMContactListService extends QMBaseService {

    private static final int LOADING_DELAY = 500;

    private static final String TAG = QBFriendListHelper.class.getSimpleName();
    private static final String PRESENCE_CHANGE_ERROR = "Presence change error: could not find friend in DB by id = ";
    private static final String ENTRIES_UPDATING_ERROR = "Failed to update friends list";
    private static final String ENTRIES_DELETED_ERROR = "Failed to delete friends";
    private static final String SUBSCRIPTION_ERROR = "Failed to confirm subscription";
    private static final String ROSTER_INIT_ERROR = "ROSTER isn't initialized. Please make relogin";

    private QBContactList qbContactList;

    public QMContactListService(){
        super();
        qbContactList= QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual,
                new SubscriptionListener());
        qbContactList.setSubscriptionMode(QBRoster.SubscriptionMode.mutual);
        qbContactList.addRosterListener(new RosterListener());
    }

    @Override
    protected void serviceWillStart() {

    }

    /**
     *  Add user to contact list request
     */
    public void addUserToContactListRequest(QBUser user){
    }

    /**
    *  Remove user from contact list request
    */
    public void removeUserFromContactListWithUserID(int userId){

    }

    /**
    *  Accept contact request
    */
    public void acceptContactRequest(int userId){

    }

    /**
     *  Reject contact request
     */
    public void rejectContactRequest(int userId){

    }

    private class RosterListener implements QBRosterListener {

        @Override
        public void entriesDeleted(Collection<Integer> userIdsList) {
//            try {
//                deleteFriends(userIdsList);
//            } catch (QBResponseException e) {
//                Log.e(TAG, ENTRIES_DELETED_ERROR, e);
//            }
        }

        @Override
        public void entriesAdded(Collection<Integer> userIdsList) {
        }

        @Override
        public void entriesUpdated(Collection<Integer> idsList) {
//            try {
//                updateUsersAndFriends(idsList);
//            } catch (QBResponseException e) {
//                Log.e(TAG, ENTRIES_UPDATING_ERROR, e);
//            }
        }

        @Override
        public void presenceChanged(QBPresence presence) {
//            User user = dataManager.getUserDataManager().get(presence.getUserId());
//            if (user == null) {
//                ErrorUtils.logError(TAG, PRESENCE_CHANGE_ERROR + presence.getUserId());
//            } else {
//                notifyUserStatusChanged(user.getUserId());
//            }
        }
    }



    private class SubscriptionListener implements QBSubscriptionListener {

        @Override
        public void subscriptionRequested(int userId) {
//            try {
//                createUserRequest(userId);
//            } catch (Exception e) {
//                Log.e(TAG, SUBSCRIPTION_ERROR, e);
//            }
        }
    }

}
