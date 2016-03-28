package com.quickblox.q_municate.utils.helpers;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookServiceException;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.widget.WebDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.util.ArrayList;
import java.util.List;

public class FacebookHelper {

    private static final String PERMISSION_EMAIL = "email";
    private static final String PERMISSION_USER_FRIENDS = "user_friends";
    private static final String PERMISSION_PUBLISH_STREAM = "publish_stream";

    private Activity activity;
    private Session.StatusCallback facebookStatusCallback;
    private Resources resources;

    public FacebookHelper(Activity activity, Bundle savedInstanceState,
            Session.StatusCallback facebookStatusCallback) {
        this.activity = activity;
        this.facebookStatusCallback = facebookStatusCallback;
        resources = activity.getResources();
        initFacebook(savedInstanceState);
    }

    public static void logout() {
        if (Session.getActiveSession() != null) {
            Session.getActiveSession().closeAndClearTokenInformation();
        }
    }

    private void initFacebook(Bundle savedInstanceState) {
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session != null) {
            return;
        }
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

    public void onSaveInstanceState(Bundle outState) {
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Session.getActiveSession().onActivityResult(activity, requestCode, resultCode, data);
    }

    public void loginWithFacebook() {
        Session currentSession = Session.getActiveSession();
        if (currentSession == null || currentSession.getState().isClosed()) {
            Session session = new Session.Builder(activity).build();
            Session.setActiveSession(session);
            currentSession = session;
        }

        if (currentSession.isOpened()) {
            Session.openActiveSession(activity, true, facebookStatusCallback);
        } else {
            Session.OpenRequest openRequest = new Session.OpenRequest(activity);

//            openRequest.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
//            openRequest.setLoginBehavior(SessionLoginBehavior.SSO_ONLY);
            openRequest.setCallback(facebookStatusCallback);

            List<String> permissionsList = generatePermissionsList();
            openRequest.setPermissions(permissionsList);

            Session session = new Session.Builder(activity).build();
            Session.setActiveSession(session);
            session.openForPublish(openRequest);
        }
    }

    private List<String> generatePermissionsList() {
        List<String> permissionsList = new ArrayList<String>();
        permissionsList.add(PERMISSION_EMAIL);
        permissionsList.add(PERMISSION_PUBLISH_STREAM);
        permissionsList.add(PERMISSION_USER_FRIENDS);
        return permissionsList;
    }

    public void onActivityStart() {
        Session.getActiveSession().addCallback(facebookStatusCallback);
    }

    public void onActivityStop() {
        if (Session.getActiveSession() != null) {
            Session.getActiveSession().removeCallback(facebookStatusCallback);
        }
    }

    public boolean isSessionOpened() {
        return Session.getActiveSession().isOpened();
    }

    private Bundle getBundleForFriendsRequest() {
        Bundle postParams = new Bundle();
        postParams.putString(ConstsCore.FB_REQUEST_PARAM_TITLE, resources.getString(R.string.invite_friends_subject_of_invitation));
        postParams.putString(ConstsCore.FB_REQUEST_PARAM_MESSAGE, resources.getString(R.string.invite_friends_body_of_invitation));
        return postParams;
    }

    public WebDialog getWebDialogRequest() {
        Bundle postParams = getBundleForFriendsRequest();
        return (new WebDialog.RequestsDialogBuilder(activity,
                Session.getActiveSession(), postParams)).setOnCompleteListener(
                getWebDialogOnCompleteListener()).build();
    }

    private WebDialog.OnCompleteListener getWebDialogOnCompleteListener() {
        return new WebDialog.OnCompleteListener() {

            @Override
            public void onComplete(Bundle values, FacebookException facebookException) {
                parseFacebookRequestError(values, facebookException);
            }
        };
    }

    private void parseFacebookRequestError(Bundle values, FacebookException facebookException) {
        if (facebookException != null) {
            if (facebookException instanceof FacebookOperationCanceledException) {
                ToastUtils.longToast(R.string.invite_friends_fb_request_canceled);
            } else if (facebookException instanceof FacebookServiceException) {
                final int errorCodeCancel = 4201;
                FacebookServiceException facebookServiceException = (FacebookServiceException) facebookException;
                if (errorCodeCancel == facebookServiceException.getRequestError().getErrorCode()) {
                    ToastUtils.longToast(R.string.invite_friends_fb_request_canceled);
                } else {
                    ErrorUtils
                            .showError(activity, facebookServiceException.getRequestError().getErrorMessage());
                }
            } else if (!TextUtils.isEmpty(facebookException.getMessage())) {
                ErrorUtils.showError(activity, facebookException);
            }
        } else {
            final String requestId = values.getString("request");
            if (requestId != null) {
                ToastUtils.longToast(R.string.dlg_success_request_facebook);
            } else {
                ToastUtils.longToast(R.string.invite_friends_fb_request_canceled);
            }
        }
    }
}