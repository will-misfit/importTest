package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.DeviceType;

/**
 * Created by Will Hou on 1/13/16.
 */
public class SyncRayDevice extends SyncCommonDevice {
    public SyncRayDevice(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.BMW;
    }
}
