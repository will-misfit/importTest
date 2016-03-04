package com.misfit.syncsdk.task.state;

import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.callback.ConnectionStateCallback;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.utils.ContextManager;

import java.util.TimerTask;

/**
 * reconnect device post to OTA
 * */
public class ReconnectState extends State implements ConnectionStateCallback {

    private OtaTask otaTask;
    private int mRemainingRetry = 2;
    private final static int TIMEOUT_CONNECT = 45000;

    public ReconnectState(OtaTask otaTask) {
        this.otaTask = otaTask;
    }

    @Override
    public void execute() {
        final ConnectionManager connectionManager = ConnectionManager.getInstance();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(otaTask.getSerialNumber());
        if (proxy == null) {
            otaTask.onFailed("proxy is null for unknown reason");
            return;
        }
        ShineDevice device = connectionManager.getShineDevice(otaTask.getSerialNumber());
        if (device == null) {
            otaTask.onFailed("device not ready");
            return;
        }
        if (proxy.isConnected()) {
            otaTask.onSucceed();
            return;
        }

        setNewTimeOutTask(new TimerTask() {
            @Override
            public void run() {
                mRemainingRetry--;
                if (mRemainingRetry > 0) {
                    otaTask.cancelTimerTask();
                    execute();
                } else {
                    cleanup();
                    otaTask.onFailed("reconnect failed");
                }
            }
        }, TIMEOUT_CONNECT);

        proxy.subscribeConnectionStateChanged(this);
        proxy.connectProfile(device, ContextManager.getInstance().getContext());
    }

    private void cleanup() {
        cancelCurrentTimeoutTask();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(otaTask.getSerialNumber());
        if (proxy != null) {
            proxy.unsubscribeConnectionStateChanged(this);
        }
    }

    @Override
    public void stop() {
        cleanup();
    }

    @Override
    public void onConnectionStateChanged(ShineProfile.State newState) {
        if (newState == ShineProfile.State.CONNECTED) {
            cleanup();
            if (otaTask.needRetryOta()) {
                otaTask.startRetry();
            } else {
                otaTask.onSucceed();
            }
        }
    }
}
