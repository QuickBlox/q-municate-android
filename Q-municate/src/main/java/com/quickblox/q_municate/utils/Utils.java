package com.quickblox.q_municate.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.model.Friend;

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
            if (Consts.TOKEN_REQUIRED_ERROR.equals(error)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExactError(QBResponseException e, String msgError) {
        Log.d(Utils.class.getSimpleName(), "");
        List<String> errors = e.getErrors();
        for (String error : errors) {
            Log.d(Utils.class.getSimpleName(), "error =" +error);
            if (error.contains(msgError)) {
                Log.d(Utils.class.getSimpleName(), error + " contains "+msgError);
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

    public static QBUser friendToUser(Friend friend) {
        if (friend == null) {
            return null;
        }
        QBUser user = new QBUser();
        user.setId(friend.getId());
        user.setFullName(friend.getFullname());
        return user;
    }

    public static int[] toIntArray(List<Integer> integerList)  {
        int[] intArray = new int[integerList.size()];
        int i = 0;
        for (Integer e : integerList)
            intArray[i++] = e.intValue();
        return intArray;
    }

    public static ArrayList<Integer> toArrayList(int[] itemArray)  {
        ArrayList<Integer> integerList = new ArrayList<Integer>(itemArray.length);
        int i = 0;
        for (int item : itemArray) {
            integerList.add(item);
        }
        return integerList;
    }
}