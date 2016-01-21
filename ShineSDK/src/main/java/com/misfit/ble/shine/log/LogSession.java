package com.misfit.ble.shine.log;

import com.misfit.ble.sdk.GlobalVars;
import com.misfit.ble.setting.SDKSetting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class LogSession {
	
	private String mUserId;
	
	private long mStartTime;
	private long mEndTime;
	
	private String mSerialNumber;
	private String mFirmwareVersion;
	private String mModelNumber;
	private ArrayList<LogItem> mLogItems;
	private boolean mIsLogOngoing;
	
	public LogSession(String serialNumber) {
		mSerialNumber = serialNumber;
		mLogItems = new ArrayList<LogItem>();
		mUserId = SDKSetting.getUserId();
		mIsLogOngoing = false;
	}
	
	public String getName() {
		return String.format(Locale.US, "sync%s%d%s%d",
				LogManager.SESSION_NAME_COMPONENT_SEPARATOR,
				mStartTime, 
				LogManager.SESSION_NAME_COMPONENT_SEPARATOR, 
				System.identityHashCode(this));
	}
	
	public void start() {
		mStartTime = System.currentTimeMillis();
		mIsLogOngoing = true;
	}
	
	public void stop() {
		mEndTime = System.currentTimeMillis();
		mIsLogOngoing = false;
	}
	
	public boolean isLogOngoing() {
		return mIsLogOngoing;
	}

	public boolean hasLogItem() {
		return !mLogItems.isEmpty();
	}

	public String getFirmwareVersion() {
		return mFirmwareVersion;
	}
	
	public void setFirmwareVersion(String firmwareVersion) {
		mFirmwareVersion = firmwareVersion;
	}
	
	public String getModelNumber() {
		return mModelNumber;
	}
	
	public void setModelNumber(String modelNumber) {
		mModelNumber = modelNumber;
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
	
	public void clearLogItems() {
		mLogItems.clear();
	}
	
	public void addLogItem(LogItem logItem) {
		if (mLogItems.contains(logItem))
			return;
		
		mLogItems.add(logItem);
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		try {
			json.put("user_id", ((mUserId != null) ? mUserId : ""));
			json.put("start_at", mStartTime / 1000);
			json.put("end_at", mEndTime / 1000);
			json.put("sdk_version", ((GlobalVars.getSDKVersion() != null) ? GlobalVars.getSDKVersion() : ""));
			json.put("system_version", GlobalVars.getSystemVersion());
			json.put("platform", "Android");
			json.put("device_model", ((GlobalVars.getDeviceName() != null) ? GlobalVars.getDeviceName() : ""));
			json.put("serial_number", ((mSerialNumber != null) ? mSerialNumber : ""));
			json.put("firmware_version", ((mFirmwareVersion != null) ? mFirmwareVersion : ""));
			
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
