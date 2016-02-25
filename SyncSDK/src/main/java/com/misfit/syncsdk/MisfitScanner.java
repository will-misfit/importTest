package com.misfit.syncsdk;

import android.content.Context;
import android.util.Log;

import com.misfit.ble.shine.ShineAdapter;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.syncsdk.callback.SyncScanCallback;
import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.device.SyncFlashDevice;
import com.misfit.syncsdk.device.SyncIwcDevice;
import com.misfit.syncsdk.device.SyncRayDevice;
import com.misfit.syncsdk.device.SyncShine2Device;
import com.misfit.syncsdk.device.SyncShineDevice;
import com.misfit.syncsdk.device.SyncSwarovskiDevice;
import com.misfit.syncsdk.utils.ContextUtils;

/**
 * class to scan devices, via ShineSdkAdapterProxy
 */
public class MisfitScanner implements ShineAdapter.ShineScanCallback {

    private final static String TAG = "MisfitScanner";

    ShineSdkAdapterProxy mShineSDKAdapter;
    SyncScanCallback mCallback;
    int mExpectedDeviceType;

    private static MisfitScanner sharedInstance;

    private MisfitScanner(Context context) {
        mShineSDKAdapter = new ShineSdkAdapterProxy(context);
    }

    public static MisfitScanner getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new MisfitScanner(ContextUtils.getInstance().getContext());
        }
        return sharedInstance;
    }

    public boolean isBluetoothEnabled() {
        return mShineSDKAdapter.mShineAdapter.isEnabled();
    }

    public void enableBluetooth() {
        mShineSDKAdapter.mShineAdapter.enableBluetooth();
    }

    /**
     * support invoke from external - App
     *
     * @param expectedDeviceType
     * @param callback
     */
    public boolean startScan(int expectedDeviceType, SyncScanCallback callback) {
        Log.d(TAG, String.format("startScan, expected device type of %s", DeviceType.getDeviceTypeText(expectedDeviceType)));
        mCallback = callback;
        mExpectedDeviceType = expectedDeviceType;
        boolean result = mShineSDKAdapter.startScanning(this);
        return result;
    }

    /**
     * support invoke from internal - ScanTask
     *
     * @param scanCallback
     */
    public boolean startScan(ShineAdapter.ShineScanCallback scanCallback) {
        //TODO:check if scanning
        Log.d(TAG, "startScan without specified device type");
        boolean result = mShineSDKAdapter.startScanning(scanCallback);
        return result;
    }

    public void stopScan() {
        Log.d(TAG, "stop scan");
        mShineSDKAdapter.stopScanning();
    }

    @Override
    public void onScanResult(ShineDevice device, int rssi) {
        if (mCallback == null) {
            return;
        }

        int deviceType = DeviceType.getDeviceType(device.getSerialNumber());
        Log.d(TAG, String.format("onScanResult, serialNumber %s, MAC Addr %s, device type %s",
            device.getSerialNumber(), device.getAddress(), DeviceType.getDeviceTypeText(deviceType)));
        if (deviceType != mExpectedDeviceType) {
            return;
        }

        ConnectionManager.getInstance().saveShineDevice(device.getSerialNumber(), device);

        //TODO:wait to implement-different device
        SyncCommonDevice commonDevice;
        switch (deviceType) {
            case DeviceType.SWAROVSKI_SHINE:
                commonDevice = new SyncSwarovskiDevice(device.getSerialNumber());
                break;
            case DeviceType.SHINE:
                commonDevice = new SyncShineDevice(device.getSerialNumber());
                break;
            case DeviceType.BMW:
                commonDevice = new SyncRayDevice(device.getSerialNumber());
                break;
            case DeviceType.FLASH:
                commonDevice = new SyncFlashDevice(device.getSerialNumber());
                break;
            case DeviceType.PLUTO:
                commonDevice = new SyncShine2Device(device.getSerialNumber());
                break;
            case DeviceType.SILVRETTA:
                commonDevice = new SyncIwcDevice(device.getSerialNumber());
                break;
            default:
                commonDevice = new SyncShineDevice(device.getSerialNumber());
                break;
        }

        mCallback.onScanResultFiltered(commonDevice, rssi);
    }
}
