package com.misfit.syncsdk.task;

import android.text.TextUtils;

import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;

public class CheckFirmwareTask extends Task {

    @Override
    protected void prepare() {
        mLogEvent = createLogEvent(LogEventType.CHECK_FIRMWARE);
    }

    @Override
    protected void execute() {
        String modelName = mTaskSharedData.getModelName();
        if (TextUtils.isEmpty(modelName)) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "model name is empty");
            taskIgnored("modelName is empty");
            return;
        }
        FirmwareManager firmwareManager = FirmwareManager.getInstance();
        // Callback necessary to write in log!
        firmwareManager.checkLatestFirmware(modelName, mTaskSharedData.getFirmwareVersion());
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
