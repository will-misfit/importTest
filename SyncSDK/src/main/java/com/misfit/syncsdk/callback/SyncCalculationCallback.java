package com.misfit.syncsdk.callback;

import com.misfit.syncsdk.model.SdkActivityChangeTag;
import com.misfit.syncsdk.model.SdkAutoSleepStateChangeTag;
import com.misfit.syncsdk.model.SdkProfile;
import com.misfit.syncsdk.model.SdkTimeZoneOffset;
import com.misfit.syncsdk.operator.SyncOperationResultCallback;

import java.util.List;

/**
 * callback for calculation during sync. All methods are used to query data from App invoker
 */
public interface SyncCalculationCallback {
    /**
     * query the Profile table in database of App
     *
     * @return: SdkProfile instance which holds user's age, gender, height, weight, and weight unit
     * */
    SdkProfile getProfileInDatabase();

    /**
     * query the SdkActivityChangeTag list from Settings change history of App
     *
     * @parameter: int[2] of session start time and end time
     * @return: the ActivityTag before the session start time, and the ActivityTag change list between the time period
     * */
    List<SdkActivityChangeTag> getSdkActivityChangeTagList(int[] startEndTime);

    /**
     * query the SdkAutoSleepStateChangeTag list from Settings change history of App
     *
     * @parameter: int[2] of session start time and end time
     * @return: the AutoSleepState change list between the time period
     * */
    List<SdkAutoSleepStateChangeTag> getSdkAutoSleepStateChangeTagList(int[] startEndTime);

    /**
     * query the SdkTimeZoneOffset from current Settings of App
     * */
    SdkTimeZoneOffset getSdkTimeZoneOffsetInCurrentSettings();

    /**
     * query the SdkTimeZoneOffset before given moment
     * */
    SdkTimeZoneOffset getSdkTimeZoneOffsetBefore(long timestamp);

    /**
     * query the SdkTimeZoneOffset changes since given moment from App
     * */
    List<SdkTimeZoneOffset> getSdkTimeZoneOffsetListAfter(long timestamp);
}
