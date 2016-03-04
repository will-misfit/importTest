package com.misfit.syncsdk.task;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfile.ActionResult;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.ble.util.MutableBoolean;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.algorithm.AlgorithmUtils;
import com.misfit.syncsdk.algorithm.DailyUserDataBuilder;
import com.misfit.syncsdk.enums.FailedReason;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.model.PostCalculateData;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;
import com.misfit.syncsdk.model.SettingsElement;
import com.misfit.syncsdk.utils.CheckUtils;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

import java.util.ArrayList;
import java.util.List;


/**
 * Task implementation to call sync() of ShineSDK, and complete the calculation with algorithm library
 * this task covers LogEvent of GetActivity and Calculate.
 */
public class SyncAndCalculateTask extends Task implements ShineProfile.SyncCallback {

    private final static String TAG = "SyncAndCalculateTask";

    private List<SyncResult> mSyncResultSummary;

    private Handler mMainHander = new Handler(Looper.getMainLooper());

    /* interface of Task */
    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.GET_ACTIVITY);
    }

    @Override
    protected void execute() {
        mLogEvent.start();
        MLog.d(TAG, "execute()");

        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            MLog.d(TAG, String.format("execute(), ShineSdkProfileProxy not ready"));
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProfileProxy is not ready");
            mTaskSharedData.setFailureReasonInLogSession(FailedReason.SYNC_FAIL);
            taskFailed("proxy not prepared");
            return;
        }

        if (mTaskSharedData.getReadDataCallback() == null) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ReadDataCallback is not ready");
            mTaskSharedData.setFailureReasonInLogSession(FailedReason.SYNC_FAIL);
            taskFailed("ReadDataCallback is not ready");
            return;
        }

        cancelCurrentTimerTask();
        mCurrTimerTask = createTimeoutTask();
        TimerManager.getInstance().addTimerTask(mCurrTimerTask, SdkConstants.READ_DATA_TIMEOUT);

        proxy.startSyncing(this);
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
        cancelCurrentTimerTask();
        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;
    }

    /* callback of ShineProfile.SyncCallback */
    @Override
    public void onSyncDataRead(Bundle extraInfo, MutableBoolean shouldStop) {
        final float progress = extraInfo.getFloat(ShineProfile.SYNC_PROGRESS_KEY, 0.0f);
        mMainHander.post(new Runnable() {
            @Override
            public void run() {
                MLog.d(TAG, String.format("progress %.2f", progress));
            }
        });
    }

    @Override
    public void onSyncDataReadCompleted(final List<SyncResult> syncResults, MutableBoolean shouldStop) {
        mMainHander.post(new Runnable() {
            @Override
            public void run() {
                MLog.d(TAG, "onSyncDataReadCompleted: List<SyncResult> size is " + syncResults.size());
                mSyncResultSummary = syncResults;

                // if test the ShineSDK ShineProfile SyncCallback result
                if (mTaskSharedData.getReadDataCallback() != null) {
                    mTaskSharedData.getReadDataCallback().onRawDataReadCompleted(syncResults);
                }
            }
        });
    }

    @Override
    public void onSyncCompleted(final ShineProfile.ActionResult resultCode) {
        mMainHander.post(new Runnable() {
            @Override
            public void run() {
                handleOnSyncCompleted(resultCode);
            }
        });
    }

    private void handleOnSyncCompleted(ActionResult resultCode) {
        boolean success = (mSyncResultSummary != null);
        MLog.d(TAG, String.format("OnSyncCompleted callback is received, result is %s", Boolean.toString(success)));

        if (success) {
            mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
            mLogSession.appendEvent(mLogEvent);
            mLogEvent = null;
            handleOnShineSdkSyncSucceed();
        } else {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "SyncResultSummary is null");
            mLogSession.appendEvent(mLogEvent);
            mLogEvent = null;
            handleOnShineSdkSyncFailed();
        }
    }

    private void handleOnShineSdkSyncSucceed() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.CALCULATE);
        mLogEvent.start();

        if (CheckUtils.isCollectionEmpty(mSyncResultSummary)) {
            mLogEvent.end(LogEvent.RESULT_SUCCESS, "List<SyncResult> is empty");
            taskSucceed();
            return;
        }

        SyncedDataCalculationTask syncCalculateTask = new SyncedDataCalculationTask(mSyncResultSummary);
        syncCalculateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void handleOnShineSdkSyncFailed() {
        MLog.d(TAG, "handleOnShineSdkSyncFailed()");
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
                    MLog.d(TAG, "Null sync result found.");
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
            long lastSyncTime = mTaskSharedData.getSyncParams().lastSyncTime;

            if (syncResult.mActivities.isEmpty()) {
                return;  // if no data in syncResult.mActivities, nothing to filter
            }
            final int n = syncResult.mActivities.size();

            // if lastSyncTime is later than current tail activity's start time, nothing to filter
            long tailActivityStartTime = syncResult.mActivities.get(n - 1).mStartTimestamp;
            if (tailActivityStartTime < lastSyncTime) {
                MLog.d(TAG, "No data is newer than last data synced, do not import those activities");
                return;
            }

            if (lastSyncTime == 0l) {
                MLog.d(TAG, "Last sync time has not been saved before, so that nothing to filter");
                return;
            }
            AlgorithmUtils.filterSyncResultInternalData(syncResult, lastSyncTime);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            taskSucceed();
        }
    }

    private void saveMisfitSyncData(SyncResult syncResult) {
        MLog.d(TAG, String.format("saveMisfitSyncData(), syncResult size %d", syncResult.mActivities.size()));

        if (syncResult != null && !CheckUtils.isCollectionEmpty(syncResult.mActivities)) {
            boolean supportActivityTagging = mTaskSharedData.supportSettingsElement(
                    SettingsElement.ACTIVITY_TAGGING);
            boolean supportStream = mTaskSharedData.isStreamingSupported();

            if (mTaskSharedData.getDeviceType() == DeviceType.FLASH) {
                if (!CheckUtils.isCollectionEmpty(syncResult.mSessionEvents) && !supportActivityTagging) {
                    MLog.d(TAG, String.format("Device do not support activity tagging, tags: %d", syncResult.mSessionEvents.size()));
                    syncResult.mSessionEvents.clear();
                }
                SdkActivitySessionGroup sdkActivitySessionGroup = DailyUserDataBuilder.getInstance().buildUserDataForFlash(syncResult,
                    mTaskSharedData.getSyncParams().settingsChangeListSinceLastSync,
                    mTaskSharedData.getSyncParams().userProfile);
                if (mTaskSharedData.getReadDataCallback() != null) {
                    PostCalculateData postCalculateData = mTaskSharedData.getReadDataCallback().onDataCalculateCompleted(sdkActivitySessionGroup);
                    mTaskSharedData.setPostCalculateData(postCalculateData);
                }
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "ActivitySessionGroup is built up");
            } else {
                if (!CheckUtils.isCollectionEmpty(syncResult.mTapEventSummarys)) {
                    if (!supportActivityTagging && !supportStream) {
                        MLog.d(TAG, String.format("Device do not support activity tagging and streaming, tags: %d", syncResult.mTapEventSummarys.size()));
                        syncResult.mTapEventSummarys.clear();
                    }
                }

                SdkActivitySessionGroup sdkActivitySessionGroup = DailyUserDataBuilder.getInstance().buildUserDataForShine(syncResult,
                    mTaskSharedData.getSyncParams().settingsChangeListSinceLastSync,
                    mTaskSharedData.getSyncParams().userProfile);
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
                if (mTaskSharedData.getReadDataCallback() != null) {
                    PostCalculateData postCalculateData = mTaskSharedData.getReadDataCallback().onDataCalculateCompleted(sdkActivitySessionGroup);
                    mTaskSharedData.setPostCalculateData(postCalculateData);
                }
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "ActivitySessionGroup is built up");
            }
        }
    }
}