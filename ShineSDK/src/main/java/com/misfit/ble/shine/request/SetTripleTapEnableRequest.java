package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetTripleTapEnableRequest extends Request {
	
	private boolean mTripleTapEnable;
	
	@Override
	public String getRequestName() {
		return "setTripleTapEnable";
	}
	
	@Override
	public int getTimeOut() {
		return 0;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public byte getParameterId() {
		return Constants.DEVICE_CONFIG_PARAMETER_ID_DEVICE_SETTINGS_IN_OUT;
	}
	
	public byte getSettingId() {
		return Constants.DEVICE_SETTING_ID_TRIPLE_TAP_ENABLE;
	}
	
	public void buildRequest(boolean enable) {
		mTripleTapEnable = enable;
		
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = getParameterId();
		byte settingsId = getSettingId();
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, settingsId);
		byteBuffer.put(3, (byte) (enable ? 0x01 : 0x00));
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void buildRequestWithParams(String paramsString) {
		String[] params = paramsString.split(",");
		if (params.length != 1)
			return;
		
		boolean enable = Boolean.parseBoolean(params[0]);
		buildRequest(enable);
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("tripleTapEnable", mTripleTapEnable);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	public String getParamsHint() {
		return "true/false/1/0";
	}
	
	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
