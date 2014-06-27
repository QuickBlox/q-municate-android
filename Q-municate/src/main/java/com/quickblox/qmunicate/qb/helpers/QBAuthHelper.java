package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.text.TextUtils;

import com.facebook.Session;
import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.model.QBSession;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.model.LoginType;

import java.io.File;

public class QBAuthHelper extends BaseHelper {

    public QBAuthHelper(Context context) {
        super(context);
    }

    public QBUser login(QBUser inputUser) throws QBResponseException {
        QBUser user;
        try {
            QBAuth.createSession();
            String password = inputUser.getPassword();
            user = QBUsers.signIn(inputUser);
            String token = QBAuth.getBaseService().getToken();
            user.setPassword(password);
            AppSession.startSession(LoginType.EMAIL, user, token);
        } catch (Exception exc) {
            throw new QBResponseException(context.getString(R.string.dlg_fail_rest_login));
        }
        return user;
    }

    public QBUser login(String socialProvider, String accessToken,
            String accessTokenSecret) throws QBResponseException {
        QBUser user;
        try {
            QBSession session = QBAuth.createSession();
            user = QBUsers.signInUsingSocialProvider(socialProvider, accessToken, accessTokenSecret);
            user.setPassword(session.getToken());
            String token = QBAuth.getBaseService().getToken();
            AppSession.startSession(LoginType.FACEBOOK, user, token);
        } catch (Exception exc) {
            throw new QBResponseException(context.getString(R.string.dlg_fail_rest_login));
        }
        return user;
    }

    public QBUser signup(QBUser inputUser, File file) throws QBResponseException {
        QBUser user;
        try {
            QBAuth.createSession();
            String password = inputUser.getPassword();
            inputUser.setOldPassword(password);
            user = QBUsers.signUpSignInTask(inputUser);
            if (null != file) {
                QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
                user.setWebsite(qbFile.getPublicUrl());
                user = QBUsers.updateUser(inputUser);
            }
            user.setPassword(password);
            String token = QBAuth.getBaseService().getToken();
            AppSession.startSession(LoginType.EMAIL, user, token);
        } catch (Exception exc) {
            throw new QBResponseException(context.getString(R.string.dlg_fail_rest_login));
        }
        return inputUser;
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
        if (TextUtils.isEmpty(inputUser.getWebsite())) {
            return inputUser;
        }
        String password = inputUser.getPassword();
        user = QBUsers.updateUser(inputUser);
        user.setPassword(password);
        return user;
    }

    public QBUser updateUser(QBUser user, File file) throws QBResponseException {
        QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
        user.setWebsite(qbFile.getPublicUrl());
        user.setFileId(qbFile.getId());
        return updateUser(user);
    }

    public void resetPassword(String email) throws QBResponseException {
        QBAuth.createSession();
        QBUsers.resetPassword(email);
    }
}