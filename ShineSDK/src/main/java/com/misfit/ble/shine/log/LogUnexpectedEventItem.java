package com.misfit.ble.shine.log;

import org.json.JSONException;
import org.json.JSONObject;

public class LogUnexpectedEventItem extends LogItem {
	
	public static final String EVENT_UNEXPECTED = "unexpected";
	
	public long mTimeMillis;
	public int mState;
	public String mName;
	public JSONObject mValue;
	
	
	public LogUnexpectedEventItem(int profileState, String actionName, JSONObject...jsonObjects) {
		super(EVENT_UNEXPECTED);
		
		mTimeMillis = System.currentTimeMillis();
		mState = profileState;
		mName = actionName;
		if (jsonObjects != null && jsonObjects.length > 0) {
			mValue = jsonObjects[0];
		}
	}
	
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("event", EVENT_UNEXPECTED);

			JSONObject jsonDetails = new JSONObject();
			jsonDetails.put("timestamp", mTimeMillis * 1.0 / 1000);
			jsonDetails.put("state", mState);
			jsonDetails.put("name", mName);
			jsonDetails.put("value", mValue);

			jsonObject.put("requestStarted", jsonDetails);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject; 
	}	
}
