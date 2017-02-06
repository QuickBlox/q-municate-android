package com.quickblox.q_municate.ui.activities.authorization;

import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.helpers.FlurryAnalyticsHelper;
import com.quickblox.q_municate.utils.helpers.GoogleAnalyticsHelper;
import com.quickblox.q_municate_auth_service.QMAuthService;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import java.util.concurrent.Callable;

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

    public ServiceManager(BaseAuthActivity authActivity) {
        this.authActivity = authActivity;
        authService = QMAuthService.getInstance();
        userService = QMUserService.getInstance();
        userCache = userService.getUserCache();
    }

    public void login(QBUser user) {
        final String userPassword = user.getPassword();
        authService.login(user)
                .map(new Func1<QBUser, QMUser>() {
                    @Override
                    public QMUser call(QBUser qbUser) {
                        QMUser result = QMUser.convert(qbUser);
                        userCache.createOrUpdate(result);
                        return result;
                    }
                })
                .flatMap(new Func1<QBUser, Observable<QBUser>>() {
                    @Override
                    public Observable<QBUser> call(QBUser qbUser) {
                        return updateUserIfNeed(qbUser, userPassword);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<QBUser>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError" + e.getMessage());
                        e.printStackTrace();
                        authActivity.hideProgress();
                        parseExceptionMessage(e.getMessage());
                    }

                    @Override
                    public void onNext(QBUser qbUser) {

                        String token = QBSessionManager.getInstance().getToken();
                        qbUser.setPassword(userPassword);

                        saveOwnerUser(qbUser);

                        AppSession.startSession(qbUser);


                        authActivity.startMainActivity(qbUser);

                        // send analytics data
                        GoogleAnalyticsHelper.pushAnalyticsData(authActivity, qbUser, "User Sign In");
                        FlurryAnalyticsHelper.pushAnalyticsData(authActivity);
                    }
                });
    }

    private Observable<QBUser> updateUserIfNeed(final QBUser inputUser, final String password) {

        return Observable.fromCallable(new Callable<QBUser>() {
            @Override
            public QBUser call() throws Exception {
                QBUser user = inputUser;

                if (!hasUserCustomData(inputUser)) {
                    inputUser.setOldPassword(password);

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
                }
                return user;
            }
        });
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

    private void parseExceptionMessage(String errorMessage) {
        if (errorMessage != null) {
            if (errorMessage.equals(authActivity.getString(R.string.error_bad_timestamp))) {
                errorMessage = authActivity.getString(R.string.error_bad_timestamp_from_app);
            } else if (errorMessage.equals(authActivity.getString(R.string.error_login_or_email_required))) {
                errorMessage = authActivity.getString(R.string.error_login_or_email_required_from_app);
            } else if (errorMessage.equals(authActivity.getString(R.string.error_email_already_taken))
                    && AppSession.getSession().getLoginType().equals(LoginType.FACEBOOK)) {
                errorMessage = authActivity.getString(R.string.error_email_already_taken_from_app);
            } else if (errorMessage.equals(authActivity.getString(R.string.error_unauthorized))) {
                errorMessage = authActivity.getString(R.string.error_unauthorized_from_app);
            }

            ErrorUtils.showError(authActivity, errorMessage);
        }
    }

}