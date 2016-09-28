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

    /**
     *  This method called when some QBRequest falling. Use this method for handling errors, like show alert with error.
     *
     *  @param QBResponseException instance.
     */
     void handleErrorResponse(QBResponseException exception);
}
