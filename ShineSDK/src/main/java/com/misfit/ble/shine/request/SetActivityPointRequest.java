package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetActivityPointRequest extends Request {
	
	private long mActivityPoint;
	
	@Override
	public String getRequestName() {
		return "setActivityPoint";
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
		return Constants.DEVICE_CONFIG_PARAMETER_ID_ACTIVITY_POINT;
	}
	
	public void buildRequest(long activityPoint) {
		mActivityPoint = activityPoint;
		
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = getParameterId();
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.putInt(2, Convertor.unsignedIntFromLong(activityPoint << 8));
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void buildRequestWithParams(String paramsString) {
		String[] params = paramsString.split(",");
		if (params.length != 1)
			return;
		
		long activityPoint = Long.parseLong(params[0]);
		buildRequest(activityPoint);
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("activityPoint", mActivityPoint);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	public String getParamsHint() {
		return "points";
	}
	
	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
