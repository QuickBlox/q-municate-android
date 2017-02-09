package com.quickblox.q_municate.utils.helpers;

import android.app.Activity;
import android.content.Intent;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;

import java.util.ArrayList;
import java.util.List;

public class FacebookHelper {

    private static final String PERMISSION_EMAIL = "email";
    private static final String PERMISSION_USER_FRIENDS = "user_friends";

    private Activity activity;
    private FBAccessTokenTracker fbAccessTokenTracker;
    private CallbackManager fbCallbackManager;
    private LoginManager fbLoginManager;

    public FacebookHelper(Activity activity) {
        this.activity = activity;
        initFacebook();
    }

    public void logout() {
        LoginManager.getInstance().logOut();
    }

    private void initFacebook() {
        FacebookSdk.sdkInitialize(activity.getApplicationContext());

        fbCallbackManager = CallbackManager.Factory.create();

        fbLoginManager = LoginManager.getInstance();

        this.fbAccessTokenTracker = new FBAccessTokenTracker();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void login(FacebookCallback<LoginResult> facebookLoginCallback) {
        fbLoginManager.registerCallback(fbCallbackManager, facebookLoginCallback);
        fbLoginManager.logInWithReadPermissions(activity, generatePermissionsList());
    }

    public List<String> generatePermissionsList() {
        List<String> permissionsList = new ArrayList<String>();
        permissionsList.add(PERMISSION_EMAIL);
        permissionsList.add(PERMISSION_USER_FRIENDS);
        return permissionsList;
    }

    public void onActivityStart() {
        fbAccessTokenTracker.startTracking();
    }

    public void onActivityStop() {
        fbAccessTokenTracker.stopTracking();
    }

    public boolean isSessionOpened() {
        return CoreSharedHelper.getInstance().getFBToken() != null && AccessToken.getCurrentAccessToken() != null;
    }

    private class FBAccessTokenTracker extends AccessTokenTracker {

        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken != null) {
                CoreSharedHelper.getInstance().saveFBToken(currentAccessToken.getToken());
            } else {
                CoreSharedHelper.getInstance().saveFBToken(null);
            }
        }
    }
}