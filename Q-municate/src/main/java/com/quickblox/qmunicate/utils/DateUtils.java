package com.quickblox.qmunicate.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static long dateToLong(Date date) {
        return date.getTime();
    }

    public static Date longToDate(long dateLong) {
        return new Date(dateLong);
    }

    public static String longToMessageDate(long dateLong) {
        Date date = new Date(dateLong);
        String dateString =  new SimpleDateFormat("yyyy-MM-dd").format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss").format(date);
        return dateString + "\n" + timeString;
    }
}