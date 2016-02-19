package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.MLog;

import java.util.TimerTask;

public class DisconnectTask extends Task implements ShineSdkProfileProxy.ConnectionStateCallback {

    private final static String TAG = "DisconnectTask";

    @Override
    protected void prepare() {
        mLogEvent = createLogEvent(LogEventType.DISCONNECT);
    }

    @Override
    protected void execute() {
        mLogEvent.start();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            mLogEvent.end(LogEvent.RESULT_SUCCESS, "disconnected already");
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

        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;
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
            mLogEvent.end(LogEvent.RESULT_SUCCESS, "state is " + newState);
            taskSucceed();
        }
    }
}
