package com.misfit.syncsdk.task;

import android.text.TextUtils;
import android.util.Log;

import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;

/**
 * CheckFirmwareTask's execution does not need to wait for result
 * */
public class CheckFirmwareTask extends Task {

    private static final String TAG = "CheckFirmwareTask";

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.CHECK_FIRMWARE);
    }

    @Override
    protected void execute() {
        MLog.d(TAG, "execute() enters");
        mLogEvent.start();
        String modelName = mTaskSharedData.getModelName();
        if (TextUtils.isEmpty(modelName)) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "model name is empty");
            Log.d(TAG, "execute(), modelName is null");
            taskIgnored("modelName is empty");
            return;
        }

        FirmwareManager firmwareManager = FirmwareManager.getInstance();
        firmwareManager.checkLatestFirmware(modelName, mTaskSharedData.getFirmwareVersion());
        mLogEvent.end(LogEvent.RESULT_SUCCESS, String.format("%s started", TAG));
        taskSucceed();
    }

    @Override
    protected void onStop() {
    }

    @Override
    protected void cleanup() {
        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;
    }
}
