package com.quickblox.q_municate_core.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtilsCore {

    private static long MILLIS_VALUE = 1000;

    public static long getCurrentTime() {
        return System.currentTimeMillis() / MILLIS_VALUE;
    }

    public static long getTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis() / MILLIS_VALUE;
    }
}