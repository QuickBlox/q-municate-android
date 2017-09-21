package com.quickblox.q_municate.utils;

import com.quickblox.q_municate.BuildConfig;

public class StringObfuscator {

    public static String getApplicationId() {
        return BuildConfig.APP_ID;
    }

    public static String getAuthKey() {
        return BuildConfig.AUTH_KEY;
    }

    public static String getAuthSecret() {
        return BuildConfig.AUTH_SECRET;
    }

    public static String getAccountKey(){
        return BuildConfig.ACCOUNT_KEY;
    }

    public static String getAppVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static boolean getDebugEnabled() {
        return BuildConfig.DEBUG;
    }

    public static String getTwitterConsumerKey(){
        return BuildConfig.TWITTER_KEY;
    }

    public static String getTwitterConsumerSecret(){
        return BuildConfig.TWITTER_SECRET;
    }

    public static String getFirebaseAuthProjectId(){
        return BuildConfig.FIREBASE_AUTH_PROJECT_ID;
    }
}