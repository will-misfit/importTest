package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GetConnectionParameterRequest extends Request {

	public static class Response extends BaseResponse {
		public byte status;
		public double connectionInterval;
		public int connectionLatency;
		public int supervisionTimeout;
	}
	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}

	@Override
	public String getRequestName() {
		return "getConnectionParameter";
	}

	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public byte getParameterId() {
		return Constants.DEVICE_CONFIG_PARAMETER_ID_CONNECTION_PARAMETER_GET;
	}

	public void buildRequest() {
		byte operation = Constants.DEVICE_CONFIG_OPERATION_GET;
		byte parameterId = getParameterId();

		ByteBuffer byteBuffer = ByteBuffer.allocate(10);
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
			if (bytes.length < 9) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				response.status = byteBuffer.get(2);
				response.connectionInterval = byteBuffer.getShort(3) * 1.25;
				response.connectionLatency = byteBuffer.getShort(5);
				response.supervisionTimeout = byteBuffer.getShort(7) * 10;
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
				json.put("status", mResponse.status);
				json.put("connectionInterval", mResponse.connectionInterval);
				json.put("connectionLatency", mResponse.connectionLatency);
				json.put("supervisionTimeout", mResponse.supervisionTimeout);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
