package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OTAEnterRequest extends Request {
	
	public static class Response extends BaseResponse {}
	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	@Override
	public String getRequestName() {
		return "otaEnter";
	}
	
	public byte getParameterId() {
		return Constants.DEVICE_CONFIG_PARAMETER_ID_DIAGNOSTIC_FUNCTIONS;
	}
	
	public byte getFunctionId() {
		return Constants.DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_OTA_ENTER;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest() {
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = getParameterId();
		byte functionId = getFunctionId();
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, functionId);
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		mResponse = parseResponse(bytes);
		mIsCompleted = true;
	}
	
	protected Response parseResponse(byte[] bytes) {
		Response response = new Response();
		
		response.result = validateResponse(bytes, Constants.DEVICE_CONFIG_OPERATION_RESPONSE, getParameterId());
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 3 || Constants.DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_OTA_ENTER_RESPONSE != bytes[2]) {
				response.result = Constants.RESPONSE_ERROR;
			}
		}
		return response;
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
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
