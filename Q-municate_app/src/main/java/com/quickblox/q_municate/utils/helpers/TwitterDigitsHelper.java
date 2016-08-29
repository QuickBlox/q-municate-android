package com.quickblox.q_municate.utils.helpers;

import android.app.Activity;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.quickblox.q_municate.R;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

public class TwitterDigitsHelper {

    private final Activity activity;

//    test values
// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "90CcwzxplDO6nzCmgSP8u37IY";
    private static final String TWITTER_SECRET = "61mnFULWURLwKxwjdMQwdZOMD6422dFYe218Nd8eRNGXSrYQDx";

    private final AuthCallback twitterDigitsAuthCallback;

    public TwitterDigitsHelper(Activity activity, AuthCallback twitterDigitsAuthCallback) {
        this.activity = activity;
        this.twitterDigitsAuthCallback = twitterDigitsAuthCallback;
        initTwitterDigits();
    }

    private void initTwitterDigits() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(activity.getApplicationContext(),
                new TwitterCore(authConfig),
                new Digits.Builder()
                        .withTheme(R.style.AppTheme).build());

    }

    public AuthCallback getAuthCallback(){
        return twitterDigitsAuthCallback;
    }

    public void loginWithTwitterDigits(){
        AuthConfig authConfig = new AuthConfig.Builder().withAuthCallBack(getAuthCallback()).build();
        Digits.authenticate(authConfig);
    }

    public static void logout(){
        Digits.clearActiveSession();
    }

}
