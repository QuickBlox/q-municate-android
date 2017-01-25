package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static int getAppVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            ErrorUtils.logError(e);
        }
        return 0;
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            ErrorUtils.logError(e);
        }
        return null;
    }

    public static boolean isTokenDestroyedError(QBResponseException e) {
        List<String> errors = e.getErrors();
        for (String error : errors) {
            if (ConstsCore.TOKEN_REQUIRED_ERROR.equals(error)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExactError(QBResponseException e, String msgError) {
        Log.d(Utils.class.getSimpleName(), "");
        List<String> errors = e.getErrors();
        for (String error : errors) {
            Log.d(Utils.class.getSimpleName(), "error =" + error);
            if (error.contains(msgError)) {
                Log.d(Utils.class.getSimpleName(), error + " contains " + msgError);
                return true;
            }
        }
        return false;
    }


    public static void closeOutputStream(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                ErrorUtils.logError(e);
            }
        }
    }

    public static QBUser friendToUser(QMUser friend) {
        if (friend == null) {
            return null;
        }
        QBUser user = new QBUser();
        user.setId(friend.getId());
        user.setFullName(friend.getFullName());
        return user;
    }

    public static int[] toIntArray(List<Integer> integerList) {
        int[] intArray = new int[integerList.size()];
        int i = 0;
        for (Integer e : integerList) {
            intArray[i++] = e.intValue();
        }
        return intArray;
    }

    public static ArrayList<Integer> toArrayList(int[] itemArray) {
        ArrayList<Integer> integerList = new ArrayList<Integer>(itemArray.length);
        for (int item : itemArray) {
            integerList.add(item);
        }
        return integerList;
    }

    public static boolean validateNotNull(Object object) {
        return object != null;
    }

    public static String customDataToString(UserCustomData userCustomData) {
        JSONObject jsonObject = new JSONObject();

        setJsonValue(jsonObject, UserCustomData.TAG_AVATAR_URL, userCustomData.getAvatarUrl());
        setJsonValue(jsonObject, UserCustomData.TAG_STATUS, userCustomData.getStatus());
        setJsonValue(jsonObject, UserCustomData.TAG_IS_IMPORT, userCustomData.getIsImport());

        return jsonObject.toString();
    }

    private static void setJsonValue(JSONObject jsonObject, String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            try {
                jsonObject.put(key, value);
            } catch (JSONException e) {
                ErrorUtils.logError(e);
            }
        }
    }

    public static UserCustomData customDataToObject(String userCustomDataString) {
        if (TextUtils.isEmpty(userCustomDataString)) {
            return new UserCustomData();
        }

        UserCustomData userCustomData = null;
        GsonBuilder gsonBuilder = new GsonBuilder();

        Gson gson = gsonBuilder.create();

        try {
            userCustomData = gson.fromJson(userCustomDataString, UserCustomData.class);
        } catch (JsonSyntaxException e) {
            ErrorUtils.logError(e);
        }

        return userCustomData;
    }
}