package com.misfit.syncsdk.task;

import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.task.state.CheckLatestFirmwareState;
import com.misfit.syncsdk.task.state.PrepareOtaState;
import com.misfit.syncsdk.task.state.State;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;

/**
 * OtaTask is composed of several State instances, which works one by one in order as below:
 *  GetLatestFirmwareState
 *  AskAppSuggestionState
 *  PrepareOtaState
 *  OtaState
 *  WaitForConnectState
 *  ReconnectState
 * */
public class OtaTask extends Task {

    private final static String TAG = "OtaTask";

    private State mCurrState;

    private String mLatestFirmwareVersion;

    private boolean mRetryOta = false;

    private boolean mForceOta = false;

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.OTA);
    }

    @Override
    protected void execute() {
        mLogEvent.start();
        if (mRetryOta) {
            gotoState(new PrepareOtaState(this));
        } else {
            gotoState(new CheckLatestFirmwareState(this, mTaskSharedData.getModelName(), mTaskSharedData.getFirmwareVersion()));
        }
    }

    public void gotoState(State state) {
        if (mIsFinished.get()) {
            return;
        }
        MLog.d(TAG, String.format("go to state = %s", state.getClass().getSimpleName()));
        mCurrState = state;
        state.execute();
    }

    @Override
    public void stop() {
        mCurrState.stop();
        super.stop();
        ShineSdkProfileProxy profileProxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (profileProxy != null) {
            profileProxy.interruptCurrentAction();
        } else {
            //TODO:MLog
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
    }

    public void setLatestFirmwareVersion(String latestFirmwareVersion) {
        this.mLatestFirmwareVersion = latestFirmwareVersion;
    }

    public String getLatestFirmwareVersion() {
        return this.mLatestFirmwareVersion;
    }

    public void shouldForceOta(boolean forceOta) {
        this.mForceOta = forceOta;
    }

    public boolean ifForceOta() {
        return this.mForceOta;
    }

    /* API open to State subclass to notify the operation result */
    public void onSucceed() {
        taskSucceed();
    }

    public void onFailed(String reason) {
        taskFailed(reason);
    }

    public void startRetry() {
        retry();
    }

    public SyncOtaCallback getSyncOtaCallback() {
        return mTaskSharedData.getSyncOtaCallback();
    }

    public String getSerialNumber() {
        return mTaskSharedData.getSerialNumber();
    }

    public void cancelTimerTask() {
        cancelCurrentTimerTask();
    }

    public boolean needRetryOta() {
        return mRetryOta;
    }

    public void shouldRetryOta(boolean shouldOrNot) {
        mRetryOta = shouldOrNot;
    }

}
