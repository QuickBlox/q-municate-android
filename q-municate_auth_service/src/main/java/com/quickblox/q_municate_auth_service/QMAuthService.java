package com.quickblox.q_municate_auth_service;


import com.quickblox.auth.model.QBProvider;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.q_municate_base_service.QMBaseService;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;


import rx.Observable;

public class QMAuthService extends QMBaseService {

    private static final String TAG = QMAuthService.class.getSimpleName();

    private static QMAuthService instance;

    private boolean authorized;

    private QMAuthServiceListener listener;

    public static QMAuthService getInstance(){
        return instance;
    }

    public static void init(){
        instance = new QMAuthService();
    }

    @Override
    protected void serviceWillStart() {
    }

    public Observable<QBUser> login(final QBUser user) {
        Performer<QBUser> performer = QBUsers.signIn(user);
        Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        return observable;
    }

    public QBUser loginSync(final QBUser user) throws QBResponseException {
        return QBUsers.signIn(user).perform();
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

    public QBUser loginSync(final String socialProvider, final String accessToken, final String accessTokenSecret) throws QBResponseException {
        Performer<QBUser> performer = null;
        if (socialProvider.equals(QBProvider.TWITTER_DIGITS)){
            performer = QBUsers.signInUsingTwitterDigits(accessToken, accessTokenSecret);
        } else {
            performer = QBUsers.signInUsingSocialProvider(socialProvider, accessToken, accessTokenSecret);
        }
        return performer.perform();
    }

    public Observable<QBUser> signup(final QBUser user){
        Performer<QBUser> performer = QBUsers.signUp(user);
        Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        return observable;
    }

    public Observable<QBUser> signUpLogin(final QBUser user){
        Performer<QBUser> performer = QBUsers.signUpSignInTask(user);
        Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        return observable;
    }

    public QBUser signUpLoginSync(final QBUser user) throws QBResponseException {
        return QBUsers.signUpSignInTask(user).perform();
    }

    public Observable<Void>  logout(){
        Performer<Void> performer = QBUsers.signOut();
        final Observable<Void> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        return observable;
    }

    public void  logoutSync() throws QBResponseException {
        QBUsers.signOut().perform();
    }

    public Observable<Void> resetPassword(String email){
        Performer<Void> performer = QBUsers.resetPassword(email);
        final Observable<Void> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        return observable;
    }

    public void resetPasswordSync(String email) throws QBResponseException {
        QBUsers.resetPassword(email).perform();
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
