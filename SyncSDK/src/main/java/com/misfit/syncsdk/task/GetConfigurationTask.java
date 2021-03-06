package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.callback.ReadDataCallback;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.log.LogSession;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.MLog;

import java.util.Hashtable;

/**
 * Task to get Shine configuration
 */
public class GetConfigurationTask extends Task implements ShineProfile.ConfigurationCallback {

    private final static String TAG = "GetConfigurationTask";

    @Override
    protected void prepare() {
        mLogEvent = GeneralUtils.createLogEvent(LogEventType.GetConfiguration);
    }

    @Override
    protected void execute() {
        mLogEvent.start();
        ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (proxy == null || !proxy.isConnected()) {
            mLogEvent.end(LogEvent.RESULT_FAILURE, "ShineSdkProfileProxy is not ready");
            taskFailed("proxy not prepared");
            return;
        }

        MLog.d(TAG, "execute()");
        updateExecuteTimer();

        proxy.gettingDeviceConfiguration(this);
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

    /**
     * NOTE: the ShineConfiguration returned by Device needs to send to App invoker
     * */
    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
        MLog.d(TAG, String.format("onConfigCompleted() actionId=%s, result=%s", actionID, resultCode));
        if (actionID == ActionID.GET_CONFIGURATION) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                ConfigurationSession configSession = (ConfigurationSession)data.get(ShineProperty.SHINE_CONFIGURATION_SESSION);
                mTaskSharedData.setConfigurationSession(configSession);
                updateLogSessionFields(configSession);

                ReadDataCallback readDataCallback = mTaskSharedData.getReadDataCallback();
                if (readDataCallback != null) {
                    readDataCallback.onGetShineConfigurationCompleted(configSession);
                }

                mLogEvent.end(LogEvent.RESULT_SUCCESS, "");
                taskSucceed();
            } else {
                mLogEvent.end(LogEvent.RESULT_FAILURE, "resultCode is " + resultCode);
                retry();
            }
        } else {
            MLog.d(TAG, "unexpected action=" + actionID + ", result=" + resultCode);
        }
    }

    private void updateLogSessionFields(ConfigurationSession configSession) {
        if (configSession == null || configSession.mShineConfiguration == null) {
            return;
        }

        LogSession logSession = mTaskSharedData.getLogSession();
        logSession.setPreTimezone(configSession.mTimeZoneOffset);
        logSession.setPreClockState(configSession.mShineConfiguration.mClockState);
        logSession.setPreGoal(configSession.mShineConfiguration.mGoalValue);
        logSession.setPreActivityPoint(configSession.mShineConfiguration.mActivityPoint);
        logSession.setActivityTaggingState(configSession.mShineConfiguration.mActivityTaggingState);
        logSession.setBattery(configSession.mShineConfiguration.mBatteryLevel);

        logSession.save();
    }
}
