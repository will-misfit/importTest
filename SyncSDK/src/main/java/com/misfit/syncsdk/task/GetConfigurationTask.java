package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
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
 * Task to get Shine configuration
 */
public class GetConfigurationTask extends Task implements ShineProfile.ConfigurationCallback {

    private final static String TAG = "GetConfigurationTask";

    @Override
    protected void prepare() {
        mLogEvent = createLogEvent(LogEventType.GET_CONFIGURATION);
    }

    @Override
    protected void execute() {
        mLogEvent.start();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProfileProxy is not ready");
            taskFailed("proxy not prepared");
            return;
        }
        proxy.gettingDeviceConfiguration(this);
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
        if (actionID == ActionID.GET_CONFIGURATION) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                // TODO: check the object got from Hashtable data with key of Shine_Configuration_Session
                mTaskSharedData.setConfigurationSession((ConfigurationSession) data.get(ShineProperty.SHINE_CONFIGURATION_SESSION));
                MLog.d(TAG, "config: " + mTaskSharedData.getConfigurationSession().mShineConfiguration.toString());
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
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
