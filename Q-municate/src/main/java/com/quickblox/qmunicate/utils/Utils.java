package com.quickblox.qmunicate.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.view.View;

import java.lang.reflect.Method;

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

    public static void disableViewHardwareAcceleration(View view) {
        try {
            Method setLayerTypeMethod = view.getClass().getMethod("setLayerType",
                    new Class[]{int.class, Paint.class});
            setLayerTypeMethod.invoke(view, new Object[]{View.LAYER_TYPE_SOFTWARE, null});
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }
}