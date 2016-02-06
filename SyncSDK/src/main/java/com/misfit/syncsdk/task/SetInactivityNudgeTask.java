package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.utils.MLog;

import java.util.Hashtable;

public class SetInactivityNudgeTask extends Task implements ConnectionManager.ConfigCompletedCallback {

    private final static String TAG = "SetInactivityNudgeTask";

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            taskIgnored("proxy not prepared");
            return;
        }
        ConnectionManager.getInstance().subscribeConfigCompleted(mTaskSharedData.getSerialNumber(), this);
        if (mTaskSharedData.getSyncParams().inactivityNudgeSettings != null) {
            proxy.setInactivityNudge(mTaskSharedData.getSyncParams().inactivityNudgeSettings);
        } else {
            taskSucceed();
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
        if (actionID == ActionID.SET_INACTIVITY_NUDGE) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                taskSucceed();
            } else {
                retry();
            }
        } else {
            MLog.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }
}
