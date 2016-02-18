/**
 * UnmapEventAnimationTask.java
 * Sync-SDK-Android
 * Created by TerryZhou on 2/14/16
 */
package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.utils.MLog;

import java.util.Hashtable;

/**
 * Note: execute this task only when it is Flash Button
 */
public class UnmapEventAnimationTask extends Task implements ShineProfile.ConfigurationCallback {

    private final static String TAG = "UnmapEventAnimationTask";

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
        proxy.unmapAllEventAnimation(this);
    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        if (actionID == ActionID.SET_SINGLE_ALARM_TIME || actionID == ActionID.CLEAR_ALL_ALARMS) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                // TODO: not need to unsubscribe ConfigCompletedCallback?
                taskSucceed();
            } else {
                retryAndIgnored();
            }
        } else {
            MLog.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }
}
