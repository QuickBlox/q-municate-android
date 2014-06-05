package com.quickblox.qmunicate.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.model.Friend;

import java.io.IOException;
import java.io.OutputStream;

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
}