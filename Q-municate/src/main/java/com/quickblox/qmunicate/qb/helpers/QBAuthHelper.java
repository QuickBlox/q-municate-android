package com.quickblox.qmunicate.qb.helpers;

import com.facebook.Session;
import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.model.QBSession;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;

import org.jivesoftware.smack.XMPPException;

import java.io.File;

public class QBAuthHelper {

    private String TAG = QBAuthHelper.class.getSimpleName();

    private QBUser user;

    public QBUser login(QBUser user) throws QBResponseException, XMPPException {
        QBAuth.createSession();
        String password = user.getPassword();
        this.user = QBUsers.signIn(user);
        this.user.setPassword(password);
        loginChat(this.user);

        return this.user;
    }

    public QBUser login(String socialProvider, String accessToken,
            String accessTokenSecret) throws QBResponseException, XMPPException {
        QBSession session = QBAuth.createSession();
        user = QBUsers.signInUsingSocialProvider(socialProvider, accessToken, accessTokenSecret);
        user.setPassword(session.getToken());
        loginChat(user);

        return user;
    }

    public QBUser signup(QBUser user, File file) throws QBResponseException, XMPPException {
        QBAuth.createSession();
        this.user = QBUsers.signUpSignInTask(user);
        if (null != file) {
            QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
            this.user.setWebsite(qbFile.getUid());
            this.user = QBUsers.updateUser(user);
        }
        loginChat(this.user);

        return user;
    }

    public void logout() throws QBResponseException {
        Session.getActiveSession().closeAndClearTokenInformation();
        QBAuth.deleteSession();
        QBChatService.getInstance().logout();
    }

    public QBUser updateUser(QBUser user) throws QBResponseException {
        String password = user.getPassword();
        this.user = QBUsers.updateUser(user);
        this.user.setPassword(password);

        return this.user;
    }

    public QBUser updateUser(QBUser user, File file) throws QBResponseException {
        QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
        user.setWebsite(qbFile.getUid());
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

    private void loginChat(QBUser user) throws XMPPException {
        QBChatService.getInstance().loginWithUser(user);
    }
}
