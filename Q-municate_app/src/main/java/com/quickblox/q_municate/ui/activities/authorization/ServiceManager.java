package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.authorization.BaseAuthActivity;
import com.quickblox.q_municate.utils.helpers.FlurryAnalyticsHelper;
import com.quickblox.q_municate.utils.helpers.GoogleAnalyticsHelper;
import com.quickblox.q_municate_auth_service.QMAuthService;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;
import com.twitter.sdk.android.core.identity.AuthHandler;
import com.twitter.sdk.android.core.internal.TwitterCollection;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by pelipets on 1/6/17.
 */

public class ServiceManager {

    public static final String TAG = "ServiceManager";

    private Context context;

    private QMAuthService authService;
    private QMUserService userService;

    public ServiceManager(){
        this.context = App.getInstance();
        authService = QMAuthService.getInstance();
        userService = QMUserService.getInstance();
    }

    public Observable<QBUser> login(QBUser user) {
        final String userPassword = user.getPassword();

        Observable<QBUser> result = authService.login(user).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        .map(new Func1<QBUser,QBUser>() {
            @Override
            public QBUser call(QBUser qbUser) {
                CoreSharedHelper.getInstance().saveUsersImportInitialized(true);

                String password = userPassword;

                if (!hasUserCustomData(qbUser)) {
                    qbUser.setOldPassword(password);
                    try {
                        updateUser(qbUser);
                    } catch (QBResponseException e) {
                        Log.d(TAG, "updateUser " + e.getMessage());
                    }
                }

                qbUser.setPassword(password);

                saveOwnerUser(qbUser);

                AppSession.startSession(qbUser);

                return qbUser;
            }
        });

        return result;
    }

    public  Observable<QBUser>  login(final String socialProvider, final String accessToken, final String accessTokenSecret) {
        Observable<QBUser> result = authService.login(QBProvider.FACEBOOK, accessToken, accessTokenSecret).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                            updateUser(qbUser);
                        } catch (QBResponseException e) {
                            Log.d(TAG, "updateUser " + e.getMessage());
                        }
                        return qbUser;
                    }
                });

        return result;
    }


    private QBUser updateUser(QBUser inputUser) throws QBResponseException {
        QBUser user;

        String password = inputUser.getPassword();

        UserCustomData userCustomDataNew = getUserCustomData(inputUser);
        inputUser.setCustomData(Utils.customDataToString(userCustomDataNew));

        inputUser.setPassword(null);
        inputUser.setOldPassword(null);

        user = userService.updateUserSync(QMUser.convert(inputUser));


        if (LoginType.EMAIL.equals(AppSession.getSession().getLoginType())) {
            user.setPassword(password);
        } else {
            user.setPassword(QBSessionManager.getInstance().getToken());
        }

        return user;
    }

    private boolean hasUserCustomData(QBUser user) {
        if (TextUtils.isEmpty(user.getCustomData())) {
            return false;
        }
        UserCustomData userCustomData = Utils.customDataToObject(user.getCustomData());
        return userCustomData != null;
    }

    private void saveOwnerUser(QBUser qbUser) {
        QMUser user = QMUser.convert(qbUser);
        QMUserService.getInstance().getUserCache().createOrUpdate(user);
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
