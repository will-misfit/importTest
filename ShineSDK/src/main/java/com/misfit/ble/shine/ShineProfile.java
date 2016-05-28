package com.misfit.ble.shine;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.misfit.ble.BuildConfig;
import com.misfit.ble.sdk.GlobalVars;
import com.misfit.ble.setting.flashlink.CustomModeEnum;
import com.misfit.ble.setting.flashlink.FlashButtonMode;
import com.misfit.ble.setting.lapCounting.LapCountingMode;
import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.setting.pluto.GoalHitNotificationSettings;
import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.ble.setting.pluto.PlutoSequence;
import com.misfit.ble.setting.speedo.ActivityType;
import com.misfit.ble.shine.ShineProfileCore.ShineCoreCallback;
import com.misfit.ble.shine.compatibility.FirmwareCompatibility;
import com.misfit.ble.shine.controller.BoltControllers;
import com.misfit.ble.shine.controller.ChangeSerialNumberPhaseController;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.ble.shine.controller.FlashLinkControllers;
import com.misfit.ble.shine.controller.GetStreamingConfigurationPhaseController;
import com.misfit.ble.shine.controller.OTAPhaseController;
import com.misfit.ble.shine.controller.OTAPhaseController.OTAPhaseControllerCallback;
import com.misfit.ble.shine.controller.PhaseController;
import com.misfit.ble.shine.controller.PhaseController.PhaseControllerCallback;
import com.misfit.ble.shine.controller.PlutoControllers;
import com.misfit.ble.shine.controller.SetConnectionParametersPhaseController;
import com.misfit.ble.shine.controller.SetStreamingConfigurationPhaseController;
import com.misfit.ble.shine.controller.ShineControllers;
import com.misfit.ble.shine.controller.StreamingUserInputEventsPhaseController;
import com.misfit.ble.shine.controller.SyncPhaseController;
import com.misfit.ble.shine.controller.SyncPhaseController.SyncPhaseControllerCallback;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.log.ConnectFailCode;
import com.misfit.ble.shine.log.ConnectFailCode.ConnectPhase;
import com.misfit.ble.shine.log.ConnectFailCode.HandshakePhase;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.log.LogEventItem.RequestFinishedLog;
import com.misfit.ble.shine.log.LogEventItem.RequestStartedLog;
import com.misfit.ble.shine.log.LogEventItem.ResponseFinishedLog;
import com.misfit.ble.shine.log.LogEventItem.ResponseStartedLog;
import com.misfit.ble.shine.log.LogManager;
import com.misfit.ble.shine.log.LogSession;
import com.misfit.ble.shine.log.LogUnexpectedEventItem;
import com.misfit.ble.shine.request.EventActionUnmappingAllRequest;
import com.misfit.ble.shine.request.EventAnimationMappingRequest;
import com.misfit.ble.shine.request.EventMappingSystemControlRequest;
import com.misfit.ble.shine.request.FileListRequest;
import com.misfit.ble.shine.request.PlayButtonAnimationRequest;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.ble.util.Convertor;
import com.misfit.ble.util.MutableBoolean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public final class ShineProfile {
    private static final String TAG = "ShineProfile";

    public enum ActionResult {
        SUCCEEDED, FAILED, TIMED_OUT, INTERNAL_ERROR, INTERRUPTED, UNSUPPORTED
    }

    public enum State {
        IDLE(0), CONNECTING(1), CONNECTED(2), OTA(3), DISCONNECTING(4), CLOSED(5), DISCONNECTED(6);

        int mValue;
        State(int value) {
            mValue = value;
        }

        int getValue() {
            return mValue;
        }
    }

	public interface ConnectionCallback {
		void onConnectionStateChanged(ShineProfile shineProfile, State newState);
	}

    public interface ConnectionCallbackForTest extends ConnectionCallback{
        void onConnectionStateChangedForTest(ShineProfile shineProfile, int status, int newState, int failCode);
    }

	public interface SyncCallback {
        /**
         * Called when a syncResult was read, and notify the progress. if number of syncResult is 0, it will not be called.
         * @param extraInfo include current progress, data key={@link #SYNC_PROGRESS_KEY}
         * @param shouldStop the way to control the sync procedure.
         */
		void onSyncDataRead(Bundle extraInfo, MutableBoolean shouldStop);

        /**
         * Called when all syncResults were read, including 0 syncResult.
         * @param syncResults syncResults which has been corrected timestamp
         * @param shouldStop the way to control the sync procedure.
         */
		void onSyncDataReadCompleted(List<SyncResult> syncResults, MutableBoolean shouldStop);

        /**
         * Called when the hardware log was read.
         * @param hwLog
         */
        void onHardwareLogRead(byte[] hwLog);
        /**
         * Called when sync procedure was finished, whether success or failure.
         * @param resultCode result of procedure
         */
		void onSyncCompleted(ActionResult resultCode);
	}

	public interface OTACallback {
		void onOTACompleted(ActionResult resultCode);
		void onOTAProgressChanged(float progress);
	}

	public interface StreamingCallback {
        void onStreamingStarted(ActionResult resultCode);
		void onStreamingButtonEvent(int eventID);
		void onStreamingStopped(ActionResult resultCode);
		void onHeartbeatReceived();
	}

    public interface ConfigurationCallback {
        void onConfigCompleted(ActionID actionID, ActionResult resultCode, Hashtable<ShineProperty, Object> data);
	}

    public static final int DEVICE_FAMILY_UNKNOWN = 0;
    public static final int DEVICE_FAMILY_SHINE = 1;
    public static final int DEVICE_FAMILY_FLASH = 2;
    public static final int DEVICE_FAMILY_BUTTON = 3;
    public static final int DEVICE_FAMILY_SHINE_MKII = 4;
    public static final int DEVICE_FAMILY_PLUTO = 5;
    public static final int DEVICE_FAMILY_SILVRETTA = 6;
    public static final int DEVICE_FAMILY_BMW = 7;

    private static final int CONNECT_RETRIES = 5; // internal connect retry count if connect() invoked from app
    private int mCurrentConnectIndex = 0;

    public static final String SYNC_RAW_DATA_KEY = "com.misfit.ble.Shine.ShineProfile.syncRawDataKey";
    public static final String SYNC_PROGRESS_KEY = "com.misfit.ble.Shine.ShineProfile.syncProgressDataKey";
    public static final String SYNC_ORIGINAL_EPOCH_KEY = "com.misfit.ble.Shine.ShineProfile.syncOriginalEpochKey";
    public static final String SYNC_CORRECTED_EPOCH_KEY = "com.misfit.ble.Shine.ShineProfile.syncCorrectedEpochKey";

    // iVars
    private ShineProfileCore mShineProfileCore;
	private ConnectionCallback mConnectionCallback;
	private ConfigurationCallback mConfigurationCallback;
    private ConfigurationCallback mReadRSSICallback;

    private volatile State mState;
	private volatile ActionID mActionID = null;
	private volatile ActionID mButtonRequestActionID = null;
	private volatile boolean mIsInStreaming = false;

    private String mFirmwareVersion;
    private String mModelNumber;

    private LogSession mLogSession;
    private LogEventItem mCurrentLogItem;
    private LogEventItem mCurrentPhaseLogItem;
    private LogEventItem mCurrentRequestLogItem;
    private LogEventItem mCurrentButtonRequestLogItem;
    private LogEventItem mCurrentDataTransferringLogItem;
    private int mTransferredSize;
    private int mTransferringResult;

    private int mNumberOfConnectionRetriesLeft;

	private ShineControllers mShineControllers;
	private PlutoControllers mPlutoControllers;
	private FlashLinkControllers mFlashLinkControllers;
	private BoltControllers mBoltControllers;

    private Handler mainHandler;

    // to record internal error code of connect operation
    private ConnectFailCode mConnectFailCode;

    private boolean mInConnectAttempt = false;

    private Timer mTimer = new Timer("ShineProfileTimer");
    private Timer mConnectTimer = new Timer("Connect4Sync");

    /**
	 * Set Configuration Callback
	 *
	 * @param configurationCallback refer to {@link com.misfit.ble.shine.ShineProfile.ConfigurationCallback}
	 */
	private void setConfigurationCallback(ConfigurationCallback configurationCallback) {
		this.mConfigurationCallback = configurationCallback;
	}

	// Flow Control
    private PhaseController mCurrentPhaseController;

    /*package*/ ShineProfile(Context context, ShineDevice shineDevice) {
        GlobalVars.setUpApplicationContext(context);
        LogManager.getDefault().uploadLogSession();

        mLogSession = new LogSession(shineDevice.getSerialNumber());
        mShineProfileCore = new ShineProfileCore(context, shineDevice);
        mShineProfileCore.setLogSession(mLogSession);
        setState(State.IDLE);

        mainHandler = new Handler(Looper.getMainLooper());

        mPlutoControllers = new PlutoControllers(mPhaseControllerCallback);
        mShineControllers = new ShineControllers(mPhaseControllerCallback);
        mFlashLinkControllers = new FlashLinkControllers(mPhaseControllerCallback);
        mBoltControllers = new BoltControllers(mPhaseControllerCallback);

        monitorBluetoothState();

        mConnectFailCode = new ConnectFailCode();
    }

    /**
     * Public Interface
     */
    /*package*/ boolean connect(boolean autoConnect,
								ConnectionCallback connectionCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (mState != State.IDLE && mState != State.CLOSED) {
                logUnexpectedEvent(LogEventItem.EVENT_CONNECT);
                Log.d(TAG, "connect() return false: state is not IDLE");
                return false;
            }

            mTimer.schedule(new TimerTask() { // be certain to stop internal reconnect in a while
                @Override
                public void run() {
                    synchronized(mShineProfileCore.lockObject){
                        mInConnectAttempt = false;
                    }
                }
            }, Constants.EXTERNAL_CONNECT_TIMEOUT);

            mInConnectAttempt = true;
            mLogSession.start();

            mCurrentLogItem = newLogEventItem(LogEventItem.EVENT_CONNECT);
            mCurrentLogItem.mRequestStartedLog = new RequestStartedLog(null);

            mConnectFailCode.reset();

            boolean result = mShineProfileCore.connect(autoConnect, mCoreCallback);
            mCurrentConnectIndex = 0;

            mCurrentLogItem.mRequestFinishedLog = new RequestFinishedLog(result ? ShineProfileCore.RESULT_SUCCESS : ShineProfileCore.RESULT_FAILURE);

            if (result) {
                setState(State.CONNECTING);
                mNumberOfConnectionRetriesLeft = CONNECT_RETRIES;
				mConnectionCallback = connectionCallback;
                mConnectTimer.schedule( new IndexedTimerTask(mCurrentConnectIndex) {
                        @Override
                        public void run() {
                            if (getIndex() == mCurrentConnectIndex
                                && mState == State.CONNECTING
                                && mInConnectAttempt) {
                                disconnect();
                            }
                        }
                }, Constants.CONNECT_CALLBACK_TIMEOUT);
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "connect(): mShineProfileCore.connect() returns false");
                }
            }
            return result;
        }
    }

    /**
    * establish Gatt connection internally, with updated BluetoothGatt instance inside
    * */
    private boolean internalConnect() {
        Log.d(TAG, "internalConnect() entered");

        if (mNumberOfConnectionRetriesLeft == 0 || !mInConnectAttempt) return false;

        Log.d(TAG, "internalConnect(), mNumberOfConnectionRetriesLeft = " + mNumberOfConnectionRetriesLeft);

        synchronized (mShineProfileCore.lockObject) {
            if (mState != State.CLOSED) {
                logUnexpectedEvent(LogEventItem.EVENT_RECONNECT_GATT);
                Log.d(TAG, "internalConnect() returns false: state is not CLOSED");
                return false;
            }

            mShineProfileCore.updateIBluetoothGatt();

            mCurrentLogItem = newLogEventItem(LogEventItem.EVENT_RECONNECT_GATT);
            mCurrentLogItem.mRequestStartedLog = new RequestStartedLog(null);

            mConnectFailCode.reset();

            boolean result = mShineProfileCore.connect(false, mCoreCallback);
            mCurrentConnectIndex++;

            mCurrentLogItem.mRequestFinishedLog
                = new RequestFinishedLog(result ? ShineProfileCore.RESULT_SUCCESS : ShineProfileCore.RESULT_FAILURE);
            if(result) {
                setState(State.CONNECTING);
                mNumberOfConnectionRetriesLeft--;
                mConnectTimer.schedule(new IndexedTimerTask(mCurrentConnectIndex) {
                    @Override
                    public void run() {
                        if (getIndex() == mCurrentConnectIndex
                                && mState == State.CONNECTING
                                && mInConnectAttempt) {
                            disconnect();
                        }
                    }
                }, Constants.CONNECT_CALLBACK_TIMEOUT);
            }
            return result;
        }
    }

    private boolean connectImpl() {
        mCurrentLogItem = newLogEventItem(LogEventItem.EVENT_CONNECT);
        mCurrentLogItem.mRequestStartedLog = new RequestStartedLog(null);

        boolean result = mShineProfileCore.connect();
        mCurrentLogItem.mRequestFinishedLog = new RequestFinishedLog(result ? ShineProfileCore.RESULT_SUCCESS : ShineProfileCore.RESULT_FAILURE);
        return result;
    }

	/**
	 * Get the configuration. The current configuration will be returned via the callback
	 * @return operation was started successfully
	 */
    public boolean getDeviceConfiguration(ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_DEVICE_CONFIGURATION);
                return false;
            }

            return startPhaseController(mShineControllers.getDeviceConfiguration(mFirmwareVersion, mModelNumber, configurationCallback));
        }
    }

	/**
	 * Update the configuration which includes clock state, daily goal, progress and other configurable properties
	 *
	 * @param configurationSession refer to {@link ConfigurationSession}
	 * @return operation was started successfully
	 */
	public boolean setDeviceConfiguration(ConfigurationSession configurationSession, ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_SET_DEVICE_CONFIGURATION);
                return false;
            }

            return startPhaseController(mShineControllers.setDeviceConfiguration(mFirmwareVersion, mModelNumber, configurationSession, configurationCallback));
        }
    }

	/**
	 * Start a new sync session with Shine/Flash
	 *
	 * @param syncCallback refer to {@link com.misfit.ble.shine.ShineProfile.SyncCallback}
	 * @return operation was started successfully
	 */
	public boolean sync(SyncCallback syncCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_SYNC);
                Log.d(TAG, "sync() return false: is not ready");
                return false;
            }

            if (!mLogSession.isLogOngoing()) {
                mLogSession.start();
            }
			mCurrentPhaseController = new SyncPhaseController(mPhaseControllerCallback, syncCallback, mSyncPhaseControllerCallback);
			return startPhaseController(mCurrentPhaseController);
        }
    }

	/**
	 * Flash new firmware to Shine/Flash.
	 *
	 * @param firmwareData firmwareData binary data of the firmware
	 * @param otaCallback  refer to {@link com.misfit.ble.shine.ShineProfile.OTACallback}
	 * @return indicates the success or failure of this operation
	 */
	public boolean ota(byte[] firmwareData, OTACallback otaCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_OTA);
                Log.d(TAG, "ota() return false: is not ready");
                return false;
            }

			mCurrentPhaseController = new OTAPhaseController(mPhaseControllerCallback, otaCallback, mOTAPhaseControllerCallback, firmwareData);
            return startPhaseController(mCurrentPhaseController);
        }
    }

	/**
	 * Change Serial Number
	 *
	 * @param serialNumber refer to {@link String}
	 * @return operation was started successfully
	 */
	public boolean changeSerialNumber(String serialNumber, ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_CHANGE_SERIAL_NUMBER);
                Log.d(TAG, "changeSerialNumber() return false: is not ready");
                return false;
            }

            String currentSerialNumber = mShineProfileCore.getDevice().getSerialNumber();

			return startPhaseController(new ChangeSerialNumberPhaseController(mPhaseControllerCallback, configurationCallback, serialNumber, currentSerialNumber));
        }
    }

	/**
	 * Set Connection Paramters
	 *
	 * @param connectionParameters refer to {@link ShineConnectionParameters}
	 * @return operation was started successfully
	 */
	public boolean setConnectionParameters(ShineConnectionParameters connectionParameters, ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_SET_CONNECTION_PARAMETERS);
                Log.d(TAG, "setConnectionParameters return false: is not ready");
                return false;
            }

			return startPhaseController(new SetConnectionParametersPhaseController(mPhaseControllerCallback, configurationCallback, connectionParameters));
        }
    }

	/**
	 * Get Connection Parameters
	 *
	 * @return operation was started successfully
	 */
	public boolean getConnectionParameters(ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_CONNECTION_PARAMETERS);
                Log.d(TAG, "getConnectionParameters() return false: is not ready");
                return false;
            }

            if (!mLogSession.isLogOngoing()) {
                mLogSession.start();
            }
            return startPhaseController(mShineControllers.getConnectionParameters(configurationCallback));
        }
    }

	/**
	 * Set Flash Button Mode
	 *
	 * @param flashButtonMode refer to {@link FlashButtonMode}
	 * @return operation was started successfully
	 */
	public boolean setFlashButtonMode(FlashButtonMode flashButtonMode, ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_SET_FLASH_BUTTON_MODE);
                Log.d(TAG, "setFlashButtonMode() return false: is not ready");
                return false;
            }

            return startPhaseController(mShineControllers.setFlashButtonMode(flashButtonMode, configurationCallback));
        }
    }

	/**
	 * Get Flash Button Mode
	 *
	 * @return operation was started successfully
	 */
	public boolean getFlashButtonMode(ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_FLASH_BUTTON_MODE);
                return false;
            }

            return startPhaseController(mShineControllers.getFlashButtonMode(configurationCallback));
        }
    }

	/**
	 * Play the LED animation on Shine/Flash.
	 *
	 * @return operation was started successfully
	 */
	public boolean playAnimation(ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_PLAY_ANIMATION);
                Log.d(TAG, "playAnimation() return false: is not ready");
                return false;
            }
            if (!mLogSession.isLogOngoing()) {
                mLogSession.start();
            }
            return startPhaseController(mShineControllers.playAnimation(configurationCallback));
        }
    }

	/**
	 * Stop playing the LED animation on Shine/Flash.
	 *
	 * @return operation was started successfully
	 */
	public boolean stopPlayingAnimation(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_STOP_PLAYING_ANIMATION);
                Log.d(TAG, "stopPlayAnimation() return false: is not ready");
				return false;
			}

			return startPhaseController(mShineControllers.stopPlayingAnimation(configurationCallback));
		}
	}

	/**
	 * Activate Flash, permanently put it in ACTIVATED mode.
	 *
	 * @return operation was started successfully
	 */
	public boolean activate(ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_ACTIVATE);
                return false;
            }

            return startPhaseController(mShineControllers.activate(configurationCallback));
        }
    }

	/**
	 * Get Flash's activation state. The current state will be returned via the callback.
	 *
	 * @return operation was started successfully
	 */
	public boolean getActivationState(ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_ACTIVATION_STATE);
                return false;
            }

            return startPhaseController(mShineControllers.getActivationState(configurationCallback));
        }
    }

    /**
     * Get Device Lap Counting status.
     *
     * @return operation was started successfully
     */
    public boolean getLapCountingStatus(ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_LAP_COUNTING_STATUS);
                return false;
            }

            return startPhaseController(mShineControllers.getLapCountingStatus(configurationCallback));
        }
    }

    /**
     * Set Lap Counting license info
     *
     * @param licenseInfo the byte array license info to be set.
     * @param configurationCallback refer to {@link com.misfit.ble.shine.ShineProfile.ConfigurationCallback}
     * @return operation was started successfully
     */
    public boolean setLapCountingLicenseInfo(byte[] licenseInfo, ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_SET_LAP_COUNTING_LICENSE_INFO);
                return false;
            }

            return startPhaseController(mShineControllers.setLapCountingLicenseInfo(licenseInfo, configurationCallback));
        }
    }

    /**
     * Set Lap Counting mode
     *
     * @param mode the mode to be set, refer to {@link com.misfit.ble.setting.lapCounting.LapCountingMode}
     * @param timeout the value of total time, used by Lap Counting Timeout Mode , not be cared and is 0 in Lap Counting Manual Mode.
     * @param configurationCallback refer to {@link com.misfit.ble.shine.ShineProfile.ConfigurationCallback}
     * @return operation was started successfully
     */
    public boolean setLapCountingMode(LapCountingMode mode, short timeout, ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_SET_LAP_COUNTING_MODE);
                return false;
            }

            return startPhaseController(mShineControllers.setLapCountingMode(mode, timeout, configurationCallback));
        }
    }

	/**
	 * Streaming User Input Event
	 * @param streamingCallback refer to {@link com.misfit.ble.shine.ShineProfile.StreamingCallback}
	 * @return operation was started successfully
	 */
    public boolean streamUserInputEvents(StreamingCallback streamingCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_STREAM_USER_INPUT_EVENTS);
                Log.d(TAG, "activate() return false: is not ready");
                return false;
            }

            submitLogSession();

			short fileHandle = FirmwareCompatibility.streamingFileHandle(getDeviceFamily(), mFirmwareVersion);
            String deviceAddress = getDevice().getAddress();
            boolean result = startPhaseController(new StreamingUserInputEventsPhaseController(mPhaseControllerCallback, streamingCallback, deviceAddress, fileHandle));
            if(result){
                mIsInStreaming = true;
            }
            return result;
        }
    }

	/**
	 * Get Streaming Configuration
	 *
	 * @return operation was started successfully
	 */
    public boolean getStreamingConfiguration(ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_STREAMING_CONFIGURATION);
                return false;
            }

            return startPhaseController(new GetStreamingConfigurationPhaseController(mPhaseControllerCallback, configurationCallback));
        }
    }

	/**
	 * Set Streaming Configuration
	 *
	 * @param streamingConfiguration refer to {@link ShineStreamingConfiguration}
	 * @param configurationCallback
	 * @return operation was started successfully
	 */
	public boolean setStreamingConfiguration(ShineStreamingConfiguration streamingConfiguration, ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_SET_STREAMING_CONFIGURATION);
                return false;
            }

			return startPhaseController(new SetStreamingConfigurationPhaseController(mPhaseControllerCallback, configurationCallback, streamingConfiguration));
        }
    }

    private boolean sendButtonRequest(Request request) {
        mCurrentButtonRequestLogItem = newLogEventItem(request.getRequestName());
        mCurrentButtonRequestLogItem.mRequestStartedLog = new RequestStartedLog(request.getRequestDescriptionJSON());

        // TODO Check
        if (FirmwareCompatibility.isSupportedRequest(mFirmwareVersion, mModelNumber, request) == false) {
            mCurrentButtonRequestLogItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_UNSUPPORTED);
            return false;
        }

        if (mShineProfileCore.sendButtonRequest(request) == false) {
            mCurrentButtonRequestLogItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_FAILURE, request.getRequestDataJSON());
        }

        return true;
    }

    private boolean isButtonRequest(Request request) {
        return request instanceof PlayButtonAnimationRequest
                || request instanceof EventAnimationMappingRequest
                || request instanceof EventActionUnmappingAllRequest
                || request instanceof EventMappingSystemControlRequest;
    }

    private void sendButtonRequestCallback(Request request, int result, ConfigurationCallback configurationCallback) {
        mCurrentButtonRequestLogItem.mRequestFinishedLog = new RequestFinishedLog(result, request.getRequestDataJSON());

        boolean succeeded = (ShineProfileCore.RESULT_SUCCESS == result);

        switch (mButtonRequestActionID) {
        case START_BUTTON_ANIMATION:
            mButtonRequestActionID = null;

            if (succeeded) {
                configurationCallback.onConfigCompleted(ActionID.START_BUTTON_ANIMATION, ActionResult.SUCCEEDED, null);
            } else {
                configurationCallback.onConfigCompleted(ActionID.START_BUTTON_ANIMATION, ActionResult.FAILED, null);
            }
            break;
        case MAP_EVENT_ANIMATION:
            mButtonRequestActionID = null;

            if (succeeded) {
                configurationCallback.onConfigCompleted(ActionID.MAP_EVENT_ANIMATION, ActionResult.SUCCEEDED, null);
            } else {
                configurationCallback.onConfigCompleted(ActionID.MAP_EVENT_ANIMATION, ActionResult.FAILED, null);
            }
            break;
        case UNMAP_ALL_EVENT_ANIMATION:
            mButtonRequestActionID = null;

            if (succeeded) {
                configurationCallback.onConfigCompleted(ActionID.UNMAP_ALL_EVENT_ANIMATION, ActionResult.SUCCEEDED, null);
            } else {
                configurationCallback.onConfigCompleted(ActionID.UNMAP_ALL_EVENT_ANIMATION, ActionResult.FAILED, null);
            }
            break;
        case EVENT_MAPPING_SYSTEM_CONTROL:
            mButtonRequestActionID = null;

            if (succeeded) {
                configurationCallback.onConfigCompleted(ActionID.EVENT_MAPPING_SYSTEM_CONTROL, ActionResult.SUCCEEDED, null);
            } else {
                configurationCallback.onConfigCompleted(ActionID.EVENT_MAPPING_SYSTEM_CONTROL, ActionResult.FAILED, null);
            }
            break;
        }
    }

	/**
	 * Start button Animation
	 *
	 * @param buttonAnimation
	 * @param numOfRepeats
	 * @return operation was started successfully
	 */
	public boolean startButtonAnimation(short buttonAnimation, short numOfRepeats, ConfigurationCallback configCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady() && !isStreaming()) {
                logUnexpectedEvent(LogEventItem.EVENT_BUTTON_ANIMATION);
                return false;
            }

            setConfigurationCallback(configCallback);
            mButtonRequestActionID = ActionID.START_BUTTON_ANIMATION;

            PlayButtonAnimationRequest request = new PlayButtonAnimationRequest();
            request.buildRequest(buttonAnimation, numOfRepeats);
            return sendButtonRequest(request);
        }
    }

	/**
	 * Map Event Animations
	 *
	 * @param eventAnimationMappings
	 * @return operation was started successfully
	 */
	public boolean mapEventAnimations(ShineEventAnimationMapping[] eventAnimationMappings, ConfigurationCallback configCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady() && !isStreaming()) {
                logUnexpectedEvent(LogEventItem.EVENT_MAP_EVENT_ANIMATION);
                return false;
            }

            if (eventAnimationMappings.length > EventAnimationMappingRequest.MAX_EVENTS)
                return false;

            setConfigurationCallback(configCallback);
            mButtonRequestActionID = ActionID.MAP_EVENT_ANIMATION;

            EventAnimationMappingRequest request = new EventAnimationMappingRequest();
            request.buildRequest(eventAnimationMappings);
            return sendButtonRequest(request);
        }
    }

	/**
	 * Unmap Event Animations
	 *
	 * @return operation was started successfully
	 */
	public boolean unmapAllEventAnimation(ConfigurationCallback configCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady() && !isStreaming()) {
                logUnexpectedEvent(LogEventItem.EVENT_UNMAP_ALL_EVENT_ANIMATION);
                return false;
            }

            setConfigurationCallback(configCallback);
            mButtonRequestActionID = ActionID.UNMAP_ALL_EVENT_ANIMATION;

            EventActionUnmappingAllRequest request = new EventActionUnmappingAllRequest();
            request.buildRequest();
            return sendButtonRequest(request);
        }
    }

	/**
	 * System control event mapping
	 * @param controlBits
	 * @return operation was started successfully
	 */
    public boolean systemControlEventMapping(short controlBits, ConfigurationCallback configCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady() && !isStreaming()) {
                logUnexpectedEvent(LogEventItem.EVENT_EVENT_MAPPING_SYSTEM_CONTROL);
                return false;
            }

            setConfigurationCallback(configCallback);
            mButtonRequestActionID = ActionID.EVENT_MAPPING_SYSTEM_CONTROL;

            byte unsignedControlBits = Convertor.unsignedByteFromShort(controlBits);
            unsignedControlBits = (byte) (unsignedControlBits | ShineEventMappingSystemControl.MASK_DEFAULT);

            EventMappingSystemControlRequest request = new EventMappingSystemControlRequest();
            request.buildRequest(unsignedControlBits);
			return sendButtonRequest(request);
        }
    }

	/**
	 * Set extra advertising data state
	 *
	 * @param enabled
	 * @return operation was started successfully
	 */
	public boolean setExtraAdvertisingDataState(boolean enabled, ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_SET_EXTRA_ADV_DATA_STATE);
                return false;
            }

            return startPhaseController(mShineControllers.setExtraAdvertisingDataState(enabled, configurationCallback));
        }
    }

	/**
	 * Get extra advertising data state
	 *
	 * @return operation was started successfully
	 */
	public boolean getExtraAdvertisingDataState(ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_EXTRA_ADV_DATA_STATE);
                return false;
            }

            return startPhaseController(mShineControllers.getExtraAdvertisingDataState(configurationCallback));
        }
    }

	/**
	 * Get Firmware version
	 * @return firmware version
	 */
    public String getFirmwareVersion() {
        synchronized (mShineProfileCore.lockObject) {
            if (State.CONNECTED != mState) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_FIRMWARE_VERSION);
                return null;
            }
            return mFirmwareVersion;
        }
    }

	/**
	 * Get model number
	 * @return model number
	 */
    public String getModelNumber() {
        synchronized (mShineProfileCore.lockObject) {
            if (State.CONNECTED != mState) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_MODEL_NUMBER);
                return null;
            }
            return mModelNumber;
        }
    }

	/**
	 * Get Device Family
	 * @return device family
	 */
	public int getDeviceFamily() {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_GET_DEVICE_FAMILY);
				return DEVICE_FAMILY_UNKNOWN;
			}

			if (!isConnected()) {
				logUnexpectedEvent(LogEventItem.EVENT_GET_DEVICE_FAMILY);
				return DEVICE_FAMILY_UNKNOWN;
			}

			int deviceFamily = DEVICE_FAMILY_UNKNOWN;

			if (mModelNumber.startsWith("SH") ||mModelNumber.startsWith("SC")|| mModelNumber.startsWith("misfit model num")) {
				deviceFamily = DEVICE_FAMILY_SHINE;
			} else if (mModelNumber.startsWith("SV")) {
				deviceFamily = DEVICE_FAMILY_SHINE_MKII;
			} else if (mModelNumber.startsWith("S2")) {
				deviceFamily = DEVICE_FAMILY_PLUTO;
			} else if (mModelNumber.startsWith("C1")) {
                deviceFamily = DEVICE_FAMILY_SILVRETTA;
            }else if (mModelNumber.startsWith("B0")) {
                deviceFamily = DEVICE_FAMILY_BMW;
            }else if (mModelNumber.startsWith("F")) {
				//TODO: system Bluetooth failure leads to deviceName not available
				String deviceName = null;
				if (getDevice() != null) {
					deviceName = getDevice().getName();
				}

				if (deviceName != null) {
					if (deviceName.equalsIgnoreCase("Shine")
							|| getDevice().getName().equalsIgnoreCase("Zhine 2")) {
						deviceFamily = DEVICE_FAMILY_FLASH;
					} else {
						deviceFamily = DEVICE_FAMILY_BUTTON;
					}
				}
			}
			return deviceFamily;
		}
	}

	/**
	 * Read RSSI
	 * @param configCallback
	 * @return operation was started successfully
	 */
    public boolean readRssi(ConfigurationCallback configCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isConnected()) {
                logUnexpectedEvent(LogEventItem.EVENT_READ_RSSI);
                return false;
            }

            mReadRSSICallback = configCallback;
            return mShineProfileCore.readRemoteRssi();
        }
    }

    private void disconnect() {
        synchronized (mShineProfileCore.lockObject) {
            if (mState == State.IDLE || mState == State.CLOSED) {
                logUnexpectedEvent(LogEventItem.EVENT_DISCONNECT);
                return;
            }
            mCurrentLogItem = newLogEventItem(LogEventItem.EVENT_DISCONNECT);
            mCurrentLogItem.mRequestStartedLog = new RequestStartedLog(null);

            if (mState == State.CONNECTED) {
                stopOnGoingOperation();
            }
            setState(State.DISCONNECTING);

            mShineProfileCore.disconnect();
            mCurrentLogItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_SUCCESS);

            mConnectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mState == State.CLOSED || mState == State.DISCONNECTED)
                        return; // close() already or close() is working on

                    setState(State.DISCONNECTED);
                    if (mInConnectAttempt) {
                        internalClose();
                    } else {
                        closeImpl();
                    }
                }
            }, Constants.DISCONNECT_CALLBACK_TIMEOUT);
        }
    }

	/**
	 * Close attempt from external
	 */
    public void close() {
        notifySessionStop();
        synchronized (mShineProfileCore.lockObject) {
            mInConnectAttempt = false; // exit internal retry connect operation
        }

        if (mState == State.DISCONNECTING) { // disconnect() working on
            return;
        } else if(mState == State.CLOSED) { // close() already executed
            return;
        } else if (mState == State.IDLE) { // no connect invoked since initialization, unexpected
            logUnexpectedEvent(LogEventItem.EVENT_CLOSE);
            return;
        } else { // need to disconnect ahead of close
            disconnect();
            return;
        }
    }

    /**
    * Close operation for external
    * */
    private void closeImpl() {
        if (mState == State.IDLE || mState == State.CLOSED) return; // no need to run close action any more

        LogEventItem logEventItem = newLogEventItem(LogEventItem.EVENT_CLOSE);
        logEventItem.mRequestStartedLog = new RequestStartedLog(null);

        stopOnGoingOperation();
        mShineProfileCore.close();
        setState(State.CLOSED);

        logEventItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_SUCCESS);

        if (mLogSession.isLogOngoing()) {
            mLogSession.stop();
        }
        submitLogSession();

        stopMonitorBluetoothState();
        mConnectionCallback.onConnectionStateChanged(this, State.CLOSED);
    }

    /**
     * close connection internally, be part of reconnect management
     * another connect will be called internally
     * */
    private void internalClose() {
        Log.d(TAG, "internalClose() entered");
        if (!mInConnectAttempt)  return;

        synchronized (mShineProfileCore.lockObject) {
            if (mState == State.IDLE || mState == State.CLOSED) return;

            LogEventItem logEventItem = newLogEventItem(LogEventItem.EVENT_CLOSE_GATT);
            logEventItem.mRequestStartedLog = new RequestStartedLog(null);

            stopOnGoingOperation();
            mShineProfileCore.close();
            setState(State.CLOSED);

            logEventItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_SUCCESS);

            if (mNumberOfConnectionRetriesLeft > 0) {
                mConnectTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        internalConnect();
                    }
                }, Constants.INTERVAL_BEFORE_CONNECT);
            } else {
                if (mLogSession.isLogOngoing()) {
                    mLogSession.stop();
                }
                submitLogSession();
                mConnectionCallback.onConnectionStateChanged(this, State.CLOSED);
            }
        }
    }

    /**
     *  action will be invoked when sync session is stopped in app
     *  it is determined by app about when to stop log session of current sync session
     */
    public void notifySessionStop() {
        if (mLogSession.isLogOngoing()) {
            mLogSession.stop();
        }
        submitLogSession();
    }

    public ShineDevice getDevice() {
        synchronized (mShineProfileCore.lockObject) {
            if (mState == State.IDLE) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_DEVICE);
                return null;
            }

            LogEventItem logEventItem = newLogEventItem(LogEventItem.EVENT_GET_DEVICE);
            logEventItem.mRequestStartedLog = new RequestStartedLog(null);

            ShineDevice shineDevice = mShineProfileCore.getDevice();

            logEventItem.mRequestFinishedLog = new RequestFinishedLog(
                    ShineProfileCore.RESULT_SUCCESS);

            JSONObject valueJSON = new JSONObject();
            if (shineDevice != null) {
                try {
                    valueJSON.put("name", shineDevice.getName());
                    valueJSON.put("address", shineDevice.getAddress());
                    valueJSON.put("serialNumber", shineDevice.getSerialNumber());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            logEventItem.mResponseFinishedLog = new ResponseFinishedLog(ShineProfileCore.RESULT_SUCCESS, valueJSON);

            return shineDevice;
        }
    }

    /**
     * Private - PhaseController
     */
    private boolean startPhaseController(PhaseController phaseController) {
        ActionID actionID = phaseController.getActionID();

        if (actionID == null) {
            Log.e(TAG, "PhaseController NULL");
            return false;
        }

        LogEventItem logEventItem = makeLogEventItemFromEventName(phaseController.getStartLogEventName());
        if (logEventItem == null) {
            Log.e(TAG, "LogEventItem NULL");
            return false;
        }

        mCurrentPhaseLogItem = logEventItem;
        mCurrentPhaseLogItem.mRequestStartedLog = new RequestStartedLog(null);

        mActionID = actionID;
		if(mActionID == ActionID.OTA) {
			mState = State.OTA;
		}

        mCurrentPhaseController = phaseController;
        mCurrentPhaseController.start();

        mCurrentPhaseLogItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_SUCCESS);
		return true;
    }

	/**
	 * Interrupt current command
	 * @return
	 */
    public boolean interrupt() {
        synchronized (mShineProfileCore.lockObject) {
            if(!isStreaming()){
                if (!isBusy() || mCurrentPhaseController == null) {
                    logUnexpectedEvent(LogEventItem.EVENT_INTERRUPT);
                    return false;
                }
            }

            LogEventItem logEventItem = makeLogEventItemFromEventName(LogEventItem.EVENT_INTERRUPT);
            if (logEventItem == null) {
                Log.e(TAG, "LogEventItem NULL");
                return false;
            }

            logEventItem.mRequestStartedLog = new RequestStartedLog(null);
            mCurrentPhaseController.stop();
            logEventItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_SUCCESS);

            mIsInStreaming = false;

            return true;
        }
    }

    private void stopOnGoingOperation() {
		if (!isBusy()) {
			return;
		}
		interrupt();
    }

    public State getState() {
        return mState;
    }

	public ActionID getCurrentAction() {
		return mActionID;
	}

    /**
     * Make Log Event Item from Event Name
     * @param eventName
     * @return
     */
    private LogEventItem makeLogEventItemFromEventName(String eventName) {
        LogEventItem logEventItem = null;
        if (!TextUtils.isEmpty(eventName)) {
            logEventItem =  newLogEventItem(eventName);
        }
        return logEventItem;
    }

    private void submitLogSession() {
        LogManager.getDefault().saveAndUploadLog(mLogSession);
        mLogSession.clearLogItems();
    }

    /**
     * Private - Internal vars
     */

    private void setState(State newState) {
        if (mState != newState) {
            mState = newState;
        }
    }

    private void setFirmwareVersion(String firmwareVersion) {
        mFirmwareVersion = firmwareVersion;

        if (mLogSession.getFirmwareVersion() == null) {
            mLogSession.setFirmwareVersion(firmwareVersion);
        }
    }

    private void setModelNumber(String modelNumber) {
        mModelNumber = modelNumber;

        if (mLogSession.getModelNumber() == null) {
            mLogSession.setModelNumber(modelNumber);
        }
    }

    private void updateShineSerialNumber(String serialNumber) {
        ShineDevice shineDevice = mShineProfileCore.getDevice();
        if (serialNumber == null
                || shineDevice == null
                || serialNumber.equals(shineDevice.getSerialNumber()))
            return;

        shineDevice.setSerialNumber(serialNumber);
        ShineDeviceFactory.saveDevicesCache();
    }

	/**
	 * Check the connection of the program with the Bluetooth Device
	 * @return
	 */
	public boolean isConnected() {
		return (State.CONNECTED == mState || State.OTA == mState);
	}

	/**
	 * Check the program is busy executing a controller
	 * @return
	 */
    public boolean isBusy() {
        return isConfiguring() || isTransferringData() || isUpdatingFirmware();
    }

    public boolean isReady() {
        return (mState == State.CONNECTED) && (mActionID == null);
    }

	/**
	 * Check the program is executing a configured action
	 * @return
	 */
	private boolean isConfiguring() {
		return (mState == State.CONNECTED) && !(mActionID == null || mActionID == ActionID.STREAM_USER_INPUT_EVENTS || mActionID == ActionID.SYNC);
    }

	/**
	 * Check the program is executing a transferred action
	 * @return
	 */
    private boolean isTransferringData() {
		return (mState == State.CONNECTED) && (mActionID == ActionID.STREAM_USER_INPUT_EVENTS || mActionID == ActionID.SYNC);
	}

	/**
	 * Check the program is executing an OTA updating
	 * @return
	 */
	private boolean isUpdatingFirmware() {
		return mState == State.OTA;
	}

    public boolean isStreaming() {
        return (mState == State.CONNECTED) && mIsInStreaming;
    }

    /**
     * Log Event
     */

    private void logUnexpectedEvent(String eventName, JSONObject...jsonObjects) {
        if (mLogSession != null) {
            LogUnexpectedEventItem unexpectedEvent = new LogUnexpectedEventItem(mState.getValue(), eventName, jsonObjects);
            mLogSession.addLogItem(unexpectedEvent);
        }
    }

    private LogEventItem newLogEventItem(String eventName) {
        LogEventItem logEventItem = new LogEventItem(eventName);
        if (mLogSession != null) {
            mLogSession.addLogItem(logEventItem);
        }
        return logEventItem;
    }

    /**
     * MisfitShineProfileCoreCallback
     */
    private ShineCoreCallback mCoreCallback = new ShineCoreCallback() {

        @Override
        public void onFirstResponseReceivedResult(Request request, int result, byte[] data) {
            // Disable synchronized since this method doesn't do anything aside from logging.
            // synchronized (ShineProfile.this.lockObject) {
            if (mCurrentRequestLogItem == null || mCurrentRequestLogItem.mResponseStartedLog != null)
                return;

            JSONObject json = new JSONObject();
            try {
                json.put("data", Convertor.bytesToString(data));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //for dummy file list request
            if (mState == State.CONNECTING && request instanceof FileListRequest){
                mCurrentRequestLogItem.mResponseStartedLog = new ResponseStartedLog(result, json);
                return;
            }

            if (isBusy() == false) {
                logUnexpectedEvent(LogEventItem.EVENT_ON_FIRST_RESPONSE_RECEIVED, json);
                return;
            }
            mCurrentRequestLogItem.mResponseStartedLog = new ResponseStartedLog(result, json);
        };

        @Override
        public void onResponseReceivedResult(Request request, int result) {
            synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
                //for dummy file list request
                if(mState==State.CONNECTING && request instanceof FileListRequest){
                    mCurrentRequestLogItem.mResponseFinishedLog = new ResponseFinishedLog(result, request.getResponseDescriptionJSON());
                    didSentDummyFileListRequest();
                    return;
                }
                if (isBusy() == false) {
                    logUnexpectedEvent(LogEventItem.EVENT_ON_RESPONSE_RECEIVED, request.getResponseDescriptionJSON());
                    return;
                }
                LogEventItem requestLogEventItem = mCurrentRequestLogItem;

                requestLogEventItem.mResponseFinishedLog = new ResponseFinishedLog(result, request.getResponseDescriptionJSON());
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, requestLogEventItem.getEventName()+ " response finish: " + requestLogEventItem.mResponseFinishedLog.toJSONObject());
                }
                mCurrentPhaseController.onResponseReceivedResult(request, result);
            }
        };

        @Override
		public void onRequestSentResult(Request request, int result) {
			synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
                //for dummy file list request
                if (mState == State.CONNECTING && request instanceof FileListRequest) {
                    mCurrentRequestLogItem.mRequestFinishedLog = new RequestFinishedLog(result, request.getRequestDataJSON());
                    return;
                } else if (isButtonRequest(request)) {
					sendButtonRequestCallback(request, result, mConfigurationCallback);
					return;
				} else if (isBusy() == false) {
					logUnexpectedEvent(LogEventItem.EVENT_ON_REQUEST_SENT, request.getNameJSON());
					return;
				} else if (mCurrentPhaseController == null) {
                    Log.e(TAG, "Current PhaseController is NULL");
                    return;
                }
                mCurrentRequestLogItem.mRequestFinishedLog = new RequestFinishedLog(result, request.getRequestDataJSON());
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, mCurrentRequestLogItem.getEventName() + " request finish: " + mCurrentRequestLogItem.mRequestFinishedLog.toJSONObject());
                }
				mCurrentPhaseController.onRequestSentResult(request, result);
			}
		}

        @Override
        public void onConnectionStateChange(int status, int newState) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.format("onConnectionStateChange() is called, status is %d, newState is %d", status, newState));
            }

            synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
                int result = (status == BluetoothGatt.GATT_SUCCESS)
                        ? ShineProfileCore.RESULT_SUCCESS : ShineProfileCore.RESULT_FAILURE;

                if (mConnectionCallback != null && mConnectionCallback instanceof ConnectionCallbackForTest) {
                    ConnectionCallbackForTest callbackForTest = (ConnectionCallbackForTest) mConnectionCallback;
                    callbackForTest.onConnectionStateChangedForTest(ShineProfile.this, status, newState,
                        mConnectFailCode.sumConnectFailEnum());
                }
                if (mState != State.CONNECTING && mState != State.DISCONNECTING) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("result", result);
                        json.put("status", status);
                        json.put("mState", mState);
                        json.put("newState", newState);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    logUnexpectedEvent(LogEventItem.EVENT_ON_CONNECTION_STATE_CHANGE, json);
                } else {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("newState", newState);
                        json.put("status", status);
                        json.put("mState", mState);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mCurrentLogItem.mResponseFinishedLog = new ResponseFinishedLog(result, json);
                }

                if (mState == State.CONNECTING) { // record the Bluetooth Gatt status only when connecting
                    mConnectFailCode.setConnectGattStatus(status);
                }

                if (mState == State.CONNECTING && newState == BluetoothProfile.STATE_CONNECTED) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        mConnectFailCode.setConnectPhase(ConnectPhase.MISFIT_HANDSHAKE);
                        mConnectFailCode.setHandshakePhase(HandshakePhase.DISCOVER_SERVICES);
                        mConnectTimer.schedule(new IndexedTimerTask(mCurrentConnectIndex) {
                            @Override
                            public void run() {
                                if (getIndex() == mCurrentConnectIndex
                                        && mState == State.CONNECTING
                                        && mInConnectAttempt) {
                                    if (!mShineProfileCore.handshake()) {
                                        disconnect();
                                    }
                                }
                            }
                        }, Constants.DISCOVERYSERVICES_DELAY);
                    } else { // for all the other non GATT_SUCCESS status
                        disconnect();
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    setState(State.DISCONNECTED);
                    if (mInConnectAttempt){
                        internalClose();
                    } else {
                        closeImpl();
                    }
                }
            }
        }

        @Override
        public void onHandshakingResult(int result) {
            synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onHandshakingResult(), result is %d" + result);
                }

                if (mState != State.CONNECTING) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onHandshakingResult(), mState is not CONNECTING, return now");
                    }
                    JSONObject json = new JSONObject();
                    try {
                        json.put("result", result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    logUnexpectedEvent(LogEventItem.EVENT_ON_SUBSCRIBE_CHARACTERISTICS, json);
                    return;
                }

                if (result == ShineProfileCore.RESULT_SUCCESS) {
                    mCurrentLogItem = newLogEventItem(LogEventItem.EVENT_GET_SERIAL_NUMBER);
                    mCurrentLogItem.mRequestStartedLog = new RequestStartedLog(null);
                    mConnectFailCode.setHandshakePhase(HandshakePhase.GET_SERIAL_NUMBER);

                    if (mShineProfileCore.readSerialNumber() == false) {
                        mCurrentLogItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_FAILURE);
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "call ShineProfileCore#readSerialNumber() return false, disconnect now");
                        }
                        disconnect();
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onHandshakingResult(), result is not SUCCESS, disconnect now");
                    }
                    disconnect();
                }
            }
        }

        /**
         * this callback is added only to update mConnectFailCode when DiscoverServices() completes
         */
        @Override
        public void onServicesDiscovered() {
            mConnectFailCode.setHandshakePhase(HandshakePhase.SUBSCRIBE_CHARACTERISTIC_NOTIFICATION);
        }

        @Override
        public void onSerialNumberReadResult(int result, String serialNumber) {
            synchronized (ShineProfile.this.mShineProfileCore.lockObject) {

                JSONObject json = new JSONObject();
                try {
                    json.put("serialNumber", serialNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (mState != State.CONNECTING) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onSerialNumberReadResult(), mState != CONNECTING, return now instead of readModelName");
                    }
                    logUnexpectedEvent(LogEventItem.EVENT_ON_SERIAL_NUMBER_READ, json);
                    return;
                }
                mCurrentLogItem.mResponseFinishedLog = new ResponseFinishedLog(result, json);

                if (result == ShineProfileCore.RESULT_SUCCESS) {
                    updateShineSerialNumber(serialNumber);

                    mCurrentLogItem = newLogEventItem(LogEventItem.EVENT_GET_MODEL_NUMBER);
                    mCurrentLogItem.mRequestStartedLog = new RequestStartedLog(null);
                    mConnectFailCode.setHandshakePhase(HandshakePhase.GET_MODEL_NAME);

                    if (mShineProfileCore.readModelNumber() == false) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "onSerialNumberReadResult(), readModelNumber() return false, disconnect now");
                        }
                        mCurrentLogItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_FAILURE);
                        disconnect();
                    }
                } else if (result == ShineProfileCore.RESULT_INSUFFICIENT_AUTHENTICATION) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onSerialNumberReadResult(), result is INSUFFICIENT_AUTHENTICATION, createBond() now");
                    }
                    createBond();
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onSerialNumberReadResult(), disconnect in 2s later");
                    }
					mainHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							disconnect();
						}
					}, 2000);
				}
            }
        }

        @Override
        public void onModelNumberReadResult(int result, String modelNumber) {
            synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
                JSONObject json = new JSONObject();
                try {
                    json.put("model", modelNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (mState != State.CONNECTING) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onModelNumberReadResult(), mState != CONNECTING, return now instead of readFirmwareVersion");
                    }
                    logUnexpectedEvent(LogEventItem.EVENT_ON_MODEL_NUMBER_READ, json);
                    return;
                }
                mCurrentLogItem.mResponseFinishedLog = new ResponseFinishedLog(result, json);

                if (result == ShineProfileCore.RESULT_SUCCESS && !TextUtils.isEmpty(modelNumber)) {
                    setModelNumber(modelNumber);

                    mCurrentLogItem = newLogEventItem(LogEventItem.EVENT_GET_FIRMWARE_VERSION);
                    mCurrentLogItem.mRequestStartedLog = new RequestStartedLog(null);
                    mConnectFailCode.setHandshakePhase(HandshakePhase.GET_FIRMWARE_VERSION);

                    if (mShineProfileCore.readFirmwareVersion() == false) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "onModelNumberReadResult(), readFirmwareVersion() return false, disconnect now");
                        }
                        mCurrentLogItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_FAILURE);
                        disconnect();
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onModelNumberReadResult(), disconnect now");
                    }
                    disconnect();
                }
            }
        }

        @Override
        public void onFirmwareVersionReadResult(int result, String firmwareVersion) {
            synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
                JSONObject json = new JSONObject();
                try {
                    json.put("version", firmwareVersion);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (mState != State.CONNECTING) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onFirmwareVersionReadResult(), mState != CONNECTING, return now instead of sendDummyFileListRequest");
                    }
                    logUnexpectedEvent(LogEventItem.EVENT_ON_FIRMWARE_VERSION_READ, json);
                    return;
                }
                mCurrentLogItem.mResponseFinishedLog = new ResponseFinishedLog(result, json);

                if (result == ShineProfileCore.RESULT_SUCCESS) {
                    setFirmwareVersion(firmwareVersion);
                    mConnectFailCode.setHandshakePhase(HandshakePhase.DONE);
                    sendDummyFileListRequest();
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onFirmwareVersionReadResult(), result != SUCCESS, disconnect");
                    }
                    disconnect();
                }
            }
        }

        @Override
        public void onReadRemoteRssi(int result, int rssi) {
            synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
				if(mReadRSSICallback == null) {
					Log.e(TAG, "ReadRSSICallback is NULL");
                    return;
				}

                JSONObject json = new JSONObject();
                try {
                    json.put("result", result);
                    json.put("rssi", rssi);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!isConnected()) {
                    logUnexpectedEvent(LogEventItem.EVENT_ON_READ_REMOTE_RSSI, json);
                    return;
                }

				Hashtable<ShineProperty, Object> objects = new Hashtable<>();
                if (result == ShineProfileCore.RESULT_SUCCESS) {
					objects.put(ShineProperty.RSSI, rssi);
                    mReadRSSICallback.onConfigCompleted(ActionID.READ_REMOTE_RSSI, ActionResult.SUCCEEDED, objects);
                } else {
					objects.put(ShineProperty.RSSI, -1);
                    mReadRSSICallback.onConfigCompleted(ActionID.READ_REMOTE_RSSI, ActionResult.FAILED, objects);
                }
            }
        }

        @Override
        public void onPackageTransferred(int result, int totalSize, int transferredSize) {
            // NOTE: Disable synchronized here to avoid affecting performance.
            // synchronized (ShineProfile.this.lockObject) {
            if (mActionID != ActionID.OTA) {
                JSONObject json = new JSONObject();
                try {
                    json.put("totalSize", totalSize);
                    json.put("transferredSize", transferredSize);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                logUnexpectedEvent(LogEventItem.EVENT_ON_PACKAGE_TRANSFERRED, json);
                return;
            }

            mTransferringResult = result;
            mTransferredSize = transferredSize;
            if (mCurrentPhaseController instanceof OTAPhaseController) {
                ((OTAPhaseController) mCurrentPhaseController).onDataTransferResult(result, totalSize, transferredSize);
            }
        }
    };

    private PhaseControllerCallback mPhaseControllerCallback = new PhaseControllerCallback() {


        @Override
        public void onPhaseControllerSendRequest(PhaseController phase, Request request) {
            synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
                if (phase == null || phase.equals(mCurrentPhaseController) == false) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("expectedPhase", mCurrentPhaseController.getClass().getName());
                        json.put("actualPhase", phase.getClass().getName());
                        json.put("request", request.getRequestDescriptionJSON());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    logUnexpectedEvent(LogEventItem.EVENT_ON_SYNC_PHASE_SEND_REQUEST, json);
                    return;
                }

                mCurrentRequestLogItem = newLogEventItem(request.getRequestName());
                mCurrentRequestLogItem.mRequestStartedLog = new RequestStartedLog(request.getRequestDescriptionJSON());
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, mCurrentRequestLogItem.getEventName() + " request start: " + mCurrentRequestLogItem.mRequestStartedLog.toJSONObject());
                }
                // TODO Check
                if (FirmwareCompatibility.isSupportedRequest(mFirmwareVersion, mModelNumber, request) == false) {
                    mCurrentRequestLogItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_UNSUPPORTED);
                    mCurrentPhaseController.onRequestSentResult(request, ShineProfileCore.RESULT_UNSUPPORTED);
                    return;
                }

                sendRequestImpl(request, 2);
            }
        }

        /*
         * Problem: when sending "GET" request, there's a possibility that "onChanged" event occurs before "onWrite" does. All attempts to send the next request before "onWrite" will result in failures.
         * Solution: retry upto two times when sendRequest fail, each delayed by 250 msecs.
         */
        public void sendRequestImpl(final Request request, final int retryLeft) {
            if (mCurrentPhaseController == null || mCurrentPhaseController.hasFinished())
                return;

            if (mShineProfileCore.sendRequest(request)) {
                mCurrentRequestLogItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_SUCCESS, request.getRequestDataJSON());
            } else if (retryLeft <= 0) {
                mCurrentRequestLogItem.mRequestFinishedLog = new RequestFinishedLog(ShineProfileCore.RESULT_FAILURE, request.getRequestDataJSON());
                mCurrentPhaseController.onRequestSentResult(request, ShineProfileCore.RESULT_FAILURE);
            } else {
                Log.d(ShineProfile.TAG, "sendRequestImpl - request: " + request.getRequestName() + ", retry left: " + retryLeft);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendRequestImpl(request, retryLeft - 1);
                    }
                }, 250);
            }
        }

		@Override
		public void onPhaseControllerCompleted(PhaseController phase) {
			synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
				if (phase == null || phase.equals(mCurrentPhaseController) == false) {
					JSONObject json = new JSONObject();
					try {
						json.put("expectedPhase", mCurrentPhaseController.getClass().getName());
						json.put("actualPhase", phase.getClass().getName());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					logUnexpectedEvent(LogEventItem.EVENT_ON_SYNC_PHASE_COMPLETED, json);
					return;
				}

				mCurrentPhaseLogItem.mResponseFinishedLog = new ResponseFinishedLog(mCurrentPhaseController.getResultCode(), null);

				if(isBusy()) {	// Reset to CONNECTED after completed
					mActionID = null;
					setState(State.CONNECTED);
				}
			}
		}

		@Override
		public void onPhaseControllerFailed(PhaseController phase) {
			synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
				if (phase == null || phase.equals(mCurrentPhaseController) == false) {
					JSONObject json = new JSONObject();
					try {
						json.put("expectedPhase", mCurrentPhaseController.getClass().getName());
						json.put("actualPhase", phase.getClass().getName());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					logUnexpectedEvent(LogEventItem.EVENT_ON_SYNC_PHASE_FAILED, json);
					return;
				}

				mCurrentPhaseLogItem.mResponseFinishedLog = new ResponseFinishedLog(mCurrentPhaseController.getResultCode(), null);

                if(phase instanceof StreamingUserInputEventsPhaseController) {
                    mIsInStreaming = false;
                }

				if(isBusy()) {	// Reset to CONNECTED after completed
					mActionID = null;
					setState(State.CONNECTED);
				}
			}
		}

		@Override
		public void onPhaseControllerDisconnect(PhaseController syncPhase) {
			synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
				disconnect();
			}
		}

		@Override
		public void onPhaseControllerUpdateSerialNumber(PhaseController syncPhase) {
			synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
				updateShineSerialNumber(((ChangeSerialNumberPhaseController) mCurrentPhaseController).getSerialNumber());
			}
		}

		@Override
		public void onPhaseControllerSubmitLogSession(PhaseController syncPhase) {
			synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
				submitLogSession();
			}
		}
	};

    private OTAPhaseControllerCallback mOTAPhaseControllerCallback = new OTAPhaseControllerCallback() {

        @Override
        public void onOTAPhaseControllerTransferData(OTAPhaseController otaPhase, byte[] data, float interpacketDelay) {
            synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
                if (otaPhase == null || otaPhase.equals(mCurrentPhaseController) == false) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("expectedPhase", mCurrentPhaseController.getClass().getName());
                        json.put("actualPhase", otaPhase.getClass().getName());
                        json.put("dataLength", data.length);
                        json.put("interpacketDelay", interpacketDelay);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    logUnexpectedEvent(LogEventItem.EVENT_ON_SYNC_PHASE_TRANSFER_DATA, json);
                    return;
                }

                JSONObject json = new JSONObject();
                try {
                    json.put("dataLength", data.length);
                    json.put("interpacketDelay", interpacketDelay);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mCurrentDataTransferringLogItem = newLogEventItem(LogEventItem.EVENT_TRANSFER_DATA);
                mCurrentDataTransferringLogItem.mRequestStartedLog = new RequestStartedLog(json);

                mTransferredSize = 0;
                mTransferringResult = ShineProfileCore.RESULT_SUCCESS;
                mShineProfileCore.transferData(data, interpacketDelay);
            }
        }

        @Override
        public void onOTAPhaseControllerStopTransferData(OTAPhaseController otaPhase) {
            synchronized (ShineProfile.this.mShineProfileCore.lockObject) {
                if (otaPhase == null || otaPhase.equals(mCurrentPhaseController) == false) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("expectedPhase", mCurrentPhaseController.getClass().getName());
                        json.put("actualPhase", otaPhase.getClass().getName());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    logUnexpectedEvent(LogEventItem.EVENT_ON_SYNC_PHASE_STOP_TRANSFER_DATA, json);
                    return;
                }
                mShineProfileCore.stopTransferData();

                if (mCurrentDataTransferringLogItem != null) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("transferredSize", mTransferredSize);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mCurrentDataTransferringLogItem.mRequestFinishedLog = new RequestFinishedLog(mTransferringResult, json);
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, mCurrentDataTransferringLogItem.getEventName() + " request finish: " + mCurrentDataTransferringLogItem.mRequestFinishedLog.toJSONObject().toString());
                    }
                }
            }
        }

		@Override
		public void onOTAPhaseControllerProgressChanged(OTAPhaseController otaPhase, float progress, OTACallback otaCallback) {
			// NOTE: Disable synchronized here to avoid affecting performance.
			// synchronized (ShineProfile.this.lockObject) {
			if (otaPhase == null || otaPhase.equals(mCurrentPhaseController) == false) {
				JSONObject json = new JSONObject();
				try {
					json.put("expectedPhase", mCurrentPhaseController.getClass().getName());
					json.put("actualPhase", otaPhase.getClass().getName());
					json.put("progress", progress);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				logUnexpectedEvent(LogEventItem.EVENT_ON_SYNC_PHASE_PROGRESS_CHANGED, json);
				return;
			}
			otaCallback.onOTAProgressChanged(progress);
		}
	};

    private SyncPhaseControllerCallback mSyncPhaseControllerCallback = new SyncPhaseControllerCallback() {

        @Override
        public void onSyncPhaseControllerSyncDataReadProgress(SyncPhaseController syncPhaseController, Bundle extraInfo, MutableBoolean shouldStop, SyncCallback syncCallback) {
            if (syncPhaseController == null || syncPhaseController.equals(mCurrentPhaseController) == false) {
                JSONObject json = new JSONObject();
                try {
                    json.put("expectedPhase", mCurrentPhaseController.getClass().getName());
                    json.put("actualPhase", syncPhaseController.getClass().getName());
                    json.put("progress", extraInfo.getFloat(ShineProfile.SYNC_PROGRESS_KEY, -1));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                logUnexpectedEvent(LogEventItem.EVENT_ON_SYNC_PHASE_PROGRESS_CHANGED, json);
                shouldStop.setValue(true);
                return;
            }
            syncCallback.onSyncDataRead(extraInfo, shouldStop);
        }

        @Override
        public void onSyncPhaseControllerSyncDataReadCompleted(SyncPhaseController syncPhaseController, List<SyncResult> syncResults,  MutableBoolean shouldStop, SyncCallback syncCallback) {
            if (syncPhaseController == null || syncPhaseController.equals(mCurrentPhaseController) == false) {
                JSONObject json = new JSONObject();
                try {
                    json.put("expectedPhase", mCurrentPhaseController.getClass().getName());
                    json.put("actualPhase", syncPhaseController.getClass().getName());
                    json.put("progress", "read_completed");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                logUnexpectedEvent(LogEventItem.EVENT_ON_SYNC_PHASE_PROGRESS_CHANGED, json);
                shouldStop.setValue(true);
                return;
            }
            JSONObject value = new JSONObject();
            if (syncResults != null && syncResults.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                try {
                    for (SyncResult syncResult : syncResults) {
                        JSONObject jsonSyncResult = new JSONObject();
                        jsonSyncResult.put("startTimestamp", syncResult.getHeadStartTime());
                        jsonSyncResult.put("activityCount", syncResult.getTotalMinutes());
                        jsonArray.put(jsonSyncResult);
                    }
                    value.put("arrayData", jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                LogEventItem logEventItem = newLogEventItem(LogEventItem.EVENT_SYNC_RESULT_TRACKING);
                logEventItem.mResponseFinishedLog = new ResponseFinishedLog(0, value);
            }
            syncCallback.onSyncDataReadCompleted(syncResults, shouldStop);
        }

        @Override
        public void onGetActivityDataFinished() {
            mLogSession.stop();
            submitLogSession();
            mLogSession.start();
        }

        @Override
        public void onHWLogRead(byte[] hwLog, SyncCallback syncCallback) {
            if (syncCallback != null) {
                syncCallback.onHardwareLogRead(hwLog);
            }
        }
    };

    /*
     * Bonding State
     */
    private boolean createBond() {
        if (getDevice() == null)
            return false;

        return getDevice().createBond();
    }

    /*
     * Bluetooth State
     */
    private void monitorBluetoothState() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        GlobalVars.getApplicationContext().registerReceiver(mBluetoothStateBroadcastReceiver, filter);
    }

    private void stopMonitorBluetoothState() {
        try {
            GlobalVars.getApplicationContext().unregisterReceiver(mBluetoothStateBroadcastReceiver);
        } catch (IllegalArgumentException exception) {
            String deviceAddress = (getDevice() != null) ? getDevice().getAddress() : "UnknownDevice";
            Log.e(TAG, deviceAddress + " - state=" + mState);
            exception.printStackTrace();
        }
    }

    /**
     * TODO: Workaround FTC Characteristic stuck at "InProgress" when 1st connected.
     */
    private void sendDummyFileListRequest() {
        synchronized (mShineProfileCore.lockObject) {
            FileListRequest request = new FileListRequest(){
                @Override
                public int getTimeOut() {
                    return Constants.DUMMY_FILE_LIST_TIMEOUT;
                }
            };
            request.buildRequest();

            mCurrentRequestLogItem = newLogEventItem(LogEventItem.EVENT_SEND_DUMMY_FILE_LIST);
            mCurrentRequestLogItem.mRequestStartedLog = new RequestStartedLog(null);

            mShineProfileCore.sendRequest(request);
        }
    }

    private void didSentDummyFileListRequest() {
        synchronized (mShineProfileCore.lockObject) { // finally handshake succeeds
            mInConnectAttempt = false;
        }
        setState(State.CONNECTED);
        mConnectionCallback.onConnectionStateChanged(ShineProfile.this, State.CONNECTED);
    }

    private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if (mState != State.CLOSED) {
                            close();
                        }
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    case BluetoothAdapter.STATE_ON:
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "BluetoothAdapter state changed: " + state);
                        break;
                }
            }
        }
    };

	/**
	 * Pluto API
	 * @author Quoc-Hung Le
	 */

	/**
	 * Configure LED, Vibe and Sound when user stay idle for too long
	 *
	 * @param inactivityNudgeSettings refer to {@link InactivityNudgeSettings}
	 * @return operation was started successfully
	 */
	public boolean setInactivityNudge(InactivityNudgeSettings inactivityNudgeSettings, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_SET_INACTIVITY_NUDGE);
                Log.d(TAG, "setInactivityNudge() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.setInactivityNudge(inactivityNudgeSettings, configurationCallback));
		}
	}

	/**
	 * Get current inactivity nudge settings
	 * @return operation was started successfully
	 */
	public boolean getInactivityNudge(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_GET_INACTIVITY_NUDGE);
                Log.d(TAG, "getInactiveNudge() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.getInactivityNudge(configurationCallback));
		}
	}

	/**
	 * Configure alarm time and LED, Vibe and Sound sequence.
	 *
	 * @param alarmSettings refer to {@link AlarmSettings}
	 * @return operation was started successfully
	 */
	public boolean setSingleAlarm(AlarmSettings alarmSettings, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_SET_SINGLE_ALARM_TIME);
                Log.d(TAG, "setSingleAlarm() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.setSingleAlarm(alarmSettings, configurationCallback));
		}
	}

	/**
	 * Get current alarm settings
	 *
	 * @param alarmDay refer to Constants in {@link AlarmSettings}: SUNDAY, MONDAY,..., ALL_DAYS
	 * @return operation was started successfully
	 */
	public boolean getSingleAlarm(byte alarmDay, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_GET_SINGLE_ALARM_TIME);
                Log.d(TAG, "getSingleAlarm() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.getSingleAlarm(alarmDay, configurationCallback));
		}
	}

	/**
	 * Remove all existing alarms
	 * @return operation was started successfully
	 */
	public boolean clearAllAlarms(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if(!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_CLEAR_ALL_ALARMS);
                Log.d(TAG, "clearAllAlarms() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.clearAllAlarms(configurationCallback));
		}
	}

	/**
	 * Configure LED, Vibe and Sound when user hit goal
	 *
	 * @param goalHitNotificationSettings refer to {@link GoalHitNotificationSettings}
	 * @return operation was started successfully
	 */
	public boolean setGoalReachNotification(GoalHitNotificationSettings goalHitNotificationSettings, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if(!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_SET_GOAL_HIT_NOTIFICATION);
                Log.d(TAG, "setGoalReachNotification() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.setGoalReachNotification(goalHitNotificationSettings, configurationCallback));
		}
	}

	/**
	 * Get current goal hit notification settings
	 *
	 * @return operation was started successfully
	 */
	public boolean getGoalReachNotification(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if(!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_GET_GOAL_HIT_NOTIFICATION);
                Log.d(TAG, "getGoalReachNotification() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.getGoalReachNotification(configurationCallback));
		}
	}

	/**
	 * Configure LED, Vibe and Sound when there is call or text coming
	 *
	 * @param notificationsSettings refer to {@link NotificationsSettings}
	 * @return
	 */
	public boolean setCallTextNotifications(NotificationsSettings notificationsSettings, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_SET_CALL_TEXT_NOTIFICATIONS);
                Log.d(TAG, "setCallTextNotification() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.setCallTextNotifications(notificationsSettings, configurationCallback));
		}
	}

	/**
	 * Get current notification settings
	 *
	 * @return operation was started successfully
	 */
	public boolean getCallTextNotifications(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if(!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_GET_CALL_TEXT_NOTIFICATIONS);
                Log.d(TAG, "getCallTextNotification() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.getCallTextNotifications(configurationCallback));
		}
	}

    /**
     * Set activity type, only for speedo
     *
     * @param activityType refer to {@link ActivityType}
     * @return operation was started successfully
     */
    public boolean setActivityType(ActivityType activityType, ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_SET_ACTIVITY_TYPE);
                Log.d(TAG, "setActivityType() return false: is not ready");
                return false;
            }

            return startPhaseController(mShineControllers.setActivityType(activityType, configurationCallback));
        }
    }

    /**
     * Get activity type, only for speedo. If succeeded, will return {@link ActivityType} by key {@link ShineProperty#ACTIVITY_TYPE}
     *
     * @return operation was started successfully
     */
    public boolean getActivityType(ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_GET_ACTIVITY_TYPE);
                Log.d(TAG, "getActivityType() return false: is not ready");
                return false;
            }

            return startPhaseController(mShineControllers.getActivityType(configurationCallback));
        }
    }

	/**
	 * Disable all BLE notifications
	 *
	 * @return operation was started successfully
	 */
	public boolean disableAllCallTextNotifications(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if(!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_DISABLE_ALL_CALL_TEXT_NOTIFICATIONS);
                Log.d(TAG, "disableAllCallTextNotification() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.disableAllNotifications(configurationCallback));
		}
	}

 	/**
	 * Send call notifcation
	 *
	 * @return operation was started successfully
	 */
	public boolean sendCallNotification(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_SEND_CALL_NOTIFICATION);
				return false;
			}

			return startPhaseController(mPlutoControllers.sendCallNotification(configurationCallback));
		}
	}

	/**
	 * Send text notifcation
	 *
	 * @return operation was started successfully
	 */
	public boolean sendTextNotification(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_SEND_TEXT_NOTIFICATION);
				return false;
			}

			return startPhaseController(mPlutoControllers.sendTextNotification(configurationCallback));
		}
	}

    /**
     * Stop call/text notification
     *
     * @return operation was started successfully
     */
    public boolean sendStopNotification(ConfigurationCallback configurationCallback) {
        synchronized(mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_STOP_NOTIFICATION);
                return false;
            }
            return startPhaseController(mPlutoControllers.stopNotification(configurationCallback));
        }
    }

	/**
	 * Play {@code vibrationSequence} for {@code numOfRepeats} times
	 *
	 * @param vibrationSequence    refer to {@link com.misfit.ble.setting.pluto.PlutoSequence.Vibe}
	 * @param numOfRepeats         number of repeats
	 * @param millisBetweenRepeats milliseconds between repeats
	 * @return operation was started successfully
	 */
	public boolean playVibration(PlutoSequence.Vibe vibrationSequence, short numOfRepeats, int millisBetweenRepeats, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_PLAY_VIBRATION);
                Log.d(TAG, "playVibration() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.playVibration(vibrationSequence, numOfRepeats, millisBetweenRepeats, configurationCallback));
		}
	}

	/**
	 * Play {@code soundSequence} for {@code numOfRepeats} times
	 *
	 * @param soundSequence        refer to {@link com.misfit.ble.setting.pluto.PlutoSequence.Sound}
	 * @param numOfRepeats         number of repeats
	 * @param millisBetweenRepeats milliseconds between repeats
	 * @return operation was started successfully
	 */
	public boolean playSound(PlutoSequence.Sound soundSequence, short numOfRepeats, int millisBetweenRepeats, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_PLAY_SOUND);
                Log.d(TAG, "playSound() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.playSound(soundSequence, numOfRepeats, millisBetweenRepeats, configurationCallback));
		}
	}

	/**
	 * Play {@code ledAnimationSequence} for {@code numOfRepeats} times
	 *
	 * @param ledAnimationSequence refer to {@link com.misfit.ble.setting.pluto.PlutoSequence.LED}
	 * @param numOfRepeats         number of repeats
	 * @param millisBetweenRepeats milliseconds between repeats
	 * @return operation was started successfully
	 */
	public boolean playLEDAnimation(PlutoSequence.LED ledAnimationSequence, short numOfRepeats, int millisBetweenRepeats, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_PLAY_LED_ANIMATION);
                Log.d(TAG, "playLEDAnimation() return false: is not ready");
                return false;
			}

			return startPhaseController(mPlutoControllers.playLedAnimation(ledAnimationSequence, numOfRepeats, millisBetweenRepeats, configurationCallback));
		}
	}

    /**
     * Start a specified notification, include LED/Vibration
     * <br>NOTE: currently device has a feature to stop any animation/vibe that take long time (>35s) to play. So number of repeats will be limited to 20 for short animation/vibe and 10 for long one.</br>
     * @param led could be one of {@link com.misfit.ble.setting.pluto.PlutoSequence.LED#SPECIFIED_SHORT}, {@link com.misfit.ble.setting.pluto.PlutoSequence.LED#SPECIFIED_LONG}
     * @param color could be one of {@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_BLUE}, {@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_GREEN}, {@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_ORANGE}, {@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_PINK}, {@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_PURPLE}, {@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_YELLOW}
     * @param animationRepeats 0 for not play, 1-20 for number of blinks
     * @param timeBetweenAnimationRepeats the time between every repeats in milliseconds, NOT USED YET
     * @param vibe could be one of {@link com.misfit.ble.setting.pluto.PlutoSequence.Vibe#SPECIFIED_SHORT}, {@link com.misfit.ble.setting.pluto.PlutoSequence.Vibe#SPECIFIED_LONG}
     * @param vibeRepeats 0 for not play, 1-20 for number of vibes
     * @param timeBetweenVibeRepeats the time between every repeats in milliseconds, NOT USED YET
     * @return operation was started successfully
     */
    public boolean startSpecifiedNotification(PlutoSequence.LED led,
                                           PlutoSequence.Color color,
                                           byte animationRepeats,
                                           short timeBetweenAnimationRepeats,
                                           PlutoSequence.Vibe vibe,
                                           byte vibeRepeats,
                                           short timeBetweenVibeRepeats,
                                           ShineProfile.ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_START_SPECIFIED_NOTIFICATION);
                return false;
            }
            return startPhaseController(mPlutoControllers.startSpecifiedNotification(led, color, animationRepeats, timeBetweenAnimationRepeats, vibe, vibeRepeats, timeBetweenVibeRepeats, configurationCallback));
        }
    }

    /**
     * Start a specified animation
     * <br>NOTE: currently device has a feature to stop any animation/vibe that take long time (>35s) to play. So number of repeats will be limited to 20 for short animation/vibe and 10 for long one.</br>
     * @param led could be one of {@link com.misfit.ble.setting.pluto.PlutoSequence.LED#SPECIFIED_SHORT}, {@link com.misfit.ble.setting.pluto.PlutoSequence.LED#SPECIFIED_LONG}
     * @param repeats 0 for not play, 1-20 for number of blinks
     * @param timeBetweenRepeats the time between every repeats in milliseconds, NOT USED YET
     * @param color could be one of
     * <br>{@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_BLUE}</br>
     * <br>{@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_GREEN}</br>
     * <br>{@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_ORANGE}</br>
     * <br>{@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_PINK}</br>
     * <br>{@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_PURPLE}</br>
     * <br>{@link com.misfit.ble.setting.pluto.PlutoSequence.Color#SPECIFIED_YELLOW}</br>
     * @return operation was started successfully
     */
    public boolean startSpecifiedAnimation(PlutoSequence.LED led, byte repeats, short timeBetweenRepeats, PlutoSequence.Color color,
                                           ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_STOP_NOTIFICATION);
                return false;
            }
            return startPhaseController(mPlutoControllers.startSpecifiedAnimation(led, repeats, timeBetweenRepeats, color, configurationCallback));
        }
    }

    /**
     * Start a specified vibration
     * <br>NOTE: currently device has a feature to stop any animation/vibe that take long time (>35s) to play. So number of repeats will be limited to 20 for short animation/vibe and 10 for long one.</br>
     * @param vibe could be one of {@link com.misfit.ble.setting.pluto.PlutoSequence.Vibe#SPECIFIED_SHORT}, {@link com.misfit.ble.setting.pluto.PlutoSequence.Vibe#SPECIFIED_LONG}
     * @param repeats 0 for not play, 1-20 for number of vibes
     * @param timeBetweenRepeats the time between every repeats in milliseconds, NOT USED YET
     * @return operation was started successfully
     */
    public boolean startSpecifiedVibration(PlutoSequence.Vibe vibe, byte repeats, short timeBetweenRepeats,
                                           ConfigurationCallback configurationCallback) {
        synchronized (mShineProfileCore.lockObject) {
            if (!isReady()) {
                logUnexpectedEvent(LogEventItem.EVENT_STOP_NOTIFICATION);
                return false;
            }
            return startPhaseController(mPlutoControllers.startSpecifiedVibration(vibe, repeats, timeBetweenRepeats, configurationCallback));
        }
    }

	/**
	 * Set custom mode for flash link event
	 *
	 * @param actionType    refer to {@link com.misfit.ble.setting.flashlink.CustomModeEnum.ActionType}
	 * @param eventNumber   refer to {@link com.misfit.ble.setting.flashlink.CustomModeEnum.MemEventNumber}
	 * @param animNumber    refer to {@link com.misfit.ble.setting.flashlink.CustomModeEnum.AnimNumber}
	 * @param keyCode       refer to {@link com.misfit.ble.setting.flashlink.CustomModeEnum.KeyCode}
	 * @param releaseEnable <br> wait-release-enable = true: the volume will keep increasing until we release the button </br>
	 *                      <br> wait-release-enable = false: volume only increases once regardless we are holding the button or not </br>
	 *                      <br> Note: wait-release-enable must be false for button actions that don't include hold gesture (e.g. single, double, triple click). Otherwise volume won't stop increasing/decreasing since there is no following release event for those actions.
	 * @return operation was started successfully
	 */
	public boolean setCustomMode(CustomModeEnum.ActionType actionType, CustomModeEnum.MemEventNumber eventNumber,
								 CustomModeEnum.AnimNumber animNumber, CustomModeEnum.KeyCode keyCode,
								 boolean releaseEnable, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_SET_CUSTOM_MODE);
				return false;
			}

			return startPhaseController(mFlashLinkControllers.setCustomMode(actionType, eventNumber, animNumber, keyCode,
                FirmwareCompatibility.customModeReleaseEnable(releaseEnable, getDeviceFamily(), mFirmwareVersion), configurationCallback));
		}
	}

	public boolean addGroupId(short groupId, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_ADD_GROUP_ID);
				return false;
			}

			return startPhaseController(mBoltControllers.addGroupId(groupId, configurationCallback));
		}
	}

	public boolean getGroupId(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_GET_GROUP_ID);
				return false;
			}

			return startPhaseController(mBoltControllers.getGroupId(configurationCallback));
		}
	}

	public boolean setPasscode(byte[] passcode, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_SET_PASSCODE);
				return false;
			}

			return startPhaseController(mBoltControllers.setPassCode(passcode, configurationCallback));
		}
	}

	public boolean getPasscode(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_GET_PASSCODE);
				return false;
			}

            return startPhaseController(mBoltControllers.getPassCode(configurationCallback));
        }
    }
    
	/**
	 * Unmap all events
	 * @return operation was started successfully
	 */
	public boolean unmapAllEvents(ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_UNMAP_ALL_EVENTS);
				return false;
			}

			return startPhaseController(mFlashLinkControllers.unmapAllEvents(configurationCallback));
		}
	}

	/**
	 * Unmap a specific event
	 * @param eventNumber refer to {@link com.misfit.ble.setting.flashlink.CustomModeEnum.MemEventNumber}
	 * @return operation was started successfully
	 */
	public boolean unmapEvent(CustomModeEnum.MemEventNumber eventNumber, ConfigurationCallback configurationCallback) {
		synchronized (mShineProfileCore.lockObject) {
			if (!isReady()) {
				logUnexpectedEvent(LogEventItem.EVENT_UNMAP_EVENT);
				return false;
			}

			return startPhaseController(mFlashLinkControllers.unmapEvent(eventNumber, configurationCallback));
		}
	}

    /**
     * exposed to Misfit App via ShineProfile to get current connect error code
     * */
    public int getConnectFailCodeEnum() {
        return mConnectFailCode.sumConnectFailEnum();
    }

    /**
     * inherited class of TimerTask which holds an index of connect round
     */
    abstract class IndexedTimerTask extends TimerTask {
        private int mIndex = 0;

        public IndexedTimerTask(int idx) {
            mIndex = idx;
        }

        public int getIndex() {
            return mIndex;
        }

        @Override
        public abstract void run();
    }
}
