package com.quickblox.qmunicate.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

    private static long MILLIS_VALUE = 1000;

    public static String longToMessageDate(long dateLong) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateLong * MILLIS_VALUE);
        String timeString = new SimpleDateFormat("hh:mm").format(calendar.getTime());
        return timeString;
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis() / MILLIS_VALUE;
    }
}