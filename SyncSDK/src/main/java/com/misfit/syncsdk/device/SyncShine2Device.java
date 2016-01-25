package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.model.SettingsElement;
import com.misfit.syncsdk.model.TaskSharedData;

/**
 * Created by Will Hou on 1/13/16.
 */
public class SyncShine2Device extends SyncCommonDevice {
    public SyncShine2Device(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.PLUTO;
        mTaskSharedData = new TaskSharedData(mSerialNumber, mDeviceType);
    }

    @Override
    public boolean supportSettingsElement(SettingsElement element) {
        return element == SettingsElement.BATTERY
                || element == SettingsElement.WEARING_POSITION
                || element == SettingsElement.MOVE
                || element == SettingsElement.ALARM
                || element == SettingsElement.NOTIFICATION
                || element == SettingsElement.BUTTON
                || element == SettingsElement.CLOCK
                || element == SettingsElement.SERIAL_NUMBER
                || element == SettingsElement.SHOW_DEVICE;
    }
}
