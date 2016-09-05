package com.quickblox.q_municate.utils;

import android.os.Build;

public class DeviceInfoUtils {

    private static final String DEVICE = "Device: ";
    private static final String SDK_VERSION = "SDK version: ";
    private static final String MODEL = "Model: ";
    private static final String APP_VERSION = "Q-municate build version: ";
    private static final String NEW_LINE = "\n";
    private static final String DIVIDER_STRING = "----------";

    public static StringBuilder getDeviseInfoForFeedback() {
        StringBuilder infoStringBuilder = new StringBuilder();
        infoStringBuilder.append(DEVICE).append(android.os.Build.DEVICE);
        infoStringBuilder.append(NEW_LINE);
        infoStringBuilder.append(SDK_VERSION).append(Build.VERSION.SDK_INT);
        infoStringBuilder.append(NEW_LINE);
        infoStringBuilder.append(MODEL).append(android.os.Build.MODEL);
        infoStringBuilder.append(NEW_LINE);
        infoStringBuilder.append(APP_VERSION).append(StringObfuscator.getAppVersionName());
        infoStringBuilder.append(NEW_LINE);
        infoStringBuilder.append(DIVIDER_STRING);
        infoStringBuilder.append(NEW_LINE);
        return infoStringBuilder;
    }
}