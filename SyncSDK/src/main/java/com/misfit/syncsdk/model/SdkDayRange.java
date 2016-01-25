package com.misfit.syncsdk.model;

import java.util.Locale;

/**
 * model class to reflect com.misfitwearables.prometheus.model.DayRange
 */
public class SdkDayRange {
    public long startTime;
    public long endTime;
    public String day;
    public int timezoneOffset;

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s, %d, %d, %d", day, startTime, endTime, timezoneOffset);
    }
}
