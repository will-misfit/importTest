package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.model.SyncParams;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

/**
 * start user input streaming
 */
public class StartUserInputStreamingTask extends Task implements ShineProfile.StreamingCallback {
    private final static String TAG = "StartUserInputStreamingTask";

    SyncParams mSyncParams;
    ShineProfile.StreamingCallback mStreamingCallback = null;

    @Override
    protected void prepare() {
        mSyncParams = mTaskSharedData.getSyncParams();
        if (mSyncParams != null) {
            mStreamingCallback = mSyncParams.streamingCallback;
        }

        mLogEvent = GeneralUtils.createLogEvent(LogEventType.StartFileStreaming);
    }

    @Override
    protected void execute() {
        mLogEvent.start();

        if (mTaskSharedData.getDeviceType() != DeviceType.FLASH_LINK) {
            String msg = String.format("Ignore this task as it is not %s", DeviceType.getDeviceTypeText(DeviceType.FLASH_LINK));
            mLogEvent.end(LogEvent.RESULT_OTHER, msg);
            taskIgnored(msg);
            return;
        }

        ConnectionManager connectionManager = ConnectionManager.getInstance();
        ShineSdkProfileProxy proxy = connectionManager.getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null) {
            proxy = connectionManager.createShineProfileProxy(mTaskSharedData.getSerialNumber());
        }

        if (proxy.isStreaming()) {
            mLogEvent.end(LogEvent.RESULT_SUCCESS, "streaming now");
            taskSucceed();
            return;
        }

        updateExecuteTimer();
        proxy.startStreamingUserInputEvents(this);
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

    /* interface of StreamingCallback */
    @Override
    public void onStreamingStarted(ShineProfile.ActionResult actionResult) {
        MLog.d(TAG, "onStreamingStarted(), actionResult is " + actionResult);

        if (actionResult == ShineProfile.ActionResult.SUCCEEDED) {
            mLogEvent.end(LogEvent.RESULT_SUCCESS);
        } else {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "actionResult is " + actionResult);
        }

        if (mStreamingCallback != null) {
            mStreamingCallback.onStreamingStarted(actionResult);
        }
    }

    @Override
    public void onStreamingButtonEvent(final int eventType) {
        if (mStreamingCallback != null) {
            mStreamingCallback.onStreamingButtonEvent(eventType);
        }
    }

    @Override
    public void onStreamingStopped(ShineProfile.ActionResult actionResult) {
        if (mStreamingCallback != null) {
            mStreamingCallback.onStreamingStopped(actionResult);
        }
    }

    @Override
    public void onHeartbeatReceived() {
        if (mStreamingCallback != null) {
            mStreamingCallback.onHeartbeatReceived();
        }
    }
}
