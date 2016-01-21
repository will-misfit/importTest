package com.misfit.syncsdk.model;

import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.syncsdk.callback.SyncAnimationCallback;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.device.DeviceBehavior;
import com.misfit.syncsdk.utils.SdkConstants;

/**
 * to support data/context shared between Task instances, or between Task and Operator
 */
public class TaskSharedData {

    private String mSerialNumber;

    private int mRemainingRetryCount = 0; // remaining retry count of Operator

    private ConfigurationSession mConfigurationSession;

    private int mDeviceType; // opt to update later

    private SyncSyncCallback mSyncSyncCallback;

    private SyncAnimationCallback mSyncAnimationCallback;

    private DeviceBehavior mDeviceBehavior;

    public TaskSharedData(String serialNumber, int deviceType, DeviceBehavior deviceBehavior) {
        this(serialNumber, deviceType, deviceBehavior, SdkConstants.OPERATOR_RETRY_TIMES);
    }

    public TaskSharedData(String serialNumber, int deviceType, DeviceBehavior deviceBehavior, int retryTimes) {
        mSerialNumber = serialNumber;
        mDeviceType = deviceType;
        mDeviceBehavior = deviceBehavior;
        mRemainingRetryCount = retryTimes;
    }

    public int getDeviceType() {
        return mDeviceType;
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }

    public void setSyncSyncCallback(SyncSyncCallback syncSyncCallback) {
        this.mSyncSyncCallback = syncSyncCallback;
    }

    public SyncSyncCallback getSyncSyncCallback() {
        return mSyncSyncCallback;
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
        mSyncSyncCallback = null;
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
}
