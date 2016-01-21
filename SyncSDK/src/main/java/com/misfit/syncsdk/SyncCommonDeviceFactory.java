package com.misfit.syncsdk;

import com.misfit.ble.shine.ShineDevice;
import com.misfit.syncsdk.device.SyncCommonDevice;

/**
 * Created by Will-Hou on 1/11/16.
 */
public class SyncCommonDeviceFactory {
    public static SyncCommonDevice generateDevice(ShineDevice device) {
        return null;
    }  //for scanner, generate device with touchable device(has bluetoothDevice)

    public static SyncCommonDevice generateDevice(String serialNumber) {
        return null;
    }  //for DeviceManager
}
