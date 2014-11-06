package com.quickblox.q_municate.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookServiceException;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.widget.WebDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FacebookHelper {

    public static final List<String> PERMISSIONS = Arrays.asList("publish_actions", "publish_stream");
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
        TipsManager.setIsJustLogined(true);
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(activity).setPermissions(Arrays.asList("user_friends")).setCallback(facebookStatusCallback));
        } else {
            Session.openActiveSession(activity, true, facebookStatusCallback);
        }
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

    public void postInviteToWall(Request.Callback requestCallback, String[] selectedFriends) {
        Session session = Session.getActiveSession();
        if (session != null) {
            Resources resources = activity.getResources();
            Bundle postParams = new Bundle();
            postParams.putString(ConstsCore.FB_WALL_PARAM_NAME, resources.getString(R.string.inf_fb_wall_param_name));
            postParams.putString(ConstsCore.FB_WALL_PARAM_DESCRIPTION, resources.getString(R.string.inf_fb_wall_param_description));
            postParams.putString(ConstsCore.FB_WALL_PARAM_LINK, resources.getString(R.string.inf_fb_wall_param_link));
            postParams.putString(ConstsCore.FB_WALL_PARAM_PICTURE, resources.getString(R.string.inf_fb_wall_param_picture));
            postParams.putString(ConstsCore.FB_WALL_PARAM_PLACE, resources.getString(R.string.inf_fb_wall_param_place));
            postParams.putString(ConstsCore.FB_WALL_PARAM_TAGS, TextUtils.join(",", selectedFriends));
            Request request = new Request(session, ConstsCore.FB_WALL_PARAM_FEED, postParams, HttpMethod.POST, requestCallback);
            RequestAsyncTask task = new RequestAsyncTask(request);
            task.execute();
        }
    }

    private Bundle getBundleForFriendsRequest() {
        Bundle postParams = new Bundle();
        postParams.putString(ConstsCore.FB_REQUEST_PARAM_TITLE, resources.getString(R.string.inf_subject_of_invitation));
        postParams.putString(ConstsCore.FB_REQUEST_PARAM_MESSAGE, resources.getString(R.string.inf_body_of_invitation));
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
                DialogUtils.showLong(activity, resources.getString(R.string.inf_fb_request_canceled));
            } else if (facebookException instanceof FacebookServiceException) {
                final int errorCodeCancel = 4201;
                FacebookServiceException facebookServiceException = (FacebookServiceException) facebookException;
                if (errorCodeCancel == facebookServiceException.getRequestError().getErrorCode()) {
                    DialogUtils.showLong(activity, resources.getString(R.string.inf_fb_request_canceled));
                } else {
                    ErrorUtils.showError(activity,
                            facebookServiceException.getRequestError().getErrorMessage());
                }
            } else if (!TextUtils.isEmpty(facebookException.getMessage())) {
                ErrorUtils.showError(activity, facebookException);
            }
        } else {
            final String requestId = values.getString("request");
            if (requestId != null) {
                DialogUtils.showLong(activity, resources.getString(R.string.dlg_success_request_facebook));
            } else {
                DialogUtils.showLong(activity, resources.getString(R.string.inf_fb_request_canceled));
            }
        }
    }

    public boolean checkPermissions() {
        Session session = Session.getActiveSession();
        List<String> permissions = session.getPermissions();
        if (!isSubsetOf(FacebookHelper.PERMISSIONS, permissions)) {
            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(activity,
                    FacebookHelper.PERMISSIONS);
            session.requestNewPublishPermissions(newPermissionsRequest);
            return false;
        }
        return true;
    }

    private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }
}