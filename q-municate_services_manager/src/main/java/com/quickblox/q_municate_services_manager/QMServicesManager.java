package com.quickblox.q_municate_services_manager;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_base_service.QMServiceManagerListener;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class QMServicesManager implements QMServiceManagerListener, QMUserService.QMUserServiceCacheDataSource, QMUserService.QMUserServiceListener {


    @Override
    public QBUser getCurrentUser() {
        return null;
    }

    @Override
    public boolean isAuthorized() {
        return false;
    }

    @Override
    public void handleErrorResponse(QBResponseException exception) {

    }

    @Override
    public void cachedUsers(List<QBUser> users) {

    }

    @Override
    public void didLoadUsersFromCache(QMUserService userService, List<QBUser> users) {

    }

    @Override
    public void didAddUsers(QMUserService userService, List<QBUser> users) {

    }

    @Override
    public void didUpdateUsers(QMUserService userService, List<QBUser> users) {

    }
}
