package com.quickblox.qmunicate.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static Date stringToDate(String dateString) {
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat();
        try {
            date = sdf.parse(dateString);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
