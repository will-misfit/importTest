package com.misfit.syncsdk.callback;

import com.misfit.syncsdk.model.SdkActivityChangeTag;
import com.misfit.syncsdk.model.SdkProfile;
import com.misfit.syncsdk.operator.SyncOperationResultCallback;

import java.util.List;

/**
 * Created by Will Hou on 1/11/16.
 */
public interface SyncSyncCallback extends SyncOperationResultCallback {
    //FIXME: wait to declare
    void onSyncDataOutput();

    /**
     * query the Profile table in database of App
     *
     * @return: SdkProfile instance which holds user's age, gender, height, weight, and weight unit
     * */
    SdkProfile getProfileInDatabase();

    /**
     * query the Settings table in database of App
     *
     * @parameter: int[2] of session start time and end time
     * @return: the ActivityTag before the session start time, and the ActivityTag change list between the time period
     * */
    List<SdkActivityChangeTag> getSdkActivityChangeTagList(int[] startEndTime);
}
