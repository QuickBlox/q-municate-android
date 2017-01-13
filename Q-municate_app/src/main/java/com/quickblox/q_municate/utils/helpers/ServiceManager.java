package com.quickblox.q_municate.utils.helpers;

import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.ui.activities.authorization.BaseAuthActivity;
import com.quickblox.q_municate_auth_service.QMAuthService;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_user_service.QMUserService;
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

    private QMAuthService authService;
    private QMUserService userhService;

    public ServiceManager(){
        authService = QMAuthService.getInstance();
        userhService = QMUserService.getInstance();
    }

//    public void login(QBUser user) {
//        //QBLoginCompositeCommand.start(this, user);
//        authService.login(user).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//        .flatMap(new Func1<QBUser, Observable<QBUser>>() {
//            @Override
//            public Observable<QBUser> call(QBUser qbUser) {
////                userCache.update(qbUser);
////                return observable;
//            }
//        })
//        .subscribe(new Observer<QBUser>() {
//            @Override
//            public void onCompleted() {
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.d(TAG, "onError" + e.getMessage());
//            }
//
//            @Override
//            public void onNext(QBUser qbUser) {
//
//                //String password = qbUser.getPassword();
//                String password = userPassword;
//
//                if (!hasUserCustomData(qbUser)) {
//                    qbUser.setOldPassword(password);
//                    try {
//                        updateUser(qbUser);
//                    } catch (QBResponseException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                String token = QBSessionManager.getInstance().getToken();
//                qbUser.setPassword(password);
//
//                saveOwnerUser(qbUser);
//
//                AppSession.startSession(LoginType.EMAIL, qbUser, token);
//
//                startMainActivity(qbUser);
//
//                // send analytics data
//                GoogleAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this, qbUser, "User Sign In");
//                FlurryAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this);
//            }
//        });
//    }
//
//    private QBUser updateUser(QBUser inputUser) throws QBResponseException {
//        QBUser user;
//
//        String password = inputUser.getPassword();
//
//        UserCustomData userCustomDataNew = getUserCustomData(inputUser);
//        inputUser.setCustomData(Utils.customDataToString(userCustomDataNew));
//
//        inputUser.setPassword(null);
//        inputUser.setOldPassword(null);
//
//        //user = QBUsers.updateUser(inputUser).perform();
//        userService.updateUser(inputUser).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread()). subscribe(new Observer<QBUser>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                // handle errors
//            }
//
//            @Override
//            public void onNext(QBUser qbUser) {
//                //user = qbUser;
//            }
//        });
//
//        user = inputUser;
//
//        if (LoginType.EMAIL.equals(AppSession.getSession().getLoginType())) {
//            user.setPassword(password);
//        } else {
//            user.setPassword(QBAuth.getSession().perform().getToken());
//        }
//
//        return user;
//    }

}
