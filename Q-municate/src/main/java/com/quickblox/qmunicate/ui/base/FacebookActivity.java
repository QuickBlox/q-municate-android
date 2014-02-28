package com.quickblox.qmunicate.ui.base;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.qmunicate.qb.QBSocialLoginTask;

public abstract class FacebookActivity extends BaseActivity {

    protected Session.StatusCallback facebookStatusCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFacebook(savedInstanceState);
        facebookStatusCallback = new FacebookSessionStatusCallback();
    }

    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(facebookStatusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(facebookStatusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    private void initFacebook(Bundle savedInstanceState) {
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, facebookStatusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(facebookStatusCallback));
            }
        }
    }

    protected void loginWithFacebook() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(facebookStatusCallback));
        } else {
            Session.openActiveSession(this, true, facebookStatusCallback);
        }
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                new QBSocialLoginTask(FacebookActivity.this).execute(QBProvider.FACEBOOK, session.getAccessToken(), null);
            }
        }
    }
}