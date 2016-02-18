package com.misfit.syncsdk.model;

import android.support.annotation.NonNull;

import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.syncsdk.callback.ReadDataCallback;
import com.misfit.syncsdk.callback.SyncAnimationCallback;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.device.DeviceBehavior;
import com.misfit.syncsdk.log.LogSession;
import com.misfit.syncsdk.utils.SdkConstants;

/**
 * to support data/context shared between Task instances, or between Task and Operator
 */
public class TaskSharedData {

    private String mSerialNumber;
    private String mModelName;
    private String mFirmwareVersion;

    private int mRemainingRetryCount = 0; // remaining retry count of Operator

    private ConfigurationSession mConfigurationSession;

    private int mDeviceType; // opt to update later

    private ReadDataCallback mReadDataCallback;
    private SyncCalculationCallback mSyncCalculationCallback;
    private SyncOtaCallback mSyncOtaCallback;
    private SyncAnimationCallback mSyncAnimationCallback;

    private DeviceBehavior mDeviceBehavior;

    private SyncSyncParams mSyncParams;

    private LogSession mLogSession;

    public TaskSharedData(String serialNumber, int deviceType) {
        this(serialNumber, deviceType, SdkConstants.OPERATOR_RETRY_TIMES);
    }

    public TaskSharedData(String serialNumber, int deviceType, int retryTimes) {
        mSerialNumber = serialNumber;
        mDeviceType = deviceType;
        mRemainingRetryCount = retryTimes;
    }

    public SyncSyncParams getSyncParams() {
        return mSyncParams;
    }

    public void setSyncParams(SyncSyncParams syncParams) {
        mSyncParams = syncParams;
        if(mLogSession == null) {
            mLogSession = new LogSession(syncParams.appVersion, syncParams.userId);
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

    public void setSyncCalculationCallback(SyncCalculationCallback syncCalculationCallback) {
        this.mSyncCalculationCallback = syncCalculationCallback;
    }

    public SyncCalculationCallback getSyncCalculationCallback() {
        return mSyncCalculationCallback;
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
        mDeviceBehavior = null;
        mConfigurationSession = null;
        mSyncCalculationCallback = null;
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
}
