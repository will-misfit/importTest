package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.model.TaskSharedData;

/**
 * Created by Will Hou on 1/13/16.
 */
public class SyncFlashDevice extends SyncCommonDevice {
    public SyncFlashDevice(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.FLASH;
        mTaskSharedData = new TaskSharedData(mSerialNumber, mDeviceType);
    }
}
