package com.misfit.syncsdk.task;

import android.util.Log;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;

import java.util.Hashtable;

/**
 * Created by Will Hou on 1/12/16.
 */
public class SetConnectionParameterTask extends Task implements ConnectionManager.ConfigCompletedCallback {

    private final static String TAG = "SetConnParameterTask";

    ShineConnectionParameters mParameters;

    public SetConnectionParameterTask(ShineConnectionParameters parameters) {
        mParameters = parameters;

    }

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy != null && proxy.isConnected()) {
            ConnectionManager.getInstance().subscribeConfigCompleted(mTaskSharedData.getSerialNumber(), this);
            proxy.startSettingConnectionParams(mParameters);
        } else {
            taskIgnored("connection did not ready");
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
        ConnectionManager.getInstance().unsubscribeConfigCompleted(mTaskSharedData.getSerialNumber(), this);
    }

    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        if (actionID == ActionID.SET_CONNECTION_PARAMETERS) {
            ConnectionManager.getInstance().unsubscribeConfigCompleted(mTaskSharedData.getSerialNumber(), this);
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                taskSucceed();
            } else {
                retryAndIgnored();
            }
        } else {
            Log.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }
}
