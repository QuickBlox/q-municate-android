package com.quickblox.qmunicate.utils;

import java.util.Date;

public class DateUtils {

    public static long dateToLong(Date date) {
        return date.getTime();
    }

    public static Date longToDate(long dateLong) {
        return new Date(dateLong);
    }
}