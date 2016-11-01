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

    private boolean authorized;

    private QMAuthServiceListener listener;

    @Override
    protected void serviceWillStart() {
    }

    public Observable<QBUser> login(final QBUser user) {
        Performer<QBUser> performer = QBUsers.signIn(user);
        Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        return observable;
    }

    public Observable<QBUser> login(final String socialProvider, final String accessToken, final String accessTokenSecret){
        Performer<QBUser> performer = null;
        if (socialProvider.equals(QBProvider.TWITTER_DIGITS)){
            performer = QBUsers.signInUsingTwitterDigits(accessToken, accessTokenSecret);
        } else {
            performer = QBUsers.signInUsingSocialProvider(socialProvider, accessToken, accessTokenSecret);
        }
        Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        return observable;
    }

    public Observable<QBUser> signup(final QBUser user){
        Performer<QBUser> performer = QBUsers.signUpSignInTask(user);
        Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        return observable;
    }

    public Observable<Void>  logout(){
        Performer<Void> performer = QBUsers.signOut();
        final Observable<Void> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        return observable;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public QMAuthServiceListener getListener() {
        return listener;
    }

    public void setListener(QMAuthServiceListener listener) {
        this.listener = listener;
    }

    private void notifyLogin(QBUser user){
        if(listener != null){
            listener.login(user);
        }
    }

    private void notifyLogout(QMAuthService authService){
        if(listener != null){
            listener.logout(authService);
        }
    }

    public interface QMAuthServiceListener{
        void login(QBUser user);
        void logout(QMAuthService authService);

    }

}
