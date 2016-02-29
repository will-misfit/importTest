package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.callback.ConnectionStateCallback;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

import java.util.TimerTask;

public class DisconnectTask extends Task implements ConnectionStateCallback {

    private final static String TAG = "DisconnectTask";

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.DISCONNECT);
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

        cancelCurrentTimerTask();
        mCurrTimerTask = createTimeoutTask();
        TimerManager.getInstance().addTimerTask(mCurrTimerTask, SdkConstants.DISCONNECT_TIMEOUT);

        proxy.subscribeConnectionStateChanged(this);
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

    @Override
    protected TimerTask createTimeoutTask() {
        return new TimerTask() {
            @Override
            public void run() {
                MLog.d(TAG, "time out");
                retryAndIgnored();
            }
        };
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
