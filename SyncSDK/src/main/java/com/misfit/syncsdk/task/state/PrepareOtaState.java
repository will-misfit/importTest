package com.misfit.syncsdk.task.state;

import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.utils.MLog;

/**
 * get to confirm whether latest firmware is ready or not
 * if it is not ready yet while unnecessary to OTA, skip OTA
 * */
public class PrepareOtaState extends State implements FirmwareManager.DownloadLatestFirmwareListener {

    private static final String TAG = "PrepareOtaState";

    private OtaTask otaTask;

    public PrepareOtaState(OtaTask otaTask) {
        this.otaTask = otaTask;
    }

    @Override
    public void execute() {
        MLog.d(TAG, String.format("Force OTA = ", Boolean.toString(otaTask.ifForceOta())));

        FirmwareManager firmwareManager = FirmwareManager.getInstance();

        if (firmwareManager.isFirmwareExisting(otaTask.getLatestFirmwareVersion())) {
            String fileName = FirmwareManager.getFirmwareFileName(otaTask.getLatestFirmwareVersion());
            otaTask.gotoState(new OtaState(otaTask, fileName));
        } else if (otaTask.ifForceOta()) {
            firmwareManager.onFirmwareReady(otaTask.getLatestFirmwareVersion(), this);
        } else {
            otaTask.onSucceed();  // skip OTA
        }
    }

    @Override
    // TODO: clean the callback assign to FirmwareManager
    public void stop() {
    }

    @Override
    // TODO: clean the callback assign to FirmwareManager
    public void onSucceed(String fileName) {
        MLog.d(TAG, "firmware ready, fileName=" + fileName);
        otaTask.gotoState(new OtaState(otaTask, fileName));
    }

    @Override
    // TODO: clean the callback assign to FirmwareManager
    public void onFailed(int errorReason) {
        otaTask.onFailed("download not ok");
    }
}
