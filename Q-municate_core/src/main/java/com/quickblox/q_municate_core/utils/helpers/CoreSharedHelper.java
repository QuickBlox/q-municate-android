package com.quickblox.q_municate_core.utils.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.quickblox.q_municate_core.models.LoginType;

import static com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper.Constants.*;

public class CoreSharedHelper {

    public interface Constants {

        String NAME = "Q-municate";

        String LOGIN_TYPE = "login_type";
        String IMPORT_INITIALIZED = "import_initialized";
        String FIRST_AUTH = "first_auth";
        String QB_TOKEN = "qb_token";
        String FB_TOKEN = "fb_token";

        String USER_ID = "user_id";
        String USER_EMAIL = "user_email";
        String USER_PASSWORD = "user_password";
        String USER_FULL_NAME = "full_name";

        String PUSH_NEED_TO_OPEN_DIALOG = "push_need_to_open_dialog";
        String PUSH_DIALOG_ID = "push_dialog_id";
        String PUSH_USER_ID = "push_user_id";
        String PUSH_REGISTRATION_ID = "push_registration_id";
        String PUSH_APP_VERSION = "push_app_version";
        String PUSH_IS_SUBSCRIBED_ON_QB_SERVER = "push_subscribed_on_qb_server";
    }

    protected final SharedPreferences sharedPreferences;
    protected final SharedPreferences.Editor sharedPreferencesEditor;

    private static CoreSharedHelper instance;

    public static CoreSharedHelper getInstance() {
        return instance;
    }

    public CoreSharedHelper(Context context) {
        instance = this;
        sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    protected void delete(String key) {
        if (sharedPreferences.contains(key)) {
            sharedPreferencesEditor.remove(key).commit();
        }
    }

    protected void savePref(String key, Object value) {
        delete(key);

        if (value instanceof Boolean) {
            sharedPreferencesEditor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            sharedPreferencesEditor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            sharedPreferencesEditor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            sharedPreferencesEditor.putLong(key, (Long) value);
        } else if (value instanceof String) {
            sharedPreferencesEditor.putString(key, (String) value);
        } else if (value instanceof Enum) {
            sharedPreferencesEditor.putString(key, value.toString());
        } else if (value != null) {
            throw new RuntimeException("Attempting to save non-primitive preference");
        }

        sharedPreferencesEditor.commit();
    }

    protected <T> T getPref(String key) {
        return (T) sharedPreferences.getAll().get(key);
    }

    protected <T> T getPref(String key, T defValue) {
        T returnValue = (T) sharedPreferences.getAll().get(key);
        return returnValue == null ? defValue : returnValue;
    }

    public void clearAll() {
        sharedPreferencesEditor.clear();
    }

    public String getLoginType() {
        return getPref(Constants.LOGIN_TYPE, LoginType.EMAIL.toString());
    }

    public void saveLoginType(String loginType) {
        savePref(Constants.LOGIN_TYPE, loginType);
    }

    public boolean isUsersImportInitialized() {
        return getPref(Constants.IMPORT_INITIALIZED, false);
    }

    public void saveUsersImportInitialized(boolean save) {
        savePref(Constants.IMPORT_INITIALIZED, save);
    }

    public boolean isFirstAuth() {
        return getPref(Constants.FIRST_AUTH, false);
    }

    public void saveFirstAuth(boolean firstAuth) {
        savePref(Constants.FIRST_AUTH, firstAuth);
    }

    public String getQBToken() {
        return getPref(Constants.QB_TOKEN, null);
    }

    public void saveQBToken(String token) {
        savePref(Constants.QB_TOKEN, token);
    }

    public String getFBToken() {
        return getPref(Constants.FB_TOKEN, null);
    }

    public void saveFBToken(String token) {
        savePref(Constants.FB_TOKEN, token);
    }

    public int getUserId() {
        return getPref(Constants.USER_ID, 0);
    }

    public void saveUserId(int id) {
        savePref(Constants.USER_ID, id);
    }

    public String getUserEmail() {
        return getPref(Constants.USER_EMAIL, null);
    }

    public void saveUserEmail(String email) {
        savePref(Constants.USER_EMAIL, email);
    }

    public String getUserPassword() {
        return getPref(Constants.USER_PASSWORD, null);
    }

    public void saveUserPassword(String password) {
        savePref(Constants.USER_PASSWORD, password);
    }

    public String getUserFullName() {
        return getPref(Constants.USER_FULL_NAME, null);
    }

    public void saveUserFullName(String fullName) {
        savePref(Constants.USER_FULL_NAME, fullName);
    }

    public void clearUserData() {
        saveUserId(0);
        saveUserEmail(null);
        saveUserPassword(null);
        saveUserFullName(null);
    }

    public boolean needToOpenDialog() {
        return getPref(Constants.PUSH_NEED_TO_OPEN_DIALOG, false);
    }

    public void saveNeedToOpenDialog(boolean open) {
        savePref(Constants.PUSH_NEED_TO_OPEN_DIALOG, open);
    }

    public String getPushDialogId() {
        return getPref(Constants.PUSH_DIALOG_ID, null);
    }

    public void savePushDialogId(String dialogId) {
        savePref(Constants.PUSH_DIALOG_ID, dialogId);
    }

    public int getPushUserId() {
        return getPref(Constants.PUSH_USER_ID, 0);
    }

    public void savePushUserId(int userId) {
        savePref(Constants.PUSH_USER_ID, userId);
    }

    public String getPushRegistrationId() {
        return getPref(Constants.PUSH_REGISTRATION_ID, null);
    }

    public void savePushRegistrationId(String registrationId) {
        savePref(Constants.PUSH_REGISTRATION_ID, registrationId);
    }

    public int getPushAppVersion() {
        return getPref(Constants.PUSH_APP_VERSION, 0);
    }

    public void savePushAppVersion(int appVersion) {
        savePref(Constants.PUSH_APP_VERSION, appVersion);
    }

    public boolean isSubscribedOnPush() {
        return getPref(Constants.PUSH_IS_SUBSCRIBED_ON_QB_SERVER, false);
    }

    public void saveSubscribedOnPush(boolean subscribed) {
        savePref(Constants.PUSH_IS_SUBSCRIBED_ON_QB_SERVER, subscribed);
    }
}