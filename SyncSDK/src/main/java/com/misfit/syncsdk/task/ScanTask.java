package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineAdapter;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.MisfitScanner;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.utils.MLog;

import java.util.TimerTask;


/**
 * Created by Will Hou on 1/12/16.
 */
public class ScanTask extends Task implements ShineAdapter.ShineScanCallback {

    private final static String TAG = "ScanTask";

    private final static long SCAN_TIMEOUT = 30000;

    private TimerTask createTimeoutTask() {
        cancelCurrentTimerTask();
        mCurrTimerTask = new TimerTask() {
            @Override
            public void run() {
                MLog.d(TAG, "time out, will do retry");
                onStop();
                retry();
            }
        };
        return mCurrTimerTask;
    }

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        //check if should not scan
        if (ConnectionManager.getInstance().getShineDevice(mTaskSharedData.getSerialNumber()) != null) {
            MLog.d(TAG, "ConnectionManager already has the device, no need to scan");
            taskSucceed();
            return;
        }
        TimerManager.getInstance().addTimerTask(createTimeoutTask(), SCAN_TIMEOUT);
        MisfitScanner.getInstance().startScan(this);
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
        cancelCurrentTimerTask();
        MisfitScanner.getInstance().stopScan();
    }

    @Override
    public void onScanResult(ShineDevice device, int rssi) {
        if (mIsFinished) {
            return;
        }
        if (mTaskSharedData.getSerialNumber().equals(device.getSerialNumber())) {
            //TODO:should add task phase to avoid callback was invoked again.
            cancelCurrentTimerTask();
            ConnectionManager.getInstance().saveShineDevice(device.getSerialNumber(), device);
            MisfitScanner.getInstance().stopScan();
            taskSucceed();
        }
    }
}
