package com.misfit.syncsdk.utils;

import com.misfit.syncsdk.model.SdkDayRange;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * conversion between either two kinds of Date, Calender and TimeZone objects.
 * some methods are similar prometheus.utils.DateUtil
 */
public class DateUtils {

    public static final String FT_DATE = "yyyy-MM-dd";

    /*
    * this variable follows the one in prometheus.utils.DateUtil, maybe this one is unnecessary to be ThreadLocal
    * */
    public static ThreadLocal<SimpleDateFormat> sDfDate = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(FT_DATE, Locale.US);
        }
    };

    /*
     * create a TimeZone from time zone offset seconds
     * */
    public static TimeZone getTimeZoneByOffset(int offset) {
        int intervalHour = offset / 3600;
        int intervalMin = (offset % 3600) / 60;
        StringBuilder timeZoneId = new StringBuilder("GMT");
        if (offset < 0) {
            intervalMin = Math.abs(intervalMin);
            timeZoneId.append("-");
        } else {
            timeZoneId.append("+");
        }
        timeZoneId.append(intervalHour).append(":").append(intervalMin == 0 ? "00" : intervalMin);
        return TimeZone.getTimeZone(timeZoneId.toString());
    }

    public static SdkDayRange getSpecificDayRange(long seconds, TimeZone tz) {
        Calendar cal = Calendar.getInstance(tz);
        cal.setTimeInMillis(seconds * 1000);
        return getTimeIntervalSince1970(cal);
    }

    public static SdkDayRange getTimeIntervalSince1970(final Calendar day) {
        return getTimeIntervalSince1970(day, day.getTimeZone());
    }

    /**
     * create a day range between 00:00:00 and 23:59:59 on the date of Calendar with the time zone
     * */
    public static SdkDayRange getTimeIntervalSince1970(final Calendar day, final TimeZone tz) {
        final SdkDayRange goalDate = new SdkDayRange();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        calendar.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH));
        goalDate.startTime = calendar.getTimeInMillis() / 1000L;
        goalDate.day = dateFormat(calendar);

        calendar.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        goalDate.endTime = calendar.getTimeInMillis() / 1000L;

        goalDate.timeZoneOffsetSeconds = tz.getOffset(calendar.getTimeInMillis()) / 1000;
        return goalDate;
    }

    public static String dateFormat(Calendar calendar) {
        SimpleDateFormat dfDate = sDfDate.get();
        dfDate.setTimeZone(calendar.getTimeZone());
        return dfDate.format(calendar.getTime());
    }
}
