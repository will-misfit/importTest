package com.misfit.syncsdk.task.state;

import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.utils.MLog;

/**
 * check latest firmware version and get to know it should OTA or not
 * */
public class CheckLatestFirmwareState extends State implements FirmwareManager.CheckLatestFirmwareListener {

    private static final String TAG = "CheckLatestFirmwareState";

    private OtaTask mOtaTask;
    boolean mShouldStop = false;
    private String mModelName;
    private String mFwVersion;

    public CheckLatestFirmwareState(OtaTask otaTask, String modelName, String fwVersion) {
        this.mOtaTask = otaTask;
        this.mModelName = modelName;
        this.mFwVersion = fwVersion;
    }

    @Override
    public void execute() {
        mShouldStop = false;
        //TODO:check modelName & firmwareVersion ready
        //TODO:handle check latest firmware failed
        FirmwareManager.getInstance().shouldOta(mModelName, mFwVersion, this);
    }

    @Override
    public void stop() {
        mShouldStop = true;
    }

    @Override
    public void onSucceed(boolean shouldOta, String firmwareVersion) {
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
        if (mShouldStop) {
            return;
        }
        mOtaTask.onFailed("get Latest firmware fail");
    }
}
