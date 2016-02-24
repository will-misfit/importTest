package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.MLog;

import java.util.Hashtable;

/**
 * Task to set configuration
 * TODO: ShineSDKProvider.setConfigurationSession?
 */
public class SetConfigurationTask extends Task implements ShineProfile.ConfigurationCallback {
    private static final String TAG = "SetConfigurationTask";

    @Override
    protected void prepare() {
        mLogEvent = createLogEvent(LogEventType.SET_CONFIGURATION);
    }

    @Override
    protected void execute() {
        mLogEvent.start();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProfileProxy is not ready");
            taskFailed("ShineSdkProfileProxy not prepared yet");
            return;
        }
        ShineConfiguration shineConfig = mTaskSharedData.getSyncCalculationCallback().getShineConfiguration();
        proxy.setDeviceConfiguration(shineConfig, this);
        taskSucceed();
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
        MLog.d(TAG, String.format("onConfigCompleted() actionId=%s, result=%s", actionID, resultCode));
        if (actionID == ActionID.SET_CONFIGURATION) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                // TODO: check the object got from Hashtable data with key of Shine_Configuration_Session
                mTaskSharedData.setConfigurationSession((ConfigurationSession) data.get(ShineProperty.SHINE_CONFIGURATION_SESSION));
                mLogEvent.end(LogEvent.RESULT_SUCCESS);
                taskSucceed();
            } else {
                mLogEvent.end(LogEvent.RESULT_FAILURE, "resultCode is " + resultCode);
                retry();
            }
        } else {
            MLog.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }
}
