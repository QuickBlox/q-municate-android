package com.quickblox.q_municate.utils.helpers;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsOAuthSigning;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.Map;

public class TwitterDigitsHelper {

    public static final String PROVIDER = "X-Auth-Service-Provider";
    public static final String CREDENTIALS = "X-Verify-Credentials-Authorization";

    public TwitterDigitsHelper() {
    }

    public void login(AuthCallback authCallback){
        AuthConfig authConfig = new AuthConfig.Builder().withAuthCallBack(authCallback).build();
        Digits.authenticate(authConfig);
    }

    public void logout(){
        Digits.clearActiveSession();
    }

    public static Map<String, String> retrieveCurrentAuthHeaders(){
        TwitterAuthConfig authConfig = TwitterCore.getInstance().getAuthConfig();
        TwitterAuthToken authToken = Digits.getActiveSession().getAuthToken();
        DigitsOAuthSigning authSigning = new DigitsOAuthSigning(authConfig, authToken);
        return authSigning.getOAuthEchoHeadersForVerifyCredentials();
    }
}
