package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GetBatteryRequest extends Request {
	public static class Response extends BaseResponse {
		public short batteryLevel; 
	}
	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	@Override
	public String getRequestName() {
		return "getBattery";
	}
	
	public byte getParameterId() {
		return Constants.DEVICE_CONFIG_PARAMETER_ID_BATTERY_LEVEL;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest() {
		byte operation = Constants.DEVICE_CONFIG_OPERATION_GET;
		byte parameterId = getParameterId();
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(2);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.DEVICE_CONFIG_OPERATION_RESPONSE, getParameterId());
		
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 3) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				response.batteryLevel = Convertor.unsignedByteToShort(byteBuffer.get(2));
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
				json.put("batteryLevel", mResponse.batteryLevel);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
