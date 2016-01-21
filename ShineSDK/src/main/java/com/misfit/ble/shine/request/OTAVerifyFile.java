package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OTAVerifyFile extends Request {
	
	public static class Response extends BaseResponse {
		public byte status;
	}
	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	@Override
	public String getRequestName() {
		return "otaVerifyFile";
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest() {
		byte operation = Constants.FILE_CONTROL_OPERATION_OTA_VERIFY_FILE;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		byteBuffer.putShort(1, Constants.FILE_HANDLE_OTA);
				
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		mResponse = parseResponse(bytes);
		mIsCompleted = true;
	}
	
	protected Response parseResponse(byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.FILE_CONTROL_OPERATION_OTA_VERIFY_FILE_RESPONSE);
		response.status = 0;
		
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 4) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				short fileHandle = byteBuffer.getShort(1);
				response.status = byteBuffer.get(3);
				if (Constants.FILE_HANDLE_OTA != fileHandle || Constants.FILE_CONTROL_RESPONSE_SUCCESS != response.status) {
					response.result = Constants.RESPONSE_ERROR;
				}
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
				json.put("status", mResponse.status);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

}
