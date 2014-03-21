package com.quickblox.qmunicate.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.quickblox.qmunicate.R;

public class PrefsHelper {

    public static final String PREF_REMEMBER_ME = "Remember Me";
    public static final String PREF_LOGIN_TYPE = "Login type";
    public static final String PREF_USER_EMAIL = "Email";
    public static final String PREF_USER_PASSWORD = "Password";
    public static final String PREF_PUSH_NOTIFICATIONS = "Push Notifications";
    public static final String PREF_LANDING_SHOWN = "Landing screen was shown";
    public static final String PREF_USER_LEARNED_DRAWER = "Navigation Drawer learned";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public PrefsHelper(Context context) {
        String prefsFile = context.getString(R.string.prefs_name);
        sharedPreferences = context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void delete(String key) {
        if (sharedPreferences.contains(key)) {
            editor.remove(key).commit();
        }
    }

    public void savePref(String key, Object value) {
        delete(key);

        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else {
            throw new RuntimeException("Attempting to save non-primitive preference");
        }

        editor.commit();
    }

    @SuppressWarnings("unchecked")
    public <T> T getPref(String key) {
        return (T) sharedPreferences.getAll().get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPref(String key, T defValue) {
        T returnValue = (T) sharedPreferences.getAll().get(key);
        return returnValue == null ? defValue : returnValue;
    }

    public boolean isPrefExists(String key) {
        return sharedPreferences.contains(key);
    }
}
