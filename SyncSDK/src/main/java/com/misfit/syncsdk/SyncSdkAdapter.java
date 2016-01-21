package com.misfit.syncsdk;

import android.content.Context;

import com.misfit.ble.setting.SDKSetting;
import com.misfit.syncsdk.callback.SyncScanCallback;
import com.misfit.syncsdk.device.SyncCommonDevice;

/**
 * Created by Will Hou on 1/11/16.
 */
public class SyncSdkAdapter {

    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    private static SyncSdkAdapter sharedInstance;

    private SyncSdkAdapter() {
    }

    public static SyncSdkAdapter getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new SyncSdkAdapter();
        }
        return sharedInstance;
    }

    //FIXME: need double check
    public void init(Context context, String userId) {
        mContext = context.getApplicationContext();
        SDKSetting.setUp(context, userId);
    }

    /**
     * start scanning device
     * @param expectedDeviceType
     * @param scanCallback
     * @return true if scanning started, false for else.
     */
    public boolean startScanning(int expectedDeviceType, SyncScanCallback scanCallback) {
        //FIXME: check if should stop current scanning
        if (MisfitScanner.getInstance().isBluetoothEnabled() == false) {
            return false;
        }
        MisfitScanner.getInstance().startScan(expectedDeviceType, scanCallback);
        return true;
    }

    public void stopScanning() {
        MisfitScanner.getInstance().stopScan();
    }

    public SyncCommonDevice getDevice(String serialNumber) {
        return MisfitDeviceManager.getInstance().getSpecificDevice(serialNumber);
    }
}
