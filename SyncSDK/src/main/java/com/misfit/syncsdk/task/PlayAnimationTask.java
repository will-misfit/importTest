package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;

import java.util.Hashtable;


/**
 * Task to play animation
 */
public class PlayAnimationTask extends Task implements ShineProfile.ConfigurationCallback {

    private final static String TAG = "PlayAnimationTask";

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.PlayAnimation);
    }

    @Override
    protected void execute() {
        mLogEvent.start();

        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            MLog.d(TAG, "execute(), ShineSdkProfileProxy not ready");
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProfileProxy not ready");
            taskFailed("proxy not prepared");
            return;
        }

        MLog.d(TAG, "execute()");
        updateExecuteTimer();

        proxy.playAnimation(this);
    }

    @Override
    public void onStop() {
    }

    @Override
    protected void cleanup() {
        cancelCurrentTimerTask();
        mLogSession.appendEvent(mLogEvent);
        mLogEvent = null;
    }

    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        MLog.d(TAG, String.format("actionID %s, resultCode %s", actionID, resultCode));

        if (actionID == ActionID.ANIMATE) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
                taskSucceed();
            } else {
                retry();
            }
        }
    }
}
