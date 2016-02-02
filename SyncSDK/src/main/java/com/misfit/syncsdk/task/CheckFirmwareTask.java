package com.misfit.syncsdk.task;

import android.text.TextUtils;

import com.misfit.syncsdk.FirmwareManager;

public class CheckFirmwareTask extends Task {

    @Override
    protected void prepare() {

    }

    @Override
    public boolean couldIgnoreResult() {
        return true;
    }

    @Override
    protected void execute() {
        String modelName = mTaskSharedData.getModelName();
        if (TextUtils.isDigitsOnly(modelName)) {
            taskFailed("modelName is empty");
            return;
        }
        FirmwareManager firmwareManager = FirmwareManager.getInstance();
        firmwareManager.checkLatestFirmware(modelName, mTaskSharedData.getFirmwareVersion());
    }

    @Override
    protected void onStop() {
    }

    @Override
    protected void cleanup() {

    }
}
