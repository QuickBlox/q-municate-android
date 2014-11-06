package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.text.TextUtils;

import com.facebook.Session;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.File;

public class QBAuthHelper extends BaseHelper {

    public QBAuthHelper(Context context) {
        super(context);
    }

    public QBUser login(QBUser inputUser) throws QBResponseException, BaseServiceException {
        QBUser user;
        QBAuth.createSession();
        String password = inputUser.getPassword();
        user = QBUsers.signIn(inputUser);
        user.setCustomDataClass(UserCustomData.class);

        // TODO: temp block
        if (!isUpdatedUserCustomData(user)) {
            user.setOldPassword(password);
            updateUser(user);
        }
        // end todo

        String token = QBAuth.getBaseService().getToken();
        user.setPassword(password);
        AppSession.startSession(LoginType.EMAIL, user, token);
        return user;
    }

    public QBUser login(String socialProvider, String accessToken,
            String accessTokenSecret) throws QBResponseException, BaseServiceException {
        QBUser user;
        QBSession session = QBAuth.createSession();
        user = QBUsers.signInUsingSocialProvider(socialProvider, accessToken, accessTokenSecret);
        user.setPassword(session.getToken());
        user.setCustomDataClass(UserCustomData.class);

        // TODO: temp block
        if (!isUpdatedUserCustomData(user)) {
            user.setOldPassword(session.getToken());
            updateUser(user);
        }
        // end todo

        user.setPassword(session.getToken());
        String token = QBAuth.getBaseService().getToken();
        AppSession.startSession(LoginType.FACEBOOK, user, token);
        return user;
    }

    public QBUser signup(QBUser inputUser, File file) throws QBResponseException, BaseServiceException {
        QBUser user;
        UserCustomData userCustomData = new UserCustomData();

        QBAuth.createSession();
        String password = inputUser.getPassword();
        inputUser.setOldPassword(password);
        inputUser.setCustomDataAsObject(userCustomData);

        user = QBUsers.signUpSignInTask(inputUser);

        if (file != null) {
            QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
            userCustomData.setAvatar_url(qbFile.getPublicUrl());
            inputUser.setCustomDataAsObject(userCustomData);
            user = QBUsers.updateUser(inputUser);
        }

        user.setCustomDataClass(UserCustomData.class);
        user.setPassword(password);
        String token = QBAuth.getBaseService().getToken();
        AppSession.startSession(LoginType.EMAIL, user, token);
        return user;
    }

    public void logout() throws QBResponseException {
        AppSession activeSession = AppSession.getSession();
        if (activeSession != null) {
            activeSession.closeAndClear();
        }
        Session.getActiveSession().closeAndClearTokenInformation();
        QBAuth.deleteSession();
    }

    public QBUser updateUser(QBUser inputUser) throws QBResponseException {
        QBUser user;

        UserCustomData userCustomData = getUserCustomData(inputUser);

        inputUser.setCustomDataAsObject(userCustomData);

        String password = inputUser.getPassword();
        user = QBUsers.updateUser(inputUser);
        user.setCustomDataClass(UserCustomData.class);
        user.setPassword(password);
        return user;
    }

    public QBUser updateUser(QBUser user, File file) throws QBResponseException {
        QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
        user.setWebsite(qbFile.getPublicUrl());
        UserCustomData userCustomData = getUserCustomData(user);
        userCustomData.setAvatar_url(qbFile.getPublicUrl());
        user.setCustomDataAsObject(userCustomData);

        return updateUser(user);
    }

    // TODO: temp method
    private UserCustomData getUserCustomData(QBUser user) {
        if (TextUtils.isEmpty(user.getCustomData())) {
            return new UserCustomData(user.getWebsite(), ConstsCore.EMPTY_STRING, ConstsCore.ZERO_INT_VALUE);
        }

        UserCustomData userCustomDataNew = null;
        UserCustomData userCustomDataOld = null;

        userCustomDataOld = (UserCustomData) user.getCustomDataAsObject();

        if (userCustomDataOld != null) {
            userCustomDataNew = userCustomDataOld;
        } else {
            userCustomDataNew = new UserCustomData();
        }

        if (!TextUtils.isEmpty(user.getWebsite())) {
            userCustomDataNew.setAvatar_url(user.getWebsite());
        }

        return userCustomDataNew;
    }

    // TODO: temp method
    private boolean isUpdatedUserCustomData(QBUser user) {
        if (TextUtils.isEmpty(user.getCustomData())) {
            return false;
        }

        UserCustomData userCustomDataOld = (UserCustomData) user.getCustomDataAsObject();

        if (userCustomDataOld != null) {
            return true;
        } else {
            return false;
        }
    }

    public void resetPassword(String email) throws QBResponseException {
        QBAuth.createSession();
        QBUsers.resetPassword(email);
    }
}