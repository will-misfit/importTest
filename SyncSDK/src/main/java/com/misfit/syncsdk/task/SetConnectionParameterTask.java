package com.misfit.syncsdk.task;

import android.util.Log;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

import java.util.Hashtable;
import java.util.TimerTask;

/**
 * Task to set connection parameter
 */
public class SetConnectionParameterTask extends Task implements ShineProfile.ConfigurationCallback {

    private final static String TAG = "SetConnParameterTask";

    ShineConnectionParameters mParameters;

    public SetConnectionParameterTask(ShineConnectionParameters parameters) {
        mParameters = parameters;
    }

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.SET_CONNECTION_PARAMETER);
    }

    @Override
    protected void execute() {
        MLog.d(TAG, "execute()");
        mLogEvent.start();

        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProfileProxy is not ready");
            taskFailed("proxy not prepared");
            return;
        }

        cancelCurrentTimerTask();
        mCurrTimerTask = createTimeoutTask();
        TimerManager.getInstance().addTimerTask(mCurrTimerTask, SdkConstants.SET_CONNECTION_PARAM_TIMEOUT);

        proxy.startSettingConnectionParams(mParameters, this);
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
        cancelCurrentTimerTask();
        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;
    }

    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        MLog.d(TAG, String.format("actionID %s, resultCode %s", actionID, resultCode));
        // as the timeout value of ShineProfile method differs with the one of Task, this callback may be invoked
        // when this Task finishes (succeed/fail/ignore)
        if (mIsFinished.get()) {
            return;
        }

        if (actionID == ActionID.SET_CONNECTION_PARAMETERS) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
                taskSucceed();
            } else {
                if (mLogEvent != null) {
                    mLogEvent.end(LogEvent.RESULT_FAILURE, String.format("resultCode %s", resultCode));
                }
                retryAndIgnored();
            }
        }
    }

    @Override
    protected TimerTask createTimeoutTask() {
        return new TimerTask() {
            @Override
            public void run() {
                MLog.d(TAG, "Task instance timeout timer ticks!");
                taskIgnored("timeout");
            }
        };
    }
}
