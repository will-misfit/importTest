package com.misfit.syncsdk.task;

import com.misfit.syncsdk.log.LogEventType;

/**
 * Task to set configuration
 */
public class SetConfigurationTask extends Task {
    @Override
    protected void prepare() {
        mLogEvent = createLogEvent(LogEventType.SET_CONFIGURATION);
    }

    @Override
    protected void execute() {
        taskSucceed();
    }

    @Override
    public void onStop() {

    }

    @Override
    protected void cleanup() {

    }
}
