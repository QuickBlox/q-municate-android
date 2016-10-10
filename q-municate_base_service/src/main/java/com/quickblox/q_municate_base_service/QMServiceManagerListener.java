package com.quickblox.q_municate_base_service;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

public interface QMServiceManagerListener {

    /**
     *  Get user from current session
     *
     *  @return QBUUser instance
     */
    QBUser getCurrentUser();

    /**
     *  Check is current session is authorized
     *
     *  @return true if authorized
     */
    boolean isAuthorized();

}
