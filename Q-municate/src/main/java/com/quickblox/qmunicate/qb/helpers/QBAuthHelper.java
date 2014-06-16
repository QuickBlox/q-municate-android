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

import java.io.File;

public class QBAuthHelper extends BaseHelper {

    private String TAG = QBAuthHelper.class.getSimpleName();

    private QBUser user;

    public QBAuthHelper(Context context) {
        super(context);
    }

    public QBUser login(QBUser user) throws QBResponseException {
        try {
            QBAuth.createSession();
            String password = user.getPassword();
            this.user = QBUsers.signIn(user);
            this.user.setPassword(password);
        } catch (QBResponseException exc) {
            throw new QBResponseException(context.getString(R.string.dlg_fail_rest_login));
        }

        return this.user;
    }

    public QBUser login(String socialProvider, String accessToken,
            String accessTokenSecret) throws QBResponseException {
        QBSession session = QBAuth.createSession();
        user = QBUsers.signInUsingSocialProvider(socialProvider, accessToken, accessTokenSecret);
        user.setPassword(session.getToken());

        return user;
    }

    public QBUser signup(QBUser user, File file) throws QBResponseException {
        QBAuth.createSession();
        String password = user.getPassword();
        user.setOldPassword(password);
        this.user = QBUsers.signUpSignInTask(user);
        if (null != file) {
            QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
            this.user.setWebsite(qbFile.getPublicUrl());
            this.user = QBUsers.updateUser(user);
        }
        this.user.setPassword(password);

        return user;
    }

    public void logout() throws QBResponseException {
        Session.getActiveSession().closeAndClearTokenInformation();
        QBAuth.deleteSession();
        user = null;
    }

    public QBUser updateUser(QBUser user) throws QBResponseException {
        if (TextUtils.isEmpty(user.getWebsite())) {
            return user;
        }
        String password = user.getPassword();
        this.user = QBUsers.updateUser(user);
        this.user.setPassword(password);

        return this.user;
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

    public QBUser getUser() {
        return user;
    }

    public boolean isLoggedIn() {
        return user != null;
    }
}