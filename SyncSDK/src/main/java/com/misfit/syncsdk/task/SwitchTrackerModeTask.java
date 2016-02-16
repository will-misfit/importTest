package com.misfit.syncsdk.task;

import com.misfit.ble.setting.flashlink.FlashButtonMode;
import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.utils.MLog;

import java.util.Hashtable;

/**
 * when Flash Button want to sync with flagship app, its FlashButtonMode needs to set
 */
public class SwitchTrackerModeTask extends Task implements ShineProfile.ConfigurationCallback {

    private static final String TAG = "SwitchTrackerModeTask";

    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        if (actionID == ActionID.SET_FLASH_BUTTON_MODE) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                taskSucceed();
            } else {
                retryAndIgnored();
            }
        } else {
            MLog.d(TAG, "unexpected action = " + actionID + ", result = " + resultCode);
        }
    }

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
        proxy.setFlashButtonMode(FlashButtonMode.TRACKER, this);
    }

    @Override
    protected void onStop() {
    }

    @Override
    protected void cleanup() {
    }
}
