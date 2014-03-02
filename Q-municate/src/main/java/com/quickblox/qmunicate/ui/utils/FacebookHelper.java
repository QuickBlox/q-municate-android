package com.quickblox.qmunicate.ui.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;

public class FacebookHelper {
    private Activity activity;
    private Session.StatusCallback facebookStatusCallback;

    public FacebookHelper(Activity activity, Bundle savedInstanceState, Session.StatusCallback facebookStatusCallback) {
        this.activity = activity;
        this.facebookStatusCallback = facebookStatusCallback;
        initFacebook(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Session.getActiveSession().onActivityResult(activity, requestCode, resultCode, data);
    }

    public void loginWithFacebook() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(activity).setCallback(facebookStatusCallback));
        } else {
            Session.openActiveSession(activity, true, facebookStatusCallback);
        }
    }

    public void onActivityStart() {
        Session.getActiveSession().addCallback(facebookStatusCallback);
    }

    public void onActivityStop() {
        Session.getActiveSession().removeCallback(facebookStatusCallback);
    }

    private void initFacebook(Bundle savedInstanceState) {
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(activity, null, facebookStatusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(activity);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(activity).setCallback(facebookStatusCallback));
            }
        }
    }

    public boolean isSessionOpened() {
        return Session.getActiveSession().isOpened();
    }
}
