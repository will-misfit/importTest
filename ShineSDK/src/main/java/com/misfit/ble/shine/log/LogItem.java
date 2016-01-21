package com.misfit.ble.shine.log;

import org.json.JSONException;
import org.json.JSONObject;

public class LogItem {
	
	protected String mEventName;

	public String getEventName() {
		return mEventName;
	}
	
	public LogItem(String eventName) {
		mEventName = eventName;
	}
	
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("event", mEventName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
}
