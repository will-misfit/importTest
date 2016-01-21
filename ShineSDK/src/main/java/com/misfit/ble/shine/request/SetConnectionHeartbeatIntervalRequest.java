package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetConnectionHeartbeatIntervalRequest extends Request {

	private long mConnectionHeartbeatInterval;
	
	@Override
	public String getRequestName() {
		return "setConnectionHeartbeatInterval";
	}
	
	@Override
	public int getTimeOut() {
		return 0;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest(long connectionHeartbeatInterval) {
		mConnectionHeartbeatInterval = connectionHeartbeatInterval;
		
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_STREAMING_CONFIGURATION;
		byte settingId = Constants.STREAMING_SETTING_ID_CONNECTION_HEARTBEAT_INTERVAL;
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(7);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, settingId);
		byteBuffer.putInt(3, Convertor.unsignedIntFromLong(mConnectionHeartbeatInterval));
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("connectionHeartbeatInterval", mConnectionHeartbeatInterval);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	public String getParamsHint() {
		return "connectionHeartbeatInterval";
	}
	
	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}

}
