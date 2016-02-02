package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.utils.MLog;

import java.util.Hashtable;


/**
 * Created by Will Hou on 1/12/16.
 */
public class PlayAnimationTask extends Task implements ConnectionManager.ConfigCompletedCallback {

    private final static String TAG = "PlayAnimationTask";

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy != null && proxy.isConnected()) {
            ConnectionManager.getInstance().subscribeConfigCompleted(mTaskSharedData.getSerialNumber(), this);
            proxy.playAnimation();
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
        if (actionID == ActionID.ANIMATE) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                ConnectionManager.getInstance().unsubscribeConfigCompleted(mTaskSharedData.getSerialNumber(), this);
                taskSucceed();
            } else {
                retry();
            }
        } else {
            MLog.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }
}
