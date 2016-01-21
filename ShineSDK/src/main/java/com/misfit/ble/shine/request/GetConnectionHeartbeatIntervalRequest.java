package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GetConnectionHeartbeatIntervalRequest extends Request {
	
	public static class Response extends BaseResponse {
		public long connectionHeartbeatInterval; // unsigned int
	}
	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	@Override
	public String getRequestName() {
		return "getConnectionHeartbeatInterval";
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest() {
		byte operation = Constants.DEVICE_CONFIG_OPERATION_GET;
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_STREAMING_CONFIGURATION;
		byte settingId = Constants.STREAMING_SETTING_ID_CONNECTION_HEARTBEAT_INTERVAL;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, settingId);
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, 
				Constants.DEVICE_CONFIG_OPERATION_RESPONSE, 
				Constants.DEVICE_CONFIG_PARAMETER_ID_STREAMING_CONFIGURATION);
		
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 6) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				response.connectionHeartbeatInterval = (long)byteBuffer.getInt(2);
			}
		}
		mResponse = response;
		mIsCompleted = true;
	}
	
	@Override
	public void buildRequestWithParams(String paramsString) {
		buildRequest();
	}
	
	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			if (mResponse != null) {
				json.put("result", mResponse.result);
				json.put("connectionHeartbeatInterval", mResponse.connectionHeartbeatInterval);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

}
