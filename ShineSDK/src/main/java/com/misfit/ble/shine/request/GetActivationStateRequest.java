package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GetActivationStateRequest extends Request {
	
	private static final byte ACTIVATED = 0x01;

	public static class Response extends BaseResponse{
		public boolean activated;
	}

	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	@Override
	public String getRequestName() {
		return "getActivationState";
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public byte getParameterId() {
		return Constants.DEVICE_CONFIG_PARAMETER_ID_DIAGNOSTIC_FUNCTIONS;
	}
	
	public void buildRequest() {
		byte operation   = Constants.DEVICE_CONFIG_OPERATION_GET;
		byte parameterId = getParameterId();
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(0, operation);
        byteBuffer.put(1, parameterId);
        byteBuffer.put(2, Constants.DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_ACTIVATE);
        
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
				response.activated = (ACTIVATED == byteBuffer.get(2));
			}
		}
		mResponse = response;
		mIsCompleted = true;
	}

	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			if (mResponse != null) {
				json.put("result", mResponse.result);
				json.put("activated", mResponse.activated);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
