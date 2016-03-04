package com.misfit.syncsdk.task.state;

import com.misfit.syncsdk.task.OtaTask;

/**
 * ask App invoker whether to force OTA
 * */
public class AskAppForceOtaState extends State {

    private OtaTask otaTask;

    public AskAppForceOtaState(OtaTask otaTask) {
        this.otaTask = otaTask;
    }

    @Override
    public void execute() {
        if (otaTask.getSyncOtaCallback() != null) {
            otaTask.shouldForceOta(otaTask.getSyncOtaCallback().isForceOta(true));
            otaTask.gotoState(new PrepareOtaState(otaTask));
        } else {
            otaTask.onSucceed();
        }
    }

    @Override
    public void stop() {
    }
}
