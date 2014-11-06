package com.quickblox.q_municate.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static long MILLIS_VALUE = 1000;
    private static String STRING_TODAY = "Today";
    private static String STRING_YESTERDAY = "Yesterday";

    public static String longToMessageDate(long dateLong) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateLong * MILLIS_VALUE);
        String timeString = new SimpleDateFormat("hh:mm").format(calendar.getTime());
        return timeString;
    }

    public static String longToMessageListHeaderDate(long dateLong) {
        String timeString;

        Locale locale = new Locale("en");

        Calendar calendar = Calendar.getInstance();
        int currentDate = calendar.getTime().getDate();

        calendar.setTimeInMillis(dateLong * MILLIS_VALUE);
        int inputDate = calendar.getTime().getDate();

        if (inputDate == currentDate) {
            timeString = STRING_TODAY;
        } else if (inputDate == currentDate - 1) {
            timeString = STRING_YESTERDAY;
        } else {
            Date time = calendar.getTime();
            timeString = new SimpleDateFormat("EEEE", locale).format(time) + ", " + inputDate  + " " +
                    new SimpleDateFormat("MMMM", locale).format(time);
        }

        return timeString;
    }
}