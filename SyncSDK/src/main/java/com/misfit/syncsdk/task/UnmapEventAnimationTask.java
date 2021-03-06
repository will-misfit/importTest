package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

import java.util.Hashtable;

/**
 * Note: execute this task only when it is Flash Button
 */
public class UnmapEventAnimationTask extends Task implements ShineProfile.ConfigurationCallback {

    private final static String TAG = "UnmapEventAnimationTask";

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.UnmapEventAnimation);
    }

    @Override
    protected void execute() {
        mLogEvent.start();

        if (mTaskSharedData.getDeviceType() != DeviceType.FLASH_LINK) {
            String msg = String.format("Ignore this task as it is not %s", DeviceType.getDeviceTypeText(DeviceType.FLASH_LINK));
            mLogEvent.end(LogEvent.RESULT_OTHER, msg);
            taskIgnored(msg);
            return;
        }

        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProfileProxy is not ready");
            taskFailed("proxy not prepared");
            return;
        }

        updateExecuteTimer();
        proxy.unmapAllEventAnimation(this);
    }

    @Override
    protected void onStop() {
    }

    @Override
    protected void cleanup() {
        cancelCurrentTimerTask();
        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;
    }

    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        if (actionID == ActionID.SET_SINGLE_ALARM_TIME || actionID == ActionID.CLEAR_ALL_ALARMS) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
                taskSucceed();
            } else {
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "resultCode is " + resultCode);
                retryAndIgnored();
            }
        } else {
            MLog.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }
}
