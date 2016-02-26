package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineAdapter;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.MisfitScanner;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.enums.FailedReason;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

import java.util.TimerTask;


/**
 * Task for scan
 */
public class ScanTask extends Task implements ShineAdapter.ShineScanCallback {

    private final static String TAG = "ScanTask";

    private TimerTask createTimeoutTask() {
        cancelCurrentTimerTask();
        mCurrTimerTask = new TimerTask() {
            @Override
            public void run() {
                MLog.d(TAG, "scan time out, required device is not found yet");
                mTaskSharedData.setFailureReason(FailedReason.NO_DEVICE_WITH_SERIAL_NUMBER_FOUND);
                taskFailed("scan timeout");
            }
        };
        return mCurrTimerTask;
    }

    /**
     *  prepare(), execute(), cleanup(), onStop() are invoked in start(TaskSharedData)
     */
    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.START_SCANNING);
    }

    @Override
    protected void execute() {
        mLogEvent.start(mTaskSharedData.getSerialNumber());
        //check if should not scan
        if (ConnectionManager.getInstance().getShineDevice(mTaskSharedData.getSerialNumber()) != null) {
            MLog.d(TAG, "ConnectionManager already has the device, no need to scan");
            mLogEvent.end(LogEvent.RESULT_SUCCESS, "no need to scan this device");
            taskSucceed();
            return;
        }

        TimerManager.getInstance().addTimerTask(createTimeoutTask(), SdkConstants.SCAN_ONE_DEVICE_TIMEOUT);
        MisfitScanner.getInstance().startScan(this);
        mLogEvent.end(LogEvent.RESULT_SUCCESS, "scan cmd is started");

        // LogEvent of startScanning must be appended immediately as later it turns to be another LogEvent
        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
        cancelCurrentTimerTask();
        mLogSession.appendEvent(mLogEvent);

        mLogEvent = GeneralUtils.createLogEvent(LogEventType.STOP_SCANNING);
        mLogEvent.start();
        MisfitScanner.getInstance().stopScan();
        mLogEvent.end(LogEventType.STOP_SCANNING, "");

        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;
    }

    @Override
    public void onScanResult(ShineDevice device, int rssi) {
        if (mIsFinished) {
            return;
        }

        mLogEvent = GeneralUtils.createLogEvent(LogEventType.SCANNED_DEVICE);
        mLogEvent.start();
        mLogEvent.end(LogEvent.RESULT_SUCCESS, device.getSerialNumber());
        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;

        if (mTaskSharedData.getSerialNumber().equals(device.getSerialNumber())) {
            cancelCurrentTimerTask();
            ConnectionManager.getInstance().saveShineDevice(device.getSerialNumber(), device);
            taskSucceed();
        }
    }
}
