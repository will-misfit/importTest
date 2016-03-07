package com.misfit.syncsdk.task;

import com.misfit.ble.setting.pluto.GoalHitNotificationSettings;
import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

import java.util.Hashtable;

/**
 * to set Hit Goal Notification settings
 */
public class SetHitGoalNotificationTask extends Task implements ShineProfile.ConfigurationCallback {
    private final static String TAG = "SetHitGoalNotification";

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.SET_GOAL_MET_NOTIFICATION);
    }

    @Override
    protected void execute() {
        mLogEvent.start();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProfileProxy is not ready");
            taskIgnored("proxy not prepared");
            return;
        }

        GoalHitNotificationSettings goalHitNotificationSettings = mTaskSharedData.getSyncParams().goalHitNotificationSettings;
        if (goalHitNotificationSettings == null) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "Hit Goal Notification settings is null");
            taskIgnored("Hit Goal Notification settings is null");
            return;
        }

        updateExecuteTimer();
        proxy.setHitGoalNotification(goalHitNotificationSettings, this);
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
        if (actionID == ActionID.SET_GOAL_HIT_NOTIFICATION) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
                taskSucceed();
            } else {
                mLogEvent.end(LogEvent.RESULT_FAILURE, "resultCode is " + resultCode);
                retryAndIgnored();
            }
        } else {
            MLog.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }
}
