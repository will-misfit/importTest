package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.utils.MLog;

import java.util.TimerTask;

public class DisconnectTask extends Task implements ShineSdkProfileProxy.ConnectionStateCallback {

    private final static String TAG = "DisconnectTask";

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            taskSucceed();
            return;
        }
        proxy.subscribeConnectionStateChanged(this);
        TimerManager.getInstance().addTimerTask(createTimeoutTimerTask(), 2000);
        proxy.close();
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
        cancelCurrentTimerTask();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy != null) {
            proxy.unsubscribeConnectionStateChanged(this);
        }
    }

    TimerTask createTimeoutTimerTask() {
        cancelCurrentTimerTask();
        mCurrTimerTask = new TimerTask() {
            @Override
            public void run() {
                MLog.d(TAG, "time out");
                retryAndIgnored();
            }
        };
        return mCurrTimerTask;
    }


    @Override
    public void onConnectionStateChanged(ShineProfile.State newState) {
        MLog.d(TAG, "connectionStateChanged() newState=" + newState);
        if (mIsFinished) {
            return;
        }
        if (newState == ShineProfile.State.CLOSED) {
            taskSucceed();
        }
    }
}
