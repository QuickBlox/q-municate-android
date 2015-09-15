package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static com.quickblox.q_municate_core.utils.CoreSharedHelper.Constants.*;

public class CoreSharedHelper {

    public interface Constants {

        String NAME = "Q-municate";
        String IMPORT_INITIALIZED = "import_initialized";
        String FIRST_AUTH = "first_auth";
    }

    protected final SharedPreferences sharedPreferences;
    protected final SharedPreferences.Editor sharedPreferencesEditor;

    private static CoreSharedHelper instance;

    public static CoreSharedHelper getSharedHelper() {
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
}