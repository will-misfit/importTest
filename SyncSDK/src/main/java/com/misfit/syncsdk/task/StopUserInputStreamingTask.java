package com.misfit.syncsdk.task;

import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.GeneralUtils;

/**
 * stop user input streaming
 */
public class StopUserInputStreamingTask extends Task {
    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.STOP_FILE_STREAMING);
    }

    @Override
    protected void execute() {
        mLogEvent.start();

        ConnectionManager connectionManager = ConnectionManager.getInstance();
        ShineSdkProfileProxy proxy = connectionManager.getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null) {
            proxy = connectionManager.createShineProfileProxy(mTaskSharedData.getSerialNumber());
        }

        if (!proxy.isConnected()) {
            String failReason = "ShineSdkProxy is not connected";
            mLogEvent.end(LogEvent.RESULT_FAILURE, failReason);
            taskFailed(failReason);
            return;
        } else if (!proxy.isStreaming()) {
            mLogEvent.end(LogEvent.RESULT_SUCCESS);
            taskSucceed();
        }

        // TODO: StreamingCallback is set during startUserInputStreaming(), so the callback of onStreamingStopped() cannot be caught here
        proxy.interruptCurrentAction();
        mLogEvent.end(LogEvent.RESULT_SUCCESS);
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;
    }
}
