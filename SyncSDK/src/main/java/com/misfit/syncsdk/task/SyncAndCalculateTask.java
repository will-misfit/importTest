package com.misfit.syncsdk.task;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfile.ActionResult;
import com.misfit.ble.util.MutableBoolean;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.algorithm.AlgorithmUtils;
import com.misfit.syncsdk.algorithm.DailyUserDataBuilder;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;
import com.misfit.syncsdk.utils.CheckUtils;
import com.misfit.syncsdk.model.SettingsElement;

import java.util.ArrayList;
import java.util.List;


/**
 * Task implementation to call sync() of ShineSDK, and complete the calculation with algorithm library
 */
public class SyncAndCalculateTask extends Task implements ShineProfile.SyncCallback {

    private final static String TAG = "SyncAndCalculateTask";

    private List<SyncResult> mSyncResultSummary;

    //ShineSdkSyncCompleteCallback mShineSdkSyncCompleteCallback;

    /* callback of Task */
    @Override
    protected void prepare() {
    }

    @Override
    protected void execute() {
        if (mTaskSharedData.getSyncSyncCallback() == null) {
            taskFailed("SyncSyncCallback is not ready");
            return;
        }

        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy != null && proxy.isConnected()) {
//            ConnectionManager.getInstance().subscribeConfigCompleted(mTaskSharedData.serialNumber, this);//FIXME:need this?
            proxy.startSyncing(this);
        } else {
            taskFailed("connection did not ready");
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
    }

    /* callback of ShineProfile.SyncCallback */
    @Override
    public void onSyncDataRead(Bundle extraInfo, MutableBoolean shouldStop) {
        float progress = extraInfo.getFloat(ShineProfile.SYNC_PROGRESS_KEY, 0.0f);
        Log.d(TAG, "progress=" + progress);
    }

    @Override
    public void onSyncDataReadCompleted(List<SyncResult> syncResults, MutableBoolean shouldStop) {
        Log.d(TAG, "onSyncDataReadCompleted: List<SyncResult> size is " + syncResults.size());
        mSyncResultSummary = syncResults;

        // if test the ShineSDK ShineProfile SyncCallback result
        if (mTaskSharedData.getSyncSyncCallback() != null) {
            mTaskSharedData.getSyncSyncCallback().onShineProfileSyncReadDataCompleted(syncResults);
        }
    }

    @Override
    public void onSyncCompleted(ShineProfile.ActionResult resultCode) {
        handleOnSyncCompleted(resultCode);
    }

    private void handleOnSyncCompleted(ActionResult resultCode) {
        boolean success = (mSyncResultSummary != null);
        Log.d(TAG, String.format("OnSyncCompleted callback is received, result is %s",Boolean.toString(success)));

        if (success) {
            handleOnShineSdkSyncSucceed();
        } else {
            handleOnShineSdkSyncFailed();
        }
    }

    private void handleOnShineSdkSyncSucceed() {
        SyncedDataCalculationTask syncCalculateTask = new SyncedDataCalculationTask(mSyncResultSummary);
        syncCalculateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void handleOnShineSdkSyncFailed() {
        Log.d(TAG, "handleOnShineSdkSyncFailed()");
        taskFailed("ShineSDK sync failed");
    }

    private class SyncedDataCalculationTask extends AsyncTask<Void, Void, Void> {

        private List<SyncResult> rawSyncDataList;
        private SyncResult syncResult;

        public SyncedDataCalculationTask(List<SyncResult> rawSyncDataFiles) {
            this.rawSyncDataList = new ArrayList<>(rawSyncDataFiles.size());
            for (SyncResult syncResult : rawSyncDataFiles) {
                if (syncResult != null) {
                    this.rawSyncDataList.add(syncResult);
                } else {
                    Log.d(TAG, "Null sync result found.");
                }
            }
            this.syncResult = new SyncResult();
        }

        @Override
        protected Void doInBackground(Void... params) {
            sortRawData();
            filterRawData();
            saveMisfitSyncData(syncResult);
            return null;
        }

        /**
         * sort the raw data
         */
        private void sortRawData() {
            Log.d(TAG, "sortRawData()");
            if (CheckUtils.isCollectionEmpty(rawSyncDataList)) {
                return;
            }
            AlgorithmUtils.sortSyncResultList(rawSyncDataList);
            for (int i = 1; i < rawSyncDataList.size(); i++) {
                AlgorithmUtils.handleNotContinuousActivities(rawSyncDataList.get(i - 1), rawSyncDataList.get(i));
            }
            syncResult = AlgorithmUtils.mergeSyncResults(rawSyncDataList);
        }

        /**
         * filter the raw data with lastSyncTime
         */
        private void filterRawData() {
            Log.d(TAG, "filterRawData()");
            long lastSyncTime = getLastSyncTime();

            if (syncResult.mActivities.isEmpty()) return;  // if no data in syncResult.mActivities, nothing to filter

            final int n = syncResult.mActivities.size();

            // if lastSyncTime is later than current tail activity's start time, nothing to filter
            long tailActivityStartTime = syncResult.mActivities.get(n - 1).mStartTimestamp;
            if (tailActivityStartTime < lastSyncTime) {
                Log.d(TAG, "No data is newer than last data synced, do not import those activities");
                return;
            }

            if (lastSyncTime == 0l) {
                Log.d(TAG, "Last sync time has not been saved before, so that nothing to filter");
                return;
            }
            AlgorithmUtils.filterSyncResultInternalData(syncResult, lastSyncTime);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.d(TAG, "Save sync data task onPostExecute " + System.currentTimeMillis());
        }
    }

    private void saveMisfitSyncData(SyncResult syncResult) {
        Log.d(TAG, "saveMisfitSyncData()");
        if (syncResult != null && !CheckUtils.isCollectionEmpty(syncResult.mActivities)) {
            boolean supportActivityTagging = mTaskSharedData.supportSettingsElement(
                    SettingsElement.ACTIVITY_TAGGING);
            boolean supportStream = mTaskSharedData.isStreamingSupported();

            if (mTaskSharedData.getDeviceType() == DeviceType.FLASH) {
                if (!CheckUtils.isCollectionEmpty(syncResult.mSessionEvents) && !supportActivityTagging) {
                    Log.d(TAG, String.format("Device do not support activity tagging, tags: %d", syncResult.mSessionEvents.size()));
                    syncResult.mSessionEvents.clear();
                }
                DailyUserDataBuilder.getInstance().buildDailyUserDataForFlash(syncResult, mTaskSharedData.getSyncCalculationCallback());
            } else {
                if (!CheckUtils.isCollectionEmpty(syncResult.mTapEventSummarys)) {
                    if (!supportActivityTagging && !supportStream) {
                        // FIXME, previous sync log
                        Log.d(TAG, String.format("Device do not support activity tagging and streaming, tags: %d", syncResult.mTapEventSummarys.size()));
                        syncResult.mTapEventSummarys.clear();
                    }
                }

                List<SdkActivitySessionGroup> sdkActivitySessionGroups =
                    DailyUserDataBuilder.getInstance().buildDailyUserDataForShine(syncResult, mTaskSharedData.getSyncCalculationCallback());
                if (mTaskSharedData.getSyncSyncCallback() != null) {
                    mTaskSharedData.getSyncSyncCallback().onSyncAndCalculationCompleted(sdkActivitySessionGroups);
                }
                taskSucceed();
            }
        }
    }

    /**
     * in Misfit app code, the LastSyncTime needs to save in SharedProference. it is not ready yet in SyncSDK
     * */
    private long getLastSyncTime() {
        return 0l;
    }
}