package com.misfit.ble.shine.request;

import com.misfit.ble.setting.flashlink.FlashButtonMode;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetFlashButtonModeRequest extends Request {
	
	private boolean mActivityTrackingEnabled;
	private FlashButtonMode mFlashButtonMode;
	
	@Override
	public String getRequestName() {
		return "setFlashButtonMode";
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
		return Constants.DEVICE_SETTING_ID_DEVICE_MODE;
	}
	
	public void buildRequest(boolean activityTrackingEnabled, FlashButtonMode flashButtonMode) {
		mActivityTrackingEnabled = activityTrackingEnabled;
		mFlashButtonMode = flashButtonMode;
		
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = getParameterId();
		byte settingsId = getSettingId();
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, settingsId);
		byteBuffer.put(3, (byte) (activityTrackingEnabled ? 0x01 : 0x00));
		byteBuffer.put(4, Convertor.unsignedByteFromShort(flashButtonMode.getId()));
		
		mRequestData = byteBuffer.array();
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		String[] params = paramsString.split(",");
		if (params.length != 2)
			return;

		boolean enable = Boolean.parseBoolean(params[0]);
		short mode = Short.valueOf(params[1]);
		buildRequest(enable, FlashButtonMode.get(mode));
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("activityTrackingEnabled", mActivityTrackingEnabled);
			json.put("flashButtonMode", mFlashButtonMode.getId());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	public String getParamsHint() {
		return "trackingEnabled, buttonMode";
	}
	
	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
