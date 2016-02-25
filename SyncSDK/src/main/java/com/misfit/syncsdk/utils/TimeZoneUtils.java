package com.misfit.syncsdk.utils;

import android.util.SparseArray;
import com.misfit.syncsdk.model.SdkTimezoneOffset;

import java.util.List;
import java.util.TimeZone;

/**
 * similar class of prometheus.service.TimezoneService
 */
public class TimeZoneUtils {

    /**
     * method moved from prometheus.service.TimezoneService#getTimezoneChanges()
     * */
    public static SparseArray<TimeZone> getTimezoneChanges(SdkTimezoneOffset firstChange,
                                                    List<SdkTimezoneOffset> followingChanges) {
        // TODO: below method can be refactored
        SparseArray<TimeZone> timezoneChanges = new SparseArray<>();
        if (firstChange == null) {
            if (followingChanges.size() > 0) {
                int timezoneOffset = followingChanges.get(0).getTimezoneOffsetInSecond();
                TimeZone tz = DateUtils.getTimeZoneByOffset(timezoneOffset);
                timezoneChanges.put(0, tz);
            } else {
                timezoneChanges.put(0, TimeZone.getDefault());
            }
        } else {
            int timezoneOffset = firstChange.getTimezoneOffsetInSecond();
            TimeZone tz = DateUtils.getTimeZoneByOffset(timezoneOffset);
            timezoneChanges.put((int)firstChange.getTimestamp(), tz);
        }

        if (followingChanges != null) {
            for (SdkTimezoneOffset sdkTimezoneOffset : followingChanges) {
                int timezoneOffset = sdkTimezoneOffset.getTimezoneOffsetInSecond();
                TimeZone tz = DateUtils.getTimeZoneByOffset(timezoneOffset);
                timezoneChanges.put((int) sdkTimezoneOffset.getTimestamp(), tz);
            }
        }
        return timezoneChanges;
    }
}
