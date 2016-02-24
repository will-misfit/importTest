package com.misfit.syncsdk;

import android.text.TextUtils;

/**
 * Created by Will Hou on 1/11/16.
 */
public class DeviceType {
    public final static int UNKNOWN = 0;
    public final static int SHINE = 1;
    public final static int FLASH = 2;
    public final static int SWAROVSKI_SHINE = 3;
    public final static int SPEEDO_SHINE = 4;
    public final static int SHINE_MK_II = 5;
    public final static int PLUTO = 6;
    public final static int SILVERATTA = 7;
    public final static int RAY = 9;

    private int mTypeVal;

    DeviceType(int typeVal) {
        mTypeVal = typeVal;
    }

    public static int getDeviceType(String serialNumber) {
        if (TextUtils.isEmpty(serialNumber)) {
            return DeviceType.UNKNOWN;
        } else if (serialNumber.startsWith("SH")) {
            return DeviceType.SHINE;
        } else if (serialNumber.startsWith("F")) {
            return DeviceType.FLASH;
        }else if (serialNumber.startsWith("SC")) {
            return DeviceType.SWAROVSKI_SHINE;
        } else if (serialNumber.startsWith("B0")) {
            return DeviceType.RAY;
        } else if (serialNumber.startsWith("S2")) {
            return DeviceType.PLUTO;
        } else if (serialNumber.startsWith("C1")) {
            return DeviceType.SILVERATTA;
        } else if (serialNumber.startsWith("SV0EZ")) {
            return DeviceType.SPEEDO_SHINE;
        } else if (serialNumber.startsWith("SV")) {
            return DeviceType.SHINE_MK_II;
        } else {
            return DeviceType.UNKNOWN;
        }
        //TODO:We need to classify the flash and button by model name
    }

    public static String getDeviceTypeText(int deviceType) {
        switch (deviceType) {
            case UNKNOWN:
                return "unknown";
            case SHINE:
                return "shine";
            case FLASH:
                return "flash";
            case SWAROVSKI_SHINE:
                return "swarovski_shine";
            case SPEEDO_SHINE:
                return "speedo_shine";
            case SHINE_MK_II:
                return "shine_mk_ii";
            case PLUTO:
                return "pluto";
            case SILVERATTA:
                return "silvretta";
            case RAY:
                return "bmw";
            default:
                return "unknown";
        }
    }

    public static String getDeviceTypeText(String serialNumber) {
        int deviceType = getDeviceType(serialNumber);
        return getDeviceTypeText(deviceType);
    }
}
