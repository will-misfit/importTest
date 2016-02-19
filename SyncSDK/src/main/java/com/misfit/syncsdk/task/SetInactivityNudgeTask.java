package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.MLog;

import java.util.Hashtable;

public class SetInactivityNudgeTask extends Task implements ShineProfile.ConfigurationCallback {

    private final static String TAG = "SetInactivityNudgeTask";

    @Override
    protected void prepare() {
        mLogEvent = createLogEvent(LogEventType.SET_INACTIVE_NUDGES);
    }

    @Override
    protected void execute() {
        mLogEvent.start();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProfileProxy is not ready");
            taskIgnored("proxy not prepared");
            return;
        }
        if (mTaskSharedData.getSyncParams().inactivityNudgeSettings == null) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "InactivityNudge settings is null");
            taskIgnored("InactivityNudge settings is null");
            return;
        }
        proxy.setInactivityNudge(mTaskSharedData.getSyncParams().inactivityNudgeSettings, this);
    }

    @Override
    public void onStop() {

    }

    @Override
    protected void cleanup() {
        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;
    }

    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        if (actionID == ActionID.SET_INACTIVITY_NUDGE) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
                taskSucceed();
            } else {
                mLogEvent.end(LogEvent.RESULT_FAILURE, "resultCode is " + resultCode);
                retryAndIgnored();
            }
        } else {
            MLog.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }
}
