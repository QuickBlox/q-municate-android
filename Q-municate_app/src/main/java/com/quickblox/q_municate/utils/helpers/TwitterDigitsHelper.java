package com.quickblox.q_municate.utils.helpers;

import android.content.Context;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsOAuthSigning;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.StringObfuscator;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class TwitterDigitsHelper {

    private final Context context;

    public static final String PROVIDER = "X-Auth-Service-Provider";
    public static final String CREDENTIALS = "X-Verify-Credentials-Authorization";

    public TwitterDigitsHelper(Context context) {
        this.context = context;
        initTwitterDigits();
    }

    private void initTwitterDigits() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                StringObfuscator.getTwitterConsumerKey(),
                StringObfuscator.getTwitterConsumerSecret());

        Fabric.with(context.getApplicationContext(),
                new TwitterCore(authConfig),
                new Digits.Builder()
                        .withTheme(R.style.AppTheme).build());
    }

    public void login(AuthCallback authCallback){
        AuthConfig authConfig = new AuthConfig.Builder().withAuthCallBack(authCallback).build();
        Digits.authenticate(authConfig);
    }

    public static void logout(){
        Digits.clearActiveSession();
    }

    public static Map<String, String> retrieveCurrentAuthHeaders(){
        TwitterAuthConfig authConfig = TwitterCore.getInstance().getAuthConfig();
        TwitterAuthToken authToken = Digits.getActiveSession().getAuthToken();
        DigitsOAuthSigning authSigning = new DigitsOAuthSigning(authConfig, authToken);
        return authSigning.getOAuthEchoHeadersForVerifyCredentials();
    }
}
