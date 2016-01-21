package com.misfit.syncsdk.algorithm;

import com.misfit.ble.shine.result.SyncResult;
import com.misfit.cloud.algorithm.models.ACEEntryVect;
import com.misfit.cloud.algorithm.models.ActivityShineVect;
import com.misfit.cloud.algorithm.models.SWLEntryVect;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.model.SdkActivitySession;

import java.util.List;

/**
 *
 */
public class DailyUserDataBuilder {

    static {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("MisfitAlgorithmLib");
    }

    private static final String TAG = "DailyUserDataBuilder";
    private static DailyUserDataBuilder sDefaultInstance;

    public synchronized static DailyUserDataBuilder getInstance() {
        if (sDefaultInstance == null) {
            sDefaultInstance = new DailyUserDataBuilder();
        }
        return sDefaultInstance;
    }

    protected DailyUserDataBuilder() {}

    public List<SdkActivitySession> buildDailyUserDataForShine(SyncResult syncResult, SyncSyncCallback syncSyncCallback) {
        ActivityShineVect activityShineVect = AlgorithmUtils.convertSdkActivityToShineActivityForShine(
            syncResult.mActivities, syncResult.mTapEventSummarys);
        SWLEntryVect swlEntryVec = AlgorithmUtils.convertSwimSessionsToSWLEntry(syncResult.mSwimSessions);
        ACEEntryVect aceEntryVect = new ACEEntryVect(); // placeholder for ACE algorithm result in future
        return SdkActivitySessionBuilder.buildSDKActivitySessionForShine(activityShineVect, aceEntryVect, swlEntryVec, syncSyncCallback);
    }

    public void buildDailyUserDataForFlash(SyncResult syncResult, String serialNumber) {
        ActivityShineVect activityShineVect = AlgorithmUtils.convertSdkActivityToShineActivityForFlash(
            syncResult.mActivities, syncResult.mSessionEvents);
        // buildAndSaveDaysForFlash(activityShineVect, syncStartTime, serialNumber);
    }
}
