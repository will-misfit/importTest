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

import com.misfit.ble.sample.utils.AESEncrypt;
import com.misfit.ble.sample.utils.Convertor;
import com.misfit.ble.sample.utils.GsonUtils;
import com.misfit.ble.sample.utils.MLog;
import com.misfit.ble.setting.SDKSetting;
import com.misfit.ble.setting.flashlink.CustomModeEnum;
import com.misfit.ble.setting.flashlink.FlashButtonMode;
import com.misfit.ble.setting.lapCounting.LapCountingMode;
import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.setting.pluto.GoalHitNotificationSettings;
import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.ble.setting.pluto.PlutoSequence;
import com.misfit.ble.setting.speedo.ActivityType;
import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineAdapter;
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

import java.nio.ByteBuffer;
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
                onOperationCompleted("OTA COMPLETED - SHINE RESET");
            } else {
                mAutoOtaCountDown--;
                if (mAutoOtaCountDown < 0 || mTempOtaFile == null) {
                    Log.i(TAG, "auto continue ota, left try:" + mAutoOtaCountDown);
                    onOperationCompleted("OTA FAILED");
                } else {
                    mShineProfile.ota(mTempOtaFile, otaCallback);
                }
            }
        }

        @Override
        public void onOTAProgressChanged(float progress) {
            String message = "OTA PROGRESS: " + String.format("%.1f", progress * 100) + "%";
            onOperationCompleted(SHINE_SERVICE_OTA_PROGRESS_CHANGED, message);
        }
    };

    private ShineProfile.SyncCallback syncCallback = new ShineProfile.SyncCallback() {
        @Override
        public void onSyncDataRead(Bundle extraInfo, MutableBoolean shouldStop) {
            onOperationCompleted("onSyncRead: "+ String.format("%.1f", extraInfo.getFloat(ShineProfile.SYNC_PROGRESS_KEY, 0) * 100) + "%");
        }

        @Override
        public void onSyncDataReadCompleted(List<SyncResult> syncResults, MutableBoolean shouldStop) {
            for(SyncResult syncResult : syncResults) {
                mSummaryResult.mActivities.addAll(0, syncResult.mActivities);
                mSummaryResult.mTapEventSummarys.addAll(0, syncResult.mTapEventSummarys);
                mSummaryResult.mSessionEvents.addAll(0, syncResult.mSessionEvents);
                mSummaryResult.mSwimSessions.addAll(0, syncResult.mSwimSessions);
            }
        }

        @Override
        public void onHardwareLogRead(byte[] hwLog) {
            MLog.i(TAG, "hw log:{" + com.misfit.ble.util.Convertor.bytesToString(hwLog) + "}");
        }

        @Override
        public void onSyncCompleted(ShineProfile.ActionResult resultCode) {
            if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                onOperationCompleted("onSyncSucceeded:" + buildSyncResultString(mSummaryResult));
            } else {
                onOperationCompleted("onSyncFailed:" + buildSyncResultString(mSummaryResult));
            }
        }
    };

    private ShineProfile.StreamingCallback streamingCallback = new ShineProfile.StreamingCallback() {
        @Override
        public void onStreamingButtonEvent(int eventID) {
            String message = "Received event: " + buildUserInputEventString(eventID);
            onOperationCompleted(SHINE_SERVICE_STREAMING_USER_INPUT_EVENTS_RECEIVED_EVENT,eventID, message);
        }

        @Override
        public void onStreamingStarted(ShineProfile.ActionResult resultCode) {
            if (ShineProfile.ActionResult.SUCCEEDED == resultCode) {
                onOperationCompleted("STREAMING USER INPUT EVENTS STARTED");
            } else {
                onOperationCompleted("STREAMING USER INPUT EVENTS FAILED");
            }
        }

        @Override
        public void onStreamingStopped(ShineProfile.ActionResult resultCode) {
            if (ShineProfile.ActionResult.SUCCEEDED == resultCode) {
                onOperationCompleted("STREAMING USER INPUT EVENTS ENDED");
            } else {
                onOperationCompleted("STREAMING USER INPUT EVENTS FAILED");
            }
        }

        @Override
        public void onHeartbeatReceived() {
            onOperationCompleted("onHeartbeatReceived");
        }
    };

    private void buildMessage(ActionID actionID, ShineProfile.ActionResult result, String content) {
        onOperationCompleted(actionID + " " + result + ": " + content);
    }

    private ShineProfile.ConfigurationCallback configurationCallback = new ShineProfile.ConfigurationCallback() {
        @Override
        public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
            switch (actionID) {
                case READ_REMOTE_RSSI:
                    if (ShineProfile.ActionResult.SUCCEEDED == resultCode) {
                        int rssi = (int) data.get(ShineProperty.RSSI);
                        Bundle mBundle = new Bundle();
                        mBundle.putInt(MisfitShineService.EXTRA_RSSI, rssi);

                        Message msg = Message.obtain(mHandler, SHINE_SERVICE_RSSI_READ);
                        msg.setData(mBundle);
                        msg.sendToTarget();
                    } else {
                        Bundle mBundle = new Bundle();
                        mBundle.putInt(MisfitShineService.EXTRA_RSSI, -1);

                        Message msg = Message.obtain(mHandler, SHINE_SERVICE_RSSI_READ);
                        msg.setData(mBundle);
                        msg.sendToTarget();
                    }
                    break;
                default:
                    buildMessage(actionID, resultCode, GsonUtils.getGon().toJson(data));
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
        mShineProfile.stopPlayingAnimation(configurationCallback);
    }

    public void startActivating() {
        mShineProfile.activate(configurationCallback);
    }

    public void startGettingActivationState() {
        mShineProfile.getActivationState(configurationCallback);
    }

    public void startGettingLapCountingStatus() {
        mShineProfile.getLapCountingStatus(configurationCallback);
    }

    public void startSettingLapCountingLicenseInfo(String serialNumber, boolean isReady) {
        final byte[] LICENSE_READY = {0x10, 0x10, 0x10, 0x10, 0x10, 0x10};
        final byte[] LICENSES_NOT_READY = {0x01, 0x01, 0x01, 0x01, 0x01, 0x01};

        ByteBuffer buffer = ByteBuffer.allocate(16);
        if (isReady) {
            buffer.put(LICENSE_READY);
        } else {
            buffer.put(LICENSES_NOT_READY);
        }

        int time = 1483142400;// 2016-12-31-12:00 (UTC)
        Log.d(TAG, "lap time is " + time);

        byte[] timeBytes = Convertor.unsignedInt2BEBytes(time);
        Log.d(TAG, "lap timeBytes is " + Convertor.bytesToString(timeBytes, ":"));

        buffer.put(timeBytes);

        byte[] originData = buffer.array();
        Log.d(TAG, "lap origin data is " + Convertor.bytesToString(originData, ":"));

        final String DEFAULT_PASSCODE_POSTFIX = "MISFIT";
        String passcode = serialNumber + DEFAULT_PASSCODE_POSTFIX;
        Log.d(TAG, "lap passcode is " + passcode);

        byte[] encryptData = AESEncrypt.encrypt(buffer.array(), passcode);
        Log.d(TAG, "lap encrypt data is " + Convertor.bytesToString(encryptData, ":"));

        byte[] decryptData = AESEncrypt.decrypt(encryptData, passcode);
        Log.d(TAG, "lap decrypt data is " + Convertor.bytesToString(decryptData, ":"));

        mShineProfile.setLapCountingLicenseInfo(encryptData, configurationCallback);
    }

    public void startSettingLapCountingMode(String modeStr) {
        String[] params = modeStr.split(",");
        if (params.length == 2) {
            LapCountingMode mode = LapCountingMode.getModeFromByte(Byte.parseByte(params[0]));
            short timeout = Short.parseShort(params[1]);
            mShineProfile.setLapCountingMode(mode, timeout, configurationCallback);
        } else {
            Toast.makeText(this, "Please input the fields:[mode], [timeout]", Toast.LENGTH_SHORT).show();
            configurationCallback.onConfigCompleted(ActionID.SET_LAP_COUNTING_MODE, ShineProfile.ActionResult.FAILED, null);
        }
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

    public void setActivityType(ActivityType activityType) {
        mShineProfile.setActivityType(activityType, configurationCallback);
    }

    public void getActivityType() {
        mShineProfile.getActivityType(configurationCallback);
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
        return mShineProfile.setInactivityNudge(inactivityNudgeSettings, configurationCallback);
    }

    public boolean startGettingInactivityNudge() {
        return mShineProfile.getInactivityNudge(configurationCallback);
    }

    public boolean startSettingAlarm(AlarmSettings alarmSettings) {
        return mShineProfile.setSingleAlarm(alarmSettings, configurationCallback);
    }

    public boolean startGettingAlarm() {
        return mShineProfile.getSingleAlarm(AlarmSettings.ALL_DAYS, configurationCallback);
    }

    public boolean startClearingAllAlarms() {
        return mShineProfile.clearAllAlarms(configurationCallback);
    }

    public boolean startSettingGoalHitNotification(GoalHitNotificationSettings goalHitNotificationSettings) {
        return mShineProfile.setGoalReachNotification(goalHitNotificationSettings, configurationCallback);
    }

    public boolean startGettingGoalHitNotification() {
        return mShineProfile.getGoalReachNotification(configurationCallback);
    }

    public boolean startSettingCallTextNotification(NotificationsSettings notificationsSettings) {
        return mShineProfile.setCallTextNotifications(notificationsSettings, configurationCallback);
    }

    public boolean startGettingCallTextNotification() {
        return mShineProfile.getCallTextNotifications(configurationCallback);
    }

    public boolean startSendingCallNotification() {
        return mShineProfile.sendCallNotification(configurationCallback);
    }

    public boolean startSendingTextNofication() {
        return mShineProfile.sendTextNotification(configurationCallback);
    }

    public boolean startSendingStopNofication() {
        return mShineProfile.sendStopNotification(configurationCallback);
    }

    public boolean startDisablingAllNofications() {
        return mShineProfile.disableAllCallTextNotifications(configurationCallback);
    }

    public boolean startSpecifiedAnimation(PlutoSequence.LED led, byte repeats, short timeBetweenRepeats, PlutoSequence.Color color) {
        return mShineProfile.startSpecifiedAnimation(led, repeats, timeBetweenRepeats, color, configurationCallback);
    }

    public boolean startSpecifiedVibration(PlutoSequence.Vibe vibe, byte repeats, short timeBetweenRepeats) {
        return mShineProfile.startSpecifiedVibration(vibe, repeats, timeBetweenRepeats, configurationCallback);
    }


    public boolean startSpecifiedNotification(PlutoSequence.LED led,
                                              PlutoSequence.Color color,
                                              byte animationRepeats,
                                              short timeBetweenAnimationRepeats,
                                              PlutoSequence.Vibe vibe,
                                              byte vibeRepeats,
                                              short timeBetweenVibeRepeats) {
        return mShineProfile.startSpecifiedNotification(led, color, animationRepeats, timeBetweenAnimationRepeats, vibe, vibeRepeats, timeBetweenVibeRepeats, configurationCallback);
    }


    public boolean playLEDAnimation(PlutoSequence.LED sequence, short mRepeat, int milliSecondsRepeat) {
        return mShineProfile.playLEDAnimation(sequence, mRepeat, milliSecondsRepeat, configurationCallback);
    }

    public boolean playVibration(PlutoSequence.Vibe sequence, short mRepeat, int milliSecondsRepeat) {
        return mShineProfile.playVibration(sequence, mRepeat, milliSecondsRepeat, configurationCallback);
    }

    public boolean playSound(PlutoSequence.Sound sequence, short mRepeat, int milliSecondsRepeat) {
        return mShineProfile.playSound(sequence, mRepeat, milliSecondsRepeat, configurationCallback);
    }

    /**
     * Bolt
     */
    public boolean addGroupId(short groupId) {
        return mShineProfile.addGroupId(groupId, configurationCallback);
    }

    public boolean getGroupId() {
        return mShineProfile.getGroupId(configurationCallback);
    }

    public boolean setPasscode(byte[] passcode) {
        return mShineProfile.setPasscode(passcode, configurationCallback);
    }

    public boolean getPasscode() {
        return mShineProfile.getPasscode(configurationCallback);
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


    /* Flash Link */
    public void unmapAllEvents() {
        mShineProfile.unmapAllEvents(configurationCallback);
    }

    public void unmapEvent(CustomModeEnum.MemEventNumber eventNumber) {
        mShineProfile.unmapEvent(eventNumber, configurationCallback);
    }

    public void setCustomMode(CustomModeEnum.ActionType actionType, CustomModeEnum.MemEventNumber eventNumber,
                              CustomModeEnum.AnimNumber animNumber, CustomModeEnum.KeyCode keyCode,
                              boolean releaseEnable) {
        mShineProfile.setCustomMode(actionType, eventNumber, animNumber, keyCode, releaseEnable, configurationCallback);
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
