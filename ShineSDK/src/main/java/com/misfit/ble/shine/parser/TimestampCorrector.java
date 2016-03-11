package com.misfit.ble.shine.parser;

import com.misfit.ble.shine.result.Activity;
import com.misfit.ble.shine.result.SyncResult;

import java.util.ArrayList;
import java.util.List;

public class TimestampCorrector {

    public static final long THE_MAGIC_TIMESTAMP = 1369008000;    // May 20th, 2013
    public static final long DEFAULT_FILE_TIMESTAMP = -1;

    private static class SyncDataTimestampInfo {
        long mFileTimestamp;
        long mFileEndTimestamp;
        SyncResult mSyncData;

        public SyncDataTimestampInfo(long fileTimestamp, long fileEndTimestamp, SyncResult syncData) {
            mFileTimestamp = fileTimestamp;
            mFileEndTimestamp = fileEndTimestamp;
            mSyncData = syncData;
        }
    }

    private boolean mHasTimeReset = false;
    private ArrayList<SyncDataTimestampInfo> mPendingSyncDataInfo = new ArrayList<>();

    public static boolean shouldCorrectTimestamp(long timestamp) {
        return timestamp <= THE_MAGIC_TIMESTAMP;
    }

    public SyncResult processSyncData(SyncResult syncData, long fileTimestamp, boolean isLastFile) {
        if (!mHasTimeReset && !shouldCorrectTimestamp(fileTimestamp)) {
            return syncData;
        }

        mHasTimeReset = true;

        long fileEndTimestamp = fileTimestamp;
        List<Activity> activities = syncData.mActivities;
        if (null != activities && activities.size() > 0) {
            Activity lastActivity = activities.get(activities.size() - 1);
            fileEndTimestamp = lastActivity.mEndTimestamp;
        }

        SyncDataTimestampInfo syncDataInfo = new SyncDataTimestampInfo(fileTimestamp, fileEndTimestamp, syncData);
        mPendingSyncDataInfo.add(syncDataInfo);

        if (isLastFile) {
            return postProcessPendingSyncData();
        }

        return null;
    }
    
    private SyncResult postProcessPendingSyncData() {
        long previousFileTimestamp = DEFAULT_FILE_TIMESTAMP;
        long previousFileTimestampCorrected = System.currentTimeMillis() / 1000;

        ArrayList<SyncResult> syncResults = new ArrayList<>();

        // NOTE: syncData must be processed in the reserved order (from the later to earlier ones)
        for (int i = mPendingSyncDataInfo.size() - 1; i >= 0; --i) {
            SyncDataTimestampInfo info = mPendingSyncDataInfo.get(i);
            SyncResult syncData = info.mSyncData;
            long fileTimestamp = info.mFileTimestamp;
            long fileEndTimestamp = info.mFileEndTimestamp;
            long delta = 0;

            if (fileTimestamp >= previousFileTimestamp || previousFileTimestamp == DEFAULT_FILE_TIMESTAMP) {
                // There's a reset before this file was created => shift data to the beginning of previous file
                delta = previousFileTimestampCorrected - fileEndTimestamp;
            } else {
                delta = previousFileTimestampCorrected - previousFileTimestamp;
            }

            previousFileTimestamp = fileTimestamp;
            previousFileTimestampCorrected = fileTimestamp + delta;

            syncData.correctSyncDataTimestamp(delta);
            syncResults.add(0, syncData);
        }

        // NOTE: merge processed syncData in ascending order of time
        return SyncResult.mergeSyncData(syncResults);
    }
}
