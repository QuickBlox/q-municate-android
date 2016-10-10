package com.quickblox.q_municate_auth_service;

import com.facebook.login.LoginManager;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.q_municate_base_service.QMBaseService;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;

import java.io.File;

import rx.Observable;
import rx.functions.Func1;

public class QMAuthService extends QMBaseService {

    private static final String TAG = QMAuthService.class.getSimpleName();

    @Override
    protected void serviceWillStart() {

    }

    public Observable<QBUser> login(final QBUser user) {
        Observable<QBUser> result = null;
        Performer<QBSession> performer = QBAuth.createSession();
        Observable<QBSession> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        result = observable.flatMap(new Func1<QBSession, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBSession qbSession) {
                Performer<QBUser> performerUser = QBUsers.signIn(user);
                final Observable<QBUser> observableUser = performerUser.convertTo(RxJavaPerformProcessor.INSTANCE);
                observableUser.flatMap(new Func1<QBUser, Observable<QBUser>>() {
                    @Override
                    public Observable<QBUser> call(QBUser qbUser) {
                        String token = null;
                        try {
                            token = QBAuth.getBaseService().getToken();
                        } catch (BaseServiceException e) {
                            ErrorUtils.logError(TAG, "login(QBUser)  - " + e.getMessage());
                        }
                        AppSession.startSession(LoginType.EMAIL, qbUser, token);
                        return observableUser;
                    }
                });
                return observableUser;
            }
        });

        return result;
    }

    public Observable<QBUser> login(final String socialProvider, final String accessToken, final String accessTokenSecret){
        Observable<QBUser> result = null;
        Performer<QBSession> performer = QBAuth.createSession();
        Observable<QBSession> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        result = observable.flatMap(new Func1<QBSession, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBSession qbSession) {
                Performer<QBUser> performerUser = null;
                if (socialProvider.equals(QBProvider.TWITTER_DIGITS)){
                    performerUser = QBUsers.signInUsingTwitterDigits(accessToken, accessTokenSecret);
                    CoreSharedHelper.getInstance().saveTDServiceProvider(accessToken);
                    CoreSharedHelper.getInstance().saveTDCredentials(accessTokenSecret);
                } else {
                    performerUser = QBUsers.signInUsingSocialProvider(socialProvider, accessToken, accessTokenSecret);
                    CoreSharedHelper.getInstance().saveFBToken(accessToken);
                }

                final Observable<QBUser> observableUser = performerUser.convertTo(RxJavaPerformProcessor.INSTANCE);
                observableUser.flatMap(new Func1<QBUser, Observable<QBUser>>() {
                    @Override
                    public Observable<QBUser> call(QBUser qbUser) {
                        String token = null;
                        try {
                            token = QBAuth.getBaseService().getToken();
                        } catch (BaseServiceException e) {
                            ErrorUtils.logError(TAG, "login(String, String, String)  - " + e.getMessage());
                        }
                        AppSession.startSession(socialProvider.equals(QBProvider.FACEBOOK) ? LoginType.FACEBOOK : LoginType.TWITTER_DIGITS, qbUser, token);
                        return observableUser;
                    }
                });
                return observableUser;
            }
        });

        return result;
    }

    public Observable<QBUser> signup(final QBUser user){
        Observable<QBUser> result = null;
        Performer<QBSession> performer = QBAuth.createSession();
        Observable<QBSession> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        result = observable.flatMap(new Func1<QBSession, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBSession qbSession) {
                Performer<QBUser> performerUser = QBUsers.signUpSignInTask(user);
                final Observable<QBUser> observableUser = performerUser.convertTo(RxJavaPerformProcessor.INSTANCE);
                observableUser.flatMap(new Func1<QBUser, Observable<QBUser>>() {
                    @Override
                    public Observable<QBUser> call(QBUser qbUser) {
                        String token = null;
                        try {
                            token = QBAuth.getBaseService().getToken();
                        } catch (BaseServiceException e) {
                            ErrorUtils.logError(TAG, "signup(QBUser)  - " + e.getMessage());
                        }
                        AppSession.startSession(LoginType.EMAIL, qbUser, token);
                        return observableUser;
                    }
                });
                return observableUser;
            }
        });

        return result;
    }

    public Observable<Void>  logout(){
        Observable<Void> result = null;
        Performer<Void> performer = QBUsers.signOut();
        final Observable<Void> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        result = observable.flatMap(new Func1<Void, Observable<Void>>() {
            @Override
            public Observable<Void> call(Void qbVoid) {
                Performer<Void> performerSession=  QBAuth.deleteSession();
                final Observable<Void> observableSession = performerSession.convertTo(RxJavaPerformProcessor.INSTANCE);
                observableSession.flatMap(new Func1<Void, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(Void qbVoid) {
                        AppSession.getSession().closeAndClear();
                        return observableSession;
                    }
                });
                return observable;
            }
        });


        return result;
    }

    public interface QMAuthServiceListener{
        void login(QBUser user);
        void logout(QMAuthService authService);

    }



//    - (QB_NONNULL QBRequest *)signUpAndLoginWithUser:(QB_NONNULL QBUUser *)user completion:(void(^QB_NULLABLE_S)(QBResponse *QB_NONNULL_S response, QBUUser *QB_NULLABLE_S userProfile))completion;
//
//    - (QB_NONNULL QBRequest *)logInWithUser:(QB_NONNULL QBUUser *)user completion:(void(^QB_NULLABLE_S)(QBResponse *QB_NONNULL_S response, QBUUser *QB_NULLABLE_S userProfile))completion;
//
//    - (QB_NONNULL QBRequest *)loginWithTwitterDigitsAuthHeaders:(QB_NONNULL NSDictionary *)authHeaders completion:(void(^QB_NULLABLE_S)(QBResponse *QB_NONNULL_S response, QBUUser *QB_NULLABLE_S userProfile))completion;
//
//    - (QB_NONNULL QBRequest *)logInWithFacebookSessionToken:(QB_NONNULL NSString *)sessionToken completion:(void(^QB_NULLABLE_S)(QBResponse *QB_NONNULL_S response, QBUUser *QB_NULLABLE_S userProfile))completion;
//
//    - (QB_NONNULL QBRequest *)loginWithTwitterAccessToken:(QB_NONNULL NSString *)accessToken accessTokenSecret:(QB_NONNULL NSString *)accessTokenSecret completion:(void(^QB_NULLABLE_S)(QBResponse *QB_NONNULL_S response, QBUUser *QB_NULLABLE_S userProfile))completion;
//
//    - (QB_NONNULL QBRequest *)logOut:(void(^QB_NULLABLE_S)(QBResponse *QB_NONNULL_S response))completion;
//
//    - (QB_NONNULL BFTask QB_GENERIC(QBUUser *) *)signUpAndLoginWithUser:(QB_NONNULL QBUUser *)user;
//
//    - (QB_NONNULL BFTask QB_GENERIC(QBUUser *) *)loginWithUser:(QB_NONNULL QBUUser *)user;
//
//    - (QB_NONNULL BFTask QB_GENERIC(QBUUser *) *)loginWithTwitterDigitsAuthHeaders:(QB_NONNULL NSDictionary *)authHeaders;
//
//    - (QB_NONNULL BFTask QB_GENERIC(QBUUser *) *)loginWithFacebookSessionToken:(QB_NONNULL NSString *)sessionToken;
//
//    - (QB_NONNULL BFTask QB_GENERIC(QBUUser *) *)loginWithTwitterAccessToken:(QB_NONNULL NSString *)accessToken accessTokenSecret:(QB_NONNULL NSString *)accessTokenSecret;
//
//    - (QB_NONNULL BFTask *)logout;


//    @protocol QMAuthServiceDelegate <NSObject>
//    @optional
//
///**
// *  It called when auth service did log out
// *
// *  @param authService QMAuthService instance
// */
//    - (void)authServiceDidLogOut:(QB_NONNULL QMAuthService *)authService;
//
///**
// *  It called when auth service did log in with user
// *
// *  @param authService QMAuthService instance
// *  @param user logined QBUUser
// */
//    - (void)authService:(QB_NONNULL QMAuthService *)authService didLoginWithUser:(QB_NONNULL QBUUser *)user;
//
//    @end


}
