package com.misfit.syncsdk.model;

/**
 * model class to reflect auto sleep state change in Settings from App side
 */
public class SdkAutoSleepStateChangeTag {

    private long mTimestamp = 0;

    private boolean mIsAutoSleepState = true;

    public SdkAutoSleepStateChangeTag(){}

    public SdkAutoSleepStateChangeTag(long timestamp, boolean isAutoSleep) {
        mTimestamp = timestamp;
        mIsAutoSleepState = isAutoSleep;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public boolean isAutoSleepState() {
        return mIsAutoSleepState;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public void setAutoSleepState(boolean autoSleepState) {
        this.mIsAutoSleepState = autoSleepState;
    }
}
