package com.misfit.syncsdk;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.device.SyncFlashDevice;
import com.misfit.syncsdk.device.SyncIwcDevice;
import com.misfit.syncsdk.device.SyncRayDevice;
import com.misfit.syncsdk.device.SyncShine2Device;
import com.misfit.syncsdk.device.SyncShineDevice;
import com.misfit.syncsdk.device.SyncSwarovskiDevice;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Will-Hou on 1/11/16.
 */
public class MisfitDeviceManager {

    private static MisfitDeviceManager sharedInstance;

    private Map<String, SyncCommonDevice> syncCommonDeviceMap;

    private MisfitDeviceManager() {
        syncCommonDeviceMap = new HashMap<>();
    }

    public static MisfitDeviceManager getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new MisfitDeviceManager();
        }
        return sharedInstance;
    }

    // TODO: when to remove SyncCommonDevice item
    public SyncCommonDevice getSpecificDevice(@NonNull String serialNumber) {
        if (syncCommonDeviceMap.containsKey(serialNumber)) {
            return syncCommonDeviceMap.get(serialNumber);
        }

        int deviceType = DeviceType.getDeviceType(serialNumber);
        SyncCommonDevice result;
        switch(deviceType) {
            case DeviceType.SHINE:
                result = new SyncShineDevice(serialNumber);
                break;
            case DeviceType.PLUTO:
                result = new SyncShine2Device(serialNumber);
                break;
            case DeviceType.FLASH:
                result = new SyncFlashDevice(serialNumber);
                break;
            case DeviceType.SPEEDO_SHINE:
            case DeviceType.SHINE_MK_II:
                result = new SyncShineDevice(serialNumber);
                break;
            case DeviceType.BMW:
                result = new SyncRayDevice(serialNumber);
                break;
            case DeviceType.SILVRETTA:
                result = new SyncIwcDevice(serialNumber);
                break;
            case DeviceType.SWAROVSKI_SHINE:
                result = new SyncSwarovskiDevice(serialNumber);
                break;
            default:
                result = new SyncShineDevice(serialNumber);
                break;
        }
        syncCommonDeviceMap.put(serialNumber, result);
        return result;
    }
}
