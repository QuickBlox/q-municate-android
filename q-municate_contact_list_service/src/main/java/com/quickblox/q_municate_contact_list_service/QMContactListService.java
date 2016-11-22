package com.quickblox.q_municate_contact_list_service;

import com.quickblox.chat.QBRoster;
import com.quickblox.core.QBSettings;
import com.quickblox.q_municate_base_service.QMBaseService;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.users.model.QBUser;

public class QMContactListService extends QMBaseService {

    private QBRoster qbContactList;

    public QMContactListService(){
        super();
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

}
