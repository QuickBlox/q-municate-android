package com.quickblox.q_municate.utils.helpers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate_auth_service.QMAuthService;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.File;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by pelipets on 1/6/17.
 */

public class ServiceManager {

    public static final String TAG = "ServiceManager";
    private static final String TAG_ANDROID = "android";

    private static ServiceManager instance;

    private Context context;

    private QMAuthService authService;
    private QMUserService userService;

    public static ServiceManager getInstance(){
        if(instance == null){
            instance = new ServiceManager();
        }
        return instance;
    }

    private ServiceManager(){
        this.context = App.getInstance();
        authService = QMAuthService.getInstance();
        userService = QMUserService.getInstance();
    }

    public Observable<QBUser> login(QBUser user) {
        final String userPassword = user.getPassword();

        Observable<QBUser> result = authService.login(user)
                .subscribeOn(Schedulers.io())
                .map(new Func1<QBUser,QBUser>() {
                    @Override
                    public QBUser call(QBUser qbUser) {
                        CoreSharedHelper.getInstance().saveUsersImportInitialized(true);

                        String password = userPassword;

                        if (!hasUserCustomData(qbUser)) {
                            qbUser.setOldPassword(password);
                            try {
                                updateUserSync(qbUser);
                            } catch (QBResponseException e) {
                                Log.d(TAG, "updateUser " + e.getMessage());
                                throw Exceptions.propagate(e);
                            }
                        }

                        qbUser.setPassword(password);

                        saveOwnerUser(qbUser);

                        AppSession.startSession(qbUser);

                        return qbUser;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        return result;
    }

    public  Observable<QBUser>  login(final String socialProvider, final String accessToken, final String accessTokenSecret) {
        Observable<QBUser> result = authService.login(socialProvider, accessToken, accessTokenSecret).subscribeOn(Schedulers.io())
                .map(new Func1<QBUser, QBUser>() {
                    @Override
                    public QBUser call(QBUser qbUser) {
                        UserCustomData userCustomData = Utils.customDataToObject(qbUser.getCustomData());
                        if (QBProvider.FACEBOOK.equals(socialProvider) && TextUtils.isEmpty(userCustomData.getAvatarUrl())) {
                            //Actions for first login via Facebook
                            CoreSharedHelper.getInstance().saveUsersImportInitialized(false);
                            getFBUserWithAvatar(qbUser);
                        } else if (QBProvider.TWITTER_DIGITS.equals(socialProvider) && TextUtils.isEmpty(qbUser.getFullName())) {
                            //Actions for first login via Twitter Digits
                            CoreSharedHelper.getInstance().saveUsersImportInitialized(false);
                            getTDUserWithFullName(qbUser);
                        }
                        try {
                            updateUserSync(qbUser);
                        } catch (QBResponseException e) {
                            Log.d(TAG, "updateUser " + e.getMessage());
                            throw Exceptions.propagate(e);
                        }

                        qbUser.setPassword(QBSessionManager.getInstance().getToken());

                        saveOwnerUser(qbUser);

                        AppSession.startSession(qbUser);

                        return qbUser;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        return result;
    }

    public Observable<Void>  logout() {
        AppSession.getSession().closeAndClear();

        Observable<Void> result = QMAuthService.getInstance().logout()
                .subscribeOn(Schedulers.io())
                .map(new Func1<Void, Void>() {
                    @Override
                    public Void call(Void aVoid) {
                        clearDataAfterLogOut();
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
        return result;
    }

    public  Observable<Void> resetPassword(String email) {
       return QMAuthService.getInstance().resetPassword(email)
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<QMUser> changePasswordUser(QBUser inputUser)  {
        final String password = inputUser.getPassword();
        QMUser qmUser = QMUser.convert(inputUser);
        Observable<QMUser> result  =   QMUserService.getInstance().updateUser(qmUser)
                .subscribeOn(Schedulers.io())
                .map(new Func1<QBUser,QMUser>() {
                    @Override
                    public QMUser call(QBUser qbUser) {
                        QMUser user = QMUser.convert(qbUser);
                        user.setPassword(password);
                        return user;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        return result;
    }


    public Observable<QMUser>  updateUser(QBUser inputUser)  {
        Observable<QMUser> result = null;
        final String password = inputUser.getPassword();

        UserCustomData userCustomDataNew = getUserCustomData(inputUser);
        inputUser.setCustomData(Utils.customDataToString(userCustomDataNew));

        inputUser.setPassword(null);
        inputUser.setOldPassword(null);
        QMUser qmUser = QMUser.convert(inputUser);
        result =  QMUserService.getInstance().updateUser(qmUser)
                .subscribeOn(Schedulers.io())
                .map(new Func1<QMUser, QMUser>() {
                    @Override
                    public QMUser call(QMUser qmUser) {
                        if (LoginType.EMAIL.equals(AppSession.getSession().getLoginType())) {
                            qmUser.setPassword(password);
                        } else {
                            qmUser.setPassword(QBSessionManager.getInstance().getToken());
                        }
                        return qmUser;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        return result;
    }

    public  Observable<QMUser>  updateUser(final QBUser user, final File file) {

        Observable<QMUser> result = null;

        Performer<QBFile> performer = QBContent.uploadFileTask(file, true, (String) null);
        final Observable<QBFile> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable
                .subscribeOn(Schedulers.io())

                .flatMap(new Func1<QBFile, Observable<QMUser>>() {
                    @Override
                    public Observable<QMUser> call(QBFile qbFile) {
                        QBUser newUser = new QBUser();

                        newUser.setId(user.getId());
                        newUser.setPassword(user.getPassword());
                        newUser.setFileId(qbFile.getId());
                        newUser.setFullName(user.getFullName());

                        UserCustomData userCustomData = getUserCustomData(user);
                        userCustomData.setAvatarUrl(qbFile.getPublicUrl());
                        newUser.setCustomData(Utils.customDataToString(userCustomData));

                        return updateUser(newUser);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        return result;
    }

    public QBUser loginSync(QBUser inputUser) throws QBResponseException, BaseServiceException {
        QBUser qbUser;
        String password = inputUser.getPassword();
        qbUser = QMAuthService.getInstance().loginSync(inputUser);

        if (!hasUserCustomData(qbUser)) {
            qbUser.setOldPassword(password);
            updateUserSync(qbUser);
        }

        qbUser.setPassword(password);

        saveOwnerUser(qbUser);

        AppSession.startSession(qbUser);

        return qbUser;
    }

    public QBUser loginSync(String socialProvider, String accessToken,
                        String accessTokenSecret) throws QBResponseException, BaseServiceException {
        QBUser qbUser;
        qbUser = QMAuthService.getInstance().loginSync(socialProvider, accessToken, accessTokenSecret);

        if (socialProvider.equals(QBProvider.TWITTER_DIGITS)){
            CoreSharedHelper.getInstance().saveTDServiceProvider(accessToken);
            CoreSharedHelper.getInstance().saveTDCredentials(accessTokenSecret);
        } else {
            CoreSharedHelper.getInstance().saveFBToken(accessToken);
        }

        QBSession session = QBSessionManager.getInstance().getActiveSession();

        qbUser.setPassword(session.getToken());

        if (!hasUserCustomData(qbUser)) {
            qbUser.setOldPassword(session.getToken());
            qbUser = updateUserSync(qbUser);
        }

        qbUser.setPassword(session.getToken());
        String qbToken = QBAuth.getBaseService().getToken();

        saveOwnerUser(qbUser);

        AppSession.startSession(qbUser);

        return qbUser;
    }

    public QBUser signupSync(QBUser inputUser, File file) throws QBResponseException, BaseServiceException {
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

        saveOwnerUser(qbUser);

        AppSession.startSession(qbUser);

        return qbUser;
    }

    public void logoutSync() throws QBResponseException {
        AppSession activeSession = AppSession.getSession();
        if (activeSession != null) {
            activeSession.closeAndClear();
        }

        QMAuthService.getInstance().logoutSync();
    }

    public QBUser updateUserSync(QBUser inputUser) throws QBResponseException {
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
            user.setPassword(QBSessionManager.getInstance().getToken());
        }

        return user;
    }

    public QBUser updateUserSync(QBUser user, File file) throws QBResponseException {
        QBUser newUser = new QBUser();

        QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null).perform();
        newUser.setId(user.getId());
        newUser.setPassword(user.getPassword());
        newUser.setFileId(qbFile.getId());
        newUser.setFullName(user.getFullName());

        UserCustomData userCustomData = getUserCustomData(user);
        userCustomData.setAvatarUrl(qbFile.getPublicUrl());
        newUser.setCustomData(Utils.customDataToString(userCustomData));

        return updateUserSync(newUser);
    }


    public void resetPasswordSync(String email) throws QBResponseException {
        QMAuthService.getInstance().resetPasswordSync(email);
    }

    public QBUser changePasswordUserSync(QBUser inputUser) throws QBResponseException {
        QBUser user;
        String password = inputUser.getPassword();
        QMUser qmUser = QMUser.convert(inputUser);
        user = QMUserService.getInstance().updateUserSync(qmUser);
        user.setPassword(password);

        return user;
    }

    private void clearDataAfterLogOut() {
        SubscribeService.unSubscribeFromPushes(context);
        DataManager.getInstance().clearAllTables();
        CoreSharedHelper.getInstance().clearAll();

    }

    private void saveOwnerUser(QBUser qbUser) {
        QMUser user = UserFriendUtils.createLocalUser(qbUser);
        QMUserService.getInstance().getUserCache().createOrUpdate(user);
    }


    private boolean hasUserCustomData(QBUser user) {
        if (TextUtils.isEmpty(user.getCustomData())) {
            return false;
        }
        UserCustomData userCustomData = Utils.customDataToObject(user.getCustomData());
        return userCustomData != null;
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

    private QBUser getFBUserWithAvatar(QBUser user) {
        String avatarUrl = context.getString(com.quickblox.q_municate_core.R.string.url_to_facebook_avatar, user.getFacebookId());
        user.setCustomData(Utils.customDataToString(getUserCustomData(avatarUrl)));
        return user;
    }

    private QBUser getTDUserWithFullName(QBUser user){
        user.setFullName(user.getPhone());
        user.setCustomData(Utils.customDataToString(getUserCustomData(ConstsCore.EMPTY_STRING)));
        return user;
    }

    private UserCustomData getUserCustomData(String avatarUrl) {
        String isImport = "1"; // TODO: temp, first FB or TD login (for correct work need use crossplatform)
        return new UserCustomData(avatarUrl, ConstsCore.EMPTY_STRING, isImport);
    }

}
