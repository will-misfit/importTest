package com.misfit.syncsdk.model;

import android.support.annotation.NonNull;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.callback.ConnectionStateCallback;
import com.misfit.syncsdk.callback.ReadDataCallback;
import com.misfit.syncsdk.callback.SyncAnimationCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.device.DeviceBehavior;
import com.misfit.syncsdk.enums.FailedReason;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.log.LogSession;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.SdkConstants;

/**
 * to support data/context shared between Task instances, or between Task and Operator
 */
public class TaskSharedData {

    private String mSerialNumber;
    private String mModelName;
    private String mFirmwareVersion;

    private int mRemainingRetryCount = 0; // remaining retry count of Operator

    private int mDeviceType; // opt to update later

    /* callback with App invoker, from startSync() arguments */
    private ReadDataCallback mReadDataCallback;
    private SyncOtaCallback mSyncOtaCallback;
    private SyncAnimationCallback mSyncAnimationCallback;
    private DeviceBehavior mDeviceBehavior;

    /* sync parameters from App invoker, from startSync() arguments */
    private SyncParams mSyncParams;

    /* post sync data from App invoker, it will be set when calculate completes */
    private PostCalculateData mPostCalculateData;

    /* result of getConfigurationTask, used as old ShineConfiguration */
    private ConfigurationSession mConfigurationSession;

    private LogSession mLogSession;

    /**
     * ConnectionStateCallback to monitor connection state change(mainly Disconnected) during entire sync process
     *
     * if Disconnected unexpectedly, record it in LogSession - FailureReason field.
     * other Task subclass will notice the unexpected Disconnected as ShineProfile.isConnected() return false
     * */
    private ConnectionStateCallback mConnectionStateCallbackPostConnect = new ConnectionStateCallback() {
        @Override
        public void onConnectionStateChanged(ShineProfile.State newState) {
            if (newState == ShineProfile.State.DISCONNECTED) {
                setFailureReasonInLogSession(FailedReason.DISCONNECTED_UNEXPECTEDLY);
            } else {
                // when the Sync process runs after connect, we don't care other State except Disconnected
                LogEvent logEvent = GeneralUtils.createLogEvent(LogEventType.UNEXPECTED_CONNECTION_STATE);
                mLogSession.appendEvent(logEvent);
            }
        }
    };

    public TaskSharedData(String serialNumber, int deviceType) {
        this(serialNumber, deviceType, SdkConstants.OPERATOR_RETRY_TIMES);
    }

    public TaskSharedData(String serialNumber, int deviceType, int retryTimes) {
        mSerialNumber = serialNumber;
        mDeviceType = deviceType;
        mRemainingRetryCount = retryTimes;
    }

    public SyncParams getSyncParams() {
        return mSyncParams;
    }

    public void setSyncParams(SyncParams syncParams) {
        mSyncParams = syncParams;
        // now mLogSession should not be initialized yet
        if(mLogSession == null) {
            mLogSession = new LogSession(syncParams.appVersion, syncParams.userId);
            mLogSession.setSerialNumber(mSerialNumber);
            mLogSession.save();    // as long as data update, save LogSession to local file
        }
    }

    public String getModelName() {
        return mModelName;
    }

    public void setModelName(String mModelName) {
        this.mModelName = mModelName;
    }

    public String getFirmwareVersion() {
        return mFirmwareVersion;
    }

    public void setFirmwareVersion(String mFirmwareVersion) {
        this.mFirmwareVersion = mFirmwareVersion;
    }

    public int getDeviceType() {
        return mDeviceType;
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }

    public SyncOtaCallback getSyncOtaCallback() {
        return mSyncOtaCallback;
    }

    public void setDeviceBehavior(@NonNull DeviceBehavior deviceBehavior) {
        this.mDeviceBehavior = deviceBehavior;
    }

    public void setSyncOtaCallback(SyncOtaCallback mSyncOtaCallback) {
        this.mSyncOtaCallback = mSyncOtaCallback;
    }

    public void setReadDataCallback(ReadDataCallback readDataCallback) {
        this.mReadDataCallback = readDataCallback;
    }

    public ReadDataCallback getReadDataCallback() {
        return this.mReadDataCallback;
    }

    public void setSyncAnimationCallback(SyncAnimationCallback mSyncAnimationCallback) {
        this.mSyncAnimationCallback = mSyncAnimationCallback;
    }

    public SyncAnimationCallback getSyncAnimationCallback() {
        return mSyncAnimationCallback;
    }

    public void setConfigurationSession(ConfigurationSession mConfigurationSession) {
        this.mConfigurationSession = mConfigurationSession;
    }

    public ConfigurationSession getConfigurationSession() {
        return mConfigurationSession;
    }

    public PostCalculateData getPostCalculateDate() {
        return mPostCalculateData;
    }

    public void setPostCalculateData(PostCalculateData postCalculateData) {
        mPostCalculateData = postCalculateData;
    }

    public int getRemainingRetryCount() {
        return mRemainingRetryCount;
    }

    public int consumeRetryCount() {
        if (mRemainingRetryCount == 0) {
            return -1;
        }
        return --mRemainingRetryCount;
    }

    public void cleanUp() {
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mSerialNumber);
        if (proxy != null) {
            proxy.unsubscribeConnectionStateChanged(mConnectionStateCallbackPostConnect);
        }

        mDeviceBehavior = null;
        mConfigurationSession = null;
        mSyncAnimationCallback = null;
    }

    public boolean supportSettingsElement(SettingsElement settingsElement) {
        if (mDeviceBehavior != null) {
            return mDeviceBehavior.supportSettingsElement(settingsElement);
        } else {
            return false;
        }
    }

    public boolean isStreamingSupported() {
        if (mDeviceBehavior != null) {
            return mDeviceBehavior.isStreamingSupported();
        } else {
            return false;
        }
    }

    public LogSession getLogSession() {
        return mLogSession;
    }

    public ConnectionStateCallback getPostConnectConnectionStateCallback() {
        return mConnectionStateCallbackPostConnect;
    }

    public void setFailureReasonInLogSession(int failureReasonId) {
        if (mLogSession != null) {
            mLogSession.setFailureReason(failureReasonId);
            mLogSession.save();
        }
    }
}
