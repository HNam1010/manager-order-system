package com.example.be.commons.util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class DateUtil {

    public static String convertddMMyyyy(Date date) {
        return getDateWithFormat(date, "dd-MM-yyyy");
    }

    public static String convertddMMyyyyHHmmss(Date date) {
        return getDateWithFormat(date, "dd-MM-yyyy HH:mm:ss");
    }

    public static String convertddMMyyyyTHHmmss(Date date) {
        return getDateWithFormat(date, "dd-MM-yyyy'T'HH:mm:ss");
    }

    public static String convertyyyyMMddTHHmmss(Date date) {
        return getDateWithFormat(date, "yyyy-MM-dd'T'HH:mm:ss");
    }

    public static Date getBeginningOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public static Date getEndOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTime();
    }

    public static Timestamp getTimestampBeginningOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTimeInMillis());
    }

    public static Timestamp getTimestampEndOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTimeInMillis());
    }

    public static String getDateWithFormat(Date date, String pattern) {
        if (date == null) return "";
        SimpleDateFormat fmt = new SimpleDateFormat(pattern);
        return fmt.format(date);
    }

}
