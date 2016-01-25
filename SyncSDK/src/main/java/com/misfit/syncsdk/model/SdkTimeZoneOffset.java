package com.misfit.syncsdk.model;

/**
 * model class to reflect Timezone offset in seconds
 */
public class SdkTimeZoneOffset {

    private long mTimestamp;

    private int mTimezoneOffsetInSecond;

    public long getTimestamp() {
        return mTimestamp;
    }

    public int getTimezoneOffsetInSecond() {
        return mTimezoneOffsetInSecond;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public void setmTimezoneOffsetInSecond(int timezoneOffsetInSecond) {
        this.mTimezoneOffsetInSecond = timezoneOffsetInSecond;
    }
}
