package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.ContextUtils;
import com.misfit.syncsdk.utils.MLog;

import java.util.TimerTask;


/**
 * Task instance to do the connect operation.
 */
public class ConnectTask extends Task implements ShineSdkProfileProxy.ConnectionStateCallback {

    private final static String TAG = "ConnectTask";

    private final static long CONNECT_TASK_TIMEOUT = 45000;

    @Override
    protected void prepare() {
        mLogEvent = createLogEvent(LogEventType.CONNECT);
    }

    @Override
    protected void execute() {
        mLogEvent.start(mTaskSharedData.getSerialNumber());

        ConnectionManager connectionManager = ConnectionManager.getInstance();
        ShineSdkProfileProxy proxy = connectionManager.getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null) {
            proxy = connectionManager.createShineProfileProxy(mTaskSharedData.getSerialNumber());
        }
        if (proxy.isConnected()) {
            mLogEvent.end(LogEvent.RESULT_SUCCESS, "connected already, no need to start connect");
            taskSucceed();
            return;
        }

        //get device
        ShineDevice device = connectionManager.getShineDevice(mTaskSharedData.getSerialNumber());
        if (device == null) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "device not ready");
            taskFailed("device not ready");
            return;
        }

        //set timeout
        mCurrTimerTask = createTimerTask();
        TimerManager.getInstance().addTimerTask(mCurrTimerTask, CONNECT_TASK_TIMEOUT);

        //connect
        proxy.subscribeConnectionStateChanged(this);
        proxy.connectProfile(device, ContextUtils.getInstance().getContext());
    }

    @Override
    public void onStop() {
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy != null) {
            proxy.unsubscribeConnectionStateChanged(this);
            proxy.close();
        }
    }

    @Override
    protected void cleanup() {
        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;

        cancelCurrentTimerTask();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy != null) {
            proxy.unsubscribeConnectionStateChanged(this);
        }
    }

    @Override
    public void onConnectionStateChanged(ShineProfile.State state) {
        if (mIsFinished) {
            return;
        }
        if (state == ShineProfile.State.CONNECTED) {
            updateDeviceInfo();
            mLogEvent.end(LogEvent.RESULT_SUCCESS, "connected");
            taskSucceed();
        } else {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "changed connection state is " + state);
            retry();
        }
    }

    private void updateDeviceInfo() {
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
