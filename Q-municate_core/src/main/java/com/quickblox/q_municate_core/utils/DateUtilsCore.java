package com.quickblox.q_municate_core.utils;

public class DateUtilsCore {

    private static long MILLIS_VALUE = 1000;

    public static long getCurrentTime() {
        return System.currentTimeMillis() / MILLIS_VALUE;
    }
}