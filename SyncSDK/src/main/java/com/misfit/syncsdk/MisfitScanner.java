package com.misfit.syncsdk;

import android.content.Context;
import android.util.Log;

import com.misfit.ble.shine.ShineAdapter;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.syncsdk.callback.SyncScanCallback;
import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.device.SyncFlashDevice;
import com.misfit.syncsdk.device.SyncIwcDevice;
import com.misfit.syncsdk.device.SyncMKIIDevice;
import com.misfit.syncsdk.device.SyncRayDevice;
import com.misfit.syncsdk.device.SyncShine2Device;
import com.misfit.syncsdk.device.SyncShineDevice;
import com.misfit.syncsdk.device.SyncSpeedoDevice;
import com.misfit.syncsdk.device.SyncSwarovskiDevice;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogSession;
import com.misfit.syncsdk.utils.ContextManager;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * class to scan devices, via ShineSdkAdapterProxy
 * it implements ShineScanCallback for ShineSDK, and invoke SyncScanCallback from Client
 *
 * it is used to scan an expected device type, it must be monitored by timer management
 */
public class MisfitScanner implements ShineAdapter.ShineScanCallback {

    private final static String TAG = "MisfitScanner";

    ShineSdkAdapterProxy mShineSDKAdapter;

    SyncScanCallback mClientScanCallback;

    int mExpectedDeviceType;

    // current architecture which put scan with given device type
    private LogSession mLogSession;

    private LogEvent mCurrLogEvent;

    private AtomicBoolean isScanning = new AtomicBoolean(false);

    private static MisfitScanner sharedInstance ;

    private MisfitScanner(Context context) {
        mShineSDKAdapter = new ShineSdkAdapterProxy(context);
        isScanning.set(false);
    }

    public static MisfitScanner getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new MisfitScanner(ContextManager.getInstance().getContext());
        }
        return sharedInstance;
    }

    public boolean isBluetoothEnabled() {
        return mShineSDKAdapter.mShineAdapter.isEnabled();
    }

    public void enableBluetooth() {
        mShineSDKAdapter.mShineAdapter.enableBluetooth();
    }

    private TimerTask mScanTimerTask;


    /**
     * support invoke from external - App
     *
     * @param expectedDeviceType
     * @param callback
     */
    public boolean startScan(int expectedDeviceType, SyncScanCallback callback) {
        if (isScanning.get()) {
            return false;
        }

        isScanning.set(true);
        Log.d(TAG, String.format("startScan, expected device type of %s", DeviceType.getDeviceTypeText(expectedDeviceType)));
        mClientScanCallback = callback;
        mExpectedDeviceType = expectedDeviceType;

        mScanTimerTask = createTimerTask();
        TimerManager.getInstance().addTimerTask(mScanTimerTask, SdkConstants.SCAN_DEVICE_TYPE_TIMEOUT);

        return mShineSDKAdapter.startScanning(this);
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

        isScanning.set(false);
        cancelTimerTask();
    }

    @Override
    public void onScanResult(ShineDevice device, int rssi) {
        if (mClientScanCallback == null) {
            return;
        }

        int deviceType = DeviceType.getDeviceType(device.getSerialNumber());
        if (mExpectedDeviceType != DeviceType.UNKNOWN && deviceType != mExpectedDeviceType) {
            return;
        }

        Log.d(TAG, String.format("onScanResult, serialNumber %s, MAC Addr %s, device type %s, while expected %s",
            device.getSerialNumber(), device.getAddress(),
            DeviceType.getDeviceTypeText(deviceType),
            DeviceType.getDeviceTypeText(mExpectedDeviceType)));

        ConnectionManager.getInstance().saveShineDevice(device.getSerialNumber(), device);

        SyncCommonDevice commonDevice;
        switch (deviceType) {
            case DeviceType.SHINE:
                commonDevice = new SyncShineDevice(device.getSerialNumber());
                break;
            case DeviceType.SWAROVSKI_SHINE:
                commonDevice = new SyncSwarovskiDevice(device.getSerialNumber());
                break;
            case DeviceType.SPEEDO_SHINE:
                commonDevice = new SyncSpeedoDevice(device.getSerialNumber());
                break;
            case DeviceType.SHINE_MK_II:
                commonDevice = new SyncMKIIDevice(device.getSerialNumber());
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

        mClientScanCallback.onScanResultFiltered(commonDevice, rssi);
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                MLog.d(TAG, "Scanning for given device type is timeout");
                // if there is LogSession, write the FailureReason of LogSession
            }
        };
    }

    private void cancelTimerTask() {
        if (mScanTimerTask != null) {
            mScanTimerTask.cancel();
            mScanTimerTask = null;
        }
    }
}
