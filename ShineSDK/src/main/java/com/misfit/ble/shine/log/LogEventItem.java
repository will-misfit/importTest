package com.misfit.ble.shine.log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LogEventItem extends LogItem {
	
	public static final String EVENT_START_SCANNING = "startScanning";
	public static final String EVENT_STOP_SCANNING = "stopScanning";
	public static final String EVENT_RESTART_SCANNING = "restartScanning";
	public static final String EVENT_SCAN_RESULT = "scanResult";
	public static final String EVENT_CONNECTED_DEVICE = "connectedDevice";
	public static final String EVENT_CONNECTED_BLUETOOTH_DEVICE = "connectedBluetoothDevice";

	public static final String EVENT_SEND_DUMMY_FILE_LIST = "sendDummyFileList";

	public static final String EVENT_CONNECT = "connect";
	public static final String EVENT_DISCONNECT = "disconnect";
	public static final String EVENT_CLOSE = "close";
	public static final String EVENT_CLOSE_BY_APP = "closeByApp";
	public static final String EVENT_CLOS_BY_BT_OFF = "closeByBluetoothOff";
	public static final String EVENT_RECONNECT_GATT = "reconnectGatt";
	public static final String EVENT_CLOSE_GATT = "closeGatt";
	
	public static final String EVENT_DISCOVER_SERVICES = "discoverServices";
	public static final String EVENT_SUBSCRIBE_CHARACTERISTIC = "subscribeCharacteristic";
    public static final String EVENT_GET_SERIAL_NUMBER = "getSerialNumber";
	public static final String EVENT_GET_FIRMWARE_VERSION = "getFirmwareVersion";
	public static final String EVENT_GET_MODEL_NUMBER = "getModelNumber";
	public static final String EVENT_GET_DEVICE_FAMILY = "getDeviceFamily";
	public static final String EVENT_GET_DEVICE = "getDevice";

	public static final String EVENT_INTERRUPT = "interrupt";
	
	public static final String EVENT_PLAY_ANIMATION = "playAnimation";
	public static final String EVENT_STOP_PLAYING_ANIMATION = "stopPlayingAnimation";
	
	public static final String EVENT_GET_DEVICE_CONFIGURATION = "getDeviceConfiguration";
	
	public static final String EVENT_SET_DEVICE_CONFIGURATION = "setDeviceConfiguration";
	
	public static final String EVENT_SYNC = "sync";
	public static final String EVENT_SYNC_RESULT_TRACKING = "syncResultTracking";

	public static final String EVENT_OTA = "ota";
	
	public static final String EVENT_CHANGE_SERIAL_NUMBER = "changeSerialNumber";
	
	public static final String EVENT_SET_CONNECTION_PARAMETERS = "setConnectionParameters";

	public static final String EVENT_GET_CONNECTION_PARAMETERS = "getConnectionParameters";

	public static final String EVENT_SET_FLASH_BUTTON_MODE = "setFlashButtonMode";

	public static final String EVENT_GET_FLASH_BUTTON_MODE = "getFlashButtonMode";

	public static final String EVENT_GET_MAPPING_TYPE = "getMappingType";


	// Venus
	public static final String EVENT_ACTIVATE = "activate";
	public static final String EVENT_GET_ACTIVATION_STATE = "getActivationState";
	public static final String EVENT_STREAM_USER_INPUT_EVENTS = "streamUserInputEvents";
	
	public static final String EVENT_READ_RSSI = "readRSSI";
	
	public static final String EVENT_TRANSFER_DATA = "transferData";
	
	public static final String EVENT_ON_FIRST_RESPONSE_RECEIVED = "onFirstResponseReceived";
	public static final String EVENT_ON_RESPONSE_RECEIVED = "onResponseReceived";
	public static final String EVENT_ON_REQUEST_SENT = "onRequestSent"; 
	public static final String EVENT_ON_CONNECTION_STATE_CHANGE = "onConnectionStateChange"; 
	public static final String EVENT_ON_SUBSCRIBE_CHARACTERISTICS = "onHandshaking";
    public static final String EVENT_ON_SERIAL_NUMBER_READ = "onSerialNumberRead";
	public static final String EVENT_ON_FIRMWARE_VERSION_READ = "onFirmwareVersionRead";
	public static final String EVENT_ON_MODEL_NUMBER_READ = "onModelNumberRead";
	public static final String EVENT_ON_READ_REMOTE_RSSI = "onReadRemoteRssi";
	public static final String EVENT_ON_PACKAGE_TRANSFERRED = "onPackageTransferred";
	
	public static final String EVENT_ON_SYNC_PHASE_SEND_REQUEST = "onSyncPhaseSendRequest";
	public static final String EVENT_ON_SYNC_PHASE_COMPLETED = "onSyncPhaseCompleted";
	public static final String EVENT_ON_SYNC_PHASE_FAILED = "onSyncPhaseFailed";
	public static final String EVENT_ON_SYNC_PHASE_TRANSFER_DATA = "onSyncPhaseTransferData";
	public static final String EVENT_ON_SYNC_PHASE_STOP_TRANSFER_DATA = "onSyncPhaseStopTransferData";
	public static final String EVENT_ON_SYNC_PHASE_PROGRESS_CHANGED = "onSyncPhaseProgressChanged";
	
	public static final String EVENT_ON_STREAM_USER_INPUT_EVENTS_RECEIVED = "onUserInputEventsReceived";
	
	public static final String EVENT_GET_STREAMING_CONFIGURATION = "getStreamingConfiguration";
	
	public static final String EVENT_SET_STREAMING_CONFIGURATION = "setStreamingConfiguration";
	
	public static final String EVENT_BUTTON_ANIMATION = "buttonAnimation";
	public static final String EVENT_MAP_EVENT_ANIMATION = "mapEventAnimation";
	public static final String EVENT_UNMAP_ALL_EVENT_ANIMATION = "unmapAllEventAnimation";
	public static final String EVENT_EVENT_MAPPING_SYSTEM_CONTROL = "eventMappingSystemControl";

	// Quoc-Hung Le
	public static final String EVENT_SET_EXTRA_ADV_DATA_STATE = "setExtraAdvertisingDataState";
	public static final String EVENT_GET_EXTRA_ADV_DATA_STATE = "getExtraAdvertisingDataState";

	// Pluto
	public static final String EVENT_SET_INACTIVITY_NUDGE = "setInactivityNudge";
	public static final String EVENT_GET_INACTIVITY_NUDGE = "getInactivityNudge";

	public static final String EVENT_SET_GOAL_HIT_NOTIFICATION = "setGoalHitNotification";
	public static final String EVENT_GET_GOAL_HIT_NOTIFICATION = "getGoalHitNotification";

	public static final String EVENT_SET_SINGLE_ALARM_TIME = "setSingleAlarmTime";
	public static final String EVENT_GET_SINGLE_ALARM_TIME = "getSingleAlarmTime";
	public static final String EVENT_CLEAR_ALL_ALARMS = "clearAllAlarms";

	public static final String EVENT_SET_CALL_TEXT_NOTIFICATIONS = "setCallTextNotifications";
	public static final String EVENT_GET_CALL_TEXT_NOTIFICATIONS = "getCallTextNotifications";
	public static final String EVENT_DISABLE_ALL_CALL_TEXT_NOTIFICATIONS = "disableAllCallTextNotifications";
	public static final String EVENT_SEND_CALL_NOTIFICATION = "sendCallNotification";
	public static final String EVENT_SEND_TEXT_NOTIFICATION = "sendTextNotification";
	public static final String EVENT_STOP_NOTIFICATION = "stopNotification";

	public static final String EVENT_PLAY_VIBRATION = "playVibration";
	public static final String EVENT_PLAY_SOUND = "playSound";
	public static final String EVENT_PLAY_LED_ANIMATION = "playLEDAnimation";

	public static final String EVENT_START_SPECIFIED_NOTIFICATION = "startSpecifiedNotification";
	public static final String EVENT_START_SPECIFIED_ANIMATION = "startSpecifiedAnimation";
	public static final String EVENT_START_SPECIFIED_VIBRATION = "startSpecifiedVibration";

	// Flash Link
	public static final String EVENT_SET_CUSTOM_MODE = "setCustomMode";
	public static final String EVENT_UNMAP_ALL_EVENTS = "unmapAllEvents";
	public static final String EVENT_UNMAP_EVENT = "unmapEvent";

	// Bolt
	public static final String EVENT_ADD_GROUP_ID = "addGroupId";
	public static final String EVENT_GET_GROUP_ID = "getCommandId";

	public static final String EVENT_SET_PASSCODE = "setPassCode";
	public static final String EVENT_GET_PASSCODE = "getPassCode";

	// LapCounting
	public static final String EVENT_GET_LAP_COUNTING_STATUS = "getLapCountingStatus";
	public static final String EVENT_SET_LAP_COUNTING_LICENSE_INFO = "setLapCountingLicenseInfo";
	public static final String EVENT_SET_LAP_COUNTING_MODE = "setLapCountingMode";

	// Speedo
	public static final String EVENT_SET_ACTIVITY_TYPE = "setActivityType";
	public static final String EVENT_GET_ACTIVITY_TYPE = "getActivityType";

	public static class RequestStartedLog {
		public long mTimeMillis;
		public JSONObject mValue;
		
		public RequestStartedLog(JSONObject value) {
			mTimeMillis = System.currentTimeMillis();
			mValue = value;
		}
		
		public JSONObject toJSONObject() {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("timestamp", mTimeMillis * 1.0 / 1000);
				jsonObject.put("value", mValue);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jsonObject;
		}
	}
	
	public static class RequestFinishedLog {
		public long mTimeMillis;
		public int mResult;
		public JSONObject mValue;
		
		public RequestFinishedLog(int result) {
			mTimeMillis = System.currentTimeMillis();
			mResult = result;
		}
		
		public RequestFinishedLog(int result, JSONObject value) {
			mTimeMillis = System.currentTimeMillis();
			mResult = result;
			mValue = value;
		}
		
		public JSONObject toJSONObject() {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("timestamp", mTimeMillis * 1.0 / 1000);
				jsonObject.put("result", mResult);
				jsonObject.put("value", mValue);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jsonObject;
		}
	}
	
	public static class ResponseStartedLog {
		public long mTimeMillis;
		public int mResult;
		public JSONObject mValue;
		
		public ResponseStartedLog(int result, JSONObject value) {
			mTimeMillis = System.currentTimeMillis();
			mResult = result;
			mValue = value;
		}
		
		public JSONObject toJSONObject() {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("timestamp", mTimeMillis * 1.0 / 1000);
				jsonObject.put("result", mResult);
				jsonObject.put("value", mValue);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jsonObject;
		}
	}
	
	public static class ResponseFinishedLog {
		private long mTimeMillis;
		private int mResult;
		private JSONObject mValue;
		
		public ResponseFinishedLog(int result, JSONObject value) {
			mTimeMillis = System.currentTimeMillis();
			mResult = result;
			mValue = value;
		}
		
		public JSONObject toJSONObject() {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("timestamp", mTimeMillis * 1.0 / 1000);
				jsonObject.put("result", mResult);
				jsonObject.put("value", mValue);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jsonObject;
		}
	}
	
	public RequestStartedLog mRequestStartedLog;
	public RequestFinishedLog mRequestFinishedLog;
	public ResponseStartedLog mResponseStartedLog;
	public ResponseFinishedLog mResponseFinishedLog;
	
	public LogEventItem(String eventName) {
		super(eventName);
	}
	
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("event", mEventName);
			
			if (mRequestStartedLog != null)
				jsonObject.put("requestStarted", mRequestStartedLog.toJSONObject());
			
			if (mRequestFinishedLog != null)
				jsonObject.put("requestFinished", mRequestFinishedLog.toJSONObject());
			
			if (mResponseStartedLog != null)
				jsonObject.put("responseStarted", mResponseStartedLog.toJSONObject());
			
			if (mResponseFinishedLog != null)
				jsonObject.put("responseFinished", mResponseFinishedLog.toJSONObject());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
}
