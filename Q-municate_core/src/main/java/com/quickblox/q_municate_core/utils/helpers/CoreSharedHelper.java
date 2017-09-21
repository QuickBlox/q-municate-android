package com.quickblox.q_municate_core.utils.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import static com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper.Constants.*;

public class CoreSharedHelper {

    public class Constants {

        public static final String NAME = "Q-municate";

        public static final String IMPORT_INITIALIZED = "import_initialized";
        public static final String FIRST_AUTH = "first_auth";
        public static final String FB_TOKEN = "fb_token";
        public static final String FIREBASE_TOKEN = "firebase_token";

        public static final String USER_ID = "user_id";
        public static final String USER_EMAIL = "user_email";
        public static final String USER_PASSWORD = "user_password";
        public static final String USER_FULL_NAME = "full_name";
        public static final String USER_FB_ID = "facebook_id";
        public static final String USER_TWITTER_ID = "twitter_id";
        public static final String USER_TD_ID = "twitter_digits_id";
        public static final String USER_CUSTOM_DATA = "user_custom_data";

        public static final String PUSH_NEED_TO_OPEN_DIALOG = "push_need_to_open_dialog";
        public static final String PUSH_DIALOG_ID = "push_dialog_id";
        public static final String PUSH_USER_ID = "push_user_id";
        public static final String PUSH_REGISTRATION_ID = "push_registration_id";
        public static final String PUSH_APP_VERSION = "push_app_version";

        public static final String CALL_HW_CODEC = "call_hw_codec";
        public static final String CALL_RESOLUTION = "call_resolution";
        public static final String CALL_STARTBITRATE = "call_startbitrate";
        public static final String CALL_STARTBITRATE_VALUE = "call_startbitrate_value";
        public static final String CALL_VIDEO_CODEC = "call_video_codec";
        public static final String CALL_AUDIO_CODEC = "call_audio_codec";

        public static final String LAST_OPEN_ACTIVITY = "last_open_activity";

        public static final String PERMISSIONS_SAVE_FILE_WAS_REQUESTED = "permission_save_file_was_requested";
    }

    protected final SharedPreferences sharedPreferences;
    protected final SharedPreferences.Editor sharedPreferencesEditor;

    private static CoreSharedHelper instance;

    public static CoreSharedHelper getInstance() {
        if (instance == null) {
            throw new NullPointerException("CoreSharedHelper was not initialized!");
        }
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

    public String getFBToken() {
        return getPref(Constants.FB_TOKEN, null);
    }

    public void saveFBToken(String token) {
        savePref(Constants.FB_TOKEN, token);
    }

    public void saveFirebaseToken(String firebaseToken){
        savePref(FIREBASE_TOKEN, firebaseToken);
    }

    public String getFirebaseToken(){
        return getPref(FIREBASE_TOKEN, null);
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

    public void saveFBId(String facebookId){
        savePref(USER_FB_ID, facebookId);
    }

    public String getFBId(){
        return getPref(USER_FB_ID);
    }


    public void saveTwitterId(String twitterId){
        savePref(USER_TWITTER_ID, twitterId);
    }

    public String getTwitterId(){
        return getPref(USER_TWITTER_ID);
    }

    public void saveUserCustomData(String customData){
        savePref(USER_CUSTOM_DATA, customData);
    }

    public String getUserCustomData(){
        return getPref(USER_CUSTOM_DATA);
    }

    public void saveTwitterDigitsId(String twitterDigitsId){
        savePref(USER_TD_ID, twitterDigitsId);
    }

    public String getTwitterDigitsId(){
        return getPref(USER_TD_ID);
    }

    public boolean isPermissionsSaveFileWasRequested(){
        return getPref(Constants.PERMISSIONS_SAVE_FILE_WAS_REQUESTED, false);
    }

    public void savePermissionsSaveFileWasRequested(boolean requested){
        savePref(Constants.PERMISSIONS_SAVE_FILE_WAS_REQUESTED, requested);
    }

    public void clearUserData() {
        saveUserId(0);
        saveUserEmail(null);
        saveUserPassword(null);
        saveUserFullName(null);
        saveFBId(null);
        saveTwitterId(null);
        saveTwitterDigitsId(null);
        saveUserCustomData(null);

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

    public boolean getCallHwCodec(boolean defValue) {
        return getPref(Constants.CALL_HW_CODEC, defValue);
    }

    public void saveCallHwCodec(boolean value) {
        savePref(Constants.CALL_HW_CODEC, value);
    }

    public int getCallResolution(int defValue) {
        return getPref(Constants.CALL_RESOLUTION, defValue);
    }

    public void saveCallResolution(int value) {
        savePref(Constants.CALL_RESOLUTION, value);
    }

    public String getCallStartbitrate(String defValue) {
        return getPref(Constants.CALL_STARTBITRATE, defValue);
    }

    public void saveCallStartbitrate(String value) {
        savePref(Constants.CALL_STARTBITRATE, value);
    }

    public int getCallStartbitrateValue(int defValue) {
        return getPref(Constants.CALL_STARTBITRATE_VALUE, defValue);
    }

    public void saveCallStartbitrateValue(int value) {
        savePref(Constants.CALL_STARTBITRATE_VALUE, value);
    }

    public int getCallVideoCodec(int defValue) {
        return getPref(Constants.CALL_VIDEO_CODEC, defValue);
    }

    public void saveCallVideoCodec(int value) {
        savePref(Constants.CALL_VIDEO_CODEC, value);
    }

    public String getCallAudioCodec(String defValue) {
        return getPref(Constants.CALL_AUDIO_CODEC, defValue);
    }

    public void saveLastOpenActivity(String activityClassName) {
        savePref(LAST_OPEN_ACTIVITY, activityClassName);
    }

    public String getLastOpenActivity() {
        return getPref(LAST_OPEN_ACTIVITY);
    }
}