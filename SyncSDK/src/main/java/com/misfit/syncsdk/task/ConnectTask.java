package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.callback.ConnectionStateCallback;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.ContextManager;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

import java.util.TimerTask;


/**
 * Task instance to do the connect operation.
 */
public class ConnectTask extends Task implements ConnectionStateCallback {

    private final static String TAG = "ConnectTask";


    /* inherited interface API of Task */
    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.CONNECT);
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
            MLog.d(TAG, "execute(), ShineProfile connected already, connect task succeed");
            mLogEvent.end(LogEvent.RESULT_SUCCESS, "connected already, no need to start connect");
            taskSucceed();
            return;
        }

        //get device
        ShineDevice device = connectionManager.getShineDevice(mTaskSharedData.getSerialNumber());
        if (device == null) {
            MLog.d(TAG, "execute(), device not ready");
            mLogEvent.end(LogEvent.RESULT_FAILURE, "device not ready");
            taskFailed("device not ready");
            return;
        }

        updateExecuteTimer(SdkConstants.CONNECT_TIMEOUT);

        //connect
        proxy.subscribeConnectionStateChanged(this);
        proxy.connectProfile(device, ContextManager.getInstance().getContext());
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

    /* inherited interface API of ConnectionStateCallback */
    /**
     * ConnectionStateCallback inside ConnectTask only cares Connected and Disconnected event during this task duration
     * when the task finish, no matter succeed or failed, its ConnectionStateCallback should be removed from ShineSdk
     * */
    @Override
    public void onConnectionStateChanged(ShineProfile.State state) {
        if (mIsFinished.get()) {
            return;
        }

        MLog.d(TAG, String.format("onConnectionStateChanged(), newState %s", state));
        if (state == ShineProfile.State.CONNECTED) {
            updateDeviceInfo();
            mLogEvent.end(LogEvent.RESULT_SUCCESS, "connected");

            // ConnectTask successful, subscribe a monitoring ConnectionStateCallback for remaining Tasks
            ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
            if (proxy != null) {
                ConnectionStateCallback postConnectMonitor = mTaskSharedData.getPostConnectConnectionStateCallback();
                proxy.subscribeConnectionStateChanged(postConnectMonitor);
            }

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
        // TODO: for SyncFlashDevice, it is optional to update DeviceType to FlashLink now
    }

    /*
    * TimerTask to monitor if the task is not completed successfully in time
    * */
    @Override
    protected TimerTask createTimeoutTask() {
        return new TimerTask() {
            @Override
            public void run() {
                MLog.d(TAG, "connect time out");
                retry();
            }
        };
    }
}
