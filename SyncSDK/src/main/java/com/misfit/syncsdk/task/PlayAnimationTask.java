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
public class PlayAnimationTask extends Task implements ShineProfile.ConfigurationCallback {

    private final static String TAG = "PlayAnimationTask";

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
        proxy.playAnimation(this);
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        if (actionID == ActionID.ANIMATE) {
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
