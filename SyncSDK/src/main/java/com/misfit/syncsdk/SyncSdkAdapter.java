package com.misfit.syncsdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;

import com.misfit.ble.setting.SDKSetting;
import com.misfit.syncsdk.callback.SyncScanCallback;
import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.enums.ScanFailedReason;
import com.misfit.syncsdk.utils.ContextManager;

/**
 * Class open to app providing static methods
 */
public class SyncSdkAdapter {

    private Context mContext;

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
    public void init(Context context, String userId, String authToken) {
        mContext = context.getApplicationContext();
        ContextManager.getInstance().setContext(mContext);
        ContextManager.getInstance().setUserAuthToken(authToken);
        SDKSetting.setUp(context, userId);
    }

    /**
     * start scanning device
     * @param expectedDeviceType
     * @param scanCallback
     * @return true if scanning started, false for else.
     *
     * FIXME: does it need to start a LogSession within startScanning?
     */
    public void startScanning(int expectedDeviceType, SyncScanCallback scanCallback) {
        //FIXME: check if should stop current scanning
        //FIXME: warning for memory leaking(SyncScanCallback).
        if (MisfitScanner.getInstance().isBluetoothEnabled() == false) {
            scanCallback.onScanFailed(ScanFailedReason.NO_BLUETOOTH);
            return;
        }

        boolean result = MisfitScanner.getInstance().startScan(expectedDeviceType, scanCallback);
        if (result == false) {
            scanCallback.onScanFailed(ScanFailedReason.INTERNAL_ERROR);
        }
    }

    public void stopScanning() {
        MisfitScanner.getInstance().stopScan();
    }

    public SyncCommonDevice getDevice(String serialNumber) {
        return MisfitDeviceManager.getInstance().getSpecificDevice(serialNumber);
    }
}
