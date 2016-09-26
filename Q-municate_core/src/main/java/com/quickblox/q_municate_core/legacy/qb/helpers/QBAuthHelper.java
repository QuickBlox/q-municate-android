package com.quickblox.q_municate_core.legacy.qb.helpers;

import android.content.Context;
import android.text.TextUtils;

import com.facebook.login.LoginManager;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.model.QBSession;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.q_municate_core.legacy.models.AppSession;
import com.quickblox.q_municate_core.legacy.models.LoginType;
import com.quickblox.q_municate_core.legacy.models.UserCustomData;
import com.quickblox.q_municate_core.legacy.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_core.legacy.utils.UserFriendUtils;
import com.quickblox.q_municate_core.legacy.utils.Utils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.File;

public class QBAuthHelper extends BaseHelper {

    private static final String TAG_ANDROID = "android";

    public QBAuthHelper(Context context) {
        super(context);
    }

    public QBUser login(QBUser inputUser) throws QBResponseException, BaseServiceException {
        QBUser qbUser;
        QBAuth.createSession();
        String password = inputUser.getPassword();
        qbUser = QBUsers.signIn(inputUser);

        if (!hasUserCustomData(qbUser)) {
            qbUser.setOldPassword(password);
            updateUser(qbUser);
        }

        String token = QBAuth.getBaseService().getToken();
        qbUser.setPassword(password);

        saveOwnerUser(qbUser);

        AppSession.startSession(LoginType.EMAIL, qbUser, token);

        return qbUser;
    }

    private void saveOwnerUser(QBUser qbUser) {
        User user = UserFriendUtils.createLocalUser(qbUser, User.Role.OWNER);
        DataManager.getInstance().getUserDataManager().createOrUpdate(user);
    }

    public QBUser login(String socialProvider, String accessToken,
            String accessTokenSecret) throws QBResponseException, BaseServiceException {
        QBUser qbUser;
        QBSession session = QBAuth.createSession();

        if (socialProvider.equals(QBProvider.TWITTER_DIGITS)){
            qbUser = QBUsers.signInUsingTwitterDigits(accessToken, accessTokenSecret);
            CoreSharedHelper.getInstance().saveTDServiceProvider(accessToken);
            CoreSharedHelper.getInstance().saveTDCredentials(accessTokenSecret);
        } else {
            qbUser = QBUsers.signInUsingSocialProvider(socialProvider, accessToken, accessTokenSecret);
            CoreSharedHelper.getInstance().saveFBToken(accessToken);
        }

        qbUser.setPassword(session.getToken());

        if (!hasUserCustomData(qbUser)) {
            qbUser.setOldPassword(session.getToken());
            qbUser = updateUser(qbUser);
        }

        qbUser.setPassword(session.getToken());
        String qbToken = QBAuth.getBaseService().getToken();

        saveOwnerUser(qbUser);

        AppSession.startSession(socialProvider.equals(QBProvider.FACEBOOK)
                ? LoginType.FACEBOOK
                : LoginType.TWITTER_DIGITS,
                qbUser, qbToken);

        return qbUser;
    }

    public QBUser signup(QBUser inputUser, File file) throws QBResponseException, BaseServiceException {
        QBUser qbUser;
        UserCustomData userCustomData = new UserCustomData();

        QBAuth.createSession();
        String password = inputUser.getPassword();
        inputUser.setOldPassword(password);
        inputUser.setCustomData(Utils.customDataToString(userCustomData));

        StringifyArrayList<String> stringifyArrayList = new StringifyArrayList<String>();
        stringifyArrayList.add(TAG_ANDROID);
        inputUser.setTags(stringifyArrayList);

        qbUser = QBUsers.signUpSignInTask(inputUser);

        if (file != null) {
            QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
            userCustomData.setAvatarUrl(qbFile.getPublicUrl());
            inputUser.setCustomData(Utils.customDataToString(userCustomData));
            qbUser = QBUsers.updateUser(inputUser);
        }

        qbUser.setCustomDataClass(UserCustomData.class);
        qbUser.setPassword(password);
        String token = QBAuth.getBaseService().getToken();

        saveOwnerUser(qbUser);

        AppSession.startSession(LoginType.EMAIL, qbUser, token);

        return qbUser;
    }

    public void logout() throws QBResponseException {
        AppSession activeSession = AppSession.getSession();
        if (activeSession != null) {
            activeSession.closeAndClear();
        }

        LoginManager.getInstance().logOut();
        QBAuth.deleteSession();
    }

    public QBUser updateUser(QBUser inputUser) throws QBResponseException {
        QBUser user;

        String password = inputUser.getPassword();

        UserCustomData userCustomDataNew = getUserCustomData(inputUser);
        inputUser.setCustomData(Utils.customDataToString(userCustomDataNew));

        inputUser.setPassword(null);
        inputUser.setOldPassword(null);

        user = QBUsers.updateUser(inputUser);

        if (LoginType.EMAIL.equals(AppSession.getSession().getLoginType())) {
            user.setPassword(password);
        } else {
            user.setPassword(QBAuth.getSession().getToken());
        }

        return user;
    }

    public QBUser updateUser(QBUser user, File file) throws QBResponseException {
        QBUser newUser = new QBUser();

        QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
        newUser.setId(user.getId());
        newUser.setPassword(user.getPassword());
        newUser.setFileId(qbFile.getId());
        newUser.setFullName(user.getFullName());

        UserCustomData userCustomData = getUserCustomData(user);
        userCustomData.setAvatarUrl(qbFile.getPublicUrl());
        newUser.setCustomData(Utils.customDataToString(userCustomData));

        return updateUser(newUser);
    }

    private UserCustomData getUserCustomData(QBUser user) {
        if (TextUtils.isEmpty(user.getCustomData())) {
            return new UserCustomData();
        }

        UserCustomData userCustomData = Utils.customDataToObject(user.getCustomData());

        if (userCustomData != null) {
            return userCustomData;
        } else {
            return new UserCustomData();
        }
    }

    private boolean hasUserCustomData(QBUser user) {
        if (TextUtils.isEmpty(user.getCustomData())) {
            return false;
        }
        UserCustomData userCustomData = Utils.customDataToObject(user.getCustomData());
        return userCustomData != null;
    }

    public void resetPassword(String email) throws QBResponseException {
        QBAuth.createSession();
        QBUsers.resetPassword(email);
    }

    public QBUser changePasswordUser(QBUser inputUser) throws QBResponseException {
        QBUser user;
        String password = inputUser.getPassword();
        user = QBUsers.updateUser(inputUser);
        user.setPassword(password);

        return user;
    }
}