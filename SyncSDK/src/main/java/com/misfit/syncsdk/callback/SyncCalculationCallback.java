package com.misfit.syncsdk.callback;

import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.syncsdk.model.SdkActivityChangeTag;
import com.misfit.syncsdk.model.SdkAutoSleepStateChangeTag;
import com.misfit.syncsdk.model.SdkProfile;
import com.misfit.syncsdk.model.SdkTimeZoneOffset;

import java.util.List;

/**
 * callback for calculation during sync. All methods are used to query data from App invoker
 */
public interface SyncCalculationCallback {
    /**
     * get ShineConfiguration from App to update device config
     * */
    ShineConfiguration getUpdatedShineConfiguration();

    /**
     * query the Profile table in database
     *
     * @return: SdkProfile instance which holds user's age, gender, height, weight, and weight unit
     * */
    SdkProfile getUserProfile();

    /**
     * query the SdkActivityChangeTag list from Settings change history
     *
     * @parameter: int[2] of session start time and end time
     * @return: the ActivityTag before the session start time, and the ActivityTag change list between the time period
     * */
    List<SdkActivityChangeTag> getSdkActivityChangeTagList(long startTime, long endTime);

    /**
     * query the SdkAutoSleepStateChangeTag list from Settings change history
     *
     * @parameter: int[2] of session start time and end time
     * @return: the AutoSleepState change list between the time period
     * */
    List<SdkAutoSleepStateChangeTag> getSdkAutoSleepStateChangeTagList(long startTime, long endTime);

    /**
     * query the SdkTimeZoneOffset from current Settings
     * */
    SdkTimeZoneOffset getSdkTimeZoneOffsetInCurrentSettings();

    /**
     * query the SdkTimeZoneOffset before given moment
     * */
    SdkTimeZoneOffset getSdkTimeZoneOffsetBefore(long timestamp);

    /**
     * query the SdkTimeZoneOffset changes since given moment
     * */
    List<SdkTimeZoneOffset> getSdkTimeZoneOffsetListAfter(long timestamp);
}
