package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.utils.MLog;

import java.util.TimerTask;


/**
 * Created by Will Hou on 1/13/16.
 */
public class DisconnectTask extends Task implements ConnectionManager.ConnectionStateCallback {

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
        ConnectionManager.getInstance().subscribeConnectionStateChanged(mTaskSharedData.getSerialNumber(), this);
        TimerManager.getInstance().addTimerTask(createTimeoutTimerTask(), 2000);
        proxy.close();
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
        cancelCurrentTimerTask();
        ConnectionManager.getInstance().unsubscribeConnectionStateChanged(mTaskSharedData.getSerialNumber(), this);
    }

    TimerTask createTimeoutTimerTask() {
        cancelCurrentTimerTask();
        mCurrTimerTask = new TimerTask() {
            @Override
            public void run() {
                MLog.d(TAG, "time out");
                ConnectionManager.getInstance().unsubscribeConnectionStateChanged(mTaskSharedData.getSerialNumber(), DisconnectTask.this);
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
            ConnectionManager.getInstance().unsubscribeConnectionStateChanged(mTaskSharedData.getSerialNumber(), this);
            cancelCurrentTimerTask();
            taskSucceed();
        }
    }
}
