package com.quickblox.q_municate.utils;

import android.content.res.Resources;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static final long SECOND_IN_MILLIS = 1000;
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
    public static final long YEAR_IN_MILLIS = DAY_IN_MILLIS * 365;

    private static final SimpleDateFormat FULL_DATE_FORMAT;
    private static final SimpleDateFormat SHORT_DATE_FORMAT;
    private static final SimpleDateFormat SHORT_DATE_WITHOUT_DIVIDERS_FORMAT;
    private static final SimpleDateFormat SIMPLE_TIME_FORMAT;
    private static final SimpleDateFormat DAY_AND_MONTH_AND_YEAR_FULL_FORMAT;
    private static final SimpleDateFormat DAY_AND_MONTH_AND_YEAR_SHORT_FORMAT;
    private static final SimpleDateFormat DAY_AND_SHORT_MONTH_FORMAT;
    private static final SimpleDateFormat SHORT_MONTH_AND_DAY_FORMAT;

    static {
        FULL_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SHORT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        SHORT_DATE_WITHOUT_DIVIDERS_FORMAT = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        SIMPLE_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
        DAY_AND_MONTH_AND_YEAR_FULL_FORMAT = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
        DAY_AND_MONTH_AND_YEAR_SHORT_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        DAY_AND_SHORT_MONTH_FORMAT = new SimpleDateFormat("dd MMM", Locale.getDefault());
        SHORT_MONTH_AND_DAY_FORMAT = new SimpleDateFormat("MMM dd", Locale.getDefault());
    }

    public static long nowSeconds() {
        return nowMillis() / SECOND_IN_MILLIS;
    }

    public static long nowMillis() {
        return getCalendar().getTimeInMillis();
    }

    public static Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getDefault());
    }

    public static Calendar getCalendar(int year, int month, int day) {
        Calendar calendar = getCalendar();
        calendar.set(year, month, day);
        return calendar;
    }

    public static Calendar getCalendar(long seconds) {
        Calendar calendar = getCalendar();
        calendar.setTimeInMillis(seconds * SECOND_IN_MILLIS);
        return calendar;
    }

    public static long roundToDays(long seconds) {
        Calendar c = getCalendar(seconds);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        return c.getTimeInMillis() / SECOND_IN_MILLIS;
    }

    public static long getEndOfADay(long seconds) {
        Calendar calendar = getCalendar(seconds);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis() / SECOND_IN_MILLIS;
    }

    public static boolean isTimePassed(long seconds) {
        return DateUtils.getCalendar(seconds).before(DateUtils.getCalendar());
    }

    /**
     * @return string in "Hours:Minutes" format, i.e. <b>11:23</b>
     */
    public static String formatDateSimpleTime(long milliSeconds) {
        return SIMPLE_TIME_FORMAT.format(new Date(milliSeconds));
    }

    /**
     * @return string in "Day-Month-Year" format, i.e. <b>17-08-1992</b>
     */
    public static String formatDateFull(long seconds) {
        return FULL_DATE_FORMAT.format(new Date(seconds * SECOND_IN_MILLIS));
    }

    public static String formatDateFullShortMonth(long seconds) {
        return DAY_AND_MONTH_AND_YEAR_SHORT_FORMAT.format(new Date(seconds * SECOND_IN_MILLIS));
    }

    /**
     * @param dateString must be in Day-Month-Year format, i.e. <b>17-08-1992</b>
     * @return time in seconds
     */
    public static long parseDateFull(String dateString) {
        try {
            return FULL_DATE_FORMAT.parse(dateString).getTime() / SECOND_IN_MILLIS;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date().getTime() / SECOND_IN_MILLIS;
        }
    }

    /**
     * @param seconds time in seconds
     * @return time in format 2d 5h 5m + adds "ago" if seconds value is less than current time
     */
    public static String toDaysHoursMinutesLeftShort(long seconds) {
        return toDaysHoursMinutesLeft(seconds, true);
    }

    /**
     * @param seconds time in seconds
     * @return time in format 2 days 5 hours 5 minutes + adds "ago" if seconds value is less than current time
     */
    public static String toDaysHoursMinutesLeftFull(long seconds) {
        return toDaysHoursMinutesLeft(seconds, false);
    }

    private static String toDaysHoursMinutesLeft(long seconds, boolean shortFormat) {
        Calendar offerTime = getCalendar(seconds);
        Calendar now = getCalendar();
        now.set(Calendar.MILLISECOND, 0);
        now.set(Calendar.SECOND, 0);

        long timeDiff = Math.abs(offerTime.getTimeInMillis() - now.getTimeInMillis());
        int days = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);
        int hours = (int) (TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(days));
        int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours));

        Resources res = App.getInstance().getResources();
        StringBuilder stringBuilder = new StringBuilder();
        if (days > 0) {
            String daysString = shortFormat
                    ? App.getInstance().getString(R.string.days_short, days)
                    : res.getQuantityString(R.plurals.days, days, days);
            stringBuilder.append(daysString).append(" ");
        }
        if (hours > 0) {
            String hoursString = shortFormat
                    ? App.getInstance().getString(R.string.hours_short, hours)
                    : res.getQuantityString(R.plurals.hours, hours, hours);
            stringBuilder.append(hoursString).append(" ");
        }
        if (minutes > 0 && days == 0) {
            String minutesString = shortFormat
                    ? App.getInstance().getString(R.string.minutes_short, minutes)
                    : res.getQuantityString(R.plurals.minutes, minutes, minutes);
            stringBuilder.append(minutesString);
        } else if (days == 0) {
            stringBuilder.append(shortFormat
                    ? App.getInstance().getString(R.string.minutes_short, 0)
                    : res.getQuantityString(R.plurals.minutes, 0));
        }

        if (offerTime.before(now)) {
            stringBuilder.append(" ").append(App.getInstance().getString(R.string.date_ago));
        }

        return stringBuilder.toString();
    }

    public static String leftDays(long seconds) {
        StringBuilder sb = new StringBuilder();
        Calendar now = getCalendar();
        Calendar offerTime = getCalendar(seconds);

        long timeDiff = Math.abs(offerTime.getTimeInMillis() - now.getTimeInMillis());
        int days = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);
        sb.append(App.getInstance().getResources().getQuantityString(R.plurals.days, days, days));

        return sb.toString();
    }

    public static String leftMinutesHours(long seconds) {
        Calendar now = getCalendar();
        Calendar offerTime = getCalendar(seconds);

        long timeDiff = Math.abs(offerTime.getTimeInMillis() - now.getTimeInMillis());
        long days = TimeUnit.MILLISECONDS.toDays(timeDiff);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours);
        StringBuilder sb = new StringBuilder();
        Resources res = App.getInstance().getResources();
        if (hours > 0) {
            sb.append(res.getQuantityString(R.plurals.hours, (int) hours, hours)).append(" ");
        }
        if (minutes > 0) {
            sb.append(res.getQuantityString(R.plurals.minutes, (int) minutes, minutes));
        }
        return sb.toString();
    }

    public static int daysBetween(long seconds) {
        long date = roundToDays(seconds);
        long currentDate = roundToDays(nowSeconds());
        long diff = date - currentDate;

        return (int) TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS);
    }

    /**
     * @return today/yesterday string or "Day month, year" format, i.e. <b>17 April, 2015</b>
     */
    public static String toTodayYesterdayFullMonthDate(long seconds) {
        return toTodayYesterdayDateByFormat(seconds, DAY_AND_MONTH_AND_YEAR_FULL_FORMAT);
    }

    /**
     * @return today/yesterday string or "Day month, year" format, i.e. <b>17 Apr 2015</b>
     */
    public static String toTodayYesterdayShortMonthDate(long seconds) {
        return toTodayYesterdayDateByFormat(seconds, DAY_AND_MONTH_AND_YEAR_SHORT_FORMAT);
    }

    /**
     * @return today/yesterday string or "Day/Month/Year" format, i.e. <b>27/04/15</b>
     */
    public static String toTodayYesterdayShortDate(long seconds) {
        return toTodayYesterdayDateByFormat(seconds, SHORT_DATE_FORMAT);
    }

    /**
     * @return today/yesterday string or "Day Month" format, i.e. <b>27 Dec</b>
     */
    public static String toTodayYesterdayShortDateWithoutYear1(long seconds) {
        return toTodayYesterdayDateByFormat(seconds, DAY_AND_SHORT_MONTH_FORMAT);
    }

    /**
     * @return today/yesterday string or "Day Month" format, i.e. <b>Dec 27</b>
     */
    public static String toTodayYesterdayShortDateWithoutYear2(long milliSeconds) {
        return toTodayYesterdayDateByFormat(milliSeconds / SECOND_IN_MILLIS, SHORT_MONTH_AND_DAY_FORMAT);
    }

    private static String toTodayYesterdayDateByFormat(long seconds, SimpleDateFormat simpleDateFormat) {
        long today = roundToDays(nowSeconds());
        long inputDay = roundToDays(seconds);

        if (inputDay == today) {
            return App.getInstance().getString(R.string.today);
        } else if (inputDay == today - DAY_IN_MILLIS) {
            return App.getInstance().getString(R.string.yesterday);
        } else {
            return simpleDateFormat.format(new Date(seconds * SECOND_IN_MILLIS));
        }
    }

    /**
     * @return <b>27042015</b>
     */
    public static long toShortDateLong(long seconds) {
        Calendar calendar = getCalendar(seconds);
        return Long.parseLong(SHORT_DATE_WITHOUT_DIVIDERS_FORMAT.format(calendar.getTime()));
    }
}