package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.text.TextUtils;

import com.facebook.login.LoginManager;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.q_municate_auth_service.QMAuthService;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_db.managers.DataManager;
//import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
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
        String password = inputUser.getPassword();
        qbUser = QMAuthService.getInstance().loginSync(inputUser);

        if (!hasUserCustomData(qbUser)) {
            qbUser.setOldPassword(password);
            updateUser(qbUser);
        }

        String token = QBSessionManager.getInstance().getToken();
        qbUser.setPassword(password);

        saveOwnerUser(qbUser);

        AppSession.startSession(LoginType.EMAIL, qbUser, token);

        return qbUser;
    }

    private void saveOwnerUser(QBUser qbUser) {
        QMUser user = UserFriendUtils.createLocalUser(qbUser);
        QMUserService.getInstance().getUserCache().createOrUpdate(user);
    }

    public QBUser login(String socialProvider, String accessToken,
            String accessTokenSecret) throws QBResponseException, BaseServiceException {
        QBUser qbUser;
        qbUser = QMAuthService.getInstance().loginSync(socialProvider, accessToken, accessTokenSecret);

        if (socialProvider.equals(QBProvider.TWITTER_DIGITS)){
            //qbUser = QBUsers.signInUsingTwitterDigits(accessToken, accessTokenSecret).perform();
            CoreSharedHelper.getInstance().saveTDServiceProvider(accessToken);
            CoreSharedHelper.getInstance().saveTDCredentials(accessTokenSecret);
        } else {
            //qbUser = QBUsers.signInUsingSocialProvider(socialProvider, accessToken, accessTokenSecret).perform();
            CoreSharedHelper.getInstance().saveFBToken(accessToken);
        }

        QBSession session = QBSessionManager.getInstance().getActiveSession();

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

        String password = inputUser.getPassword();
        inputUser.setOldPassword(password);
        inputUser.setCustomData(Utils.customDataToString(userCustomData));

        StringifyArrayList<String> stringifyArrayList = new StringifyArrayList<String>();
        stringifyArrayList.add(TAG_ANDROID);
        inputUser.setTags(stringifyArrayList);

        qbUser = QMAuthService.getInstance().signUpLoginSync(inputUser);

        if (file != null) {
            QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null).perform();
            userCustomData.setAvatarUrl(qbFile.getPublicUrl());
            inputUser.setCustomData(Utils.customDataToString(userCustomData));
            qbUser = QBUsers.updateUser(inputUser).perform();
        }

        qbUser.setCustomDataClass(UserCustomData.class);
        qbUser.setPassword(password);
        String token = QBSessionManager.getInstance().getToken();

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
        QMAuthService.getInstance().logoutSync();
    }

    public QBUser updateUser(QBUser inputUser) throws QBResponseException {
        QBUser user;

        String password = inputUser.getPassword();

        UserCustomData userCustomDataNew = getUserCustomData(inputUser);
        inputUser.setCustomData(Utils.customDataToString(userCustomDataNew));

        inputUser.setPassword(null);
        inputUser.setOldPassword(null);

        QMUser qmUser = QMUser.convert(inputUser);
        user = QMUserService.getInstance().updateUserSync(qmUser);

        if (LoginType.EMAIL.equals(AppSession.getSession().getLoginType())) {
            user.setPassword(password);
        } else {
            user.setPassword(QBAuth.getSession().perform().getToken());
        }

        return user;
    }

    public QBUser updateUser(QBUser user, File file) throws QBResponseException {
        QBUser newUser = new QBUser();

        QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null).perform();
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
        QMAuthService.getInstance().resetPasswordSync(email);

    }

    public QBUser changePasswordUser(QBUser inputUser) throws QBResponseException {
        QBUser user;
        String password = inputUser.getPassword();
        QMUser qmUser = QMUser.convert(inputUser);
        user = QMUserService.getInstance().updateUserSync(qmUser);
        user.setPassword(password);

        return user;
    }
}