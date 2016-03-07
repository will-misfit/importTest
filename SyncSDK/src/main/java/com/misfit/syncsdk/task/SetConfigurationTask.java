package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.TimerManager;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.log.LogSession;
import com.misfit.syncsdk.model.PostCalculateData;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

import java.util.Hashtable;

/**
 * Task to set configuration
 */
public class SetConfigurationTask extends Task implements ShineProfile.ConfigurationCallback {
    private static final String TAG = "SetConfigurationTask";

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.SET_CONFIGURATION);
    }

    /**
     * ShineConfiguraiton fields:
     *   ClockState     - from Pedometer in App
     *   TripleTapState - from Pedometer in App
     *   ActivityTaggingState - from App
     *   ActivityPoint - from App, post calculation, dependent on ActivityDay in App
     *   GoalValue     - from Settings in App
     *   BatteryLevel  - from getShineConfiguration result
     * */
    @Override
    protected void execute() {
        mLogEvent.start();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProfileProxy is not ready");
            taskFailed("ShineSdkProfileProxy not prepared yet");
            return;
        }

        ShineConfiguration shineConfiguration = mTaskSharedData.getSyncParams().shineConfiguration;
        // if App does not send ShineConfiguraiton, use the saved one when getShineConfiguration
        if (shineConfiguration == null) {
            ConfigurationSession configSession = mTaskSharedData.getConfigurationSession();
            if (configSession != null) {
                shineConfiguration = configSession.mShineConfiguration;
            }
        }

        // set ShineConfiguraiton.ActivityPoint by the todayPoints by App
        PostCalculateData postCalculateData = mTaskSharedData.getPostCalculateDate();
        if (postCalculateData != null) {
            shineConfiguration.mActivityPoint = postCalculateData.todayPoints;
        }

        updateLogSessionFields(mTaskSharedData.getConfigurationSession());

        MLog.d(TAG, "execute()");
        updateExecuteTimer();

        ShineConfiguration shineConfig = mTaskSharedData.getSyncParams().shineConfiguration;
        proxy.setDeviceConfiguration(shineConfig, this);
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
        MLog.d(TAG, String.format("onConfigCompleted() actionId=%s, result=%s", actionID, resultCode));
        if (actionID == ActionID.SET_CONFIGURATION) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                // TODO: check the object got from Hashtable data with key of Shine_Configuration_Session
                mTaskSharedData.setConfigurationSession((ConfigurationSession) data.get(ShineProperty.SHINE_CONFIGURATION_SESSION));
                mLogEvent.end(LogEvent.RESULT_SUCCESS);
                taskSucceed();
            } else {
                mLogEvent.end(LogEvent.RESULT_FAILURE, "resultCode is " + resultCode);
                retry();
            }
        } else {
            String msg = String.format("unexpected actionID is %d, resultCode is %d", actionID, resultCode);
            mLogEvent.end(LogEvent.RESULT_FAILURE, msg);
            MLog.d(TAG, msg);
        }
    }

    private void updateLogSessionFields(ConfigurationSession configSession) {
        if (configSession == null || configSession.mShineConfiguration == null) {
            return;
        }

        LogSession logSession = mTaskSharedData.getLogSession();
        logSession.setPostSyncTimezone(configSession.mTimeZoneOffset);
        logSession.setPostClockState(configSession.mShineConfiguration.mClockState);
        logSession.setPostSyncGoal(configSession.mShineConfiguration.mGoalValue);
        logSession.setPostSyncActivityPoint(configSession.mShineConfiguration.mActivityPoint);
        logSession.setActivityTaggingState(configSession.mShineConfiguration.mActivityTaggingState);

        logSession.save();
    }
}
