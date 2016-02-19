package com.misfit.syncsdk.task;

import android.util.Log;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;

import java.util.Hashtable;

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
        mLogEvent = createLogEvent(LogEventType.SET_CONNECTION_PARAMETER);
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
        proxy.startSettingConnectionParams(mParameters, this);
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
        if (actionID == ActionID.SET_CONNECTION_PARAMETERS) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
                taskSucceed();
            } else {
                mLogEvent.end(LogEvent.RESULT_FAILURE, "result code is " + resultCode);
                retryAndIgnored();
            }
        } else {
            Log.d(TAG, "unexpected action = " + actionID + ", result=" + resultCode);
        }
    }
}
