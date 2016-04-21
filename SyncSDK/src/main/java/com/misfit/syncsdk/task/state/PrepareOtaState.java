package com.misfit.syncsdk.task.state;

import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.utils.MLog;

/**
 * if latest firmware is existing now, OTA now
 * or if firmware download is not done, wait for its complete if necessary(force OTA)
 * or else, skip OtaTask
 * */
public class PrepareOtaState extends State implements FirmwareManager.DownloadFirmwareListener {

    private static final String TAG = "PrepareOtaState";

    private OtaTask otaTask;

    public PrepareOtaState(OtaTask otaTask) {
        this.otaTask = otaTask;
    }

    @Override
    public void execute() {
        MLog.d(TAG, String.format("Force OTA = ", Boolean.toString(otaTask.ifForceOta())));

        if (FirmwareManager.isFirmwareExisting(otaTask.getLatestFirmwareVersion())) {
            String fileName = FirmwareManager.getFirmwareFileName(otaTask.getLatestFirmwareVersion());
            otaTask.gotoState(new OtaState(otaTask, fileName));
        } else if (otaTask.ifForceOta()) {
            FirmwareManager.getInstance().subscribeFirmwareDownload(this);
        } else {
            otaTask.onSucceed();
        }
    }

    @Override
    public void stop() {
        FirmwareManager.getInstance().unsubscribeFirmwareDownload(this);
    }

    @Override
    public void onSucceed(String fileName) {
        FirmwareManager.getInstance().unsubscribeFirmwareDownload(this);
        MLog.d(TAG, "firmware ready, fileName=" + fileName);
        otaTask.gotoState(new OtaState(otaTask, fileName));
    }

    @Override
    public void onFailed(int errorReason) {
        FirmwareManager.getInstance().unsubscribeFirmwareDownload(this);
        otaTask.onFailed("download not ok");
    }
}
