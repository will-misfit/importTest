package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Request {
	
	// iVars
	protected boolean mIsCompleted;
	protected byte[] mRequestData;
	
	public String getRequestName() {
		return null;
	}
	
	abstract public String getCharacteristicUUID();
	
	public int getTimeOut() {
		return 3000;
	}
	
	public boolean getIsCompleted() {
		return mIsCompleted;
	}
	
	public byte[] getRequestData() {
		return mRequestData;
	}
	
	protected static byte validateResponse(byte[] bytes, byte expectedOperation) {
		if (bytes.length <= 0) {
			return Constants.RESPONSE_MISMATCHED;
		}
		
		byte operationCode = bytes[0];
		if (operationCode != expectedOperation) {
			return Constants.RESPONSE_MISMATCHED;
		}
		return Constants.RESPONSE_SUCCESS;
	}
	
	protected static byte validateResponse(byte[] bytes, byte expectedOperation, byte expectedParameterId) {
		byte status = validateResponse(bytes, expectedOperation);
		if (status == Constants.RESPONSE_SUCCESS) {
			if (bytes.length <= 1) {
				status = Constants.RESPONSE_MISMATCHED;
			} else {
				byte parameterId = bytes[1];
				if (parameterId != expectedParameterId) {
					status = Constants.RESPONSE_MISMATCHED;
				}
			}
		}
		return status;
	}
	
	protected static byte validateResponse(byte[] bytes, byte expectedOperation, byte expectedParameterId, byte expectedSettingId) {
		byte status = validateResponse(bytes, expectedOperation, expectedParameterId);
		if (status == Constants.RESPONSE_SUCCESS) {
			if (bytes.length <= 2) {
				status = Constants.RESPONSE_MISMATCHED;
			} else {
				byte settingId = bytes[2];
				if (settingId != expectedSettingId) {
					status = Constants.RESPONSE_MISMATCHED;
				}
			}
		}
		return status;
	}

	public void buildRequestWithParams(String paramsString) {}
	
	public void onRequestSent(int status) {}
	
	public void handleResponse(String characteristicUUID, byte[] bytes) {}
	
	public JSONObject getNameJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("name", getRequestName());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public JSONObject getRequestDescriptionJSON() { return null; }
	
	public JSONObject getRequestDataJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("data", Convertor.bytesToString(mRequestData));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public JSONObject getResponseDescriptionJSON() { return null; }
	
	public String getParamsHint() { return null; }
	
	public boolean isWaitingForResponse() {
		return mIsCompleted == false;
	}

	public BaseResponse getResponse() { return null; }
}
