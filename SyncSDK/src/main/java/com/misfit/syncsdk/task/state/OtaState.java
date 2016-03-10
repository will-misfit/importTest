package com.misfit.syncsdk.task.state;

import android.text.TextUtils;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.utils.LocalFileUtils;
import com.misfit.syncsdk.utils.MLog;

import java.util.TimerTask;

/**
 * execute OTA
 * */
public class OtaState extends State implements ShineProfile.OTACallback {
    private static final String TAG = "OtaState";

    private final static long TIMEOUT_OTA = 10 * 1000;

    private OtaTask otaTask;

    private String mOtaFileName;

    private SyncOtaCallback mSyncOtaCallback;

    //TODO:we can not unsubscribe the ota callback. that is why we use this variable.
    private boolean mIsStateFinished = false;

    public OtaState(OtaTask otaTask, String otaFileName) {
        this.otaTask = otaTask;
        this.mOtaFileName = otaFileName;
        mSyncOtaCallback = otaTask.getSyncOtaCallback();
    }

    @Override
    public void execute() {
        if (otaTask.needRetryOta()) {
            otaTask.shouldRetryOta(false);
            MLog.d(TAG, "retry ota");
        }

        MLog.d(TAG, "binary file name=" + mOtaFileName);
        if (TextUtils.isEmpty(mOtaFileName)) {
            otaTask.onFailed("firmware file name is empty");
            return;
        }

        byte[] firmwareData = LocalFileUtils.read(FirmwareManager.FIRMWARE_FOLDER, mOtaFileName);
        if (firmwareData == null) {
            otaTask.onFailed("file not ready");
            return;
        }

        MLog.d(TAG, "start ota, name=" + mOtaFileName + ", len=" + firmwareData.length);
        ShineSdkProfileProxy profileProxy = ConnectionManager.getInstance().getShineSDKProfileProxy(otaTask.getSerialNumber());
        if (profileProxy == null || !profileProxy.isConnected()) {
            otaTask.onFailed("proxy not ready");
            return;
        }

        setNewTimeOutTask(new TimerTask() {
            @Override
            public void run() {
                handleOtaCompleted(false);
            }
        }, TIMEOUT_OTA);
        profileProxy.startOTA(firmwareData, this);
    }

    @Override
    public void stop() {
        cancelCurrentTimeoutTask();
    }

    @Override
    public void onOTACompleted(ShineProfile.ActionResult resultCode) {
        MLog.d(TAG, "ota completed");
        cancelCurrentTimeoutTask();
        handleOtaCompleted(resultCode == ShineProfile.ActionResult.SUCCEEDED);
    }

    synchronized private void handleOtaCompleted(boolean isSucceed) {
        if (mIsStateFinished) {
            MLog.d(TAG, "state already finished");
            return;
        }
        mIsStateFinished = true;
        otaTask.shouldRetryOta(!isSucceed);
        if (isSucceed && mSyncOtaCallback != null) {
            mSyncOtaCallback.onOtaCompleted();
        }
        otaTask.gotoState(new WaitForConnectState(otaTask));
    }

    @Override
    public void onOTAProgressChanged(float progress) {
        MLog.d(TAG, "ota progress = " + progress);
        if (mIsStateFinished) {
            MLog.d(TAG, "state already finished");
            return;
        }
        if (mSyncOtaCallback != null) {
            mSyncOtaCallback.onOtaProgress(progress);
        }
    }
}
