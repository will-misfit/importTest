package com.misfit.syncsdk.task;

import android.text.TextUtils;

import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.GeneralUtils;

/**
 * CheckFirmwareTask is special in entire task list: its execution result does not need to wait
 * */
public class CheckFirmwareTask extends Task {

    private static final String TAG = "CheckFirmwareTask";

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.CHECK_FIRMWARE);
    }

    @Override
    protected void execute() {
        mLogEvent.start();
        String modelName = mTaskSharedData.getModelName();
        if (TextUtils.isEmpty(modelName)) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "model name is empty");
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
