package com.misfit.ble.sample;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.misfit.ble.sample.utils.Convertor;
import com.misfit.ble.setting.SDKSetting;
import com.misfit.ble.setting.flashlink.CustomModeEnum;
import com.misfit.ble.setting.flashlink.FlashButtonMode;
import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.setting.pluto.GoalHitNotificationSettings;
import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.ble.setting.pluto.PlutoSequence;
import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineAdapter;
import com.misfit.ble.shine.ShineAdapter.ShineScanCallback;
import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.ShineEventAnimationMapping;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.ShineStreamingConfiguration;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.ble.shine.result.Activity;
import com.misfit.ble.shine.result.SessionEvent;
import com.misfit.ble.shine.result.SwimSession;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.ble.shine.result.TapEventSummary;
import com.misfit.ble.shine.result.UserInputEvent;
import com.misfit.ble.util.MutableBoolean;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MisfitShineService extends Service {

    private static final String TAG = MisfitShineService.class.getSimpleName();

    public static final int SHINE_SERVICE_INITIALIZED = 0;
    public static final int SHINE_SERVICE_DISCOVERED = 1;
    public static final int SHINE_SERVICE_CONNECTED = 2;
    public static final int SHINE_SERVICE_CLOSED = 3;
    public static final int SHINE_SERVICE_OPERATION_END = 4;
    public static final int SHINE_SERVICE_OTA_RESET = 5;
    public static final int SHINE_SERVICE_RSSI_READ = 6;
    public static final int SHINE_SERVICE_OTA_PROGRESS_CHANGED = 7;
    public static final int SHINE_SERVICE_STREAMING_USER_INPUT_EVENTS_RECEIVED_EVENT = 8;
    public static final int SHINE_SERVICE_BUTTON_EVENTS = 9;
    public static final int SHINE_SERVICE_MESSAGE = 10;

    // Bundle Key
    public static final String EXTRA_DEVICE = "MisfitShineService.extra.device";
    public static final String EXTRA_RSSI = "MisfitShineService.extra.rssi";
    public static final String EXTRA_MESSAGE = "MisfitShineService.extra.message";
    public static final String EXTRA_SERIAL_STRING = "MisfitShineService.extra.serialstring";
    public static final String EXTRA_CONN_TIME = "MisfitShineService.extra.connecttime";
    /**
     * Connecting TimeOut Timer
     */
    private static int sConnectingTimeout = 45000;

    /**
     * Service's Binder
     */
    private final IBinder binder = new LocalBinder();
    protected Handler mHandler;
    protected Handler mDeviceDiscoveringHandler;
    private ShineProfile mShineProfile;
    private ShineAdapter mShineAdapter;
    private SyncResult mSummaryResult = null;

    private Timer mConnectingTimeOutTimer = new Timer();
    private ConnectingTimedOutTimerTask mCurrentConnectingTimeOutTimerTask = null;
    private long mConnTimerStartTime;

    private int lastConnErrorCode = -1;

    private byte[] mTempOtaFile;
    private int mAutoOtaCountDown;
    private final static int AUTO_OTA_MAX_COUNT = 50;

    /**
     * for external connect timeout listener
     */
    private ConnectTimeoutListener mExternalConnectTimeoutListener;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public MisfitShineService getService() {
            return MisfitShineService.this;
        }
    }

    /**
     * Set Up
     */
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            SDKSetting.setUp(this.getApplicationContext(), "user_cn@example.com");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        if (SDKSetting.isBleSupported(this)) {
            mShineAdapter = ShineAdapter.getDefaultAdapter(this);
        }
    }

    @Override
    public void onDestroy() {
        if (mShineProfile != null) {
            mShineProfile.close();
        }

        super.onDestroy();
    }

    public void setHandler(final Handler handler) {
        mHandler = handler;
    }

    public void setDeviceDiscoveringHandler(final Handler handler) {
        mDeviceDiscoveringHandler = handler;
    }

    /**
     * Callback
     */
    private ShineAdapter.ShineScanCallbackForTest mShineScanCallback = new ShineAdapter.ShineScanCallbackForTest() {
        @Override
        public void onScanResult(ShineDevice device, int rssi) {
            onDeviceFound(device, rssi);
        }
    };

    private ShineProfile.OTACallback otaCallback = new ShineProfile.OTACallback() {
        @Override
        public void onOTACompleted(ShineProfile.ActionResult resultCode) {
            if (ShineProfile.ActionResult.SUCCEEDED == resultCode) {
                mShineCallback_v1.onOTASucceeded();
            } else {
                mAutoOtaCountDown--;
                if (mAutoOtaCountDown < 0 || mTempOtaFile == null) {
                    Log.i(TAG, "auto continue ota, left try:" + mAutoOtaCountDown);
                    mShineCallback_v1.onOTAFailed();
                } else {
                    mShineProfile.ota(mTempOtaFile, otaCallback);
                }
            }
        }

        @Override
        public void onOTAProgressChanged(float progress) {
            mShineCallback_v1.onOTAProgressChanged(progress);
        }
    };

    private ShineProfile.SyncCallback syncCallback = new ShineProfile.SyncCallback() {
        @Override
        public void onSyncDataRead(Bundle extraInfo, MutableBoolean shouldStop) {
            mShineCallback_v1.onSyncDataReadProgress(extraInfo, shouldStop);
        }

        @Override
        public void onSyncDataReadCompleted(List<SyncResult> syncResults, MutableBoolean shouldStop) {
            mShineCallback_v1.onSyncDataReadCompleted(syncResults, shouldStop);
        }

        @Override
        public void onHardwareLogRead(byte[] hwLog) {
            Log.i(TAG, "hw log:{" + com.misfit.ble.util.Convertor.bytesToString(hwLog) + "}");
        }

        @Override
        public void onSyncCompleted(ShineProfile.ActionResult resultCode) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                mShineCallback_v1.onSyncSucceeded();
            } else {
                mShineCallback_v1.onSyncFailed();
            }
        }
    };

    private ShineProfile.StreamingCallback streamingCallback = new ShineProfile.StreamingCallback() {
        @Override
        public void onStreamingButtonEvent(int eventID) {
            mShineCallback_v1.onStreamingUserInputEventsReceivedEvent(eventID);
        }

        @Override
        public void onStreamingStarted(ShineProfile.ActionResult resultCode) {
            if (ShineProfile.ActionResult.SUCCEEDED == resultCode) {
                onOperationCompleted("STREAMING USER INPUT EVENTS STARTED");
            } else {
                mShineCallback_v1.onStreamingUserInputEventsFailed();
            }
        }

        @Override
        public void onStreamingStopped(ShineProfile.ActionResult resultCode) {
            if (ShineProfile.ActionResult.SUCCEEDED == resultCode) {
                mShineCallback_v1.onStreamingUserInputEventsEnded();
            } else {
                mShineCallback_v1.onStreamingUserInputEventsFailed();
            }
        }

        @Override
        public void onHeartbeatReceived() {
            onOperationCompleted("onHeartbeatReceived");
        }
    };

    private ShineProfile.ConfigurationCallback configurationCallback = new ShineProfile.ConfigurationCallback() {
        @Override
        public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
            switch (actionID) {
                case GET_CONFIGURATION:
                    ConfigurationSession session = null;
                    if (data != null) {
                        session = (ConfigurationSession) data.get(ShineProperty.SHINE_CONFIGURATION_SESSION);
                    }

                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onGettingDeviceConfigurationSucceeded(session);
                    } else {
                        mShineCallback_v1.onGettingDeviceConfigurationFailed(session);
                    }
                    break;
                case SET_CONFIGURATION:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onSettingDeviceConfigurationSucceeded();
                    } else {
                        mShineCallback_v1.onSettingDeviceConfigurationFailed();
                    }
                    break;
                case CHANGE_SERIAL_NUMBER:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onChangingSerialNumberSucceeded();
                    } else {
                        mShineCallback_v1.onChangingSerialNumberFailed();
                    }
                    break;
                case SET_CONNECTION_PARAMETERS: {
                    ShineConnectionParameters connectionParameters = null;
                    if (data != null) {
                        connectionParameters = (ShineConnectionParameters) data.get(ShineProperty.CONNECTION_PARAMETERS);
                    }

                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onSettingConnectionParametersSucceeded(connectionParameters);
                    } else {
                        mShineCallback_v1.onSettingConnectionParametersFailed(connectionParameters);
                    }
                    break;
                }
                case GET_CONNECTION_PARAMETERS: {
                    ShineConnectionParameters connectionParameters = null;
                    if (data != null) {
                        connectionParameters = (ShineConnectionParameters) data.get(ShineProperty.CONNECTION_PARAMETERS);
                    }

                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onGettingConnectionParametersSucceeded(connectionParameters);
                    } else {
                        mShineCallback_v1.onGettingConnectionParametersFailed(connectionParameters);
                    }
                    break;
                }
                case GET_FLASH_BUTTON_MODE:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        FlashButtonMode flashButtonMode = null;
                        if (data != null && data.get(ShineProperty.FLASH_BUTTON_MODE) != null) {
                            flashButtonMode = (FlashButtonMode) data.get(ShineProperty.FLASH_BUTTON_MODE);
                        }
                        mShineCallback_v1.onGettingFlashButtonModeSucceeded(flashButtonMode);
                    } else {
                        mShineCallback_v1.onGettingFlashButtonModeFailed();
                    }
                    break;
                case SET_FLASH_BUTTON_MODE:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onSettingFlashButtonModeSucceeded();
                    } else {
                        mShineCallback_v1.onSettingFlashButtonModeFailed();
                    }
                    break;
                case ANIMATE:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onPlayAnimationSucceeded();
                    } else {
                        mShineCallback_v1.onPlayAnimationFailed();
                    }
                    break;
                case ACTIVATE:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onActivateSucceeded();
                    } else {
                        mShineCallback_v1.onActivateFailed();
                    }
                    break;
                case GET_ACTIVATION_STATE:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        boolean wasActivated = false;
                        if (data != null && data.get(ShineProperty.ACTIVATION_STATE) != null) {
                            wasActivated = (Boolean) data.get(ShineProperty.ACTIVATION_STATE);
                        }
                        mShineCallback_v1.onGettingActivationStateSucceeded(wasActivated);
                    } else {
                        mShineCallback_v1.onGettingActivationStateFailed();
                    }
                    break;
                case READ_REMOTE_RSSI:
                    int rssi = -1;
                    if (data != null) {
                        rssi = (int) data.get(ShineProperty.RSSI);
                    }
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onReadRssiSucceeded(rssi);
                    } else {
                        mShineCallback_v1.onReadRssiFailed();
                    }
                    break;
                case GET_STREAMING_CONFIGURATION:
                    ShineStreamingConfiguration streamingConfiguration = null;
                    if (data != null) {
                        streamingConfiguration = (ShineStreamingConfiguration) data.get(ShineProperty.STREAMING_CONFIGURATION);
                    }
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onGettingStreamingConfigurationSucceeded(streamingConfiguration);
                    } else {
                        mShineCallback_v1.onGettingStreamingConfigurationFailed();
                    }
                    break;
                case SET_STREAMING_CONFIGURATION:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onSettingStreamingConfigurationSucceeded();
                    } else {
                        mShineCallback_v1.onSettingStreamingConfigurationFailed();
                    }
                    break;
                case MAP_EVENT_ANIMATION:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onMapEventAnimationSucceeded();
                    } else {
                        mShineCallback_v1.onMapEventAnimationFailed();
                    }
                    break;
                case START_BUTTON_ANIMATION:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onStartButtonAnimationSucceeded();
                    } else {
                        mShineCallback_v1.onStartButtonAnimationFailed();
                    }
                    break;
                case UNMAP_ALL_EVENT_ANIMATION:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onUnmapAllEventAnimationSucceeded();
                    } else {
                        mShineCallback_v1.onUnmapAllEventAnimationFailed();
                    }
                    break;
                case EVENT_MAPPING_SYSTEM_CONTROL:
                    if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                        mShineCallback_v1.onSystemControlEventMappingSucceeded();
                    } else {
                        mShineCallback_v1.onSystemControlEventMappingFailed();
                    }
                    break;
                case GET_EXTRA_ADV_DATA_STATE:
                    if (ShineProfile.ActionResult.SUCCEEDED == resultCode) {
                        boolean enabled = false;
                        if (data != null && data.get(ShineProperty.EXTRA_ADVERTISING_DATA_STATE) != null) {
                            enabled = (Boolean) data.get(ShineProperty.EXTRA_ADVERTISING_DATA_STATE);
                        }
                        mShineCallback_v1.onGettingExtraAdvertisingDataStateRequestSucceeded(enabled);
                    } else {
                        mShineCallback_v1.onGettingExtraAdvertisingDataStateRequestFailed();
                    }
                    break;
                case SET_EXTRA_ADV_DATA_STATE:
                    if (ShineProfile.ActionResult.SUCCEEDED == resultCode) {
                        mShineCallback_v1.onSettingExtraAdvertisingDataStateRequestSucceeded();
                    } else {
                        mShineCallback_v1.onSettingExtraAdvertisingDataStateRequestFailed();
                    }
                    break;
                default:
                    onOperationCompleted("Received configurationCallback - action: " + actionID + ", result: " + resultCode + ", data: " + data);
                    break;
            }
        }
    };

    /**
     * Public Interface - Scanning
     */
    public boolean startScanning() {
        if (mShineAdapter == null)
            return false;

        boolean result = true;
        try {
            mShineAdapter.startScanning(mShineScanCallback);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    public void stopScanning() {
        if (mShineAdapter == null)
            return;

        mShineAdapter.stopScanning(mShineScanCallback);
    }

    /**
     * Public Interface - Bonding
     */
    public boolean createBond(ShineDevice device) {
        return device != null && device.createBond();
    }

    public boolean removeBond(ShineDevice device) {
        return device != null && device.removeBond();
    }

    public boolean getConnectedShines() {
        if (mShineAdapter == null)
            return false;

        mShineAdapter.getConnectedShines(new ShineAdapter.ShineRetrieveCallback() {
            @Override
            public void onConnectedShinesRetrieved(List<ShineDevice> connectedShines) {
                for (ShineDevice device : connectedShines) {
                    onDeviceFound(device, 0);
                }
            }
        });

        return true;
    }

    private void onDeviceFound(ShineDevice device, int rssi) {
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(MisfitShineService.EXTRA_DEVICE, device);
        mBundle.putString(MisfitShineService.EXTRA_SERIAL_STRING, device.getSerialNumber());
        mBundle.putInt(MisfitShineService.EXTRA_RSSI, rssi);

        Message msg = Message.obtain(mDeviceDiscoveringHandler, SHINE_SERVICE_DISCOVERED);
        msg.setData(mBundle);
        msg.sendToTarget();
    }

    /**
     * Public Interface - Operate
     */
    public boolean connect(ShineDevice device) {
        try {
            if (mShineProfile != null) {
                mShineProfile.close();
            }

            if (device.isInvalid()) {
                Toast.makeText(this, "ShineDevice instance has become INVALID. Please scan for it again!", Toast.LENGTH_SHORT).show();
                return false;
            }

            mShineProfile = device.connectProfile(this, false, new ShineProfile.ConnectionCallbackForTest() {
                @Override
                public void onConnectionStateChanged(ShineProfile shineProfile, ShineProfile.State newState) {
                    boolean isConnected = (ShineProfile.State.CONNECTED == newState);
                    mShineCallback_v1.onConnectionStateChanged(shineProfile, isConnected);
                }

                @Override
                public void onConnectionStateChangedForTest(ShineProfile shineProfile, int status, int newState, int failCode) {
                    Log.w(TAG, String.format("onConnectionStateChange(), status=%d, newState=%d, timestamp=%d, failCode=%d",
                        status, newState, System.currentTimeMillis(), failCode));
                }
            });
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }

        if (mShineProfile == null)
            return false;

        startConnectionTimeOutTimer();
        return true;
    }

    /**
     * overload connect() with param of ConnectTimeoutListener
     * @param device
     * @param connTimeoutListener
     * @return
     */
    public boolean connect(ShineDevice device, ConnectTimeoutListener connTimeoutListener) {
        setConnectTimeoutListener(connTimeoutListener);
        return connect(device);
    }

    private ShineDevice.ShineHIDConnectionCallback mHIDConnectionCallback = new ShineDevice.ShineHIDConnectionCallback() {
        @Override
        public void onHIDConnectionStateChanged(ShineDevice device, int state) {
            Bundle mBundle = new Bundle();
            mBundle.putString(EXTRA_MESSAGE, device.getAddress() + " - HIDConnectionStateChanged, BluetoothProfile.State=" + state);

            Message msg = Message.obtain(mHandler, SHINE_SERVICE_MESSAGE);
            msg.setData(mBundle);
            msg.sendToTarget();
        }
    };

    public boolean hidConnect(ShineDevice device) {
        if (device == null)
            return false;

        device.registerHIDConnectionCallback(mHIDConnectionCallback);
        return device.hidConnect();
    }

    public boolean hidDisconnect(ShineDevice device) {
        if (device == null)
            return false;

        device.registerHIDConnectionCallback(mHIDConnectionCallback);
        return device.hidDisconnect();
    }

    public int getDeviceFamily() {
        return mShineProfile.getDeviceFamily();
    }

    public boolean isBusy() {
        return mShineProfile != null && mShineProfile.isBusy();
    }

    public boolean isStreaming() {
        return mShineProfile != null && mShineProfile.isBusy();
    }

    public boolean isUserEventStreaming(){
        return mShineProfile != null && mShineProfile.isStreaming();
    }

    public boolean isReady() {
        return mShineProfile != null && mShineProfile.isReady();
    }

    public void startGettingDeviceConfiguration() {
        mShineProfile.getDeviceConfiguration(configurationCallback);
    }

    private class SetConfigurationSession extends ConfigurationSession {
        public SetConfigurationSession(ShineConfiguration shineConfiguration) {
            super();
            mShineConfiguration = shineConfiguration;
        }

        private void prepareSetTimeParams() {
            long timestamp = System.currentTimeMillis();
            mTimestamp = timestamp / 1000;
            mPartialSecond = (short)(timestamp - mTimestamp * 1000);
            mTimeZoneOffset = (short)(TimeZone.getDefault().getOffset(timestamp) / 1000 / 60);
        }
    }

    public void startSettingDeviceConfiguration(String paramsString) {
        ShineConfiguration shineConfiguration = new ShineConfiguration();

        if (paramsString != null) {
            String[] params = paramsString.split(",");
            if (params.length == 5) {
                shineConfiguration.mActivityPoint = Long.parseLong(params[0].trim());
                shineConfiguration.mGoalValue = Long.parseLong(params[1].trim());
                shineConfiguration.mClockState = Byte.parseByte(params[2].trim());
                shineConfiguration.mTripleTapState = Byte.parseByte(params[3].trim());
                shineConfiguration.mActivityTaggingState = Byte.parseByte(params[4].trim());
            } else {
                Toast.makeText(this, "Please input the following fields: [point], [goal], [clockState], [tripleTapState] and [activityTaggingState]", Toast.LENGTH_SHORT).show();
                configurationCallback.onConfigCompleted(ActionID.SET_CONFIGURATION, ShineProfile.ActionResult.FAILED, null);
                return;
            }
        }

        SetConfigurationSession configurationSession = new SetConfigurationSession(shineConfiguration);
        configurationSession.prepareSetTimeParams();

        mShineProfile.setDeviceConfiguration(configurationSession, configurationCallback);
    }

    public void startSync() {
        mSummaryResult = new SyncResult();
        mShineProfile.sync(syncCallback);
    }

    public void startOTAing(byte[] firmwareData, boolean shouldAutoRetry) {
        if (shouldAutoRetry) {
            mTempOtaFile = firmwareData;
            mAutoOtaCountDown = AUTO_OTA_MAX_COUNT;
        }
        mShineProfile.ota(firmwareData, otaCallback);
    }

    public void startChangingSerialNumber(String serialNumber) {
        mShineProfile.changeSerialNumber(serialNumber, configurationCallback);
    }

    public void startSettingConnectionParameters(String connectionParameters) {
        ShineConnectionParameters parameters = null;
        if (connectionParameters != null) {
            String[] components = connectionParameters.split(",");
            if (components.length >= 3) {
                parameters = new ShineConnectionParameters(Double.parseDouble(components[0].trim()),
                    Integer.parseInt(components[1].trim()), Integer.parseInt(components[2].trim()));
            }
        }
        mShineProfile.setConnectionParameters(parameters, configurationCallback);
    }

    public void startGettingConnectionParameters() {
        mShineProfile.getConnectionParameters(configurationCallback);
    }

    public void startSettingFlashButtonMode(String paramsString) {
        FlashButtonMode flashButtonMode = FlashButtonMode.CUSTOM_MODE;
        if (paramsString != null) {
            String[] components = paramsString.split(",");
            if (components.length >= 1) {
                try {
                    short id = Short.parseShort(components[0]);
                    if (id >= 1 && id <= 7) {
                        flashButtonMode = FlashButtonMode.get(id);
                    }
                } catch (NumberFormatException nfe) {
                    return;
                }
            } else {
                return;
            }
        }
        mShineProfile.setFlashButtonMode(flashButtonMode, configurationCallback);
    }

    public void startGettingFlashButtonMode() {
        mShineProfile.getFlashButtonMode(configurationCallback);
    }

    public void readRssi() {
        if (mShineProfile != null) {
            mShineProfile.readRssi(configurationCallback);
        }
    }

    public void playAnimation() {
        mShineProfile.playAnimation(configurationCallback);
    }

    public void stopPlayingAnimation() {
        mShineProfile.stopPlayingAnimation(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("stopPlayingAnimationSucceeded");
                } else {
                    onOperationCompleted("stopPlayingAnimationFailed");
                }
            }
        });
    }

    public void startActivating() {
        mShineProfile.activate(configurationCallback);
    }

    public void startGettingActivationState() {
        mShineProfile.getActivationState(configurationCallback);
    }

    public void startStreamingUserInputEvents() {
        mShineProfile.streamUserInputEvents(streamingCallback);
    }

    public void startGettingStreamingConfig() {
        mShineProfile.getStreamingConfiguration(configurationCallback);
    }

    public void startSettingStreamingConfig(String paramsString) {
        ShineStreamingConfiguration configuration = new ShineStreamingConfiguration();

        if (paramsString != null) {
            String[] params = paramsString.split(",");
            if (params.length == 2) {
                configuration.mNumberOfMappedEventPackets = Integer.parseInt(params[0].trim());
                configuration.mConnectionHeartbeatInterval = Long.parseLong(params[1].trim());
            } else {
                Toast.makeText(this, "Please input the following fields: [numberOfMappedEventPackets], [connectionHeartbeatInterval]", Toast.LENGTH_SHORT).show();
                configurationCallback.onConfigCompleted(ActionID.SET_STREAMING_CONFIGURATION, ShineProfile.ActionResult.FAILED, null);
                return;
            }
        }

        mShineProfile.setStreamingConfiguration(configuration, configurationCallback);
    }

    public void startButtonAnimation(byte animationId, byte numOfRepeats) {
        mShineProfile.startButtonAnimation(animationId, numOfRepeats, configurationCallback);
    }

    public void startButtonAnimation(String paramsString) {
        if (paramsString != null) {
            String[] params = paramsString.split(",");
            if (params.length == 2) {
                short animationId = Short.parseShort(params[0]);
                short numOfRepeats = Short.parseShort(params[1]);
                mShineProfile.startButtonAnimation(animationId, numOfRepeats, configurationCallback);
                return;
            }
        }

        Toast.makeText(this, "Please input the following fields: [animation id], [num of repeats]", Toast.LENGTH_SHORT).show();
        configurationCallback.onConfigCompleted(ActionID.SET_STREAMING_CONFIGURATION, ShineProfile.ActionResult.FAILED, null);
    }

    public void mapEventAnimation(String paramsString) {
        if (paramsString != null) {
            String[] params = paramsString.split(",");
            if (params.length % 7 == 0) {
                ShineEventAnimationMapping[] mappings = new ShineEventAnimationMapping[params.length / 7];

                for (int i = 0; i < params.length; i += 7) {
                    short eventId = Short.parseShort(params[i]);
                    short activeAndConnectedDefaultAnimation = Short.parseShort(params[i + 1]);
                    short activeAndConnectedDefaultAnimationRepeat = Short.parseShort(params[i + 2]);
                    short unconnectedDefaultAnimation = Short.parseShort(params[i + 3]);
                    short unconnectedDefaultAnimationRepeat = Short.parseShort(params[i + 4]);
                    short timeoutAnimation = Short.parseShort(params[i + 5]);
                    short timeoutAnimationRepeat = Short.parseShort(params[i + 6]);

                    ShineEventAnimationMapping mapping = new ShineEventAnimationMapping(
                            eventId,
                            activeAndConnectedDefaultAnimation, activeAndConnectedDefaultAnimationRepeat,
                            unconnectedDefaultAnimation, unconnectedDefaultAnimationRepeat,
                            timeoutAnimation, timeoutAnimationRepeat);

                    mappings[i / 7] = mapping;
                }

                mShineProfile.mapEventAnimations(mappings, configurationCallback);
                return;
            }
        }

        Toast.makeText(this, "Please input the following fields: "
                + "[event id], "
                + "[active animation id], [active animation repeat], "
                + "[unconnected animation id], [unconnected animation repeat], "
                + "[timeout animation id], [timeout animation repeat], ...", Toast.LENGTH_SHORT).show();
        configurationCallback.onConfigCompleted(ActionID.MAP_EVENT_ANIMATION, ShineProfile.ActionResult.FAILED, null);
    }

    public void unmapEventAnimation() {
        mShineProfile.unmapAllEventAnimation(configurationCallback);
    }

    public void systemControlEventMapping(String paramsString) {
        if (paramsString != null) {
            String[] params = paramsString.split(",");
            if (params.length == 1) {
                short controlBits = Short.parseShort(params[0]);
                mShineProfile.systemControlEventMapping(controlBits, configurationCallback);
                return;
            }
        }

        Toast.makeText(this, "Please input the following fields: [controlBits]", Toast.LENGTH_SHORT).show();
        configurationCallback.onConfigCompleted(ActionID.EVENT_MAPPING_SYSTEM_CONTROL, ShineProfile.ActionResult.FAILED, null);
    }

    public void close() {
        if (mShineProfile != null) {
            lastConnErrorCode = mShineProfile.getConnectFailCodeEnum();
            mShineProfile.close();
        }
    }

    public void startSettingAdvEventState(boolean enable) {
        mShineProfile.setExtraAdvertisingDataState(enable, configurationCallback);
    }

    public boolean startGettingAdvEventState() {
        return mShineProfile.getExtraAdvertisingDataState(configurationCallback);
    }

    public void interrupt() {
        mShineProfile.interrupt();
    }

    /**
     * Pluto
     */
    public boolean startSettingInactivityNudge(InactivityNudgeSettings inactivityNudgeSettings) {
        return mShineProfile.setInactivityNudge(inactivityNudgeSettings, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onSettingInactivityNudgeSucceed");
                } else {
                    onOperationCompleted("onSettingInactivityNudgeFailed");
                }
            }
        });
    }

    public boolean startGettingInactivityNudge() {
        return mShineProfile.getInactivityNudge(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    if (data == null || data.get(ShineProperty.INACTIVITY_NUDGE_SETTINGS) == null) {
                        Log.e(TAG, "startGettingInactivityNudge: no data");
                        return;
                    }

                    InactivityNudgeSettings inactivityNudgeSettings = (InactivityNudgeSettings) data.get(ShineProperty.INACTIVITY_NUDGE_SETTINGS);
                    onOperationCompleted("onGettingInactivityNudgeSucceed\n" + inactivityNudgeSettings.toString());
                } else {
                    onOperationCompleted("onGettingInactivityNudgeFailed");
                }
            }
        });
    }

    public boolean startSettingAlarm(AlarmSettings alarmSettings) {
        return mShineProfile.setSingleAlarm(alarmSettings, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onSettingAlarmSucceed");
                } else {
                    onOperationCompleted("onSettingAlarmFailed");
                }
            }
        });
    }

    public boolean startGettingAlarm() {
        return mShineProfile.getSingleAlarm(AlarmSettings.ALL_DAYS, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    if (data == null || data.get(ShineProperty.ALARM_SETTINGS) == null) {
                        onOperationCompleted("onGettingAlarmSucceed\n" + "null alarmSetting");
                        return;
                    }

                    AlarmSettings alarmSettings = (AlarmSettings) data.get(ShineProperty.ALARM_SETTINGS);
                    onOperationCompleted("onGettingAlarmSucceed\n" + alarmSettings.toString());
                } else {
                    onOperationCompleted("onGettingAlarmFailed");
                }
            }
        });
    }

    public boolean startClearingAllAlarms() {
        return mShineProfile.clearAllAlarms(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onClearingAllAlarmsSucceed");
                } else {
                    onOperationCompleted("onClearingAllAlarmsFailed");
                }
            }
        });
    }

    public boolean startSettingGoalHitNotification(GoalHitNotificationSettings goalHitNotificationSettings) {
        return mShineProfile.setGoalReachNotification(goalHitNotificationSettings, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onSettingGoalHitNotificationSucceed");
                } else {
                    onOperationCompleted("onSettingGoalHitNotificationFailed");
                }
            }
        });
    }

    public boolean startGettingGoalHitNotification() {
        return mShineProfile.getGoalReachNotification(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    if (data == null || data.get(ShineProperty.GOAL_HIT_NOTIFICATION_SETTINGS) == null) {
                        Log.e(TAG, "startGettingInactivityNudge: no data");
                        return;
                    }

                    GoalHitNotificationSettings goalHitNotificationSettings = (GoalHitNotificationSettings) data.get(ShineProperty.GOAL_HIT_NOTIFICATION_SETTINGS);
                    onOperationCompleted("onGettingGoalHitNotificationSucceed\n" + goalHitNotificationSettings.toString());
                } else {
                    onOperationCompleted("onGettingGoalHitNotificationFailed");
                }
            }
        });
    }

    public boolean startSettingCallTextNotification(NotificationsSettings notificationsSettings) {
        return mShineProfile.setCallTextNotifications(notificationsSettings, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onSettingCallTextNotificationsSucceed");
                } else {
                    onOperationCompleted("onSettingCallTextNotificationsFailed");
                }
            }
        });
    }

    public boolean startGettingCallTextNotification() {
        return mShineProfile.getCallTextNotifications(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    if (data == null || data.get(ShineProperty.CALL_TEXT_NOTIFICATION_SETTINGS) == null) {
                        Log.e(TAG, "startGettingInactivityNudge: no data");
                        return;
                    }

                    NotificationsSettings notificationsSettings = (NotificationsSettings) data.get(ShineProperty.CALL_TEXT_NOTIFICATION_SETTINGS);
                    onOperationCompleted("onGettingCallTextNotificationsSucceed\n" + notificationsSettings.toString());
                } else {
                    onOperationCompleted("onGettingCallTextNotificationsFailed");
                }
            }
        });
    }

    public boolean startSendingCallNotification() {
        return mShineProfile.sendCallNotification(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onSendingCallNotificationSucceed");
                } else {
                    onOperationCompleted("onSendingCallNotificationFailed");
                }
            }
        });
    }

    public boolean startSendingTextNofication() {
        return mShineProfile.sendTextNotification(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onSendingTextNotificationSucceed");
                } else {
                    onOperationCompleted("onSendingTextNotificationFailed");
                }
            }
        });
    }

    public boolean startSendingStopNofication() {
        return mShineProfile.sendStopNotification(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onSendingStopNotificationSucceed");
                } else {
                    onOperationCompleted("onSendingStopNotificationFailed");
                }
            }
        });
    }

    public boolean startDisablingAllNofications() {
        return mShineProfile.disableAllCallTextNotifications(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onDisablingAllNotificationsSucceed");
                } else {
                    onOperationCompleted("onDisablingAllNotificationsFailed");
                }
            }
        });
    }

    public boolean playLEDAnimation(PlutoSequence.LED sequence, short mRepeat, int milliSecondsRepeat) {
        return mShineProfile.playLEDAnimation(sequence, mRepeat, milliSecondsRepeat, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onPlayingLEDAnimationSucceed");
                } else {
                    onOperationCompleted("onPlayingLEDAnimationFailed");
                }
            }
        });
    }

    public boolean playVibration(PlutoSequence.Vibe sequence, short mRepeat, int milliSecondsRepeat) {
        return mShineProfile.playVibration(sequence, mRepeat, milliSecondsRepeat, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onPlayingVibrationSucceed");
                } else {
                    onOperationCompleted("onPlayingVibrationFailed");
                }
            }
        });
    }

    public boolean playSound(PlutoSequence.Sound sequence, short mRepeat, int milliSecondsRepeat) {
        return mShineProfile.playSound(sequence, mRepeat, milliSecondsRepeat, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onPlayingSoundSucceed");
                } else {
                    onOperationCompleted("onPlayingSoundFailed");
                }
            }
        });
    }

    /**
     * Bolt
     */
    public boolean addGroupId(short groupId) {
        return mShineProfile.addGroupId(groupId, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("addGroupIdSucceed");
                } else {
                    onOperationCompleted("addGroupIdFailed");
                }
            }
        });
    }

    public boolean getGroupId() {
        return mShineProfile.getGroupId(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    if (data == null || data.get(ShineProperty.GROUP_ID) == null) {
                        Log.e(TAG, "getGroupId: no data");
                        return;
                    }

                    short groupId = (short) data.get(ShineProperty.GROUP_ID);
                    onOperationCompleted("getGroupIdSucceeded\n" + groupId);
                } else {
                    onOperationCompleted("getGroupIdFailed");
                }
            }
        });
    }

    public boolean setPasscode(byte[] passcode) {
        return mShineProfile.setPasscode(passcode, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("setPasscodeSucceed");
                } else {
                    onOperationCompleted("setPasscodeFailed");
                }
            }
        });
    }

    public boolean getPasscode() {
        return mShineProfile.getPasscode(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    if (data == null || data.get(ShineProperty.PASSCODE) == null) {
                        Log.e(TAG, "getPasscode: no data");
                        return;
                    }

                    byte[] passcode = (byte[]) data.get(ShineProperty.PASSCODE);
                    onOperationCompleted("getPasscodeSucceeded\n" + Convertor.bytesToHex(passcode));
                } else {
                    onOperationCompleted("getPasscodeFailed");
                }
            }
        });
    }


    /**
     * Connection Timer
     */
    private void onConnectingTimedOut(ConnectingTimedOutTimerTask timerTask) {
        if (timerTask == mCurrentConnectingTimeOutTimerTask) {
            mCurrentConnectingTimeOutTimerTask = null;
            close();
        }
        if (mExternalConnectTimeoutListener != null) {
            mExternalConnectTimeoutListener.onShineConnectTimeout();
        }
    }

    private void startConnectionTimeOutTimer() {
        stopConnectionTimeOutTimer();
        mCurrentConnectingTimeOutTimerTask = new ConnectingTimedOutTimerTask();
        mConnectingTimeOutTimer.schedule(mCurrentConnectingTimeOutTimerTask, sConnectingTimeout);
        mConnTimerStartTime = Calendar.getInstance().getTimeInMillis();
    }

    public void stopConnectionTimeOutTimer() {
        if (mCurrentConnectingTimeOutTimerTask != null) {
            mCurrentConnectingTimeOutTimerTask.mIsCancelled = true;
            mCurrentConnectingTimeOutTimerTask.cancel();
        }
    }

    private class ConnectingTimedOutTimerTask extends TimerTask {
        public boolean mIsCancelled = false;

        public ConnectingTimedOutTimerTask() {
            mIsCancelled = false;
        }

        @Override
        public void run() {
            if (!mIsCancelled) {
                MisfitShineService.this.onConnectingTimedOut(this);
            }
        }
    }

    /**
     * Util
     */
    private String buildSyncResultString(SyncResult syncResult) {
        StringBuilder stringBuilder = new StringBuilder();
        if (syncResult != null) {
            if (syncResult.mSwimSessions != null) {
                for (SwimSession swimSession : syncResult.mSwimSessions) {
                    stringBuilder.append(String.format("\nSwimSession - " + swimSession.toString()));
                }
            }

            if (syncResult.mTapEventSummarys != null) {
                for (TapEventSummary tapEventSummary : syncResult.mTapEventSummarys) {
                    stringBuilder.append("\nTapEventSummary - timestamp:" + tapEventSummary.mTimestamp +
                            " tapType:" + tapEventSummary.mTapType +
                            " tapCount:" + tapEventSummary.mCount);
                }
            }

            if (syncResult.mSessionEvents != null) {
                for (SessionEvent sessionEvent : syncResult.mSessionEvents) {
                    stringBuilder.append("\nSessionEvent - timestamp:" + sessionEvent.mTimestamp +
                            " eventType:" + sessionEvent.mType);
                }
            }

            int totalPoint = 0;
            int totalSteps = 0;
            if (syncResult.mActivities != null) {
                for (Activity activity : syncResult.mActivities) {
                    totalPoint += activity.mPoints;
                    totalSteps += activity.mBipedalCount;
                }
            }
            stringBuilder.append("\nActivity - totalPoint:" + totalPoint + " totalSteps:" + totalSteps);
        }
        return stringBuilder.toString();
    }

    private String buildShineConfigurationString(ConfigurationSession session) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\nTimeStamp: " + session.mTimestamp);
        stringBuilder.append("\nPartialSecond: " + session.mPartialSecond);
        stringBuilder.append("\nTimeZoneOffset: " + session.mTimeZoneOffset);

        stringBuilder.append("\nActivityPoint: " + session.mShineConfiguration.mActivityPoint);
        stringBuilder.append("\nGoalValue: " + session.mShineConfiguration.mGoalValue);
        stringBuilder.append("\nClockState: " + session.mShineConfiguration.mClockState);
        stringBuilder.append("\nTripleTapState: " + session.mShineConfiguration.mTripleTapState);
        stringBuilder.append("\nActivityTaggingState: " + session.mShineConfiguration.mActivityTaggingState);
        stringBuilder.append("\nBatteryLevel: " + session.mShineConfiguration.mBatteryLevel);

        return stringBuilder.toString();
    }

    private String buildConnectionParametersString(ShineConnectionParameters connectionParameters) {
        if (connectionParameters == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\nConnection Interval: " + connectionParameters.getConnectionInterval());
        stringBuilder.append("\nConnection Latency: " + connectionParameters.getConnectionLatency());
        stringBuilder.append("\nSupervision Timeout: " + connectionParameters.getSupervisionTimeout());

        return stringBuilder.toString();
    }

    private String buildUserInputEventString(int eventID) {
        String eventName = "unknown";

        switch (eventID) {
            case UserInputEvent.EVENT_TYPE_NO_EVENT:
            case UserInputEvent.EVENT_TYPE_CONSUMED:
                eventName = "INTERNAL EVENT";
                break;
            case UserInputEvent.EVENT_TYPE_TIMEOUT:
                eventName = "TIMEOUT";
                break;
            case UserInputEvent.EVENT_TYPE_END_OF_ANIMATION:
                eventName = "END OF ANIMATION";
                break;
            case UserInputEvent.EVENT_TYPE_END_OF_SEQUENCE:
                eventName = "END OF SEQUENCE";
                break;
            case UserInputEvent.EVENT_TYPE_CONNECTION_CLOSE:
                eventName = "CONNECTION CLOSE";
                break;

            case UserInputEvent.EVENT_TYPE_BUTTON_PRESS:
                eventName = "BUTTON_PRESS";
                break;
            case UserInputEvent.EVENT_TYPE_BUTTON_RELEASE_AFTER_SHORT_PRESS:
                eventName = "BUTTON_PRESS_RELEASE";
                break;
            case UserInputEvent.EVENT_TYPE_BUTTON_LONG_PRESS:
                eventName = "BUTTON_LONG_PRESS";
                break;
            case UserInputEvent.EVENT_TYPE_BUTTON_RELEASE_AFTER_LONG_PRESS:
                eventName = "BUTTON_LONG_PRESS_RELEASE";
                break;

            case UserInputEvent.EVENT_TYPE_BUTTON_SINGLE_PRESS:
                eventName = "SINGLE_PRESS";
                break;
            case UserInputEvent.EVENT_TYPE_BUTTON_DOUBLE_PRESS:
                eventName = "DOUBLE_PRESS";
                break;
            case UserInputEvent.EVENT_TYPE_BUTTON_TRIPLE_PRESS:
                eventName = "TRIPLE_PRESS";
                break;
            case UserInputEvent.EVENT_TYPE_BUTTON_DOUBLE_PRESS_AND_HOLD:
                eventName = "DOUBLE_PRESS_AND_HOLD";
                break;
            case UserInputEvent.EVENT_TYPE_BUTTON_TRIPLE_PRESS_AND_HOLD:
                eventName = "TRIPLE_PRESS_AND_HOLD";
                break;
            case UserInputEvent.EVENT_TYPE_QUADRA_TAP_BEGIN:
                eventName = "QUADRA_TAP_BEGIN";
                break;
            case UserInputEvent.EVENT_TYPE_QUADRA_TAP_END:
                eventName = "QUADRA_TAP_END";
                break;
            case UserInputEvent.EVENT_TYPE_SINGLE_TAP:
                eventName = "SINGLE_TAP";
                break;
            case UserInputEvent.EVENT_TYPE_DOUBLE_TAP:
                eventName = "DOUBLE_TAP";
                break;
            case UserInputEvent.EVENT_TYPE_TRIPLE_TAP:
                eventName = "TRIPLE_TAP";
                break;

            default:
                break;
        }

        return eventName;
    }

    private String getDeviceFamilyName(int deviceFamily) {
        String deviceFamilyName = "Unknown";

        switch (deviceFamily) {
            case ShineProfile.DEVICE_FAMILY_SHINE:
                deviceFamilyName = "Shine";
                break;
            case ShineProfile.DEVICE_FAMILY_FLASH:
                deviceFamilyName = "Flash";
                break;
            case ShineProfile.DEVICE_FAMILY_BUTTON:
                deviceFamilyName = "Button";
                break;
            case ShineProfile.DEVICE_FAMILY_SHINE_MKII:
                deviceFamilyName = "Shine MKII";
                break;
            case ShineProfile.DEVICE_FAMILY_PLUTO:
                deviceFamilyName = "Pluto";
                break;
            case ShineProfile.DEVICE_FAMILY_SILVRETTA:
                deviceFamilyName = "Silvretta";
                break;
            case ShineProfile.DEVICE_FAMILY_BMW:
                deviceFamilyName = "BMW";
                break;
            default:
                break;
        }
        return deviceFamilyName;
    }

    private String buildStreamingConfigurationString(ShineStreamingConfiguration streamingConfiguration) {
        if (streamingConfiguration == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nNumber of Mapped Event Packets: " + streamingConfiguration.mNumberOfMappedEventPackets);
        stringBuilder.append("\nConnection Heartbeat Interval: " + streamingConfiguration.mConnectionHeartbeatInterval);
        return stringBuilder.toString();
    }

    private void onOperationCompleted(int eventId, String message) {
        Bundle mBundle = new Bundle();
        mBundle.putString(EXTRA_MESSAGE, message);

        Message msg = Message.obtain(mHandler, eventId);
        msg.setData(mBundle);
        msg.sendToTarget();
    }

    private void onOperationCompleted(int eventId, int params, String message) {
        Bundle mBundle = new Bundle();
        mBundle.putString(EXTRA_MESSAGE, message);

        Message msg = Message.obtain(mHandler, eventId);
        msg.arg1 = params;
        msg.setData(mBundle);
        msg.sendToTarget();
    }

    private void onOperationCompleted(String message) {
        onOperationCompleted(SHINE_SERVICE_OPERATION_END, message);
    }

    public static int getConnectingTimeout() {
        return sConnectingTimeout;
    }

    public static synchronized void setConnectingTimeout(int connectingTimeout) {
        MisfitShineService.sConnectingTimeout = connectingTimeout;
    }

    public static synchronized void resetConnectingTimeout() {
        MisfitShineService.sConnectingTimeout = 80000;
    }

    /*
         * Old Interface
         */
    private ShineCallback_v1 mShineCallback_v1 = new ShineCallback_v1() {
        @Override
        public void onConnectionStateChanged(ShineProfile shineProfile, boolean isConnected) {

            if (isConnected) {
                stopConnectionTimeOutTimer();
                long connTimeInMilli = Calendar.getInstance().getTimeInMillis() - mConnTimerStartTime;
                int connTimeInSeconds = (int) (connTimeInMilli / 1000);
                //TODO:after connected
                String firmwareVersion = mShineProfile.getFirmwareVersion();
                String modelNumber = mShineProfile.getModelNumber();
                String deviceFamilyName = getDeviceFamilyName(mShineProfile.getDeviceFamily());

                Bundle mBundle = new Bundle();
                mBundle.putParcelable(MisfitShineService.EXTRA_DEVICE, mShineProfile.getDevice());
                mBundle.putString(MisfitShineService.EXTRA_MESSAGE, deviceFamilyName + " - " + firmwareVersion + " - " + modelNumber);

                mBundle.putInt(MisfitShineService.EXTRA_CONN_TIME, connTimeInSeconds);

                Message msg = Message.obtain(mHandler, SHINE_SERVICE_CONNECTED);
                msg.setData(mBundle);
                msg.sendToTarget();

                if (mExternalConnectTimeoutListener != null) {
                    mExternalConnectTimeoutListener.onShineConnected();
                }
            } else {
                mShineProfile = null;

                Message msg = Message.obtain(mHandler, SHINE_SERVICE_CLOSED);
                msg.sendToTarget();
            }
        }

        @Override
        public void onSyncFailed() {
            onOperationCompleted("onSyncFailed:" + buildSyncResultString(mSummaryResult));
        }

        ;

        @Override
        public void onSyncSucceeded() {
            onOperationCompleted("onSyncSucceeded:" + buildSyncResultString(mSummaryResult));
        }

        @Override
        public void onSyncDataReadProgress(Bundle extraInfo, MutableBoolean shouldStop) {
            onOperationCompleted("onSyncRead: "+ String.format("%.1f", extraInfo.getFloat(ShineProfile.SYNC_PROGRESS_KEY, 0) * 100) + "%");
        }

        @Override
        public void onSyncDataReadCompleted(List<SyncResult> results, MutableBoolean shouldStop) {
            for(SyncResult syncResult : results) {
                mSummaryResult.mActivities.addAll(0, syncResult.mActivities);
                mSummaryResult.mTapEventSummarys.addAll(0, syncResult.mTapEventSummarys);
                mSummaryResult.mSessionEvents.addAll(0, syncResult.mSessionEvents);
                mSummaryResult.mSwimSessions.addAll(0, syncResult.mSwimSessions);
            }
        }

        @Override
        public void onGettingDeviceConfigurationFailed(ConfigurationSession session) {
            onOperationCompleted("onGettingDeviceConfigurationFailed:" + buildShineConfigurationString(session));
        }

        @Override
        public void onGettingDeviceConfigurationSucceeded(ConfigurationSession session) {
            onOperationCompleted("onGettingDeviceConfigurationSucceeded:" + buildShineConfigurationString(session));
        }

        @Override
        public void onSettingDeviceConfigurationFailed() {
            onOperationCompleted("onSettingDeviceConfigurationFailed");
        }

        ;

        @Override
        public void onSettingDeviceConfigurationSucceeded() {
            onOperationCompleted("onSettingDeviceConfigurationSucceeded");
        }

        ;

        @Override
        public void onOTAFailed() {
            onOperationCompleted("OTA FAILED");
        }

        @Override
        public void onOTASucceeded() {
            onOperationCompleted("OTA COMPLETED - SHINE RESET");
        }

        @Override
        public void onOTAProgressChanged(float progress) {
            String message = "OTA PROGRESS: " + String.format("%.1f", progress * 100) + "%";
            onOperationCompleted(SHINE_SERVICE_OTA_PROGRESS_CHANGED, message);
        }

        public void onPlayAnimationSucceeded() {
            onOperationCompleted("PLAY ANIMATION SUCCEEDED");
        }

        ;

        public void onPlayAnimationFailed() {
            onOperationCompleted("PLAY ANIMATION FAILED");
        }

        @Override
        public void onChangingSerialNumberSucceeded() {
            onOperationCompleted("CHANGE SERIAL NUMBER SUCCEEDED");
        }

        @Override
        public void onChangingSerialNumberFailed() {
            onOperationCompleted("CHANGE SERIAL NUMBER FAILED");
        }

        @Override
        public void onSettingConnectionParametersSucceeded(ShineConnectionParameters connectionParameters) {
            String message = "SET CONNECTION PARAMETERS SUCCEEDED" + buildConnectionParametersString(connectionParameters);
            onOperationCompleted(message);
        }

        @Override
        public void onSettingConnectionParametersFailed(ShineConnectionParameters connectionParameters) {
            String message = "SET CONNECTION PARAMETERS FAILED" + buildConnectionParametersString(connectionParameters);
            onOperationCompleted(message);
        }

        @Override
        public void onActivateSucceeded() {
            onOperationCompleted("ACTIVATE SUCCEEDED");
        }

        @Override
        public void onActivateFailed() {
            onOperationCompleted("ACTIVATE FAILED");
        }

        @Override
        public void onGettingActivationStateSucceeded(boolean isActivated) {
            onOperationCompleted("Activated:" + String.valueOf(isActivated));
        }

        @Override
        public void onGettingActivationStateFailed() {
            onOperationCompleted("GET ACTIVATION STATE FAILED");
        }

        public void onStreamingUserInputEventsEnded() {
            onOperationCompleted("STREAMING USER INPUT EVENTS ENDED");
        }

        public void onStreamingUserInputEventsFailed() {
            onOperationCompleted("STREAMING USER INPUT EVENTS FAILED");
        }

        public void onStreamingUserInputEventsReceivedEvent(int eventID) {
            String message = "Received event: " + buildUserInputEventString(eventID);
            onOperationCompleted(SHINE_SERVICE_STREAMING_USER_INPUT_EVENTS_RECEIVED_EVENT,eventID, message);
        }

        @Override
        public void onReadRssiSucceeded(int rssi) {
            Bundle mBundle = new Bundle();
            mBundle.putInt(MisfitShineService.EXTRA_RSSI, rssi);

            Message msg = Message.obtain(mHandler, SHINE_SERVICE_RSSI_READ);
            msg.setData(mBundle);
            msg.sendToTarget();
        }

        public void onReadRssiFailed() {
            Bundle mBundle = new Bundle();
            mBundle.putInt(MisfitShineService.EXTRA_RSSI, -1);

            Message msg = Message.obtain(mHandler, SHINE_SERVICE_RSSI_READ);
            msg.setData(mBundle);
            msg.sendToTarget();
        }

        public void onGettingStreamingConfigurationSucceeded(ShineStreamingConfiguration streamingConfiguration) {
            onOperationCompleted("onGettingStreamingConfigurationSucceeded: " + buildStreamingConfigurationString(streamingConfiguration));
        }

        public void onGettingStreamingConfigurationFailed() {
            onOperationCompleted("onGettingStreamingConfigurationFailed");
        }

        public void onSettingStreamingConfigurationSucceeded() {
            onOperationCompleted("onSettingStreamingConfigurationSucceeded");
        }

        public void onSettingStreamingConfigurationFailed() {
            onOperationCompleted("onSettingStreamingConfigurationFailed");
        }

        @Override
        public void onMapEventAnimationFailed() {
            onOperationCompleted(SHINE_SERVICE_BUTTON_EVENTS, "onMapEventAnimationFailed");
        }

        @Override
        public void onMapEventAnimationSucceeded() {
            onOperationCompleted(SHINE_SERVICE_BUTTON_EVENTS, "onMapEventAnimationSucceeded");
        }

        @Override
        public void onStartButtonAnimationFailed() {
            onOperationCompleted(SHINE_SERVICE_BUTTON_EVENTS, "onStartButtonAnimationFailed");
        }

        @Override
        public void onStartButtonAnimationSucceeded() {
            onOperationCompleted(SHINE_SERVICE_BUTTON_EVENTS, "onStartButtonAnimationSucceeded");
        }

        @Override
        public void onUnmapAllEventAnimationFailed() {
            onOperationCompleted(SHINE_SERVICE_BUTTON_EVENTS, "onUnmapAllEventAnimationFailed");
        }

        @Override
        public void onUnmapAllEventAnimationSucceeded() {
            onOperationCompleted(SHINE_SERVICE_BUTTON_EVENTS, "onUnmapAllEventAnimationSucceeded");
        }

        @Override
        public void onSystemControlEventMappingFailed() {
            onOperationCompleted(SHINE_SERVICE_BUTTON_EVENTS, "onEventMappingSystemControlFailed");
        }

        @Override
        public void onSystemControlEventMappingSucceeded() {
            onOperationCompleted(SHINE_SERVICE_BUTTON_EVENTS, "onEventMappingSystemControlSucceeded");
        }

        public void onGettingConnectionParametersSucceeded(ShineConnectionParameters connectionParameters) {
            String message = "GET CONNECTION PARAMETERS SUCCEEDED" + buildConnectionParametersString(connectionParameters);
            onOperationCompleted(message);
        }

        @Override
        public void onGettingConnectionParametersFailed(ShineConnectionParameters connectionParameters) {
            String message = "GET CONNECTION PARAMETERS FAILED" + buildConnectionParametersString(connectionParameters);
            onOperationCompleted(message);
        }

        @Override
        public void onGettingExtraAdvertisingDataStateRequestFailed() {
            onOperationCompleted("onGettingExtraAdvertisingDataStateRequestFailed");
        }

        @Override
        public void onGettingExtraAdvertisingDataStateRequestSucceeded(boolean enable) {
            onOperationCompleted("onGettingExtraAdvertisingDataStateRequestSucceeded - enable: " + enable);
        }

        @Override
        public void onGettingFlashButtonModeFailed() {
            onOperationCompleted("onGettingFlashButtonModeFailed");
        }

        @Override
        public void onGettingFlashButtonModeSucceeded(FlashButtonMode flashButtonMode) {
            onOperationCompleted("onGettingFlashButtonModeSucceeded - flashButtonMode: " + flashButtonMode.getId());
        }

        @Override
        public void onSettingExtraAdvertisingDataStateRequestFailed() {
            onOperationCompleted("onSettingExtraAdvertisingDataStateRequestFailed");
        }

        @Override
        public void onSettingExtraAdvertisingDataStateRequestSucceeded() {
            onOperationCompleted("onSettingExtraAdvertisingDataStateRequestSucceeded");
        }

        @Override
        public void onSettingFlashButtonModeFailed() {
            onOperationCompleted("onSettingFlashButtonModeFailed");
        }

        @Override
        public void onSettingFlashButtonModeSucceeded() {
            onOperationCompleted("onSettingFlashButtonModeSucceeded");
        }
    };

    /* Flash Link */
    public void unmapAllEvents() {
        mShineProfile.unmapAllEvents(new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("unmapAllEventsSucceeded");
                } else {
                    onOperationCompleted("unmapAllEventsFailed");
                }
            }
        });
    }

    public void unmapEvent(CustomModeEnum.MemEventNumber eventNumber) {
        mShineProfile.unmapEvent(eventNumber, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("unmapEventSucceeded");
                } else {
                    onOperationCompleted("unmapEventFailed");
                }
            }
        });
    }

    public void setCustomMode(CustomModeEnum.ActionType actionType, CustomModeEnum.MemEventNumber eventNumber,
                              CustomModeEnum.AnimNumber animNumber, CustomModeEnum.KeyCode keyCode,
                              boolean releaseEnable) {
        mShineProfile.setCustomMode(actionType, eventNumber, animNumber, keyCode, releaseEnable, new ShineProfile.ConfigurationCallback() {
            @Override
            public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    onOperationCompleted("onSettingCustomModeSucceeded");
                } else {
                    onOperationCompleted("onSettingCustomModeFailed");
                }
            }
        });
    }

    public interface ConnectTimeoutListener {
        void onShineConnectTimeout();

        void onShineConnected();
    }

    void setConnectTimeoutListener(ConnectTimeoutListener listener) {
        mExternalConnectTimeoutListener = listener;
    }

    int getLastConnectionErrorCode() {
        if (mShineProfile == null) {
            return lastConnErrorCode;
        }
        return mShineProfile.getConnectFailCodeEnum();
    }
}
