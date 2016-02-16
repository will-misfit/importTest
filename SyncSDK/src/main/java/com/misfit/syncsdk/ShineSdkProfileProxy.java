package com.misfit.syncsdk;

import android.content.Context;
import android.util.Log;

import com.misfit.ble.setting.flashlink.FlashButtonMode;
import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.setting.pluto.GoalHitNotificationSettings;
import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.ShineEventAnimationMapping;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfile.ConnectionCallback;
import com.misfit.ble.shine.ShineProfile.ConfigurationCallback;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.ble.shine.log.ConnectFailCode;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * proxy of com.misfit.ble.shine.ShineProfile to provide API of ShineProfile
 */
public class ShineSdkProfileProxy {

    public static final String TAG = "ShineSdkProfileProxy";

    private ShineProfile mShineProfile;

    public interface ConnectionStateCallback {
        void onConnectionStateChanged(ShineProfile.State newState);
    }

    protected ConnectionCallback mConnectionCallback = new ConnectionCallback() {
        @Override
        public void onConnectionStateChanged(ShineProfile shineProfile, ShineProfile.State newState) {
            if (mConnectionStateCallbacks.isEmpty()) {
                return;
            }
            for (ConnectionStateCallback callback : mConnectionStateCallbacks) {
                callback.onConnectionStateChanged(newState);
            }
        }
    };

    List<ConnectionStateCallback> mConnectionStateCallbacks = new ArrayList<>();

    public void subscribeConnectionStateChanged(ConnectionStateCallback connectionStateCallback) {
        if (mConnectionStateCallbacks.contains(connectionStateCallback)) {
            return;
        }
        mConnectionStateCallbacks.add(connectionStateCallback);
    }

    public void unsubscribeConnectionStateChanged(ConnectionStateCallback connectionStateCallback) {
        if (mConnectionStateCallbacks != null) {
            mConnectionStateCallbacks.remove(connectionStateCallback);
        }
    }

    public void releaseCallbacks() {
        mConnectionStateCallbacks.clear();
    }

    public void close() {
        if (mShineProfile != null) {
            mShineProfile.close();
            mShineProfile = null;
        }
    }

    public void startOTA(byte[] firmwareData, ShineProfile.OTACallback otaCallback) {
        if (mShineProfile != null) {
            mShineProfile.ota(firmwareData, otaCallback);
        } else {
            Log.d(TAG, "can't start OTA, mShineProfile is null");
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

    public void startGettingDeviceConfiguration(ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.getDeviceConfiguration(callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't get device config");
        }
    }

    public void setDeviceConfig(ShineConfiguration shineConfiguration, ConfigurationCallback callback) {
        if (mShineProfile != null) {
            SetConfigurationSession setConfigurationSession = new SetConfigurationSession(shineConfiguration);
            setConfigurationSession.prepareSetTimeParams();
            mShineProfile.setDeviceConfiguration(setConfigurationSession, callback);
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

    public void playAnimation(ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.playAnimation(callback);
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

    public void startChangeSerialNumber(String newSerialNumber, ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.changeSerialNumber(newSerialNumber, callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't reset serial number");
        }
    }

    public void startSettingConnectionParams(ShineConnectionParameters params, ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.setConnectionParameters(params, callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't start setting connection params");
        }
    }

    // Venus support
    /* (non-Javadoc)
     * @see com.misfitwearables.prometheus.ble.AbstractShineProvider#startActivating()
	 */
    public void startActivating(ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.activate(callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't start activating");
        }
    }

    public void startGettingActivationState(ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.getActivationState(callback);
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

    public void mapEventAnimation(ShineEventAnimationMapping[] mappings, ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.mapEventAnimations(mappings, callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't map event user animation");
        }
    }

    public void startButtonAnimation(short animationId, short repeats, ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.startButtonAnimation(animationId, repeats, callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't start button animation");
        }
    }

    public void unmapAllEventAnimation(ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.unmapAllEventAnimation(callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't unmap all button animation");
        }
    }

    public void setSingleAlarm(AlarmSettings alarmSettings, ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.setSingleAlarm(alarmSettings, callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't set single alarm");
        }
    }

    public void setCallTextNotification(NotificationsSettings notification, ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.setCallTextNotifications(notification, callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't set call text notification");
        }
    }

    public void disableAllCallTextNotification(ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.disableAllCallTextNotifications(callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't disable call text notification");
        }
    }

    public void setInactivityNudge(InactivityNudgeSettings inactivityNudgeSettings, ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.setInactivityNudge(inactivityNudgeSettings, callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't set InactivityNudge");
        }
    }

    public void clearAllAlarms(ShineProfile.ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.clearAllAlarms(callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't clear all alarms");
        }
    }

    public void setHitGoalNotification(GoalHitNotificationSettings goalHitNotificationSettings, ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.setGoalReachNotification(goalHitNotificationSettings, callback);
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

    public boolean sendCallNotification(ConfigurationCallback callback) {
        if (mShineProfile != null) {
            return mShineProfile.sendCallNotification(callback);
        } else {
            Log.d(TAG, "ShineProfile = null, can't send call notification");
            return false;
        }
    }

    public boolean sendTextNotification(ConfigurationCallback callback) {
        if (mShineProfile != null) {
            return mShineProfile.sendTextNotification(callback);
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

    public void stopAnimation(ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.stopPlayingAnimation(callback);
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

    public void setFlashButtonMode(FlashButtonMode buttonMode, ConfigurationCallback callback) {
        if (mShineProfile != null) {
            mShineProfile.setFlashButtonMode(buttonMode, callback);
        }
    }
}
