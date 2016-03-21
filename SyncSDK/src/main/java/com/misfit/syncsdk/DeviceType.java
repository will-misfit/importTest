package com.misfit.syncsdk;

import android.text.TextUtils;

import com.misfit.syncsdk.utils.CheckUtils;

/**
 * DeviceType option
 */
public class DeviceType {
    public final static int UNKNOWN = 0;
    public final static int SHINE = 1;
    public final static int FLASH = 2;
    public final static int SWAROVSKI_SHINE = 3;
    public final static int SPEEDO_SHINE = 4;
    public final static int SHINE_MK_II = 5;
    public final static int PLUTO = 6;
    public final static int FLASH_LINK = 7;
    public final static int SILVRETTA = 8;
    public final static int BMW = 9;

    private int mTypeVal;

    DeviceType(int typeVal) {
        mTypeVal = typeVal;
    }

    public static int getDeviceType(String serialNumber) {
        return getDeviceType(serialNumber, null);
    }

    /**
     * per design, model name 'FL.2.1' for Flash Button, 'FL.2.0' for Misfit Flash
     * */
    public static int getDeviceType(String serialNumber, String modelName) {
        int result = DeviceType.UNKNOWN;

        if (TextUtils.isEmpty(serialNumber)) {
            result = DeviceType.UNKNOWN;
        } else if (serialNumber.startsWith("SH")) {
            result = DeviceType.SHINE;
        }else if (serialNumber.startsWith("SC")) {
            result = DeviceType.SWAROVSKI_SHINE;
        } else if (serialNumber.startsWith("B0")) {
            result = DeviceType.BMW;
        } else if (serialNumber.startsWith("S2")) {
            result = DeviceType.PLUTO;
        } else if (serialNumber.startsWith("C1")) {
            result = DeviceType.SILVRETTA;
        } else if (serialNumber.startsWith("SV0EZ")) {
            result = DeviceType.SPEEDO_SHINE;
        } else if (serialNumber.startsWith("SV")) {
            result = DeviceType.SHINE_MK_II;
        } else if (serialNumber.startsWith("F")) {
            result = DeviceType.FLASH;
        }

        if (result == DeviceType.FLASH) {
            if (!CheckUtils.isStringEmpty(modelName) && modelName.equals("FL.2.1")) {
                result = DeviceType.FLASH_LINK;
            }
        }
        return result;
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
            case FLASH_LINK:
                return "flash_link";
            case SILVRETTA:
                return "silvretta";
            case BMW:
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
