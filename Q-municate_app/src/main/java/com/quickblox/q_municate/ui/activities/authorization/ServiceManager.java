package com.quickblox.q_municate.ui.activities.authorization;

import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.exception.QBResponseException;
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
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;
import com.twitter.sdk.android.core.identity.AuthHandler;

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

    private BaseAuthActivity authActivity;

    private QMAuthService authService;
    private QMUserService userService;
    private QMUserCache userCache;
    private QBAuthHelper authHelper;

    public ServiceManager(BaseAuthActivity authActivity){
        this.authActivity = authActivity;
        authService = QMAuthService.getInstance();
        userService = QMUserService.getInstance();
        userCache = userService.getUserCache();
        authHelper = new QBAuthHelper(authActivity);
    }

    public void login(QBUser user, Observer<QBUser> observer) {
        final String userPassword = user.getPassword();
        authService.login(user).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        .map(new Func1<QBUser,QMUser>() {
            @Override
            public QMUser call(QBUser qbUser) {
                QMUser result = QMUser.convert(qbUser);
                userCache.createOrUpdate(result);

                String password = userPassword;

                if (!hasUserCustomData(qbUser)) {
                    qbUser.setOldPassword(password);
                    try {
                        updateUser(qbUser);
                    } catch (QBResponseException e) {
                        e.printStackTrace();
                    }
                }

                qbUser.setPassword(password);

                saveOwnerUser(qbUser);

                AppSession.startSession(qbUser);

                return result;
            }
        })
        .subscribe(observer);
    }

    public void  login(String socialProvider, final String accessToken, final String accessTokenSecret, Observer<QBUser> observer) {
        authService.login(QBProvider.FACEBOOK, accessToken, accessTokenSecret).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<QBUser, QBUser>() {
                    @Override
                    public QBUser call(QBUser qbUser) {
                        try {
                            authHelper.updateUser(qbUser);
                        } catch (QBResponseException e) {
                            Log.d(TAG, "updateUser " + e.getMessage());
                        }
                        return qbUser;
                    }
                }).subscribe(observer);
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
            user.setPassword(QBAuth.getSession().perform().getToken());
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

}
