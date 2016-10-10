package com.quickblox.q_municate_services_manager;

import com.quickblox.q_municate_base_service.QMServiceManagerListener;
import com.quickblox.users.model.QBUser;

public class QMServicesManager implements QMServiceManagerListener {


    @Override
    public QBUser getCurrentUser() {
        return null;
    }

    @Override
    public boolean isAuthorized() {
        return false;
    }

}
