package com.quickblox.q_municate.ui.activities.authorization;

import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.ui.activities.authorization.BaseAuthActivity;
import com.quickblox.q_municate.utils.helpers.FlurryAnalyticsHelper;
import com.quickblox.q_municate.utils.helpers.GoogleAnalyticsHelper;
import com.quickblox.q_municate_auth_service.QMAuthService;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

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

    public ServiceManager(BaseAuthActivity authActivity){
        this.authActivity = authActivity;
        authService = QMAuthService.getInstance();
        userService = QMUserService.getInstance();
        userCache = userService.getUserCache();
    }

    public void login(QBUser user) {
        final String userPassword = user.getPassword();
        authService.login(user).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        .map(new Func1<QBUser,QMUser>() {
            @Override
            public QMUser call(QBUser qbUser) {
                QMUser result = QMUser.convert(qbUser);
                userCache.createOrUpdate(result);
                return result;
            }
        })
        .subscribe(new Observer<QBUser>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError" + e.getMessage());
            }

            @Override
            public void onNext(QBUser qbUser) {
                String password = userPassword;

                if (!hasUserCustomData(qbUser)) {
                    qbUser.setOldPassword(password);
                    try {
                        updateUser(qbUser);
                    } catch (QBResponseException e) {
                        e.printStackTrace();
                    }
                }

                String token = QBSessionManager.getInstance().getToken();
                qbUser.setPassword(password);

                saveOwnerUser(qbUser);

                AppSession.startSession(qbUser);


                authActivity.startMainActivity(qbUser);

                // send analytics data
                GoogleAnalyticsHelper.pushAnalyticsData(authActivity, qbUser, "User Sign In");
                FlurryAnalyticsHelper.pushAnalyticsData(authActivity);
            }
        });
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
