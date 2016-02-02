package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.utils.ContextUtils;
import com.misfit.syncsdk.utils.MLog;

import java.util.TimerTask;


/**
 * Task instance to do the connect operation.
 */
public class ConnectTask extends Task implements ConnectionManager.ConnectionStateCallback {

    private final static String TAG = "ConnectTask";

    private final static long CONNECT_TASK_TIMEOUT = 45000;

    @Override
    protected void prepare() {
    }

    @Override
    protected void execute() {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        ShineSdkProfileProxy proxy = connectionManager.getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null) {
            proxy = connectionManager.createShineProfileProxy(mTaskSharedData.getSerialNumber());
        }
        if (proxy.isConnected()) {
            taskSucceed();
            return;
        }

        //get device
        ShineDevice device = connectionManager.getShineDevice(mTaskSharedData.getSerialNumber());
        if (device == null) {
            taskFailed("device not ready");
            return;
        }

        //set timeout
        mCurrTimerTask = createTimerTask();
        TimerManager.getInstance().addTimerTask(mCurrTimerTask, CONNECT_TASK_TIMEOUT);

        //connect
        ShineSdkProfileProxy shineSdkProfileProxy = connectionManager.createShineProfileProxy(mTaskSharedData.getSerialNumber());
        connectionManager.subscribeConnectionStateChanged(mTaskSharedData.getSerialNumber(), this);
        shineSdkProfileProxy.connectProfile(device, ContextUtils.getInstance().getContext());
    }

    @Override
    public void onStop() {
        ConnectionManager.getInstance().unsubscribeConnectionStateChanged(mTaskSharedData.getSerialNumber(), this);
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy != null) {
            proxy.close();
        }
    }

    @Override
    protected void cleanup() {
        cancelCurrentTimerTask();
        ConnectionManager.getInstance().unsubscribeConnectionStateChanged(mTaskSharedData.getSerialNumber(), this);
    }

    @Override
    public void onConnectionStateChanged(ShineProfile.State state) {
        if (mIsFinished) {
            return;
        }
        ConnectionManager.getInstance().unsubscribeConnectionStateChanged(mTaskSharedData.getSerialNumber(), this);
        if (state == ShineProfile.State.CONNECTED) {
            updateTaskSharedData();
            taskSucceed();
        } else {
            retry();
        }
    }

    private void updateTaskSharedData() {
        ShineSdkProfileProxy profileProxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        mTaskSharedData.setFirmwareVersion(profileProxy.getFirmwareVersion());
        mTaskSharedData.setModelName(profileProxy.getModelNumber());
    }

    /*
    * TimerTask to monitor if the task is not completed successfully in time
    * */
    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                MLog.d(TAG, "connect time out");
                retry();
            }
        };
    }
}
