package com.misfit.syncsdk.task;

import android.text.TextUtils;

import com.misfit.syncsdk.FirmwareManager;

public class CheckFirmwareTask extends Task {

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        String modelName = mTaskSharedData.getModelName();
        if (TextUtils.isEmpty(modelName)) {
            taskIgnored("modelName is empty");
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
