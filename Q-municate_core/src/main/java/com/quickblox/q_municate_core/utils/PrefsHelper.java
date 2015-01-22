package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {

    public static final String PREF_REMEMBER_ME = "remember_me";
    public static final String PREF_LOGIN_TYPE = "login_type";
    public static final String PREF_DIALOG_ID = "dialog_id";
    public static final String PREF_USER_EMAIL = "email";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_IS_LOGINED = "is_logined";
    public static final String PREF_USER_PASSWORD = "password";
    public static final String PREF_PUSH_NOTIFICATIONS = "push_notifications";
    public static final String PREF_IMPORT_INITIALIZED = "import_initialized";
    public static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    public static final String PREF_SIGN_UP_INITIALIZED = "sign_up_initialized";
    public static final String PREF_MISSED_MESSAGE = "missed_message";
    public static final String PREF_STATUS = "status";
    public static final String PREF_PUSH_MESSAGE = "message";
    public static final String PREF_REG_USER_ID = "registered_push_user";
    public static final String PREF_GCM_SENDER_ID = "265299067289";
    public static final String PREF_USER_AGREEMENT = "user_agreement";
    public static final String PREF_JOINED_TO_ALL_DIALOGS = "joined_to_all_dialogs";

    public static final String PREF_PUSH_MESSAGE_NEED_TO_OPEN_DIALOG = "push_need_to_open_dialog";
    public static final String PREF_PUSH_MESSAGE_DIALOG_ID = "push_dialog_id";
    public static final String PREF_PUSH_MESSAGE_USER_ID = "push_user_id";

    public static final String PREF_REG_ID = "registration_id";
    public static final String PREF_APP_VERSION = "appVersion";
    public static final String PREF_RECEIVE_PUSH = "receive_push";
    public static final String PREF_IS_SUBSCRIBED_ON_SERVER = "subscribed_on_server";
    public static final String PREF_USER_FULL_NAME = "full_name";
    public static final String PREF_SESSION_TOKEN = "session_token";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private static PrefsHelper instance;

    public static PrefsHelper getPrefsHelper() {
        return instance;
    }

    public PrefsHelper(Context context) {
        instance = this;
        String prefsFile = context.getPackageName();
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
        } else if (value instanceof Enum) {
            editor.putString(key, value.toString());
        } else if (value != null) {
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
