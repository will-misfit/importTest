package com.misfit.syncsdk.model;

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

    public SdkResourceSettings(long timestamp, boolean autoSleepState, int defaultTripleTap, int timezoneOffset) {
        mTimestamp = timestamp;
        mAutoSleepState = autoSleepState;
        mDefaultTripleTap = defaultTripleTap;
        mTimezoneOffset = timezoneOffset;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public boolean getAutoSleepState() {
        return mAutoSleepState;
    }

    public int getDefaultTripleState() {
        return mDefaultTripleTap;
    }

    public int getTimezoneOffset() {
        return mTimezoneOffset;
    }
}