package com.misfit.syncsdk.task;

import com.misfit.ble.setting.flashlink.FlashButtonMode;
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
 * when Flash Button want to sync with flagship app, its FlashButtonMode needs to set
 * Note: execute this task only when it is Flash Button
 */
public class SwitchTrackerModeTask extends Task implements ShineProfile.ConfigurationCallback {

    private static final String TAG = "SwitchTrackerModeTask";

    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        if (actionID == ActionID.SET_FLASH_BUTTON_MODE) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
                taskSucceed();
            } else {
                mLogEvent.end(LogEvent.RESULT_FAILURE, "resultCode is " + resultCode);
                retryAndIgnored();
            }
        } else {
            MLog.d(TAG, "unexpected action = " + actionID + ", result = " + resultCode);
        }
    }

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.SwitchTrackerMode);
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
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProxy is not ready");
            taskFailed("proxy not prepared");
            return;
        }

        updateExecuteTimer();
        proxy.setFlashButtonMode(FlashButtonMode.TRACKER, this);
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
}
