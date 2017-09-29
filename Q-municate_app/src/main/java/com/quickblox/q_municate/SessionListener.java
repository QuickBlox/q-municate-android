package com.quickblox.q_municate;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.session.QBSessionListenerImpl;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.auth.session.QBSessionParameters;
import com.quickblox.q_municate.service.SessionJobService;
import com.quickblox.q_municate.ui.activities.authorization.LandingActivity;
import com.quickblox.q_municate.utils.helpers.FirebaseAuthHelper;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.users.model.QBUser;

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

            if (QBProvider.FIREBASE_PHONE.equals(provider)
                    || QBProvider.TWITTER_DIGITS.equals(provider)) { //for correct migration from TWITTER_DIGITS to FIREBASE_PHONE

                FirebaseAuthHelper.getIdTokenForCurrentUser(new FirebaseAuthHelper.RequestFirebaseIdTokenCallback() {
                    @Override
                    public void onSuccess(String authToken) {
                        Log.d(TAG, "onSuccess authToken: " + authToken);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAuthHelper.EXTRA_FIREBASE_ACCESS_TOKEN, authToken);
                        SessionJobService.startSignInSocial(App.getInstance(), bundle);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "onError error: " + e.getMessage());
                        Intent intent = new Intent(App.getInstance(), LandingActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        LandingActivity.start(App.getInstance(), intent);
                    }
                });
            }
        }
    }
}
