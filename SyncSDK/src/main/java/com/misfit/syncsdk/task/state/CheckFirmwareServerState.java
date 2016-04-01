package com.misfit.syncsdk.task.state;

import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.utils.MLog;

/**
 * given model name, check latest firmware version on remote server
 * given current firmware version of device, decide whether need to OTA
 * */
public class CheckFirmwareServerState extends State implements FirmwareManager.CheckFirmwareServerListener {

    private static final String TAG = "CheckLatestFirmwareState";

    private OtaTask mOtaTask;
    boolean mShouldStop = false;
    private String mModelName;
    private String mFwVersion;

    public CheckFirmwareServerState(OtaTask otaTask, String modelName, String fwVersion) {
        this.mOtaTask = otaTask;
        this.mModelName = modelName;
        this.mFwVersion = fwVersion;
    }

    @Override
    public void execute() {
        mShouldStop = false;
        FirmwareManager.getInstance().checkLatestFirmware(mModelName, mFwVersion, this);
    }

    @Override
    public void stop() {
        FirmwareManager.getInstance().unsubscribeFirmwareCheck(this);
        mShouldStop = true;
    }

    @Override
    public void onSucceed(boolean shouldOta, String firmwareVersion) {
        FirmwareManager.getInstance().unsubscribeFirmwareCheck(this);
        MLog.d(TAG, String.format("shouldOta = %s, firmware version = %s", shouldOta, firmwareVersion));
        if (mShouldStop) {
            return;
        }
        if (shouldOta) {
            mOtaTask.setLatestFirmwareVersion(firmwareVersion);
            mOtaTask.gotoState(new AskAppForceOtaState(mOtaTask));
        } else {
            mOtaTask.onSucceed();
        }
    }

    @Override
    public void onFailed(int errorReason) {
        FirmwareManager.getInstance().unsubscribeFirmwareCheck(this);
        if (mShouldStop) {
            return;
        }
        mOtaTask.onFailed("get Latest firmware fail");
    }
}
