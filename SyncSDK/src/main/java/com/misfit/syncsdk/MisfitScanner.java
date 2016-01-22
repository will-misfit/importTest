package com.misfit.syncsdk;

import android.content.Context;
import android.util.Log;

import com.misfit.ble.shine.ShineAdapter;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.syncsdk.callback.SyncScanCallback;
import com.misfit.syncsdk.device.SyncCommonDevice;
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

    public boolean isBluetoothEnabled(){
        return mShineSDKAdapter.mShineAdapter.isEnabled();
    }

    /**
     * support invoke from external - App
     *
     * @param expectedDeviceType
     * @param callback
     */
    public void startScan(int expectedDeviceType, SyncScanCallback callback) {
        Log.d(TAG, "startScan");
        mCallback = callback;
        mExpectedDeviceType = expectedDeviceType;
        mShineSDKAdapter.startScanning(this);
    }

    /**
     * support invoke from internal - ScanTask
     *
     * @param scanCallback
     */
    public void startScan(ShineAdapter.ShineScanCallback scanCallback) {
        //TODO:check if scanning
        Log.d(TAG, "startScan");
        mShineSDKAdapter.startScanning(scanCallback);
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
        Log.d(TAG, String.format("%s, serialNumber=%s, mac=%s", deviceType, device.getSerialNumber(), device.getAddress()));
        if (deviceType != mExpectedDeviceType) {
            return;
        }

        Log.d(TAG, "found " + deviceType + ", serialNumber=" + device.getSerialNumber());
        ConnectionManager.getInstance().saveShineDevice(device.getSerialNumber(), device);

        //TODO:wait to implement-different device
        SyncCommonDevice commonDevice;
        if (deviceType == DeviceType.SWAROVSKI_SHINE) {
            commonDevice = new SyncSwarovskiDevice(device.getSerialNumber());
        } else {
            commonDevice = new SyncShineDevice(device.getSerialNumber());
        }
        mCallback.onScanResultFiltered(commonDevice, rssi);
    }
}
