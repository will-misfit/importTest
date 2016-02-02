package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.utils.MLog;

import java.util.Hashtable;

/**
 * Created by Will Hou on 1/13/16.
 */
public class GetConfigurationTask extends Task implements ConnectionManager.ConfigCompletedCallback {

    private final static String TAG = "GetConfigurationTask";

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy != null && proxy.isConnected()) {
            ConnectionManager.getInstance().subscribeConfigCompleted(mTaskSharedData.getSerialNumber(), this);
            proxy.startGettingDeviceConfiguration();
        } else {
            taskFailed("connection did not ready");
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
        MLog.d(TAG, String.format("onConfigCompleted() actionId=%s, result=%s", actionID, resultCode));
        if (actionID == ActionID.GET_CONFIGURATION) {
            ConnectionManager.getInstance().unsubscribeConfigCompleted(mTaskSharedData.getSerialNumber(), this);
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                // TODO: check the object got from Hashtable data with key of Shine_Configuration_Session
                mTaskSharedData.setConfigurationSession((ConfigurationSession) data.get(ShineProperty.SHINE_CONFIGURATION_SESSION));
                MLog.d(TAG, "config: " + mTaskSharedData.getConfigurationSession().mShineConfiguration.toString());
                taskSucceed();
            } else {
                retry();
            }
        } else {
            MLog.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }
}
