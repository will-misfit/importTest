package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.DeviceType;

/**
 * subclass of SyncCommonDevice for Swarovski Shine
 */
public class SyncSwarovskiDevice extends SyncCommonDevice {
    public SyncSwarovskiDevice(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.SWAROVSKI_SHINE;
    }
}
