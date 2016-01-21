package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FileEraseRequest extends Request {

	private Response mResponse;
	private short mFileHandle;

	@Override
	public Response getResponse() {
		return mResponse;
	}

	public static class Response extends BaseResponse {
		public byte status;
	}
	
	@Override
	public String getRequestName() {
		return "fileErase";
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest(short fileHandle) {
		mFileHandle = fileHandle;
		
		byte operation = Constants.FILE_CONTROL_OPERATION_ERASE;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		byteBuffer.putShort(1, fileHandle);
        
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void buildRequestWithParams(String paramsString) {
		if (paramsString == null || paramsString.length() <= 0)
			return;
		
		String[] params = paramsString.split(",");
		if (params.length != 1)
			return;
		
		if (params[0].startsWith("0x"))
			params[0] = params[0].substring(2);
		
		short fileHandle = Short.parseShort(params[0], 16);
		buildRequest(fileHandle);
	}
	
	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		mResponse = parseResponse(bytes);
		mIsCompleted = true;
	}
	
	private Response parseResponse(byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.FILE_CONTROL_OPERATION_ERASE_RESPONSE);
		response.status = 0;
		
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 4) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				short handle = byteBuffer.getShort(1);
				
				response.status = byteBuffer.get(3);
				if (handle != mFileHandle || response.status != Constants.FILE_CONTROL_RESPONSE_SUCCESS) {
					response.result = Constants.RESPONSE_ERROR;
				}
			}
		}
		return response;
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("fileHandle", Convertor.unsignedShortToInteger(mFileHandle));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
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

	@Override
	public String getParamsHint() {
		return "handle:0x0100";
	}
}
