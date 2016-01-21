package com.misfit.syncsdk;

import android.content.Context;
import android.util.Log;

import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.setting.pluto.GoalHitNotificationSettings;
import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.ShineEventAnimationMapping;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.ble.shine.log.ConnectFailCode;

import java.util.TimeZone;

/**
 * proxy of com.misfit.ble.shine.ShineProfile, part of previous com.misfitwearables.ShineSDKProvider
 */
public class ShineSdkProfileProxy {

    public static final String TAG = "ShineSdkProfileProxy";

    private static ShineProfile mShineProfile;


    protected ShineProfile.ConnectionCallback mConnectionCallback;
    protected ShineProfile.ConfigurationCallback mConfigurationCallBack;

    public ShineSdkProfileProxy(ShineProfile.ConnectionCallback connectionCallback,
                                ShineProfile.ConfigurationCallback configurationCallback) {
        mConnectionCallback = connectionCallback;
        mConfigurationCallBack = configurationCallback;
    }

    public boolean close() {
        if (mShineProfile != null) {
            mShineProfile.close();
            mShineProfile = null;
            return true;
        }
        return false;
    }

    public void startOTA(byte[] firmwareData, ShineProfile.OTACallback otaCallback) {
        if (mShineProfile != null) {
            mShineProfile.ota(firmwareData, otaCallback);
        } else {
            Log.d(TAG, "can't start OTAing, mShineProfile = null");
        }
    }

    public boolean connectProfile(ShineDevice device, Context context) {
        if (mConnectionCallback != null && device != null) {
            if (mShineProfile != null) {
                mShineProfile.close();
            }
            mShineProfile = device.connectProfile(context, false, mConnectionCallback);
            if (mShineProfile == null) {
                Log.d(TAG, "try to connect profile but return null");
                return false;
            }
        } else {
            Log.d(TAG, "can't connect profile, device = null || mShineCallback = null");
            return false;
        }

        return true;
    }

    public void startGettingDeviceConfiguration() {
        if (mShineProfile != null) {
            mShineProfile.getDeviceConfiguration(mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't get device config");
        }
    }

    public void startSettingDeviceConfig(ShineConfiguration shineConfiguration) {
        if (mShineProfile != null) {
            SetConfigurationSession setConfigurationSession = new SetConfigurationSession(shineConfiguration);
            setConfigurationSession.prepareSetTimeParams();
            mShineProfile.setDeviceConfiguration(setConfigurationSession, mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't set device config");
        }
    }

    public void startSyncing(ShineProfile.SyncCallback syncCallback) {
        if (mShineProfile != null) {
            mShineProfile.sync(syncCallback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't start sync");
        }
    }

    public void playAnimation() {
        if (mShineProfile != null) {
            mShineProfile.playAnimation(mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't play animation");
        }
    }

    public ShineDevice getDevice() {
        if (mShineProfile != null) {
            return mShineProfile.getDevice();
        } else {
            Log.d(TAG, "ShineProfile = null, can't get device");
            return null;
        }
    }

    public String getFirmwareVersion() {
        if (mShineProfile != null) {
            return mShineProfile.getFirmwareVersion();
        } else {
            Log.d(TAG, "ShineProfile = null, can't get firmware version");
            return null;
        }
    }

    public String getModelNumber() {
        if (mShineProfile != null) {
            return mShineProfile.getModelNumber();
        } else {
            Log.d(TAG, "ShineProfile = null, can't get model number");
            return null;
        }
    }

    public void startChangeSerialNumber(String newSerialNumber) {
        if (mShineProfile != null) {
            mShineProfile.changeSerialNumber(newSerialNumber, mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't reset serial number");
        }
    }

    public void startSettingConnectionParams(ShineConnectionParameters params) {
        if (mShineProfile != null) {
            mShineProfile.setConnectionParameters(params, mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't start setting connection params");
        }
    }


    // Venus support
    /* (non-Javadoc)
     * @see com.misfitwearables.prometheus.ble.AbstractShineProvider#startActivating()
	 */
    public void startActivating() {
        if (mShineProfile != null) {
            mShineProfile.activate(mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't start activating");
        }
    }

    public void startGettingActivationState() {
        if (mShineProfile != null) {
            mShineProfile.getActivationState(mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't start getting activation state");
        }
    }

    public void startStreamingUserInputEvents(ShineProfile.StreamingCallback streamingCallback) {
        if (mShineProfile != null) {
            mShineProfile.streamUserInputEvents(streamingCallback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't start streaming user input events");
        }
    }

    public void mapEventAnimation(ShineEventAnimationMapping[] mappings) {
        if (mShineProfile != null) {
            mShineProfile.mapEventAnimations(mappings, mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't map event user animation");
        }
    }

    public void startButtonAnimation(short animationId, short repeats) {
        if (mShineProfile != null) {
            mShineProfile.startButtonAnimation(animationId, repeats, mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't start button animation");
        }
    }

    public void unmapAllEventAnimation() {
        if (mShineProfile != null) {
            mShineProfile.unmapAllEventAnimation(mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't unmap all button animation");
        }
    }

    public void setSingleAlarm(AlarmSettings alarmSettings) {
        if (mShineProfile != null) {
            mShineProfile.setSingleAlarm(alarmSettings, mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't set single alarm");
        }
    }

    public void setCallTextNotification(NotificationsSettings notification) {
        if (mShineProfile != null) {
            mShineProfile.setCallTextNotifications(notification, mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't set call text notification");
        }
    }

    public void disableAllCallTextNotification() {
        if (mShineProfile != null) {
            mShineProfile.disableAllCallTextNotifications(mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't disable call text notification");
        }
    }

    public void setInactivityNudge(InactivityNudgeSettings inactivityNudgeSettings) {
        if (mShineProfile != null) {
            mShineProfile.setInactivityNudge(inactivityNudgeSettings, mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't set InactivityNudge");
        }
    }

    public void clearAllAlarms() {
        if (mShineProfile != null) {
            mShineProfile.clearAllAlarms(mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't clear all alarms");
        }
    }

    public void setHitGoalNotification(GoalHitNotificationSettings goalHitNotificationSettings) {
        if (mShineProfile != null) {
            mShineProfile.setGoalReachNotification(goalHitNotificationSettings, mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't set hit goal notification");
        }
    }

    private class SetConfigurationSession extends ConfigurationSession {
        public SetConfigurationSession(ShineConfiguration shineConfiguration) {
            super();
            mShineConfiguration = shineConfiguration;
        }

        private void prepareSetTimeParams() {
            long timestamp = System.currentTimeMillis();
            mTimestamp = timestamp / 1000;
            mPartialSecond = (short) (timestamp - mTimestamp * 1000);
            mTimeZoneOffset = (short) (TimeZone.getDefault().getOffset(timestamp) / 1000 / 60);
        }
    }

    public boolean sendCallNotification() {
        if (mShineProfile != null) {
            return mShineProfile.sendCallNotification(mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't send call notification");
            return false;
        }
    }

    public boolean sendTextNotification() {
        if (mShineProfile != null) {
            return mShineProfile.sendTextNotification(mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't send text notification");
            return false;
        }
    }

    public void interruptCurrentAction() {
        if (mShineProfile != null) {
            mShineProfile.interrupt();
        } else {
            Log.d(TAG, "ShineProfile = null, can't interrupt");
        }
    }

    public void stopAnimation() {
        if (mShineProfile != null) {
            mShineProfile.stopPlayingAnimation(mConfigurationCallBack);
        } else {
            Log.d(TAG, "ShineProfile = null, can't interrupt");
        }
    }

    public boolean isStreaming() {
        if (mShineProfile != null) {
            return mShineProfile.isStreaming();
        } else {
            Log.d(TAG, "ShineProfile = null, can't judge is streaming");
        }
        return false;
    }

    public boolean isConnected() {
        return (mShineProfile != null && mShineProfile.isConnected());
    }

    public void notifySessionStop() {
        if (mShineProfile != null) {
            mShineProfile.notifySessionStop();
        } else {
            Log.d(TAG, "ShineProfile = null, can't notify session stop");
        }
    }

    public int getConnectFailCode() {
        int reason = ConnectFailCode.CONNECT_SUCCESS;
        if (mShineProfile != null) {
            reason = mShineProfile.getConnectFailCodeEnum();
        }
        return reason;
    }
}
