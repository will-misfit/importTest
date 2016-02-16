package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.utils.MLog;

import java.util.Hashtable;

public class StopAnimationTask extends Task implements ShineProfile.ConfigurationCallback {

    private final static String TAG = "StopAnimationTask";

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            taskFailed("proxy not prepared");
            return;
        }
        proxy.stopAnimation(this);
    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void cleanup() {

    }

    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        if (actionID == ActionID.STOP_ANIMATING) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                taskSucceed();
            } else {
                retryAndIgnored();
            }
        } else {
            MLog.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }
}
