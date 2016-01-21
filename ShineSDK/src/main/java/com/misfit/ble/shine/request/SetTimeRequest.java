package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetTimeRequest extends Request {
	
	private long mTimestamp;
	private int mPartialSeconds;
	private short mTimezoneOffsetInMinutes;
	
	@Override
	public String getRequestName() {
		return "setTime";
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
		return Constants.DEVICE_CONFIG_PARAMETER_ID_TIME;
	}
	
	public void buildRequest(long timestamp, int partialSeconds, short timezoneOffsetInMinutes) {
		mTimestamp = timestamp;
		mPartialSeconds = partialSeconds;
		mTimezoneOffsetInMinutes = timezoneOffsetInMinutes;
		
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = getParameterId();
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.putInt(2, Convertor.unsignedIntFromLong(timestamp));
		byteBuffer.putShort(6, Convertor.unsignedShortFromInteger(partialSeconds));
		byteBuffer.putShort(8, timezoneOffsetInMinutes);
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void buildRequestWithParams(String paramsString) {
		String[] params = paramsString.split(",");
		if (params.length != 3)
			return;
		
		long timestamp = Long.parseLong(params[0]);
		int partialSeconds = Integer.parseInt(params[1]);
		short timezoneOffsetInMinutes = Short.parseShort(params[2]);
		buildRequest(timestamp, partialSeconds, timezoneOffsetInMinutes);
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("timestamp", mTimestamp);
			json.put("partialSeconds", mPartialSeconds);
			json.put("timezoneOffsetInMinutes", mTimezoneOffsetInMinutes);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	public String getParamsHint() {
		return "timestamp:1379142000,\npartialSeconds:0,\ntimezone:420";
	}
	
	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
