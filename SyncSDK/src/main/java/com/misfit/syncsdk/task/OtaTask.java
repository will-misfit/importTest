package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.OtaType;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.callback.SyncOtaCallback;


/**
 * Created by Will Hou on 1/13/16.
 */
public class OtaTask extends Task implements ShineProfile.OTACallback {

    SyncOtaCallback mSyncOtaCallback;
    String mLatestFirmwareVersion;
    String mCurrFirmwareVersion;
    int mDeviceType;

    FirmwareManager.GetLatestFirmwareListener mGetLatestFirmwareListener = new FirmwareManager.GetLatestFirmwareListener() {
        @Override
        public void onSucceed(boolean isSuccessful, String latestFirmware) {
            mLatestFirmwareVersion = latestFirmware;
            getSuggestionFromApp();
        }

        @Override
        public void onFailed(int errorReason) {
            //TODO:failed?
        }
    };

    FirmwareManager.DownloadLatestFirmwareListener mDownloadFirmwareListener = new FirmwareManager.DownloadLatestFirmwareListener() {
        @Override
        public void onFinished(String filePath) {
            startOta();
        }

        @Override
        public void onFailed(int errorReason) {
            //TODO:force ota failed.
        }
    };

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        //TODO:get current firmware version

        //get latest firmware version
        FirmwareManager.getInstance().checkLatestFirmware("shine", mCurrFirmwareVersion);
    }

    private void getSuggestionFromApp() {
        //get ota type
        boolean hasNewFirmware = FirmwareManager.getInstance().shouldUpdate(mDeviceType, mCurrFirmwareVersion, mLatestFirmwareVersion);
        if (mSyncOtaCallback != null) {
            int otaType = mSyncOtaCallback.getOtaSuggestion(hasNewFirmware);
            beforeOta(otaType);
        } else {
            taskSucceed();
        }
    }

    private void beforeOta(int otaType) {
        FirmwareManager firmwareManager = FirmwareManager.getInstance();
        switch (otaType) {
            case OtaType.NO_NEED_TO_OTA:
                taskSucceed();
                break;
            case OtaType.NEED_OTA:
                if (firmwareManager.isNewFirmwareReadyNow(mLatestFirmwareVersion)) {
                    startOta();
                } else {
                    taskSucceed();  //skip this time
                }
                break;
            case OtaType.FORCE_OTA:
                // TODO
                break;
            default:
                //TODO:Log as unexpected event
                break;
        }
    }

    private void startOta() {
        //get ota file
        byte[] firmwareData = null;
        //start ota file
        ShineSdkProfileProxy profileProxy = null;
        profileProxy.startOTA(firmwareData, this);
    }

    @Override
    public void stop() {
        super.stop();
        ShineSdkProfileProxy profileProxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (profileProxy != null) {
            profileProxy.interruptCurrentAction();
        } else {
            //TODO:log
        }
    }

    @Override
    public void onStop() {

    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void onOTACompleted(ShineProfile.ActionResult resultCode) {
        //TODO:log
        if (mSyncOtaCallback != null) {
            mSyncOtaCallback.onOtaCompleted();
        }
    }

    @Override
    public void onOTAProgressChanged(float progress) {
        //TODO:log
        if (mSyncOtaCallback != null) {
            mSyncOtaCallback.onOtaProgress(progress);
        }
    }
}
