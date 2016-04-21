package com.misfit.syncsdk.model;

import com.misfit.syncsdk.enums.SdkActivityType;

/**
 * model class of partial fields of flagship App Settings
 * it is used to hold settings fields from flagship App
 *
 */
public class SdkResourceSettings {

    private long mTimestamp;

    private boolean mAutoSleepState;

    private int mDefaultTripleTap;   // enum int of syncsdk.enums.SdkActivityType

    private int mTimezoneOffset;

    public SdkResourceSettings(long timestamp, boolean autoSleepState, @SdkActivityType.ActivityType int defaultTripleTap, int timezoneOffset) {
        mTimestamp = timestamp;
        mAutoSleepState = autoSleepState;
        mDefaultTripleTap = defaultTripleTap;
        mTimezoneOffset = timezoneOffset;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public boolean getAutoSleepState() {
        return mAutoSleepState;
    }

    @SdkActivityType.ActivityType
    public int getDefaultTripleState() {
        return mDefaultTripleTap;
    }

    public int getTimezoneOffset() {
        return mTimezoneOffset;
    }
}
