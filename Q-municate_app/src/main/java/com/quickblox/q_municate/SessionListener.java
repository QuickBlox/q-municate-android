package com.quickblox.q_municate;


import android.os.Bundle;
import android.util.Log;

import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsOAuthSigning;
import com.digits.sdk.android.DigitsSession;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.session.QBSessionListenerImpl;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.auth.session.QBSessionParameters;
import com.quickblox.q_municate.service.SessionJobService;
import com.quickblox.q_municate.utils.helpers.TwitterDigitsHelper;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.users.model.QBUser;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.Map;

public class SessionListener {

    private static final String TAG = SessionListener.class.getSimpleName();

    private final QBSessionListener listener;

    public SessionListener(){
        listener = new QBSessionListener();
        QBSessionManager.getInstance().addListener(listener);
    }

    private static class QBSessionListener extends QBSessionListenerImpl {

        @Override
        public void onSessionUpdated(QBSessionParameters sessionParameters) {
            Log.d(TAG, "onSessionUpdated pswd:" + sessionParameters.getUserPassword()
                    + ", iserId : " + sessionParameters.getUserId());
            QBUser qbUser = AppSession.getSession().getUser();
            if (sessionParameters.getSocialProvider() != null) {
                qbUser.setPassword(QBSessionManager.getInstance().getToken());
            } else {
                qbUser.setPassword(sessionParameters.getUserPassword());
            }
            AppSession.getSession().updateUser(qbUser);
        }

        @Override
        public void onProviderSessionExpired(String provider) {
            Log.d(TAG, "onProviderSessionExpired :" +provider );

            if (!QBProvider.TWITTER_DIGITS.equals(provider)){
                return;
            }
            TwitterAuthConfig authConfig = TwitterCore.getInstance().getAuthConfig();
            DigitsSession session = Digits.getActiveSession();
            if (session == null) {
                return;
            }
            TwitterAuthToken authToken = session.getAuthToken();
            DigitsOAuthSigning authSigning = new DigitsOAuthSigning(authConfig, authToken);
            Map<String, String> authHeaders = authSigning.getOAuthEchoHeadersForVerifyCredentials();

            Log.d(TAG, "authHeaders provider = " + authHeaders.get(TwitterDigitsHelper.PROVIDER)
                    + "\n, credentials= " + authHeaders.get(TwitterDigitsHelper.CREDENTIALS));

            Bundle bundle = new Bundle();
            bundle.putString(TwitterDigitsHelper.PROVIDER, authHeaders.get(TwitterDigitsHelper.PROVIDER));
            bundle.putString(TwitterDigitsHelper.CREDENTIALS, authHeaders.get(TwitterDigitsHelper.CREDENTIALS));
            SessionJobService.startSignInSocial(App.getInstance(), bundle);
        }
    }
}
