package com.misfit.ble.shine.log;

import com.misfit.ble.sdk.GlobalVars;
import com.misfit.ble.setting.SDKSetting;
import com.misfit.ble.shine.ShineAdapter.ShineScanCallback;
import com.misfit.ble.shine.log.LogEventItem.RequestStartedLog;
import com.misfit.ble.shine.log.LogEventItem.ResponseFinishedLog;
import com.misfit.ble.util.Convertor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class ScanLogSession {
	
	// Limit the number of scanResult events. Keep the first 10 events and the last 30 events.
	private static final int EVENTS_LOWER_BOUND = 10;
	private static final int EVENTS_UPPER_BOUND = EVENTS_LOWER_BOUND + 30;
	
	private long mStartTime;
	private long mEndTime;
	
	private ArrayList<LogItem> mLogItems;
	
	public ScanLogSession() {
		mLogItems = new ArrayList<>();
	}

	public String getName() {
		return String.format(Locale.US, "scan%s%d%s%d",
				LogManager.SESSION_NAME_COMPONENT_SEPARATOR,
				mStartTime, 
				LogManager.SESSION_NAME_COMPONENT_SEPARATOR,
				System.identityHashCode(this));
	}

	public void start(ShineScanCallback callback) {
		mStartTime = System.currentTimeMillis();
		
		JSONObject json = new JSONObject();
		try {
			json.put("callback", Convertor.identity(callback));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		LogEventItem logEventItem = new LogEventItem(LogEventItem.EVENT_START_SCANNING);
		logEventItem.mRequestStartedLog = new RequestStartedLog(json);
		mLogItems.add(logEventItem);
	}

	public void startWithException(String exceptionMsg) {
		JSONObject json = new JSONObject();
		try {
			json.put("ScanException", exceptionMsg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		LogEventItem logEventItem = new LogEventItem(LogEventItem.EVENT_START_SCANNING);
		logEventItem.mRequestStartedLog = new RequestStartedLog(json);
		mLogItems.add(logEventItem);
	}

    /**
     * work with ShineAdapter.internalStartScanning(), compared to start():
     * - without log session start time update
     * - log restartIndex instead of hex conversion of callback
     * */
	public void internalStart(int restartIndex) {
        JSONObject json = new JSONObject();
        try {
            json.put("restartIndex", restartIndex);
        } catch(JSONException e) {
            e.printStackTrace();
        }
        LogEventItem logEventItem = new LogEventItem(LogEventItem.EVENT_RESTART_SCANNING);
        logEventItem.mRequestStartedLog = new RequestStartedLog(json);
        mLogItems.add(logEventItem);
    }
	
	public void stop(ShineScanCallback callback) {
		JSONObject json = new JSONObject();
		try {
			json.put("callback", Convertor.identity(callback));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		LogEventItem logEventItem = new LogEventItem(LogEventItem.EVENT_STOP_SCANNING);
		logEventItem.mRequestStartedLog = new RequestStartedLog(json);
		mLogItems.add(logEventItem);
		
		mEndTime = System.currentTimeMillis();
	}

    /**
     * work with ShineAdapter.internalStopScanning(), compared to stop():
     * - without log session end time update
     * - without hex conversion of callback
     * */
    public void internalStop() {
        JSONObject json = new JSONObject();
        LogEventItem logEventItem = new LogEventItem(LogEventItem.EVENT_STOP_SCANNING);
        logEventItem.mRequestStartedLog = new LogEventItem.RequestStartedLog(json);
        mLogItems.add(logEventItem);
    }

	public void clearLogItems() {
		mLogItems.clear();
	}

	public void addLogEventItem(LogEventItem logEventItem) {
		if (logEventItem != null) {
			mLogItems.add(logEventItem);
		}
	}

	public void appendScanFailError(int errorCode) {
		JSONObject json = new JSONObject();
        try {
			json.put("ScanFailErrorCode", Integer.toString(errorCode));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		appendLogEventItem(json);
	}

	public void appendLogEventItem(JSONObject jsonObj) {
		if (mLogItems.size() >= EVENTS_UPPER_BOUND) {
			mLogItems.remove(EVENTS_LOWER_BOUND);
		}
		LogEventItem logEventItem = new LogEventItem(LogEventItem.EVENT_SCAN_RESULT);
		logEventItem.mResponseFinishedLog = new ResponseFinishedLog(0, jsonObj);
		mLogItems.add(logEventItem);
	}
	
	@Override
	public String toString() {
		String content = null;
		try {
			content = toJSON().toString(4);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		try {
			String userId = SDKSetting.getUserId();
			json.put("user_id", ((userId != null) ? userId : ""));
			json.put("start_at", mStartTime / 1000);
			json.put("end_at", mEndTime / 1000);
			json.put("sdk_version", ((GlobalVars.getSDKVersion() != null) ? GlobalVars.getSDKVersion() : ""));
			json.put("system_version", GlobalVars.getSystemVersion());
			json.put("platform", "Android");
			json.put("device_model", ((GlobalVars.getDeviceName() != null) ? GlobalVars.getDeviceName() : ""));
			
			JSONArray jsonArray = new JSONArray();
			
			@SuppressWarnings("unchecked")
			ArrayList<LogItem> logItemsClone = (ArrayList<LogItem>) mLogItems.clone();
			for (LogItem logItem : logItemsClone) {
				JSONObject jsonObject = logItem.toJSONObject();
				if (jsonObject != null) {
					jsonArray.put(jsonObject);
				}
			}
			json.put("events", jsonArray);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
}
